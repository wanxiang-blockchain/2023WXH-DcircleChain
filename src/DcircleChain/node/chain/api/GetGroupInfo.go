package api

import (
	"context"
	"dcircleserver/src/im/db"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetGroupInfoRequest struct {
	Address string `json:"address"`
}

type GetGroupInfoResponse struct {
	Address               string        `json:"address"`
	MemberNums            int64         `json:"memberNums"`
	Status                int           `json:"status"` //-1-解散 1-正常
	CreateTime            int64         `json:"createTime"`
	CreatorAddress        string        `json:"creatorAddress"`
	ContentNums           int64         `json:"contentNums"`
	Name                  string        `json:"name"`
	Avatar                db.AvatarInfo `json:"avatar"`
	AvatarOnChainTxId     string        `json:"avatarOnChainTxId"`
	NameOnChainTxId       string        `json:"nameOnChainTxId"`
	EssenceKey            string        `json:"essenceKey"`
	MaxMemberNums         int           `json:"maxMemberNums"`
	JoinedTotalMemberNums int           `json:"joinedTotalMemberNums"`
}

func (s *suite) APIGetGroupInfo(ctx context.Context, request *GetGroupInfoRequest) *GetGroupInfoResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetGroupInfo2")
	logger.Debug("start")

	logger.Debug(fmt.Sprintf("req: %v", request))

	doc, err := db.NewChat(ctx).FindByChatId(request.Address)
	if err != nil {
		logger.Error(fmt.Sprintf("FindByChatId err %v", err))
		s.Request.Terminate(err)
	}

	if len(doc.ChatId) == 0 {
		return &GetGroupInfoResponse{}
	}

	res := &GetGroupInfoResponse{
		Address:               doc.ChatId,
		MemberNums:            int64(doc.NowMemberCount),
		Status:                doc.Status,
		CreateTime:            doc.CreateTime,
		CreatorAddress:        doc.CreatorUid,
		ContentNums:           0, //todo xhb
		Name:                  doc.Name,
		Avatar:                doc.Avatar640,
		AvatarOnChainTxId:     doc.AvatarOnChainTxId,
		NameOnChainTxId:       doc.NameOnChainTxId,
		MaxMemberNums:         doc.MaxMemberCount,
		JoinedTotalMemberNums: doc.JoinedTotalCount,
	}

	//if doc.IsNormal() {
	//	chatCountMap, err := db.NewChatMember(ctx).CountByChatId(request.Address)
	//	if err != nil {
	//		logger.Error(err)
	//	}
	//	if count, ok := chatCountMap[doc.ChatId]; ok {
	//		res.MemberNums = int64(count)
	//	}
	//}

	//获取精华消息的密钥 精华会话创建者密钥为精华密钥
	esChat, err := db.NewChat(ctx).FindEssenceChatId(request.Address)
	if err != nil {
		logger.Error(err)
	}

	if len(esChat.ChatId) > 0 {
		member, err := db.NewChatMember(ctx).FindByUidAndChatId(esChat.CreatorUid, esChat.ChatId)
		if err != nil {
			logger.Error(err)
		}
		res.EssenceKey = member.SecretKey
	}
	return res
}
