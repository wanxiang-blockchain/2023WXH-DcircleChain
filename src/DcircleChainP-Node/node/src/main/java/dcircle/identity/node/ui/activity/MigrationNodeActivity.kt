package dcircle.identity.node.ui.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.base.foundation.BaseActivity
import com.base.foundation.DCircleScope
import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import com.base.foundation.getUs
import com.base.foundation.utils.MakeToast
import com.base.thridpart.constants.Router
import com.google.gson.Gson
import dcircle.identity.node.databinding.ActivityMigrationNodeBinding
import dcircle.identity.node.ui.api.GetNodeConfigResponse
import dcircle.identity.node.ui.widget.ChooseServerPop
import kotlinx.coroutines.launch

@Route(path = Router.Node.migrationNode)
class MigrationNodeActivity:BaseActivity() {
    private lateinit var binding: ActivityMigrationNodeBinding

    override fun initPageUi() {
        binding.tvAddress.text = getUs().getUid()
        binding.ivBack.setOnClickListener { finish() }
        binding.tvHope.setOnClickListener {
            MakeToast.showShort("敬请期待")
        }
        DCircleScope.launch {
            KeyVal.FindByKey(KeyVal.Keys.NodeConfig.toString())?.apply {
                Gson().fromJson(this.Value, GetNodeConfigResponse::class.java).apply {
                    binding.tvNetworkAddress.hint = this.netWork
                }
            }
        }
        binding.tvChooseServer.setOnClickListener {
            ChooseServerPop(this,binding.tvChooseServer.text.toString()).apply {
                binding.llCreate.setOnClickListener {
                    this@MigrationNodeActivity.binding.tvChooseServer.text = binding.tvCreate.text
                    dismiss()
                }
                binding.llOneDrive.setOnClickListener {
                    this@MigrationNodeActivity.binding.tvChooseServer.text = binding.tvOneDrive.text
                    dismiss()
                }
                binding.llMicrosoft.setOnClickListener {
                    this@MigrationNodeActivity.binding.tvChooseServer.text = binding.tvMicrosoft.text
                    dismiss()
                }
                binding.llAws.setOnClickListener {
                    this@MigrationNodeActivity.binding.tvChooseServer.text = binding.tvAws.text
                    dismiss()
                }
                showPopupWindow(it)
            }
        }
    }

    override fun initViewBinding(): ViewBinding {
        binding = ActivityMigrationNodeBinding.inflate(LayoutInflater.from(this),null,false)
        return binding
    }
}