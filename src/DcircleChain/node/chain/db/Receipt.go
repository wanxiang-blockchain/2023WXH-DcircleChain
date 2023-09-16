package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type ReceiptDocument struct {
	TransactionHash HexString `bson:"_id"`
	BlockHash       HexString
	BlockNumber     int64
	From            EthAddress
	To              EthAddress
	TxType          string
	Status          string
	Timestamp       UnixTimeMs
}

type Receipt struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Receipt) collection() *mongo.Collection {
	const colName = "Receipt"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Receipt) field() *ReceiptDocument0Field {
	return NewReceiptDocument0Field("")
}

func (col *Receipt) NewReceipt(txHash HexString, from, to EthAddress, txType string) error {
	doc := ReceiptDocument{
		TransactionHash: txHash,
		From:            from,
		To:              to,
		TxType:          txType,
		Status:          TransactionStatusPending,
		Timestamp:       UnixTimeMs(time.Now().UnixMilli()),
	}
	_, err := col.collection().InsertOne(col.ctx, doc)
	return err
}

func (col *Receipt) UpdateStatusToSuccess(txHash HexString) error {
	filter := col.field().TransactionHash().Eq(txHash)
	update := col.field().Status().Set(TransactionStatusSuccess)
	_, err := col.collection().UpdateOne(col.ctx, filter.ToBsonD(), update.ToBsonM())
	return err
}

func (col *Receipt) UpdateStatusToFailure(txHash HexString) error {
	filter := col.field().TransactionHash().Eq(txHash)
	update := col.field().Status().Set(TransactionStatusFailure)
	_, err := col.collection().UpdateOne(col.ctx, filter.ToBsonD(), update.ToBsonM())
	return err
}

func NewReceipt(ctx context.Context) *Receipt {
	_, logger := log.WithCtx(ctx)
	return &Receipt{
		ctx:    ctx,
		logger: logger,
	}
}
