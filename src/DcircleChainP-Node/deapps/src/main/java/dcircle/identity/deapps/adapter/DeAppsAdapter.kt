package dcircle.identity.deapps.adapter

import com.base.foundation.utils.MakeToast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dcircle.identity.deapps.R
import dcircle.identity.deapps.bean.DeApp
import dcircle.identity.deapps.databinding.ItemDeAppsBinding

class DeAppsAdapter: BaseQuickAdapter<DeApp,BaseViewHolder>(R.layout.item_de_apps) {

    override fun convert(holder: BaseViewHolder, item: DeApp) {
        val binding = ItemDeAppsBinding.bind(holder.itemView)
        binding.imgLogo.setImageResource(item.logoResId)
        binding.tvName.text = item.appName
        binding.tvDesc.text = item.appDesc
        binding.tvHopeIng.setOnClickListener {
            MakeToast.showShort("敬请期待")
        }

    }
}