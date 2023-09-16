package com.base.foundation.utils

import android.widget.Toast
import com.blankj.utilcode.util.Utils

class MakeToast {
    companion object{
        fun showShort(format: String, vararg args: Any){
            val msg = String.format(format, *args)
            Toast.makeText(Utils.getApp(), msg, Toast.LENGTH_SHORT).show()
        }

        fun showLong(format: String, vararg args: Any){
            val msg = String.format(format, *args)
            Toast.makeText(Utils.getApp(), msg, Toast.LENGTH_LONG).show()
        }

        fun showShort(msgId: Int){
            Toast.makeText(Utils.getApp(), msgId, Toast.LENGTH_SHORT).show()
        }

        fun showLong(msgId: Int){
            Toast.makeText(Utils.getApp(), msgId, Toast.LENGTH_LONG).show()
        }
    }


}