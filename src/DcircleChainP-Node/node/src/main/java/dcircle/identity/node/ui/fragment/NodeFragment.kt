package dcircle.identity.node.ui.fragment

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.base.foundation.BaseFragment
import com.base.foundation.DCircleScope
import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import com.base.foundation.getUs
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.google.gson.Gson
import dcircle.identity.node.databinding.FragmentNodeBinding
import dcircle.identity.node.ui.api.GetNodeConfig
import dcircle.identity.node.ui.api.GetNodeConfigResponse
import kotlinx.coroutines.launch

class NodeFragment : BaseFragment(){
    private lateinit var binding: FragmentNodeBinding
    override fun initViewBinding(): ViewBinding {
        binding = FragmentNodeBinding.inflate(LayoutInflater.from(context),null,false)
        initPageView()
        return binding
    }

    override fun initPageView() {

        binding.tvAddress.text = getUs().getUid()

        binding.fBtn.setOnClickDelay {
            ARouter.getInstance().build(Router.Node.migrationNode)
                .navigation()
        }

        getUs().nc.addObserver(this, KeyVal.ChangedEvent(KeyVal.Keys.NodeConfig.toString())) {
            DCircleScope.launch {
                loadFromDB()
            }
        }

        DCircleScope.launch {
            loadFromDB()
            loadFromServer()
        }
    }

    private fun loadFromDB() {
        DCircleScope.launch {
            KeyVal.FindByKey(KeyVal.Keys.NodeConfig.toString())?.apply {
                Gson().fromJson(this.Value, GetNodeConfigResponse::class.java).apply {
                    binding.tvAddress.text = getUs().getUid()
                    binding.tvNetworkAddress.text = this.netWork
                    binding.tvService.text = this.serviceProvider
                    binding.tvCpu.text = this.cpu
                    binding.tvMemory.text = this.memory
                    binding.tvStorage.text = this.storage
                }
            }
        }
    }

    private suspend fun loadFromServer() {
        GetNodeConfig()
    }
}