package com.yhtech.did.ui.activity

import OkDownloader
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.base.foundation.BranchName
import com.base.foundation.DCircleEnv
import com.base.foundation.Env
import com.base.foundation.NetScope
import com.base.foundation.oss.GetSandboxDBFile
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setVisible
import com.google.gson.Gson
import com.yhtech.did.R
import com.yhtech.did.databinding.ActivityWebBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

@Route(path = Router.Did.webActivity)
class WebActivity : AppCompatActivity() {
    lateinit var binding :ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWebView()
        binding.tvReload.setOnClickDelay{
            binding.llErr.setVisible(false)
            binding.webView.setVisible(true)
            binding.webView.reload()
        }
    }

    private fun getUrl():String{
        return intent.getStringExtra(Constants.PARAM).toString()
    }

    private fun getTvTitle():String{
        return intent.getStringExtra(Constants.TITLE)?:""
    }

    private fun initWebView(){
        initWebViewConfig()
        setDefaultZoom()
        initProgress()
        initClick()
        Log.d("WebActivity", "loadUrl=${getUrl()}")
        binding.webView.loadUrl(getUrl())
        if (getTvTitle().isNotEmpty()) {
            binding.titleView.setTitle(getTvTitle())
        }
    }

    private fun initClick(){
        checkForward()
        binding.titleView.setLeftImage(R.mipmap.ic_close_black)
        binding.ivWebBack.setOnClickDelay {
            if(binding.webView.canGoBack()){
                binding.webView.goBack()
                checkForward()
                return@setOnClickDelay
            }
            finish()
        }
        binding.ivWebForward.setOnClickDelay {
            if(binding.webView.canGoForward()){
                binding.webView.goForward()
                checkForward()
                return@setOnClickDelay
            }
        }
    }

    private fun checkForward(){
        if(binding.webView.canGoForward()){
            binding.ivWebForward.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, com.yhtech.image_preview.R.color.black))
        }else{
            binding.ivWebForward.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, com.yhtech.image_preview.R.color.color_dbdbdb))
        }
    }

    private fun initProgress(){
        binding.pbWeb.max = 100

        // 设置setWebChromeClient对象
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.pbWeb.progress = newProgress

                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }
        }


        //设置此方法可在WebView中打开链接
        binding.webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                setErrUi()
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return
                }
                setErrUi()
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.pbWeb.visibility = View.INVISIBLE

            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.pbWeb.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url!!)
                return true
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {

                if (Env == DCircleEnv.dev) {
                    Log.d("WebActivity", "will request=${Gson().toJson(request)}")
                    return super.shouldInterceptRequest(view, request)
                }

                request?.apply {
                    Log.d("WebActivity", "method=${this.method} url=${this.url} path=${this.url.path} start request=${Gson().toJson(this)}")
                    if (this.url.path.isNullOrEmpty()) {
                        return@apply
                    }

                    val path = this.url.path.toString()
                    val file = File(GetSandboxDBFile().path(path))
                    Log.d("WebActivity", "method=${this.method} url=${this.url} path=${this.url.path} file=${file}")
                    if (!file.exists()) {
                        NetScope.launch {
                            OkDownloader(this@apply.url.toString(), GetSandboxDBFile().path(path)).Start()
                        }
                        return@apply
                    }

                    getMimeType(file)?.let { mimeType ->
                        try {
                            Log.d("WebActivity", "method=${this.method} url=${this.url} path=${this.url.path} mimeType=${mimeType}")
                            return WebResourceResponse(mimeType, "UTF-8", FileInputStream(file))
                        } catch (_:Exception) { }
                    }
                }

                Log.d("WebActivity", "will request=${Gson().toJson(request)}")
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    fun setErrUi(){
        binding.apply {
            webView.setVisible(false)
            llErr.setVisible(true)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebViewConfig(){
        binding.webView.settings.apply {
            userAgentString = "DCircle Android $BranchName"
            javaScriptEnabled = true
            setSupportZoom(false)
            blockNetworkImage = false
            builtInZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            databaseEnabled = true
            domStorageEnabled = true
            allowFileAccessFromFileURLs = true
            loadsImagesAutomatically = true
        }
    }

    private fun setDefaultZoom() {
       binding.webView.apply {
           setInitialScale(1)
           settings.loadWithOverviewMode = true
           settings.useWideViewPort = true
       }
    }
}

fun getMimeType(file: File): String? {
    val extension = file.extension.lowercase()
    return when (extension.lowercase()) {
        "html" -> "text/html"
        "css" -> "text/css"
        "js" -> "application/javascript"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "svg" -> "image/svg+xml"
        "mp3" -> "audio/mpeg"
        "ogg" -> "audio/ogg"
        "wav" -> "audio/wav"
        "webm" -> "audio/webm"
        "mp4" -> "video/mpeg"
        "ogv" -> "video/ogg"
        "ttf" -> "font/ttf"
        "otf" -> "font/otf"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "pdf" -> "application/pdf"
        "zip" -> "application/zip"
        else -> null
    }
}