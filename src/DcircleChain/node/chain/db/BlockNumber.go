package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
)

const BlockNumberId = "BlockNumber"

type BlockNumberDocument struct {
	Key    string `bson:"_id"`
	Number uint64
}

type BlockNumber struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *BlockNumber) collection() *mongo.Collection {
	const colName = "BlockNumber"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *BlockNumber) field() *BlockNumberDocument0Field {
	return NewBlockNumberDocument0Field("")
}

func (col *BlockNumber) initBlockNumber() (number uint64, err error) {
	doc := &BlockNumberDocument{Key: BlockNumberId, Number: 1}
	_, err = col.collection().InsertOne(col.ctx, doc)
	if err != nil {
		return 0, err
	}
	return doc.Number, nil
}

func (col *BlockNumber) NewBlockNumber() (nonce uint64, err error) {
	doc := new(BlockNumberDocument)
	err = col.collection().FindOneAndUpdate(
		col.ctx,
		col.field().Key().Eq(BlockNumberId).ToBsonD(),
		col.field().Number().Inc(1).ToBsonM()).Decode(doc)
	if err == nil {
		return doc.Number, nil
	}
	if err != mongo.ErrNoDocuments {
		return 0, err
	}
	return col.initBlockNumber()
}

func NewBlockNumber(ctx context.Context) *BlockNumber {
	_, logger := log.WithCtx(ctx)
	return &BlockNumber{ctx: ctx, logger: logger}
}
