package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/message"
	"fmt"
	"github.com/xpwu/go-log/log"
	"math"
)

type PullEssenceMessageRequest struct {
	ChatId string `json:"chatId"`
	End    int64  `json:"end"` //传-1时 为从头再拉
	Limit  int64  `json:"limit"`
}

type PullEssenceMessageItem struct {
	MsgId      string              `json:"msgId"`
	ChatId     string              `json:"chatId"`
	Seq        int64               `json:"seq"`
	Content    string              `json:"content"`
	Encrypted  db.MessageEncrypted `json:"encrypted"`
	CreatorUid string              `json:"creatorUid"`
	CreateTime int64               `json:"createTime"`
	MsgType    message.Type        `json:"msgType"`
}

type PullEssenceMessageResponse struct {
	Items []PullEssenceMessageItem `json:"items"`
}

func (s *suite) APIPullEssenceMessage(ctx context.Context, request *PullEssenceMessageRequest) *PullEssenceMessageResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("PullEssenceMessage")
	logger.Debug(fmt.Sprintf("req: %v", request))
	res := &PullEssenceMessageResponse{Items: []PullEssenceMessageItem{}}
	if len(request.ChatId) == 0 {
		logger.Debug("ChatId is nil")
		return res
	}

	limit := request.Limit
	const maxLimit = 50
	if limit > maxLimit {
		limit = maxLimit
	}

	if limit <= 0 {
		limit = maxLimit
	}

	const maxSeq = math.MaxInt64
	end := request.End
	if end < 0 {
		end = maxSeq
	}

	esChat, err := db.NewChat(ctx).FindEssenceChatId(request.ChatId)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	if len(esChat.ChatId) == 0 {
		logger.Debug("没有找到精华会话，chatId:", request.ChatId)
		return res
	}

	messageList, err := PullDownEssenceMessage(ctx, esChat.ChatId, end, limit)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	for _, doc := range messageList {
		res.Items = append(res.Items, PullEssenceMessageItem{
			MsgId:      doc.MsgId,
			ChatId:     doc.ChatId,
			Seq:        doc.Seq,
			Content:    doc.Content,
			Encrypted:  doc.Encrypted,
			CreatorUid: doc.CreatorUid,
			CreateTime: doc.CreateTime,
			MsgType:    doc.MsgType,
		})
	}

	return res
}

func PullDownEssenceMessage(ctx context.Context, chatId string, end int64, limit int64) (msgList []*db.MessageDocument, err error) {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("PullDownEssenceMessage")
	msgList = []*db.MessageDocument{}
	if limit <= 0 {
		panic(fmt.Sprintf("PullDownEssenceMessage limit:%v is err", limit))
	}

	if end <= 1 {
		logger.Debug("end ==1 直接返回")
		return
	}

	noAuthMap, err := db.NewAuth(ctx).GetNoAuthedBetweenSeq(chatId, []string{db.AllUid}, 0, end)
	if err != nil {
		logger.Error(err)
		return
	}

	var docs []*db.MessageDocument
	for {
		if end <= 1 {
			break
		}
		docs, err = db.NewMessage(ctx).ReadToEndSeq(chatId, end, limit)
		if err != nil {
			logger.Error(err)
			return
		}

		for _, doc := range docs {
			if _, ok := noAuthMap[doc.Seq]; ok {
				continue
			}
			msgList = append(msgList, doc)
		}

		if len(msgList) >= int(limit) {
			break
		}

		if len(docs) < int(limit) {
			break
		}
		end = docs[len(docs)-1].Seq
	}

	return
}
