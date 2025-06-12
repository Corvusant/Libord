package com.atomic_crucible.libord.types

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.flatten

data class Category (
    val value: String
)

enum class EntryType {
    Noun, Verb, Adjective, Phrase, None
}

enum class Article {
    der, die, das
}

val ErrorEntry : Entry = Entry("##ERROR##", listOf(), EntryType.None, None)
data class Entry (
    val value: String,
    val categories: List<Category>,
    val entryType: EntryType,
    val article: Optional<Article>
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

