package db

import (
	"context"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-db-mongo/mongodb/updater"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

type TransactionDocument struct {
	//keccak256(Message)
	TxnID HexString `bson:"_id"`
	Nonce uint64

	// 属于哪一个块
	BlockHash HexString
	From      EthAddress
	To        EthAddress

	Gas      DCChainValue
	GasPrice DCChainValue
	GasFees  DCChainValue
	// 最大限制的gas
	GasLimit DCChainValue

	Value          DCChainValue
	TransactionFee DCChainValue

	TxnType uint64
	// 签名数据
	Data RLPData

	// 签名相关
	//V string
	//R string
	//S string

	Sign HexString

	// 交易时间
	Timestamp UnixTimeMs
}

type Transaction struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *Transaction) collection() *mongo.Collection {
	const colName = "Transaction"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *Transaction) field() *TransactionDocument0Field {
	return NewTransactionDocument0Field("")
}

func (col *Transaction) NewTransaction(txnID, sign HexString, from, to EthAddress,
	value, gasFees, gasLimit DCChainValue, txnType uint64, data RLPData) error {
	doc := TransactionDocument{
		TxnID:     txnID,
		Sign:      sign,
		From:      from,
		To:        to,
		Value:     value,
		GasFees:   gasFees,
		GasLimit:  gasLimit,
		TxnType:   txnType,
		Data:      data,
		Timestamp: UnixTimeMs(time.Now().UnixMilli()),
	}
	_, err := col.collection().InsertOne(col.ctx, doc)
	return err
}

func (col *Transaction) GetTransactionByTxId(txId HexString) (*TransactionDocument, error) {
	var doc *TransactionDocument
	err := col.collection().FindOne(col.ctx, col.field().TxnID().Eq(txId).ToBsonD()).Decode(&doc)
	return doc, err
}

func (col *Transaction) UpdateTransactionByTxId(txIds []HexString, blockHash HexString) error {
	updateDoc := updater.Batch(
		col.field().BlockHash().Set(blockHash),
	)
	_, err := col.collection().UpdateOne(col.ctx, col.field().TxnID().In(txIds).ToBsonD(), updateDoc.ToBsonM())
	return err
}

func NewTransaction(ctx context.Context) *Transaction {
	_, logger := log.WithCtx(ctx)
	return &Transaction{
		ctx:    ctx,
		logger: logger,
	}
}
