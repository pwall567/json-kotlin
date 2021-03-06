/*
 * @(#) JSONAutoTest.kt
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
import kotlin.test.expect
import kotlin.test.Test

import java.lang.reflect.Type

/**
 * Tests for [JSONAuto].  These tests are somewhat rudimentary because [JSONAuto] simply delegates requests to
 * [JSONSerializer] and [JSONDeserializer], and those classes are extensively tested separately.
 *
 * @author  Peter Wall
 */
class JSONAutoTest {

    @Test fun `JSONAuto should correctly stringify an object`() {
        val obj = Dummy1("abdef", 54321)
        val expected = JSONObject().apply {
            putValue("field1", "abdef")
            putValue("field2", 54321)
        }
        expect(expected) { JSON.parse(JSONAuto.stringify(obj)) }
    }

    @Test fun `JSONAuto should parse a string to create an object`() {
        val json = """{"field1":"abdef","field2":54321}"""
        val expected: Dummy1? = Dummy1("abdef", 54321)
        expect(expected) { JSONAuto.parse(json) }
    }

    @Test fun `JSONAuto should parse a string to create an object with explicit type`() {
        val json = """{"field1":"abdef","field2":54321}"""
        expect(Dummy1("abdef", 54321)) { JSONAuto.parse(Dummy1::class.starProjectedType, json) }
    }

    @Test fun `JSONAuto should parse List using parameterised type`() {
        val json = """[{"field1":"abcdef","field2":567},{"field1":"qwerty","field2":9999}]"""
        val expected = listOf(Dummy1("abcdef", 567), Dummy1("qwerty", 9999))
        expect(expected) { JSONAuto.parse<List<Dummy1>>(json) }
    }

    @Test fun `JSONAuto should parse List using Java Type`() {
        val json = """[{"field1":567,"field2":"abcdef"},{"field1":9999,"field2":"qwerty"}]"""
        val type: Type = JavaClass2::class.java.getField("field1").genericType
        expect(listOf(JavaClass1(567, "abcdef"), JavaClass1(9999, "qwerty"))) { JSONAuto.parse(type, json) }
    }

}
