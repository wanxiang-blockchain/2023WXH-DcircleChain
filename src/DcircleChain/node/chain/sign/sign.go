package sign

import (
	"context"
	"dcircleserver/chain/db"
	"errors"
	"fmt"
	"github.com/google/uuid"
	"github.com/xpwu/go-log/log"
	"github.com/xpwu/go-tinyserver/api"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type VerifyResponse struct {
	Error        error
	PublicKey    db.HexPublicKey
	RLP          RLP
	NonceInValid bool
	Address      db.EthAddress
	SignHash     db.HexString
}

type VerifyRequest struct {
	Signature string        `json:"signature"`
	Nonce     uint64        `json:"nonce"`
	From      db.EthAddress `json:"from"`
	Message   db.HexString  `json:"message"`
}

func VerifyNonce(ctx context.Context, fromNonce uint64, address db.EthAddress) (uint64, error) {
	nonce, err := db.NewNonce(ctx).GetNonceWithAddress(address)
	if err != nil && err != mongo.ErrNoDocuments {
		return 0, err
	}
	if fromNonce < nonce {
		return 0, errors.New(fmt.Sprintf("fromNonce:%v lt nonce:%v", fromNonce, nonce))
	}
	return nonce, nil
}

func verifyEnvironment(message db.RLPData) (RLP, error) {
	rlpData, err := DecodeRLPData(message)
	log.Debug("rlpData:", rlpData)
	if err != nil {
		return RLP{}, err
	}
	if rlpData.ChainId != ConfigValue.Chain {
		errString := fmt.Sprintf("environment verify is fail,server:%d,client:%d", ConfigValue.Chain, rlpData.ChainId)
		return RLP{}, errors.New(errString)
	}
	return rlpData, nil
}

func VerifySign(from db.EthAddress, message db.RLPData, hexSign db.HexString) (db.HexPublicKey, db.EthAddress, error) {
	publicKey, error := RecoverPublicKeyFromSignature(hexSign, message)
	if error != nil {
		return "", "", error
	}

	address, error := RecoverAddressWithPublicKey(publicKey)
	if error != nil {
		return "", "", error
	}

	if from != address {
		return "", "", errors.New(fmt.Sprintf("from address(%s) != public address(%s)", from, address))
	}
	return publicKey, address, nil
}

func VerifyWithRequest(ctx context.Context, request VerifyRequest) *VerifyResponse {
	if request.Signature == "" {
		return &VerifyResponse{Error: fmt.Errorf("sign is empty")}
	}
	if request.Message == "" {
		return &VerifyResponse{Error: fmt.Errorf("sign message is empty")}
	}
	if request.From == "" {
		return &VerifyResponse{Error: fmt.Errorf("sign from is empty")}
	}
	if request.Nonce == 0 {
		return &VerifyResponse{Error: fmt.Errorf("sign nonce is empty")}
	}
	return Verify(ctx, request.Nonce, request.From, request.Message, request.Signature)
}

func VerifyAndResponse(ctx context.Context, request VerifyRequest, r *api.Request) *VerifyResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("VerifyAndResponse")
	rsp := VerifyWithRequest(ctx, request)
	//err := VerifyWithRequest(ctx, request).Error
	if rsp.Error != nil {
		logger.Error(rsp.Error)
		r.Terminate(rsp.Error)
	}
	return rsp
}

func Verify(ctx context.Context, fromNonce uint64, from db.EthAddress, hexMessage db.HexString, hexSign db.HexString) *VerifyResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("Verify")
	message, err := HexDecode(hexMessage)
	if err != nil {
		logger.Error("HexDecode hexMessage ", err)
		return &VerifyResponse{Error: err}
	}
	nonce, err := VerifyNonce(ctx, fromNonce, from)
	if err != nil {
		logger.Error("VerifyNonce ", err)
		return &VerifyResponse{Error: err, NonceInValid: true}
	}

	rlp, err := verifyEnvironment(message)
	if err != nil {
		logger.Error("verifyEnvironment ", err)
		return &VerifyResponse{Error: err}
	}

	if !db.InOpCode(rlp.OpCode) {
		logger.Error("opcode verify is fail, OpCode:", rlp.OpCode)
		return &VerifyResponse{Error: errors.New("opcode verify is fail")}
	}

	pukKey, address, err := VerifySign(from, message, hexSign)
	if err != nil {
		logger.Error("verifySign ", err)
		return &VerifyResponse{Error: err}
	}

	signBytes, err := HexDecode(hexSign)
	if err != nil {
		logger.Error("hexSign ", err)
		return &VerifyResponse{Error: err}
	}

	signInfo := &db.SignDocument{
		Sign:     signBytes,
		Nonce:    nonce,
		From:     from,
		To:       rlp.To,
		OpCode:   rlp.OpCode,
		Payload:  rlp.Payload,
		Message:  message,
		SignTime: rlp.SignTime,
	}
	signInfo.TxID = db.BuildTxID(message)

	// TODO noah 这里是否考虑用事务保证同时落库
	err = db.NewSign(ctx).Persistence(signInfo)
	if err != nil {
		logger.Error("signInfo ", err)
		return &VerifyResponse{Error: err}
	}

	err = db.NewSignLog(ctx).Insert(&db.SignLogDocument{
		SignHash:   signInfo.TxID,
		ReqId:      uuid.New().String(),
		Status:     0,
		FailReason: "",
		CreateTime: db.UnixTimeMs(time.Now().UnixMilli()),
		Data:       "",
	})
	if err != nil {
		logger.Error("insert signLog ", err)
		return &VerifyResponse{Error: err}
	}
	db.NewNonce(ctx).UpdateNonceWithAddress(address)
	return &VerifyResponse{
		PublicKey: pukKey,
		RLP:       rlp,
		Address:   address,
		SignHash:  signInfo.TxID,
	}
}

func GetOpCode(ctx context.Context, hexMessage db.HexString) (op db.OpCode, err error) {
	message, err := HexDecode(hexMessage)
	if err != nil {
		return
	}

	rlp, err := verifyEnvironment(message)
	if err != nil {
		return
	}
	return rlp.OpCode, nil
}

func GetTxIdByHexMessage(ctx context.Context, hexMessage db.HexString) (txId string) {
	message, err := HexDecode(hexMessage)
	if err != nil {
		return ""
	}

	txId = db.BuildTxID(message)
	return
}
