/*
 * @(#) JSONDelegateTest.kt
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
import kotlin.test.expect

class JSONDelegateTest {

    @Test fun `JSONDelegate should parse simple type`() {
        val json = "1"
        val result by JSONDelegate<Int>(json)
        expect(1) { result }
    }

    @Test fun `JSONDelegate should parse complex type`() {
        val json = """["abc","def","ghi"]"""
        val result by JSONDelegate<List<String>>(json)
        expect(listOf("abc", "def", "ghi")) { result }
    }

    @Test fun `JSONDelegate should parse nested complex type`() {
        val json = """[[]]"""
        val result: List<List<String>> by JSONDelegate(json)
        expect(listOf(emptyList())) { result }
    }

    @Test fun `JSONDelegate should parse null`() {
        val json = "null"
        val result: String? by JSONDelegate(json)
        expect(null) { result }
    }

}
