package com.atomic_crucible.libord.types

import android.content.Context

import com.atomic_crucible.libord.R

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.flatten

data class Category (
    val value: String
)

enum class EntryType {
    Noun,
    Verb,
    Adjective,
    Phrase,
    None
}

fun EntryType.getLocalizedName(c : Context) : String =
    when (this) {
        EntryType.Noun -> c.getString(R.string.entryType_Noun)
        EntryType.Verb -> c.getString(R.string.entryType_verb)
        EntryType.Adjective -> c.getString(R.string.entryType_adjective)
        EntryType.Phrase -> c.getString(R.string.entryType_phrase)
        EntryType.None -> c.getString(R.string.entryType_none)
    }

enum class Article {
    der,
    die,
    das
}

val ErrorEntry : Entry = Entry("##ERROR##", listOf(), EntryType.None, None, None)
data class Entry (
    val value: String,
    val categories: List<Category>,
    val entryType: EntryType,
    val article: Optional<Article>,
    val plural: Optional<String>
)
{
    override fun hashCode(): Int {
        return article.flatten({
            value.hashCode()
                .and(categories.hashCode())
                .and(entryType.hashCode())
                .and(it.hashCode())
        },{
            value.hashCode()
                .and(categories.hashCode())
                .and(entryType.hashCode())
        })
    }
}

