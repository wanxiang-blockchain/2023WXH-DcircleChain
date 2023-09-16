package com.yhtech.image_preview.ui.activity

import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.anywithyou.stream.Duration
import com.base.foundation.Aes
import com.base.foundation.DCircleScope
import com.base.foundation.db.DownloadTask
import com.base.foundation.db.GetProgress
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.base.foundation.oss.DecryptOSSToDBFile
import com.base.foundation.oss.GetSandboxDBFile
import com.base.foundation.utils.MakeToast
import com.base.thridpart.constants.Router
import com.base.thridpart.setInVisible
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.FileUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaeger.library.StatusBarUtil
import com.yhtech.image_preview.R
import com.yhtech.image_preview.databinding.ActivityImageListBinding
import com.yhtech.image_preview.ui.Im
import com.yhtech.image_preview.ui.adapter.ImagePreviewListAdapter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File


@Route(path = Router.ImagePre.imgListPreview)
class ImageListActivity : AppCompatActivity(),ObserverAble {
    lateinit var binding:ActivityImageListBinding
    private var initPos = 0
    var chatMode = true
    var adapter = ImagePreviewListAdapter()
    private var isShowOperation = true
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        //设置
        StatusBarUtil.setDarkMode(this)
        StatusBarUtil.setTranslucent(this)
        getIntentData()
        initClick()
    }

    fun showOperation(){
        if (isShowOperation) {
            binding.liOperation.setInVisible(false)
            initOperationClick()
            DCircleScope.launch {
                delay(5000)
                binding.liOperation.setInVisible(true)
                binding.liOperation.setOnClickDelay { }
                binding.imgMore.setOnClickDelay { }
                binding.imgDownLoad.setOnClickDelay { }
            }
        }else{
            binding.liOperation.setInVisible(true)
        }
    }

    private fun initOperationClick(){
        binding.liOrginalDownload.setOnClickDelay {
            val origin = adapter.data[binding.viewpagerImglist.currentItem].original
            if (origin.objectId.isEmpty()) {
                return@setOnClickDelay
            }

            DCircleScope.launch {
                getUs().nc.removeAll(this@ImageListActivity)
                getUs().nc.addObserver(this@ImageListActivity, DownloadTask.ProgressEvent(origin.objectId)) {
                    DCircleScope.launch {
                        val item = adapter.data[binding.viewpagerImglist.currentItem].original
                        if (it.objectId  == item.objectId) {
                            val progress = DownloadTask.GetProgress(origin.objectId)
                            binding.tvDrawingSize.text = "(${progress}%)"
                        }
                    }
                }
                getUs().nc.addObserver(this@ImageListActivity, DownloadTask.SuccessEvent(origin.objectId)) {
                    getUs().nc.removeEvent(this@ImageListActivity, DownloadTask.SuccessEvent(origin.objectId))
                    getUs().nc.removeEvent(this@ImageListActivity,  DownloadTask.ProgressEvent(origin.objectId))

                    val item = adapter.data[binding.viewpagerImglist.currentItem].original
                    if (it.objectId  == item.objectId) {
                        binding.liOrginalDownload.setVisible(false)
                    }
                }
                getUs().downloader.AddTask(DownloadTask(origin.objectId, 0))
            }
        }
        binding.imgDownLoad.setOnClickDelay {
            var item = adapter.data[binding.viewpagerImglist.currentItem].original
            if (item.objectId.isEmpty()) {
                item =  adapter.data[binding.viewpagerImglist.currentItem].large
            }
            if (item.objectId.isEmpty()) {
                item =  adapter.data[binding.viewpagerImglist.currentItem].thumb
            }
            val objectId = item.objectId
            val objectKey = item.key
            val suffix = item.type
            getUs().nc.removeEvent(DownloadTask.SuccessEvent::class.java)
            getUs().nc.addEvent(DownloadTask.SuccessEvent(objectId)){e,removeIt->
                removeIt()

                DCircleScope.launch {
                    if (DecryptOSSToDBFile(objectId, Aes(objectKey))!=null) {
                        return@launch
                    }
                    val file = File(GetSandboxDBFile().path(objectId))
                    val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),  file.name+".jpg")
                    if (path.exists()){
                        return@launch
                    }
                    val isSuccess = FileUtils.copy(file, path)
                    val data = file.canRead()
                    if (!isSuccess) {
                        MakeToast.showShort(getString(R.string.download_failed))
                        return@launch
                    }

                    // 通知媒体库有新的图片
                    MediaScannerConnection.scanFile(applicationContext, arrayOf(path.absolutePath), null){_,_->
                        MakeToast.showShort(getString(R.string.toast_save_photo))
                    }
                }
            }

            DCircleScope.launch {
                try {
                    withTimeout(Duration.Second/Duration.Millisecond) {
                        DCircleScope.launch {
                            getUs().downloader.AddTask(DownloadTask(objectId, 0))
                        }
                    }
                } catch (e:TimeoutCancellationException) {
                    MakeToast.showShort(getString(R.string.update_downloading))
                }
            }

        }
    }
    private fun initClick() {
        showOperation()
        binding.viewpagerImglist.setOnClickDelay {
            finishAfterTransition()
        }

        binding.imgBack.setOnClickDelay {
            finishAfterTransition()
        }
    }


    private fun initRecycler() {
        binding.viewpagerImglist.adapter = adapter
        binding.viewpagerImglist.registerOnPageChangeCallback(object :OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (chatMode){
                    val data = adapter.data[position]
                    updateSize(data)
                }
                showOperation()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    fun updateSize(data:Im.MsgImageContent){
        if (data.original.objectId.isEmpty()) {
            binding.liOrginalDownload.setVisible(false)
            return
        }

        if (File(GetSandboxDBFile().path(data.original.objectId)).exists()) {
            binding.liOrginalDownload.setVisible(false)
            return
        }

        binding.liOrginalDownload.setVisible(true)
    }

    private val lists:List<Im.MsgImageContent> get() {
        return try {
            val json = intent.getStringExtra("data")?:""
            Gson().fromJson(json,object : TypeToken<List<Im.MsgImageContent>>(){}.type)
        } catch (_:Exception) {
            emptyList()
        }
    }
    private fun getIntentData() {
        chatMode = intent.getBooleanExtra("chatMode",false)
        initPos = intent.getIntExtra("initPos",0)
        isShowOperation = intent.getBooleanExtra("isShowOperation",true)
        binding.liOrginalDownload.setVisible(chatMode)
        binding.liOperation.setVisible(chatMode)
        binding.imgMore.setVisible(chatMode)


        adapter.setList(lists)
        if (adapter.data.size>0){
            binding.viewpagerImglist.setCurrentItem(initPos,false)
            if (chatMode){
                updateSize(adapter.data[initPos])
            }
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            onBackPressed()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> startY = ev.rawY
            MotionEvent.ACTION_UP -> {
                val endY = ev.rawY
                val distanceY = endY - startY
                if (distanceY > 200) { // 设置滑动退出的阈值，可以根据实际情况调整
                    finish()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}