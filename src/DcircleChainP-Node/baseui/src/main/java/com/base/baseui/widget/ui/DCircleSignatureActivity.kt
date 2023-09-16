package com.base.baseui.widget.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.anywithyou.stream.Duration
import com.base.baseui.R
import com.base.baseui.databinding.ActivitySignDidBinding
import com.base.baseui.databinding.ItemSignDidBinding
import com.base.baseui.databinding.ListHeaderSignDidBinding
import com.base.baseui.widget.dialog.SignDialog
import com.base.baseui.widget.dialog.SignDialogVerify
import com.base.foundation.Aes
import com.base.foundation.DCircleEnv
import com.base.foundation.DCircleScope
import com.base.foundation.Env
import com.base.foundation.chain.GetNonceKey
import com.base.foundation.chain.GetNonceSus
import com.base.foundation.chain.GetSignResult
import com.base.foundation.chain.GetSignResultItem
import com.base.foundation.chain.OpCode
import com.base.foundation.chain.SignResult
import com.base.foundation.db.KeyVal
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.foundation.nc.ObserverAble
import com.base.foundation.utils.WithTimeout
import com.base.thridpart.setOnClickDelay
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.gson.Gson
import kotlinx.coroutines.launch


class DCircleSignatureActivity : AppCompatActivity(),ObserverAble {
    lateinit var binding: ActivitySignDidBinding
    private lateinit var headerViewBinding: ListHeaderSignDidBinding
    val adapter = DCircleSignatureActivityAdapter()

    override fun onBackPressed() {
        DCircleScope.launch {
            askDCircleSignatureListener.onResponse(AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.CANCEL))
        }

        finish()
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition( R.anim.activity_slide_in, R.anim.activity_slient)
        super.onCreate(savedInstanceState)
        binding = ActivitySignDidBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getUs().nc.addObserver(this, AskDCircleSignatureRequest.ChangedEvent()) {
            this.adapter.setList(it.request.items)
        }

        val json = intent.getStringExtra(AskDCircleSignatureRequest::class.java.simpleName)?:throw Error("param ${AskDCircleSignatureRequest::class.java.simpleName} required")
        val request = Gson().fromJson(json, AskDCircleSignatureRequest::class.java)
        this.adapter.setList(request.items)

        headerViewBinding = ListHeaderSignDidBinding.inflate(LayoutInflater.from(this), null, false)
        adapter.addHeaderView(headerViewBinding.root)
        adapter.bindRecyclerView(binding.recyclerView)
        headerViewBinding.tvAddress.text = request.fromEthAddress
        headerViewBinding.tvName.text = "DcircleChain Mainnet"
        headerViewBinding.tvCount.text = "${getString(R.string.number_signatures)}：${request.items.size}"

        binding.ivColse.setOnClickDelay {
            DCircleScope.launch {
                finish()
                askDCircleSignatureListener.onResponse(AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.CANCEL))
            }
        }

        binding.btnSign.setOnClickDelay {
            DCircleScope.launch {
                SignDialogVerify(request.fromEthAddress, object : SignDialog.Listener {
                    override suspend fun onSuccess(dialog: SignDialog?, aes: Aes) {
                        val response = AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.SUCCESS)
                        response.aes = aes
                        response.fromEthAddress = request.fromEthAddress
                        response.results = adapter.data.map { it.result }.toMutableList()
                        dialog?.dismiss()
                        finish()
                        askDCircleSignatureListener.onResponse(response)
                    }

                    override fun onCancel() {
                        DCircleScope.launch {
                            askDCircleSignatureListener.onResponse(AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.CANCEL))
                        }
                    }

                    override fun onError(err: Error) {
                        DCircleScope.launch {
                            askDCircleSignatureListener.onResponse(AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.FAIL))
                        }
                    }

                })
            }
        }
    }




    override fun finish() {
        super.finish()
        getUs().nc.removeAll(this)
        overridePendingTransition( R.anim.activity_slient, R.anim.activity_slide_out)
    }


    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    inner class DCircleSignatureActivityAdapter :
        BaseQuickAdapter<AskDCircleSignatureRequest.Item, BaseViewHolder>(R.layout.item_sign_did) {

        fun bindRecyclerView(recyclerView: RecyclerView) {
            recyclerView.adapter = this
        }

        override fun convert(holder: BaseViewHolder, item: AskDCircleSignatureRequest.Item) {
            val binding = ItemSignDidBinding.bind(holder.itemView)
            binding.tvTxnAsh.text = item.TxnHash
            binding.tvType.text = item.name
            binding.tvSignSort.text = "${getString(R.string.login_signature_1)}${adapter.getItemPosition(item)+1}"
            val messages:MutableList<String> = mutableListOf()
            if (item.payload.value.isNotEmpty()) {
                messages.add(String(item.payload.value, Charsets.UTF_8))
            }
            messages.add("RLP(message): " + "0x" + item.result.getRPLMessage())
            if (Env == DCircleEnv.pro) {
                binding.tvInformation.text = messages.joinToString("\n")
            } else {
                messages.add(0, "nonce: ${item.result.getNonce()}")
                binding.tvInformation.text = messages.joinToString("\n")
            }
        }
    }

}

