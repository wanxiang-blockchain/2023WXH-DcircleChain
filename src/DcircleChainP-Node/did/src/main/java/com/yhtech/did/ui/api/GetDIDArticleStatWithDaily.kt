package com.yhtech.did.ui.api

import com.base.baseui.widget.api.convert
import com.base.baseui.widget.api.GetDIDAllRoleStatResponse
import com.base.foundation.sendSus
import com.base.foundation.db.DIDArticleStat
import com.base.foundation.db.insert
import com.base.foundation.db.update
import com.base.foundation.getUs

class GetDIDArticleStatWithDailyRequest {
    var didAddress: String = ""
    var dates  :Array<String> = arrayOf()
}

class GetDIDArticleStatWithDailyResponse {
    var items: Array<GetDIDAllRoleStatResponse.Item> = arrayOf()
}

suspend fun GetDIDArticleStatWithDaily(didAddress: String,dates:Array<String>): Error? {
    val request = GetDIDArticleStatWithDailyRequest()
    request.didAddress = didAddress
    request.dates   = dates
    val (ret, err) = sendSus("/im/chat/getDIDArticleStatWithDaily", request, GetDIDArticleStatWithDailyResponse::class.java)
    if (err != null) {
        return err
    }
    if (ret.items.isEmpty()) {
        return null
    }
    val retItems = ret.items.convert(DIDArticleStat.StatRoleType.ArticleStat.value)

    var failed = retItems.insert()
    if (failed.isNotEmpty()) {
        failed = (retItems.filter { item -> failed.contains(item.statId) }).toTypedArray().update()
    }

    val event = DIDArticleStat.ChangedEvent(didAddress)
    getUs().nc.postToMain(event)
    return null
}
