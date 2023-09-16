package com.github.xpwu.ktdbtable_processor

import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class TableInfo(
  val Name: String,
  val Version: Int,
  val Type: TypeElement,
  val Columns: ArrayList<ColumnInfo> = ArrayList(),
  // default Migrators() and Initializer()
  var MigInit: Pair<String, String> = Pair("", ""),
  var ValFieldName: String = ""
)

enum class Type {
  TEXT,
  INTEGER,
  REAL,
  BLOB,
}

fun printTypeError(errorType: String): String {
  return """type <${errorType}> error! Only support: 
    Byte, Short, Int, Long   => INTEGER
    Boolean => INTEGER ( true -> 1 ; false -> 0 )
    String => TEXT
    Float, Double => REAL
    ByteArray  => BLOB
    """
}

/**
 *
 * Byte, Short, Int, Long  => INTEGER
 * Boolean => INTEGER ( true -> 1 ; false -> 0 )
 * String => TEXT
 * Float, Double => REAL
 * ByteArray => BLOB
 *
 */

val entity2column = mapOf<String, Type>(
  Int::class.java.canonicalName to Type.INTEGER,
  "java.lang.Integer" to Type.INTEGER,
//  UInt::class.java.canonicalName to Type.INTEGER,
  Short::class.java.canonicalName to Type.INTEGER,
  "java.lang.Short" to Type.INTEGER,
//  UShort::class.java.canonicalName to Type.INTEGER,
  Byte::class.java.canonicalName to Type.INTEGER,
  "java.lang.Byte" to Type.INTEGER,
//  UByte::class.java.canonicalName to Type.INTEGER,
  Long::class.java.canonicalName to Type.INTEGER,
  "java.lang.Long" to Type.INTEGER,
//  ULong::class.java.canonicalName to Type.INTEGER,
  Boolean::class.java.canonicalName to Type.INTEGER,
  "java.lang.Boolean" to Type.INTEGER,
  Float::class.java.canonicalName to Type.REAL,
  "java.lang.Float" to Type.REAL,
  Double::class.java.canonicalName to Type.REAL,
  "java.lang.Double" to Type.REAL,
  String::class.java.canonicalName to Type.TEXT,
  ByteArray::class.java.canonicalName to Type.BLOB,
//  UByteArray::class.java.canonicalName to Type.BLOB,
  // content values 暂不支持，转换留给调用方
//  Array<Byte>::class.java.canonicalName to Type.BLOB,
//  Array<UByte>::class.java.canonicalName to Type.BLOB,
)

typealias FunName = String
val fromByteArray = emptyMap<String, FunName>().toMutableMap()
val toByteArray = emptyMap<String, FunName>().toMutableMap()

val getFun = mapOf<String, (String)->String >(
  Int::class.java.canonicalName to {i -> "getInt($i)"},
  "java.lang.Integer" to {i -> "getInt($i)"},
  Short::class.java.canonicalName to {i -> "getShort($i)"},
  "java.lang.Short" to {i -> "getShort($i)"},
  Byte::class.java.canonicalName to {i -> "getShort($i).toByte()"},
  "java.lang.Byte" to {i -> "getShort($i).toByte()"},
  Long::class.java.canonicalName to {i -> "getLong($i)"},
  "java.lang.Long" to {i -> "getLong($i)"},
  Boolean::class.java.canonicalName to {i -> "getInt($i) != 0"},
  "java.lang.Boolean" to {i -> "getInt($i) != 0"},
  Float::class.java.canonicalName to {i -> "getFloat($i)"},
  "java.lang.Float" to {i -> "getFloat($i)"},
  Double::class.java.canonicalName to {i -> "getDouble($i)"},
  "java.lang.Double" to {i -> "getDouble($i)"},
  String::class.java.canonicalName to {i -> "getString($i)"},
  ByteArray::class.java.canonicalName to {i -> "getBlob($i)"},
)

class ColumnInfo(
  val Typ: Type,
  val FieldName: String,
  val ColumnAnno: Column,
  val NotNull: Boolean,
  val IndexAnnotations: Array<Index>,
  val DataType: TypeMirror,
  val NotPrimaryType: Boolean,
)

val columnTypePre = mapOf<String, String>(
  Boolean::class.java.canonicalName to "Boolean",
  "java.lang.Boolean" to "Boolean",
  Long::class.java.canonicalName to "Long",
  "java.lang.Long" to "Long",
  Int::class.java.canonicalName to "Int",
  "java.lang.Integer" to "Int",
  Short::class.java.canonicalName to "Short",
  "java.lang.Short" to "Short",
  Byte::class.java.canonicalName to "Byte",
  "java.lang.Byte" to "Byte",
  Float::class.java.canonicalName to "Float",
  "java.lang.Float" to "Float",
  Double::class.java.canonicalName to "Double",
  "java.lang.Double" to "Double",
  String::class.java.canonicalName to "String",
  ByteArray::class.java.canonicalName to "ByteArray",
)

