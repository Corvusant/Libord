package com.atomic_crucible.libord

import android.content.Context
import com.google.gson.reflect.TypeToken
import java.io.File
import com.atomic_crucible.libord.optional.*
import com.atomic_crucible.libord.optional.flatten
import com.atomic_crucible.libord.optional.map

val CATEGORY_ALL = Category("All")

fun getCategoriesFromWords(words:List<Word>) =
    words.map { it.categories.toMutableList() }
        .flatten()
        .distinct()


object WordLibrary {
    private const val FILE_NAME = "Library.json"
    private var words = mutableListOf<Word>()
    private var lastSelectedCategory : Optional<Category> = None

    var onCategoryCleared: ((Category) -> Unit)? = null
    var onCategoryAdded: ((Category) -> Unit)? = null

    fun load(context: Context, converter : JsonConverter) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<Word>>() {}.type
            val loadedWords : MutableList<Word> = converter.fromJson(json, type) ?: mutableListOf()
            loadNewWords(loadedWords.toList())
            lastSelectedCategory = fromNullable(
                words.map { w -> w.categories.first() }
                .sortedBy{ c -> c.value }
                .distinct()
                .firstOrNull())
        }
    }

    fun loadNewWords(inWords: List<Word> ) : Unit
    {
        val categories = getCategories()
        val newCategories = getCategoriesFromWords(inWords)
        val addedCategories = categories.union(newCategories).subtract(categories)

        words = words.union(inWords)
            .toMutableList()

        for (category in addedCategories) {
            onCategoryAdded?.invoke(category)
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

    fun getCategories(): List<Category> =
         getCategoriesFromWords(words)

    fun getWordsByCategory(category: Category): List<Word> {
        return words.filter { it.categories.contains(category) }
    }

    fun deleteWord(wordToDelete: Word, context: Context) {
        val category = wordToDelete.categories.first()
        val before = getWordsByCategory(category).size

        words.removeAll { it.value == wordToDelete.value && it.categories == wordToDelete.categories }

        save(context, JsonConverter)

        val after = getWordsByCategory(category).size
        if (before > 0 && after == 0) {
            onCategoryCleared?.invoke(category)
        }
        save(context, JsonConverter)
    }

    fun deleteAllInCategory(category: Category, context: Context) {
        val hadWords = words.removeAll { it.categories.contains(category) }
        save(context, JsonConverter)

        if (hadWords) {
            onCategoryCleared?.invoke(category)
        }
    }

    fun getRandomWord(category: String): Word? {
        return words.filter { it.categories.contains(Category(category)) }.randomOrNull()
    }

    fun getAllWords(): List<Word> = words.toList()
}