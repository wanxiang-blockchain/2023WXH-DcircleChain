package dcircle.identity.node.ui.api

import com.base.foundation.sendSus
import com.base.foundation.db.KeyVal
import com.base.foundation.db.insert
import com.base.foundation.db.update
import com.base.foundation.getUs
import com.google.gson.Gson

private class GetNodeConfigRequest

class GetNodeConfigResponse {
    var netWork: String = ""
    var serviceProvider: String = ""
    var cpu: String = ""
    var memory: String = ""
    var storage: String = ""
}

suspend fun GetNodeConfig():Error? {
    val request = GetNodeConfigRequest()

    val (ret, err) = sendSus("/im/chat/NodeConfig", request, GetNodeConfigResponse::class.java)
    if (err != null) {
        return err
    }

    val keyVal = KeyVal()
    keyVal.Key = KeyVal.Keys.NodeConfig.toString()
    keyVal.Value = Gson().toJson(ret)
    if (keyVal.insert()!=null) {
        keyVal.update()
    }

    getUs().nc.postToMain(KeyVal.ChangedEvent(KeyVal.Keys.NodeConfig.toString()))

    return null
}