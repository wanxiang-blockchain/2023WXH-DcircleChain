package db

import (
	"context"
	"github.com/xpwu/go-cmd-dbindex/indexcmd"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
)

func init() {
	indexcmd.Add(func() indexcmd.Creator {
		return NewNonce(context.TODO())
	}, "create Nonce Index")
}

type NonceDocument struct {
	UserID EthAddress `bson:"_id"`
	Nonce  uint64
}

type Nonce struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Nonce) CreateIndex() {

}

func (col *Nonce) collection() *mongo.Collection {
	const colName = "nonce"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Nonce) field() *NonceDocument0Field {
	return NewNonceDocument0Field("")
}

func (col *Nonce) GetNonceWithAddress(address EthAddress) (nonce uint64, err error) {
	if len(address) == 0 {
		return
	}
	var doc = &NonceDocument{}
	err = col.collection().FindOne(col.ctx, col.field().UserID().Eq(address).ToBsonD()).Decode(doc)
	if err != nil {
		return
	}
	return doc.Nonce, nil
}

func (col *Nonce) GetNonceWithAddressOrInit(address EthAddress) (nonce uint64, err error) {
	var doc = &NonceDocument{}
	err = col.collection().FindOne(col.ctx, col.field().UserID().Eq(address).ToBsonD()).Decode(doc)
	if err == nil {
		nonce = doc.Nonce
		return
	}
	if err != mongo.ErrNoDocuments {
		return
	}

	return col.InitNonce(address)
}

func (col *Nonce) InitNonce(address EthAddress) (nonce uint64, err error) {
	doc := &NonceDocument{UserID: address, Nonce: uint64(1)}
	_, err = col.collection().InsertOne(col.ctx, doc)
	if err != nil {
		return 0, err
	}
	return doc.Nonce, nil
}

//func (col *Nonce) SetNonceWithAddress(userid EthAddress, nonce uint64) error {
//	filter := col.field().UserID().Eq(userid)
//	update := col.field().Nonce().Set(nonce)
//	_, err := col.collection().UpdateOne(col.ctx, filter.ToBsonD(), update.ToBsonM())
//	if err == mongo.ErrNoDocuments {
//		doc := &NonceDocument{UserID: userid, Nonce: nonce}
//		_, err := col.collection().InsertOne(col.ctx, doc)
//		if err != nil {
//			return err
//		}
//	}
//	if err != nil && err != mongo.ErrNoDocuments {
//		return err
//	}
//	return nil
//}

func (col *Nonce) UpdateNonceWithAddress(address EthAddress) (nonce uint64, err error) {
	doc := new(NonceDocument)
	err = col.collection().FindOneAndUpdate(col.ctx, col.field().UserID().Eq(address).ToBsonD(), col.field().Nonce().Inc(1).ToBsonM()).Decode(doc)
	if err == nil {
		return doc.Nonce, nil
	}

	if err != mongo.ErrNoDocuments {
		return 0, err
	}

	return col.InitNonce(address)
}

//func (col *Nonce) UpdateNonceWithAddressAndPreVersion(address EthAddress, preVersion int) (err error) {
//	_, err = col.collection().UpdateOne(col.ctx,
//		filter.And(
//			col.field().UserID().Eq(address),
//			col.field().Nonce().Eq(uint64(preVersion))).ToBsonD(),
//		col.field().Nonce().Inc(1).ToBsonM())
//
//	return
//}

func NewNonce(ctx context.Context) *Nonce {
	_, logger := log.WithCtx(ctx)
	return &Nonce{ctx: ctx, logger: logger}
}
