package com.github.xpwu.ktdbtable.where

class In private constructor( field: String, argSqlValue: String) : Where {
  constructor (field: String, values: LongArray) : this(field, values.joinToString(",", "(", ")"))
  constructor (field: String, values: ShortArray) : this(field, values.joinToString(",", "(", ")"))
  constructor (field: String, values: ByteArray) : this(field, values.joinToString(",", "(", ")"))
  constructor (field: String, values: IntArray) : this(field, values.joinToString(",", "(", ")"))
  constructor (field: String, values: BooleanArray) : this(field, values.joinToString(",", "(", ")") { if (it) "1" else "0"})

  constructor(field: String, values: Array<String>) : this(field, values.joinToString(",", "(", ")") { "?" }) {
    bindArgs = values
  }

  private var bindArgs = emptyArray<String>()

  override val ArgSQL: String = "$field IN $argSqlValue"

  override val BindArgs: Array<String>
    get() = bindArgs

}