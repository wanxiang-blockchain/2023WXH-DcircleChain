package com.github.xpwu.ktdbtable.where

private interface op {
  val value: String
}

enum class LGOperator(override val value: String) : op {
  GT(">"),
  LT("<"),
}

enum class LGEOperator(override val value: String) : op {
  GT(">"),
  LT("<"),
  EQ("="),
  NEQ("!="),
  GTE(">="),
  LTE("<="),
}

enum class EOperator(override val value: String) : op {
  EQ("="),
  NEQ("!="),
}

enum class Null(override val value: String): op {
  IS(" IS NULL"),
  NOT(" IS NOT NULL")
}

class Compare private constructor(field: String, op: op, value: Any) : Where {

  constructor (field: String, op: LGEOperator, value: Long) : this(field, op, value as Any)
  constructor (field: String, op: LGEOperator, value: Byte) : this(field, op, value.toLong())
  constructor (field: String, op: LGEOperator, value: Int) : this(field, op, value.toLong())
  constructor (field: String, op: LGEOperator, value: Short) : this(field, op, value.toLong())

  constructor (field: String, op: LGOperator, value: Double) : this(field, op, value as Any)
  constructor (field: String, op: LGOperator, value: Float) : this(field, op, value.toDouble())

  // true => '!=0'; false => '=0'
  constructor (field: String, value: Boolean) : this(field, if (value) EOperator.NEQ else EOperator.EQ, 0)

  // null or not null
  constructor (field: String, op: Null) : this(field, op, "" as Any)

  constructor(field: String, op: EOperator, value: String) : this(field, op, "?" as Any) {
    bindArgs = arrayOf(value)
  }

  private var bindArgs = emptyArray<String>()

  override val ArgSQL: String = field + op.value + value.toString()

  override val BindArgs: Array<String>
    get() = bindArgs
}