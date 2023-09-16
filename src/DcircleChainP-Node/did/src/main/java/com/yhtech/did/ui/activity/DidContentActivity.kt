package com.yhtech.did.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.base.baseui.widget.dialog.TipsDialog
import com.base.baseui.widget.others.WaitSecretKeyPage
import com.base.foundation.DCircleScope
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.DIDArticleLog
import com.base.foundation.db.DIDBlockMetaNode
import com.base.foundation.db.findByAddress
import com.base.foundation.db.GetDIDArticleARMeta
import com.base.foundation.db.GetDIDArticleEncMeta
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.base.foundation.utils.fromHex
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay

import com.blankj.utilcode.util.ActivityUtils
import com.google.gson.Gson
import com.yhtech.did.R
import com.yhtech.did.databinding.ActivityDidContentBinding
import com.yhtech.did.databinding.LayoutDidContentHeaderBinding
import com.yhtech.did.ui.api.getDIDArticle
import com.yhtech.did.ui.api.GetDIDMeBuy
import com.yhtech.did.ui.rv.DIDArticleAdapter
import com.yhtech.image_preview.ui.Im
import com.yhtech.image_preview.ui.UIQueue
import com.yhtech.image_preview.ui.bean.GetDIDAesKey
import com.yhtech.image_preview.ui.invoke
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Route(path = Router.Did.didContent)
class DidContentActivity : AppCompatActivity(), ObserverAble,DIDArticleAdapter.Owner {

    private lateinit var binding: ActivityDidContentBinding
    private lateinit var headerBinding: LayoutDidContentHeaderBinding
    private val queue = UIQueue()
    private var mAdapter = DIDArticleAdapter(this)
    private lateinit var waitSecretKeyPage: WaitSecretKeyPage

