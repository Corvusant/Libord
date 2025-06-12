package com.atomic_crucible.libord.serialization

import com.atomic_crucible.libord.optional.serialization.OptionalTypeAdapterFactory
import com.atomic_crucible.libord.types.Entry

import com.google.gson.GsonBuilder
import java.lang.reflect.Type

object JsonConverter {

    private var gson = GsonBuilder()
        .registerTypeAdapter(Entry::class.java, EntryDeserializer())
        .registerTypeAdapterFactory(OptionalTypeAdapterFactory())
        .create()

    fun <A> toJson (item:A) : String {
        return gson.toJson(item)
    }

    fun <A> fromJson(json: String, typeToken: Type) : A {
        return gson.fromJson<A>(json, typeToken)
    }
}