package push

import (
  "context"
  "fmt"
  "github.com/xpwu/go-log/log"
  "github.com/xpwu/go-stream/push/core"
  "github.com/xpwu/go-stream/push/protocol"
  "github.com/xpwu/go-xnet/xtcp"
  "io"
  "time"
)

func Start() {

  for _, s := range configValue.Servers {
    if !s.Net.Listen.On() {
      continue
    }

    // 转换数据
    s.AckTimeout = s.AckTimeout * time.Second

    go runServer(s)
  }
}

func runServer(s *server) {
  logger := log.NewLogger()
  defer func() {
    if r := recover(); r != nil {
      logger.Fatal(r)
      logger.Error(fmt.Sprintf("%s server down! will restart after 5 seconds.", s.Net.Listen.LogString()))
      time.Sleep(5*time.Second)
      go runServer(s)
      logger.Info("server restart!")
    }
  }()

  if s.CloseSubProtocolId == s.DataSubProtocolId {
    panic("push config error. CloseSubProtocolId must not be equal to DataSubProtocolId")
  }

  var handler xtcp.HandlerFun = func(conn *xtcp.Conn) {
    ctx, logger := log.WithCtx(conn.Context())
    ctx, cancelF := context.WithCancel(ctx)

    logger.Debug("new connection")

    // read request
    request := read(ctx, conn)

    // 20 表示一个相对大的数而已，预计不会阻塞chan就ok
    end := make(chan error, 20)

    // 一个比较长的时间
    const largeTime = 2*time.Hour
    const keepalive = 30 * time.Second
    timer := time.NewTimer(largeTime)
    count := 0

    _for:
    for {
      select {
      case r,ok := <-request:
        if !ok {
          cancelF()
          _ = conn.Close()
          break _for
        }
        go processRequest(ctx, s, r, end)
        count++
        if !timer.Stop() {
          <-timer.C
        }
        timer.Reset(largeTime)
      case err := <-end:
        if err != nil {
          logger.Error(err)
          cancelF()
          _ = conn.Close()
          break _for
        }
        count--
        if count == 0 {
          if !timer.Stop() {
            <-timer.C
          }
          // 全部处理完后再保持连接30s
          timer.Reset(keepalive)
        }
      case <-timer.C:
        logger.Info("keepalive timeout, close conn")
        cancelF()
        _ = conn.Close()
        break _for
      }
    }

    // no pipeline
    //keepAlive := 5 * time.Second
    //for {
    //  r := protocol.NewRequest(conn)
    //  err := r.Read(keepAlive)
    //  if err == io.EOF {
    //    logger.Debug("connection closed by peer ")
    //    return
    //  }
    //  if err != nil {
    //    logger.Debug("read error: ", err, ", will close connection")
    //    return
    //  }
    //
    //  // 暂时不使用pipeline的方式处理
    //  if err := processRequest(s, r); err != nil {
    //    logger.Error("processRequest error: ", err)
    //    return
    //  }
    //
    //  keepAlive = 30 * time.Second
    //}
  }

  tcpServer := &xtcp.Server{
    Net:     s.Net,
    Handler: handler,
    Name:    "push",
  }

  tcpServer.ServeAndBlock()

}

func read(ctx context.Context, conn *xtcp.Conn) <-chan *protocol.Request {
  _,logger := log.WithCtx(ctx)
  request := make(chan *protocol.Request)
  go func() {
    defer func() {
      close(request)
    }()
    for {
      r := protocol.NewRequest(conn)
      err := r.Read(time.Time{})
      if err == io.EOF {
        logger.Debug("connection closed by peer ")
        return
      }
      if err != nil {
        logger.Debug("read error: ", err, ", will close connection")
        return
      }
      request <- r
    }
  }()
  return request
}

func processRequest(ctx context.Context, s *server, r *protocol.Request, end chan error) {

  clientConn, st := core.GetClientConn(ctx, string(r.Token))
  if st != core.Success {
    end <- protocol.NewResponse(r, st).Write()
    return
  }

  if r.SubProtocol == s.CloseSubProtocolId {
   core.CloseClientConn(ctx, clientConn)
    end <- protocol.NewResponse(r).Write()
    return
  }

  // 根据配置要求，0 表示不等待ack
  cancel := context.CancelFunc(func() {})
  if s.AckTimeout != 0 {
    ctx,cancel = context.WithTimeout(ctx, s.AckTimeout)
  }

  st = core.PushDataToClient(ctx, clientConn, r.Data)
  cancel()
  end <- protocol.NewResponse(r, st).Write()

}
