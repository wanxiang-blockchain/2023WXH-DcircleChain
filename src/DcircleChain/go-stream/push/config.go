package push

import (
	"github.com/xpwu/go-config/configs"
	"github.com/xpwu/go-xnet/xtcp"
	"time"
)

type config struct {
	Servers []*server
}

type server struct {
	Net                *xtcp.Net
	AckTimeout				 time.Duration	`conf:",uint:s;0: no ack"`
	CloseSubProtocolId byte	`conf:"-"`
	DataSubProtocolId  byte	`conf:"-"`
}

var configValue = &config{
	Servers: []*server{
		{
			Net:                xtcp.DefaultNetConfig(),
			AckTimeout:					30,
			CloseSubProtocolId: 1,
			DataSubProtocolId:  0,
		},
	},
}

func init() {
	configs.Unmarshal(configValue)
}
