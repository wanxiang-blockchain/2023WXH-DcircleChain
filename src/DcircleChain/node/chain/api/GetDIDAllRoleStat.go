package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/redis"
	"dcircleserver/src/im/utils"
)

type GetDIDAllRoleStatRequest struct {
	UserAddress string `json:"userAddress"`
}

type GetDIDAllRoleStatResponseItem struct {
	Address        string `json:"id"`
	DataUpdateTime int64  `json:"dataUpdateTime"`
	RoleStatType   string `json:"roleStatType"`
	db.StatContent
	Ymd string `json:"ymd"` // 日期 yyyymmdd
}

type GetDIDAllRoleStatResponse struct {
	Items []GetDIDAllRoleStatResponseItem `json:"items"`
}

func (s *suite) APIGetDIDAllRoleStat(ctx context.Context, request *GetDIDAllRoleStatRequest) *GetDIDAllRoleStatResponse {
	ctx, logger := utils.NewContextWithReqId(ctx, s.Request.ReqID)
	logger.PushPrefix("APIGetDIDAllRoleStat")
	logger.Debug("start")

	resp := &GetDIDAllRoleStatResponse{
		Items: []GetDIDAllRoleStatResponseItem{},
	}

	if len(request.UserAddress) == 0 {
		logger.Debug("UserAddress is nil")
		return resp
	}

	userAddress := request.UserAddress
	yesterday, _, _ := utils.GetYesterdayDateWithSplit()
	lastStatTime, err := redis.NewKeyVal(ctx).GetValue(redis.LAST_STAT_TIME_KEY)
	if err == nil {
		yesterday = utils.UnixTimeMicroToDate(lastStatTime, "20060102")
	}

	// 群主
	doc, err := db.NewDIDStatRoleDaily(ctx).FindByObjectIdAndRole(userAddress, db.GroupCreatorStat, yesterday)
	if err != nil {
		logger.Error(err)
	} else {
		resp.Items = append(resp.Items, GetDIDAllRoleStatResponseItem{
			Address:        doc.RoleObjectId,
			DataUpdateTime: doc.UpdateTime,
			RoleStatType:   db.GroupCreatorStat,
			Ymd:            doc.Ymd,
			StatContent:    doc.StatContent,
		})
	}

	// 创作者
	doc, err = db.NewDIDStatRoleDaily(ctx).FindByObjectIdAndRole(userAddress, db.CreatorStat, yesterday)
	if err != nil {
		logger.Error(err)
	} else {
		resp.Items = append(resp.Items, GetDIDAllRoleStatResponseItem{
			Address:        doc.RoleObjectId,
			DataUpdateTime: doc.UpdateTime,
			RoleStatType:   db.CreatorStat,
			Ymd:            doc.Ymd,
			StatContent:    doc.StatContent,
		})
	}

	// 消费者
	doc, err = db.NewDIDStatRoleDaily(ctx).FindByObjectIdAndRole(userAddress, db.ConsumerStat, yesterday)
	if err != nil {
		logger.Error(err)
	} else {
		resp.Items = append(resp.Items, GetDIDAllRoleStatResponseItem{
			Address:        doc.RoleObjectId,
			DataUpdateTime: doc.UpdateTime,
			RoleStatType:   db.ConsumerStat,
			Ymd:            doc.Ymd,
			StatContent:    doc.StatContent,
		})
	}

	// 传播者
	doc, err = db.NewDIDStatRoleDaily(ctx).FindByObjectIdAndRole(userAddress, db.TransferStat, yesterday)
	if err != nil {
		logger.Error(err)
	} else {
		resp.Items = append(resp.Items, GetDIDAllRoleStatResponseItem{
			Address:        doc.RoleObjectId,
			DataUpdateTime: doc.UpdateTime,
			RoleStatType:   db.TransferStat,
			Ymd:            doc.Ymd,
			StatContent:    doc.StatContent,
		})
	}
	return resp
}
