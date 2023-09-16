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

type AnchoringDocument struct {
	RootHash    HexString
	PreRootHash HexString
	Nonce       int64
	// 锚高
	AnchoringNumber int64
	Timestamp       UnixTimeMs

	// 块hash 默克尔树
	BlockHashTree []string
}

type Anchoring struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Anchoring) collection() *mongo.Collection {
	const colName = "Anchoring"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Anchoring) field() *AnchoringDocument0Field {
	return NewAnchoringDocument0Field("")
}

func (col *Anchoring) GetLatest() (*AnchoringDocument, error) {
	var doc *AnchoringDocument
	err := col.collection().FindOne(col.ctx, bson.M{},
		options.FindOne().SetSort(col.field().AnchoringNumber().DescIndex().ToBsonD())).Decode(&doc)
	if err != nil {
		return nil, err
	}
	return doc, nil
}

func (col *Anchoring) NewAnchoring(parentHash, anchoringHash HexString, anchoringNumber int64, blockTree []string, data []byte) error {
	doc := AnchoringDocument{
		RootHash:        anchoringHash,
		PreRootHash:     parentHash,
		AnchoringNumber: anchoringNumber,
		BlockHashTree:   blockTree,
		Timestamp:       UnixTimeMs(time.Now().UnixMilli()),
	}
	_, err := col.collection().InsertOne(col.ctx, doc)
	return err
}

func NewAnchoring(ctx context.Context) *Anchoring {
	_, logger := log.WithCtx(ctx)
	return &Anchoring{ctx: ctx, logger: logger}
}
