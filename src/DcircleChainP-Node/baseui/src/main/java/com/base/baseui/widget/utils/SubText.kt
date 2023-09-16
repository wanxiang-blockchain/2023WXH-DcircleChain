package com.base.baseui.widget.utils

import java.lang.Integer.max

class SubText {
    companion object{

        fun shortenString(str:String): String {
            return shortenString(str,8)
        }
        private fun shortenString(str: String, count:Int): String {
            if (str.length <= count) {
                return str
            }
            return str.substring(0,count)+ "..." + str.substring(max(0,str.length-count) until str.length)
        }


    }

}