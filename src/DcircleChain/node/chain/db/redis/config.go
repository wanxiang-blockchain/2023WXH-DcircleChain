package redis

import (
	"github.com/xpwu/go-config/configs"
	"github.com/xpwu/go-db-redis/rediscache"
)

var ConfigValue = rediscache.Config{
	Host:      "192.168.6.165",
	Port:      6667,
	DBNo:      1,
	TimeoutMs: 0,
}

func init() {
	configs.Unmarshal(&ConfigValue)
}
