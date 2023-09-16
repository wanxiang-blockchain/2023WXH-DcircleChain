package sign

import (
	"dcircleserver/chain/db"
	"errors"
	"github.com/ethereum/go-ethereum/rlp"
)

type RLP struct {
	Nonce    uint64
	From     db.EthAddress
	To       db.EthAddress
	OpCode   db.OpCode
	SignTime db.UnixTimeMs
	Payload  []byte
	ChainId  db.ChainId
}

const ToAddress = "wwwwww"

// RLPData Nonce uint64, From EthAddress, To EthAddress, OpCode, SignTime UnixTimeMs, Payload []byte, ChainId
func encodeRLPData(nonce uint64, from db.EthAddress, To db.EthAddress, OpCode db.OpCode, signTime uint64, Payload []byte, ChainId db.ChainId) ([]byte, error) {
	data := []interface{}{
		nonce,
		from,
		To,
		OpCode,
		signTime,
		Payload,
		ChainId,
	}
	return rlp.EncodeToBytes(data)
}

func EncodeRLPData(rlp RLP) (db.RLPData, error) {
	return encodeRLPData(
		rlp.Nonce,
		rlp.From,
		rlp.To,
		rlp.OpCode,
		rlp.SignTime,
		rlp.Payload,
		rlp.ChainId,
	)
}

func DecodeRLPData(encodeData db.RLPData) (RLP, error) {
	var data []interface{}
	err := rlp.DecodeBytes(encodeData, &data)
	if err != nil {
		println("DecodeBytes:", err.Error())
		return RLP{}, err
	}

	if len(data) != 7 {
		return RLP{}, errors.New("rlp data is invalid")
	}
	rlpData := RLP{}

	rlpData.Nonce, err = toUint64(data[0].([]byte))
	if err != nil {
		return RLP{}, errors.New("nonce is invalid")
	}
	rlpData.From = string(data[1].([]byte))
	rlpData.To = string(data[2].([]byte))
	rlp.DecodeBytes(data[3].([]byte), &rlpData.OpCode)
	signTimeBytes, ok := data[4].([]uint8)
	if !ok {
		return RLP{}, errors.New("signTime is invalid")
	}
	rlpData.SignTime, err = toUint64(signTimeBytes)
	if err != nil {
		return RLP{}, errors.New("signTime is invalid")
	}
	rlpData.Payload = data[5].([]byte)
	rlp.DecodeBytes(data[6].([]byte), &rlpData.ChainId)
	return rlpData, nil
}

func toUint64(b []byte) (uint64, error) {
	length := len(b)
	if length == 0 {
		return 0, errors.New("input is null")
	} else if length == 1 {
		return uint64(b[0]), nil
	} else {
		pro := uint64(b[length-1])
		to, err := toUint64(b[:length-1])
		if err != nil {
			return 0, err
		}
		return pro + to*256, nil
	}
}
