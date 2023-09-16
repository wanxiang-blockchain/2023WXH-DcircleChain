package websocket

import (
  "context"
  "encoding/binary"
  "fmt"
  "github.com/gorilla/websocket"
  "github.com/xpwu/go-log/log"
  "github.com/xpwu/go-log/log/level"
  conn2 "github.com/xpwu/go-stream/conn"
  "github.com/xpwu/go-stream/fakehttp"
  "github.com/xpwu/go-stream/proxy"
  "github.com/xpwu/go-xnet/xhttp"
  "github.com/xpwu/go-xnet/xtcp"
  "io"
  "net/http"
  "net/url"
  "strings"
  "sync"
  "time"
)

func Start() {

  for _, s := range configValue.Servers {
    if !s.Net.Listen.On() {
      continue
    }

    go runServer(s)
  }
}

func runServer(s *server) {
  ctx,logger := log.WithCtx(context.Background())

  defer func() {
    if r := recover(); r != nil {
      logger.Fatal(r)
      logger.Error(fmt.Sprintf("%s server down! will restart after 5 seconds.", s.Net.Listen.LogString()))
      time.Sleep(5*time.Second)
      go runServer(s)
      logger.Info("server restart!")
    }
  }()

  //checkServer(s)
  s.checkValue(logger)

  logger.Info("server(websocket) listen " + s.Net.Listen.LogString())

  var upgrader = &websocket.Upgrader{
    HandshakeTimeout: 30 * time.Second,
    ReadBufferSize:   2048,
    WriteBufferSize:  4096,
    WriteBufferPool:  &BufferPool{},
    CheckOrigin:      checkOrigin(s),
  }

  httpServer := &http.Server{
    Addr:     s.Net.Listen.String(),
    Handler:  handler(upgrader, s),
    ErrorLog: log.NewSysLog(logger, level.ERROR),
  }

  err := xhttp.SeverAndBlockWithName(ctx, httpServer, s.Net, "websocket")

  if err != nil {
    _ = httpServer.Close()
    panic(err)
  }
}

//func checkServer(configValue *server) {
//  configValue.HeartBeat_s *= time.Second
//
//  configValue.OriginRegex = make([]*regexp.Regexp, len(configValue.Origin))
//  for i, s := range configValue.Origin {
//    configValue.OriginRegex[i] = regexp.MustCompile(escapeReg(s))
//  }
//
//  configValue.ProxyVar = proxy.CompileConf(configValue.Proxy)
//}

func escapeReg(str string) string {
  str = strings.Replace(str, ".", "\\.", -1)
  str = strings.Replace(str, "?", "\\?", -1)
  str = "^" + strings.Replace(str, "*", ".*", -1) + "$"

  return str
}

type BufferPool struct {
  pool sync.Pool
}

func (b *BufferPool) Get() interface{} {
  return b.pool.Get()
}

func (b *BufferPool) Put(x interface{}) {
  b.pool.Put(x)
}

func checkOrigin(s *server) func(r *http.Request) bool {

  return func(r *http.Request) bool {
    // 可以使用 * 通配符,

    // 如果与请求host 相同，默认允许
    if checkSameOrigin(r) {
      return true
    }

    origin := r.Header["Origin"][0]
    for _, reg := range s.OriginRegex {
      if origin == reg.FindString(origin) {
        return true
      }
    }

    return false
  }
}

// checkSameOrigin returns true if the origin is not set or is equal to the request host.
func checkSameOrigin(r *http.Request) bool {
  origin := r.Header["Origin"]
  if len(origin) == 0 {
    return true
  }
  u, err := url.Parse(origin[0])
  if err != nil {
    return false
  }

  return strings.EqualFold(u.Host, r.Host)
  //return equalASCIIFold(u.Host, r.Host)
}

// equalASCIIFold returns true if s is equal to t with ASCII case folding as
// defined in RFC 4790.
//func equalASCIIFold(s, t string) bool {
//  for s != "" && t != "" {
//    sr, size := utf8.DecodeRuneInString(s)
//    s = s[size:]
//    tr, size := utf8.DecodeRuneInString(t)
//    t = t[size:]
//    if sr == tr {
//      continue
//    }
//    if 'A' <= sr && sr <= 'Z' {
//      sr = sr + 'a' - 'A'
//    }
//    if 'A' <= tr && tr <= 'Z' {
//      tr = tr + 'a' - 'A'
//    }
//    if sr != tr {
//      return false
//    }
//  }
//  return s == t
//}

