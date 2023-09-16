package com.base.foundation.oss

import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.base.foundation.getAppContext


fun GetOssClient(stsToken: AliyunStsToken, endpoint: String):OSSClient {
    val configuration = ClientConfiguration()
    // 设置最大并发数。
    configuration.maxConcurrentRequest = 5
    // 设置Socket层传输数据的超时时间。
    configuration.socketTimeout = 50000
    // 设置建立连接的超时时间。
    configuration.connectionTimeout = 50000
    // 设置日志文件大小。
    configuration.maxLogSize = (3 * 1024 * 1024).toLong()
    // 请求失败后最大的重试次数。
    configuration.maxErrorRetry = 3
    val credentialProvider: OSSCredentialProvider = OSSStsTokenCredentialProvider(stsToken.accessKeyId, stsToken.accessKeySecret, stsToken.stsToken)
    // 创建OSSClient实例。
    return OSSClient(getAppContext(), endpoint, credentialProvider,configuration)
}


suspend fun GetOSSClient():Pair<OSSClient?, BucketInfo?> {
    val (bucketInfo, err1) = GetBucketInfo()
    if (err1!=null) {
        return Pair(null, null)
    }


    val (stsToken, err) = GetAliyunSTSToken()
    if (err!=null) {
        return Pair(null, null)
    }

    val client = GetOssClient(stsToken, bucketInfo.endpoint)
    return Pair(client, bucketInfo)
}