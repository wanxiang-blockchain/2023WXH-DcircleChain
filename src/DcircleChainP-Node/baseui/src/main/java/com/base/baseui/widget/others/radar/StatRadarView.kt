package com.base.baseui.widget.others.radar

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import com.base.baseui.R
import com.base.baseui.databinding.LayoutStasticsRadarViewBinding
import com.base.baseui.widget.others.radar.util.getLinearGradientColor
import com.base.foundation.DCircleScope
import com.base.foundation.db.DIDArticleStat
import com.base.foundation.db.FindLatestByArticle
import com.base.foundation.db.FindLatestByRole
import com.blankj.utilcode.util.SizeUtils
import kotlinx.coroutines.launch


class StatRadarView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {
    val binding = LayoutStasticsRadarViewBinding.inflate(LayoutInflater.from(context), this, true)


    private val COLOR_LEVEL_ONE = Color.parseColor("#86e64f")
    private val COLOR_LEVEL_TWO = Color.parseColor("#54ead6")
    private val COLOR_LEVEL_THREE = Color.parseColor("#3a86ff")
    private val COLOR_LEVEL_FOUL = Color.parseColor("#8f4bee")
    private val COLOR_LEVEL_FIVE = Color.parseColor("#ff9a00")
    private val COLOR_LEVEL_SIX = Color.parseColor("#e92727")


    init {
         val values: Array<Float> = arrayOf(0f, 0f, 0f, 0f, 0f, 0f)
         val data = RadarData(values.toMutableList())
         data.isValueTextEnable = true
         data.vauleTextColor = Color.BLACK
         data.valueTextSize = SizeUtils.dp2px(0f).toFloat()
         data.lineWidth = SizeUtils.dp2px(1f).toFloat()
         binding.radarView.addData(data)
    }


    fun setRadar(type: RadarType, id: String, isShowValue: Boolean = true) {
        if(!isShowValue){
            binding.llData1.isInvisible = true
            binding.clData2.isInvisible = true
            binding.clData3.isInvisible = true
            binding.llData4.isInvisible = true
            val params = binding.radarView.layoutParams
            params.width = SizeUtils.dp2px(350f)
            params.height = SizeUtils.dp2px(350f)
            binding.radarView.layoutParams = params
            binding.radarView.hideMiddle()
        }
        when (type) {
            RadarType.User -> {
                setUserData(id)
            }

            RadarType.Article -> {
                setDidData(id)
            }
        }
    }

    private fun setUserData(id: String) {
        DCircleScope.launch {
            val userStats = DIDArticleStat.FindLatestByRole(
                id, arrayOf(
                    DIDArticleStat.StatRoleType.CreatorStat,
                    DIDArticleStat.StatRoleType.GroupStat,
                    DIDArticleStat.StatRoleType.TransferStat,
                    DIDArticleStat.StatRoleType.ConsumerStat,
                    DIDArticleStat.StatRoleType.InviteJoinGroupStat
                )
            )
            val didCreate =
                userStats.find { it.RoleStatType == DIDArticleStat.StatRoleType.CreatorStat.value }
            binding.tvData2.text = (didCreate?.ConsumptionTimes ?: 0).toString()
            didCreate?.ConsumptionTimes?.let {
                if(it>0){
                    binding.ivPic2.setColorFilter(getImageColor(it))
                    binding.cvPic2.setCardBackgroundColor(getImageColor(it))
                }
            }

            val didGroup =
                userStats.find { it.RoleStatType == DIDArticleStat.StatRoleType.GroupStat.value }
            binding.tvData1.text = (didGroup?.ConsumptionTimes ?: 0).toString()
            didGroup?.ConsumptionTimes?.let {
                if(it>0){
                    binding.ivPic1.setColorFilter(getImageColor(it))
                    binding.cvPic1.setCardBackgroundColor(getImageColor(it))
                }
            }

            val didTransfer =
                userStats.find { it.RoleStatType == DIDArticleStat.StatRoleType.TransferStat.value }
            val invite = userStats.find { it.RoleStatType == DIDArticleStat.StatRoleType.InviteJoinGroupStat.value }
            val effectiveCount = (didTransfer?.ConsumptionTimes ?: 0) + (invite?.JoinTimes ?: 0)
            binding.tvData3.text = effectiveCount.toString()
            effectiveCount.let {
                if(it>0){
                    binding.ivPic3.setColorFilter(getImageColor(it))
                    binding.cvPic3.setCardBackgroundColor(getImageColor(it))
                }
            }

            val didConsume =
                userStats.find { it.RoleStatType == DIDArticleStat.StatRoleType.ConsumerStat.value }
            binding.tvData4.text = (didConsume?.ContentNums ?: 0).toString()
            didConsume?.ContentNums?.let {
                if(it>0){
                    binding.ivPic4.setColorFilter(getImageColor(it))
                    binding.cvPic4.setCardBackgroundColor(getImageColor(it))
                }
            }

            val values: Array<Float> = arrayOf(
                (didConsume?.ContentNums ?: 0).toFloat(),
                0f,
                0f,
                (didGroup?.ConsumptionTimes ?: 0).toFloat(),
                (didCreate?.ConsumptionTimes ?: 0).toFloat(),
                effectiveCount.toFloat()
            )
            setRadarValue(values.toMutableList())
            binding.tvRadarName1.text =  context.getString(R.string.did_data_organization)
            binding.tvRadarName2.text = context.getString(R.string.did_data_creativity)
            binding.tvRadarName3.text =  context.getString(R.string.did_data_communication_power)
            binding.tvRadarName4.text =  context.getString(R.string.did_data_consumption_power)
        }
    }

