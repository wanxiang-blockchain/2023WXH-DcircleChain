package core

import (
  "errors"
  "fmt"
  "github.com/xpwu/go-xnet/connid"
  "strings"
)

type Token struct {
  ConnId connid.Id // connection id
  HostId string
}

const TokenLen = 32

func (t *Token) String() string {
  id := "_" + t.ConnId.String()
  return t.HostId[:TokenLen-len(id)] + id
}

func ResumeToken(str string, hostId string) (tk Token, err error) {
  sp := strings.Split(str, "_")

  if len(sp) != 2 || !strings.HasPrefix(hostId, sp[0]) {
    err = errors.New(fmt.Sprintf("host prefix of Token(%s) error. full hostid is %s, but remote prefix is %s",
      str, hostId, sp[0]))
    return
  }

  id,err := connid.ResumeIdFrom(sp[1])

  return Token{id, hostId}, err
}

