package com.yhtech.did.ui.api

suspend fun getDIDArticleStat(userAddress: String): Error? {
    return com.base.baseui.widget.api.GetDIDArticleStat(userAddress)
}
