package com.github.xpwu.ktdbtable.where

abstract class logic(private val wheres: Array<Where>) : Where {

  override val BindArgs: Array<String> by lazy {
    val list = ArrayList<String>()
    for (where in wheres) {
      list.addAll(where.BindArgs)
    }
    list.toArray(emptyArray())
  }
}

class And(wheres: Array<Where>) : logic(wheres) {

  override val ArgSQL: String by lazy {
    val builder = StringBuilder()
    for (i in 0 until wheres.size - 1) {
      if (wheres[i] is Or) {
        builder.append("(")
      }
      builder.append(wheres[i].ArgSQL)
      if (wheres[i] is Or) {
        builder.append(")")
      }
      builder.append(" AND ")
    }
    if (wheres[wheres.size - 1] is Or) {
      builder.append("(")
    }
    builder.append(wheres[wheres.size - 1].ArgSQL)
    if (wheres[wheres.size - 1] is Or) {
      builder.append(")")
    }
    builder.toString()
  }
}

open class Or(wheres: Array<Where>) : logic(wheres) {

  override val ArgSQL: String by lazy {
    val builder = StringBuilder()
    for (i in 0 until wheres.size - 1) {
      if (wheres[i] is And) {
        builder.append("(")
      }
      builder.append(wheres[i].ArgSQL)
      if (wheres[i] is And) {
        builder.append(")")
      }
      builder.append(" OR ")
    }
    if (wheres[wheres.size - 1] is And) {
      builder.append("(")
    }
    builder.append(wheres[wheres.size - 1].ArgSQL)
    if (wheres[wheres.size - 1] is And) {
      builder.append(")")
    }
    builder.toString()
  }
}

class Not(where: Where) : Where by where {

  override val ArgSQL: String by lazy {
    val builder = StringBuilder()
    builder.append("NOT ")
    if (where is logic) {
      builder.append("(")
    }
    builder.append(where.ArgSQL)
    if (where is logic) {
      builder.append(")")
    }

    builder.toString()
  }
}

