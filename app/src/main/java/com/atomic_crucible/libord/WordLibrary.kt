package com.atomic_crucible.libord

import android.content.Context
import com.google.gson.reflect.TypeToken
import java.io.File
import com.atomic_crucible.libord.optional.*

object WordLibrary {
    private const val FILE_NAME = "words.json"
    private var words = mutableListOf<Word>()
    private var lastSelectedCategory : Optional<Category> = None

    var onCategoryCleared: ((Category) -> Unit)? = null

    fun load(context: Context, converter : JsonConverter) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<Word>>() {}.type
            words = converter.fromJson(json, type) ?: mutableListOf()
            lastSelectedCategory = fromNullable(
                words.map { w -> w.category }
                .sortedBy{ c -> c.value }
                .distinct()
                .firstOrNull())
        }
    }

    fun save(context: Context, conveter: JsonConverter) {
        val json = conveter.toJson(words)
        File(context.filesDir, FILE_NAME).writeText(json)
    }

    fun setLastSelectedCategory(category: Optional<Category>)
    {
        lastSelectedCategory = category;
    }

    fun getLastSelectedCategory() : Optional<Category>
        = lastSelectedCategory

    fun addWord(word: Word, context: Context) {
        words.add(word)

        save(context, JsonConverter)
    }

    fun getCategories(): List<String> {
        return words.map { it.category.value }.distinct()
    }

    fun getWordsByCategory(category: Category): List<Word> {
        return words.filter { it.category == category }
    }

    fun deleteWord(wordToDelete: Word, context: Context) {
        val category = wordToDelete.category
        val before = getWordsByCategory(category).size

        words.removeAll { it.value == wordToDelete.value && it.category == wordToDelete.category }

        save(context, JsonConverter)

        val after = getWordsByCategory(category).size
        if (before > 0 && after == 0) {
            onCategoryCleared?.invoke(category)
        }
        save(context, JsonConverter)
    }

    fun deleteAllInCategory(category: Category, context: Context) {
        val hadWords = words.any { it.category == category }
        words.removeAll { it.category == category }
        save(context, JsonConverter)

        if (hadWords) {
            onCategoryCleared?.invoke(category)
        }
    }

    fun getRandomWord(category: String): Word? {
        return words.filter { it.category.value == category }.randomOrNull()
    }

    fun getAllWords(): List<Word> = words.toList()
}