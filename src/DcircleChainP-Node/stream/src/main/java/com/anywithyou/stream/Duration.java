package com.anywithyou.stream;

public class Duration {
  static public final long Microsecond = 1;
  static public final long Millisecond = 1000 * Microsecond;
  static public final long Second = 1000 * Millisecond;
  static public final long Minute = 60 * Second;
  static public final long Hour = 60 * Minute;

  // 10 second: 10*Duration.Second
  public Duration(long d) {
    this.d = d;
  }

  public long second() {
    return d/Second;
  }

  public long milliSecond() {
    return d/Millisecond;
  }

  public long minute() {
    return d/Minute;
  }

  private final long d;
}
