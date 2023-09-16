package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/attachment"
	"dcircleserver/src/im/utils"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetChatDIDArticleByTagRequest struct {
	ChatId    string `json:"chatId"`
	TagId     string `json:"tagId"`
	PageIndex int    `json:"pageIndex"` //从0开始
	PageSize  int    `json:"pageSize"`
}

type ChatDIDArticleItem struct {
	DIDAddress     string           `json:"didAddress"`
	Title          string           `json:"title"`
	Abstract       string           `json:"abstract"`
	AbstractImage  attachment.Image `json:"abstractImage"`
	CreateTime     int64            `json:"createTime"`
	CreatorAddress string           `json:"creatorAddress"`
	CreatorName    string           `json:"creatorName"`
	UpdateTime     int64            `json:"updateTime"`
}

type GetChatDIDArticleByTagResponse struct {
	Items []ChatDIDArticleItem `json:"items"`
}

func (s *suite) APIGetChatDIDArticleByTag(ctx context.Context, request *GetChatDIDArticleByTagRequest) *GetChatDIDArticleByTagResponse {
	ctx, logger := utils.NewContextWithReqId(ctx, s.Request.ReqID)
	logger.PushPrefix("GetChatDIDArticleByTag")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))
	res := &GetChatDIDArticleByTagResponse{Items: []ChatDIDArticleItem{}}
	if len(request.ChatId) == 0 || len(request.TagId) == 0 || request.PageIndex < 0 || request.PageSize <= 0 {
		logger.Debug(fmt.Sprintf("参数错误 chatId:%v, tagId:%v, PageIndex:%v PageSize:%v",
			request.ChatId, request.TagId, request.PageIndex, request.PageSize))
		return res
	}

	oneChat, err := db.NewChat(ctx).FindByChatId(request.ChatId)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	if !oneChat.IsPublicGroup() && !oneChat.IsChannel() {
		logger.Debug("不是公开频道或公开群，chatId:", request.ChatId)
		return res
	}

	pageSize := request.PageSize
	if pageSize > 50 {
		pageSize = 50
	}

	doc, err := db.NewDidArticleTag(ctx).FindByChatIdAndTagId(request.ChatId, request.TagId)
	if err != nil {
		logger.Error(fmt.Sprintf("FindByChatIdAndTagId err %v", err))
		s.Request.Terminate(err)
	}

	if len(doc.ChatId) == 0 {
		return res
	}

	start := request.PageIndex * request.PageSize
	end := start + request.PageSize

	if len(doc.Article) <= start {
		return res
	}

	var temp []db.Article
	if end < len(doc.Article) {
		temp = doc.Article[start:end]
	} else {
		temp = doc.Article[start:]
	}

	var it ChatDIDArticleItem
	for _, one := range temp {
		it, err = GetOneDIDArticleItem(ctx, one.Address)
		if err != nil {
			logger.Error("GetOneDIDArticleItem address:", one.Address, err)
			s.Request.Terminate(err)
		}
		res.Items = append(res.Items, it)
	}
	return res
}

func GetOneDIDArticleItem(ctx context.Context, didAddress string) (res ChatDIDArticleItem, err error) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetOneDIDArticleItem")
	res = ChatDIDArticleItem{
		DIDAddress: didAddress,
	}
	if len(didAddress) == 0 {
		logger.Debug("didaddress is nil")
		return
	}
	oneArticle, err := db.NewDIDArticle(ctx).FindByAddress(didAddress)
	if err != nil {
		logger.Error(err)
		return
	}

	if len(oneArticle.Address) == 0 || len(oneArticle.CurrentBlockRootHash) == 0 {
		logger.Debug(fmt.Sprintf("didn't find this article:%v CurrentBlockRootHash:%v", didAddress, oneArticle.CurrentBlockRootHash))
		return
	}

	articleBlock, err := db.NewDIDArticleBlock(ctx).FindByRootHash(oneArticle.CurrentBlockRootHash)
	if err != nil {
		logger.Error(err)
		return
	}

	if len(articleBlock.ARMetaRootHash) == 0 || len(articleBlock.EncRootHash) == 0 {
		logger.Debug(fmt.Sprintf("CurrentBlockRootHash:%v ARMetaRootHash:%v EncRootHash:%v",
			oneArticle.CurrentBlockRootHash, articleBlock.ARMetaRootHash, articleBlock.EncRootHash))
		return
	}
	arMeta, err := db.GetDidBlockMetaInfo(ctx, articleBlock.ARMetaRootHash)
	if err != nil {
		logger.Error(err)
		return
	}

	crMeta, err := db.GetDidBlockMetaInfo(ctx, articleBlock.EncRootHash)
	if err != nil {
		logger.Error(err)
		return
	}

	oneUser, err := db.NewUserInfo(ctx).FindByUid(oneArticle.CreatorUid)
	if err != nil {
		logger.Error(err)
		return
	}

	res = ChatDIDArticleItem{
		DIDAddress:     oneArticle.Address,
		Title:          crMeta.Title.Text,
		Abstract:       arMeta.Title.Text,
		CreateTime:     int64(oneArticle.CreateTime),
		CreatorAddress: oneArticle.CreatorUid,
		CreatorName:    oneUser.Name,
		UpdateTime:     articleBlock.CreateTime,
	}

	if len(arMeta.Content) > 0 {
		res.AbstractImage = arMeta.Content[0].ToImage()
	}
	return
}
