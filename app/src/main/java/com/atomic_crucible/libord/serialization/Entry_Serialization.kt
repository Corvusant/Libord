package com.atomic_crucible.libord.serialization

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.fromNullable
import com.atomic_crucible.libord.optional.getOrElse
import com.atomic_crucible.libord.optional.map
import com.atomic_crucible.libord.types.Article
import com.atomic_crucible.libord.types.Category
import com.atomic_crucible.libord.types.EntryType
import com.atomic_crucible.libord.types.ErrorEntry
import com.atomic_crucible.libord.types.Entry

import java.lang.reflect.Type

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.reflect.TypeToken


class EntryDeserializer : JsonDeserializer<Entry> {
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
                    context.deserialize<Optional<Article>>(it.get("article"),  object : TypeToken<Optional<Article>>() {}.type)
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
        .getOrElse { ErrorEntry } // find a better fallback here but in case we deserialize garbage we need to know
    }
}