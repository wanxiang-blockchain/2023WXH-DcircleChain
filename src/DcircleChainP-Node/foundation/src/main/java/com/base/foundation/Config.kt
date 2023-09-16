package com.base.foundation

const val BaseUrl = "ws://47.109.58.120:10024"
var NetConfigBaseUrl = "https://appfiles-alpha.oss-cn-hongkong.aliyuncs.com"

var DCircleScanUrl = "http://alpha.dcirclescan.io"
var TPWalletSignMessage = "SecretChatSign"

// ChainId 1 beta & release 2 alpha 3 dev
enum class ChainId(var id: Byte) {
	DEV(3),
	ALPHA(3),
	BETA(1),
	RELEASE(1),
}

var DCircleChainID = ChainId.ALPHA

enum class DCircleEnv(var value:String) {
	pro("pro"),
	dev("dev"),
	alpha("alpha"),
	beta("beta")
}

val Env:DCircleEnv = DCircleEnv.alpha

enum class DCircleAppChannel(var value: String) {
	google("google"),
	apk("apk")
}
const val AppChannel = "apk"

const val BranchName = "main"

const val CommitId = "bb687927b"

const val VersionCode = 6205
