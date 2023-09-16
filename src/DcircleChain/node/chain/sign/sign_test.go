package sign

import (
	"encoding/json"
	"fmt"
	"testing"
)

func TestRLPEnCode(t *testing.T) {
	rlp := RLP{
		Nonce:    1111,
		From:     "from",
		To:       "to",
		OpCode:   0,
		SignTime: uint64(10000000),
		Payload:  []byte("i'm superman"),
		ChainId:  2,
	}

	rlpByte, err := EncodeRLPData(rlp)
	fmt.Printf("%v", rlpByte)
	if err != nil {
		t.Error(err)
		return
	}

	decodeRlp, err := DecodeRLPData(rlpByte)
	if err != nil {
		t.Error(err)
		return
	}

	msg, _ := json.Marshal(decodeRlp)
	println(string(msg))
	if decodeRlp.OpCode != rlp.OpCode {
		t.Errorf("OpCode excepted %d,actual %d", rlp.OpCode, decodeRlp.OpCode)
		return
	}

	if decodeRlp.From != rlp.From {
		t.Errorf("From excepted %s,actual %s", rlp.From, decodeRlp.From)
		return
	}

	if decodeRlp.To != rlp.To {
		t.Errorf("To excepted %s,actual %s", rlp.To, decodeRlp.To)
		return
	}

	if decodeRlp.ChainId != rlp.ChainId {
		t.Errorf("ChainId excepted %d,actual %d", rlp.ChainId, decodeRlp.ChainId)
		return
	}

	if decodeRlp.SignTime != rlp.SignTime {
		t.Errorf("SignTime excepted %d,actual %d", rlp.SignTime, decodeRlp.SignTime)
		return
	}

}
