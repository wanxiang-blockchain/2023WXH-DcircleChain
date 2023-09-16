package com.anywithyou.stream;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


class ClientImpl {

  private void asyncErr(Client.ErrorHandler handler, Error error, boolean isConn) {
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        handler.onFailed(error, isConn);
      }
    });
  }

  private void asyncConnectSuc(Client.ConnectHandler handler) {
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        handler.onSuccess();
      }
    });
  }

  private void asyncSendSuc(Client.ResponseHandler handler, byte[] response) {
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        handler.onSuccess(response);
      }
    });
  }

  // 直接异步。当在connect的onSuccess()中直接调用close()时，会出现这里被再次调用的情况
  // 因为waitingConnects.clear()还没有执行，造成connect()的回调被调用了两次。
  private void runWaiting(Error error) {
    for(Client.ConnectHandler handler: waitingConnects) {
      if (error == null) {
        asyncConnectSuc(handler);
      } else {
        asyncErr(handler, error, true);
      }
    }

    waitingConnects.clear();
  }

  private void doAllRequest(FakeHttp.Response response) {
    for (Map.Entry<Long, ResponseHandler> entry : allRequests.entrySet()) {
      entry.getValue().onResponse(response);
    }
    allRequests.clear();
  }

  private long reqId() {
    reqId++;
    if (reqId < reqIdStart || reqId > Integer.MAX_VALUE) {
      reqId = reqIdStart;
    }
    return reqId;
  }

  // 注意：connect onConnected close onClose 之间的时序问题
  // 如果执行了connect，再执行close，然后再被调用onConnected,
  // 返给上层的信息也不能是连接成功的信息；
  // 如果执行了close，再执行connect成功了，最后给用户的也只能是连接成功

  // 无论当前连接状态，都可以重复调用
  public void connect(Client.ConnectHandler handler) {
    if (this.connState == ConnectState.Connected) {
      asyncConnectSuc(handler);
      return;
    }

    // 防止Net的回调实现为同步，而不满足这一层回调都必须异步的要求
    FinalValue<Boolean> isAsync = new FinalValue<>(false);

    this.waitingConnects.add(new Client.ConnectHandler() {
      @Override
      public void onSuccess() {
        if (!isAsync.value) {
          asyncConnectSuc(handler);
          return;
        }
        handler.onSuccess();
      }

      @Override
      public void onFailed(Error error, boolean isConn) {
        if (!isAsync.value) {
          asyncErr(handler, error, isConn);
          return;
        }
        handler.onFailed(error, isConn);
      }
    });

    if (connState == ConnectState.Connecting) {
      return;
    }

    connState = ConnectState.Connecting;

    // 底层实现不一定每次connect都回调 onConnected，
    // 所以上层通过ConnectState的判断作了逻辑加强，无论底层是否调用都需要
    // 确保逻辑正确
    this.net.connect(config.host, config.port, config.tls);
    isAsync.value = true;
  }

  // 无论当前连接状态，都可以重复调用
  public void close() {
    if (connState == ConnectState.NotConnect) {
      return;
    }

    runWaiting(new Error("connection closed by self"));
    connState = ConnectState.Closing;
    net.close();
  }

  // 如果没有连接成功，直接返回失败
  public void send(byte[] data, Map<String, String> headers, Client.ResponseHandler handler) {
    if (connState != ConnectState.Connected) {
      asyncErr(handler, new Error("not connected"), true);
      return;
    }

    long reqId = reqId();

    Handler mainHandler = new Handler();

    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            allRequests.remove(reqId);
            handler.onFailed(new Error("request timeout"), false);
          }
        });
      }
    };
    timer.schedule(timerTask, config.requestTimeout.milliSecond());

    // 防止Net的回调实现为同步，而不满足这一层回调都必须异步的要求
    FinalValue<Boolean> isAsync = new FinalValue<>(false);

    allRequests.put(reqId, new ResponseHandler() {
      @Override
      public void onResponse(FakeHttp.Response response) {
        timerTask.cancel();

        if (response.status != FakeHttp.Response.Status.Ok) {
          if (!isAsync.value) {
            asyncErr(handler, new Error(new String(response.data)), false);
            return;
          }
          handler.onFailed(new Error(new String(response.data)), false);
          return;
        }

        if (!isAsync.value) {
          asyncSendSuc(handler, response.data);
          return;
        }
        handler.onSuccess(response.data);
      }
    });

    try {
      FakeHttp.Request request = new FakeHttp.Request(data, headers);
      request.setReqId(reqId);
      request.sendTo(net);
      isAsync.value = true;
    } catch (Exception e) {
      allRequests.remove(reqId);
      timerTask.cancel();
      asyncErr(handler, new Error(e), true);
    }
  }

  public void updateNetConnectTime() {
    this.net.setConfig(new Net.Config(config.connectTimeout, config.heartbeatTime, config.frameTimeout));
  }

  public void setNet(Net net) {
    this.net = net;
    this.net.setConfig(new Net.Config(config.connectTimeout, config.heartbeatTime, config.frameTimeout));
    this.net.setDelegate(new Net.Delegate() {
      @Override
      public void onConnected() {
        if (connState != ConnectState.Connecting) {
          return;
        }
        connState = ConnectState.Connected;
        runWaiting(null);
      }

      @Override
      public void onMessage(byte[] message) {
        FakeHttp.Response response = null;
        try {
          response = new FakeHttp.Response(message);
        } catch (Exception e) {
          String str = e.getMessage();
          if (str == null) {
            str = "fakeHttp response error";
          }
          response = FakeHttp.Response.fromError(reqId, str);
        }

        if (response.isPush()) {
          // push ack 强制写给网络，不计入并发控制
          net.sendForce(response.newPushAck());
          pushCallback.onPush(response.data);
          return;
        }

        net.receivedOneResponse();

        ResponseHandler r = allRequests.get(response.reqID);
        if (r == null) {
          Log.e("client", "onMessage: not find response handler for " + response.reqID);
          return;
        }
        r.onResponse(response);
        allRequests.remove(response.reqID);
      }

      @Override
      public void onClosed(String reason) {
        if (connState == ConnectState.NotConnect) {
          return;
        }
        Log.i("stream.ClientImpl", "onClosed: " + reason);

        // 上一次连接后，所有的请求都需要回应
        doAllRequest(FakeHttp.Response.fromError(0, reason));

        // 正在连接中，不再做关闭的状态处理 (可能是调用了close(), 马上就调用了 connect() 的情况)
        if (connState == ConnectState.Connecting) {
          return;
        }

        if (connState == ConnectState.Connected) {
          peerClosedCallback.onPeerClosed();
        }
        connState = ConnectState.NotConnect;
      }

      @Override
      public void onError(Error error) {
        // 关闭中的错误，暂都默认不处理。
        if (connState == ConnectState.Closing || connState == ConnectState.NotConnect) {
//          Log.d("ClientImpl", "onError: ", error);
          return;
        }

        Log.e("stream.ClientImpl", "onError: ", error);

        // 不管什么错误，都需要清除等待中的连接
        runWaiting(error);

        // 不确定onError的时候是否已经自动会执行onClosed，这里再次明确执行一次，
        // 但是要注意onClosed的逻辑多次执行也要没有问题
        this.onClosed(error.getMessage());

        // 发生了错误，就要执行一次关闭的操作
        // 前面的其他操作可能 更改了connState，这里做二次确认
        if (connState == ConnectState.Connecting || connState == ConnectState.Connected) {
          connState = ConnectState.Closing;
        }
        net.close();
      }
    });
  }

  public Option.Value config;
  public Client.PushCallback pushCallback;
  public Client.PeerClosedCallback peerClosedCallback;

  private Net net;
  private final List<Client.ConnectHandler> waitingConnects = new ArrayList<>();

  enum ConnectState {
    NotConnect, Connecting, Connected, Closing
  }
  private ConnectState connState = ConnectState.NotConnect;

  static final private long reqIdStart = 10;
  private long reqId = reqIdStart;

  private interface ResponseHandler {
    void onResponse(FakeHttp.Response response);
  }
  private final Map<Long, ResponseHandler> allRequests = new HashMap<>();

  private final Timer timer = new Timer();
}
