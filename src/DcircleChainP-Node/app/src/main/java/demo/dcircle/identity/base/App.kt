package demo.dcircle.identity.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.base.baseui.widget.utils.LocalManageUtil
import com.base.foundation.BaseApplication
import com.base.foundation.base.AppRouter
import com.base.thridpart.constants.Constants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.github.jokar.multilanguages.library.MultiLanguage

class App: BaseApplication(), Application.ActivityLifecycleCallbacks  {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        System.loadLibrary("TrustWalletCore")
        MultiLanguage.init { context -> LocalManageUtil.getSetLanguageLocale(context) }
        MultiLanguage.setApplicationLanguage(this)
    }

    override fun attachBaseContext(base: Context?) {
        initAppRouteObserver()
        LocalManageUtil.saveSystemCurrentLanguage(base)
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            AppUtils.relaunchApp(true)
        }
        super.attachBaseContext(MultiLanguage.setLocal(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalManageUtil.saveSystemCurrentLanguage(applicationContext, newConfig)
        MultiLanguage.onConfigurationChanged(applicationContext)
    }

    private fun initAppRouteObserver() {
        AppRouter.initObserver {
            //不为空
            ARouter.getInstance().build(it.routePath)
                .withString(it.routeKey.ifEmpty { Constants.PARAM }, it.routeParam)
                .withBundle(Constants.BUNDLE,it.bundle?: Bundle())
                .navigation(AppRouter.context?.get(),object : NavigationCallback {
                    override fun onFound(postcard: Postcard?) {}

                    override fun onLost(postcard: Postcard?) {
                        AppRouter.clear()
                    }

                    override fun onArrival(postcard: Postcard?) {
                        AppRouter.clear()
                    }

                    override fun onInterrupt(postcard: Postcard?) {
                        AppRouter.clear()
                    }
                })
        }
    }



}