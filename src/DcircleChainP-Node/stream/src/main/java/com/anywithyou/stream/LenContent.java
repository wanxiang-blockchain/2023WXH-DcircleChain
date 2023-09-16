package com.anywithyou.stream;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


/**
 lencontent protocol:

 1, handshake protocol:

                        client ------------------ server
                        |                          |
                        |                          |
                        ABCDEF (A^...^F = 0xff) --->  check(A^...^F == 0xff) --- N--> over
                        (A is version)
                        |                          |
                        |                          |Y
                        |                          |
 version 1:   set client heartbeat  <----- HeartBeat_s (2 bytes, net order)
 version 2:       set config     <-----  HeartBeat_s | FrameTimeout_s | MaxConcurrent | MaxBytes | connect id
                                          HeartBeat_s: 2 bytes, net order
                                          FrameTimeout_s: 1 byte
                                          MaxConcurrent: 1 byte
                                          MaxBytes: 4 bytes, net order
                                          connect id: 8 bytes, net order
                        |                          |
                        |                          |
                        |                          |
                        data      <-------->       data


 2, data protocol:
    1) length | content
    length: 4 bytes, net order; length=sizeof(content)+4; length=0 => heartbeat

 */


class LenContent implements Net {

  static private class Connection {

    interface DelegateHandler<T> {
      void run(T d);
    }

    private DelegateHandler<Socket> onConnected;
    private DelegateHandler<String> onClosed;
    private DelegateHandler<Error> onError;
    private DelegateHandler<byte[]> onMessage;

    private Socket socket;
    private final Config config;
    private final List<byte[]> sendData = new LinkedList<>(); // send thread queue

    private String connectID;

    private Thread inputThread;
    private Thread outputThread;

    private int concurrent = 0;
    private final List<byte[]> waitForSending = new LinkedList<>();

    private final Timer timer = new Timer();

    Connection(Config config, Delegate delegate) {
      this.config = config;
      this.onConnected = new DelegateHandler<>() {
          @Override
          public void run(Socket _socket) {
              socket = _socket;
              initInputHandler();
              initOutputHandler();
              delegate.onConnected();
              onConnected = new DelegateHandler<>() {
                  @Override
                  public void run(Socket d) {
                  }
              };
          }
      };
      this.onClosed = new DelegateHandler<>() {
          @Override
          public void run(String d) {
              delegate.onClosed(d);
              onClosed = new DelegateHandler<>() {
                  @Override
                  public void run(String d) {
                  }
              };
          }
      };
      this.onError = new DelegateHandler<>() {
          @Override
          public void run(Error d) {
              delegate.onError(d);
              onError = new DelegateHandler<>() {
                  @Override
                  public void run(Error d) {
                  }
              };
          }
      };
      this.onMessage = new DelegateHandler<>() {
          @Override
          public void run(byte[] d) {
              delegate.onMessage(d);
          }
      };
    }

