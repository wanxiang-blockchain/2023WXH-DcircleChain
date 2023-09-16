package sign

import (
	"dcircleserver/chain/db"
	"github.com/xpwu/go-config/configs"
)

type config struct {
	Chain         uint8
	MasterAddress db.EthAddress
}

var ConfigValue = &config{
	Chain:         3,
	MasterAddress: "0xs2jn3j2jhbgn1mb2k1h2k1h212k",
}

func init() {
	configs.Unmarshal(ConfigValue)
}
