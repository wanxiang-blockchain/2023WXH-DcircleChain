package api

import (
	"context"
	db2 "dcircleserver/src/chain/db"
	"dcircleserver/src/im/db"
	"github.com/xpwu/go-log/log"
	"time"
)

type GetPlatformStatRequest struct {
}

type GetPlatformStatResponse struct {
	RegisterCount    int `json:"registerCount"`    //总地址数
	PubilcGroupCount int `json:"pubilcGroupCount"` //总社群数
	ConfirmDIDCount  int `json:"confirmDidCount"`  //确权内容总数
	SignCount        int `json:"signCount"`        //总交互数
	BuyDIDCount      int `json:"buyDidCount"`      //总交易数
}

func (s *suite) APIGetPlatformStat(ctx context.Context, request *GetPlatformStatRequest) *GetPlatformStatResponse {
	ctx, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetPlatformStat")
	logger.Debug("start")

	t1 := time.Now().UnixMilli()
	res := &GetPlatformStatResponse{}

	c, err := db.NewStatUserRegister(ctx).StatDaliyRegisterCountByTime(0, db.DefaultMaxMillSecond)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	var register []struct {
		RegisterCount int `bson:"registerCount"`
	}
	err = c.All(ctx, &register)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	for _, da := range register {
		res.RegisterCount = da.RegisterCount
	}

	t2 := time.Now().UnixMilli()
	logger.Debug("StatDaliyRegisterCountByTime cost ", t2-t1, " millSecond")

	c, err = db.NewDIDStatChatSource(ctx).StatDailyCreateChatByTime(0, db.DefaultMaxMillSecond, []int{db.ChatTypePublicGroup, db.ChatTypePublicChannel})
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	var chatDoc []struct {
		ChatType        int `bson:"_id"`
		CreateChatCount int `bson:"createChatCount"`
	}
	err = c.All(ctx, &chatDoc)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	for _, one := range chatDoc {
		res.PubilcGroupCount += one.CreateChatCount
	}

	t3 := time.Now().UnixMilli()
	logger.Debug("StatDailyCreateChatByTime cost ", t3-t2, " millSecond")

	confirmCount, err := db.NewDIDStatArticleSource(ctx).StatConfirmedCount()
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	res.ConfirmDIDCount = confirmCount
	t4 := time.Now().UnixMilli()
	logger.Debug("StatConfirmedCount cost ", t4-t3, " millSecond")

	signCount, err := db2.NewSign(ctx).StatSignCount()
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	res.SignCount = signCount
	t5 := time.Now().UnixMilli()
	logger.Debug("StatSignCount cost ", t5-t4, " millSecond")

	purchaseCount, err := db.NewDIDStatPurchase(ctx).StatPurchaseCount()
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}
	res.BuyDIDCount = purchaseCount
	t6 := time.Now().UnixMilli()
	logger.Debug("StatPurchaseCount cost ", t6-t5, " millSecond")

	logger.Debug("GetPlatformStat total cost ", t6-t1, " millSecond")
	return res
}
