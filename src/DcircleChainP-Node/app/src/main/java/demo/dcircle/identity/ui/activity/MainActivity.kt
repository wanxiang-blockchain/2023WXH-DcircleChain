package demo.dcircle.identity.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.base.baseui.widget.utils.SPUtil
import com.base.foundation.AppStateValue
import com.base.foundation.AppStateValueChangeEvent
import com.base.foundation.DCircleScope
import com.base.foundation.NetScope
import com.base.foundation.PPKey
import com.base.foundation.api.AppState
import com.base.foundation.api.getAppState
import com.base.foundation.api.GetNetConfig
import com.base.foundation.api.GetShareConfig
import com.base.foundation.api.setAppState
import com.base.foundation.api.SyncDIDBrowser
import com.base.foundation.api.http.StreamConnected
import com.base.foundation.chain.GetNonceSus
import com.base.foundation.db.Account
import com.base.foundation.db.ChatCmd
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.findByAddress
import com.base.foundation.db.FindCurrentProcess
import com.base.foundation.db.Me
import com.base.foundation.db.SetAllStatus
import com.base.foundation.getUs
import com.base.foundation.getWalletKey
import com.base.foundation.nc.ObserverAble
import com.base.foundation.setUs
import com.base.foundation.setWalletKey
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.OnNetworkStatusChangedListener
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.Utils
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.gson.Gson
import com.gyf.immersionbar.ImmersionBar
import com.yhtech.did.ui.BuildDIDArticleState
import com.yhtech.did.ui.fragment.DemoDidFragment
import com.yhtech.did.ui.push.pullDidCommand
import com.yhtech.did.ui.api.getDIDArticle
import com.yhtech.did.ui.api.GetLastStatTime
import dcircle.identity.deapps.ui.fragment.DeAppsFragment
import dcircle.identity.node.ui.fragment.NodeFragment
import demo.dcircle.identity.R
import demo.dcircle.identity.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = Router.Main.mainPage)
class MainActivity : AppCompatActivity() ,ObserverAble, OnNetworkStatusChangedListener, Utils.OnAppStatusChangedListener{
    private var passwordBadge: View?=null
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FIX: 当从外部应用返回 Dcircle 时，有可能会重新创建一个 Activity，此时 us 无效，但可能恢复
        savedInstanceState?.getString(::getWalletKey.name)?.let {
            if (getUs().isValid()) {
                return@let
            }

            DCircleScope.launch {
                val ppk = Gson().fromJson(it, PPKey::class.java)
                setUs(getUs().clone(ppk.address))
                setWalletKey(ppk)
            }

            startActivity(Intent(this@MainActivity, SplashActivity::class.java))
            finish()
            return
        }

        initUI()
        initNC()

        ImmersionBar.with(this)
            .autoDarkModeEnable(true)
            .navigationBarColor(R.color.color_f9f9f9)
            .statusBarColor(R.color.white)
            .fitsSystemWindows(true)
            .init()

        DCircleScope.launch {
            withContext(Dispatchers.IO) {
                val job1 = async {
                    val article = DIDArticle.FindCurrentProcess()
                    article?.apply {
                        BuildDIDArticleState(this@MainActivity, article.Address).DidEnter()
                    }
                }

                val job2 = async {
                    GetLastStatTime()
                }

                val job5 = async {
                    pullDidCommand()
                }

                listOf(job1, job2, job5).awaitAll()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Me.me, getUs().getUid())
        outState.putString(::getWalletKey.name, Gson().toJson(getWalletKey()))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val changeLanguage = intent?.getBundleExtra(Constants.BUNDLE)?.getBoolean("changeLanguage",false)
        if (changeLanguage == true){
            reStart(this)
        }
    }

    private fun reStart(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }


