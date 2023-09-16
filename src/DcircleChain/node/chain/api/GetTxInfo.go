package api

import (
	"context"
	"dcircleserver/src/im/db"
	"fmt"
	"github.com/xpwu/go-log/log"
	"time"
)

type GetTxInfoRequest struct {
	TxHash string `json:"txHash"`
}

type GetTxInfoResponse struct {
	TxHash           string `json:"txHash"`
	PayStatus        int    `json:"payStatus"` // 2 Paid
	BuyAddress       string `json:"buyUserid"`
	BuySignData      string `json:"buySignData"`
	DIDAddress       string `json:"didAddress"`
	CreateSignData   string `json:"createSignData"`
	CreatorAddress   string `json:"creatorAddress"`
	TxTime           int64  `json:"txTime"`
	SourceChatId     string `json:"sourceChatId"`
	TransferId       string `json:"transferId"`
	TransferSignData string `json:"transferSignData"`
	MNums            int64  `json:"mNums"`
	Nonce            int64  `json:"nonce"`
}

func (s *suite) APIGetTxInfo(ctx context.Context, request *GetTxInfoRequest) *GetTxInfoResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetTxInfo")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))
	if len(request.TxHash) == 0 {
		return &GetTxInfoResponse{}
	}

	//return GetTestTxData(request.TxHash)
	doc, err := db.NewDIDStatPurchase(ctx).FindById(request.TxHash)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	if len(doc.Id) == 0 {
		logger.Debug("didn't find TxId:", request.TxHash)
		return &GetTxInfoResponse{}
	}
	res := &GetTxInfoResponse{
		TxHash:           doc.Id,
		PayStatus:        doc.PayStatus,
		BuyAddress:       doc.BuyerUid,
		BuySignData:      "",
		DIDAddress:       doc.Address,
		CreateSignData:   "",
		CreatorAddress:   doc.DIDCreateUserid,
		TxTime:           int64(doc.BuyTime),
		SourceChatId:     ShowChatId(doc.SourceChatId, doc.SourceChatType),
		TransferId:       doc.TransferUid,
		TransferSignData: "",
		MNums:            int64(doc.TokenNums),
		Nonce:            0, //todo xhb
	}

	return res
}

func ShowChatId(chatId string, chatType int) string {
	const InvisibleChatId = "-"

	if chatType != db.ChatTypePublicGroup && chatType != db.ChatTypePublicChannel {
		return InvisibleChatId
	}
	return chatId
}

func GetTestTxData(txHash string) *GetTxInfoResponse {
	return &GetTxInfoResponse{
		TxHash:         txHash,
		PayStatus:      2,
		BuyAddress:     "0x1234567894344343434",
		DIDAddress:     "0xaaaaaaaa555555555",
		CreatorAddress: "0x12343435345345345333",
		TxTime:         time.Now().UnixMilli(),
		SourceChatId:   "-",
		TransferId:     "0xabbabababab12233443343",
		MNums:          int64(time.Now().Second() % 1000),
		Nonce:          9527, //todo xhb
	}
}
