package sign

import (
	"crypto/ecdsa"
	"dcircleserver/chain/db"
	"encoding/hex"
	"errors"
	"github.com/btcsuite/btcd/btcec/v2"
	"github.com/ethereum/go-ethereum/crypto"
)

func RecoverPublicKeyFromSignature(hexSignature db.HexString, message []byte) (string, error) {
	signature, err := hex.DecodeString(hexSignature)
	if err != nil {
		return "", err
	}
	hashedMessage := crypto.Keccak256(message)
	recoverPubKey, err := crypto.Ecrecover(hashedMessage, signature)
	if err != nil {
		return "", err
	}
	signatureNoRecoverID := signature[:len(signature)-1]
	verify := crypto.VerifySignature(recoverPubKey, hashedMessage, signatureNoRecoverID)
	if verify {
		return hex.EncodeToString(recoverPubKey), nil
	} else {
		return "", errors.New("verify is fail")
	}
}

func HexDecode(hexString db.HexString) ([]byte, error) {
	data, err := hex.DecodeString(hexString)
	if err != nil {
		return nil, err
	}
	return data, nil
}

func RecoverAddressWithPublicKey(publicKeyHex string) (string, error) {
	publicKey, err := NewPublicKeyFormHex(publicKeyHex)
	if err != nil {
		return "", err
	}
	return crypto.PubkeyToAddress(*publicKey).Hex(), nil
}

func NewPublicKeyFromBytes(b []byte) (*ecdsa.PublicKey, error) {
	publicKey, err := btcec.ParsePubKey(b)
	if err != nil {
		return nil, err
	}
	return publicKey.ToECDSA(), nil
}

func NewPublicKeyFormHex(s string) (*ecdsa.PublicKey, error) {
	res, err := hex.DecodeString(s)
	if err != nil {
		return nil, err
	}
	return NewPublicKeyFromBytes(res)
}
