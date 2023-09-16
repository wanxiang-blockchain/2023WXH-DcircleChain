package websocket

import (
	"fmt"
	"github.com/xpwu/go-config/configs"
	"github.com/xpwu/go-log/log"
	"github.com/xpwu/go-stream/proxy"
	"github.com/xpwu/go-xnet/xtcp"
	"regexp"
	"time"
)

type config struct {
	Servers []*server
}

type server struct {
	Net                        *xtcp.Net
	HeartBeat_s                time.Duration `conf:",unit:s"`
	Origin                     []string
	OriginRegex                []*regexp.Regexp `conf:"-"`
	Proxy                      *proxy.Config
	ProxyVar                   *proxy.ConfigVar `conf:"-"`
	MaxConcurrentPerConnection int              `conf:"-"`
	MaxBytesPerFrame           uint32           `conf:"-"`
}

func (s *server) checkValue(logger *log.Logger) {
	logger.PushPrefix(fmt.Sprintf("listen(%s)", s.Net.Listen.LogString()))

	if s.MaxConcurrentPerConnection > 20 {
		logger.Warning("MaxConcurrentPerConnection must be less than 20! Use default value: 5")
	}
	if s.MaxConcurrentPerConnection <= 0 || s.MaxConcurrentPerConnection > 20 {
		s.MaxConcurrentPerConnection = 5
	}

	s.ProxyVar = proxy.CompileConf(s.Proxy)

	if s.HeartBeat_s > 65535 {
		logger.Warning("HeartBeat_s must be less than 65535! Use default value: 240")
	}
	if s.HeartBeat_s < 1 || s.HeartBeat_s > 65535 {
		s.HeartBeat_s = 4 * 60
	}
	s.HeartBeat_s *= time.Second

	s.OriginRegex = make([]*regexp.Regexp, len(s.Origin))
	for i, origin := range s.Origin {
		s.OriginRegex[i] = regexp.MustCompile(escapeReg(origin))
	}

	logger.PopPrefix()
}

var configValue = &config{
	Servers: []*server{
		{
			Net:                        xtcp.DefaultNetConfig(),
			HeartBeat_s:                4 * 60,
			Origin:                     []string{"*"},
			Proxy:                      proxy.DefaultConfig(),
			MaxConcurrentPerConnection: 5,
			MaxBytesPerFrame:           4 * 1024 * 1024,
		},
	},
}

func init() {
	configs.Unmarshal(configValue)
}
