package api

import (
	"context"
	"dcircleserver/src/im/db"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetDIDBriefInfoRequest struct {
	AddressList []string `json:"addressList"` //最多100个
}

type DIDBriefInfoItem struct {
	Address    string `json:"address"`
	CrMetaText string `json:"crMetaText"`
}

type GetDIDBriefInfoResponse struct {
	Items []DIDBriefInfoItem `json:"items"`
}

func (s *suite) APIGetDIDBriefInfo(ctx context.Context, request *GetDIDBriefInfoRequest) *GetDIDBriefInfoResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetDIDBriefInfo")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))
	if len(request.AddressList) > 20 {
		err := fmt.Errorf("请求参数太多")
		logger.Error(err)
		s.Request.Terminate(err)
	}

	docs, err := db.NewDIDArticle(ctx).FindDIDArticleInfoByAddresses(request.AddressList...)
	if err != nil {
		logger.Error(fmt.Sprintf("FindDIDArticleInfoByAddresses err %v", err))
		s.Request.Terminate(err)
	}

	didBlockMap := map[string]string{}
	currentRootHashes := []string{}
	for _, doc := range docs {
		didBlockMap[doc.Address] = doc.CurrentBlockRootHash
		currentRootHashes = append(currentRootHashes, doc.CurrentBlockRootHash)
	}

	blocks, err := db.NewDIDArticleBlock(ctx).FindDIDArticleContentInfoByRootHashes(currentRootHashes...)
	if err != nil {
		logger.Error(fmt.Sprintf("FindDIDArticleContentInfoByRootHashes err %v", err))
		s.Request.Terminate(err)
	}

	blockCrHashMap := map[string]string{}
	crMetaHashes := []string{}
	for _, block := range blocks {
		blockCrHashMap[block.RootHash] = block.EncRootHash
		crMetaHashes = append(crMetaHashes, block.EncRootHash)
	}

	crMetaList, err := db.NewDIDBlockMeta(ctx).FindDIDBlockMetaInfoByRootHashes(crMetaHashes)
	if err != nil {
		logger.Error(fmt.Sprintf("FindDIDArticleContentInfoByRootHashes err %v", err))
		s.Request.Terminate(err)
	}

	crMetaMap := map[string]*db.DIDBlockMetaContent{}
	for _, oneMetaDoc := range crMetaList {
		var oneMeta *db.DIDBlockMetaContent
		oneMeta, err = db.ToDidBlockMetaInfo(oneMetaDoc.Content)
		if err != nil {
			logger.Error(fmt.Sprintf("ToDidBlockMetaInfo err %v", err))
			s.Request.Terminate(err)
		}

		crMetaMap[oneMetaDoc.RootHash] = oneMeta
	}
	res := &GetDIDBriefInfoResponse{
		Items: []DIDBriefInfoItem{},
	}

	for _, didAddress := range request.AddressList {
		one := DIDBriefInfoItem{Address: didAddress}
		currenceRootHash, ok := didBlockMap[didAddress]
		if !ok {
			res.Items = append(res.Items, one)
			continue
		}

		crMetaRootHash, ok := blockCrHashMap[currenceRootHash]
		if !ok {
			res.Items = append(res.Items, one)
			continue
		}

		meta, ok := crMetaMap[crMetaRootHash]
		if !ok {
			res.Items = append(res.Items, one)
			continue
		}
		one.CrMetaText = meta.Title.Text
		res.Items = append(res.Items, one)
	}

	return res
}
