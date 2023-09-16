package redis

import (
	"context"
	"dcircleserver/src/chain/db"
	"github.com/go-redis/redis"
	"github.com/xpwu/go-db-redis/rediscache"
	"github.com/xpwu/go-log/log"
)

const (
	TransactionPoolKey = "TransactionPoolKey:"
	ContentPoolKey     = "ContentPoolKey:"
	BlockPoolKey       = "BlockPoolKey:"
)

type HashPool struct {
	db     *redis.Client
	ctx    context.Context
	logger *log.Logger
}

func (pool *HashPool) LPush(key string, hash db.HexString) error {
	result := pool.db.LPush(key, hash)
	return result.Err()
}

func (pool *HashPool) RPop(key string) (db.HexString, error) {
	result := pool.db.RPop(key)
	return result.Result()
}

func (pool *HashPool) TransactionLPush(hash db.HexString) error {
	result := pool.db.LPush(TransactionPoolKey, hash)
	return result.Err()
}

func (pool *HashPool) TransactionRPush(hash db.HexString) error {
	result := pool.db.RPush(TransactionPoolKey, hash)
	return result.Err()
}

func (pool *HashPool) ContentLPush(hash db.HexString) error {
	result := pool.db.LPush(ContentPoolKey, hash)
	return result.Err()
}

func (pool *HashPool) BlockLPush(hash db.HexString) error {
	result := pool.db.LPush(BlockPoolKey, hash)
	return result.Err()
}

func (pool *HashPool) ContentRPush(hash db.HexString) error {
	result := pool.db.RPush(ContentPoolKey, hash)
	return result.Err()
}

func (pool *HashPool) ContentPop() (db.HexString, error) {
	result := pool.db.RPop(ContentPoolKey)
	return result.String(), result.Err()
}

func (pool *HashPool) TransactionPop() (db.HexString, error) {
	result := pool.db.RPop(TransactionPoolKey)
	return result.Result()
}

func (pool *HashPool) BlockPop() (db.HexString, error) {
	result := pool.db.RPop(BlockPoolKey)
	return result.Result()
}

func NewHashPool(ctx context.Context) *HashPool {
	_, logger := log.WithCtx(ctx)
	return &HashPool{ctx: ctx, logger: logger, db: rediscache.Get(ConfigValue)}
}