/*
HeartBeat_s | FrameTimeout_s | MaxConcurrent | MaxBytes | connect id
   HeartBeat_s: 2 bytes, net order
   FrameTimeout_s: 1 byte  ===0
   MaxConcurrent: 1 byte
   MaxBytes: 4 bytes, net order
   connect id: 8 bytes, net order
 */
func writeHandshake(c *conn, s *server) (err error) {
  res := make([]byte, 2 + 1 + 1 + 4 + 8)
  // 最大可设置 65535 s 的心跳时间，再实际使用中，几乎不会设置这么大的心跳时间
  binary.BigEndian.PutUint16(res, uint16(s.HeartBeat_s/time.Second))
  res[2] = byte(0) // FrameTimeout_s === 0
  res[3] = byte(s.MaxConcurrentPerConnection)
  binary.BigEndian.PutUint32(res[4:], s.MaxBytesPerFrame)
  binary.BigEndian.PutUint64(res[8:], uint64(c.Id()))

  return c.Write([][]byte{res})
}

// 心跳的处理：由服务器主动发起Ping，激发客户端回应Pong

func handler(upgrader *websocket.Upgrader,
  s *server) http.Handler {

  if s.MaxConcurrentPerConnection <= 0 || s.MaxConcurrentPerConnection > 20 {
    s.MaxConcurrentPerConnection = 5
  }

  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

    ctx,logger := log.WithCtx(r.Context())

    conn, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
      logger.Error(err)
      return
    }

    if underConn, ok := conn.UnderlyingConn().(*xtcp.Conn); ok {
      ctx = underConn.Context()
    }
    fConn := newConn(ctx, conn, s)
    ctx,logger = log.WithCtx(fConn.Context())

    logger.Debug("new connection")

    defer func() {
      if r := recover(); r != nil {
        logger.Fatal(r)
      }
      fConn.CloseWith(nil)
    }()

    conn.SetReadLimit(int64(s.MaxBytesPerFrame))

    // 当收到Pong 时，需要重新设置一下 ReadDeadLine , 继续监测是否有正常的心跳
    conn.SetPongHandler(func(appData string) error {
      logger.Debug("receive pong")
      err := conn.SetReadDeadline(time.Now().Add(2 * s.HeartBeat_s))
      if err != nil {
        logger.Error("process pong error. ", err)
        return err
      }
      return nil
    })

    if err := writeHandshake(fConn, s); err != nil {
      logger.Error("write handshake error! connection will close. ", err)
      return
    }

    for {

      logger.Debug("will read request")

      // 这里是设置的底层tcp的超时，并不表示 2次心跳时间必须要能读到message，而是
      // 读到一次message后，延长2次心跳的超时时间。虽然websocket的ping pong 是单独
      // 的收发，这里不重置超时时间也不影响心跳的逻辑，但是从上层来考虑，仍然认为收到message
      // 也是表示client存活的状态。
      err := conn.SetReadDeadline(time.Now().Add(2 * s.HeartBeat_s))
      if err != nil {
        logger.Debug("read message error. ", err, " will close connection")
        return
      }

      _, date, err := conn.ReadMessage()
      if err == io.EOF {
        logger.Debug("connection closed by peer ")
        return
      }
      if err != nil {
        logger.Debug("read message error. ", err, ", will close connection")
        return
      }

      fhttpReq, err := fakehttp.NewRequest(fConn, date)
      if err != nil {
        logger.Error(err, ", will close connection")
        return
      }
      // push ack 不计入并发统计
      yes, err := fhttpReq.IsPushAck()
      if err != nil {
        logger.Error(err, ", will close connection")
        return
      }
      if yes {
        pushId := fakehttp.Bytes2PushID(fhttpReq.Data, fConn)
        logger.Debug("receive pushAck: ", pushId.Value)
        pushId.Ack()
        continue
      }

      conn2.TryConcurrent(fConn.ctx, fConn.concurrent)

      logger.Debug(fmt.Sprintf("read request(ptr=%p)", fhttpReq))

      go func() {
        proxy.Handler(ctx, fhttpReq, s.ProxyVar)
        conn2.DoneConcurrent(ctx, fConn.concurrent)
      }()
    }

  })
}
