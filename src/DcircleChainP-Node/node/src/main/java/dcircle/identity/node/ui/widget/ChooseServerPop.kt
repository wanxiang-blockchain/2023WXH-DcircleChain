package dcircle.identity.node.ui.widget

import android.content.Context
import android.view.View
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import dcircle.identity.node.R
import dcircle.identity.node.databinding.PopChooseServerBinding
import razerdp.basepopup.BasePopupWindow

class ChooseServerPop(context: Context, private val chooseServer :String) : BasePopupWindow(context){
    lateinit var binding: PopChooseServerBinding

    init {
        contentView = createPopupById(R.layout.pop_choose_server)
        setBackgroundColor(ColorUtils.getColor(R.color.transparent))

        width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70f)
    }
    override fun createPopupById(layoutId: Int): View {
        binding = PopChooseServerBinding.bind(View.inflate(context,layoutId,null))
        binding.imgCreate.setVisible(binding.tvCreate.text == chooseServer)
        binding.imgOneDrive.setVisible(binding.tvOneDrive.text == chooseServer)
        binding.imgMicrosoft.setVisible(binding.tvMicrosoft.text == chooseServer)
        binding.imgAws.setVisible(binding.tvAws.text == chooseServer)
        return binding.root
    }
}