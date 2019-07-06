/*
 * @(#) JSONSerializerTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019 Peter Wall
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
import java.util.Enumeration
import java.util.TimeZone
import java.util.UUID

class JSONSerializerTest {

    @Test fun `null should return null`() {
        assertNull(JSONSerializer.serialize(null))
    }

    @Test fun `JSONValue should be returned as-is`() {
        val json = JSONInteger(12345)
        assertSame(json, JSONSerializer.serialize(json))
    }

    @Test fun `String should return JSONString`() {
        val str = "Hello JSON!"
        val expected = JSONString(str)
        assertEquals(expected, JSONSerializer.serialize(str))
    }

    @Test fun `StringBuilder should return JSONString`() {
        val str = "Hello JSON!"
        val sb = StringBuilder(str)
        val expected = JSONString(str)
        assertEquals(expected, JSONSerializer.serialize(sb))
    }

    @Test fun `Char should return JSONString`() {
        val char = 'Q'
        val expected = JSONString("Q")
        assertEquals(expected, JSONSerializer.serialize(char))
    }

    @Test fun `Int should return JSONInteger`() {
        val i = 123456
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInteger)
        assertTrue(intEquals(i, actual.get()))
        // Note - these assertions are complicated because JSONInteger.equals() returns true
        // for any comparison with another numeric JSON types where the values are equal
    }

    @Test fun `Int (negative) should return JSONInteger`() {
        val i = -8888
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInteger)
        assertTrue(intEquals(i, actual.get()))
    }

    @Test fun `Long should return JSONLong`() {
        val i = 12345678901234
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONLong)
        assertTrue(longEquals(i, actual.get()))
    }

    @Test fun `Long (negative) should return JSONLong`() {
        val i = -987654321987654321
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONLong)
        assertTrue(longEquals(i, actual.get()))
    }

    @Test fun `Short should return JSONInteger`() {
        val i: Short = 1234
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInteger)
        assertTrue(intEquals(i.toInt(), actual.get()))
    }

    @Test fun `Byte should return JSONInteger`() {
        val i: Byte = 123
        val actual = JSONSerializer.serialize(i)
        assertTrue(actual is JSONInteger)
        assertTrue(intEquals(i.toInt(), actual.get()))
    }

    @Test fun `Float should return JSONFloat`() {
        val f = 0.1234F
        val actual = JSONSerializer.serialize(f)
        assertTrue(actual is JSONFloat)
        assertTrue(floatEquals(f, actual.get()))
    }

    @Test fun `Float (negative) should return JSONFloat`() {
        val f = -88.987F
        val actual = JSONSerializer.serialize(f)
        assertTrue(actual is JSONFloat)
        assertTrue(floatEquals(f, actual.get()))
    }

    @Test fun `Double should return JSONDouble`() {
        val d = 987.654321
        val actual = JSONSerializer.serialize(d)
        assertTrue(actual is JSONDouble)
        assertTrue(doubleEquals(d, actual.get()))
    }

    @Test fun `Double (exponent notation) should return JSONDouble`() {
        val d = 1e40
        val actual = JSONSerializer.serialize(d)
        assertTrue(actual is JSONDouble)
        assertTrue(doubleEquals(d, actual.get()))
    }

    @Test fun `Boolean should return JSONBoolean`() {
        val t = true
        assertSame(JSONBoolean.TRUE, JSONSerializer.serialize(t))
        val f = false
        assertSame(JSONBoolean.FALSE, JSONSerializer.serialize(f))
    }

    @Test fun `CharArray should return JSONString`() {
        val ca = charArrayOf('H', 'e', 'l', 'l', 'o', '!')
        val expected = JSONString("Hello!")
        assertEquals(expected, JSONSerializer.serialize(ca))
    }

    @Test fun `Array of Char should return JSONString`() {
        val ca = arrayOf('H', 'e', 'l', 'l', 'o', '!')
        val expected = JSONString("Hello!")
        assertEquals(expected, JSONSerializer.serialize(ca))
    }

    @Test fun `Array of Int should return JSONArray`() {
        val array = arrayOf(123, 2345, 0, 999)
        val expected = JSONArray.create().addValue(123).addValue(2345).addValue(0).addValue(999)
        assertEquals(expected, JSONSerializer.serialize(array))
    }

    @Test fun `Array of Int? should return JSONArray`() {
        val array = arrayOf(123, null, 0, 999)
        val expected = JSONArray.create().addValue(123).addNull().addValue(0).addValue(999)
        assertEquals(expected, JSONSerializer.serialize(array))
    }

    @Test fun `Array of String should return JSONArray`() {
        val array = arrayOf("Hello", "Kotlin")
        val expected = JSONArray.create().addValue("Hello").addValue("Kotlin")
        assertEquals(expected, JSONSerializer.serialize(array))
    }

    @Test fun `Iterator of String should return JSONArray`() {
        val iterator = listOf("Hello", "Kotlin").iterator()
        val expected = JSONArray.create().addValue("Hello").addValue("Kotlin")
        assertEquals(expected, JSONSerializer.serialize(iterator))
    }

    @Test fun `Enumeration of String should return JSONArray`() {
        val list = listOf("Hello", "Kotlin")
        val eee = ListEnum(list)
         val expected = JSONArray.create().addValue("Hello").addValue("Kotlin")
        assertEquals(expected, JSONSerializer.serialize(eee))
    }

    class ListEnum<T>(private val list: List<T>) : Enumeration<T> {

        private var index = 0

        override fun hasMoreElements(): Boolean = index < list.size

        override fun nextElement(): T = if (hasMoreElements()) list[index++] else throw NoSuchElementException()

    }

    @Test fun `List of String should return JSONArray`() {
        val list = listOf("Hello", "Kotlin")
        val expected = JSONArray.create().addValue("Hello").addValue("Kotlin")
        assertEquals(expected, JSONSerializer.serialize(list))
    }

    @Test fun `Sequence of String should return JSONArray`() {
        val seq = listOf("Hello", "Kotlin").asSequence()
        val expected = JSONArray.create().addValue("Hello").addValue("Kotlin")
        assertEquals(expected, JSONSerializer.serialize(seq))
    }

    @Test fun `Map of String to Int should return JSONObject`() {
        val map = mapOf("abc" to 1, "def" to 4, "ghi" to 999)
        val expected = JSONObject.create().putValue("abc", 1).putValue("def", 4).putValue("ghi", 999)
        assertEquals(expected, JSONSerializer.serialize(map))
    }

    @Test fun `Map of String to String? should return JSONObject`() {
        val map = mapOf("abc" to "hello", "def" to null, "ghi" to "goodbye")
        val expected = JSONObject.create().putValue("abc", "hello").putNull("def").putValue("ghi", "goodbye")
        assertEquals(expected, JSONSerializer.serialize(map))
    }

    @Test fun `Class with toJSON() should serialize as JSONObject`() {
        val obj = DummyFromJSON(23)
        val expected = JSONObject.create().putValue("dec", "23").putValue("hex", "17")
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Enum should serialize as JSONString`() {
        val eee = DummyEnum.GAMMA
        val expected = JSONString("GAMMA")
        assertEquals(expected, JSONSerializer.serialize(eee))
    }

    @Test fun `Calendar should return JSONString`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, 3)
        cal.set(Calendar.DAY_OF_MONTH, 25)
        cal.set(Calendar.HOUR_OF_DAY, 18)
        cal.set(Calendar.MINUTE, 52)
        cal.set(Calendar.SECOND, 47)
        cal.set(Calendar.MILLISECOND, 123)
        cal.set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        val expected = JSONString("2019-04-25T18:52:47.123+10:00")
        assertEquals(expected, JSONSerializer.serialize(cal))
    }

    @Test fun `Date should return JSONString`() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2019)
        cal.set(Calendar.MONTH, 3)
        cal.set(Calendar.DAY_OF_MONTH, 25)
        cal.set(Calendar.HOUR_OF_DAY, 18)
        cal.set(Calendar.MINUTE, 52)
        cal.set(Calendar.SECOND, 47)
        cal.set(Calendar.MILLISECOND, 123)
        cal.set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        val date = cal.time
        val expected = JSONString("2019-04-25T18:52:47.123+10:00")
        assertEquals(expected, JSONSerializer.serialize(date))
    }

    @Test fun `Instant should return JSONString`() {
        val str = "2019-04-25T21:01:09.456Z"
        val inst = Instant.parse(str)
        val expected = JSONString(str)
        assertEquals(expected, JSONSerializer.serialize(inst))
    }

    @Test fun `LocalDate should return JSONString`() {
        val date = LocalDate.of(2019, 4, 25)
        val expected = JSONString("2019-04-25")
        assertEquals(expected, JSONSerializer.serialize(date))
    }

    @Test fun `LocalDateTime should return JSONString`() {
        val date = LocalDateTime.of(2019, 4, 25, 21, 6, 5)
        val expected = JSONString("2019-04-25T21:06:05")
        assertEquals(expected, JSONSerializer.serialize(date))
    }

    @Test fun `OffsetTime should return JSONString`() {
        val time = OffsetTime.of(21, 6, 5, 456000000, ZoneOffset.ofHours(10))
        val expected = JSONString("21:06:05.456+10:00")
        assertEquals(expected, JSONSerializer.serialize(time))
    }

    @Test fun `OffsetDateTime should return JSONString`() {
        val time = OffsetDateTime.of(2019, 4, 25, 21, 6, 5, 456000000, ZoneOffset.ofHours(10))
        val expected = JSONString("2019-04-25T21:06:05.456+10:00")
        assertEquals(expected, JSONSerializer.serialize(time))
    }

    @Test fun `ZonedDateTime should return JSONString`() {
        val zdt = ZonedDateTime.of(2019, 4, 25, 21, 16, 23, 123000000, ZoneId.of("Australia/Sydney"))
        val expected = JSONString("2019-04-25T21:16:23.123+10:00[Australia/Sydney]")
        assertEquals(expected, JSONSerializer.serialize(zdt))
    }

    @Test fun `Year should return JSONString`() {
        val year = Year.of(2019)
        val expected = JSONString("2019")
        assertEquals(expected, JSONSerializer.serialize(year))
    }

    @Test fun `YearMonth should return JSONString`() {
        val year = YearMonth.of(2019, 4)
        val expected = JSONString("2019-04")
        assertEquals(expected, JSONSerializer.serialize(year))
    }

    @Test fun `Duration should return JSONString`() {
        val duration = Duration.ofHours(2)
        val expected = JSONString("PT2H")
        assertEquals(expected, JSONSerializer.serialize(duration))
    }

    @Test fun `Period should return JSONString`() {
        val period = Period.ofMonths(3)
        val expected = JSONString("P3M")
        assertEquals(expected, JSONSerializer.serialize(period))
    }

    @Test fun `UUID should return JSONString`() {
        val uuidString = "12ce3730-2d97-11e7-aeed-67b0e6bf0ed7"
        val uuid = UUID.fromString(uuidString)
        val expected = JSONString(uuidString)
        assertEquals(expected, JSONSerializer.serialize(uuid))
    }

    @Test fun `BitSet should return JSONArray`() {
        val bitSet = BitSet(4)
        bitSet.set(1)
        bitSet.set(3)
        val expected = JSONArray().addValue(1).addValue(3)
        assertEquals(expected, JSONSerializer.serialize(bitSet))
    }

    @Test fun `Simple data class should return JSONObject`() {
        val obj = Dummy1("abc", 123)
        val expected = JSONObject().putValue("field1", "abc").putValue("field2", 123)
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Simple data class with extra property should return JSONObject`() {
        val obj = Dummy2("abc", 123)
        obj.extra = "qqqqq"
        val expected = JSONObject().putValue("field1", "abc").putValue("field2", 123).putValue("extra", "qqqqq")
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Derived class should return JSONObject`() {
        val obj = Derived()
        obj.field1 = "qwerty"
        obj.field2 = 98765
        obj.field3 = 0.012
        val expected = JSONObject().putValue("field1", "qwerty").putValue("field2", 98765).putValue("field3", 0.012)
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Annotated class should return JSONObject using specified name`() {
        val obj = DummyAnno()
        obj.field1 = "qwerty"
        obj.field2 = 98765
        val expected = JSONObject().putValue("field1", "qwerty").putValue("fieldX", 98765)
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Annotated data class should return JSONObject using specified name`() {
        val obj = DummyAnnoData("abc", 123)
        val expected = JSONObject().putValue("field1", "abc").putValue("fieldX", 123)
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Nested class should return nested JSONObject`() {
        val obj1 = Dummy1("asdfg", 987)
        val obj3 = Dummy3(obj1, "what?")
        val expected1 = JSONObject().putValue("field1", "asdfg").putValue("field2", 987)
        val expected = JSONObject().putJSON("dummy1", expected1).putValue("text", "what?")
        assertEquals(expected, JSONSerializer.serialize(obj3))
    }

    @Test fun `Class with @JSONIgnore should return nested JSONObject skipping field`() {
        val obj = Dummy5("alpha", "beta", "gamma")
        val expected = JSONObject().putValue("field1", "alpha").putValue("field3", "gamma")
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `Pair should return JSONArray`() {
        val pair = "xyz" to "abc"
        val expected = JSONArray().addJSON(JSONString("xyz")).addJSON(JSONString("abc"))
        assertEquals(expected, JSONSerializer.serialize(pair))
    }

    @Test fun `Triple should return JSONArray`() {
        val triple = Triple("xyz","abc","def")
        val expected = JSONArray().addJSON(JSONString("xyz")).addJSON(JSONString("abc")).addJSON(JSONString("def"))
        assertEquals(expected, JSONSerializer.serialize(triple))
    }

    @Test fun `Heterogenous Triple should return JSONArray`() {
        val triple = Triple("xyz",88,"def")
        val expected = JSONArray().addJSON(JSONString("xyz")).addJSON(JSONInteger(88)).addJSON(JSONString("def"))
        assertEquals(expected, JSONSerializer.serialize(triple))
    }

    @Test fun `object should return JSONObject()`() {
        val obj = DummyObject
        val expected = JSONObject().putValue("field1", "abc")
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `nested object should return JSONObject()`() {
        val obj = NestedDummy()
        val nested = JSONObject().putValue("field1", "abc")
        val expected = JSONObject().putJSON("obj", nested)
        assertEquals(expected, JSONSerializer.serialize(obj))
    }

    @Test fun `class with constant val should serialize correctly`() {
        val constClass = DummyWithVal()
        val expected = JSONObject().putValue("field8", "blert")
        assertEquals(expected, JSONSerializer.serialize(constClass))
    }

    @Test fun `java class should serialize correctly`() {
        val javaClass1 = JavaClass1(1234, "Hello!")
        val expected = JSONObject().putValue("field1", 1234).putValue("field2", "Hello!")
        assertEquals(expected, JSONSerializer.serialize(javaClass1))
    }

    private fun intEquals(a: Int, b: Int): Boolean {
        return a == b
    }

    private fun longEquals(a: Long, b: Long): Boolean {
        return a == b
    }

    private fun floatEquals(a: Float, b: Float): Boolean {
        return Math.abs(a - b) < 0.0000001
    }

    private fun doubleEquals(a: Double, b: Double): Boolean {
        return Math.abs(a - b) < 0.000000001
    }

}