    private fun initNC() {
        com.yhtech.did.ui.push.registerStreamPush()
        AppUtils.registerAppStatusChangedListener(this)
        NetworkUtils.registerNetworkStatusChangedListener(this)

        getUs().nc.addObserver(this,StreamConnected::class.java){e ->
            DCircleScope.launch {
                setAppState(getAppState())
            }
        }

        getUs().nc.addObserver(this,Account.ChangedEvent::class.java){
            DCircleScope.launch {
                Account.findByAddress(getUs().getUid())?.apply {
                    val view = (binding.bottomMain.getChildAt(0) as BottomNavigationMenuView).getChildAt(2) as BottomNavigationItemView
                    val bottomMenuView = binding.bottomMain.getChildAt(0) as BottomNavigationMenuView
                    val bottomNavItemView = bottomMenuView.getChildAt(2) as BottomNavigationItemView
                    if (this.status == Account.Status.DefaultPassword.int){
                        val iconView = (bottomNavItemView.getChildAt(0) as FrameLayout).getChildAt(1) as ImageView
                        val iconLocation = IntArray(2)
                        iconView.getLocationOnScreen(iconLocation)

                        val bottomNavItemLocation = IntArray(2)
                        bottomNavItemView.getLocationOnScreen(bottomNavItemLocation)
                        val iconSize = resources.getDimensionPixelSize(R.dimen.dp_22)
                        passwordBadge = View(this@MainActivity)
                        val viewSize = SizeUtils.dp2px(8f) // 设置view的大小
                        val viewParams = ViewGroup.MarginLayoutParams(viewSize, viewSize)
                        var badgeMarginLeft = iconLocation[0] + iconSize - viewSize - bottomNavItemLocation[0]
                        if (badgeMarginLeft != 0){
                            SPUtil.getInstance(this@MainActivity).badgeMarginLeft = badgeMarginLeft
                        }else{
                            badgeMarginLeft = SPUtil.getInstance(this@MainActivity).badgeMarginLeft
                        }
                        var badgeMarginTop = iconLocation[1] - bottomNavItemLocation[1]
                        if (badgeMarginTop != 0){
                            SPUtil.getInstance(this@MainActivity).badgeMarginTop = badgeMarginTop
                        }else{
                            badgeMarginTop = SPUtil.getInstance(this@MainActivity).badgeMarginTop
                        }
                        viewParams.setMargins(badgeMarginLeft, badgeMarginTop, 0, 0)
                        passwordBadge?.layoutParams = viewParams
                        passwordBadge?.setBackgroundResource(R.drawable.shape_round_f15151_9)
                        bottomNavItemView.addView(passwordBadge)
                    } else {
                        if (passwordBadge!=null) {
                            bottomNavItemView.removeView(passwordBadge)
                        }
                    }
                }
            }
        }

        getUs().nc.addObserver(this, AppStateValueChangeEvent(AppStateValue.Synced)) {
            NetScope.launch {
                val job2 = async {
                    val article = DIDArticle.FindCurrentProcess()
                    article?.apply {
                        if (this.editingDevice != DeviceUtils.getUniqueDeviceId()) {
                            return@apply
                        }

                        getDIDArticle(arrayOf(this.Address))
                    }
                }

                val job4 = async {
                    SyncDIDBrowser()
                }

                val job6 = async {
                    pullDidCommand()
                }
                listOf(job2, job4, job6).awaitAll()
            }
        }
    }

    private fun initUI() {
        val fragments = listOf(DemoDidFragment(), NodeFragment(), DeAppsFragment())
        val fragmentAdapter = FragmentAdapter(this, fragments)
        binding.viewMainPager.adapter = fragmentAdapter
        binding.bottomMain.foregroundTintList = null
        binding.bottomMain.itemIconTintList = null
        binding.bottomMain.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.action_chat -> {binding.viewMainPager.setCurrentItem(0,false) }
                R.id.action_main->{binding.viewMainPager.setCurrentItem(1,false) }
                R.id.action_did->{binding.viewMainPager.setCurrentItem(2,false)}
            }
            false
        }
        binding.bottomMain.getChildAt(0).setOnLongClickListener { //拦截长按事件
            false
        }

        //page滑动监听（下面）
        binding.viewMainPager.isUserInputEnabled = false
        binding.viewMainPager.offscreenPageLimit = 1
        binding.viewMainPager.isUserInputEnabled = false

        binding.viewMainPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomMain.menu.getItem(position).isChecked=true
            }

        })

        DCircleScope.launch {
            ChatCmd.SetAllStatus(ChatCmd.Status.Doing, ChatCmd.Status.Wait)
        }
    }

    override fun finish() {
        super.finish()
        getUs().nc.removeAll(this)
        AppUtils.unregisterAppStatusChangedListener(this)
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
    }


    class FragmentAdapter(activity: FragmentActivity, private val fragments: List<Fragment>) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDisconnected() {
        Log.d(MainActivity::class.java.simpleName, "onDisconnected")
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        Log.d(MainActivity::class.java.simpleName, "onConnected networkType=${networkType}")
        DCircleScope.launch {
            val job1 = async { setAppState(getAppState()) }
            val job4 = async { GetNetConfig() }
            val job5 = async {
                val article = DIDArticle.FindCurrentProcess()
                article?.apply {
                    BuildDIDArticleState(this@MainActivity, article.Address).DidEnter()
                }
            }
            val job6 = async {
                GetNonceSus(onlyDB = false)
            }
            val job8 = async {
                GetShareConfig()
            }
            listOf(job1, job4, job5, job6, job8).awaitAll()
        }
    }

    override fun onForeground(activity: Activity?) {
        Log.d(MainActivity::class.java.simpleName, "onForeground activity=${activity}")

        DCircleScope.launch {
            val job1 = async { setAppState(AppState.Foreground) }
            val job4 = async { GetNetConfig() }
            val job5 = async {
                val article = DIDArticle.FindCurrentProcess()
                article?.apply {
                    BuildDIDArticleState(this@MainActivity, article.Address).DidEnter()
                }
            }
            val job6 = async {
                GetNonceSus(onlyDB = false)
            }

            val job8 = async {
                GetShareConfig()
            }

            listOf(job1, job4, job5, job6, job8).awaitAll()
        }
    }

    override fun onBackground(activity: Activity?) {
        Log.d(MainActivity::class.java.simpleName, "onBackground activity=${activity}")

        DCircleScope.launch {
            val job1 = async {
                setAppState(AppState.Background)
            }

            listOf(job1).awaitAll()
        }
    }
}