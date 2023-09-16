package db

import (
	"context"
	"dcircleserver/src/chain/db/redis"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/ethereum/go-ethereum/rlp"
	"github.com/xpwu/go-cmd-dbindex/indexcmd"
	"github.com/xpwu/go-db-mongo/mongodb/filter"
	"github.com/xpwu/go-db-mongo/mongodb/index"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func init() {
	indexcmd.Add(func() indexcmd.Creator {
		return NewSign(context.TODO())
	}, "create Sign Index")
}

type SignDocument struct {
	//keccak256(Message)
	TxID     HexString `bson:"_id"`
	Nonce    uint64
	From     EthAddress
	To       EthAddress
	OpCode   OpCode
	SignTime UnixTimeMs
	Payload  []byte

	BlockNumber uint64

	// RLP(Nonce, From, To, OpCode, SignTime, Payload, ChainId)  ChainId : 1 beta & release 2 alpha 3 dev
	Message RLPData
	// ecdsa (privateKey, keccak256(Message))
	Sign []byte
}

func BuildTxID(msg []byte) HexString {
	message, _ := rlp.EncodeToBytes(msg)
	return HexStringFromBytes(crypto.Keccak256(message))
}

type Sign struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Sign) collection() *mongo.Collection {
	const colName = "sign"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Sign) field() *SignDocument0Field {
	return NewSignDocument0Field("")
}

func (col *Sign) Persistence(info *SignDocument) (err error) {
	_, err = col.collection().InsertOne(col.ctx, info)
	if err == nil {
		//  交易入池 待分块
		redis.NewHashPool(col.ctx).TransactionRPush(info.TxID)
	}
	return
}

func (col *Sign) FindByTxId(txId HexString) (*SignDocument, error) {
	var info = &SignDocument{}
	filter := col.field().TxID().Eq(txId)
	result := col.collection().FindOne(col.ctx, filter.ToBsonD())
	err := result.Decode(&info)
	if err != nil {
		return nil, err
	}
	return info, nil
}

func (col *Sign) FindByTo(to HexString) (docs []*SignDocument, err error) {
	docs = []*SignDocument{}
	c, err := col.collection().Find(col.ctx, col.field().To().Eq(to).ToBsonD())
	if err != nil {
		return
	}
	err = c.All(col.ctx, &docs)
	return
}

func (col *Sign) FindByFromAndToAndOpCode(from, to HexString, op OpCode) (docs []*SignDocument, err error) {
	docs = []*SignDocument{}
	op2 := options.Find().SetSort(bson.D{{col.field().Nonce().FullName(), 1}})
	c, err := col.collection().Find(col.ctx,
		filter.And(
			col.field().From().Eq(from),
			col.field().OpCode().Eq(op),
			col.field().To().Eq(to)).ToBsonD(),
		op2)
	if err != nil {
		return
	}
	err = c.All(col.ctx, &docs)
	return
}

func (col *Sign) StatSignCount() (int, error) {
	pipeline := []bson.M{
		bson.M{
			"$match": col.field().TxID().Gte("").ToBsonD(),
		},
		bson.M{
			"$count": "signCount",
		},
	}

	c, err := col.collection().Aggregate(col.ctx, pipeline)
	if err != nil {
		return 0, err
	}
	var s []struct {
		SignCount int `bson:"signCount"`
	}
	err = c.All(col.ctx, &s)
	if len(s) > 0 {
		return s[0].SignCount, err
	}
	return 0, err
}

func (col *Sign) CreateIndex() {
	unique := true
	indexes := []mongo.IndexModel{
		{
			Keys: index.Keys(col.field().From().DescIndex(), col.field().Nonce().AscIndex()).ToBsonD(),
			Options: &options.IndexOptions{
				Unique: &unique,
			},
		},
	}
	_, err := col.collection().Indexes().CreateMany(col.ctx, indexes)
	if err != nil {
		_, logger := log.WithCtx(col.ctx)
		logger.PushPrefix("create index")
		logger.Error(err)
	}
}

func NewSign(ctx context.Context) *Sign {
	_, logger := log.WithCtx(ctx)
	return &Sign{ctx, logger}
}
