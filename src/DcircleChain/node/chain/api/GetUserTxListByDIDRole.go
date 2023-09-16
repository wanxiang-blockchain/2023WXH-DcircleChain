package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/utils"
	"fmt"
	"github.com/xpwu/go-log/log"
)

type GetUserTxListByDIDRoleRequest struct {
	Address     string `json:"address"`
	Role        string `json:"role"`
	StartTxTime int64  `json:"startTxTime"`
	Size        int64  `json:"size"`
}

type GetUserTxListByDIDRoleItem struct {
	Address               string `json:"address"`
	TxHash                string `json:"txHash"`
	BuyUserId             string `json:"buyUserId"`
	TxTime                int64  `json:"buyTime"`
	DIDAddress            string `json:"didAddress"`
	SourceChat            string `json:"sourceChat"`
	TransferChat          string `json:"transferChat"`
	TransferUserId        string `json:"transferUserId"`
	TokenNums             int64  `json:"tokenNums"`
	BuyUserNotInChat      bool   `json:"buyUserNotInChat"`
	TransferUserNotInChat bool   `json:"transferUserNotInChat"`
	DidNotInChat          bool   `json:"didNotInChat"`
	BuyDIDBlockRootHash   string `json:"buyDidBlockRootHash"`
	CreateTime            int64  `json:"createTime"`
	TransferTime          int64  `json:"transferTime"`
}

type GetUserTxListByDIDRoleResponse struct {
	Items []GetUserTxListByDIDRoleItem `json:"items"`
}

func (s *suite) APIGetUserTxListByDIDRole(ctx context.Context, request *GetUserTxListByDIDRoleRequest) *GetUserTxListByDIDRoleResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetUserTxListByDIDRole")
	logger.Debug(fmt.Sprintf("req: %v", request))
	res := &GetUserTxListByDIDRoleResponse{Items: []GetUserTxListByDIDRoleItem{}}
	if len(request.Role) == 0 || len(request.Address) == 0 || request.Size <= 0 {
		logger.Debug(fmt.Sprintf("Role:%v Address:%v, Size:%v", request.Role, request.Address, request.Size))
		return res
	}

	limit := request.Size
	const maxLimit = 100
	if limit > maxLimit {
		limit = maxLimit
	}

	docs, err := FindPurchaseRecord(ctx, request.Role, request.Address, request.StartTxTime, limit)
	//docs, err := db.NewDIDStatPurchase(ctx).FindByRole(request.Role, request.Address, request.StartTxTime, limit)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	chatMemberMap := map[string][]string{}
	chatDidMap := map[string][]string{}
	for _, doc := range docs {
		if _, ok := chatMemberMap[doc.TransferChatId]; !ok {
			chatMemberMap[doc.TransferChatId] = []string{}
		}

		if _, ok := chatDidMap[doc.TransferChatId]; !ok {
			chatDidMap[doc.TransferChatId] = []string{}
		}
		chatMemberMap[doc.TransferChatId] = append(chatMemberMap[doc.TransferChatId], doc.TransferUid, doc.BuyerUid)
		chatDidMap[doc.TransferChatId] = append(chatDidMap[doc.TransferChatId], doc.Address)
	}

	inChatMemberMap := FindInChatMemberMap(ctx, chatMemberMap)
	inChatDidMap := FindInChatDidMap(ctx, chatDidMap)
	for _, doc := range docs {
		one := GetUserTxListByDIDRoleItem{
			Address:               request.Address,
			TxHash:                doc.Id,
			BuyUserId:             doc.BuyerUid,
			TxTime:                int64(doc.BuyTime),
			DIDAddress:            doc.Address,
			SourceChat:            ShowChatId(doc.SourceChatId, doc.SourceChatType),
			TransferChat:          ShowChatId(doc.TransferChatId, doc.TransferChatType),
			TransferUserId:        doc.TransferUid,
			TokenNums:             int64(doc.TokenNums),
			BuyUserNotInChat:      true,
			TransferUserNotInChat: true,
			DidNotInChat:          true,
			BuyDIDBlockRootHash:   doc.RootHash,
			CreateTime:            doc.DIDCreateTime,
			TransferTime:          doc.TransferTime,
		}

		if value, ok := inChatMemberMap[doc.TransferChatId]; ok {
			if _, ok2 := value[doc.BuyerUid]; ok2 {
				one.BuyUserNotInChat = false
			}
		}

		if value, ok := inChatMemberMap[doc.TransferChatId]; ok {
			if _, ok2 := value[doc.TransferUid]; ok2 {
				one.TransferUserNotInChat = false
			}
		}

		if value, ok := inChatDidMap[doc.TransferChatId]; ok {
			if _, ok2 := value[doc.Address]; ok2 {
				one.DidNotInChat = false
			}
		}

		res.Items = append(res.Items, one)
	}
	return res
}

