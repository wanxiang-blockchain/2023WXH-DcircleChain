package com.anywithyou.stream;

import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import kotlin.Function;
import kotlin.jvm.functions.Function1;

public class Client {

  public Client(Option ...options) {
    Option.Value value = new Option.Value();
    for (Option op : options) {
      op.configValue(value);
    }

    this.impl = new ClientImpl();

    this.impl.config = value;
  //默认push，调用hanlde
    this.impl.pushCallback = new PushCallback() {
      @Override
      public void onPush(byte[] data) {
        Log.e("Client.onPush", "not set push callback");
        Log.d("stream_tracker","onPush+"+new String(data));
        JavaToKotlinMiddleWare.INSTANCE.paresPushCallback(data);

      }
    };

    this.impl.peerClosedCallback = new PeerClosedCallback() {
      @Override
      public void onPeerClosed() {
          Log.d("stream_tracker","onPeerClosed");
      }
    };
    impl.setNet(new LenContent());
  }

  public void updateOptions(Option ...options) {
    for (Option op : options) {
      op.configValue(this.impl.config);
    }
    this.impl.updateNetConnectTime();
  }



  public interface PushCallback {
    void onPush(byte[] data);
  }

  /**
   * 设置新的push
   * @param delegate
   */
  public void setPushCallback(PushCallback delegate) {
    this.impl.pushCallback = new PushCallback() {
      @Override
      public void onPush(byte[] data) {
        // 异步回调push
        new Handler().post(new Runnable() {
          @Override
          public void run() {
            delegate.onPush(data);
          }
        });
      }
    };
  }


  public interface PeerClosedCallback {
    void onPeerClosed();
  }


  public void setPeerClosedCallback(PeerClosedCallback delegate) {
    this.impl.peerClosedCallback = new PeerClosedCallback() {
      @Override
      public void onPeerClosed() {
        // 异步回调 PeerClosedCallback
        new Handler().post(new Runnable() {
          @Override
          public void run() {
            delegate.onPeerClosed();
          }
        });
      }
    };
  }




  public interface ErrorHandler {
    void onFailed(Error error, boolean isConnError);
  }
  public interface ResponseHandler extends ErrorHandler{
    void onSuccess(byte[] response);
  }
  // 自动连接并发送数据
  public void Send(byte[] data, Map<String, String> headers, ResponseHandler handler) {
    connect(new ConnectHandler() {
      @Override
      public void onSuccess() {
        onlySend(data, headers, handler);
      }

      @Override
      public void onFailed(Error error, boolean isConn) {
        handler.onFailed(error, isConn);
      }
    });
  }

  public interface RecoverHandler extends ConnectHandler {}
  public void Recover(RecoverHandler handler) {
    connect(handler);
  }

  // 暂不暴露以下接口，需要进一步验证其稳定性

  private void setNet(Net net) {
    this.impl.setNet(net);
  }

  public interface ConnectHandler extends ErrorHandler{
    void onSuccess();
  }
  // 无论当前连接状态，都可以重复调用，如果连接成功，确保最后的状态为连接
  // 无论多少次调用，最后都只有一条连接
  public void connect(ConnectHandler handler) {
    impl.connect(handler);
  }

  // 无论当前连接状态，都可以重复调用，并确保最后的状态为关闭
  void close() {
    impl.close();
  }


  // 如果还没有连接，返回失败
  void onlySend(byte[] data, Map<String, String> headers, ResponseHandler handler) {
    impl.send(data, headers, handler);
  }


  private final ClientImpl impl;
}