    private void closeSocketAtMain(Socket socket) {
      Handler handler = new Handler();
      Log.i("lencontent", "close: id = " + connectID);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            socket.close();
            handleClose(handler);
          } catch (IOException e) {
            handleError(handler, new Error(e));
          }
        }
      }).start();
    }

    public void close() {
      // close执行的时候，可能connect还没有成功，就不能真正关闭socket，
      // 所以需要需要改写onConnected变量，实际执行的是关闭socket的操作
      // 而不是执行connect的后续操作。
      onConnected = new DelegateHandler<>() {
          @Override
          public void run(Socket socket) {
              closeSocketAtMain(socket);
          }
      };

      synchronized (sendData) {
        sendData.clear();
      }

      if (inputThread != null) {
        inputThread.interrupt();
      }
      if (outputThread != null) {
        outputThread.interrupt();
      }

      if (socket != null) {
        closeSocketAtMain(socket);
      }

      timer.cancel();
    }

    public void invalidAndClose() {
      // 失效后，不再向delegate发消息
      onMessage = new DelegateHandler<>() {
          @Override
          public void run(byte[] d) {

          }
      };
      onError = new DelegateHandler<>() {
          @Override
          public void run(Error d) {

          }
      };
      onClosed = new DelegateHandler<>() {
          @Override
          public void run(String d) {

          }
      };
      onConnected = new DelegateHandler<>() {
          @Override
          public void run(Socket d) {

          }
      };

      close();
    }

    // 其他线程调用
    private void handleError(Handler handler, Error error) {
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
        // 是否抛错，后续再细究
      }
      handler.post(new Runnable() {
        @Override
        public void run() {
          onError.run(error);
        }
      });
    }

    private void handleErrorAtMainThread(Error error) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
            // 是否抛错，后续再细究
          }
        }
      }).start();

      onError.run(error);
    }

    // 其他线程调用
    private void handleClose(Handler handler) {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
        // 是否抛错，后续再细究
      }
      handler.post(new Runnable() {
        @Override
        public void run() {
          onClosed.run("closed by self");
        }
      });
    }

    private byte[] getSendData() throws Exception {
      synchronized (sendData) {
        while (sendData.isEmpty()) {
          sendData.wait();
        }
        return sendData.remove(0);
      }
    }

    // 只能在输出线程中调用
    private TimerTask outputHeartbeatTimer(TimerTask timerTask) {
      timerTask.cancel();
      TimerTask ret = new TimerTask() {
        @Override
        public void run() {
          byte[] heart = new byte[4];
          for (int i = 0; i < 4; ++i) {
            heart[i] = 0;
          }
          synchronized (sendData) {
            sendData.add(heart);
            sendData.notify();
            Log.d("LenContent", "Heartbeat: send heartbeat to server");
          }
        }
      };
      timer.schedule(ret, config.HearBeatTime.milliSecond(), config.HearBeatTime.milliSecond());

      return ret;
    }

    // 只能在输出线程中调用
    private TimerTask outputFrameTimer(Handler handler, TimerTask timerTask){
      timerTask.cancel();
      TimerTask ret = new TimerTask() {
        @Override
        public void run() {
          handleError(handler, new Error("send data timeout"));
        }
      };
      timer.schedule(ret, config.FrameTimeout.milliSecond());

      return ret;
    }

    private void initOutputHandler() {
      OutputStream outputStream;
      try {
        outputStream = socket.getOutputStream();
      } catch (IOException e) {
        e.printStackTrace();
        handleErrorAtMainThread(new Error(e));
        return;
      }
      Handler handler = new Handler();

      outputThread = new Thread(new Runnable() {
        @Override
        public void run() {
          TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

            }
          };
          while (true) {
            timerTask = outputHeartbeatTimer(timerTask);

            byte[] data;
            try {
              data = getSendData();
              timerTask = outputFrameTimer(handler, timerTask);
              outputStream.write(data);
            } catch (Exception e) {
//              e.printStackTrace();
              handleError(handler, new Error(e));
              break;
            }
          }
          timerTask.cancel();
        }
      });
      outputThread.start();
    }

    // 只能在输入线程中调用
    private TimerTask inputHeartbeatTimer(Handler handler, TimerTask timerTask) {
      timerTask.cancel();
      TimerTask ret = new TimerTask() {
        @Override
        public void run() {
          handleError(handler, new Error("heartbeat timeout"));
        }
      };
      timer.schedule(ret, 2*config.HearBeatTime.milliSecond());

      return ret;
    }

    // 只能在输入线程中调用
    private TimerTask inputFrameTimer(Handler handler, TimerTask timerTask) {
      timerTask.cancel();
      TimerTask ret = new TimerTask() {
        @Override
        public void run() {
          handleError(handler, new Error("receive data timeout"));
        }
      };
      timer.schedule(ret, config.FrameTimeout.milliSecond());

      return ret;
    }

    private void initInputHandler() {
      InputStream inputStream;
      try {
        inputStream = socket.getInputStream();
      } catch (IOException e) {
        e.printStackTrace();
        handleErrorAtMainThread(new Error(e));
        return;
      }
      Handler handler = new Handler();

      inputThread = new Thread(new Runnable() {
        @Override
        public void run() {
          TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

            }
          };
          while (true) {
            timerTask = inputHeartbeatTimer(handler, timerTask);
            try {
              byte[] lengthB = new byte[4];
              int pos = 0;

              // 先读一个，表示有数据了
              int n = inputStream.read(lengthB, pos, 1);
              if (n <= 0) {
                handleError(handler, new Error("inputstream read error, maybe connection closed by peer(1)"));
                break;
              }
              pos += n;
              timerTask = inputFrameTimer(handler, timerTask);

              while (4-pos != 0 && n > 0) {
                n = inputStream.read(lengthB, pos, 4-pos);
                pos += n;
              }
              if (n <= 0) {
                handleError(handler, new Error("inputstream read error, maybe connection closed by peer(2)"));
                break;
              }

              pos = 0;
              long length = ((long) (0xff & lengthB[0]) << 24)
                + ((0xff & lengthB[1]) << 16)
                + ((0xff & lengthB[2]) << 8)
                + ((0xff & lengthB[3]));
              if (length == 0) { // heartbeat
                Log.d("LenContent", "Heartbeat: receive heartbeat from server");
                continue;
              }

              length -= 4;
              final byte[] data = new byte[(int) length];
              while (length - pos != 0 && n > 0) {
                timerTask = inputFrameTimer(handler, timerTask);
                n = inputStream.read(data, pos, (int) length - pos);
                timerTask.cancel();
                pos += n;
              }
              if (n <= 0) {
                handleError(handler, new Error("inputstream read error, maybe connection closed by peer(3)"));
                break;
              }

              handler.post(new Runnable() {
                @Override
                public void run() {
                  onMessage.run(data);
                }
              });

            } catch (IOException e) {
              handleError(handler, new Error(e));
              break;
            }
          }
          timerTask.cancel();
        }
      });
      inputThread.start();
    }

    public void connect(String host, int port, boolean tls) {
      Handler handler = new Handler();
      new Thread(new Runnable() {
        @Override
        public void run() {
          Socket socket = new Socket();
          try {
            socket.connect(new InetSocketAddress(host, port), (int) config.ConnectTimeout.milliSecond());

            if (tls) {
              SSLContext context = SSLContext.getInstance("TLS");
              context.init(null, null, null);
              SSLSocketFactory factory = context.getSocketFactory();
              SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, true);
              sslSocket.startHandshake();
              socket = sslSocket;
            }

            // 发握手数据
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(handshake());
            outputStream.flush();
            HandshakeRes handshakeRes = readHandshake(socket.getInputStream());

            final Socket finalSocket = socket;
            handler.post(new Runnable() {
              @Override
              public void run() {
                config.HearBeatTime = handshakeRes.HearBeatTime;
                config.FrameTimeout = handshakeRes.FrameTimeout;
                config.MaxBytes = handshakeRes.MaxBytes;
                config.MaxConcurrent = handshakeRes.MaxConcurrent;
                connectID = handshakeRes.ConnectId;

                Log.i("lencontent", "connect_id = " + connectID);

                onConnected.run(finalSocket);
              }
            });
          } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            try {
              socket.close();
            } catch (IOException ioException) {
              ioException.printStackTrace();
            }
            handleError(handler, new Error(e));
          }
        }
      }).start();
    }

    private byte[] handshake() {
      byte[] handshake = new byte[6];
      new Random().nextBytes(handshake);
      // version is 2
      handshake[0] = 2;
      handshake[5] = (byte) 0xff;
      for (int i = 0; i < 5; ++i) {
        handshake[5] ^= (byte) handshake[i];
      }

      return handshake;
    }

    static class HandshakeRes {
      public Duration HearBeatTime;
      public Duration FrameTimeout; // 同一帧里面的数据延时
      public int MaxConcurrent; // 一个连接上的最大并发
      public long MaxBytes; // 一次数据发送的最大字节数
      public String ConnectId;
    }

    private HandshakeRes readHandshake(InputStream inputStream) throws  IOException {
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          try {
            Log.e("LenContent", "readHandshake: timeout");
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      };
      timer.schedule(timerTask, 30*1000); // 30s

      int pos = 0;
      /*
      HeartBeat_s: 2 bytes, net order
      FrameTimeout_s: 1 byte
      MaxConcurrent: 1 byte
      MaxBytes: 4 bytes, net order
      connect id: 8 bytes, net order
      */

      byte[] handshake = new byte[2 + 1 + 1 + 4 + 8];
      while (handshake.length - pos != 0) {
        int n = inputStream.read(handshake, pos, handshake.length-pos);
        if (n <= 0) {
          throw new IOException("read handshake error, maybe connection closed by peer or timeout");
        }

        pos += n;
      }
      timerTask.cancel();

      HandshakeRes ret = new HandshakeRes();
      ret.HearBeatTime = new Duration((((0xff & handshake[0]) << 8) + (0xff & handshake[1])) * Duration.Second);
      ret.FrameTimeout = new Duration(handshake[2] * Duration.Second);
      ret.MaxConcurrent = handshake[3];
      ret.MaxBytes = Binary.Net2Host(handshake, 4, 8);
      long id1 = Binary.Net2Host(handshake, 8, 12);
      long id2 = Binary.Net2Host(handshake, 12, 16);
      ret.ConnectId = String.format("%08x", id1) + String.format("%08x", id2);
      return ret;
    }

    public void send(byte[] content) throws Exception {
      if (content.length > config.MaxBytes) {
        throw new Exception(String.format("data is too large, must be less than %d Bytes", config.MaxBytes));
      }

      waitForSending.add(content);
      _send();
    }

    public void sendForce(byte[] content) {
      byte[] len = new byte[4];
      int length = content.length + 4;
      Binary.Host2Net(length, len);

      synchronized (sendData) {
        sendData.add(len);
        sendData.add(content);
        sendData.notify();
      }
    }

    private void _send() {
      if (concurrent >= config.MaxConcurrent) {
        return;
      }
      if (waitForSending.isEmpty()) {
        return;
      }

      concurrent++;

      byte[] content = waitForSending.remove(0);

      // todo : test calling sendForce()
      byte[] len = new byte[4];
      int length = content.length + 4;
      Binary.Host2Net(length, len);

      synchronized (sendData) {
        sendData.add(len);
        sendData.add(content);
        sendData.notify();
      }
    }

    public void receivedOneResponse() {
      concurrent--;
      // 防御性代码
      if (concurrent < 0) {
        concurrent = 0;
      }

      _send();
    }

  }

  public LenContent() {
    config = new Config(new Duration(30 * Duration.Second)
      , new Duration(4 * Duration.Minute)
      , new Duration(15 * Duration.Second));
  }

  @Override
  public void connect(String host, int port, boolean tls) {
    if (connection != null) {
      connection.invalidAndClose();
    }
    connection = new Connection(config, delegate);
    connection.connect(host, port, tls);
  }

  @Override
  public void send(byte[] content) throws Exception{
    connection.send(content);
  }

  @Override
  public void sendForce(byte[] content) {
    connection.sendForce(content);
  }

  @Override
  public void receivedOneResponse() {
    connection.receivedOneResponse();
  }

  @Override
  public void close() {
    connection.close();
  }

  @Override
  public void setDelegate(Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setConfig(Config config) {
    this.config.ConnectTimeout = config.ConnectTimeout;
  }

  private Delegate delegate;
  private final Config config;
  private Connection connection;
}
