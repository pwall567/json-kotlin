/*
 * @(#) JSONDeserializerFunctionsTest.kt
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

import java.util.UUID

class JSONDeserializerFunctionsTest {

    @Test fun `should create UUID`() {
        val s = "dbfaa208-ac99-11eb-aca1-d790d50d0f28"
        expect(UUID.fromString(s)) { JSONDeserializerFunctions.createUUID(s) }
    }

    @Test fun `should throw exception on invalid UUID`() {
        val s = "dbfaa208-ac99-11eb-aca1-d790d50d0f" // 2 bytes too short
        assertFailsWith<IllegalArgumentException> { JSONDeserializerFunctions.createUUID(s) }.let {
            expect("Not a valid UUID - dbfaa208-ac99-11eb-aca1-d790d50d0f") { it.message }
        }
    }

}
