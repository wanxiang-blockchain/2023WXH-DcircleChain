package api

import (
	"context"
	"dcircleserver/src/chain/db"
	"dcircleserver/src/im/api"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetNonceRequest struct {
	Address string `json:"address"`
}

type GetNonceResponse struct {
	api.Response
	Nonce uint64 `json:"nonce"`
}

// APIGetNonce
// 功能：获取一个用户随机数，每个用户可能一样
// 初始为0 用完更新
// 存放在 mongodb
func (s *Suite) APIGetNonce(ctx context.Context, request *GetNonceRequest) *GetNonceResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("api: GetNonce, ")
	logger.Debug("start")

	//key := redis.SignNonceKeyWithAddress(request.Address)
	//nonce, err := redis.NewNonce(ctx).GetNonce(key)
	nonce, err := db.NewNonce(ctx).GetNonceWithAddressOrInit(request.Address)
	if err != nil {
		logger.Error(fmt.Sprintf("GetNonce err %v", err))
		s.Request.Terminate(err)
	}
	return &GetNonceResponse{Nonce: nonce}
}
