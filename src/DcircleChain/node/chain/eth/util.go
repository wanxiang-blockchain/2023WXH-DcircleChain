package eth

import (
	"context"
	"dcircleserver/src/chain/db"
	"dcircleserver/src/chain/eth/contracts"
	"github.com/ethereum/go-ethereum/accounts/abi/bind"
	"github.com/ethereum/go-ethereum/common"
	"github.com/xpwu/go-log/log"
)

// RefreshTokenBalance 刷新token余额/**
func RefreshTokenBalance(ctx context.Context, client bind.ContractBackend, tokenAddress common.Address, address db.EthAddress) error {
	_, logger := log.WithCtx(ctx)
	token, err := contracts.NewToken(tokenAddress, client)
	if err != nil {
		logger.Error(err)
		return err
	}
	number, err := token.BalanceOf(nil, common.HexToAddress(address))
	if err != nil {
		logger.Error(err)
		return err
	}
	err = db.NewAccount(ctx).UpdateBalance(address, number.Int64())
	if err != nil {
		logger.Error(err)
		return err
	}
	return nil
}

func RollUp() {
	
}
