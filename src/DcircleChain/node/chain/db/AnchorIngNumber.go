package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
)

const AnchorIngNumberId = "AnchorIngNumber"

type AnchorIngNumberDocument struct {
	Key    string `bson:"_id"`
	Number uint64
}

type AnchorIngNumber struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *AnchorIngNumber) collection() *mongo.Collection {
	const colName = "AnchorIngNumber"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *AnchorIngNumber) field() *BlockNumberDocument0Field {
	return NewBlockNumberDocument0Field("")
}

func (col *AnchorIngNumber) initAnchorIngNumber() (number uint64, err error) {
	doc := &BlockNumberDocument{Key: AnchorIngNumberId, Number: 1}
	_, err = col.collection().InsertOne(col.ctx, doc)
	if err != nil {
		return 0, err
	}
	return doc.Number, nil
}

func (col *AnchorIngNumber) NewAnchorIngNumber() (nonce uint64, err error) {
	doc := new(AnchorIngNumberDocument)
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
	return col.initAnchorIngNumber()
}

func NewAnchorIngNumber(ctx context.Context) *AnchorIngNumber {
	_, logger := log.WithCtx(ctx)
	return &AnchorIngNumber{ctx: ctx, logger: logger}
}
