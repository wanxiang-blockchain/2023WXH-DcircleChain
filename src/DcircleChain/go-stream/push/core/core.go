package core

import (
  "context"
  "crypto/md5"
  "encoding/hex"
  "fmt"
  "github.com/xpwu/go-log/log"
  "github.com/xpwu/go-reqid/reqid"
  "github.com/xpwu/go-stream/conn"
  "github.com/xpwu/go-stream/fakehttp"
)

var hostId = ""

func init() {
  m5 := md5.Sum([]byte(reqid.RandomID()))
  hostId = hex.EncodeToString(m5[:])

  conn.RegisterVar("pushtoken", func(conn conn.Conn) string {
    token := Token{HostId: hostId, ConnId: conn.Id()}
    return token.String()
  })
}


func GetClientConn(ctx context.Context, tokenStr string) (conn.Conn, State) {
  ctx, logger := log.WithCtx(ctx)

  token, err := ResumeToken(tokenStr, hostId)
  if err != nil {
    logger.Error(err)
    return nil, HostNameErr
  }

  con, ok := conn.GetConn(token.ConnId)
  if !ok {
    logger.Error(fmt.Sprintf("can not find conn with id(%v)", token))
    return nil, TokenNotExist
  }

  return con, Success
}

func CloseClientConn(ctx context.Context, con conn.Conn) {
  _, logger := log.WithCtx(ctx)
  logger.Info(fmt.Sprintf("close subprotocol. will close conn(id=%s). ", con.Id()))
  con.CloseWith(nil)
}

func PushDataToClient(ctx context.Context, clientCon conn.Conn, data []byte) State {
  ctx, logger := log.WithCtx(ctx)

  logger.Debug(fmt.Sprintf("push data(len=%d) to client connection(conn_id=%s)", len(data), clientCon.Id()))
  pushId := fakehttp.GetPushID(clientCon)
  // 先准备好ack的接收
  ch := pushId.WaitAck()
  // 写给客户端
  clientRes := fakehttp.NewResponseWithPush(clientCon, pushId.ToBytes(), data)
  if err := clientRes.Write(); err != nil {
    logger.Error(fmt.Sprintf("write push data to client conn(id=%s) err. ", clientCon.Id()), err)
    clientCon.CloseWith(fmt.Errorf("write push data err, %v. Will close this connection(id=%s). ",
      err, clientCon.Id()))

    return ServerInternalErr
  }

  select {
  case _,ok := <- ch:
    if !ok {
      return ServerInternalErr
    }
    return Success
  case <- ctx.Done():
    if ctx.Err() == context.DeadlineExceeded {
      pushId.CancelWaitingAck()
      return Timeout
    }
    return ServerInternalErr
  case <-clientCon.Context().Done():
    return ServerInternalErr
  }
}
