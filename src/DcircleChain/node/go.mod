module dcircleserver

go 1.16

require (
	github.com/btcsuite/btcd/btcec/v2 v2.3.2
	github.com/ethereum/go-ethereum v1.12.0
	github.com/go-redis/redis v6.15.9+incompatible
	github.com/xpwu/go-api-token v0.0.0-20230223072804-712caf14bed3
	github.com/xpwu/go-cmd v0.1.0
	github.com/xpwu/go-cmd-dbindex v0.0.0-20220320160644-d877344a4ef2
	github.com/xpwu/go-config v0.1.0
	github.com/xpwu/go-db-mongo v0.1.0
	github.com/xpwu/go-db-redis v0.1.0
	github.com/xpwu/go-log v0.1.0
	github.com/xpwu/go-tinyserver v0.1.3-0.20230411151504-8bc00c896b0f
	go.mongodb.org/mongo-driver v1.8.4
)

require github.com/decred/dcrd/dcrec/secp256k1/v4 v4.1.0 // indirect

require github.com/google/uuid v1.3.0
