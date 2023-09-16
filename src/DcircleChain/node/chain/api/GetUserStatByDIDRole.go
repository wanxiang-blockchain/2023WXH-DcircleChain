package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/redis"
	"dcircleserver/src/im/utils"
	"github.com/xpwu/go-log/log"
)

type GetUserStatByDIDRoleRequest struct {
	Address string `json:"address"`
	Role    string `json:"role"`
}

const (
	Creator  = "Creator"
	Transfer = "Transfer"
	Group    = "Group"
	Consumer = "Consumer"
)

type GetUserStatByDIDRoleResponse struct {
	Address        string `json:"address"`
	DataUpdateTime int64  `json:"dataUpdateTime"`
	db.StatContent
}

func (s *suite) APIGetUserStatByDIDRole(ctx context.Context, request *GetUserStatByDIDRoleRequest) *GetUserStatByDIDRoleResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetUserStatByDIDRole")
	res := &GetUserStatByDIDRoleResponse{
		Address: request.Address,
	}
	if len(request.Role) == 0 || len(request.Address) == 0 {
		logger.Debug("address is empty")
		return res
	}

	yesterday, _, _ := utils.GetYesterdayDateWithSplit()
	lastStatTime, err := redis.NewKeyVal(ctx).GetValue(redis.LAST_STAT_TIME_KEY)
	if err == nil {
		yesterday = utils.UnixTimeMicroToDate(lastStatTime, "20060102")
	}

	logger.Debug("yesterday: ", yesterday)
	// 群信息
	if request.Role == db.SingleGroupStat {
		doc, err := db.NewDIDStatChatDaily(ctx).GetStatWithDate(request.Address, yesterday)
		if err != nil {
			logger.Error(err)
			s.Request.Terminate(err)
		}

		res.DataUpdateTime = doc.UpdateTime
		res.StatContent = doc.StatContent
		return res
	}

	// 文章数据
	if request.Role == db.ArticleStat {
		doc, err := db.NewDIDStatArticleDaily(ctx).GetStatWithDate(request.Address, yesterday)
		if err != nil {
			logger.Error(err)
			s.Request.Terminate(err)
		}

		res.DataUpdateTime = doc.UpdateTime
		res.StatContent = doc.StatContent
		return res
	}

	// 角色数据
	doc, err := db.NewDIDStatRoleDaily(ctx).FindByObjectIdAndRole(request.Address, request.Role, yesterday)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	res.DataUpdateTime = doc.UpdateTime
	res.StatContent = doc.StatContent
	return res
}
