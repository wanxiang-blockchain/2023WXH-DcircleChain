package db

import (
	"encoding/hex"
	"github.com/ethereum/go-ethereum/rlp"
)

const (
	OpCodePCLogin                 OpCode = 0x01 // 登录到PC端
	OpCodeJoinGroup               OpCode = 0x02 // 加入公开群、频道
	OpCodeCreateGroup             OpCode = 0x03 // 创建公开群、频道
	OpCodeDismissGroup            OpCode = 0x04 // 解散公开群、频道
	OpCodeDeleteGroupMember       OpCode = 0x05 // 删除群成员员
	OpCodeExitFromGroup           OpCode = 0x06 // 退出公开群、频道
	OpCodeSetDIDArticleConfirmed  OpCode = 0x07 // 内容确权
	OpCodeSetDIDArticleToken      OpCode = 0x08 // 设置内容Token
	OpCodeBuyDIDArticle           OpCode = 0x09 // 购买内容
	OpCodeShareDIDArticleToChat   OpCode = 0x0A // 转发到会话  todo 签名更新
	OpCodeSetDIDArticleAbstract   OpCode = 0x0B // 设置摘要
	OpCodeDeleteDIDArticleInGroup OpCode = 0x0C // 群主删除内容   todo 签名更新
	OpCodeAddGroupAdminer         OpCode = 0x0D // 设置管理员
	OpCodeDeleteGroupAdminer      OpCode = 0x0E // 删除管理员
	OpCodeAuthorizedAccessDCircle OpCode = 0x0F // 授权登录
	OpCodeRevokeAccessDCircle     OpCode = 0x10 // 停止授权登录
	OpCodeReferenceArticle        OpCode = 0x11 // 引用内容
	OpCodeDIDInviteDownLoad       OpCode = 0x12 // 内容站外邀请
	OpCodeGroupInfoAvatarOnChain  OpCode = 0x13 // 群头像信息上链
	OpCodeGroupInviteDownLoad     OpCode = 0x14 // 群站外邀请
	OpCodeAddMessageEssence       OpCode = 0x15 // 添加精选消息
	OpCodeDelMessageEssence       OpCode = 0x16 // 删除精华消息
	OpCodeGroupInfoNameOnChain    OpCode = 0x17 // 群头名称信息上链
	OpCodeHandApplyForJoinChat    OpCode = 0x18 // 审核入群申请
)

var OPCODES = map[OpCode]SignAction{
	OpCodePCLogin:                 ActionPCLogin,
	OpCodeJoinGroup:               ActionJoinGroup,
	OpCodeCreateGroup:             ActionCreateGroup,
	OpCodeDismissGroup:            ActionDismissGroup,
	OpCodeDeleteGroupMember:       ActionKickOutGroup,
	OpCodeExitFromGroup:           ActionQuitFromGroup,
	OpCodeSetDIDArticleConfirmed:  ActionSetDIDConfirmed,
	OpCodeSetDIDArticleToken:      ActionSetDIDToken,
	OpCodeBuyDIDArticle:           ActionBuyDIDArticle,
	OpCodeShareDIDArticleToChat:   ActionShareDIDArticle,
	OpCodeSetDIDArticleAbstract:   ActionSetDIDAbstract,
	OpCodeAddGroupAdminer:         ActionSetGroupAdmin,
	OpCodeDeleteGroupAdminer:      ActionDeleteGroupAdmin,
	OpCodeAuthorizedAccessDCircle: ActionAuthorizedAccessDCircle,
	OpCodeRevokeAccessDCircle:     ActionRevokeAccessDCircle,
	OpCodeReferenceArticle:        ActionReferenceArticle,
	OpCodeDIDInviteDownLoad:       ActionDIDInviteDownLoad,
	OpCodeGroupInfoAvatarOnChain:  ActionGroupInfoAvatarOnChain,
	OpCodeGroupInfoNameOnChain:    ActionGroupInfoNameOnChain,
	OpCodeGroupInviteDownLoad:     ActionGroupInviteDownLoad,
	OpCodeAddMessageEssence:       ActionAddMessageEssence,
	OpCodeDelMessageEssence:       ActionDelMessageEssence,
	OpCodeHandApplyForJoinChat:    ActionHandleApplyForJoinChat,
}

const (
	TransactionStatusPending = "0x2"
	TransactionStatusSuccess = "0x1"
	TransactionStatusFailure = "0x0"
)

// HexString 0xa3a1043d3f6e9d8e76165d0051a00b217d74fde126865cb33fd6bc00c6265a8a
type HexString = string

// EthAddress 支持eip55  0x7c52e508c07558c287d5a453475954f6a547ec41 = 0x7c52e508C07558C287d5A453475954f6a547eC41
type EthAddress = string
type OpCode = uint8
type RLPData = []byte

// UnixTimeMs 13位毫秒级时间戳 1679539403992
type UnixTimeMs = uint64

// ChainId 1 beta & release 2 alpha 3 dev
type ChainId = uint8

// DCChainValue DCChain 小数点位数最大18位
type DCChainValue = int64

// TransactionList 交易哈希
type TransactionList = []HexString

// TransactionMerkleTree 交易莫克尔树
type TransactionMerkleTree struct {
}

func (t *TransactionMerkleTree) name() {

}

type BlockMerkleTree struct {
}

type HexPublicKey = string

func HexStringFromBytes(data []byte) HexString {
	return HexString(hex.EncodeToString(data))
}

func RLPDataFromBytes(data []interface{}) RLPData {
	encodedData, err := rlp.EncodeToBytes(data)
	if err != nil {
		panic(err)
	}
	return encodedData
}

func InOpCode(opcode OpCode) bool {
	if _, ok := OPCODES[opcode]; ok {
		return ok
	}
	return false
}

func GetOpCodeAction(opcode OpCode) SignAction {
	if s, ok := OPCODES[opcode]; ok {
		return s
	}
	return ""
}

type SignTxInfo struct {
	TxId   string
	OpCode OpCode
}
