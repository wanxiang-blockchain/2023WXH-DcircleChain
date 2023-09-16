package server

import (
	"dcircleserver/chain/db"
	"github.com/xpwu/go-config/configs"
)

type ChainServer struct {
	BlockMaxTransaction         int
	BlockMaxContent             int
	TokenAddress                db.EthAddress
	MasterPrivateKey            db.HexString
	TrustedSequencerPrivateKey  db.HexString
	ChainID                     int64
	ChainRpcAddress             string
	RollUpL2FrequencySeconds    int
	BlockCreateFrequencySeconds int
}

var ChainServerNode = ChainServer{
	BlockMaxTransaction:         100,
	BlockMaxContent:             100,
	TokenAddress:                "0x990cCf114ab284CC67681Efb29A66F47149611B1",
	MasterPrivateKey:            "77e8ff3c61e2b5842a37ecef3ede24177571419122a1840f128dcee8a24d8d77",
	TrustedSequencerPrivateKey:  "77e8ff3c61e2b5842a37ecef3ede24177571419122a1840f128dcee8a24d8d77",
	ChainID:                     80001,
	ChainRpcAddress:             "wss://polygon-mumbai.g.alchemy.com/v2/UJULbhnCQP1IbZsdpvbevJG0jR1eBMGF",
	RollUpL2FrequencySeconds:    24 * 60 * 60,
	BlockCreateFrequencySeconds: 2,
}

func init() {
	configs.Unmarshal(&ChainServerNode)
	//println("解析CHAIN配置：", MongoConfig.URI)
}
