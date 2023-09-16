package api

import (
	"context"
	"dcircleserver/src/chain/api"
	"dcircleserver/src/chain/db"
	chainSign "dcircleserver/src/chain/sign"
	"dcircleserver/src/chain/utils"
	"errors"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type SignatureRequest struct {
	Address  db.EthAddress `json:"signAddress"`
	Message  db.HexString  `json:"message"`
	Sign     db.HexString  `json:"sign"`
	Nonce    uint64        `json:"nonce"`
	SignHash db.HexString  `json:"signHash"`
}

type SignatureResponse struct {
	Result       api.Result   `json:"result"`
	SignHash     db.HexString `json:"signHash"`
	CurrentNonce uint64       `json:"currentNonce"`
}

const (
	ResultNonceValidation = -2
)

func (s *Suite) APISignature(ctx context.Context, request *SignatureRequest) *SignatureResponse {
	ctx, logger := utils.NewContextWithReqId(ctx, s.Request.ReqID)
	logger.PushPrefix("api: Signature, ")
	logger.Debug("start")
	logger.Debug(fmt.Sprintf("req: %v", request))

	if "" == request.Sign {
		logger.Warning("sign is need!")
		s.Request.Terminate(errors.New("sign is need"))
	}

	if 0 == request.Nonce {
		logger.Warning("Nonce is need!")
		s.Request.Terminate(errors.New("nonce is need"))
	}

	if "" == request.Address {
		logger.Warning("address is need!")
		s.Request.Terminate(errors.New("address is need"))
	}

	if "" == request.Message {
		logger.Warning("message is need!")
		s.Request.Terminate(errors.New("message is need"))
	}

	result := chainSign.Verify(ctx, request.Nonce, request.Address, request.Message, request.Sign)
	if result.NonceInValid {
		logger.Warning("Nonce validation failed")
		return &SignatureResponse{Result: ResultNonceValidation, CurrentNonce: 1}
	}

	if result.Error != nil {
		logger.Error(result.Error.Error())
		s.Request.Terminate(result.Error)
	}

	signLogProcess(ctx, result.SignHash, request)
	return &SignatureResponse{Result: api.ResultSuccess, SignHash: result.SignHash}
}

func signLogProcess(ctx context.Context, signHash string, request *SignatureRequest) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("signLogProcess")
	opCode, err := chainSign.GetOpCode(ctx, request.Message)
	if err != nil {
		logger.Error(err)
		return
	}

	data := &db.SignLogInfo{
		Action:  db.GetOpCodeAction(opCode),
		Address: request.Address,
	}

	err = db.NewSignLog(ctx).SetDataAndStatusById(signHash, db.SignLogSuccess, data.ToString(), "")
	if err != nil {
		logger.Error(err)
		return
	}
}
