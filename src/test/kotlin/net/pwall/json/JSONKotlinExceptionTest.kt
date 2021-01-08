/*
 * @(#) JSONKotlinExceptionTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2021 Peter Wall
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
import kotlin.test.assertFailsWith
import kotlin.test.expect

import net.pwall.json.JSONKotlinException.Companion.fail
import net.pwall.json.pointer.JSONPointer

class JSONKotlinExceptionTest {

    @Test fun `should create simple exception`() {
        val e = JSONKotlinException("Test message")
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(null) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should create exception with root pointer`() {
        val e = JSONKotlinException("Test message", JSONPointer.root)
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(JSONPointer.root) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should create exception with pointer`() {
        val e = JSONKotlinException("Test message", JSONPointer.root.child(0).child("ace"))
        expect("Test message") { e.text }
        expect("Test message at /0/ace") { e.message }
        expect(JSONPointer("/0/ace")) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should create exception with cause`() {
        val nested = JSONException("Nested")
        val e = JSONKotlinException("Test message", nested)
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(null) { e.pointer }
        expect(nested) { e.cause }
    }

    @Test fun `should create exception with pointer and cause`() {
        val nested = JSONException("Nested")
        val e = JSONKotlinException("Test message", JSONPointer.root.child(0).child("ace"), nested)
        expect("Test message") { e.text }
        expect("Test message at /0/ace") { e.message }
        expect(JSONPointer("/0/ace")) { e.pointer }
        expect(nested) { e.cause }
    }

    @Test fun `should throw simple exception`() {
        val e = assertFailsWith<JSONKotlinException> { fail("Test message") }
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(null) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should throw exception with root pointer`() {
        val e = assertFailsWith<JSONKotlinException> { fail("Test message", JSONPointer.root) }
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(JSONPointer.root) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should throw exception with pointer`() {
        val e = assertFailsWith<JSONKotlinException> { fail("Test message", JSONPointer.root.child(0).child("ace")) }
        expect("Test message") { e.text }
        expect("Test message at /0/ace") { e.message }
        expect(JSONPointer("/0/ace")) { e.pointer }
        expect(null) { e.cause }
    }

    @Test fun `should throw exception with cause`() {
        val nested = JSONException("Nested")
        val e = assertFailsWith<JSONKotlinException> { fail("Test message", nested) }
        expect("Test message") { e.text }
        expect("Test message") { e.message }
        expect(null) { e.pointer }
        expect(nested) { e.cause }
    }

    @Test fun `should throw exception with pointer and cause`() {
        val nested = JSONException("Nested")
        val e = assertFailsWith<JSONKotlinException> {
            fail("Test message", JSONPointer.root.child(0).child("ace"), nested)
        }
        expect("Test message") { e.text }
        expect("Test message at /0/ace") { e.message }
        expect(JSONPointer("/0/ace")) { e.pointer }
        expect(nested) { e.cause }
    }

}
