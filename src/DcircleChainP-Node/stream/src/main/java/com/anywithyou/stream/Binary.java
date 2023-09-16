package com.anywithyou.stream;

public class Binary {
  static public long Net2Host(byte[] data, int pos, int end) {
    long ret = 0;
    for (int i = pos; i < end; i++) {
      ret = (ret << 8) + (data[i]&0xff);
    }
    return ret;
  }

  static public long Net2Host(byte[] data, int pos) {
    return Net2Host(data, pos, data.length);
  }

  static public long Net2Host(byte[] data) {
    return Net2Host(data, 0, data.length);
  }

  static public void Host2Net(long v, byte[] ret, int pos, int end) {
    for (int i = end-1; i >= pos ; i--) {
      ret[i] = (byte)(v & 0xff);
      v = v >> 8;
    }
  }

  static public void Host2Net(long v, byte[] ret, int pos) {
    Host2Net(v, ret, pos, ret.length);
  }

  static public void Host2Net(long v, byte[] ret) {
    Host2Net(v, ret, 0, ret.length);
  }
}
