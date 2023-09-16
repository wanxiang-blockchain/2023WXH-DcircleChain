package demo.dcircle.identity

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL
import java.util.regex.Pattern

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
    fun test_DIDAddress() {
        val input = "a<_DidAddress::123_>ba<_DidAddress::123_>c"

        val regex = "<_DidAddress::(.+?)_>"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(input)

        while (matcher.find()) {
            val didAddress = matcher.group(1)
            println("Did Address: $didAddress")
        }
    }

    @Test
    fun test_Url() {
        val url = URL("https://test.dcirclescan.io/l/xxx")
        println("url path=${url.path} host=${url.host} protocol=${url.protocol}")
        val url2 = URL("https://test.dcirclescan.io/#/qr/l/xxx")
        println("url path=${url2.path} host=${url.host} protocol=${url.protocol}")
    }

    @Test
    fun test_Language() {
        val input = "<_La::en::hello::La_>,<_La::zhHans::123::La_>,<_La::zhHant::abc::La_>"

        val regex = "<_La::(.+?)::(.+?)::La_>"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(input)

        while (matcher.find()) {
            val key = matcher.group(1)
            val value = matcher.group(2)
            println("key - value: $key $value")
        }
    }
}