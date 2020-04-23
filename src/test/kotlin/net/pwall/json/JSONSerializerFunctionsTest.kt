/*
 * @(#) JSONSerializerFunctionsTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2020 Peter Wall
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.expect

import java.time.Instant
import java.util.Calendar

import net.pwall.json.JSONSerializerFunctions.findToJSON
import net.pwall.json.JSONSerializerFunctions.formatISO8601
import net.pwall.json.JSONSerializerFunctions.isSealedSubclass
import net.pwall.json.JSONSerializerFunctions.isToStringClass

class JSONSerializerFunctionsTest {

    @Test fun `should find toJSON when it is present`() {
        assertNotNull(DummyFromJSON::class.findToJSON())
    }

    @Test fun `should return null when toJSON is present`() {
        assertNull(Dummy1::class.findToJSON())
    }

    @Test fun `should return true when class is a subclass of a sealed class`() {
        assertTrue(NotANumber::class.isSealedSubclass())
    }

    @Test fun `should return false when class is not a subclass of a sealed class`() {
        assertFalse(Dummy1::class.isSealedSubclass())
    }

    @Test fun `should recognise a toString-able class`() {
        assertTrue(Instant::class.isToStringClass())
    }

    @Test fun `should recognise a not-toString-able class`() {
        assertFalse(Dummy1::class.isToStringClass())
    }

    @Test fun `should correctly format Calendar`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 3)
            set(Calendar.DAY_OF_MONTH, 23)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 25)
            set(Calendar.SECOND, 31)
            set(Calendar.MILLISECOND, 123)
            set(Calendar.ZONE_OFFSET, 10 * 60 * 60 * 1000)
        }
        expect("2020-04-23T19:25:31.123+10:00") { cal.formatISO8601() }
    }

}