class AskDCircleSignatureRequest(var fromEthAddress:String = getUs().getUid()) {
    class ChangedEvent : NcEvent<String>() {
        var request:AskDCircleSignatureRequest = AskDCircleSignatureRequest()
    }

    open class Payload protected constructor(var value: ByteArray) {
        var valid:Boolean = false
        companion object {
            fun New(vararg input:Pair<String, String> = arrayOf()):Payload {
                val items:MutableList<String> = mutableListOf()
                for ((key, value) in input) {
                    items.add("${key}: $value")
                }

                val payload =  Payload(items.joinToString("\n").toByteArray(Charsets.UTF_8))
                payload.valid = true
                return payload
            }
        }

    }
    class Item(
        var name: String,
        var toEthAddress: String,
        var opcode: OpCode,
        var payload: Payload,
        var result: SignResult = SignResult(),
        var actionId:String? = null
    ) {
        var TxnHash:String = ""
    }

    val items:MutableList<Item> = mutableListOf()
}

class AskDCircleSignatureResponse(var code:Code) {
    enum class Code(var int: Int) {
        SUCCESS(1),
        FAIL(0),
        CANCEL(-1)
    }

    var fromEthAddress:String = ""
    var aes:Aes = Aes()

    var results:MutableList<SignResult> = mutableListOf()
}

interface AskDCircleSignatureListener : ObserverAble {
    suspend fun onResponse(response:AskDCircleSignatureResponse)
}

private var askDCircleSignatureListener: AskDCircleSignatureListener = object : AskDCircleSignatureListener {
    override suspend fun onResponse(response: AskDCircleSignatureResponse) {
    }
}
suspend fun askDCircleSignature(request: AskDCircleSignatureRequest, listener: AskDCircleSignatureListener){
    askDCircleSignatureListener = object : AskDCircleSignatureListener {
        override suspend fun onResponse(response: AskDCircleSignatureResponse) {
            Log.d("AskDCircleSignature", "request=${Gson().toJson(request)} onResponse=${Gson().toJson(response)}")
            getUs().nc.removeEvent(listener, KeyVal.ChangedEvent(GetNonceKey(address = request.fromEthAddress)))
            getUs().nc.removeAll(listener)
            listener.onResponse(response)
        }
    }

    WithTimeout(Duration(200*Duration.Millisecond)) {
        getUs().nc.addObserver(listener, KeyVal.ChangedEvent(GetNonceKey(address = request.fromEthAddress))) {
            getUs().nc.removeEvent(listener, KeyVal.ChangedEvent(GetNonceKey(address = request.fromEthAddress)))

            DCircleScope.launch {
                val nonce = GetNonceSus(address = request.fromEthAddress, onlyDB = true)?:return@launch

                val results = GetSignResult(request.items.map {
                    require(it.payload.valid) {"payload is invalid, please use Payload.New Build Object."}
                    return@map GetSignResultItem(it.toEthAddress, it.opcode, it.payload.value, it.actionId ?: "")
                }.toTypedArray(), nonce, request.fromEthAddress)
                for (i in results.indices) {
                    request.items[i].TxnHash = results[i]?.getTxnHash().toString()
                    results[i]?.let { request.items[i].result = it }
                }

                AskDCircleSignatureRequest.ChangedEvent().apply {
                    this.request = request
                    getUs().nc.postToMain(this)
                }
            }
        }
        GetNonceSus(address = request.fromEthAddress, onlyDB = false)
    }

    val nonce = GetNonceSus(address = request.fromEthAddress, onlyDB = true)
    if (nonce==null) {
        listener.onResponse(AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.FAIL))
        return
    }

    val results = GetSignResult(request.items.map {
        require(it.payload.valid) {"payload is invalid, please use Payload.New Build Object."}
        return@map GetSignResultItem(it.toEthAddress, it.opcode, it.payload.value, it.actionId ?: "")
    }.toTypedArray(), nonce, request.fromEthAddress)
    for (i in results.indices) {
        request.items[i].TxnHash = results[i]?.getTxnHash().toString()
        results[i]?.let { request.items[i].result = it }
    }

    ActivityUtils.getTopActivity()?.apply {
        val intent = Intent(this, DCircleSignatureActivity::class.java)
        intent.putExtra(AskDCircleSignatureRequest::class.java.simpleName, Gson().toJson(request))
        this.startActivity(intent)
    }
}