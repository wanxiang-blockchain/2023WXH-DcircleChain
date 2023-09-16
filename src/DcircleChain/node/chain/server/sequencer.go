package server

import (
	"context"
	"dcircleserver/src/chain/db"
	"dcircleserver/src/chain/db/redis"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"strconv"
	"strings"
)

func (node *NodeServer) newBlock(ctx context.Context) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("runSequencer:")
	// 获取现在的BlockNo
	lastBlock, err := db.NewBlock(node.ctx).GetLatest()
	if err != nil && mongo.ErrNoDocuments != err {
		panic(err)
	}

	blockNo := int64(1)
	previousHash := ""
	txHashes := []string{}
	contentHashes := []string{}

	if mongo.ErrNoDocuments != err {
		blockNo = lastBlock.BlockNumber
		previousHash = lastBlock.BlockHash
	}
	// 生成新的块高
	newBlockNo := blockNo + 1
	// 拿到交易
	nums := 0
	for nums <= ChainServerNode.BlockMaxTransaction {
		txHash, err := redis.NewHashPool(ctx).TransactionPop()
		if err != nil {
			logger.Debug("error:", err)
			break
		}
		txHashes = append(txHashes, txHash)
		nums++
	}
	// 拿到内容hash
	contentNums := 0
	for contentNums <= ChainServerNode.BlockMaxContent {
		contentHash, err := redis.NewHashPool(node.ctx).ContentPop()
		if err != nil {
			logger.Debug("error:", err)
			break
		}
		contentHashes = append(contentHashes, contentHash)
		contentNums++
	}
	// 计算hash
	txBytes := []byte(strings.Join(txHashes, "") + strings.Join(contentHashes, ""))
	blockHash := crypto.Keccak256Hash(txBytes).Hex()
	// 有内容或者交易使用交易和内容
	if len(txHashes) == 0 && len(contentHashes) == 0 {
		blockHash = crypto.Keccak256Hash([]byte(strconv.Itoa(int(newBlockNo)))).Hex()
	}
	// 存储block
	err = db.NewBlock(ctx).NewBlock(previousHash, blockHash, newBlockNo, txHashes, contentHashes)
	if err != nil && !mongo.IsDuplicateKeyError(err) {
		// 写入失败
		logger.Error(err)
		// 错误重新投入池子
		for _, tx := range txHashes {
			redis.NewHashPool(ctx).TransactionRPush(tx)
		}
		// 错误重新投入池子
		for _, cHash := range contentHashes {
			redis.NewHashPool(ctx).ContentRPush(cHash)
		}
		return
	}
	err = redis.NewHashPool(ctx).BlockLPush(blockHash)
	if err != nil {
		logger.Error(err)
		return
	}
	logger.Debug("new Block:", newBlockNo, " ,tx count:", len(txHashes), ", content count:", len(contentHashes))
}
