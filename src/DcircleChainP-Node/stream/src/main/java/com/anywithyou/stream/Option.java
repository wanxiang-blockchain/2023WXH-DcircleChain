package com.anywithyou.stream;

public class Option {
  static public class Value {
    public String host;
    public int port;
    public boolean tls;
    public Duration connectTimeout;
    public Duration heartbeatTime;
    public Duration frameTimeout; // 同一帧里面的数据延时
    public Duration requestTimeout; //请求到响应的超时

    public Value() {
      this.host = "127.0.0.1";
      this.port = 10000;
      this.connectTimeout = new Duration(30*Duration.Second);
      this.heartbeatTime = new Duration(4*Duration.Minute);
      this.frameTimeout = new Duration(5*Duration.Second);
      this.requestTimeout = new Duration(60*Duration.Second);
      this.tls = false;
    }
  }

  static public Option Host(String host) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.host = host;
      }
    });
  }

  static public Option Port(int port) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.port = port;
      }
    });
  }

  static public Option TLS() {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.tls = true;
      }
    });
  }

  static public Option ConnectTimeout(Duration duration) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.connectTimeout = duration;
      }
    });
  }

  static public Option RequestTimeout(Duration duration) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.requestTimeout = duration;
      }
    });
  }

  // 由握手协议，在服务器中读取
  @Deprecated
  static public Option HeartbeatTime(Duration duration) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
//        value.heartbeatTime = duration;
      }
    });
  }

  // 由握手协议，在服务器中读取
  @Deprecated
  static public Option FrameTimeout(Duration duration) {
    return new Option(new Setter() {
      @Override
      public void configValue(Value value) {
        value.frameTimeout = duration;
      }
    });
  }

  public void configValue(Value value) {
    this.setter.configValue(value);
  }

  private interface Setter {
    void configValue(Value value);
  }

  private Option(Setter setter) {
    this.setter = setter;
  }

  private final Setter setter;
}
