package dcircle.identity.deapps.ui.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.base.foundation.BaseActivity
import com.base.foundation.DCircleScope
import com.base.thridpart.constants.Router
import dcircle.identity.deapps.databinding.ActivityChatAuthBinding
import kotlinx.coroutines.launch

@Route(path = Router.DeApps.chatauth)
class ChatAuthActivity : BaseActivity() {
    private lateinit var binding:ActivityChatAuthBinding

    override fun initPageUi() {
        binding.tvJoinGroup.setOnClickListener {
            DCircleScope.launch {
                ARouter.getInstance().build(Router.DeApps.authorization).navigation()
            }
        }
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun initViewBinding(): ViewBinding {
        binding = ActivityChatAuthBinding.inflate(LayoutInflater.from(this),null,false)
        return binding
    }
}