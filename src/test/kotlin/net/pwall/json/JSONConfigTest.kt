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
        val config = JSONConfig().fromJSON { json ->
            if (json !is JSONObject)
                throw JSONException("Must be JSONObject")
            Dummy1(json.getString("a"), json.getInt("b"))
        }
        val json = JSONObject().putValue("a", "xyz").putValue("b", 888)
        expect(Dummy1("xyz", 888)) { JSONDeserializer.deserialize(Dummy1::class.createType(), json, config) }
    }

    @Test fun `fromJSON mapping should map nested class`() {
        val config = JSONConfig().fromJSON { json ->
            if (json !is JSONObject)
                throw JSONException("Must be JSONObject")
            Dummy1(json.getString("a"), json.getInt("b"))
        }
        val json1 = JSONObject().putValue("a", "xyz").putValue("b", 888)
        val json2 = JSONObject().putJSON("dummy1", json1).putValue("text", "Hello!")
        expect(Dummy3(Dummy1("xyz", 888), "Hello!")) {
            JSONDeserializer.deserialize(Dummy3::class.createType(), json2, config)
        }
    }

    @Test fun `add toJSON mapping should return the function`() {
        val config = JSONConfig()
        assertNull(config.findToJSONMapping(stringType))
        assertNull(config.findToJSONMapping(String::class))
        config.toJSON<String> { str -> JSONString(str ?: throw JSONException("String expected")) }
        assertNotNull(config.findToJSONMapping(stringType))
        assertNotNull(config.findToJSONMapping(String::class))
    }

    @Test fun `toJSON mapping should map simple data class`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        expect(JSONObject().putValue("a", "xyz").putValue("b", 888)) {
            JSONSerializer.serialize(Dummy1("xyz", 888), config)
        }
    }

    @Test fun `toJSON mapping should map nested class`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        val dummy1 = JSONObject().putValue("a", "xyz").putValue("b", 888)
        expect(JSONObject().putJSON("dummy1", dummy1).putValue("text", "Hi there!")) {
            JSONSerializer.serialize(Dummy3(Dummy1("xyz", 888), "Hi there!"), config)
        }
    }

    @Test fun `toJSON mapping should be transferred on combineMappings`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        val config2 = JSONConfig().combineMappings(config)
        expect(JSONObject().putValue("a", "xyz").putValue("b", 888)) {
            JSONSerializer.serialize(Dummy1("xyz", 888), config2)
        }
    }

    @Test fun `toJSON mapping should be transferred on combineAll`() {
        val config = JSONConfig().toJSON<Dummy1> { obj ->
            obj?.let { JSONObject().putValue("a", it.field1).putValue("b", it.field2) }
        }
        val config2 = JSONConfig().combineAll(config)
        expect(JSONObject().putValue("a", "xyz").putValue("b", 888)) {
            JSONSerializer.serialize(Dummy1("xyz", 888), config2)
        }
    }

    @Test fun `JSONName annotation should be transferred on combineAll`() {
        val obj = DummyCustomAnnoData("abc", 123)
        val config = JSONConfig().addNameAnnotation(CustomName::class, "symbol")
        val config2 = JSONConfig().combineAll(config)
        expect(JSONObject().putValue("field1", "abc").putValue("fieldX", 123)) {
            JSONSerializer.serialize(obj, config2)
        }
    }

    @Test fun `toJSON mapping of nullable type should be selected correctly`() {
        val config = JSONConfig().toJSON(Dummy1::class.createType(nullable = true)) { JSONString("A") }
        expect(JSONString("A")) { JSONSerializer.serialize(Dummy1("X", 0), config) }
    }

    @Test fun `toJSON mapping of non-nullable type should be selected correctly`() {
        val config = JSONConfig().toJSON(Dummy1::class.createType(nullable = false)) { JSONString("A") }
        expect(JSONString("A")) { JSONSerializer.serialize(Dummy1("X", 0), config) }
    }

    @Test fun `toJSON mapping should select correct function among derived classes`() {
        val config = JSONConfig().toJSON<DummyA> { JSONString("A") }.toJSON<DummyB> { JSONString("B") }.
                toJSON<DummyC> { JSONString("C") }.toJSON<DummyD> { JSONString("D") }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("D")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `toJSON mapping should select correct function when order is reversed`() {
        val config = JSONConfig().toJSON<DummyD> { JSONString("D") }.toJSON<DummyC> { JSONString("C") }.
                toJSON<DummyB> { JSONString("B") }.toJSON<DummyA> { JSONString("A") }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("D")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `toJSON mapping should select correct function when exact match not present`() {
        val config = JSONConfig().toJSON<DummyA> { JSONString("A") }.toJSON<DummyB> { JSONString("B") }.
                toJSON<DummyC> { JSONString("C") }
        expect(JSONString("A")) { JSONSerializer.serialize(DummyA(), config)}
        expect(JSONString("B")) { JSONSerializer.serialize(DummyB(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyC(), config)}
        expect(JSONString("C")) { JSONSerializer.serialize(DummyD(), config)}
    }

    @Test fun `fromJSON mapping should select correct function among derived classes`() {
        val config = JSONConfig().fromJSON { if (it == JSONString("A")) DummyA() else fail() }.
                fromJSON { if (it == JSONString("B")) DummyB() else fail() }.
                fromJSON { if (it == JSONString("C")) DummyC() else fail() }.
                fromJSON { if (it == JSONString("D")) DummyD() else fail() }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("A"), config) is DummyA)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `fromJSON mapping should select correct function when order is reversed`() {
        val config = JSONConfig().fromJSON { if (it == JSONString("D")) DummyD() else fail() }.
                fromJSON { if (it == JSONString("C")) DummyC() else fail() }.
                fromJSON { if (it == JSONString("B")) DummyB() else fail() }.
                fromJSON { if (it == JSONString("A")) DummyA() else fail() }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("A"), config) is DummyA)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `fromJSON mapping should select correct function when exact match not present`() {
        val config = JSONConfig().fromJSON { if (it == JSONString("B")) DummyB() else fail() }.
                fromJSON { if (it == JSONString("C")) DummyC() else fail() }.
                fromJSON { if (it == JSONString("D")) DummyD() else fail() }
        assertTrue(JSONDeserializer.deserialize(DummyA::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyB::class.createType(), JSONString("B"), config) is DummyB)
        assertTrue(JSONDeserializer.deserialize(DummyC::class.createType(), JSONString("C"), config) is DummyC)
        assertTrue(JSONDeserializer.deserialize(DummyD::class.createType(), JSONString("D"), config) is DummyD)
    }

    @Test fun `toJSON mapping with JSONConfig toJSONString should use toString`() {
        val config = JSONConfig().toJSONString<Dummy9>()
        expect(JSONString("abcdef")) { JSONSerializer.serialize(Dummy9("abcdef"), config) }
    }

    @Test fun `fromJSON mapping with JSONConfig fromJSONString should use String constructor`() {
        val config = JSONConfig().fromJSONString<Dummy9>()
        expect(Dummy9("abcdef")) { JSONDeserializer.deserialize(JSONString("abcdef"), config) }
    }

}
