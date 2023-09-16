package com.github.xpwu.ktdbtable_processor

fun String.align(indent: String): String {
  val ret = this.prependIndent(indent)
  // cut first indent
  return ret.substring(indent.length)
}