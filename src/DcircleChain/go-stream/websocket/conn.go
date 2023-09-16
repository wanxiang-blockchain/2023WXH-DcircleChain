package websocket

import (
  "context"
  "fmt"
  "github.com/gorilla/websocket"
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
  c          *websocket.Conn
  mu         chan struct{}
  ctx        context.Context
  cancelF    context.CancelFunc
  closed     bool
  heartBeat  *time.Ticker
  once       sync.Once
  id         connid.Id
  concurrent chan struct{}
}

func newConn(ctx context.Context, c *websocket.Conn, s *server) *conn {

  // 需要固定时间就发送心跳，与是否刚发过数据无关。
  // 客户端不会主动发心跳，只能服务器固定间隔时间发送Ping 来激发客户端发送Pong
  heartBeat := time.NewTicker(s.HeartBeat_s)

  ctx, logger := log.WithCtx(ctx)

  id := connid.Id(0)
  underConn, ok := c.UnderlyingConn().(*xtcp.Conn)
  if ok {
    id = underConn.Id()
  } else {
    id = connid.New()
  }

  ret := &conn{
    c:          c,
    mu:         make(chan struct{}, 1),
    heartBeat:  heartBeat,
    id:         id,
    concurrent: make(chan struct{}, s.MaxConcurrentPerConnection),
  }

  ret.ctx, ret.cancelF = context.WithCancel(ctx)

  conn2.AddConn(ret)

  go func() {
  loop:
    for {
      select {
      case <-heartBeat.C:
        logger.Debug("send Ping")
        err := c.WriteControl(websocket.PingMessage, []byte{}, time.Now().Add(1*time.Second))
        if err != nil {
          ret.CloseWith(fmt.Errorf("write ping error. will close connection, %v", err))
        }
      case <-ret.ctx.Done():
        break loop
      }
    }
  }()

  return ret
}

func (c *conn) GetVar(name string) string {
  value, ok := conn2.GetVarValue(name, c)
  if ok {
    return value
  }

  value, ok = xtcp.GetVarValue(c.c, name)
  if ok {
    return value
  }

  return ""
}

func (c *conn) Id() connid.Id {
  return c.id
}

func (c *conn) Write(buffers net.Buffers) error {
  // 只能一个goroutines 访问
  c.mu <- struct{}{}
  defer func() {
    <-c.mu
  }()

  err := c.c.SetWriteDeadline(time.Now().Add(5 * time.Second))
  if err != nil {
    return err
  }

  writer, err := c.c.NextWriter(websocket.BinaryMessage)
  if err != nil {
    return err
  }

  for _, d := range buffers {
    if _, err = writer.Write(d); err != nil {
      return err
    }
  }

  return writer.Close()
}

func (c *conn) CloseWith(err error) {
  c.once.Do(func() {
    _, logger := log.WithCtx(c.ctx)
    c.heartBeat.Stop()
    c.Base.Close()
    c.cancelF()
    conn2.DelConn(c)
    if err != nil {
      logger.Error(err)
    }
    logger.Debug("close connection")
    _ = c.c.Close()
  })
}

func (c *conn) Context() context.Context {
  return c.ctx
}
