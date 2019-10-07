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

import kotlin.reflect.full.starProjectedType
import kotlin.test.assertNull
import kotlin.test.expect
import kotlin.test.Test

import java.lang.reflect.Type
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

class JSONFunTest {

    @Test fun `makeJSON with asJSONValue should create the same JSONObject as explicit creation`() {
        expect(JSONObject().putValue("testString", "xyz").putValue("testInt", 12345)) {
            makeJSON("testString" to "xyz".asJSONValue(), "testInt" to 12345.asJSONValue())
        }
    }

    @Test fun `makeJSON with isJSON should create the same JSONObject as explicit creation`() {
        expect(JSONObject().putValue("testString", "xyz").putValue("testInt", 12345)) {
            makeJSON("testString" isJSON "xyz", "testInt" isJSON 12345)
        }
    }

    @Test fun `asJSONValue should correctly handle String`() {
        val testString = "testing..."
        expect(JSONString(testString)) { testString.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Char`() {
        val testChar = 'Q'
        expect(JSONString("Q")) { testChar.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Int`() {
        val testInt = 987654321
        expect(JSONInt(testInt)) { testInt.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Long`() {
        val testLong = 1234567890123456789
        expect(JSONLong(testLong)) { testLong.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Short`() {
        val testShort: Short = 1234
        expect(JSONInt(testShort.toInt())) { testShort.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Byte`() {
        val testByte: Byte = 123
        expect(JSONInt(testByte.toInt())) { testByte.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Float`() {
        val testFloat = 3.14159F
        expect(JSONFloat(testFloat)) { testFloat.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Double`() {
        val testDouble = 3.14159265358979323846
        expect(JSONDouble(testDouble)) { testDouble.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle Boolean`() {
        val testBoolean = true
        expect(JSONBoolean(testBoolean)) { testBoolean.asJSONValue() }
    }

    @Test fun `isJSON should correctly handle String`() {
        val testString = "what next?"
        expect("testString" to JSONString(testString)) { "testString" isJSON testString }
    }

    @Test fun `isJSON should correctly handle Char`() {
        val testChar = 'Z'
        expect("testChar" to JSONString("Z")) { "testChar" isJSON testChar }
    }

    @Test fun `isJSON should correctly handle Int`() {
        val testInt = 987654321
        expect("testInt" to JSONInt(testInt)) { "testInt" isJSON testInt }
    }

    @Test fun `isJSON should correctly handle Long`() {
        val testLong = 1234567890123456789
        expect("testLong" to JSONLong(testLong)) { "testLong" isJSON testLong }
    }

    @Test fun `isJSON should correctly handle Short`() {
        val testShort: Short = 1234
        expect("testShort" to JSONInt(testShort.toInt())) { "testShort" isJSON testShort }
    }

    @Test fun `isJSON should correctly handle Byte`() {
        val testByte: Byte = 123
        expect("testByte" to JSONInt(testByte.toInt())) { "testByte" isJSON testByte }
    }

    @Test fun `isJSON should correctly handle Float`() {
        val testFloat = 3.14159F
        expect("testFloat" to JSONFloat(testFloat)) { "testFloat" isJSON testFloat }
    }

    @Test fun `isJSON should correctly handle Double`() {
        val testDouble = 3.14159265358979323846
        expect("testDouble" to JSONDouble(testDouble)) { "testDouble" isJSON testDouble }
    }

    @Test fun `isJSON should correctly handle Boolean`() {
        val testBoolean = true
        expect("testBoolean" to JSONBoolean(testBoolean)) { "testBoolean" isJSON testBoolean }
    }

    @Test fun `parseJSON should correctly parse string`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        val expected: Dummy1? = Dummy1("Hi there!", 888)
        expect(expected) { json.parseJSON() }
    }

    @Test fun `parseJSON should correctly parse string using parameterised type`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        expect(Dummy1("Hi there!", 888)) { json.parseJSON<Dummy1>() }
    }

    @Test fun `parseJSON should correctly parse string using explicit KClass`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        expect(Dummy1("Hi there!", 888)) { json.parseJSON(Dummy1::class) }
    }

    @Test fun `parseJSON should correctly parse string using explicit KType`() {
        val json = """{"field1":"Hi there!","field2":888}"""
        expect(Dummy1("Hi there!", 888)) { json.parseJSON(Dummy1::class.starProjectedType) }
    }

    @Test fun `stringify should work on any object`() {
        val dummy1 = Dummy1("Hi there!", 888)
        expect("""{"field1":"Hi there!","field2":888}""") { dummy1.stringifyJSON() }
    }

    @Test fun `stringify should work on null`() {
        val dummy1 = null
        expect("null") { dummy1.stringifyJSON() }
    }

    @Test fun `asJSONValue should correctly handle arbitrary object`() {
        val dummy1 = Dummy1("Hi there!", 888)
        expect(makeJSON("field1" isJSON "Hi there!", "field2" isJSON 888)) { dummy1.asJSONValue() }
    }

    @Test fun `asJSONValue should correctly handle null`() {
        val dummy1 = null
        assertNull(dummy1.asJSONValue())
    }

    @Test fun `targetKType should create correct type`() {
        val listStrings = listOf("abc", "def")
        val jsonArrayString = JSONArray().addValue("abc").addValue("def")
        expect(listStrings) { JSONDeserializer.deserialize(targetKType(List::class, String::class), jsonArrayString) }
    }

    @Test fun `toKType should convert simple class`() {
        val type: Type = JavaClass1::class.java
        expect(JavaClass1::class.starProjectedType) { type.toKType() }
    }

    @Test fun `toKType should convert parameterized class`() {
        val field = JavaClass2::class.java.getField("field1")
        val type: Type = field.genericType
        val expected = java.util.List::class.createType(
                listOf(KTypeProjection.invariant(JavaClass1::class.starProjectedType)))
        expect(expected) { type.toKType() }
    }

    @Test fun `toKType should convert parameterized class with extends`() {
        val field = JavaClass2::class.java.getField("field2")
        val type: Type = field.genericType
        val expected = java.util.List::class.createType(
                listOf(KTypeProjection.covariant(JavaClass1::class.starProjectedType)))
        expect(expected) { type.toKType() }
    }

    @Test fun `toKType should convert parameterized class with super`() {
        val field = JavaClass2::class.java.getField("field3")
        val type: Type = field.genericType
        val expected = java.util.List::class.createType(
                listOf(KTypeProjection.contravariant(JavaClass1::class.starProjectedType)))
        expect(expected) { type.toKType() }
    }

    @Test fun `toKType should convert nested parameterized class`() {
        val field = JavaClass2::class.java.getField("field4")
        val type: Type = field.genericType
        val expected = java.util.List::class.createType(
                listOf(KTypeProjection.invariant(java.util.List::class.createType(
                        listOf(KTypeProjection.invariant(JavaClass1::class.starProjectedType))))))
        expect(expected) { type.toKType() }
    }

}
