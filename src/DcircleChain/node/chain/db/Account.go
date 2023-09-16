package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-db-mongo/mongodb/updater"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type AccountDocument struct {
	AccountID  EthAddress `bson:"_id"`
	Balance    DCChainValue
	UpdateTime UnixTimeMs
}

type Account struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Account) collection() *mongo.Collection {
	const colName = "account"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Account) field() *AccountDocument0Field {
	return NewAccountDocument0Field("")
}

func (col *Account) initBalance(address string, amount int64) (err error) {
	_, err = col.collection().InsertOne(col.ctx, &AccountDocument{
		AccountID:  address,
		Balance:    DCChainValue(amount),
		UpdateTime: UnixTimeMs(time.Now().UnixMilli()),
	})
	return
}

func (col *Account) GetBalance(address EthAddress) (*AccountDocument, error) {
	var doc *AccountDocument
	filter := col.field().AccountID().Eq(address).ToBsonD()
	err := col.collection().FindOne(col.ctx, filter).Decode(&doc)
	return doc, err
}

func (col *Account) UpdateBalance(address string, amount int64) error {
	doc := updater.Batch(
		col.field().Balance().Set(amount),
		col.field().UpdateTime().Set(UnixTimeMs(time.Now().UnixMilli())),
	)
	err := col.collection().FindOneAndUpdate(col.ctx, col.field().AccountID().Eq(address), doc.ToBsonM()).Decode(doc)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			err = col.initBalance(address, amount)
		}
		return err
	}
	return nil
}

func NewAccount(ctx context.Context) *Account {
	_, logger := log.WithCtx(ctx)
	return &Account{ctx: ctx, logger: logger}
}
