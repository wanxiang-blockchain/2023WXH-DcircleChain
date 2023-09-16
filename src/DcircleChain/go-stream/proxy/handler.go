package proxy

import (
  "context"
  "github.com/xpwu/go-log/log"
  "github.com/xpwu/go-stream/fakehttp"
  "time"
)

func Handler(ctx context.Context, request *fakehttp.Request, conf *ConfigVar) {
  ctx, logger := log.WithCtx(ctx)

  defer func() {
    if r := recover(); r != nil {
      logger.Fatal(r)
      // 可能错误就是write引起的 不能再次构造错误响应 发送给客户端
      // 该请求将不会有响应  由客户端超时逻辑完成余下的处理
    }
  }()

  //pxy := NewProxy(conf)
  pxy := Creator(conf)
  if _, ok := ctx.Deadline(); !ok {
    ctxC, cancel := context.WithTimeout(ctx, 30*time.Second)
    ctx = ctxC

    defer cancel()
  }

  res := pxy.Do(ctx, request)
  if err := res.Write(); err != nil {
    logger.Error(err)
    request.Conn.CloseWith(err)
  }

}
