package dcircle.identity.deapps.ui.fragment

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.base.foundation.BaseFragment
import com.base.thridpart.constants.Router
import dcircle.identity.deapps.R
import dcircle.identity.deapps.adapter.DeAppsAdapter
import dcircle.identity.deapps.bean.DeApp
import dcircle.identity.deapps.databinding.FragmentDeAppsBinding

class DeAppsFragment : BaseFragment(){
    private lateinit var binding: FragmentDeAppsBinding
    private lateinit var adapter: DeAppsAdapter
    override fun initViewBinding(): ViewBinding {
        binding = FragmentDeAppsBinding.inflate(LayoutInflater.from(context),null,false)
        initPageView()
        return binding
    }

    override fun initPageView() {
        adapter = DeAppsAdapter()
        binding.deAppsRv.layoutManager = LinearLayoutManager(context)
        binding.deAppsRv.adapter = adapter

        val web3Chat = DeApp(R.mipmap.ic_debot,"DeBot","属于你的智能节点私人管家")
        val web3Game = DeApp(R.mipmap.ic_game,"GameVerse","集成区块链技术的游戏平台")
        val video = DeApp(R.mipmap.ic_view_block,"ViewBlock","保护并分享你创造的视频")
        val information = DeApp(R.mipmap.ic_info,"InfoLink","线上新闻信息分发平台")
        adapter.setList(listOf(web3Chat,web3Game,video,information))
        binding.tvAuthorizedAccess.setOnClickListener {
            ARouter.getInstance().build(Router.DeApps.authorization).navigation()
        }
    }

}