func FindInChatMemberMap(ctx context.Context, chatMemberMap map[string][]string) map[string]map[string]bool {
	res := map[string]map[string]bool{}
	chatMemberDb := db.NewChatMember(ctx)
	for chatId, uids := range chatMemberMap {
		uids = utils.SetStrList(uids)
		docs, _ := chatMemberDb.FindByUidsAndChatId(uids, chatId)
		if len(docs) == 0 {
			continue
		}
		res[chatId] = map[string]bool{}
		for _, doc := range docs {
			if !doc.IsAdded() {
				continue
			}
			res[chatId][doc.Uid] = true
		}
	}
	return res
}

func FindInChatDidMap(ctx context.Context, chatDidMap map[string][]string) map[string]map[string]bool {
	res := map[string]map[string]bool{}
	didArticleTagDb := db.NewDidArticleTag(ctx)
	for chatId, dids := range chatDidMap {
		dids = utils.SetStrList(dids)
		doc, _ := didArticleTagDb.FindByChatIdAndTagId(chatId, db.GetTagAll())
		if len(doc.Article) == 0 {
			continue
		}

		didMap := map[string]string{}
		for _, did := range dids {
			didMap[did] = ""
		}

		res[chatId] = map[string]bool{}
		for _, one := range doc.Article {
			if _, ok := didMap[one.Address]; ok {
				res[chatId][one.Address] = true
			}
		}
	}
	return res
}

func FindPurchaseRecord(ctx context.Context, role string, address string, start int64, limit int64) (docs []*db.DIDStatPurchaseDocument, err error) {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("FindPurchaseRecord")
	statPurchaseDb := db.NewDIDStatPurchase(ctx)
	docs, err = statPurchaseDb.FindByRole(role, address, start, limit)
	if err != nil {
		logger.Error(err)
		return
	}

	if len(docs) == 0 {
		return
	}

	eqTime := int64(0)
	switch role {
	case db.GroupCreatorStat, db.TransferStat, db.ArticleStat:
		eqTime = docs[len(docs)-1].TransferTime
	case db.CreatorStat:
		eqTime = docs[len(docs)-1].DIDCreateTime
	case db.ConsumerStat, db.SingleGroupStat:
		eqTime = int64(docs[len(docs)-1].BuyTime)
	default:
		err = fmt.Errorf("role is err, role:%v", role)
		return
	}
	eqDocs, err := statPurchaseDb.FindByRoleAndEqTime(role, address, eqTime)
	if err != nil {
		logger.Error(err)
		return
	}

	if len(eqDocs) <= 1 {
		return
	}

	lastId := docs[len(docs)-1].Id
	tempIndex := -1
	for i := 0; i < len(eqDocs); i++ {
		if lastId == eqDocs[i].Id {
			tempIndex = i
			break
		}
	}
	if tempIndex == -1 {
		return
	}
	//因为此方法不包含相等的Id 所以需要+1处理
	eqDocs = eqDocs[tempIndex+1:]
	if len(eqDocs) > 0 {
		docs = append(docs, eqDocs...)
	}
	return
}
