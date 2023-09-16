package api

import (
	"context"
	"dcircleserver/src/im/db"
	"fmt"
	"github.com/xpwu/go-log/log"
	"time"
)

type GetDIDArticleLogRequest struct {
	Address string `json:"address"`
}

type GetDIDArticleLogItem struct {
	VerHash    string `json:"verHash"`
	UpdateType int    `json:"updateType"`
	UpdateTime int64  `json:"updateTime"`
	Version    int    `json:"version"`
}

const (
	UpdateTypeContent      = 1
	UpdateTypeAbstract     = 2
	UpdateTypeTokenAddress = 3
)

type GetDIDArticleLogResponse struct {
	Items []GetDIDArticleLogItem `json:"items"`
}

func (s *suite) APIGetDIDArticleLog(ctx context.Context, request *GetDIDArticleLogRequest) *GetDIDArticleLogResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetDIDArticleLog")
	res := &GetDIDArticleLogResponse{
		Items: []GetDIDArticleLogItem{},
	}
	if len(request.Address) == 0 {
		return res
	}

	oneArticle, err := db.NewDIDArticle(ctx).FindByAddress(request.Address)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	if len(oneArticle.Address) == 0 {
		err = fmt.Errorf("not exist article, address:%v", request.Address)
		logger.Error(err)
		s.Request.Terminate(err)
	}

	if len(oneArticle.CurrentBlockRootHash) == 0 {
		return res
	}

	docs, err := db.NewDIDArticleBlock(ctx).FindByDidAddress(request.Address)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	blockMap := map[string]*db.DIDArticleBlockDocument{}
	for _, doc := range docs {
		blockMap[doc.RootHash] = doc
	}

	publishVersion := 0
	if v, ok := blockMap[oneArticle.CurrentBlockRootHash]; ok {
		publishVersion = v.Version
	}
	for _, doc := range docs {
		if doc.Status < db.StatusConfirmOk || doc.Version > publishVersion {
			continue
		}
		one := GetDIDArticleLogItem{
			VerHash:    doc.RootHash,
			Version:    doc.Version,
			UpdateTime: doc.CreateTime,
			UpdateType: calUpdateType(doc, blockMap[doc.PreRootHash]),
		}
		res.Items = append(res.Items, one)
	}
	return res
}

func calUpdateType(block *db.DIDArticleBlockDocument, preBlock *db.DIDArticleBlockDocument) (updateType int) {
	updateType = UpdateTypeContent
	if preBlock == nil || block == nil {
		return updateType
	}

	if block.CRMetaRootHash != preBlock.CRMetaRootHash {
		return
	}

	if block.ARMetaRootHash != preBlock.ARMetaRootHash {
		updateType = UpdateTypeAbstract
		return
	}

	if block.TokenAddress != preBlock.TokenAddress {
		updateType = UpdateTypeTokenAddress
		return
	}
	return
}

func GetTestArticleLog() *GetDIDArticleLogResponse {
	res := &GetDIDArticleLogResponse{
		Items: []GetDIDArticleLogItem{},
	}
	t := time.Now().UnixMilli() - 10*60*60*1000
	for i := 0; i < 50; i++ {
		upType := UpdateTypeContent
		if i%3 == 1 {
			upType = UpdateTypeAbstract
		}
		if i%3 == 0 {
			upType = UpdateTypeTokenAddress
		}
		one := GetDIDArticleLogItem{
			VerHash:    fmt.Sprintf("00000000000000%v", i),
			Version:    i,
			UpdateTime: t + int64(i*60*1000),
			UpdateType: upType,
		}
		res.Items = append(res.Items, one)
	}
	return res
}
