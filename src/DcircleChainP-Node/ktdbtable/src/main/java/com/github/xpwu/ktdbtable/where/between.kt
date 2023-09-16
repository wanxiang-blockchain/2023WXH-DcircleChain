package com.github.xpwu.ktdbtable.where

class Between private constructor(field: String, left: Any, right: Any) : Where {

  constructor (field: String, left: Long, right: Long) : this(field, left as Any, right as Any)
  constructor (field: String, left: Byte, right: Byte) : this(field, left as Any, right as Any)
  constructor (field: String, left: Int, right: Int) : this(field, left as Any, right as Any)
  constructor (field: String, left: Short, right: Short) : this(field, left as Any, right as Any)
  constructor (field: String, left: Double, right: Double) : this(field, left as Any, right as Any)
  constructor (field: String, left: Float, right: Float) : this(field, left as Any, right as Any)

  override val ArgSQL: String = "$field BETWEEN $left AND $right"

  private var bindArgs = emptyArray<String>()

  override val BindArgs: Array<String>
    get() = bindArgs
}