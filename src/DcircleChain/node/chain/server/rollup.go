package server

import (
	"context"
	"dcircleserver/chain/db"
	"dcircleserver/chain/db/redis"
	"dcircleserver/chain/eth/contracts"
	"github.com/ethereum/go-ethereum/accounts/abi/bind"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"math/big"
	"strings"
	"time"
)

func (node *NodeServer) newRollup(ctx context.Context) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("newRollup:")
	MAX := 1000
	anchoringNumber := int64(1)
	previousHash := ""
	rootHashes := []string{}

	// 获取现在的BlockNo
	last, err := db.NewAnchoring(ctx).GetLatest()
	if err != nil && mongo.ErrNoDocuments != err {
		panic(last)
	}

	if mongo.ErrNoDocuments != err {
		anchoringNumber = last.AnchoringNumber
		previousHash = last.RootHash
	}
	// 生成新的rollup高
	newAnchoringNumber := anchoringNumber + 1
	// 拿到块
	nums := 0
	for nums <= MAX {
		blockHash, err := redis.NewHashPool(ctx).BlockPop()
		if err != nil {
			logger.Debug("error:", err)
			break
		}
		rootHashes = append(rootHashes, blockHash)
		nums++
	}
	// 计算hash
	txBytes := []byte(strings.Join(rootHashes, ""))
	rootHash := crypto.Keccak256Hash(txBytes).Hex()
	// 存储block
	err = db.NewAnchoring(ctx).NewAnchoring(previousHash, rootHash, newAnchoringNumber, rootHashes, nil)
	if err != nil && !mongo.IsDuplicateKeyError(err) {
		// 写入失败
		logger.Error(err)
		// 错误重新投入池子
		for _, blockHash := range rootHashes {
			redis.NewHashPool(ctx).BlockLPush(blockHash)
		}
		return
	}
	logger.Debug("new rollup:", newAnchoringNumber)
	dcchain, err := contracts.NewDCChain(TokenAddress, client)
	current := big.NewInt(time.Now().UnixMilli())
	rollupPoint := contracts.DCRC20RollupPoint{
		RootHash:      common.HexToAddress(rootHash),
		Transactions:  []byte(""),
		ContentHashes: []byte(""),
		RollupTime:    current,
	}
	privateKey, err := crypto.HexToECDSA(ChainServerNode.TrustedSequencerPrivateKey)
	if err != nil {
		logger.Error(err)
		return
	}
	auth, err := bind.NewKeyedTransactorWithChainID(privateKey, big.NewInt(ChainServerNode.ChainID))
	if err != nil {
		logger.Error(err)
		return
	}
	tx, err := dcchain.SetCurrentRollupPoint(auth, rollupPoint)
	if err != nil {
		logger.Error(err)
		return
	}
	logger.Debug("new rollup anchoringNumber:", anchoringNumber, " \ntxn:", tx.Hash().Hex())
}
