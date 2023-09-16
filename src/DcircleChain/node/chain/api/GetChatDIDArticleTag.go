package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/utils"
	"fmt"
)

type GetChatDIDArticleTagRequest struct {
	ChatId string `json:"chatId"`
}

type GetChatDIDArticleTagResponse struct {
	Items []DidTag `json:"items"`
}

type DidTag struct {
	Id    string `json:"id"`
	Name  string `json:"name"`
	Count int    `json:"count"`
}

func (s *suite) APIGetChatDIDArticleTag(ctx context.Context, request *GetChatDIDArticleTagRequest) *GetChatDIDArticleTagResponse {
	ctx, logger := utils.NewContextWithReqId(ctx, s.Request.ReqID)
	logger.PushPrefix("GetChatDIDArticleTag")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))
	res := &GetChatDIDArticleTagResponse{Items: []DidTag{}}
	if len(request.ChatId) == 0 {
		logger.Debug("chatId is nil")
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

	doc, err := db.NewChatDidTag(ctx).FindByChatId(request.ChatId)
	if err != nil {
		logger.Error(fmt.Sprintf("FindByChatId err %v", err))
		s.Request.Terminate(err)
	}

	countMap, err := db.NewDidArticleTag(ctx).FindArticleCountByChatId(request.ChatId)
	if err != nil {
		logger.Error(fmt.Sprintf("FindArticleCountByChatId err %v", err))
		s.Request.Terminate(err)
	}
	//目前没有自动加allTag 所以在没有编辑的时候主动返回一个allTag， 后面编辑手动编辑后就有allTag
	if len(doc.ChatId) == 0 {
		one := DidTag{
			Id:    db.AllDidTag.Id,
			Name:  db.AllDidTag.Name,
			Count: 0,
		}
		if count, ok := countMap[one.Id]; ok {
			one.Count = count
		}
		res.Items = append(res.Items, one)
		return res
	}

	for _, tag := range doc.Tags {
		one := DidTag{
			Id:    tag.Id,
			Name:  tag.Name,
			Count: 0,
		}
		if count, ok := countMap[one.Id]; ok {
			one.Count = count
		}
		res.Items = append(res.Items, one)
	}
	return res
}
