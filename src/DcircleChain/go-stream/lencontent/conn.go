package lencontent

import (
  "context"
  "github.com/xpwu/go-log/log"
  conn2 "github.com/xpwu/go-stream/conn"
  "github.com/xpwu/go-xnet/connid"
  "github.com/xpwu/go-xnet/xtcp"
  "net"
  "sync"
  "time"
)

type conn struct {
  conn2.Base
  tcpC       *xtcp.Conn
  heartBeat  *time.Timer
  server     *server
  mu         sync.Mutex
  concurrent chan struct{}
}

func newConn(tcpConn *xtcp.Conn, s *server) *conn {
  _, logger := log.WithCtx(tcpConn.Context())

  ret := &conn{
    tcpC:       tcpConn,
    server:     s,
    concurrent: make(chan struct{}, s.MaxConcurrentPerConnection),
  }

  conn2.AddConn(ret)

  heartBeat := time.AfterFunc(s.HeartBeat_s, func() {
    // 可能连接已经关闭，则直接不执行，
    //并把可能占有的变量释放掉(定时器中这类变量是否go会自动释放有待研究，这里为了稳妥，手动释放一次)
    if tcpConn == nil || ret == nil || ret.heartBeat == nil {
      tcpConn = nil
      ret = nil
      return
    }

    err := tcpConn.SetWriteDeadline(time.Now().Add(1 * time.Second))
    if err != nil {
      ret.CloseWith(err)
    }

    _, err = tcpConn.Write(make([]byte, 4))
    if err != nil {
      ret.CloseWith(err)
    }

    logger.Debug("send heartbeat to client")

    if ret != nil && ret.heartBeat != nil {
      // 定时执行的时候已经关闭，则可能为nil
      ret.heartBeat.Reset(s.HeartBeat_s)
    }
  })

  ret.heartBeat = heartBeat

  return ret
}

func (c *conn) GetVar(name string) string {
  value, ok := conn2.GetVarValue(name, c)
  if ok {
    return value
  }

  value, ok = c.tcpC.GetVar(name)
  if ok {
    return value
  }

  return ""
}

func (c *conn) Id() connid.Id {
  return c.tcpC.Id()
}

func (c *conn) Write(buffers net.Buffers) error {
  // 有写操作，就重新开始心跳计时
  c.mu.Lock()
  if c.heartBeat != nil {
    c.heartBeat.Reset(c.server.HeartBeat_s)
  }
  c.mu.Unlock()

  response := newResponse(c.server, c.tcpC, buffers)

  return response.write()
}

func (c *conn) CloseWith(err error) {

  c.mu.Lock()
  if c.heartBeat != nil {
    c.heartBeat.Stop()
    c.heartBeat = nil
  }
  c.Base.Close()
  c.mu.Unlock()

  conn2.DelConn(c)

  _, logger := log.WithCtx(c.tcpC.Context())
  if err != nil {
    logger.Error(err)
  }
  logger.Debug("close connection")

  _ = c.tcpC.Close()
}

func (c *conn) Context() context.Context {
  return c.tcpC.Context()
}
