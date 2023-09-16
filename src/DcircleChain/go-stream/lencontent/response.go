package lencontent

import (
  "encoding/binary"
  "errors"
  "github.com/xpwu/go-xnet/xtcp"
  "net"
  "time"
)

/**
lencontent protocol:

 1, handshake protocol:

       client ------------------ server
          |                          |
          |                          |
       ABCDEF (A^...^F = 0xff) --->  check(A^...^F == 0xff) --- N--> over
        (A is version, now must be 1)
          |                          |
          |                          |Y
          |                          |
    set client heartbeat  <----- HeartBeat_s (2 bytes, net order)
          |                          |
          |                          |
          |                          |
        data      <-------->       data


   2, data protocol:
     1) length | content
       length: 4 bytes, net order; length=sizeof(content)+4; length=0 => heartbeat
*/

type response struct {
  conn   *xtcp.Conn
  data   net.Buffers
  server *server
}

func newResponse(s *server, conn *xtcp.Conn, buffers net.Buffers) *response {
  return &response{
    conn:   conn,
    data:   buffers,
    server: s,
  }
}

func (r *response) write() error {
  length := 4
  for _, d := range r.data {
    length += len(d)
  }

  if int(uint32(length)) != length {
    return errors.New("data is too large")
  }

  buffers := make(net.Buffers, len(r.data)+1)
  buffers[0] = make([]byte, 4)
  binary.BigEndian.PutUint32(buffers[0], uint32(length))

  for i, d := range r.data {
    buffers[1+i] = d
  }

  err := r.conn.SetWriteDeadline(time.Now().Add(r.server.FrameTimeout_s))
  if err != nil {
    return err
  }

  _, err = r.conn.WriteBuffers(buffers)
  return err
}
