package db

import (
	"github.com/xpwu/go-config/configs"
	mongoConfig "github.com/xpwu/go-db-mongo/mongodb/mongocache"
)

type ChainMongo struct {
	mongoConfig.Config
	DBName string
}

var MongoConfig = ChainMongo{
	DBName: "forChain",
}

func init() {
	configs.Unmarshal(&MongoConfig)
	//println("解析CHAIN配置：", MongoConfig.URI)
}