fun ColumnInfo.outField(tableClass: String): String {
  return """
    val ${tableClass}.Companion.${this.FieldName}
      get() = ${columnTypePre[this.DataType.toString()]?:"Else"}Column("${this.ColumnAnno.name}")
  """.trimIndent()
}

fun TableInfo.allColumnsFun(): String {
  val tableClass = this.Type.simpleName.toString()

  val filedBuilder = StringBuilder()
  for (c in this.Columns) {
    filedBuilder.append(c.FieldName).append(", ")
  }

  return """
    fun ${tableClass}.Companion.AllColumns(): List<ColumnInfo> {
      return listOf(
        ${filedBuilder.toString().align("        ")}
      )
    }
  """.trimIndent()
}

fun TableInfo.toContentValuesFun(): String {
  val tableClass = this.Type.simpleName.toString()

  val builder = StringBuilder()
  for (c in this.Columns) {
    val f = toByteArray[c.DataType.toString()]?:""
    builder.append("""
      ${tableClass}.${c.FieldName}.toString() -> cv.put(column.toString(), ${if (c.NotPrimaryType) "$f(" else ""}this.${c.FieldName}${if (c.NotPrimaryType) ")" else ""})
    """.trimIndent()).append("\n")
  }

  return """
    fun ${tableClass}.ToContentValues(columns: List<ColumnInfo> = ${tableClass}.AllColumns()): ContentValues {
      val cv = ContentValues(columns.size)
      for (column in columns) {
        when(column.toString()) {
          ${builder.toString().align("          ")}
          else -> {
            throw IllegalArgumentException("Illegal column ${'$'}column for $tableClass")
          }
        }
      }

      return cv
    }
  """.trimIndent()
}

fun TableInfo.hasClass(): String {
  val tableClass = this.Type.simpleName.toString()
  val builder = StringBuilder()
  for (c in this.Columns) {
    builder.append("""
      val ${c.FieldName}: Boolean,
    """.trimIndent()).append("\n")
  }

  return """
    data class ${tableClass}Has(
      ${builder.toString().align("      ")} 
    )
  """.trimIndent()
}

fun TableInfo.cursorToFun(): String {
  val tableClass = this.Type.simpleName.toString()

  if (this.ValFieldName.isNotEmpty()) {
    return """
      // NOT implement 'fun Cursor.To${tableClass}(out: ${tableClass}): ${tableClass}Has'.
      // Because its ${this.ValFieldName} is 'val' field
    """.trimIndent()
  }

  val getBuilder = StringBuilder()
  for (i in 0 until  this.Columns.size) {
    val c = this.Columns[i]
    val fieldName = c.FieldName
    var funName = (getFun[c.DataType.toString()]?:{""})("i")
    if (c.NotPrimaryType) {
      funName = "getBlob(i)"
    }

    val f = fromByteArray[c.DataType.toString()]?:""

    getBuilder.append("""
      ${tableClass}.${fieldName}.toString() -> { out.${fieldName} =  ${if (c.NotPrimaryType) "$f(" else ""}this.${funName}${if (c.NotPrimaryType) ")" else ""}; has[${i}] = true }
    """.trimIndent()).append("\n")
  }

  val hasBuilder = StringBuilder()
  for (i in 0 until  this.Columns.size) {
    hasBuilder.append("""
      has[$i], 
    """.trimIndent())
  }

  return """
    fun Cursor.To${tableClass}(out: ${tableClass}): ${tableClass}Has {
      val has = BooleanArray(${this.Columns.size})
      for (i in 0 until this.columnCount) {
        if (this.isNull(i)) continue
        when(this.getColumnName(i)) {
          ${getBuilder.toString().align("          ")}
          else -> {
            throw IllegalArgumentException("Illegal column ${'$'}{this.getColumnName(i)} for User")
          }
        }
      }

      return ${tableClass}Has(
        $hasBuilder
      )
    }
  """.trimIndent()
}

fun isAutoincrement(key: PrimaryKey): Boolean {
  return key == PrimaryKey.ONLY_ONE_AUTO_INC || key == PrimaryKey.ONLY_ONE_AUTO_INC_DESC
}

fun primaryKey(key: PrimaryKey): String {
  if (key == PrimaryKey.MULTI || key == PrimaryKey.MULTI_DESC) {
    return ""
  }

  return key.result
}

