package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"time"
)

type BlockDocument struct {
	BlockHash    HexString `bson:"_id"`
	PreviousHash HexString
	Nonce        int64

	// 块产生的时间
	Timestamp UnixTimeMs
	// 块高
	BlockNumber int64

	GasLimit int64
	GasUsed  int64

	// 交易的hash
	TransactionHashes []string
	// 内容hash
	ContentHashes []string
	Difficulty    int64
	//ReceiptTree TransactionMerkleTree
}

type Block struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Block) collection() *mongo.Collection {
	const colName = "Block"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Block) field() *BlockDocument0Field {
	return NewBlockDocument0Field("")
}

func (col *Block) GetLatest() (*BlockDocument, error) {
	var doc *BlockDocument
	err := col.collection().FindOne(col.ctx, bson.M{},
		options.FindOne().SetSort(col.field().BlockNumber().DescIndex().ToBsonD())).Decode(&doc)
	if err != nil {
		return nil, err
	}
	return doc, nil
}

func (col *Block) NewBlock(previousHash, blockHash HexString, blockNumber int64, transactionHashes []string, contentHashes []string) error {
	doc := BlockDocument{
		BlockHash:         blockHash,
		PreviousHash:      previousHash,
		BlockNumber:       blockNumber,
		TransactionHashes: transactionHashes,
		ContentHashes:     contentHashes,
		Timestamp:         UnixTimeMs(time.Now().UnixMilli()),
	}
	_, err := col.collection().InsertOne(col.ctx, doc)
	return err
}

func NewBlock(ctx context.Context) *Block {
	_, logger := log.WithCtx(ctx)
	return &Block{
		ctx:    ctx,
		logger: logger,
	}
}
