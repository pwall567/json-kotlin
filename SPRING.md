# Spring and `json-kotlin`

## Default Spring Serialization and Deserialization

Many users will seek to use `json-kotlin` in conjunction with the
[Spring Framework](https://spring.io/projects/spring-framework).

If a class of the following form is present in the classpath scanned by Spring, it will be used by the framework as the
default JSON serialization and deserialization function for Spring Boot etc.

```kotlin
package my.example

import java.io.Reader
import java.io.Writer
import java.lang.reflect.Type

import org.springframework.http.MediaType
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter
import org.springframework.stereotype.Service

import net.pwall.json.JSONAuto
import net.pwall.json.JSONConfig
import net.pwall.json.JSONString
import net.pwall.json.JSONStringify.appendJSON

import my.custom.type.Money

@Service
class JSONConverter : AbstractJsonHttpMessageConverter() {

    override fun canRead(mediaType: MediaType?): Boolean {
        return mediaType != null && mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)
    }

    override fun canWrite(mediaType: MediaType?): Boolean {
        return mediaType != null && mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON)
    }

    override fun readInternal(resolvedType: Type, reader: Reader): Any? {
        return JSONAuto.parse(resolvedType, reader, config)
    }

    override fun writeInternal(o: Any, type: Type?, writer: Writer) {
        writer.appendJSON(o, config)
    }

    companion object {

        /**
         * JSONConfig containing specific configuration for this application.
         *
         * It is placed in a companion object to allow it to be used from other parts of the application, e.g.
         *     val event = jsonString.parseJSON<Event>(JSONConverter.config)
         */
        val config = JSONConfig().apply {

            allowExtra = true // example configuration setting

            toJSONString<Money>() // example custom serialization

            fromJSON { json -> // example custom deserialization
                require(json is JSONString) { "JSON representation of Money must be string" }
                Money.of(json.value)
            }

        }

    }

}
```

2021-01-03