fun ColumnInfo.constraint(errLog: (String) -> Unit): String {
  // AUTOINCREMENT 必须是 INTEGER except Boolean
  if (isAutoincrement(this.ColumnAnno.primaryKey)
    && this.DataType.kind != TypeKind.LONG && this.DataType.kind != TypeKind.INT
    && this.DataType.kind != TypeKind.SHORT && this.DataType.kind != TypeKind.BYTE) {

    errLog("${this.FieldName} is AUTOINCREMENT, but whose type is not Long, Int, Short or Byte")
    return ""
  }

  val primaryKey = primaryKey(this.ColumnAnno.primaryKey)
  // 如果是主键，但是注解的notNull 不为true，强制设定注解的not null 为true
  val annoNotNull = this.ColumnAnno.notNull || primaryKey.isNotEmpty()

  val notNull = if (this.NotNull && annoNotNull) "NOT NULL" else ""

  if (primaryKey.isNotEmpty() && notNull.isEmpty()) {
    errLog("${this.FieldName} is PRIMARY KEY, but it is not NOT NULL")
    return ""
  }
  val default = if (this.ColumnAnno.defaultValue.isNotEmpty()) "DEFAULT ${this.ColumnAnno.defaultValue}" else ""
  return "`${this.ColumnAnno.name}` ${this.Typ} $primaryKey $notNull $default"
}

// "ALTER TABLE table_name ADD COLUMN ..."
typealias AlterSQL = String

fun ColumnInfo.alter(table: String, errLog: (String) -> Unit): AlterSQL {
  return "ALTER TABLE $table ADD COLUMN ${this.constraint(errLog)}"
}

// name => ("UNIQUE", column, seq)
fun ColumnInfo.index(table: String): Map<String, Triple<String, String, Int>> {
  val ret = emptyMap<String, Triple<String, String, Int>>().toMutableMap()
  for (indexA in this.IndexAnnotations) {
    val name = table + "_" + indexA.name.ifEmpty { this.ColumnAnno.name }
    val unique = if (indexA.unique) "UNIQUE" else ""
    val desc = if (indexA.desc) "DESC" else ""
    val column = "`${this.ColumnAnno.name}` $desc"
    ret[name] = Triple(unique, column, indexA.sequence)
  }

  return ret
}

typealias IndexName = String
// "CREATE INDEX IF NOT EXISTS ..."
typealias IndexSQL = String

// [("UNIQUE", column, seq)] to "(column1, column2, column3, ...)"
fun ArrayList<Triple<String, String, Int>>.toString(errLog: (String) -> Unit): String {
  if (this.isEmpty()) return ""

  val set: MutableSet<Int> = emptySet<Int>().toMutableSet()
  for ((_, _, seq) in this) {
    if (set.contains(seq)) {
      errLog("the sequence($seq) of combined_index are too much")
      return ""
    }
    set.add(seq)
  }

  this.sortWith { triple: Triple<String, String, Int>, triple2: Triple<String, String, Int> ->
    return@sortWith triple.third - triple2.third
  }

  val (unique, column, _) = this[0]
  val builder = StringBuilder()
  builder.append("(")
  builder.append(column)
  for (i in 1 until this.size) {
    val (uniquei, columni, _) = this[i]
    if (unique != uniquei) {
      errLog("unique flags are not the same with each other")
      return ""
    }

    builder.append(", ")
    builder.append(columni)
  }
  builder.append(")")

  return builder.toString()
}

// name =>  CREATE $UNIQUE INDEX IF NOT EXISTS ...
fun TableInfo.index(errLog: (String) -> Unit): Map<IndexName, IndexSQL> {
  val ret = emptyMap<IndexName, IndexSQL>().toMutableMap()
  val all = emptyMap<IndexName, ArrayList<Triple<String, String, Int>>>().toMutableMap()

  for (column in this.Columns) {
    // name => ("UNIQUE", column, seq)
    val col = column.index(this.Name)
    for ((key, value) in col) {
      var item = all[key]
      if (item == null) {
        item = ArrayList()
        all[key] = item
      }
      item.add(value)
    }
  }

  for ((indexName, value) in all) {
    if (value.isEmpty()) continue
    val con = value.toString { str -> errLog("$indexName: $str") }
    ret[indexName] = "CREATE ${value[0].first} INDEX IF NOT EXISTS `$indexName` ON `${this.Name}` $con"
  }

  return ret
}

typealias ColumnName = String

