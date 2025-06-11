package com.atomic_crucible.libord

import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.flatten

data class Category (
    val value: String
)

enum class EntryType {
    Noun, Verb, Phrase, None
}

data class Article (
    val value: String
)

val ErrorEntry : Entry = Entry("##ERROR##", listOf(), EntryType.None, None)
data class Entry (
    val value: String,
    val categories: List<Category>,
    val enum: Enum<EntryType>,
    val article: Optional<Article>
)
{
    override fun hashCode(): Int {
        return article.flatten({
            value.hashCode()
                .and(categories.hashCode())
                .and(enum.hashCode())
                .and(it.hashCode())
        },{
            value.hashCode()
                .and(categories.hashCode())
                .and(enum.hashCode())
        })
    }
}

