package db

import (
	"context"
	"encoding/json"
	"github.com/xpwu/go-cmd-dbindex/indexcmd"
	"github.com/xpwu/go-db-mongo/mongodb/index"
	"github.com/xpwu/go-db-mongo/mongodb/mongocache"
	"github.com/xpwu/go-db-mongo/mongodb/updater"
	"github.com/xpwu/go-log/log"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

func init() {
	indexcmd.Add(func() indexcmd.Creator {
		return NewSignLog(context.TODO())
	}, "create SignLog Index")
}

type SignLogDocument struct {
	SignHash   HexString `bson:"_id"`
	ReqId      string
	Status     int
	FailReason string
	CreateTime UnixTimeMs
	Data       string
}

const (
	SignLogSuccess = 1
	SignLogFail    = -1
)

type SignLog struct {
	ctx    context.Context
	logger *log.Logger
}

func (col *SignLog) collection() *mongo.Collection {
	const colName = "signlog"
	return mongocache.MustGet(col.ctx, MongoConfig.Config).Database(MongoConfig.DBName).Collection(colName)
}

func (col *SignLog) field() *SignLogDocument0Field {
	return NewSignLogDocument0Field("")
}

func (col *SignLog) Insert(info *SignLogDocument) (err error) {
	_, err = col.collection().InsertOne(col.ctx, info)
	if mongo.IsDuplicateKeyError(err) {
		err = nil
	}
	return
}

func (col *SignLog) SetDataAndStatusById(id string, status int, data string, reason string) (err error) {
	if len(id) == 0 {
		col.logger.Error("reqId is nil")
		return
	}
	f := col.field().SignHash().Eq(id)
	_, err = col.collection().UpdateOne(col.ctx,
		f.ToBsonD(),
		updater.Batch(
			col.field().Status().Set(status),
			col.field().Data().Set(data),
			col.field().FailReason().Set(reason),
			col.field().CreateTime().Set(UnixTimeMs(time.Now().UnixMilli())),
		))

	return
}
func (col *SignLog) CreateIndex() {
	indexes := []mongo.IndexModel{
		{
			Keys: index.Keys(col.field().ReqId().DescIndex()).ToBsonD(),
		},
	}
	_, err := col.collection().Indexes().CreateMany(col.ctx, indexes)
	if err != nil {
		_, logger := log.WithCtx(col.ctx)
		logger.PushPrefix("create index")
		logger.Error(err)
	}
}

func NewSignLog(ctx context.Context) *SignLog {
	_, logger := log.WithCtx(ctx)
	return &SignLog{ctx, logger}
}

type SignAction = string

const (
	ActionPCLogin                 = "Login"                   // 登录到PC端
	ActionJoinGroup               = "JoinGroup"               // 加入公开群、频道
	ActionCreateGroup             = "CreateGroup"             // 创建公开群、频道
	ActionDismissGroup            = "DismissGroup"            // 解散公开群、频道
	ActionKickOutGroup            = "KickOutGroup"            // 删除群成员员
	ActionQuitFromGroup           = "QuitFromGroup"           // 退出公开群、频道
	ActionSetDIDConfirmed         = "SetDIDConfirmed"         // 内容确权
	ActionSetDIDToken             = "SetDIDToken"             // 设置内容Token
	ActionSetDIDAbstract          = "SetDIDAbstract"          // 设置摘要
	ActionBuyDIDArticle           = "BuyDIDArticle"           // 购买内容
	ActionShareDIDArticle         = "ShareDIDArticle"         // 转发到会话
	ActionDeleteDIDArticleInGroup = "DeleteDIDArticleInGroup" // 群主删除内容
	ActionSetGroupAdmin           = "SetGroupAdmin"           // 设置管理员
	ActionDeleteGroupAdmin        = "DeleteGroupAdmin"        // 删除管理员
	ActionAuthorizedAccessDCircle = "AuthorizedAccessDCircle" // 授权登录
	ActionRevokeAccessDCircle     = "RevokeAccessDCircle"     // 停止授权登录
	ActionReferenceArticle        = "ReferenceArticle"        // 引用内容
	ActionDIDInviteDownLoad       = "InviteDownLoad"          // DID内容站外邀请
	ActionGroupInviteDownLoad     = "GroupInviteDownLoad"     // 群站外邀请
	ActionGroupInfoAvatarOnChain  = "GroupInfoAvatarOnChain"  // 群头像信息上链
	ActionGroupInfoNameOnChain    = "GroupInfoNameOnChain"    // 群名称信息上链
	ActionAddMessageEssence       = "AddMessageEssence"       // 添加精选消息
	ActionDelMessageEssence       = "DelMessageEssence "      // 删除精华消息
	ActionHandleApplyForJoinChat  = "HandleApplyForJoinChat " // 审核入群申请
)

type SignLogInfo struct {
	Action  SignAction `json:"action"`
	Address string     `json:"address"`
}

func (s *SignLogInfo) ToString() string {
	b, err := json.Marshal(s)
	if err != nil {
		return ""
	}
	return string(b)
}

type OperateGroupSignLog struct {
	SignLogInfo
	GroupId         string   `json:"groupId"`
	AffectedAddress []string `json:"affectedAddress"`
	HandleResult    int      `json:"handleResult,omitempty"`
}

func (s *OperateGroupSignLog) ToString() string {
	b, err := json.Marshal(s)
	if err != nil {
		return ""
	}
	return string(b)
}

type OperateDIDSignLog struct {
	SignLogInfo
	DIDAddress string `json:"didAddress"`
	ChatId     string `json:"chatId"`
	Context    string `json:"context"`
}

func (s *OperateDIDSignLog) ToString() string {
	b, err := json.Marshal(s)
	if err != nil {
		return ""
	}
	return string(b)
}
