package redis

import (
	"context"
	"github.com/go-redis/redis"
	"github.com/xpwu/go-db-redis/rediscache"
	"github.com/xpwu/go-log/log"
)

const SignNoncePrefix = "SIGN_NONCE:"

func SignNonceKeyWithAddress(address string) string {
	return SignNoncePrefix + address
}

type Nonce struct {
	db     *redis.Client
	ctx    context.Context
	logger *log.Logger
}

func (n *Nonce) GetNonce(key string) (nonce uint64, err error) {
	nonce, err = n.db.Get(key).Uint64()
	return
}

func (n *Nonce) SetNonce(key string, value uint64) (err error) {
	ret := n.db.Set(key, value, 0)
	return ret.Err()
}

func NewNonce(ctx context.Context) *Nonce {
	_, logger := log.WithCtx(ctx)
	return &Nonce{ctx: ctx, logger: logger, db: rediscache.Get(ConfigValue)}
}
