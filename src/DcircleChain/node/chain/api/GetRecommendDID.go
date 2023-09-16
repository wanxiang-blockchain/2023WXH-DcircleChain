package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/attachment"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetRecommendDIDRequest struct {
}

type GetRecommendDIDResponse struct {
	Items []*RecommendDIDInfo `json:"items"`
}

type RecommendDIDInfo struct {
	DIDAddress    string             `json:"didAddress"`
	BuyCount      int                `json:"buyCount"`
	PublishTime   int64              `json:"publishTime"`
	ContentTitle  string             `json:"contentTitle"`
	AbstractImage []attachment.Image `json:"abstractImage"`
	AbstractText  string             `json:"abstractText"`
	Publisher     PublisherInfo      `json:"publisher"`
}

type PublisherInfo struct {
	Address string        `json:"address"`
	Avatar  db.AvatarInfo `json:"avatar"`
	Name    string        `json:"name"`
}

func (s *suite) APIGetRecommendDID(ctx context.Context, request *GetRecommendDIDRequest) *GetRecommendDIDResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetRecommendDID")
	logger.Debug("start")

	dids, err := db.NewRecommendDID(ctx).FindAllDID()
	if err != nil {
		logger.Error(fmt.Sprintf("FindAllDID err %v", err))
		s.Request.Terminate(err)
	}

	res := &GetRecommendDIDResponse{
		Items: []*RecommendDIDInfo{},
	}
	if len(dids) == 0 {
		return res
	}

	recommendList, err := GetRecommendDIDInfo(ctx, dids)
	if err != nil {
		logger.Error(fmt.Sprintf("GetRecommendDIDInfo err %v", err))
		s.Request.Terminate(err)
	}
	res.Items = recommendList
	return res

}

func GetRecommendDIDInfo(ctx context.Context, didAddress []string) (res []*RecommendDIDInfo, err error) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetRecommendDIDInfo")
	res = []*RecommendDIDInfo{}
	someDIDArticle, err := db.NewDIDArticle(ctx).FindDIDArticleInfoByAddresses(didAddress...)
	if err != nil {
		logger.Error(err)
		return
	}

	if len(someDIDArticle) == 0 {
		logger.Debug("没有找到推荐的did")
		return
	}

	recommendDIDMap := map[string]*RecommendDIDInfo{}
	var oneRecommend *RecommendDIDInfo
	publishUids := []string{}
	resultDidAddress := []string{}
	for _, oneArticle := range someDIDArticle {
		if !oneArticle.IsPublish() {
			continue
		}
		publishUids = append(publishUids, oneArticle.CreatorUid)
		resultDidAddress = append(resultDidAddress, oneArticle.Address)

		oneRecommend, err = GetOneRecommendInfo(ctx, oneArticle)
		if err != nil {
			logger.Error(err)
		}
		recommendDIDMap[oneArticle.Address] = oneRecommend
	}

	//获取发布者信息
	publisherMap, err := GetPubilsherInfo(ctx, publishUids)
	if err != nil {
		logger.Error(err)
	}
	//统计购买次数
	didBuyMap, err := GetDIDBuyInfo(ctx, resultDidAddress)
	if err != nil {
		logger.Error(err)
	}

	for _, didAddr := range resultDidAddress {
		recommendDid, ok := recommendDIDMap[didAddr]
		if !ok {
			continue
		}
		if user, ok := publisherMap[recommendDid.Publisher.Address]; ok {
			recommendDid.Publisher = user
		}
		if buyCount, ok := didBuyMap[didAddr]; ok {
			recommendDid.BuyCount = buyCount
		}
		res = append(res, recommendDid)
	}
	return res, nil
}

func GetOneRecommendInfo(ctx context.Context, oneArticle *db.DIDArticleDocument) (oneRecommend *RecommendDIDInfo, err error) {
	oneRecommend = &RecommendDIDInfo{
		DIDAddress:    oneArticle.Address,
		BuyCount:      0,
		PublishTime:   int64(oneArticle.CreateTime),
		ContentTitle:  "",
		AbstractImage: []attachment.Image{},
		AbstractText:  "",
		Publisher: PublisherInfo{
			Address: oneArticle.CreatorUid,
		},
	}

	oneBlock, err := db.NewDIDArticleBlock(ctx).FindByRootHash(oneArticle.CurrentBlockRootHash)
	if err != nil {
		return
	}

	if len(oneBlock.RootHash) == 0 {
		return
	}

	crMeta, err := db.GetDidBlockMetaInfo(ctx, oneBlock.EncRootHash)
	if err != nil {
		return
	}
	arMeta, err := db.GetDidBlockMetaInfo(ctx, oneBlock.ARMetaRootHash)
	if err != nil {
		return
	}

	if len(crMeta.Title.Text) > 0 {
		oneRecommend.ContentTitle = crMeta.Title.Text
	}

	for _, node := range arMeta.Content {
		oneRecommend.AbstractImage = append(oneRecommend.AbstractImage, node.ToImage())
	}

	if len(arMeta.Title.Text) > 0 {
		oneRecommend.AbstractText = arMeta.Title.Text
	}
	return
}

func GetPubilsherInfo(ctx context.Context, uids []string) (publisherMap map[string]PublisherInfo, err error) {
	publisherMap = map[string]PublisherInfo{}
	if len(uids) == 0 {
		return
	}
	docs, err := db.NewUserInfo(ctx).FindByUidS(uids)
	if err != nil {
		return
	}

	for _, doc := range docs {
		publisherMap[doc.Uid] = PublisherInfo{
			Address: doc.Uid,
			Avatar:  doc.Avatar128,
			Name:    doc.Name,
		}
	}
	return
}

func GetDIDBuyInfo(ctx context.Context, didAddress []string) (didBuyMap map[string]int, err error) {
	didBuyMap = map[string]int{}
	if len(didAddress) == 0 {
		return
	}
	c, err := db.NewDIDStatPurchase(ctx).StatBuyCountByDIDAddress(didAddress)
	if err != nil {
		return
	}

	var s []struct {
		DIDAddress string `bson:"_id"`
		BuyCount   int    `bson:"buyCount"`
	}

	err = c.All(ctx, &s)
	if err != nil {
		return
	}
	for _, doc := range s {
		didBuyMap[doc.DIDAddress] = doc.BuyCount
	}
	return
}
