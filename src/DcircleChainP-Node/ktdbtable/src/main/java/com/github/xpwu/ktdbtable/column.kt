package com.github.xpwu.ktdbtable

import com.github.xpwu.ktdbtable.where.*

open class ColumnInfo(val name: String) {
  override fun toString(): String {
    return name
  }


  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true

    if (other !is ColumnInfo) return false

    if (name != other.name) return false

    return true
  }

}

val ColumnInfo.IsNull : Where
  get() = Compare(this.name, Null.IS)

val ColumnInfo.IsNotNull : Where
  get() = Compare(this.name, Null.NOT)

class ElseColumn(name: String) : ColumnInfo(name)

class ByteArrayColumn(name: String) : ColumnInfo(name)

class LongColumn(name: String) : ColumnInfo(name)

// <
infix fun LongColumn.lt(value: Long): Where {
  return Compare(this.name, LGEOperator.LT, value)
}

// >
infix fun LongColumn.gt(value: Long): Where {
  return Compare(this.name, LGEOperator.GT, value)
}

// ==
infix fun LongColumn.eq(value: Long): Where {
  return Compare(this.name, LGEOperator.EQ, value)
}

// !=
infix fun LongColumn.neq(value: Long): Where {
  return Compare(this.name, LGEOperator.NEQ, value)
}

// <=
infix fun LongColumn.lte(value: Long): Where {
  return Compare(this.name, LGEOperator.LTE, value)
}

// >=
infix fun LongColumn.gte(value: Long): Where {
  return Compare(this.name, LGEOperator.GTE, value)
}

// in
infix fun LongColumn.`in`(values: LongArray): Where {
  return In(this.name, values)
}

// between
infix fun LongColumn.btw(rang: Pair<Long, Long>): Where {
  return Between(this.name, rang.first, rang.second)
}

class IntColumn(name: String) : ColumnInfo(name)

// <
infix fun IntColumn.lt(value: Int): Where {
  return Compare(this.name, LGEOperator.LT, value)
}

// >
infix fun IntColumn.gt(value: Int): Where {
  return Compare(this.name, LGEOperator.GT, value)
}

// ==
infix fun IntColumn.eq(value: Int): Where {
  return Compare(this.name, LGEOperator.EQ, value)
}

// !=
infix fun IntColumn.neq(value: Int): Where {
  return Compare(this.name, LGEOperator.NEQ, value)
}

// <=
infix fun IntColumn.lte(value: Int): Where {
  return Compare(this.name, LGEOperator.LTE, value)
}

// >=
infix fun IntColumn.gte(value: Int): Where {
  return Compare(this.name, LGEOperator.GTE, value)
}

// in
infix fun IntColumn.`in`(values: IntArray): Where {
  return In(this.name, values)
}

// between
infix fun IntColumn.btw(rang: Pair<Int, Int>): Where {
  return Between(this.name, rang.first, rang.second)
}

class ShortColumn(name: String) : ColumnInfo(name)

// <
infix fun ShortColumn.lt(value: Short): Where {
  return Compare(this.name, LGEOperator.LT, value)
}

// >
infix fun ShortColumn.gt(value: Short): Where {
  return Compare(this.name, LGEOperator.GT, value)
}

// ==
infix fun ShortColumn.eq(value: Short): Where {
  return Compare(this.name, LGEOperator.EQ, value)
}

// !=
infix fun ShortColumn.neq(value: Short): Where {
  return Compare(this.name, LGEOperator.NEQ, value)
}

// <=
infix fun ShortColumn.lte(value: Short): Where {
  return Compare(this.name, LGEOperator.LTE, value)
}

// >=
infix fun ShortColumn.gte(value: Short): Where {
  return Compare(this.name, LGEOperator.GTE, value)
}

// in
infix fun ShortColumn.`in`(values: ShortArray): Where {
  return In(this.name, values)
}

// between
infix fun ShortColumn.btw(rang: Pair<Short, Short>): Where {
  return Between(this.name, rang.first, rang.second)
}

class ByteColumn(name: String) : ColumnInfo(name)

// <
infix fun ByteColumn.lt(value: Byte): Where {
  return Compare(this.name, LGEOperator.LT, value)
}

// >
infix fun ByteColumn.gt(value: Byte): Where {
  return Compare(this.name, LGEOperator.GT, value)
}

// ==
infix fun ByteColumn.eq(value: Byte): Where {
  return Compare(this.name, LGEOperator.EQ, value)
}

// !=
infix fun ByteColumn.neq(value: Byte): Where {
  return Compare(this.name, LGEOperator.NEQ, value)
}

// <=
infix fun ByteColumn.lte(value: Byte): Where {
  return Compare(this.name, LGEOperator.LTE, value)
}

// >=
infix fun ByteColumn.gte(value: Byte): Where {
  return Compare(this.name, LGEOperator.GTE, value)
}

// in
infix fun ByteColumn.`in`(values: ByteArray): Where {
  return In(this.name, values)
}

// between
infix fun ByteColumn.btw(rang: Pair<Byte, Byte>): Where {
  return Between(this.name, rang.first, rang.second)
}

class BooleanColumn(name: String) : ColumnInfo(name)

// ==
infix fun BooleanColumn.eq(value: Boolean): Where {
  return Compare(this.name, value)
}


class FloatColumn(name: String) : ColumnInfo(name)

// <
infix fun FloatColumn.lt(value: Float): Where {
  return Compare(this.name, LGOperator.LT, value)
}

// >
infix fun FloatColumn.gt(value: Float): Where {
  return Compare(this.name, LGOperator.GT, value)
}

// between
infix fun FloatColumn.btw(rang: Pair<Float, Float>): Where {
  return Between(this.name, rang.first, rang.second)
}

class DoubleColumn(name: String) : ColumnInfo(name)

// <
infix fun DoubleColumn.lt(value: Double): Where {
  return Compare(this.name, LGOperator.LT, value)
}

// >
infix fun DoubleColumn.gt(value: Double): Where {
  return Compare(this.name, LGOperator.GT, value)
}

// between
infix fun DoubleColumn.btw(rang: Pair<Double, Double>): Where {
  return Between(this.name, rang.first, rang.second)
}

class StringColumn(name: String) : ColumnInfo(name)

// ==
infix fun StringColumn.eq(value: String): Where {
  return Compare(this.name, EOperator.EQ, value)
}

// !=
infix fun StringColumn.neq(value: String): Where {
  return Compare(this.name, EOperator.NEQ, value)
}

// in
infix fun StringColumn.`in`(values: Array<String>): Where {
  return In(this.name, values)
}

infix fun StringColumn.fuzzy(value: String): Where {
  return Like(this.name, "%$value%")
}

// value be not changed
infix fun StringColumn.like(value: String): Where {
  return Like(this.name, value)
}