    private fun setDidData(id: String) {
        DCircleScope.launch {
            val didStat = DIDArticleStat.FindLatestByArticle(id)
            binding.tvData1.text = (didStat?.ConsumerNums?:0).toString()
            didStat?.ConsumerNums?.let {
                if(it>0){
                    binding.ivPic1.setColorFilter(getImageColor(it))
                    binding.cvPic1.setCardBackgroundColor(getImageColor(it))
                }
            }
            binding.tvData2.text = (didStat?.ConsumptionTimes?:0).toString()
            didStat?.ConsumptionTimes?.let {
                if(it>0){
                    binding.ivPic2.setColorFilter(getImageColor(it))
                    binding.cvPic2.setCardBackgroundColor(getImageColor(it))
                }
            }
            binding.tvData3.text = (didStat?.GroupNums?:0).toString()
            didStat?.GroupNums?.let {
                if(it>0){
                    binding.ivPic3.setColorFilter(getImageColor(it))
                    binding.cvPic3.setCardBackgroundColor(getImageColor(it))
                }
            }
            binding.tvData4.text = (didStat?.ReachNums?:0).toString()
            didStat?.ReachNums?.let {
                if(it>0){
                    binding.ivPic4.setColorFilter(getImageColor(it))
                    binding.cvPic4.setCardBackgroundColor(getImageColor(it))
                }
            }
            val values:Array<Float> = arrayOf((didStat?.ReachNums?:0).toFloat(),0f,0f,
                (didStat?.ConsumerNums?:0).toFloat(),(didStat?.ConsumptionTimes?:0).toFloat(),
                (didStat?.GroupNums?:0).toFloat())
            setRadarValue(values.toMutableList())
            binding.ivPic1.setImageResource(R.mipmap.ic_did_radar_1)
            binding.ivPic2.setImageResource(R.mipmap.ic_did_radar_2)
            binding.ivPic3.setImageResource(R.mipmap.ic_user_did_1)
            binding.ivPic4.setImageResource(R.mipmap.ic_did_user_3)
            binding.tvRadarName1.text = context.getString(R.string.message_consumers)
            binding.tvRadarName2.text = context.getString(R.string.consumption_times)
            binding.tvRadarName3.text = context.getString(R.string.communication_group)
            binding.tvRadarName4.text = context.getString(R.string.disseminator)

        }
    }


    private var mData = listOf<Int>()
    private fun setRadarValue(valueList: List<Float>) {
        val newIntData = valueList.map { it.toInt() }
        if(mData == newIntData){
            return
        }
        binding.radarView.clearRadarData()
        val data = RadarData(valueList)
        binding.radarView.addData(data)
        mData = newIntData
        binding.radarView.invalidate()
        binding.radarView.postInvalidate()
    }

    private fun getImageColor(data:Int):Int{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           return getLinearGradientColor(data.toFloat())
        }
        return  when(data){
            -1->0x00404040
            in 1..1 -> COLOR_LEVEL_ONE

            in 2..10 -> COLOR_LEVEL_TWO

            in 11..50 -> COLOR_LEVEL_THREE

            in 51..200 -> COLOR_LEVEL_FOUL

            in 201..500 -> COLOR_LEVEL_FIVE

            else -> COLOR_LEVEL_SIX
        }
    }
}