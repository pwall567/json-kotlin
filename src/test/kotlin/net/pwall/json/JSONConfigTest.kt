/*
 * @(#) JSONConfigTest.kt
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

import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JSONConfigTest {

    private val stringType = String::class.starProjectedType

    @Test fun `add fromJSON mapping should return the function`() {
        val config = JSONConfig()
        assertEquals(config.readBufferSize, 8192)
        assertNull(config.getFromJSONMapping(stringType))
        config.fromJSON { json -> json?.toString() }
        assertNotNull(config.getFromJSONMapping(stringType))
    }

    @Test fun `fromJSON mapping should map simple data class`() {
        val config = JSONConfig().fromJSON { json ->
            if (json !is JSONObject)
                throw JSONException("Must be JSONObject")
            Dummy1(json.getString("a"), json.getInt("b"))
        }
        val expected = Dummy1("xyz", 888)
        val json = JSONObject().putValue("a", "xyz").putValue("b", 888)
        assertEquals(expected, JSONDeserializer.deserialize(Dummy1::class.createType(), json, config))
    }

    @Test fun `fromJSON mapping should map nested class`() {
        val config = JSONConfig().fromJSON { json ->
            if (json !is JSONObject)
                throw JSONException("Must be JSONObject")
            Dummy1(json.getString("a"), json.getInt("b"))
        }
        val expected = Dummy3(Dummy1("xyz", 888), "Hello!")
        val json1 = JSONObject().putValue("a", "xyz").putValue("b", 888)
        val json2 = JSONObject().putJSON("dummy1", json1).putValue("text", "Hello!")
        assertEquals(expected, JSONDeserializer.deserialize(Dummy3::class.createType(), json2, config))
    }

    @Test fun `add toJSON mapping should return the function`() {
        val config = JSONConfig()
        assertNull(config.getToJSONMapping(stringType))
        config.toJSON<String> { str -> JSONString(str ?: throw JSONException("String expected")) }
        assertNotNull(config.getToJSONMapping(stringType))
    }

    @Test fun `toJSON mapping should map simple data class`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        val expected = JSONObject().putValue("a", "xyz").putValue("b", 888)
        assertEquals(expected, JSONSerializer.serialize(Dummy1("xyz", 888), config))
    }

    @Test fun `toJSON mapping should map nested class`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        val dummy1 = JSONObject().putValue("a", "xyz").putValue("b", 888)
        val expected = JSONObject().putJSON("dummy1", dummy1).putValue("text", "Hi there!")
        assertEquals(expected, JSONSerializer.serialize(Dummy3(Dummy1("xyz", 888), "Hi there!"), config))
    }

}