    companion object {
        fun goIntent(context: Context, didAddress: String) {
            val intent = Intent(context, DidContentActivity::class.java)
            intent.putExtra(Constants.PARAM, didAddress)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDidContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        headerBinding = LayoutDidContentHeaderBinding.inflate(layoutInflater)
        waitSecretKeyPage = WaitSecretKeyPage(this)
        require(didAddress.isNotEmpty()) {"didAddress can not be empty."}
        initUi()

        getUs().nc.addObserver(this,DIDArticle.ChangedEvent(didAddress)) { event->
            DCircleScope.launch {
                queue.invoke {
                    loadFromDB()
                }
            }
        }
        getUs().nc.addObserver(this,DIDArticle.AddSecretKeySuccessEvent(didAddress)){event ->
            DCircleScope.launch {
                queue.invoke {
                    loadFromDB()
                }
            }
        }


        DCircleScope.launch {
            queue.invoke {
                loadFromDB()
            }
            loadFromServer()
        }
    }

    private val logItem:DIDArticleLog.Item?
    get() {
        return try {
            Gson().fromJson(intent.getStringExtra("logItem"), DIDArticleLog.Item::class.java)
        } catch (_:Exception) {
            null
        }
    }

    private val didAddress:String
    get() {
        return intent.getStringExtraOrFromBundle(Constants.PARAM)
    }

    val fromDidAddress:String get() {
        return intent.getStringExtraOrFromBundle("fromDidAddress")
    }

    private fun Intent.getStringExtraOrFromBundle(key: String): String {
        val stringValue = this.getStringExtra(key)
        if (!stringValue.isNullOrEmpty()) {
            return stringValue
        }

        val bundleValue = this.getBundleExtra(Constants.BUNDLE)?.getString(key, "")
        return bundleValue ?: ""
    }

    private fun initRvScro(){
        binding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollY = recyclerView.computeVerticalScrollOffset()
                if(scrollY > headerBinding.root.height.toFloat()){
                    binding.titleViewTitle.text = headerBinding.tvTitle.text
                }else{
                    binding.titleViewTitle.text = " "
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun initUi() {
        binding.ivBack.setOnClickListener {
            if (ActivityUtils.getTopActivity()!=null){
                ActivityUtils.getTopActivity().finish()
            }
        }

        initRvScro()
        logItem?.apply {
            when(this.updateType){
                DIDArticleLog.Item.UpdateType.Content.int, DIDArticleLog.Item.UpdateType.TokenAddress.int->{
                    headerBinding.tvVersionName.text = "v${this.version} ${getString(R.string.update_content)}"
                }
                DIDArticleLog.Item.UpdateType.Abstract.int->{
                    headerBinding.tvVersionName.text = "v${this.version} ${getString(R.string.update_settings)}"
                }
            }

            headerBinding.didContetNowSee.setOnClickDelay {
                ARouter.getInstance().build(Router.Did.didHistory)
                    .withString(Constants.PARAM,didAddress)
                    .navigation()
            }
        }

        binding.ivShare.setOnClickDelay {
            DCircleScope.launch {
//                val intent = Intent(this@DidContentActivity, ForWardActivity::class.java)
//                intent.putExtra("forwardType", ForwardType.ForwardTypeDid.value)
//                intent.putStringArrayListExtra("msgIds", arrayListOf(didAddress))
//                intent.putStringArrayListExtra("chatIdsDisabled", arrayListOf(GetDCircleChatId()))
//                startActivity(intent)
            }
        }
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(this@DidContentActivity)
            adapter = mAdapter
        }
        mAdapter.setHeaderView(headerBinding.root)
    }



    private suspend  fun loadFromDB() {
        val article = DIDArticle.findByAddress(didAddress)?:return
        if (article.Status == DIDArticle.EStatus.Delete.int) {
            binding.tvDeleted.isVisible = true
            return
        }

        val aes = GetDIDAesKey(article.SecretKey)
        if (aes == null) {
            TipsDialog(this@DidContentActivity, getString(R.string.tips), getString(R.string.error_please_try_again), confirmText = getString(R.string.confirm),
                null
            ) {

            }.showDialog()
            return
        }


        GetDIDArticleEncMeta(article)?.apply {
            headerBinding.tvTitle.text = this.Title.text
            for (item in this.Content.filter { it.type == DIDBlockMetaNode.Type.Image.int }) {
                try {
                    item.objectKey = String(aes.decrypt(fromHex(item.objectKey)))
                    Log.d("DidContentActivity", "aes decrypt key=${aes.key} item=${Gson().toJson(item)}")
                } catch (e:Exception) {
                    Log.e("DidContentActivity", "aes decrypt key=${aes.key} item=${Gson().toJson(item)} err=${e}")
                }
            }
            mAdapter.setCurDidAddress(didAddress)
            mAdapter.setList(this.Content.toMutableList())
        }
        GetDIDArticleARMeta(article)?.apply {
            headerBinding.tvDesc.text = this.Title.text
        }
    }

  private suspend  fun loadFromServer() {
      coroutineScope {
          listOf(
              async {
                  getDIDArticle(arrayOf(didAddress))
              },
              async {
                  GetDIDMeBuy(arrayOf(didAddress))
              }
          ).awaitAll()
      }
  }


    override fun onDestroy() {
        super.onDestroy()
        getUs().nc.removeAll(this)
    }

    override suspend fun onImageNodeClick(position: Int) {
        val images = mAdapter.data.filter { it.type == DIDBlockMetaNode.Type.Image.int }
        if (images.isEmpty()) {
            return
        }
        val imageIndex = position - 1
        val lists = images.map {
            val image = Im.ImageAttachment().apply {
                this.objectId = it.objectId
                this.key = it.objectKey
                this.height = it.height
                this.width = it.width
                this.type = it.suffix
            }
            Im.MsgImageContent().apply {
                this.original = image
                this.large = image
                this.thumb = image
            }
        }
        ARouter.getInstance().build(Router.ImagePre.imgListPreview)
            .withBoolean("chatMode",false)
            .withBoolean("isShowOperation",false)
            .withInt("initPos",imageIndex)
            .withString("data",Gson().toJson(lists))
            .navigation()


    }
}


