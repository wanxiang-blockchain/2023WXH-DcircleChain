package com.anywithyou.stream;

import android.util.Log;
import android.util.Pair;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * content protocol:
 *    request ---
 *      reqid | headers | header-end-flag | data
 *        reqid: 4 bytes, net order;
 *        headers: < key-len | key | value-len | value > ... ;  [optional]
 *          key-len: 1 byte,  key-len = sizeof(key);
 *          value-len: 1 byte, value-len = sizeof(value);
 *        header-end-flag: 1 byte, === 0;                       [optional]
 *        data:       [optional]
 *
 *    response ---
 *      reqid | status | data
 *        reqid: 4 bytes, net order;
 *        status: 1 byte, 0---success, 1---failed
 *        data: if status==success, data=<app data>    [optional]
 *              if status==failed, data=<error reason>
 *
 */

class FakeHttp {
  static class Request {
    Request(byte[] body, Map<String, String> headers) throws Exception {
      int length = 4 + 1;
      if (body != null) {
        length += body.length;
      }

      ArrayList<Pair<byte[], byte[]>> headerList = new ArrayList<>();
      if (headers != null) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          byte[] key = entry.getKey().getBytes(StandardCharsets.UTF_8);
          byte[] value = entry.getValue().getBytes(StandardCharsets.UTF_8);

          if (key.length > 255 || value.length > 255) {
            Exception e = new Exception("key('" + entry.getKey() + "')'s length or value('"
              + entry.getValue() + "') is more than 255 ");
            Log.e("fakeHttp.request", "header error", e);
            throw e;
          }
          length += 1 + key.length + 1 + value.length;

          headerList.add(new Pair<>(key, value));
        }
      }

      byte[] request = new byte[length];


      int pos = 4;

      for (Pair<byte[], byte[]> entry : headerList) {
        byte[] key = entry.first;
        byte[] value = entry.second;

        request[pos] = (byte) key.length;
        pos++;
        System.arraycopy(key, 0, request, pos, key.length);
        pos += key.length;
        request[pos] = (byte) value.length;
        pos++;
        System.arraycopy(value, 0, request, pos, value.length);
        pos += value.length;
      }

      request[pos] = 0; // header-end
      pos++;

      if (body != null) {
        System.arraycopy(body, 0, request, pos, body.length);
      }

      data = request;
    }

    public void setReqId(long reqId) {
      data[0] = (byte) ((reqId & 0xff000000) >> 24);
      data[1] = (byte) ((reqId & 0xff0000) >> 16);
      data[2] = (byte) ((reqId & 0xff00) >> 8);
      data[3] = (byte) (reqId & 0xff);
    }

    public void sendTo(Net net) throws Exception {
      net.send(data);
    }

    private final byte[] data;
  }

  static class Response {
    enum Status {
      Ok,
      Failed
    }

    public static Response fromError(long reqID, String error) {
      return new Response(reqID, Status.Failed, error.getBytes(StandardCharsets.UTF_8));
    }

    Response(byte[] response) throws Exception {

      this.status = response[4] == 0? Status.Ok:Status.Failed;

      long reqID = 0;
      for (int i = 0; i < 4; ++i) {
        reqID = (reqID << 8) + (response[i]&0xff);
      }
      this.reqID = reqID;

      int offset = 5;
      if (reqID == 1) {
        this.pushID = Arrays.copyOfRange(response, offset, offset+4);
        offset += 4;
      }

      if (response.length <= offset) {
        this.data = null;
      } else {
        try {
          this.data = Arrays.copyOfRange(response, offset, response.length);
        } catch (Exception e) {
          Log.e("fakeHttp.response", "rawData error", e);
          throw e;
        }
      }
    }

    public boolean isPush() {
      return reqID == 1;
    }

    public byte[] newPushAck() {
      if (!isPush() || pushID.length != 4) {
        return new byte[0];
      }

      byte[] data = new byte[4+1+4];
      data[0] = (byte) ((reqID & 0xff000000) >> 24);
      data[1] = (byte) ((reqID & 0xff0000) >> 16);
      data[2] = (byte) ((reqID & 0xff00) >> 8);
      data[3] = (byte) (reqID & 0xff);

      data[4] = 0;

      data[5] = pushID[0];
      data[6] = pushID[1];
      data[7] = pushID[2];
      data[8] = pushID[3];

      return data;
    }

    public final Status status;
    public final long reqID;
    public final byte[] data;
    private byte[] pushID = null;

    private Response(long reqID, Status status, byte[] data) {
      this.reqID = reqID;
      this.status = status;
      this.data = data;
    }
  }
}
