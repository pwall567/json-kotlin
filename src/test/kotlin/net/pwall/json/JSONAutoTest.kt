/*
 * @(#) JSONAutoTest.kt
 */

package net.pwall.json

import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.test.*

import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class JSONAutoTest {

    @Test fun `null input should return null`() {
        assertNull(JSONAuto.deserialize(String::class, null))
    }

    @Test fun `when JSONValue expected it should pass through unchanged`() {
        val json = JSONDouble(0.1)
        assertSame(json, JSONAuto.deserialize(JSONValue::class,  json))
        assertSame(json, JSONAuto.deserialize(JSONDouble::class,  json))
        val json2 = JSONString("abc")
        assertSame(json2, JSONAuto.deserialize(JSONValue::class,  json2))
        assertSame(json2, JSONAuto.deserialize(JSONString::class,  json2))
    }

    @Test fun `companion object with fromJSON should use that function`() {
        val json = JSONObject.create().putValue("dec", "17").putValue("hex", "11")
        val expected: DummyFromJSON? = DummyFromJSON(17)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return string`() {
        val json = JSONString("abc")
        val expected: String? = "abc"
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `single character JSONString should return character`() {
        val json = JSONString("Q")
        val expected: Char? = 'Q'
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return character array`() {
        val json = JSONString("abcdef")
        val expected: CharArray? = arrayOf('a', 'b', 'c', 'd', 'e', 'f').toCharArray()
        assertTrue(Arrays.equals(expected, JSONAuto.deserialize(json)))
    }

    @Test fun `JSONString should return array of Char`() {
        val json = JSONString("abcdef")
        val expected: Array<Char>? = arrayOf('a', 'b', 'c', 'd', 'e', 'f')
        assertTrue(Arrays.equals(expected, JSONAuto.deserialize(Array<Char>::class, json)))
    }

    @Test fun `JSONString should return Calendar`() {
        val json = JSONString("2019-04-19T15:34:02.234+10:00")
        val cal = Calendar.getInstance()
        cal.set(2019, 3, 19, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        assertTrue(calendarEquals(cal, JSONAuto.deserialize(json)!!))
    }

    @Test fun `JSONString should return Date`() {
        val json = JSONString("2019-03-10T15:34:02.234+11:00")
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.set(2019, 2, 10, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 11 * 60 * 60 * 1000)
        val expected: Date? = cal.time
        expect(expected) { JSONAuto.deserialize(json) }
//        val json2 = JSONString("2019-03-11")
//        cal.clear()
//        cal.set(2019, 2, 11, 0, 0, 0)
//        cal.set(Calendar.MILLISECOND, 0)
//        cal.set(Calendar.ZONE_OFFSET, 0)
//        val expected2: Date? = cal.time
//        expect(expected2) { JSONAuto.deserialize(json2) }
    }

    @Test fun `JSONString should return Instant`() {
        val json = JSONString("2019-03-10T15:34:02.234Z")
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("GMT")
        cal.set(2019, 2, 10, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 0)
        val expected: Instant? = Instant.ofEpochMilli(cal.timeInMillis)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return LocalDate`() {
        val json = JSONString("2019-03-10")
        val expected: LocalDate? = LocalDate.of(2019, 3, 10)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return LocalDateTime`() {
        val json = JSONString("2019-03-10T16:43:33")
        val expected: LocalDateTime? = LocalDateTime.of(2019, 3, 10, 16, 43, 33)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return OffsetTime`() {
        val json = JSONString("16:46:11.234+10:00")
        val expected: OffsetTime? = OffsetTime.of(16, 46, 11, 234000000, ZoneOffset.ofHours(10))
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return OffsetDateTime`() {
        val json = JSONString("2019-03-10T16:46:11.234+10:00")
        val expected: OffsetDateTime? = OffsetDateTime.of(2019, 3, 10, 16, 46, 11, 234000000, ZoneOffset.ofHours(10))
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return ZonedDateTime`() {
        val json = JSONString("2019-03-10T16:46:11.234+10:00[Australia/Sydney]")
        val expected: ZonedDateTime? = ZonedDateTime.of(2019, 3, 10, 16, 46, 11, 234000000,
                ZoneId.of("Australia/Sydney"))
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return Year`() {
        val json = JSONString("2019")
        val expected: Year? = Year.of(2019)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return YearMonth`() {
        val json = JSONString("2019-03")
        val expected: YearMonth? = YearMonth.of(2019, 3)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return enum`() {
        val json = JSONString("ALPHA")
        val expected: DummyEnum? = DummyEnum.ALPHA
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONString should return BigInteger (etc)`() {
        val str = "123456789"
        val json = JSONString(str)
        val expected: BigInteger? = BigInteger(str)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONInteger should return Int`() {
        val json = JSONInteger(1234)
        val expected: Int? = 1234
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONLong should return Long`() {
        val json = JSONLong(123456789012345)
        val expected: Long? = 123456789012345
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONDouble should return Double`() {
        val json = JSONDouble(123.45)
        val expected: Double? = 123.45
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONInteger should return Short`() {
        val json = JSONInteger(1234)
        val expected: Short? = 1234
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONInteger should return Byte`() {
        val json = JSONInteger(123)
        val expected: Byte? = 123
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONArray of boolean should return BooleanArray`() {
        val json = JSONArray.create().addValue(true).addValue(false).addValue(false)
        val expected = booleanArrayOf(true, false, false)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(BooleanArray::class, json)))
    }

    @Test fun `JSONArray of boolean should fail if entries not boolean`() {
        val json = JSONArray.create().addValue(123).addValue("ABC").addValue(false)
        assertFailsWith<JSONException> { JSONAuto.deserialize(BooleanArray::class, json) }
    }

    @Test fun `JSONArray of number should return ByteArray`() {
        val json = JSONArray.create().addValue(1).addValue(2).addValue(3)
        val expected = byteArrayOf(1, 2, 3)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(ByteArray::class, json)))
    }

    @Test fun `JSONArray of character should return CharArray`() {
        val json = JSONArray.create().addValue("a").addValue("b").addValue("c")
        val expected = charArrayOf('a', 'b', 'c')
        assertTrue(deepEquals(expected, JSONAuto.deserialize(CharArray::class, json)))
    }

    @Test fun `JSONArray of number should return DoubleArray`() {
        val json = JSONArray.create().addValue(123).addValue(0).addValue(0.012)
        val expected = doubleArrayOf(123.0, 0.0, 0.012)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(DoubleArray::class, json)))
    }

    @Test fun `JSONArray of number should return FloatArray`() {
        val json = JSONArray.create().addValue(123).addValue(0).addValue(0.012)
        val expected = floatArrayOf(123.0F, 0.0F, 0.012F)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(FloatArray::class, json)))
    }

    @Test fun `JSONArray of number should return IntArray`() {
        val json = JSONArray.create().addValue(12345).addValue(2468).addValue(321321)
        val expected = intArrayOf(12345, 2468, 321321)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(IntArray::class, json)))
    }

    @Test fun `JSONArray of number should fail if entries not number`() {
        val json = JSONArray.create().addValue("12345").addValue(true).addValue(321321)
        assertFailsWith<JSONException> { JSONAuto.deserialize(IntArray::class, json) }
    }

    @Test fun `JSONArray to IntArray should fail if entries not integer`() {
        val json = JSONArray.create().addValue(12345).addValue(0.123).addValue(321321)
        assertFailsWith<JSONException> { JSONAuto.deserialize(IntArray::class, json) }
    }

    @Test fun `JSONArray of number should return LongArray`() {
        val json = JSONArray.create().addValue(123456789123456).addValue(0).addValue(321321L)
        val expected = longArrayOf(123456789123456, 0, 321321)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(LongArray::class, json)))
    }

    @Test fun `JSONArray of number should return ShortArray`() {
        val json = JSONArray.create().addValue(1234).addValue(0).addValue(321)
        val expected = shortArrayOf(1234, 0, 321)
        assertTrue(deepEquals(expected, JSONAuto.deserialize(ShortArray::class, json)))
    }

    private val listStrings = listOf("abc", "def")

    @Test fun `JSONArray of JSONString should return List of String`() {
        val json = JSONArray.create().addValue("abc").addValue("def")
        val listType = this::class.members.find { it.name == "listStrings" }?.returnType ?: fail()
        expect(listStrings) { JSONAuto.deserialize(listType, json) }
    }

    private val arrayListStrings = ArrayList(listStrings)

    @Test fun `JSONArray of JSONString should return ArrayList of String`() {
        val json = JSONArray.create().addValue("abc").addValue("def")
        val listType = this::class.members.find { it.name == "arrayListStrings" }?.returnType ?: fail()
        expect(arrayListStrings) { JSONAuto.deserialize(listType, json) }
    }

    private val linkedListStrings = LinkedList(listStrings)

    @Test fun `JSONArray of JSONString should return LinkedList of String`() {
        val json = JSONArray.create().addValue("abc").addValue("def")
        val listType = this::class.members.find { it.name == "linkedListStrings" }?.returnType ?: fail()
        expect(linkedListStrings) { JSONAuto.deserialize(listType, json) }
    }

    private val hashSetStrings = HashSet(listStrings)

    @Test fun `JSONArray of JSONString should return HashSet of String`() {
        val json = JSONArray.create().addValue("abc").addValue("def")
        val setType = this::class.members.find { it.name == "hashSetStrings" }?.returnType ?: fail()
        expect(hashSetStrings) { JSONAuto.deserialize(setType, json) }
    }

    private val linkedHashSetStrings = LinkedHashSet(listStrings)

    @Test fun `JSONArray of JSONString should return LinkedHashSet of String`() {
        val json = JSONArray.create().addValue("abc").addValue("def")
        val setType = this::class.members.find { it.name == "linkedHashSetStrings" }?.returnType ?: fail()
        expect(linkedHashSetStrings) { JSONAuto.deserialize(setType, json) }
    }

    private val mapStringInt = mapOf("abc" to 123, "def" to 456, "ghi" to 789)

    @Test fun `JSONObject should return map of String to Int`() {
        val json = JSONObject.create().putValue("abc", 123).putValue("def", 456).putValue("ghi", 789)
        val mapType = this::class.members.find { it.name == "mapStringInt" }?.returnType ?: fail()
        expect(mapStringInt) { JSONAuto.deserialize(mapType, json)}
    }

    private val linkedHashMapStringInt = LinkedHashMap(mapStringInt)

    @Test fun `JSONObject should return LinkedHashMap of String to Int`() {
        val json = JSONObject.create().putValue("abc", 123).putValue("def", 456).putValue("ghi", 789)
        val mapType = this::class.members.find { it.name == "linkedHashMapStringInt" }?.returnType ?: fail()
        val result = JSONAuto.deserialize(mapType, json)
        assertEquals(linkedHashMapStringInt, result)
        assertTrue(result is LinkedHashMap<*, *>)
    }

    @Test fun `JSONObject should return simple data class`() {
        val json = JSONObject.create().putValue("field1", "Hello").putValue("field2", 12345)
        val expected: Dummy1? = Dummy1("Hello", 12345)
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONObject should return simple data class with default parameter`() {
        val json = JSONObject.create().putValue("field1", "Hello")
        val expected: Dummy1? = Dummy1("Hello")
        expect(expected) { JSONAuto.deserialize(json) }
    }

    @Test fun `JSONObject should return data class with extra values`() {
        val json = JSONObject.create().putValue("field1", "Hello").putValue("field2", 12345).putValue("extra", "XXX")
        val expected: Dummy2? = Dummy2("Hello", 12345)
        expected?.extra = "XXX"
        val result = JSONAuto.deserialize<Dummy2>(json)
        assertEquals(expected, result)
        assertEquals("XXX", result?.extra)
    }

    @Test fun `JSONObject should return nested data class`() {
        val json1 = JSONObject.create().putValue("field1", "Whoa").putValue("field2", 98765)
        val json2 = JSONObject.create().putJSON("dummy1", json1).putValue("text", "special")
        val expected: Dummy3? = Dummy3(Dummy1("Whoa", 98765), "special")
        expect(expected) { JSONAuto.deserialize(json2) }
    }

    @Test fun `JSONObject should return nested data class with list`() {
        val json1 = JSONObject.create().putValue("field1", "Whoa").putValue("field2", 98765)
        val json2 = JSONObject.create().putValue("field1", "Hi!").putValue("field2", 333)
        val json3 = JSONArray.create().addJSON(json1).addJSON(json2)
        val json4 = JSONObject.create().putJSON("listDummy1", json3).putValue("text", "special")
        val expected: Dummy4? = Dummy4(listOf(Dummy1("Whoa", 98765), Dummy1("Hi!", 333)), "special")
        expect(expected) { JSONAuto.deserialize(json4) }
    }

    private val calendarFields = arrayOf(Calendar.YEAR, Calendar.MONTH,
            Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND,
            Calendar.MILLISECOND, Calendar.ZONE_OFFSET)

    private fun calendarEquals(a: Calendar, b: Calendar): Boolean {
        for (field in calendarFields)
            if (a.get(field) != b.get(field))
                return false
        return true
    }

    private fun deepEquals(a: BooleanArray, b: BooleanArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: ByteArray, b: ByteArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: CharArray, b: CharArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: DoubleArray, b: DoubleArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: FloatArray, b: FloatArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: IntArray, b: IntArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: LongArray, b: LongArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

    private fun deepEquals(a: ShortArray, b: ShortArray?): Boolean {
        if (b == null || a.size != b.size)
            return false
        for (i in 0 until a.size)
            if (a[i] != b[i])
                return false
        return true
    }

}

data class Dummy1(val field1: String, val field2: Int = 999)

data class Dummy2(val field1: String, val field2: Int = 999) {
    var extra: String? = null
}

data class Dummy3(val dummy1: Dummy1, val text: String)

data class Dummy4(val listDummy1: List<Dummy1>, val text: String)

data class DummyFromJSON(val int1: Int) {

    companion object {
        @Suppress("unused")
        fun fromJSON(json: JSONValue): DummyFromJSON {
            val jsonObject = json as JSONObject
            val dec = jsonObject.getString("dec").toInt()
            val hex = jsonObject.getString("hex").toInt(16)
            if (dec != hex)
                throw JSONException("Inconsistent values")
            return DummyFromJSON(dec)
        }
    }

}

enum class DummyEnum { ALPHA, BETA, GAMMA }
