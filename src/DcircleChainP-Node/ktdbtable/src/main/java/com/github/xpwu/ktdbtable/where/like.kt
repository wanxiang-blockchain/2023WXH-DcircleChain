package com.github.xpwu.ktdbtable.where

open class Like(field: String, pattern: String) : Where {

  override val ArgSQL: String = "$field LIKE ? "

  override val BindArgs: Array<String> = arrayOf(pattern)
}

// 模糊搜索
class Fuzzy(field: String, string: String) : Like(field, "%$string%")