fun TableInfo.alterColumns(logger: Logger): Map<ColumnName, AlterSQL> {
  val ret = emptyMap<ColumnName, AlterSQL>().toMutableMap()
  for (col in this.Columns) {
    ret[col.ColumnAnno.name] =
      col.alter(this.Name) { str -> logger.error(this.Type, "${this.Name}: $str") }
  }
  return ret
}

typealias TablePrimaryConstraint = String

// CREATE TABLE IF NOT EXISTS
fun TableInfo.sqlForCreating(logger: Logger): String {
  val mulPrimaryKey = ArrayList<Pair<TablePrimaryConstraint, Int>>()

  val builder = StringBuilder()
  builder.append("\"CREATE TABLE IF NOT EXISTS `${this.Name}`(")
  if (this.Columns.size == 0) {
    return ""
  }

  var lastPrimaryKey = this.Columns[0].ColumnAnno.primaryKey

  builder.append(this.Columns[0].constraint { str ->
    logger.error(
      this.Type,
      "${this.Name}: $str"
    )
  })

  if (lastPrimaryKey == PrimaryKey.MULTI_DESC || lastPrimaryKey == PrimaryKey.MULTI) {
    mulPrimaryKey.add(
      Pair(
        "${this.Columns[0].ColumnAnno.name} ${lastPrimaryKey.result}",
        this.Columns[0].ColumnAnno.sequence
      )
    )
  }

  for (i in 1 until this.Columns.size) {
    builder.append(", ")
    builder.append(this.Columns[i].constraint { str ->
      logger.error(
        this.Type,
        "${this.Name}: $str"
      )
    })

    val nowPrimaryKey = this.Columns[i].ColumnAnno.primaryKey
    if (nowPrimaryKey != PrimaryKey.FALSE && lastPrimaryKey != PrimaryKey.FALSE) {
      // 必须 都是 multi_
      if (!((lastPrimaryKey == PrimaryKey.MULTI_DESC || lastPrimaryKey == PrimaryKey.MULTI)
        && (nowPrimaryKey == PrimaryKey.MULTI_DESC || nowPrimaryKey == PrimaryKey.MULTI))) {

        logger.error(this.Type, "${this.Name}: PrimaryKey.ONLY_ONE_xx are too much")
        return ""
      }
    }
    lastPrimaryKey = nowPrimaryKey

    if (nowPrimaryKey == PrimaryKey.MULTI_DESC || nowPrimaryKey == PrimaryKey.MULTI) {
      mulPrimaryKey.add(
        Pair(
          "${this.Columns[i].ColumnAnno.name} ${nowPrimaryKey.result}",
          this.Columns[i].ColumnAnno.sequence
        )
      )
    }
  }

  val set: MutableSet<Int> = emptySet<Int>().toMutableSet()
  for ((_, seq) in mulPrimaryKey) {
    if (set.contains(seq)) {
      logger.error(this.Type, "the sequence($seq) of PrimaryKey.MULTI_xx are too much")
      return ""
    }
    set.add(seq)
  }

  mulPrimaryKey.sortWith { _1, _2 -> _1.second - _2.second }
  if (mulPrimaryKey.size != 0) {
    builder.append(", PRIMARY KEY(")
    builder.append("`${mulPrimaryKey[0].first}`")
    for (i in 1 until mulPrimaryKey.size) {
      builder.append(", ")
      builder.append("`${mulPrimaryKey[i].first}`")
    }
    builder.append(")")
  }

  // --> table_name(
  builder.append(")\"")

  return builder.toString()
}

fun Map<String, String>.toLiteral(): String {
  val builder = StringBuilder()
  builder.append(
    """
      mapOf(
    """.trimIndent()
  ).append("\n")

  for ((key, value) in this) {
    builder.append("  \"$key\" to \"$value\",\n")
  }

  builder.append(
    """
      )
    """.trimIndent()
  )

  return builder.toString()
}

fun TableInfo.allIndexFun(logger: Logger): String {
  val tableClass = this.Type.simpleName.toString()
  return """
    // IndexName => IndexSQL
    // IndexSQL: "CREATE INDEX IF NOT EXISTS ..."
    private fun ${tableClass}.Companion.allIndex(): Map<String, String> {
      return ${this.index { str -> logger.error(this.Type, "${this.Name}: $str") }.toLiteral().align("      ")}
    }
  """.trimIndent()
}

fun TableInfo.out(logger: Logger): String {
  val tableClass = this.Type.simpleName.toString()

  val column = this.alterColumns(logger).toLiteral()
  return """
    fun ${tableClass}.Companion.TableInfo(): TableInfo {
      return TableInfo(tableVersion, ${tableClass}.Migrators(), 
        ${tableClass}.allIndex(), 
        ${column.align("        ")}
      )
    }
  """.trimIndent()
}

