package proxy

import (
  "github.com/xpwu/go-var/vari"
)

type Header struct {
  Key   string
  Value string
}

type Config struct {
  Url     string
  Headers []*Header
}

func DefaultConfig() *Config{
  return &Config{
    Url:     "127.0.0.1:80/${fhttp_api}",
    Headers: []*Header{{"Pushurl", "127.0.0.1:7999/${pushtoken}"}},
  }
}


type HeaderVar struct {
  Key   string
  Value *vari.Var
}
type ConfigVar struct {
  Url     *vari.Var
  Headers []*HeaderVar
}


func CompileConf(conf *Config) *ConfigVar {
  ret := &ConfigVar{
    Url:     vari.Compile(conf.Url),
    Headers: make([]*HeaderVar, len(conf.Headers)),
  }

  for i, header := range conf.Headers {
    ret.Headers[i] = &HeaderVar{header.Key, vari.Compile(header.Value)}
  }

  return ret
}
