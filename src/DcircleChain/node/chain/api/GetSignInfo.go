package api

import (
	"context"
	"dcircleserver/src/chain/db"
	"dcircleserver/src/im/api"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetSignInfoRequest struct {
	TxID db.HexString `json:"txID"`
}

type SignInfo struct {
	TxID     db.HexString  `json:"txID"`
	From     db.EthAddress `json:"fromAddress"`
	Nonce    uint64        `json:"nonce"`
	To       db.EthAddress `json:"toAddress"`
	OpCode   db.OpCode     `json:"opCode"`
	SignTime db.UnixTimeMs `json:"SignTime"`
	Message  []byte        `json:"message"`
	PayLoad  []byte        `json:"payLoad"`
}

type GetSignInfoResponse struct {
	Result   api.Result `json:"result"`
	SignInfo SignInfo   `json:"signInfo"`
}

func (s *Suite) APIGetSignInfo(ctx context.Context, request *GetSignInfoRequest) *GetSignInfoResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("api: APIGetSignInfo, ")
	logger.Debug("start")
	logger.Debug(fmt.Sprintf("req: %v", request))
	if "" == request.TxID {
		logger.Warning("hash is need!")
		return &GetSignInfoResponse{Result: api.ResultFail}
	}

	doc, err := db.NewSign(ctx).FindByTxId(request.TxID)
	if err != nil {
		logger.Warning("get sign info is fail" + err.Error())
		return &GetSignInfoResponse{Result: api.ResultFail}
	}
	info := SignInfo{
		TxID:     request.TxID,
		From:     doc.From,
		Nonce:    doc.Nonce,
		To:       doc.To,
		OpCode:   doc.OpCode,
		SignTime: doc.SignTime,
		Message:  doc.Message,
		PayLoad:  doc.Payload,
	}
	return &GetSignInfoResponse{
		Result:   api.ResultSuccess,
		SignInfo: info,
	}
}
