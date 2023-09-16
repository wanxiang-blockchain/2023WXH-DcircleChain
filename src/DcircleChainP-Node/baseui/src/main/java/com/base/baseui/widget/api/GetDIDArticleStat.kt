package com.base.baseui.widget.api

import com.base.foundation.api.postJsonNoTokenSus
import com.base.foundation.api.Request
import com.base.foundation.api.Response
import com.base.foundation.db.DIDArticleStat
import com.base.foundation.db.insert
import com.base.foundation.db.update
import com.base.foundation.getUs
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

class GetDIDAllRoleStatRequest {
	var userAddress: String = ""
}

class GetDIDAllRoleStatResponse {
	class Item {
		var id = ""
		var dataUpdateTime = 0L
		var roleStatType   = ""
		var ymd = ""   // 年月日
		var nNums = 0  // 内容总数 规则看后端定义
		var mNums = 0  // 流通量：=流水。FREE=0
		var cNums = 0  // 消费人数
		var cTimes = 0 // 消费次数
		var tNums = 0  // 传播人数
		var tTimes = 0 // 传播次数
		var gNums = 0  // 传播群数
		var mxNums = 0 // 潜力流通量
		var cxNums = 0 // 潜力消费人数
		var sgNums = 0 // 来源群数
		var exposureGroupNums = 0  // 曝光群数
		var exposurePeopleNums = 0 // 曝光人数
		var revenueNums = 0 // 个人收入
		var joinTimes = 0 	//进群次数
		var joinUserCount = 0 //进群人数
		var shareGroupCount = 0 //分享群数

	}

	var items: Array<Item> = arrayOf()
}

fun Array<GetDIDAllRoleStatResponse.Item>.convert(roleStatType: String):Array<DIDArticleStat> {
	fun buildStatId(id: String,role : String,ymd:String): String {
		return id+"_"+role+"_"+ymd
	}

	val result = mutableListOf<DIDArticleStat>()
	for (item in this) {
		val stat = DIDArticleStat()

		stat.RoleId = item.id
		stat.DataUpdateTime           = item.dataUpdateTime
		stat.RoleStatType = item.roleStatType
		if (item.roleStatType == "") {
			stat.RoleStatType = roleStatType
		}
		stat.statId = buildStatId(item.id,stat.RoleStatType,item.ymd)
		stat.ContentNums              = item.nNums
		stat.CirculationNums          = item.mNums
		stat.ConsumerNums             = item.cNums
		stat.ConsumptionTimes         = item.cTimes
		stat.ReachNums                = item.tNums
		stat.GroupNums                = item.gNums
		stat.PotentialCirculationNums = item.mxNums
		stat.PotentialConsumerNums    = item.cxNums

		stat.sgNums                   = item.sgNums
		stat.ymd                      = item.ymd
		stat.exposureGroupNums        = item.exposureGroupNums
		stat.tTimes                   = item.tTimes
		stat.exposurePeopleNums       = item.exposurePeopleNums
		stat.revenueNums              = item.revenueNums
		stat.JoinTimes                = item.joinTimes
		stat.JoinUserCount            = item.joinUserCount
		stat.ShareGroupCount          = item.shareGroupCount

		result.add(stat)
	}
	return result.toTypedArray()
}

private suspend fun getDIDArticleStat(userAddress: String): Error? {
	val request = GetDIDAllRoleStatRequest()
	request.userAddress = userAddress
	val req = Request("",request)
	val (ret, err) = postJsonNoTokenSus<Response<GetDIDAllRoleStatResponse>>("/im/chat/GetDIDAllRoleStat2", req, getUs().nf.get(), Response(0, GetDIDAllRoleStatResponse::class.java)::class.java)
	if (err != null) {
		return err
	}

	val data = Gson().fromJson(Gson().toJson(ret.data), GetDIDAllRoleStatResponse::class.java)
	if (data.items.isEmpty()) {
		return null
	}
	val retItems = data.items.convert("")

	var failed = retItems.insert()

	if (failed.isNotEmpty()) {
		failed = (retItems.filter { item -> failed.contains(item.statId) }).toTypedArray().update()
	}

	val event = DIDArticleStat.ChangedEvent(userAddress)
	getUs().nc.postToMain(event)
	return null
}

val wait:MutableMap<String, MutableList<Channel<Error?>>> = mutableMapOf()
val mutex:Mutex = Mutex()
suspend fun GetDIDArticleStat(userAddress: String): Error? {
	val ch = Channel<Error?>(1)
	var size = 0
	mutex.lock()
	if (wait[userAddress]==null) {
		wait[userAddress] = mutableListOf()
	}
	wait[userAddress]?.add(ch)
	wait[userAddress]?.apply {
		size = this.size
	}
	mutex.unlock()

	if (size > 1) {
		return ch.receive()
	}

	val err = getDIDArticleStat(userAddress)

	var channels:MutableList<Channel<Error?>> = mutableListOf()
	mutex.lock()
	channels = wait[userAddress]?: mutableListOf()
	wait[userAddress] = mutableListOf()
	mutex.unlock()

	for (channel in channels) {
		channel.send(err)
	}
	return err
}