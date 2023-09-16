package api

import (
	"context"
	"dcircleserver/src/chain/api"
	db2 "dcircleserver/src/chain/db"
	"errors"
	"go.mongodb.org/mongo-driver/mongo"
)

type GetBalanceRequest struct {
	Address string `json:"address"`
}

type GetBalanceResponse struct {
	*api.Response
	Balance int64
}

// APIGetBalance
// 功能：获取一个地址的余额
// 初始为0 用完更新
// 存放在 mongodb
func (s *Suite) APIGetBalance(ctx context.Context, request *GetBalanceRequest) *GetBalanceResponse {
	if request.Address == "" {
		s.Request.Terminate(errors.New("address is empty!"))
	}
	doc, err := db2.NewAccount(ctx).GetBalance(request.Address)
	if err == mongo.ErrNoDocuments {
		return &GetBalanceResponse{Balance: 0, Response: api.SucceedResponse()}
	}
	if err != nil {
		s.Request.Terminate(err)
	}
	return &GetBalanceResponse{Balance: doc.Balance, Response: api.SucceedResponse()}
}
