package com.github.xpwu.ktdbtable

import android.util.Log as SysLog

class Log{
  companion object
}

fun Log.Companion.W(format: String, vararg args: Any?) {
  SysLog.w("ktdbtable", String.format(format, args))
}

fun Log.Companion.E(format: String, vararg args: Any?) {
  SysLog.e("ktdbtable", String.format(format, args))
}

fun Log.Companion.I(format: String, vararg args: Any?) {
  SysLog.i("ktdbtable", String.format(format, args))
}
