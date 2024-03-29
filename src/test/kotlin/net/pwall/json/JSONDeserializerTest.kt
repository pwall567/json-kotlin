/*
 * @(#) JSONDeserializerTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020, 2021 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json

import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.expect
import kotlin.test.fail
import kotlin.test.Test

import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Arrays
import java.util.BitSet
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.TimeZone
import java.util.UUID
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

class JSONDeserializerTest {

    @Test fun `null input should return null`() {
        assertNull(JSONDeserializer.deserialize(String::class, null))
    }

    @Test fun `null input should cause exception for non-null function`() {
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserializeNonNull(String::class, null) }
        expect("Can't deserialize null as String") { e.message }
    }

    @Test fun `when JSONValue expected it should pass through unchanged`() {
        val json = JSONDouble(0.1)
        assertSame(json, JSONDeserializer.deserialize(JSONValue::class,  json))
        assertSame(json, JSONDeserializer.deserialize(JSONDouble::class,  json))
        val json2 = JSONString("abc")
        assertSame(json2, JSONDeserializer.deserialize(JSONValue::class,  json2))
        assertSame(json2, JSONDeserializer.deserialize(JSONString::class,  json2))
    }

    @Test fun `companion object with fromJSON should use that function`() {
        val json = JSONObject().apply {
            putValue("dec", "17")
            putValue("hex", "11")
        }
        val expected: DummyFromJSON? = DummyFromJSON(17)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `companion object with multiple fromJSON should use the correct function`() {
        val json1 = JSONObject().apply {
            putValue("dec", "17")
            putValue("hex", "11")
        }
        val expected1: DummyMultipleFromJSON? = DummyMultipleFromJSON(17)
        expect(expected1) { JSONDeserializer.deserialize(json1) }
        val json2 = JSONInt(300)
        val expected2: DummyMultipleFromJSON? = DummyMultipleFromJSON(300)
        expect(expected2) { JSONDeserializer.deserialize(json2) }
        val json3 = JSONString("FF")
        val expected3: DummyMultipleFromJSON? = DummyMultipleFromJSON(255)
        expect(expected3) { JSONDeserializer.deserialize(json3) }
    }

    @Test fun `JSONString should return string`() {
        val json = JSONString("abc")
        val expected: String? = "abc"
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `single character JSONString should return character`() {
        val json = JSONString("Q")
        val expected: Char? = 'Q'
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return character array`() {
        val json = JSONString("abcdef")
        val expected: CharArray? = arrayOf('a', 'b', 'c', 'd', 'e', 'f').toCharArray()
        assertTrue(Arrays.equals(expected, JSONDeserializer.deserialize(json)))
    }

    @Test fun `JSONString should return array of Char`() {
        val json = JSONString("abcdef")
        val expected: Array<Char>? = arrayOf('a', 'b', 'c', 'd', 'e', 'f')
        assertTrue(Arrays.equals(expected, JSONDeserializer.deserialize(Array<Char>::class, json)))
    }

    @Test fun `JSONString should return Calendar`() {
        val json = JSONString("2019-04-19T15:34:02.234+10:00")
        val cal = Calendar.getInstance()
        cal.set(2019, 3, 19, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        assertTrue(calendarEquals(cal, JSONDeserializer.deserialize(json)!!))
    }

    @Test fun `JSONString should return Date`() {
        val json = JSONString("2019-03-10T15:34:02.234+11:00")
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.set(2019, 2, 10, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 11 * 60 * 60 * 1000)
        val expected: Date? = cal.time
        expect(expected) { JSONDeserializer.deserialize(json) }
//        val json2 = JSONString("2019-03-11")
//        cal.clear()
//        cal.set(2019, 2, 11, 0, 0, 0)
//        cal.set(Calendar.MILLISECOND, 0)
//        cal.set(Calendar.ZONE_OFFSET, 0)
//        val expected2: Date? = cal.time
//        expect(expected2) { JSONDeserializer.deserialize(json2) }
    }

    @Test fun `JSONString should return java-sql-Date`() {
        val json = JSONString("2019-03-10")
        val expected: java.sql.Date? = java.sql.Date.valueOf("2019-03-10")
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return java-sql-Time`() {
        val json = JSONString("22:45:41")
        val expected: java.sql.Time? = java.sql.Time.valueOf("22:45:41")
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return java-sql-Timestamp`() {
        val json = JSONString("2019-03-10 22:45:41.5")
        val expected: java.sql.Timestamp? = java.sql.Timestamp.valueOf("2019-03-10 22:45:41.5")
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return Instant`() {
        val json = JSONString("2019-03-10T15:34:02.234Z")
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("GMT")
        cal.set(2019, 2, 10, 15, 34, 2) // month value is month - 1
        cal.set(Calendar.MILLISECOND, 234)
        cal.set(Calendar.ZONE_OFFSET, 0)
        val expected: Instant? = Instant.ofEpochMilli(cal.timeInMillis)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return LocalDate`() {
        val json = JSONString("2019-03-10")
        val expected: LocalDate? = LocalDate.of(2019, 3, 10)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return LocalDateTime`() {
        val json = JSONString("2019-03-10T16:43:33")
        val expected: LocalDateTime? = LocalDateTime.of(2019, 3, 10, 16, 43, 33)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return LocalTime`() {
        val json = JSONString("16:43:33")
        val expected: LocalTime? = LocalTime.of(16, 43, 33)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return OffsetTime`() {
        val json = JSONString("16:46:11.234+10:00")
        val expected: OffsetTime? = OffsetTime.of(16, 46, 11, 234000000, ZoneOffset.ofHours(10))
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return OffsetDateTime`() {
        val json = JSONString("2019-03-10T16:46:11.234+10:00")
        val expected: OffsetDateTime? = OffsetDateTime.of(2019, 3, 10, 16, 46, 11, 234000000, ZoneOffset.ofHours(10))
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return ZonedDateTime`() {
        val json = JSONString("2019-01-10T16:46:11.234+11:00[Australia/Sydney]")
        val expected: ZonedDateTime? = ZonedDateTime.of(2019, 1, 10, 16, 46, 11, 234000000,
                ZoneId.of("Australia/Sydney"))
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return Year`() {
        val json = JSONString("2019")
        val expected: Year? = Year.of(2019)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return YearMonth`() {
        val json = JSONString("2019-03")
        val expected: YearMonth? = YearMonth.of(2019, 3)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return MonthDay`() {
        val json = JSONString("--03-10")
        val expected: MonthDay? = MonthDay.of(3, 10)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return Duration`() {
        val json = JSONString("PT2H")
        val expected: Duration? = Duration.ofHours(2)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return Period`() {
        val json = JSONString("P3M")
        val expected: Period? = Period.ofMonths(3)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return UUID`() {
        val uuid = "b082b046-ac9b-11eb-8ea7-5fc81989f104"
        val json = JSONString(uuid)
        val expected: UUID? = UUID.fromString("b082b046-ac9b-11eb-8ea7-5fc81989f104")
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should fail on invalid UUID`() {
        val json = JSONString("b082b046-ac9b-11eb-8ea7-5fc81989f1") // 2 bytes too short
        assertFailsWith<JSONException> { JSONDeserializer.deserialize<UUID>(json) }.let {
            expect("Error deserializing \"b082b046-ac9b-11eb-8ea7-5fc81989f1\" as UUID") { it.message }
        }
    }

    @Test fun `JSONString should return URI`() {
        val uriString = "http://pwall.net"
        val json = JSONString(uriString)
        val expected: URI? = URI(uriString)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return URL`() {
        val urlString = "https://pwall.net"
        val json = JSONString(urlString)
        val expected: URL? = URL(urlString)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONArray should return BitSet`() {
        val bitset = BitSet()
        bitset.set(2)
        bitset.set(7)
        val json = JSONArray().apply {
            addValue(2)
            addValue(7)
        }
        val expected: BitSet? = bitset
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return enum`() {
        val json = JSONString("ALPHA")
        val expected: DummyEnum? = DummyEnum.ALPHA
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return BigInteger`() {
        val str = "123456789"
        val json = JSONString(str)
        val expected: BigInteger? = BigInteger(str)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONLong should return BigInteger`() {
        val value = 123456789012345678
        val json = JSONLong(value)
        val expected: BigInteger? = BigInteger.valueOf(value)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONInt should return BigInteger`() {
        val value = 12345678
        val json = JSONInt(value)
        val expected: BigInteger? = BigInteger.valueOf(value.toLong())
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONString should return BigDecimal`() {
        val str = "123456789.77777"
        val json = JSONString(str)
        val expected: BigDecimal? = BigDecimal(str)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONDecimal should return BigDecimal`() {
        val str = "123456789.77777"
        val json = JSONDecimal(str)
        val expected: BigDecimal? = BigDecimal(str)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONDouble should return BigDecimal`() {
        val value = 1234.5
        val json = JSONDouble(value)
        val expected: BigDecimal? = BigDecimal(value)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONInt should return Int`() {
        val json = JSONInt(1234)
        val expected: Int? = 1234
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONLong should return Long`() {
        val json = JSONLong(123456789012345)
        val expected: Long? = 123456789012345
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONDouble should return Double`() {
        val json = JSONDouble(123.45)
        val expected: Double? = 123.45
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONInt should return Short`() {
        val json = JSONInt(1234)
        val expected: Short? = 1234
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONInt should return Byte`() {
        val json = JSONInt(123)
        val expected: Byte? = 123
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONArray of boolean should return BooleanArray`() {
        val json = JSONArray().apply {
            addValue(true)
            addValue(false)
            addValue(false)
        }
        val expected = booleanArrayOf(true, false, false)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(BooleanArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of boolean should fail if entries not boolean`() {
        val json = JSONArray().apply {
            addValue(123)
            addValue("ABC")
            addValue(false)
        }
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize(BooleanArray::class, json) }
        expect("Can't deserialize 123 as Boolean at /0") { e.message }
    }

    @Test fun `JSONArray of number should return ByteArray`() {
        val json = JSONArray().apply {
            addValue(1)
            addValue(2)
            addValue(3)
        }
        val expected = byteArrayOf(1, 2, 3)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(ByteArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of character should return CharArray`() {
        val json = JSONArray().apply {
            addValue("a")
            addValue("b")
            addValue("c")
        }
        val expected = charArrayOf('a', 'b', 'c')
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(CharArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of number should return DoubleArray`() {
        val json = JSONArray().apply {
            addValue(123)
            addValue(0)
            addValue(0.012)
        }
        val expected = doubleArrayOf(123.0, 0.0, 0.012)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(DoubleArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of number should return FloatArray`() {
        val json = JSONArray().apply {
            addValue(123)
            addValue(0)
            addValue(0.012)
        }
        val expected = floatArrayOf(123.0F, 0.0F, 0.012F)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(FloatArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of number should return IntArray`() {
        val json = JSONArray().apply {
            addValue(12345)
            addValue(2468)
            addValue(321321)
        }
        val expected = intArrayOf(12345, 2468, 321321)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(IntArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of number should fail if entries not number`() {
        val json = JSONArray().apply {
            addValue("12345")
            addValue(true)
            addValue(321321)
        }
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize(IntArray::class, json) }
        expect("Can't deserialize \"12345\" as Int at /0") { e.message }
    }

    @Test fun `JSONArray to IntArray should fail if entries not integer`() {
        val json = JSONArray().apply {
            addValue(12345)
            addValue(0.123)
            addValue(321321)
        }
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize(IntArray::class, json) }
        expect("Can't deserialize 0.123 as Int at /1") { e.message }
    }

    @Test fun `JSONArray of number should return LongArray`() {
        val json = JSONArray().apply {
            addValue(123456789123456)
            addValue(0)
            addValue(321321L)
        }
        val expected = longArrayOf(123456789123456, 0, 321321)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(LongArray::class, json) ?: fail()))
    }

    @Test fun `JSONArray of number should return ShortArray`() {
        val json = JSONArray().apply {
            addValue(1234)
            addValue(0)
            addValue(321)
        }
        val expected = shortArrayOf(1234, 0, 321)
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(ShortArray::class, json) ?: fail()))
    }

    private val stringType = String::class.starProjectedType
    private val stringTypeProjection = KTypeProjection.invariant(stringType)
    private val intType = Int::class.starProjectedType
    private val intTypeProjection = KTypeProjection.invariant(intType)
    private val listStringType = List::class.createType(listOf(stringTypeProjection))
    private val arrayListStringType = ArrayList::class.createType(listOf(stringTypeProjection))
    private val linkedListStringType = LinkedList::class.createType(listOf(stringTypeProjection))
    private val setStringType = Set::class.createType(listOf(stringTypeProjection))
    private val hashSetStringType = HashSet::class.createType(listOf(stringTypeProjection))
    private val linkedHashSetStringType = LinkedHashSet::class.createType(listOf(stringTypeProjection))
    private val mapStringIntType = Map::class.createType(listOf(stringTypeProjection, intTypeProjection))
    private val linkedHashMapStringIntType = LinkedHashMap::class.createType(listOf(stringTypeProjection,
            intTypeProjection))

    private val listStrings = listOf("abc", "def")
    private val jsonArrayString = JSONArray().addValue("abc").addValue("def")

    @Test fun `JSONArray of JSONString should return List of String`() {
        expect(listStrings) { JSONDeserializer.deserialize(listStringType, jsonArrayString) }
    }

    @Test fun `JSONArray of JSONString should return ArrayList of String`() {
        expect(ArrayList(listStrings)) { JSONDeserializer.deserialize(arrayListStringType, jsonArrayString) }
    }

    @Test fun `JSONArray of JSONString should return LinkedList of String`() {
        expect(LinkedList(listStrings)) { JSONDeserializer.deserialize(linkedListStringType, jsonArrayString) }
    }

    @Test fun `JSONArray of JSONString should return Set of String`() {
        expect(LinkedHashSet(listStrings)) { JSONDeserializer.deserialize(setStringType, jsonArrayString) }
    }

    @Test fun `JSONArray of JSONString should reject duplicate`() {
        val jsonArrayDuplicate = JSONArray().addValue("abc").addValue("def").addValue("abc")
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize(setStringType, jsonArrayDuplicate) }
        expect("Duplicate not allowed at /2") { e.message }
    }

    @Test fun `JSONArray of JSONString should return HashSet of String`() {
        expect(HashSet(listStrings)) { JSONDeserializer.deserialize(hashSetStringType, jsonArrayString) }
    }

    @Test fun `JSONArray of JSONString should return LinkedHashSet of String`() {
        expect(LinkedHashSet(listStrings)) { JSONDeserializer.deserialize(linkedHashSetStringType, jsonArrayString) }
    }

    private val mapStringInt = mapOf("abc" to 123, "def" to 456, "ghi" to 789)
    private val jsonObjectInt = JSONObject().apply {
        putValue("abc", 123)
        putValue("def", 456)
        putValue("ghi", 789)
    }

    @Test fun `JSONObject should return map of String to Int`() {
        expect(mapStringInt) { JSONDeserializer.deserialize(mapStringIntType, jsonObjectInt)}
    }

    @Test fun `JSONObject should return LinkedHashMap of String to Int`() {
        val linkedHashMapStringInt = LinkedHashMap(mapStringInt)
        val result = JSONDeserializer.deserialize(linkedHashMapStringIntType, jsonObjectInt)
        assertEquals(linkedHashMapStringInt, result)
        assertTrue(result is LinkedHashMap<*, *>)
    }

    @Test fun `JSONObject should return simple data class`() {
        val json = JSONObject().apply {
            putValue("field1", "Hello")
            putValue("field2", 12345)
        }
        val expected: Dummy1? = Dummy1("Hello", 12345)
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONObject should return simple data class with default parameter`() {
        val json = JSONObject().putValue("field1", "Hello")
        val expected: Dummy1? = Dummy1("Hello")
        expect(expected) { JSONDeserializer.deserialize(json) }
    }

    @Test fun `JSONObject should return data class with extra values`() {
        val json = JSONObject().apply {
            putValue("field1", "Hello")
            putValue("field2", 12345)
            putValue("extra", "XXX")
        }
        val expected: Dummy2? = Dummy2("Hello", 12345)
        expected?.extra = "XXX"
        val result = JSONDeserializer.deserialize<Dummy2>(json)
        assertEquals(expected, result)
        assertEquals("XXX", result?.extra)
    }

    @Test fun `JSONObject should return nested data class`() {
        val json1 = JSONObject().apply {
            putValue("field1", "Whoa")
            putValue("field2", 98765)
        }
        val json2 = JSONObject().apply {
            put("dummy1", json1)
            putValue("text", "special")
        }
        val expected: Dummy3? = Dummy3(Dummy1("Whoa", 98765), "special")
        expect(expected) { JSONDeserializer.deserialize(json2) }
    }

    @Test fun `JSONObject should return nested data class with list`() {
        val json1 = JSONObject().apply {
            putValue("field1", "Whoa")
            putValue("field2", 98765)
        }
        val json2 = JSONObject().apply {
            putValue("field1", "Hi!")
            putValue("field2", 333)
        }
        val json3 = JSONArray().apply {
            add(json1)
            add(json2)
        }
        val json4 = JSONObject().apply {
            put("listDummy1", json3)
            putValue("text", "special")
        }
        val expected: Dummy4? = Dummy4(listOf(Dummy1("Whoa", 98765), Dummy1("Hi!", 333)), "special")
        expect(expected) { JSONDeserializer.deserialize(json4) }
    }

    @Test fun `JSONObject should return simple class with properties`() {
        val json = JSONObject().apply {
            putValue("field1", "qqq")
            putValue("field2", 888)
        }
        val expected = Super()
        expected.field1 = "qqq"
        expected.field2 = 888
        expect(expected) { JSONDeserializer.deserialize(Super::class, json) }
    }

    @Test fun `JSONObject should return derived class with properties`() {
        // also test parsing from String
        val str = "{\"field1\":\"qqq\",\"field2\":888,\"field3\":12345.0}"
        val expected = Derived()
        expected.field1 = "qqq"
        expected.field2 = 888
        expected.field3 = 12345.0
        expect(expected) { JSONAuto.parse(Derived::class, str) }
    }

    @Test fun `JSONObject should return simple class with properties using name annotation`() {
        val json = JSONObject().apply {
            putValue("field1", "qqq")
            putValue("fieldX", 888)
        }
        val expected = DummyWithNameAnnotation()
        expected.field1 = "qqq"
        expected.field2 = 888
        expect(expected) { JSONDeserializer.deserialize(DummyWithNameAnnotation::class, json) }
    }

    @Test fun `JSONObject should return data class using name annotation`() {
        val json = JSONObject().apply {
            putValue("field1", "qqq")
            putValue("fieldX", 888)
        }
        expect(DummyWithParamNameAnnotation("qqq", 888)) {
            JSONDeserializer.deserialize(DummyWithParamNameAnnotation::class, json)
        }
    }

    @Test fun `JSONObject should return data class using custom name annotation`() {
        val json = JSONObject().apply {
            putValue("field1", "qqq")
            putValue("fieldX", 888)
        }
        val expected = DummyWithCustomNameAnnotation("qqq", 888)
        val config = JSONConfig().apply {
            addNameAnnotation(CustomName::class, "symbol")
        }
        expect(expected) { JSONDeserializer.deserialize(DummyWithCustomNameAnnotation::class, json, config) }
    }

    private val pairStringStringType = Pair::class.createType(listOf(stringTypeProjection, stringTypeProjection))
    private val pairStringIntType = Pair::class.createType(listOf(stringTypeProjection, intTypeProjection))
    private val tripleStringStringStringType = Triple::class.createType(listOf(stringTypeProjection,
            stringTypeProjection, stringTypeProjection))
    private val tripleStringIntStringType = Triple::class.createType(listOf(stringTypeProjection,
            intTypeProjection, stringTypeProjection))

    @Test fun `JSONArray should return Pair`() {
        val json = JSONArray().apply {
            addValue("abc")
            addValue("def")
        }
        expect("abc" to "def") { JSONDeserializer.deserialize(pairStringStringType, json) }
    }

    @Test fun `JSONArray should return Heterogenous Pair`() {
        val json = JSONArray().apply {
            addValue("abc")
            addValue(88)
        }
        expect("abc" to 88) { JSONDeserializer.deserialize(pairStringIntType, json) }
    }

    @Test fun `JSONArray should return Triple`() {
        val json = JSONArray().apply {
            addValue("abc")
            addValue("def")
            addValue("xyz")
        }
        expect(Triple("abc", "def", "xyz")) { JSONDeserializer.deserialize(tripleStringStringStringType, json) }
    }

    @Test fun `JSONArray should return Heterogenous Triple`() {
        val json = JSONArray().apply {
            addValue("abc")
            addValue(66)
            addValue("xyz")
        }
        expect(Triple("abc", 66, "xyz")) { JSONDeserializer.deserialize(tripleStringIntStringType, json) }
    }

    @Test fun `null should return null for nullable String`() {
        val json: JSONValue? = null
        assertNull(JSONDeserializer.deserialize(String::class.createType(emptyList(), true), json))
    }

    @Test fun `null should fail for non-nullable String`() {
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize(stringType, null) }
        expect("Can't deserialize null as String") { e.message }
    }

    @Test fun `JSONObject should deserialize to object`() {
        val json = JSONObject().putValue("field1", "abc")
        expect(DummyObject) { JSONDeserializer.deserialize(DummyObject::class, json) }
    }

    @Test fun `class with constant val should deserialize correctly`() {
        val json = JSONObject().putValue("field8", "blert")
        expect(DummyWithVal()) { JSONDeserializer.deserialize(DummyWithVal::class, json) }
    }

    @Test fun `java class should deserialize correctly`() {
        val json = JSONObject().apply {
            putValue("field1", 1234)
            putValue("field2", "Hello!")
        }
        expect(JavaClass1(1234, "Hello!")) { JSONDeserializer.deserialize(JavaClass1::class, json) }
    }

    @Test fun `deserialize List using Java Type should work correctly`() {
        val json = JSONArray().apply {
            add(JSONObject().apply {
                putValue("field1", 567)
                putValue("field2", "abcdef")
            })
            add(JSONObject().apply {
                putValue("field1", 9999)
                putValue("field2", "qwerty")
            })
        }
        val type: Type = JavaClass2::class.java.getField("field1").genericType
        expect(listOf(JavaClass1(567, "abcdef"), JavaClass1(9999, "qwerty"))) {
            JSONDeserializer.deserialize(type, json)
        }
    }

    @Test fun `JSONArray should deserialize into List derived type`() {
        val json = JSONArray().apply {
            addValue("2019-10-06")
            addValue("2019-10-05")
        }
        expect(DummyList(listOf(LocalDate.of(2019, 10, 6), LocalDate.of(2019, 10, 5)))) {
            JSONDeserializer.deserialize(DummyList::class, json)
        }
    }

    @Test fun `JSONObject should deserialize into Map derived type`() {
        val json = JSONObject().apply {
            putValue("aaa", "2019-10-06")
            putValue("bbb", "2019-10-05")
        }
        val expected = DummyMap(emptyMap()).apply {
            put("aaa", LocalDate.of(2019, 10, 6))
            put("bbb", LocalDate.of(2019, 10, 5))
        }
        expect(expected) { JSONDeserializer.deserialize(DummyMap::class, json) }
    }

    @Suppress("UNCHECKED_CAST")
    @Test fun `JSONArray should deserialize into Sequence`() {
        val json = JSONArray().apply {
            addValue("abcde")
            addValue("fghij")
        }
        val expected = sequenceOf("abcde", "fghij")
        val stringSequenceType = Sequence::class.createType(listOf(stringTypeProjection))
        assertTrue(sequenceEquals(expected, JSONDeserializer.deserialize(stringSequenceType, json) as Sequence<String>))
    }

    @Suppress("UNCHECKED_CAST")
    @Test fun `JSONArray should deserialize into Array`() {
        val json = JSONArray().apply {
            addValue("abcde")
            addValue("fghij")
        }
        val expected = arrayOf("abcde", "fghij")
        val stringArrayType = Array<String>::class.createType(listOf(stringTypeProjection))
        assertTrue(expected.contentEquals(JSONDeserializer.deserialize(stringArrayType, json) as Array<String>))
    }

    @Suppress("UNCHECKED_CAST")
    @Test fun `JSONArray should deserialize into nested Array`() {
        val list1 = JSONArray().apply {
            addValue("qwerty")
            addValue("asdfgh")
            addValue("zxcvbn")
        }
        val list2 = JSONArray().apply {
            addValue("abcde")
            addValue("fghij")
        }
        val json = JSONArray().apply {
            add(list1)
            add(list2)
        }
        val array1 = arrayOf("qwerty", "asdfgh", "zxcvbn")
        val array2 = arrayOf("abcde", "fghij")
        val expected = arrayOf(array1, array2)
        val stringArrayType = Array<String>::class.createType(listOf(stringTypeProjection))
        val stringArrayArrayType = Array<String>::class.createType(listOf(KTypeProjection.invariant(stringArrayType)))
        val actual = JSONDeserializer.deserialize(stringArrayArrayType, json) as Array<Array<String>>
        assertTrue(expected.contentDeepEquals(actual))
    }

    @Test fun `JSONString should deserialize to Any`() {
        val json = JSONString("Hello!")
        expect("Hello!") { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONBoolean should deserialize to Any`() {
        val json1 = JSONBoolean.TRUE
        val result1 = JSONDeserializer.deserializeAny(json1)
        assertTrue(result1 is Boolean && result1)
        val json2 = JSONBoolean.FALSE
        val result2 = JSONDeserializer.deserializeAny(json2)
        assertTrue(result2 is Boolean && !result2)
    }

    @Test fun `JSONInt should deserialize to Any`() {
        val json = JSONInt(123456)
        expect(123456) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONLong should deserialize to Any`() {
        val json = JSONLong(1234567890123456L)
        expect(1234567890123456L) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONFloat should deserialize to Any`() {
        val json = JSONFloat(0.12345F)
        expect(0.12345F) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONDouble should deserialize to Any`() {
        val json = JSONDouble(0.123456789)
        expect(0.123456789) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONZero should deserialize to Any`() {
        val json = JSONZero()
        expect(0) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONArray should deserialize to Any`() {
        val json = JSONArray().apply {
            addValue("abcde")
            addValue("fghij")
        }
        expect(listOf("abcde", "fghij")) { JSONDeserializer.deserializeAny(json) }
    }

    @Test fun `JSONObject should deserialize to Any`() {
        val json = JSONObject().apply {
            putValue("aaa", 1234)
            putValue("ccc", 9999)
            putValue("bbb", 5678)
            putValue("abc", 8888)
        }
        val result = JSONDeserializer.deserializeAny(json)
        // check that the result is a map in the correct order
        if (result is Map<*, *>) {
            val iterator = result.keys.iterator()
            expect(true) { iterator.hasNext() }
            iterator.next().let {
                expect("aaa") { it }
                expect(1234) { result[it] }
            }
            expect(true) { iterator.hasNext() }
            iterator.next().let {
                expect("ccc") { it }
                expect(9999) { result[it] }
            }
            expect(true) { iterator.hasNext() }
            iterator.next().let {
                expect("bbb") { it }
                expect(5678) { result[it] }
            }
            expect(true) { iterator.hasNext() }
            iterator.next().let {
                expect("abc") { it }
                expect(8888) { result[it] }
            }
            expect(false) { iterator.hasNext() }
        }
        else
            fail("Not a Map - $result")
    }

    @Test fun `sealed class should deserialize to correct subclass`() {
        val json = JSONObject().apply {
            putValue("class", "Const")
            putValue("number", 2.0)
        }
        expect(Const(2.0)) { JSONDeserializer.deserialize<Expr>(json) }
    }

    @Test fun `sealed class should deserialize to correct object subclass`() {
        val json = JSONObject().apply {
            putValue("class", "NotANumber")
        }
        expect(NotANumber) { JSONDeserializer.deserialize<Expr>(json) }
    }

    @Test fun `sealed class should deserialize with custom discriminator`() {
        val config = JSONConfig().apply {
            sealedClassDiscriminator = "?"
        }
        val json = JSONObject().apply {
            putValue("?", "Const")
            putValue("number", 2.0)
        }
        expect(Const(2.0)) { JSONDeserializer.deserialize<Expr>(json, config) }
    }

    @Test fun `class should ignore additional fields when allowExtra set in config`() {
        val config = JSONConfig().apply {
            allowExtra = true
        }
        val json = JSONObject().apply {
            putValue("field1", "Hello")
            putValue("field2", 123)
            putValue("extra", "allow")
        }
        expect(Dummy1("Hello", 123)) { JSONDeserializer.deserialize<Dummy1>(json, config) }
    }

    @Test fun `class annotated with @JSONAllowExtra should ignore additional fields`() {
        val json = JSONObject().apply {
            putValue("field1", "Hello")
            putValue("field2", 123)
            putValue("extra", "allow")
        }
        expect(DummyWithAllowExtra("Hello", 123)) { JSONDeserializer.deserialize<DummyWithAllowExtra>(json) }
    }

    @Test fun `class annotated with custom allow extra should ignore additional fields`() {
        val config = JSONConfig().apply {
            addAllowExtraPropertiesAnnotation(CustomAllowExtraProperties::class)
        }
        val json = JSONObject().apply {
            putValue("field1", "Hi")
            putValue("field2", 123)
            putValue("extra", "allow")
        }
        expect(DummyWithCustomAllowExtra("Hi", 123)) {
            JSONDeserializer.deserialize<DummyWithCustomAllowExtra>(json, config)
        }
    }

    @Test fun `field annotated with @JSONIgnore should be ignored on deserialization`() {
        val json = JSONObject().apply {
            putValue("field1", "one")
            putValue("field2", "two")
            putValue("field3", "three")
        }
        expect(DummyWithIgnore(field1 = "one", field3 = "three")) {
            JSONDeserializer.deserialize<DummyWithIgnore>(json)
        }
    }

    @Test fun `field annotated with custom ignore annotation should be ignored on deserialization`() {
        val config = JSONConfig().apply {
            addIgnoreAnnotation(CustomIgnore::class)
        }
        val json = JSONObject().apply {
            putValue("field1", "one")
            putValue("field2", "two")
            putValue("field3", "three")
        }
        expect(DummyWithCustomIgnore(field1 = "one", field3 = "three")) {
            JSONDeserializer.deserialize<DummyWithCustomIgnore>(json, config)
        }
    }

    @Test fun `should deserialize missing members as null where allowed`() {
        val json = JSONObject().apply {
            putValue("field2", 123)
        }
        expect(Dummy5(null, 123)) { JSONDeserializer.deserialize<Dummy5>(json) }
    }

    @Test fun `should deserialize custom parameterised type`() {
        val json = JSONObject().apply {
            put("lines", JSONArray(JSONString("abc"), JSONString("def")))
        }
        val expected = TestPage<String>(lines = listOf("abc", "def"))
        expect(expected) { JSONDeserializer.deserialize<TestPage<String>>(json) }
    }

    @Test fun `should deserialize nested custom parameterised type`() {
        val json1 = JSONObject().apply {
            put("lines", JSONArray(JSONString("abc"), JSONString("def")))
        }
        val json2 = JSONArray(json1, JSONString("xyz"))
        val expected = TestPage<String>(lines = listOf("abc", "def")) to "xyz"
        expect(expected) { JSONDeserializer.deserialize<Pair<TestPage<String>, String>>(json2) }
    }

    @Test fun `should deserialize differently nested custom parameterised type`() {
        val json = JSONObject().apply {
            put("lines", JSONArray(JSONArray(JSONString("abc"), JSONString("ABC")),
                    JSONArray(JSONString("def"), JSONString("DEF"))))
        }
        val expected = TestPage<Pair<String, String>>(lines = listOf("abc" to "ABC", "def" to "DEF"))
        expect(expected) { JSONDeserializer.deserialize<TestPage<Pair<String, String>>>(json) }
    }

    @Test fun `should deserialize complex custom parameterised type`() {
        val obj1 = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
        }
        val obj2 = JSONObject().apply {
            putValue("field1", "def")
            putValue("field2", 456)
        }
        val json = JSONObject().apply {
            put("lines", JSONArray(obj1, obj2))
        }
        val expected = TestPage<Dummy1>(lines = listOf(Dummy1("abc", 123), Dummy1("def", 456)))
        expect(expected) { JSONDeserializer.deserialize<TestPage<Dummy1>>(json) }
    }

    @Test fun `should deserialize another form of custom parameterised type`() {
        val obj1 = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
        }
        val json = JSONObject().apply {
            putValue("description", "testing")
            put("data", obj1)
        }
        val dummy1 = Dummy1("abc", 123)
        val expected = TestDataHolder("testing", dummy1)
        expect(expected) { JSONDeserializer.deserialize<TestDataHolder<Dummy1>>(json) }
    }

    @Test fun `should deserialize yet another form of custom parameterised type`() {
        val json = JSONObject().apply {
            put("lineLists", JSONArray(JSONArray(JSONString("lineA1"), JSONString("lineA2")),
                    JSONArray(JSONString("lineB1"), JSONString("lineB2"))))
        }
        val expected = TestPage2<String>(lineLists = listOf(listOf("lineA1", "lineA2"), listOf("lineB1", "lineB2")))
        expect(expected) { JSONDeserializer.deserialize<TestPage2<String>>(json)}
    }

    @Test fun `should deserialize Java Stream`() {
        val json = JSONArray(JSONString("abc"), JSONString("def"))
        val result: Stream<String> = JSONDeserializer.deserialize(json) ?: fail("result was null")
        val iterator = result.iterator()
        expect(true) { iterator.hasNext() }
        expect("abc") { iterator.next() }
        expect(true) { iterator.hasNext() }
        expect("def") { iterator.next() }
        expect(false) { iterator.hasNext() }
    }

    @Test fun `should deserialize Java IntStream`() {
        val json = JSONArray(JSONInt(2345), JSONInt(6789))
        val result: IntStream = JSONDeserializer.deserialize(json) ?: fail("result was null")
        val iterator = result.iterator()
        expect(true) { iterator.hasNext() }
        expect(2345) { iterator.next() }
        expect(true) { iterator.hasNext() }
        expect(6789) { iterator.next() }
        expect(false) { iterator.hasNext() }
    }

    @Test fun `should deserialize Java LongStream`() {
        val json = JSONArray(JSONLong(1234567812345678), JSONLong(9876543298765432))
        val result: LongStream = JSONDeserializer.deserialize(json) ?: fail("result was null")
        val iterator = result.iterator()
        expect(true) { iterator.hasNext() }
        expect(1234567812345678) { iterator.next() }
        expect(true) { iterator.hasNext() }
        expect(9876543298765432) { iterator.next() }
        expect(false) { iterator.hasNext() }
    }

    @Test fun `should deserialize Java DoubleStream`() {
        val json = JSONArray(JSONDouble(1234.5), JSONDouble(1e40))
        val result: DoubleStream = JSONDeserializer.deserialize(json) ?: fail("result was null")
        val iterator = result.iterator()
        expect(true) { iterator.hasNext() }
        expect(1234.5) { iterator.next() }
        expect(true) { iterator.hasNext() }
        expect(1e40) { iterator.next() }
        expect(false) { iterator.hasNext() }
    }

    @Test fun `should give error message with pointer`() {
        val json = JSON.parse("""{"field1":"abc","field2":"def"}""")
        val e1 = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize<Dummy1>(json) }
        expect("Can't deserialize \"def\" as Int at /field2") { e1.message }
        val e2 = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize<List<Dummy1>>(JSONArray(json)) }
        expect("Can't deserialize \"def\" as Int at /0/field2") { e2.message }
    }

    @Test fun `should give expanded error message with pointer`() {
        val json = JSON.parse("""{"field2":1}""")
        val e1 = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize<Dummy1>(json) }
        expect("Can't create Dummy1; missing: field1") { e1.message }
        val e2 = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize<List<Dummy1>>(JSONArray(json)) }
        expect("Can't create Dummy1; missing: field1 at /0") { e2.message }
    }

    @Test fun `should give expanded error message for multiple constructors`() {
        val json = JSON.parse("""[{"aaa":"X"},{"bbb":1},{"ccc":true,"ddd":0}]""")
        val e = assertFailsWith<JSONKotlinException> { JSONDeserializer.deserialize<List<MultiConstructor>>(json) }
        expect("Can't locate constructor for MultiConstructor; properties: ccc, ddd at /2") { e.message }
    }

    @Test fun `should use type projection upperBounds`() {
        val json = JSON.parse("""{"expr":{"class":"Const","number":20.0}}""")
        val expr = JSONDeserializer.deserialize<SealedClassContainer<*>>(json)?.expr
        assertTrue(expr is Const)
        expect(20.0) { expr.number }
    }

    private fun <T> sequenceEquals(seq1: Sequence<T>, seq2: Sequence<T>) = seq1.toList() == seq2.toList()

    private val calendarFields = arrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY,
            Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND, Calendar.ZONE_OFFSET)

    private fun calendarEquals(a: Calendar, b: Calendar): Boolean {
        for (field in calendarFields)
            if (a.get(field) != b.get(field))
                return false
        return true
    }

    data class TestPage<T>(val header: String? = null, val lines: List<T>)

    data class TestDataHolder<T>(val description: String, val data: T)

    data class TestPage2<T>(val header: String? = null, val lineLists: List<List<T>>)

    data class SealedClassContainer<T: Expr>(val expr: T)

}
