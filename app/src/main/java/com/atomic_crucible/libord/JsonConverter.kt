package com.atomic_crucible.libord

import com.google.gson.Gson
import java.lang.reflect.Type

object JsonConverter {

    private var gson = Gson()

    fun <A> toJson (item:A) : String {
        return gson.toJson(item)
    }

    fun <A> fromJson(json: String, typeToken: Type) : A {
        return gson.fromJson<A>(json, typeToken)
    }
}