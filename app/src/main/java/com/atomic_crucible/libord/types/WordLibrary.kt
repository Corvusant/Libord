package com.atomic_crucible.libord.types

import android.content.Context
import com.atomic_crucible.libord.serialization.JsonConverter
import com.google.gson.reflect.TypeToken
import java.io.File
import com.atomic_crucible.libord.optional.*

val CATEGORY_ALL = Category("All")

fun getCategoriesFromWords(entries:List<Entry>) =
    entries.map { it.categories.toMutableList() }
        .flatten()
        .distinct()


object WordLibrary {
    private const val FILE_NAME = "Library.json"
    private var entries = mutableListOf<Entry>()
    private var lastSelectedCategory : Optional<Category> = None

    var onCategoryCleared: ((Category) -> Unit)? = null
    var onCategoryAdded: ((Category) -> Unit)? = null

    fun load(context: Context, converter : JsonConverter) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val json = file.readText()
            val loadedEntries : MutableList<Entry> = JsonConverter.fromJson(
                json,
                object : TypeToken<MutableList<Entry>>() {}.type
            ) ?: mutableListOf()
            loadNewWords(loadedEntries.toList())
            lastSelectedCategory = fromNullable(
                entries.map { w -> w.categories.first() }
                .sortedBy{ c -> c.value }
                .distinct()
                .firstOrNull())
        }
    }

    fun loadNewWords(inEntries: List<Entry> ) : Unit
    {
        val categories = getCategories()
        val newCategories = getCategoriesFromWords(inEntries)
        val addedCategories = newCategories.subtract(categories.toSet())

        entries = entries.union(inEntries)
            .toMutableList()

        for (category in addedCategories) {
            onCategoryAdded?.invoke(category)
        }
    }

    fun save(context: Context, conveter: JsonConverter) {
        val json = JsonConverter.toJson(entries)
        File(context.filesDir, FILE_NAME).writeText(json)
    }

    fun setLastSelectedCategory(category: Optional<Category>)
    {
        lastSelectedCategory = category;
    }

    fun getLastSelectedCategory() : Optional<Category>
        = lastSelectedCategory

    fun addEntry(entry: Entry, context: Context) {
        entries.add(entry)

        save(context, JsonConverter)
    }

    fun getCategories(): List<Category> =
         getCategoriesFromWords(entries)

    fun getWordsByCategory(category: Category): List<Entry> {
        return entries.filter { it.categories.contains(category) }
    }

    fun deleteWord(entryToDelete: Entry, context: Context) {
        val category = entryToDelete.categories.first()
        val before = getWordsByCategory(category).size

        entries.removeAll { it.value == entryToDelete.value && it.categories == entryToDelete.categories }

        save(context, JsonConverter)

        val after = getWordsByCategory(category).size
        if (before > 0 && after == 0) {
            onCategoryCleared?.invoke(category)
        }
        save(context, JsonConverter)
    }

    fun deleteAllInCategory(category: Category, context: Context) {
        val hadWords = entries.removeAll { it.categories.contains(category) }
        save(context, JsonConverter)

        if (hadWords) {
            onCategoryCleared?.invoke(category)
        }
    }

    fun getRandomWord(category: Category): Optional<Entry> =
        fromNullable(entries.filter { it.categories.contains(category) }.randomOrNull())

    fun getAllWords(): List<Entry> =
        entries.toList()

    fun hasItems(category: Category): Boolean =
        entries.filter { it.categories.contains(category) }
        .any()

    fun any(): Entry = if(entries.any()) { entries.first() } else { ErrorEntry
    }
    fun updateEntry(editedEntry: Entry, newValue: Entry, context: Context) {
        when(val index = entries.indexOf(editedEntry)) {
            -1 -> return
            else -> entries[index] = newValue
        }
        save(context, JsonConverter)
    }

}