/*
 * @(#) JSONConfigTest.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020, 2021 Peter Wall
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.expect
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class JSONConfigTest {

    private val stringType = String::class.createType()

    @Test fun `add fromJSON mapping should return the function`() {
        val config = JSONConfig()
        expect(8192) { config.readBufferSize }
        expect(Charsets.UTF_8) { config.charset }
        assertNull(config.findFromJSONMapping(stringType))
        assertNull(config.findFromJSONMapping(String::class))
        config.fromJSON { json -> json?.toString() }
        assertNotNull(config.findFromJSONMapping(stringType))
        assertNotNull(config.findFromJSONMapping(String::class))
    }

    @Test fun `fromJSON mapping should map simple data class`() {
        val config = JSONConfig().apply {
            fromJSON { json ->
                if (json !is JSONObject)
                    fail("Must be JSONObject")
                Dummy1(json.getString("a"), json.getInt("b"))
            }
        }
        val json = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        expect(Dummy1("xyz", 888)) { JSONDeserializer.deserialize(Dummy1::class.createType(), json, config) }
    }

    @Test fun `fromJSON mapping should not interfere with other deserialization`() {
        val config = JSONConfig().apply {
            fromJSON { json ->
                if (json !is JSONObject)
                    fail("Must be JSONObject")
                Dummy1(json.getString("a"), json.getInt("b"))
            }
        }
        val json = JSONArray(JSONString("AAA"), JSONString("BBB"))
        val result = JSONDeserializer.deserialize<List<Any>>(json, config)
        expect(listOf("AAA", "BBB")) { result }
    }

    @Test fun `fromJSON mapping should map nested class`() {
        val config = JSONConfig().apply {
            fromJSON { json ->
                if (json !is JSONObject)
                    fail("Must be JSONObject")
                Dummy1(json.getString("a"), json.getInt("b"))
            }
        }
        val json1 = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        val json2 = JSONObject().apply {
            put("dummy1", json1)
            putValue("text", "Hello!")
        }
        expect(Dummy3(Dummy1("xyz", 888), "Hello!")) {
            JSONDeserializer.deserialize(Dummy3::class.createType(), json2, config)
        }
    }

    @Test fun `add toJSON mapping should return the function`() {
        val config = JSONConfig()
        assertNull(config.findToJSONMapping(stringType))
        assertNull(config.findToJSONMapping(String::class))
        config.toJSON<String> { str -> JSONString(str ?: fail("String expected")) }
        assertNotNull(config.findToJSONMapping(stringType))
        assertNotNull(config.findToJSONMapping(String::class))
    }

    @Test fun `toJSON mapping should map simple data class`() {
        val config = JSONConfig().apply {
            toJSON<Dummy1> { obj ->
                obj?.let {
                    JSONObject().apply {
                        putValue("a", it.field1)
                        putValue("b", it.field2)
                    }
                }
            }
        }
        val expected = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        expect(expected) { JSONSerializer.serialize(Dummy1("xyz", 888), config) }
    }

    @Test fun `toJSON mapping should map nested class`() {
        val config = JSONConfig().apply {
            toJSON<Dummy1> { obj ->
                obj?.let {
                    JSONObject().apply {
                        putValue("a", it.field1)
                        putValue("b", it.field2)
                    }
                }
            }
        }
        val dummy1 = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        val expected = JSONObject().apply {
            putJSON("dummy1", dummy1)
            putValue("text", "Hi there!")
        }
        expect(expected) { JSONSerializer.serialize(Dummy3(Dummy1("xyz", 888), "Hi there!"), config) }
    }

    @Test fun `toJSON mapping should be transferred on combineMappings`() {
        val config = JSONConfig().apply {
            toJSON<Dummy1> { obj ->
                obj?.let {
                    JSONObject().apply {
                        putValue("a", it.field1)
                        putValue("b", it.field2)
                    }
                }
            }
        }
        val config2 = JSONConfig().apply {
            combineMappings(config)
        }
        val expected = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        expect(expected) { JSONSerializer.serialize(Dummy1("xyz", 888), config2) }
    }

    @Test fun `toJSON mapping should be transferred on combineAll`() {
        val config = JSONConfig().apply {
            toJSON<Dummy1> { obj ->
                obj?.let {
                    JSONObject().apply {
                        putValue("a", it.field1)
                        putValue("b", it.field2)
                    }
                }
            }
        }
        val config2 = JSONConfig().apply {
            combineAll(config)
        }
        val expected = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        expect(expected) { JSONSerializer.serialize(Dummy1("xyz", 888), config2) }
    }

    @Test fun `JSONName annotation should be transferred on combineAll`() {
        val obj = DummyWithCustomNameAnnotation("abc", 123)
        val config = JSONConfig().apply {
            addNameAnnotation(CustomName::class, "symbol")
        }
        val config2 = JSONConfig().apply {
            combineAll(config)
        }
        val expected = JSONObject().apply {
            putValue("field1", "abc")
            putValue("fieldX", 123)
        }
        expect(expected) { JSONSerializer.serialize(obj, config2) }
    }

    @Test fun `Switch settings and numeric values should be transferred on combineAll`() {
        val config = JSONConfig().apply {
            bigIntegerString = true
            bigDecimalString = true
            includeNulls = true
            allowExtra = true
            streamOutput = true
            readBufferSize = 16384
            stringifyInitialSize = 512
        }
        val config2 = JSONConfig().apply {
            combineAll(config)
        }
        expect(true) { config2.bigIntegerString }
        expect(true) { config2.bigDecimalString }
        expect(true) { config2.includeNulls }
        expect(true) { config2.allowExtra }
        expect(true) { config2.streamOutput }
        expect(16384) { config2.readBufferSize }
        expect(512) { config2.stringifyInitialSize }
    }

    @Test fun `toJSON mapping of nullable type should be selected correctly`() {
        val config = JSONConfig().apply {
            toJSON(Dummy1::class.createType(nullable = true)) { JSONString("A") }
        }
        expect(JSONString("A")) { JSONSerializer.serialize(Dummy1("X", 0), config) }
    }

    @Test fun `toJSON mapping of non-nullable type should be selected correctly`() {
        val config = JSONConfig().apply {
            toJSON(Dummy1::class.createType(nullable = false)) { JSONString("A") }
        }
        expect(JSONString("A")) { JSONSerializer.serialize(Dummy1("X", 0), config) }
    }

    @Test fun `toJSON mapping should select correct function among derived classes`() {
        val config = JSONConfig().apply {
            toJSON<DummyA> { JSONString("A") }
            toJSON<DummyB> { JSONString("B") }
            toJSON<DummyC> { JSONString("C") }
            toJSON<DummyD> { JSONString("D") }
        }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("D")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `toJSON mapping should select correct function when order is reversed`() {
        val config = JSONConfig().apply {
            toJSON<DummyD> { JSONString("D") }
            toJSON<DummyC> { JSONString("C") }
            toJSON<DummyB> { JSONString("B") }
            toJSON<DummyA> { JSONString("A") }
        }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("D")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `toJSON mapping should select correct function when exact match not present`() {
        val config = JSONConfig().apply {
            toJSON<DummyA> { JSONString("A") }
            toJSON<DummyB> { JSONString("B") }
            toJSON<DummyC> { JSONString("C") }
        }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `fromJSON mapping should select correct function among derived classes`() {
        val config = JSONConfig().apply {
            fromJSON { if (it == JSONString("A")) DummyA() else fail() }
            fromJSON { if (it == JSONString("B")) DummyB() else fail() }
            fromJSON { if (it == JSONString("C")) DummyC() else fail() }
            fromJSON { if (it == JSONString("D")) DummyD() else fail() }
        }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("A"), config) is DummyA)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `fromJSON mapping should select correct function when order is reversed`() {
        val config = JSONConfig().apply {
            fromJSON { if (it == JSONString("D")) DummyD() else fail() }
            fromJSON { if (it == JSONString("C")) DummyC() else fail() }
            fromJSON { if (it == JSONString("B")) DummyB() else fail() }
            fromJSON { if (it == JSONString("A")) DummyA() else fail() }
        }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("A"), config) is DummyA)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `fromJSON mapping should select correct function when exact match not present`() {
        val config = JSONConfig().apply {
            fromJSON { if (it == JSONString("B")) DummyB() else fail() }
            fromJSON { if (it == JSONString("C")) DummyC() else fail() }
            fromJSON { if (it == JSONString("D")) DummyD() else fail() }
        }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `toJSON mapping with JSONConfig toJSONString should use toString`() {
        val config = JSONConfig().apply {
            toJSONString<Dummy9>()
        }
        expect(JSONString("abcdef")) { JSONSerializer.serialize(Dummy9("abcdef"), config) }
    }

    @Test fun `fromJSON mapping with JSONConfig fromJSONString should use String constructor`() {
        val config = JSONConfig().apply {
            fromJSONString<Dummy9>()
        }
        expect(Dummy9("abcdef")) { JSONDeserializer.deserialize<Dummy9>(JSONString("abcdef"), config) }
    }

    @Test fun `multiple JSONConfig mappings using apply should work correctly`() {
        val config = JSONConfig().apply {
            toJSON<Dummy1> { obj ->
                obj?.let {
                    JSONObject().apply {
                        putValue("a", it.field1)
                        putValue("b", it.field2)
                    }
                }
            }
            fromJSON { json ->
                require(json is JSONObject) { "Must be JSONObject" }
                Dummy1(json.getString("a"), json.getInt("b"))
            }
            toJSON<Dummy3> { obj ->
                obj?.let {
                    JSONObject().let { json ->
                        json["dummy1"] = JSONSerializer.serialize(it.dummy1, this)
                        json.putValue("text", it.text)
                    }
                }
            }
            fromJSON { json ->
                require(json is JSONObject) { "Must be JSONObject" }
                Dummy3(JSONDeserializer.deserialize(json.getObject("dummy1"), this) ?: fail(), json.getString("text"))
            }
        }
        val json1 = JSONObject().apply {
            putValue("a", "xyz")
            putValue("b", 888)
        }
        val dummy1 = Dummy1("xyz", 888)
        expect(json1) { JSONSerializer.serialize(dummy1, config) }
        expect(dummy1) { JSONDeserializer.deserialize<Dummy1>(json1, config) }
        val json3 = JSONObject().apply {
            put("dummy1", json1)
            putValue("text", "excellent")
        }
        val dummy3 = Dummy3(dummy1, "excellent")
        expect(json3) { JSONSerializer.serialize(dummy3, config) }
        expect(dummy3) { JSONDeserializer.deserialize<Dummy3>(json3, config) }
    }

}
