/*
 * @(#) JSONSerializerTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020 Peter Wall
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

import kotlin.math.abs
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.expect
import kotlin.test.Test

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
import java.util.BitSet
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import java.util.stream.IntStream
import java.util.stream.Stream

class JSONSerializerTest {

    @Test fun `null should return null`() {
        assertNull(JSONSerializer.serialize(null))
    }

    @Test fun `JSONValue should be returned as-is`() {
        val json = JSONInt(12345)
        assertSame(json, JSONSerializer.serialize(json))
    }

    @Test fun `String should return JSONString`() {
        val str = "Hello JSON!"
        expect(JSONString(str)) { JSONSerializer.serialize(str) }
    }

    @Test fun `StringBuilder should return JSONString`() {
        val str = "Hello JSON!"
        val sb = StringBuilder(str)
        expect(JSONString(str)) { JSONSerializer.serialize(sb) }
    }

    @Test fun `Char should return JSONString`() {
        val char = 'Q'
        expect(JSONString("Q")) { JSONSerializer.serialize(char) }
    }

    @Test fun `Int should return JSONInt`() {
        val i = 123456
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInt)
        assertTrue(intEquals(i, actual.value))
        // Note - these assertions are complicated because JSONInt.equals() returns true
        // for any comparison with another numeric JSON types where the values are equal
    }

    @Test fun `Int (negative) should return JSONInt`() {
        val i = -8888
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInt)
        assertTrue(intEquals(i, actual.value))
    }

    @Test fun `Long should return JSONLong`() {
        val i = 12345678901234
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONLong)
        assertTrue(longEquals(i, actual.value))
    }

    @Test fun `Long (negative) should return JSONLong`() {
        val i = -987654321987654321
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONLong)
        assertTrue(longEquals(i, actual.value))
    }

    @Test fun `Short should return JSONInt`() {
        val i: Short = 1234
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInt)
        assertTrue(intEquals(i.toInt(), actual.value))
    }

    @Test fun `Byte should return JSONInt`() {
        val i: Byte = 123
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInt)
        assertTrue(intEquals(i.toInt(), actual.value))
    }

    @Test fun `Float should return JSONFloat`() {
        val f = 0.1234F
        val actual = JSONSerializer.serialize(f)
        assertTrue(actual is JSONFloat)
        assertTrue(floatEquals(f, actual.value))
    }

    @Test fun `Float (negative) should return JSONFloat`() {
        val f = -88.987F
        val actual = JSONSerializer.serialize(f)
        assertTrue(actual is JSONFloat)
        assertTrue(floatEquals(f, actual.value))
    }

    @Test fun `Double should return JSONDouble`() {
        val d = 987.654321
        val actual = JSONSerializer.serialize(d)
        assertTrue(actual is JSONDouble)
        assertTrue(doubleEquals(d, actual.value))
    }

    @Test fun `Double (exponent notation) should return JSONDouble`() {
        val d = 1e40
        val actual = JSONSerializer.serialize(d)
        assertTrue(actual is JSONDouble)
        assertTrue(doubleEquals(d, actual.value))
    }

    @Test fun `Boolean should return JSONBoolean`() {
        val t = true
        assertSame(JSONBoolean.TRUE, JSONSerializer.serialize(t))
        val f = false
        assertSame(JSONBoolean.FALSE, JSONSerializer.serialize(f))
    }

    @Test fun `CharArray should return JSONString`() {
        val ca = charArrayOf('H', 'e', 'l', 'l', 'o', '!')
        expect(JSONString("Hello!")) { JSONSerializer.serialize(ca) }
    }

    @Test fun `Array of Char should return JSONString`() {
        val ca = arrayOf('H', 'e', 'l', 'l', 'o', '!')
        expect(JSONString("Hello!")) { JSONSerializer.serialize(ca) }
    }

    @Test fun `Array of Int should return JSONArray`() {
        val array = arrayOf(123, 2345, 0, 999)
        val expected = JSONArray().apply {
            addValue(123)
            addValue(2345)
            addValue(0)
            addValue(999)
        }
        expect(expected) { JSONSerializer.serialize(array) }
    }

    @Test fun `Array of Int nullable should return JSONArray`() {
        val array = arrayOf(123, null, 0, 999)
        val expected = JSONArray().apply {
            addValue(123)
            addNull()
            addValue(0)
            addValue(999)
        }
        expect(expected) { JSONSerializer.serialize(array) }
    }

    @Test fun `Array of String should return JSONArray`() {
        val array = arrayOf("Hello", "Kotlin")
        val expected = JSONArray().apply {
            addValue("Hello")
            addValue("Kotlin")
        }
        expect(expected) { JSONSerializer.serialize(array) }
    }

    @Test fun `Iterator of String should return JSONArray`() {
        val iterator = listOf("Hello", "Kotlin").iterator()
        val expected = JSONArray().apply {
            addValue("Hello")
            addValue("Kotlin")
        }
        expect(expected) { JSONSerializer.serialize(iterator) }
    }

    @Test fun `Enumeration of String should return JSONArray`() {
        val list = listOf("Hello", "Kotlin")
        val expected = JSONArray().apply {
            addValue("Hello")
            addValue("Kotlin")
        }
        expect(expected) { JSONSerializer.serialize(ListEnum(list)) }
    }

    @Test fun `List of String should return JSONArray`() {
        val list = listOf("Hello", "Kotlin")
        val expected = JSONArray().apply {
            addValue("Hello")
            addValue("Kotlin")
        }
        expect(expected) { JSONSerializer.serialize(list) }
    }

    @Test fun `Sequence of String should return JSONArray`() {
        val seq = listOf("Hello", "Kotlin").asSequence()
        val expected = JSONArray().apply {
            addValue("Hello")
            addValue("Kotlin")
        }
        expect(expected) { JSONSerializer.serialize(seq) }
    }

    @Test fun `Map of String to Int should return JSONObject`() {
        val map = mapOf("abc" to 1, "def" to 4, "ghi" to 999)
        val expected = JSONObject().apply {
            putValue("abc", 1)
            putValue("def", 4)
            putValue("ghi", 999)
        }
        expect(expected) { JSONSerializer.serialize(map) }
    }

    @Test fun `Map of String to String nullable should return JSONObject`() {
        val map = mapOf("abc" to "hello", "def" to null, "ghi" to "goodbye")
        val expected = JSONObject().apply {
            putValue("abc", "hello")
            putNull("def")
            putValue("ghi", "goodbye")
        }
        expect(expected) { JSONSerializer.serialize(map) }
    }

    @Test fun `Class with toJSON() should serialize as JSONObject`() {
        val obj = DummyFromJSON(23)
        val expected = JSONObject().apply {
            putValue("dec", "23")
            putValue("hex", "17")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Enum should serialize as JSONString`() {
        val eee = DummyEnum.GAMMA
        expect(JSONString("GAMMA")) { JSONSerializer.serialize(eee) }
    }

    @Test fun `Calendar should return JSONString`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")).apply {
            set(Calendar.YEAR, 2019)
            set(Calendar.MONTH, 3)
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 52)
            set(Calendar.SECOND, 47)
            set(Calendar.MILLISECOND, 123)
            set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        }
        expect(JSONString("2019-04-25T18:52:47.123+10:00")) { JSONSerializer.serialize(cal) }
    }

    @Test fun `Date should return JSONString`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2019)
            set(Calendar.MONTH, 3)
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 52)
            set(Calendar.SECOND, 47)
            set(Calendar.MILLISECOND, 123)
            set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        }
        val date = cal.time
        // NOTE - Java implementations are inconsistent - some will normalise the time to UTC
        // while others preserve the time zone as supplied.  The test below allows for either.
        val expected1 = JSONString("2019-04-25T18:52:47.123+10:00")
        val expected2 = JSONString("2019-04-25T08:52:47.123Z")
        val result = JSONSerializer.serialize(date)
        expect(true) { result == expected1 || result == expected2 }
    }

    @Test fun `java-sql-Date should return JSONString`() {
        val str = "2019-04-25"
        val date = java.sql.Date.valueOf(str)
        expect(JSONString(str)) { JSONSerializer.serialize(date) }
    }

    @Test fun `java-sql-Time should return JSONString`() {
        val str = "22:41:19"
        val time = java.sql.Time.valueOf(str)
        expect(JSONString(str)) { JSONSerializer.serialize(time) }
    }

    @Test fun `java-sql-Timestamp should return JSONString`() {
        val str = "2019-04-25 22:41:19.5"
        val timestamp = java.sql.Timestamp.valueOf(str)
        expect(JSONString(str)) { JSONSerializer.serialize(timestamp) }
    }

    @Test fun `Instant should return JSONString`() {
        val str = "2019-04-25T21:01:09.456Z"
        val inst = Instant.parse(str)
        expect(JSONString(str)) { JSONSerializer.serialize(inst) }
    }

    @Test fun `LocalDate should return JSONString`() {
        val date = LocalDate.of(2019, 4, 25)
        expect(JSONString("2019-04-25")) { JSONSerializer.serialize(date) }
    }

    @Test fun `LocalDateTime should return JSONString`() {
        val date = LocalDateTime.of(2019, 4, 25, 21, 6, 5)
        expect(JSONString("2019-04-25T21:06:05")) { JSONSerializer.serialize(date) }
    }

    @Test fun `LocalTime should return JSONString`() {
        val date = LocalTime.of(21, 6, 5)
        expect(JSONString("21:06:05")) { JSONSerializer.serialize(date) }
    }

    @Test fun `OffsetTime should return JSONString`() {
        val time = OffsetTime.of(21, 6, 5, 456000000, ZoneOffset.ofHours(10))
        expect(JSONString("21:06:05.456+10:00")) { JSONSerializer.serialize(time) }
    }

    @Test fun `OffsetDateTime should return JSONString`() {
        val time = OffsetDateTime.of(2019, 4, 25, 21, 6, 5, 456000000, ZoneOffset.ofHours(10))
        expect(JSONString("2019-04-25T21:06:05.456+10:00")) { JSONSerializer.serialize(time) }
    }

    @Test fun `ZonedDateTime should return JSONString`() {
        val zdt = ZonedDateTime.of(2019, 4, 25, 21, 16, 23, 123000000, ZoneId.of("Australia/Sydney"))
        expect(JSONString("2019-04-25T21:16:23.123+10:00[Australia/Sydney]")) { JSONSerializer.serialize(zdt) }
    }

    @Test fun `Year should return JSONString`() {
        val year = Year.of(2019)
        expect(JSONString("2019")) { JSONSerializer.serialize(year) }
    }

    @Test fun `YearMonth should return JSONString`() {
        val yearMonth = YearMonth.of(2019, 4)
        expect(JSONString("2019-04")) { JSONSerializer.serialize(yearMonth) }
    }

    @Test fun `MonthDay should return JSONString`() {
        val month = MonthDay.of(4, 23)
        expect(JSONString("--04-23")) { JSONSerializer.serialize(month) }
    }

    @Test fun `Duration should return JSONString`() {
        val duration = Duration.ofHours(2)
        expect(JSONString("PT2H")) { JSONSerializer.serialize(duration) }
    }

    @Test fun `Period should return JSONString`() {
        val period = Period.ofMonths(3)
        expect(JSONString("P3M")) { JSONSerializer.serialize(period) }
    }

    @Test fun `UUID should return JSONString`() {
        val uuidString = "12ce3730-2d97-11e7-aeed-67b0e6bf0ed7"
        val uuid = UUID.fromString(uuidString)
        expect(JSONString(uuidString)) { JSONSerializer.serialize(uuid) }
    }

    @Test fun `URI should return JSONString`() {
        val uriString = "http://pwall.net"
        val uri = URI(uriString)
        expect(JSONString(uriString)) { JSONSerializer.serialize(uri) }
    }

    @Test fun `URL should return JSONString`() {
        val urlString = "http://pwall.net"
        val url = URL(urlString)
        expect(JSONString(urlString)) { JSONSerializer.serialize(url) }
    }

    @Test fun `BigInteger should return JSONLong`() {
        val bigIntLong = 123456789012345678L
        val bigInteger = BigInteger.valueOf(bigIntLong)
        expect(JSONLong(bigIntLong)) { JSONSerializer.serialize(bigInteger) }
    }

    @Test fun `BigInteger should return JSONString when config option selected`() {
        val bigIntString = "123456789012345678"
        val bigInteger = BigInteger(bigIntString)
        val config = JSONConfig().apply {
            bigIntegerString = true
        }
        expect(JSONString(bigIntString)) { JSONSerializer.serialize(bigInteger, config) }
    }

    @Test fun `BigDecimal should return JSONDecimal`() {
        val bigDecString = "12345678901234567890.88888"
        val bigDecimal = BigDecimal(bigDecString)
        expect(JSONDecimal(bigDecString)) { JSONSerializer.serialize(bigDecimal) }
    }

    @Test fun `BigDecimal should return JSONString when config option selected`() {
        val bigDecString = "12345678901234567890.88888"
        val bigDecimal = BigDecimal(bigDecString)
        val config = JSONConfig().apply {
            bigDecimalString = true
        }
        expect(JSONString(bigDecString)) { JSONSerializer.serialize(bigDecimal, config) }
    }

    @Test fun `BitSet should return JSONArray`() {
        val bitSet = BitSet(4)
        bitSet.set(1)
        bitSet.set(3)
        val expected = JSONArray().apply {
            addValue(1)
            addValue(3)
        }
        expect(expected) { JSONSerializer.serialize(bitSet) }
    }

    @Test fun `Simple data class should return JSONObject`() {
        val obj = Dummy1("abc", 123)
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Simple data class with extra property should return JSONObject`() {
        val obj = Dummy2("abc", 123)
        obj.extra = "qqqqq"
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
            putValue("extra", "qqqqq")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Simple data class with optional field should omit null field`() {
        val obj = Dummy2("abc", 123)
        obj.extra = null
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Simple data class with optional field should include null field when config set`() {
        val obj = Dummy2("abc", 123)
        obj.extra = null
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("field2", 123)
            putNull("extra")
        }
        val config = JSONConfig().apply {
            includeNulls = true
        }
        expect(expected) { JSONSerializer.serialize(obj, config) }
    }

    @Test fun `Derived class should return JSONObject`() {
        val obj = Derived()
        obj.field1 = "qwerty"
        obj.field2 = 98765
        obj.field3 = 0.012
        val expected = JSONObject().apply {
            putValue("field1", "qwerty")
            putValue("field2", 98765)
            putValue("field3", 0.012)
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Annotated class should return JSONObject using specified name`() {
        val obj = DummyWithNameAnnotation()
        obj.field1 = "qwerty"
        obj.field2 = 98765
        val expected = JSONObject().apply {
            putValue("field1", "qwerty")
            putValue("fieldX", 98765)
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Annotated data class should return JSONObject using specified name`() {
        val obj = DummyWithParamNameAnnotation("abc", 123)
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("fieldX", 123)
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Annotated data class with custom annotation should return JSONObject using specified name`() {
        val obj = DummyWithCustomNameAnnotation("abc", 123)
        val config = JSONConfig().apply {
            addNameAnnotation(CustomName::class, "symbol")
        }
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("fieldX", 123)
        }
        expect(expected) { JSONSerializer.serialize(obj, config) }
    }

    @Test fun `Nested class should return nested JSONObject`() {
        val obj1 = Dummy1("asdfg", 987)
        val obj3 = Dummy3(obj1, "what?")
        val expected1 = JSONObject().apply {
            putValue("field1", "asdfg")
            putValue("field2", 987)
        }
        val expected = JSONObject().apply {
            put("dummy1", expected1)
            putValue("text", "what?")
        }
        expect(expected) { JSONSerializer.serialize(obj3) }
    }

    @Test fun `Class with @JSONIgnore should return JSONObject skipping field`() {
        val obj = DummyWithIgnore("alpha", "beta", "gamma")
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Class with custom ignore annotation should return JSONObject skipping field`() {
        val obj = DummyWithCustomIgnore("alpha", "beta", "gamma")
        val config = JSONConfig().apply {
            addIgnoreAnnotation(CustomIgnore::class)
        }
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj, config) }
    }

    @Test fun `Class with @JSONIncludeIfNull should include null field`() {
        val obj = DummyWithIncludeIfNull("alpha", null, "gamma")
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putNull("field2")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Class with custom include if null annotation should include null field`() {
        val obj = DummyWithCustomIncludeIfNull("alpha", null, "gamma")
        val config = JSONConfig().apply {
            addIncludeIfNullAnnotation(CustomIncludeIfNull::class)
        }
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putNull("field2")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj, config) }
    }

    @Test fun `Class with @JSONIncludeAllProperties should include null field`() {
        val obj = DummyWithIncludeAllProperties("alpha", null, "gamma")
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putNull("field2")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Class with custom include all properties annotation should include null field`() {
        val obj = DummyWithCustomIncludeAllProperties("alpha", null, "gamma")
        val config = JSONConfig().apply {
            addIncludeAllPropertiesAnnotation(CustomIncludeAllProperties::class)
        }
        val expected = JSONObject().apply {
            putValue("field1", "alpha")
            putNull("field2")
            putValue("field3", "gamma")
        }
        expect(expected) { JSONSerializer.serialize(obj, config) }
    }

    @Test fun `Pair should return JSONArray`() {
        val pair = "xyz" to "abc"
        val expected = JSONArray().apply {
            add(JSONString("xyz"))
            add(JSONString("abc"))
        }
        expect(expected) { JSONSerializer.serialize(pair) }
    }

    @Test fun `Triple should return JSONArray`() {
        val triple = Triple("xyz","abc","def")
        val expected = JSONArray().apply {
            add(JSONString("xyz"))
            add(JSONString("abc"))
            add(JSONString("def"))
        }
        expect(expected) { JSONSerializer.serialize(triple) }
    }

    @Test fun `Heterogenous Triple should return JSONArray`() {
        val triple = Triple("xyz",88,"def")
        val expected = JSONArray().apply {
            add(JSONString("xyz"))
            add(JSONInt(88))
            add(JSONString("def"))
        }
        expect(expected) { JSONSerializer.serialize(triple) }
    }

    @Test fun `object should return JSONObject()`() {
        val obj = DummyObject
        expect(JSONObject().putValue("field1", "abc")) { JSONSerializer.serialize(obj) }
    }

    @Test fun `nested object should return JSONObject()`() {
        val obj = NestedDummy()
        val nested = JSONObject().putValue("field1", "abc")
        expect(JSONObject().putJSON("obj", nested)) { JSONSerializer.serialize(obj) }
    }

    @Test fun `class with constant val should serialize correctly`() {
        val constClass = DummyWithVal()
        expect(JSONObject().putValue("field8", "blert")) { JSONSerializer.serialize(constClass) }
    }

    @Test fun `java class should serialize correctly`() {
        val javaClass1 = JavaClass1(1234, "Hello!")
        val expected = JSONObject().apply {
            putValue("field1", 1234)
            putValue("field2", "Hello!")
        }
        expect(expected) { JSONSerializer.serialize(javaClass1) }
    }

    @Test fun `List derived class should serialize to JSONArray`() {
        val obj = DummyList(listOf(LocalDate.of(2019, 10, 6), LocalDate.of(2019, 10, 5)))
        val expected = JSONArray().apply {
            addValue("2019-10-06")
            addValue("2019-10-05")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `Map derived class should serialize to JSONObject`() {
        val obj = DummyMap(emptyMap()).apply {
            put("aaa", LocalDate.of(2019, 10, 6))
            put("bbb", LocalDate.of(2019, 10, 5))
        }
        val expected = JSONObject().apply {
            putValue("aaa", "2019-10-06")
            putValue("bbb", "2019-10-05")
        }
        expect(expected) { JSONSerializer.serialize(obj) }
    }

    @Test fun `sealed class should serialize with extra member to indicate derived class`() {
        val expected = JSONObject().apply {
            putValue("class", "Const")
            putValue("number", 2.0)
        }
        expect(expected) { JSONSerializer.serialize(Const(2.0)) }
    }

    @Test fun `sealed class object should serialize correctly`() {
        val expected = JSONObject().apply {
            putValue("class", "NotANumber")
        }
        expect(expected) { JSONSerializer.serialize(NotANumber) }
    }

    @Test fun `sealed class should serialize with custom discriminator`() {
        val config = JSONConfig().apply {
            sealedClassDiscriminator = "?"
        }
        val expected = JSONObject().apply {
            putValue("?", "Const")
            putValue("number", 2.0)
        }
        expect(expected) { JSONSerializer.serialize(Const(2.0), config) }
    }

    @Test fun `should fail on use of circular reference`() {
        val circular1 = Circular1()
        val circular2 = Circular2()
        circular1.ref = circular2
        circular2.ref = circular1
        val exception = assertFailsWith<JSONException> {
            JSONSerializer.serialize(circular1)
        }
        expect("Circular reference: field ref in Circular2") { exception.message }
    }

    @Test fun `should omit null members`() {
        val dummy5 = Dummy5(null, 123)
        val serialized = JSONSerializer.serialize(dummy5)
        expect(true) { serialized is JSONObject }
        expect(1) { (serialized as JSONObject).size }
    }

    @Test fun `should serialize Java Stream of strings`() {
        val stream = Stream.of("abc", "def")
        val serialized = JSONSerializer.serialize(stream)
        expect(true) { serialized is JSONSequence<*> }
        with(serialized as JSONSequence<*>) {
            expect(2) { size }
            expect(JSONString("abc")) { get(0) }
            expect(JSONString("def")) { get(1) }
        }
    }

    @Test fun `should serialize Java IntStream`() {
        val stream = IntStream.of(987, 654, 321)
        val serialized = JSONSerializer.serialize(stream)
        expect(true) { serialized is JSONSequence<*> }
        with(serialized as JSONSequence<*>) {
            expect(3) { size }
            expect(JSONInt(987)) { get(0) }
            expect(JSONInt(654)) { get(1) }
            expect(JSONInt(321)) { get(2) }
        }
    }

    private fun intEquals(a: Int, b: Int): Boolean {
        return a == b
    }

    private fun longEquals(a: Long, b: Long): Boolean {
        return a == b
    }

    private fun floatEquals(a: Float, b: Float): Boolean {
        return abs(a - b) < 0.0000001
    }

    private fun doubleEquals(a: Double, b: Double): Boolean {
        return abs(a - b) < 0.000000001
    }

}
