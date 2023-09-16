package api

import (
	"context"
	"dcircleserver/src/im/db"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetGroupBriefInfoRequest struct {
	AddressList []string `json:"addressList"` //最多100个
}

type GroupBriefInfoItem struct {
	Address string `json:"address"`
	Name    string `json:"name"`
}

type GetGroupBriefInfoResponse struct {
	Items []GroupBriefInfoItem `json:"items"`
}

func (s *suite) APIGetGroupBriefInfo(ctx context.Context, request *GetGroupBriefInfoRequest) *GetGroupBriefInfoResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetGroupBriefInfo")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))
	if len(request.AddressList) > 20 {
		err := fmt.Errorf("请求参数太多")
		logger.Error(err)
		s.Request.Terminate(err)
	}

	docs, err := db.NewChat(ctx).FindByChatIds(request.AddressList...)
	if err != nil {
		logger.Error(fmt.Sprintf("FindByChatId err %v", err))
		s.Request.Terminate(err)
	}

	res := &GetGroupBriefInfoResponse{
		Items: []GroupBriefInfoItem{},
	}

	for _, doc := range docs {
		one := GroupBriefInfoItem{Address: doc.ChatId}
		if doc.IsPublicGroup() || doc.IsChannel() {
			one.Name = doc.Name
		}
		res.Items = append(res.Items, one)
	}
	return res
}
