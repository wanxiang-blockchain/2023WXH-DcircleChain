package server

import (
	"context"
	"dcircleserver/chain/db"
	"dcircleserver/chain/eth"
	"dcircleserver/chain/eth/contracts"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/ethclient"
	"github.com/xpwu/go-log/log"
	"math/big"
	"time"
)

var TokenAddress common.Address
var client *ethclient.Client

type RpcTransactionLog struct {
	Address         db.HexString   `json:"address"`
	Topics          []db.HexString `json:"topics"`
	TransactionHash db.HexString   `json:"transactionHash"`
}

type RpcTransaction struct {
	//tx *types.Transaction
	BlockNumber     *string             `json:"blockNumber,omitempty"`
	BlockHash       *common.Hash        `json:"blockHash,omitempty"`
	From            *common.Address     `json:"from,omitempty"`
	Hash            string              `json:"hash"`
	Nonce           string              `json:"nonce"`
	ContractAddress string              `json:"contractAddress"`
	To              string              `json:"to"`
	Input           string              `json:"input"`
	Logs            []RpcTransactionLog `json:"logs"`
	Raw             string              `json:"raw"`
	Value           string              `json:"value"`
	GasPrice        string              `json:"gasPrice"`
	Gas             string              `json:"gas"`
	Status          string              `json:"status"`
}

func init() {
	var err error
	TokenAddress = common.HexToAddress(ChainServerNode.TokenAddress)
	client, err = ethclient.Dial(ChainServerNode.ChainRpcAddress)
	if err != nil {
		log.Fatal(err)
	}
}

type NodeServer struct {
	ctx    context.Context
	logger *log.Logger
}

func (node *NodeServer) RunAll() {
	go node.runRollup()
	go node.runSequencer()
	go node.runScanTransaction()
}

func (node *NodeServer) runScanTransaction() {
	headers := make(chan *types.Header)
	sub, err := client.SubscribeNewHead(context.Background(), headers)
	if err != nil {
		node.logger.Fatal(err)
	}

	// TODO 断网或者崩溃之后的处理
	node.logger.Debug("runScanTransaction....")
	for {
		select {
		case err := <-sub.Err():
			log.Fatal(err)
		case header := <-headers:
			block, err := client.BlockByHash(node.ctx, header.Hash())
			if err != nil {
				log.Fatal(err)
				continue
			}

			log.Debug(fmt.Sprintf("块(%d)交易总数：%d", block.Number(), len(block.Transactions())))
			for _, transaction := range block.Transactions() {
				//log.Debug("transaction.Type()", transaction.Type())
				if transaction.To() != nil && transaction.To().Hex() == TokenAddress.Hex() {
					go node.doTransaction(transaction, block.Number())
				}
			}
		}

	}
}

func Start() {
	ctx := context.Background()
	_, logger := log.WithCtx(ctx)
	nodeServer := &NodeServer{ctx: ctx, logger: logger}
	nodeServer.RunAll()
}

func watchAll() {
	t, err := contracts.NewToken(TokenAddress, client)
	if err != nil {

	}
	sinkChan := make(chan<- *contracts.TokenTransfer)
	var from = []common.Address{}
	var to = []common.Address{}
	sub, err := t.WatchTransfer(nil, sinkChan, from, to)
	for {
		select {
		case err := <-sub.Err():
			log.Fatal(err)
		case sinkChan <- &contracts.TokenTransfer{}:

		}
	}
}

func (node *NodeServer) doTransaction(transaction *types.Transaction, blockNumber *big.Int) error {
	var d *RpcTransaction
	err := client.Client().CallContext(node.ctx, &d, "eth_getTransactionReceipt", transaction.Hash())
	if err != nil {
		log.Error("CallContext eth_getTransactionReceipt ", transaction.Hash(), " error:", err)
		return err
	}

	log.Debug(
		fmt.Sprintf(
			"CallContext check nil %s from %s to %s, contractAddress: %s,type: %d",
			transaction.Hash(),
			d.From,
			d.To,
			d.ContractAddress,
			transaction.Type(),
		),
	)

	var ToAddress, FromAddress string = "", ""
	if transaction.To() != nil {
		ToAddress = transaction.To().Hex()
	}
	if d.From != nil {
		FromAddress = d.From.Hex()
	}

	for _, transactionLog := range d.Logs {
		if transactionLog.Address == d.To {
			to := "0x" + transactionLog.Topics[len(transactionLog.Topics)-1][26:]
			log.Debug("to:", to, " eip55:", common.HexToAddress(to).Hex())
			err = eth.RefreshTokenBalance(node.ctx, client, TokenAddress, FromAddress)
			if err != nil {
				log.Error("RefreshBalance ", FromAddress, err)
			}
			return eth.RefreshTokenBalance(node.ctx, client, TokenAddress, common.HexToAddress(to).Hex())
		}
	}

	// From 或To 不为合约的交易跳过 即：只取和合约相关的交易
	//if ToAddress != TokenAddress.Hex() && FromAddress != TokenAddress.Hex() {
	//	return nil
	//}

	log.Debug(
		fmt.Sprintf(
			"block %s CallContext %s from %s to %s, status:%s ",
			blockNumber,
			transaction.Hash(),
			d.From,
			transaction.To(),
			d.Status,
		),
	)

	if ToAddress == "" {
		fmt.Println("transaction.to.nil:", transaction.Hash())
		//continue
	}
	return eth.RefreshTokenBalance(node.ctx, client, TokenAddress, FromAddress)
}

func (node *NodeServer) runSequencer() {
	blockCreateDuration := time.Second * time.Duration(ChainServerNode.BlockCreateFrequencySeconds)
	for {
		select {
		case <-time.Tick(blockCreateDuration):
			node.newBlock(node.ctx)
		}
	}
}

func (node *NodeServer) runRollup() {
	blockCreateDuration := time.Second * time.Duration(ChainServerNode.RollUpL2FrequencySeconds)
	for {
		select {
		case <-time.Tick(blockCreateDuration):
			node.newRollup(node.ctx)
		}
	}
}
