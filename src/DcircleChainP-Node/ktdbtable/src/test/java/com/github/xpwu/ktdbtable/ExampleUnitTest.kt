package com.github.xpwu.ktdbtable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun clazz() {
    assertEquals(Map::class.qualifiedName, "kotlin.collections.Map")
    assertEquals(Version::class.qualifiedName, "com.github.xpwu.ktdbtable.Version")
    assertEquals(Collection::class.qualifiedName, "kotlin.collections.Collection")
  }


  @Test
  fun column() {
    assertEquals(ByteArrayColumn("one"), LongColumn("one"))
    assertEquals(ByteArrayColumn("two"), LongColumn("two"))
    assertNotEquals(LongColumn("one"), LongColumn("two"))

    assertEquals(LongColumn("one").hashCode(), "one".hashCode())
  }
}

