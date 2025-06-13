package com.atomic_crucible.libord.optional.serialization

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.Some
import com.atomic_crucible.libord.optional.flatten
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun <A> JsonObject.getOptional(
    jsonEntry : String,
    context: JsonDeserializationContext,
    type: Type
) : Optional<A> =
    if (has(jsonEntry) && !get(jsonEntry).isJsonNull)
    {
        try {
            val item = get(jsonEntry)
            context.deserialize<Optional<A>>(item, type)
        }
        catch (e: Exception) {
            None
        }
    }
    else
    {
        None
    }

@Suppress("UNCHECKED_CAST")
class OptionalTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (Optional::class.java.isAssignableFrom(rawType)) {
            val typeArg = (type.type as ParameterizedType).actualTypeArguments[0]
            val elementAdapter = gson.getAdapter(TypeToken.get(typeArg)) as TypeAdapter<*>
            val adapter = OptionalTypeAdapter(elementAdapter)
            return adapter as TypeAdapter<T>
        }
        return null
    }
}

class OptionalTypeAdapter<T>(
    private val elementTypeAdapter: TypeAdapter<T>
) : TypeAdapter<Optional<T>>() {

    override fun write(out: JsonWriter, value: Optional<T>?)
    {
        if(value == null)
        {
            out.beginObject()
            out.name("type").value("None")
            out.endObject()
            return
        }

        value.flatten(
            {
                out.beginObject()
                out.name("type").value("Some")
                out.name("value")
                elementTypeAdapter.write(out, it)
                out.endObject()
            },
            {
                out.beginObject()
                out.name("type").value("None")
                out.endObject()
            })
    }

    override fun read(reader: JsonReader): Optional<T>
    {
        var type: String? = null
        var value: T? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "value" -> value = elementTypeAdapter.read(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (type) {
            "Some" -> Some(value!!)
            "None" -> None
            else -> throw JsonParseException("Unknown type: $type")
        }
    }
}