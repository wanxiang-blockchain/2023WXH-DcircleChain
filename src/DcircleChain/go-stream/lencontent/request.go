package lencontent

import (
  "encoding/binary"
  "errors"
  "fmt"
  "github.com/xpwu/go-xnet/connid"
  "github.com/xpwu/go-xnet/xtcp"
  "io"
  "time"
)

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

func handshake(conn *xtcp.Conn, s *server, id connid.Id) error {
  buffer := make([]byte, 6)
  err := conn.SetReadDeadline(time.Now().Add(s.FrameTimeout_s))
  if err != nil {
    return err
  }

  _, err = io.ReadFull(conn, buffer)
  if err != nil {
    return err
  }

  sum := byte(0x00)
  for _, d := range buffer {
    sum ^= d
  }
  if sum != 0xff {
    return errors.New("handshake error")
  }

  version := buffer[0]

  if version < 1 || version > 2 {
    return errors.New("lencontent only support version 1 and 2")
  }

  res := make([]byte, 2)
  // 最大可设置 65535 s 的心跳时间，再实际使用中，几乎不会设置这么大的心跳时间
  binary.BigEndian.PutUint16(res, uint16(s.HeartBeat_s/time.Second))

  if version == 1 {
    _, err = conn.WriteBuffers([][]byte{res})
    return err
  }

  res2 := make([]byte, 1+1+4+8)
  res2[0] = byte(s.FrameTimeout_s / time.Second)
  res2[1] = byte(s.MaxConcurrentPerConnection)
  binary.BigEndian.PutUint32(res2[2:], s.MaxBytesPerFrame)
  binary.BigEndian.PutUint64(res2[6:], uint64(id))
  _, err = conn.WriteBuffers([][]byte{res, res2})

  return err
}

type request struct {
  conn   *xtcp.Conn
  data   []byte
  server *server
}

func newRequest(c *xtcp.Conn, s *server) *request {
  return &request{conn: c, server: s}
}

func (r *request) read() (err error) {
  err = r.conn.SetReadDeadline(time.Now().Add(2 * r.server.HeartBeat_s))
  if err != nil {
    return err
  }

  b := make([]byte, 4)
  if _, err = io.ReadFull(r.conn, b); err != nil {
    return err
  }

  length := binary.BigEndian.Uint32(b)
  if length == 0 {
    r.data = nil
    return
  }
  if length > r.server.MaxBytesPerFrame {
    return fmt.Errorf("data's length must be less than %d Bytes", r.server.MaxBytesPerFrame)
  }

  if length <= 4 {
    return fmt.Errorf("data's length must be more than 4 Bytes")
  }

  r.data = make([]byte, length-4)

  err = r.conn.SetReadDeadline(time.Now().Add(r.server.FrameTimeout_s))
  if _, err = io.ReadFull(r.conn, r.data); err != nil {
    return err
  }

  return
}

func (r *request) isHeartbeat() bool {
  return r.data == nil
}
