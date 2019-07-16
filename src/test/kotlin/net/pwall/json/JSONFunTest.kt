/*
 * @(#) JSONFunTest.kt
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

class JSONFunTest {

    @Test fun `makeJSON with asJSON should create the same JSONObject as explicit creation`() {
        val expected = JSONObject().putValue("testString", "xyz").putValue("testInt", 12345)
        assertEquals(expected, makeJSON("testString" to "xyz".asJSON(), "testInt" to 12345.asJSON()))
    }

    @Test fun `makeJSON with toJSON should create the same JSONObject as explicit creation`() {
        val expected = JSONObject().putValue("testString", "xyz").putValue("testInt", 12345)
        assertEquals(expected, makeJSON("testString" isJSON "xyz", "testInt" isJSON 12345))
    }

    @Test fun `asJSON should correctly handle String`() {
        val testString = "testing..."
        val expected = JSONString(testString)
        assertEquals(expected, testString.asJSON())
    }

    @Test fun `asJSON should correctly handle Int`() {
        val testInt = 987654321
        val expected = JSONInt(testInt)
        assertEquals(expected, testInt.asJSON())
    }

    @Test fun `asJSON should correctly handle Long`() {
        val testLong = 1234567890123456789
        val expected = JSONLong(testLong)
        assertEquals(expected, testLong.asJSON())
    }

    @Test fun `asJSON should correctly handle Float`() {
        val testFloat = 3.14159F
        val expected = JSONFloat(testFloat)
        assertEquals(expected, testFloat.asJSON())
    }

    @Test fun `asJSON should correctly handle Double`() {
        val testDouble = 3.14159265358979323846
        val expected = JSONDouble(testDouble)
        assertEquals(expected, testDouble.asJSON())
    }

    @Test fun `asJSON should correctly handle Boolean`() {
        val testBoolean = true
        val expected = JSONBoolean(testBoolean)
        assertEquals(expected, testBoolean.asJSON())
    }

    @Test fun `value should correctly handle String`() {
        val testString = "what next?"
        val expected = Pair("testString", JSONString(testString))
        assertEquals(expected, "testString" isJSON testString)
    }

    @Test fun `value should correctly handle Int`() {
        val testInt = 987654321
        val expected = Pair("testInt", JSONInt(testInt))
        assertEquals(expected, "testInt" isJSON testInt)
    }

    @Test fun `value should correctly handle Long`() {
        val testLong = 1234567890123456789
        val expected = Pair("testLong", JSONLong(testLong))
        assertEquals(expected, "testLong" isJSON testLong)
    }

    @Test fun `value should correctly handle Float`() {
        val testFloat = 3.14159F
        val expected = Pair("testFloat", JSONFloat(testFloat))
        assertEquals(expected, "testFloat" isJSON testFloat)
    }

    @Test fun `value should correctly handle Double`() {
        val testDouble = 3.14159265358979323846
        val expected = Pair("testDouble", JSONDouble(testDouble))
        assertEquals(expected, "testDouble" isJSON testDouble)
    }

    @Test fun `value should correctly handle Boolean`() {
        val testBoolean = true
        val expected = Pair("testBoolean", JSONBoolean(testBoolean))
        assertEquals(expected, "testBoolean" isJSON testBoolean)
    }

    @Test fun `parseJSON should correctly parse string`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        val expected: Dummy1? = Dummy1("Hi there!", 888)
        assertEquals(expected, json.parseJSON())
    }

    @Test fun `parseJSON should correctly parse string using parameterised type`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        val expected = Dummy1("Hi there!", 888)
        val parsed = json.parseJSON<Dummy1>()
        assertEquals(expected, parsed)
    }

    @Test fun `stringify should work on any object`() {
        val dummy1 = Dummy1("Hi there!", 888)
        val expected = """{"field1":"Hi there!","field2":888}"""
        assertEquals(expected, dummy1.stringifyJSON())
    }

    @Test fun `stringify should work on null`() {
        val dummy1 = null
        val expected = "null"
        assertEquals(expected, dummy1.stringifyJSON())
    }

    @Test fun `asJSON should correctly handle arbitrary object`() {
        val dummy1 = Dummy1("Hi there!", 888)
        val expected = makeJSON("field1" isJSON "Hi there!", "field2" isJSON 888)
        assertEquals(expected, dummy1.asJSON())
    }

    @Test fun `asJSON should correctly handle null`() {
        val dummy1 = null
        assertNull(dummy1.asJSON())
    }

}
