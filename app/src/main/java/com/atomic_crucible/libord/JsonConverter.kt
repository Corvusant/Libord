package com.atomic_crucible.libord

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.Some
import com.atomic_crucible.libord.optional.fromNullable
import com.atomic_crucible.libord.optional.getOrElse
import com.atomic_crucible.libord.optional.map
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object JsonConverter {

    private var gson = GsonBuilder()
        .registerTypeAdapter(Entry::class.java, EntryDeserializer())
        .create()

    fun <A> toJson (item:A) : String {
        return gson.toJson(item)
    }

    fun <A> fromJson(json: String, typeToken: Type) : A {
        return gson.fromJson<A>(json, typeToken)
    }
}

class EntryDeserializer : JsonDeserializer<Entry>{
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Entry
    {
        val jsonObject = fromNullable(json.asJsonObject)
        return jsonObject.map {
            val value = it.get("value").asString

            val categoriesJson = it.getAsJsonArray("categories") ?: JsonArray()
            val categories =categoriesJson.mapNotNull { c ->
                try {
                    context.deserialize<Category>(c, Category::class.java)
                } catch (e: Exception) {
                    null
                }
            }

            val entryType = try {
                when(val entryTypeStr = it.get("entryType")?.asString)
                {
                    null ->  EntryType.Noun
                    else -> EntryType.valueOf(entryTypeStr)
                }
            } catch (e: Exception) {
                EntryType.None// Use a default if enum is missing or invalid
            }

            val article: Optional<Article> = if (it.has("article") && !it.get("article").isJsonNull) {
                try {
                    val articleObj = context.deserialize<Article>(it.get("article"), Article::class.java)
                    if (articleObj.value != null) { // intellij suggests this can never be null, this is a lie
                        Some(articleObj)
                    }
                    else
                    {
                        None
                    }
                } catch (e: Exception) {
                    None
                }
            } else {
                None
            }

            Entry(
                value,
                categories,
                entryType,
                article)
        }
        .getOrElse { ErrorEntry} // find a better fallback here but in case we deserialize garbage we need to know
    }
}