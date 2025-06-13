package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.types.CATEGORY_ALL
import com.atomic_crucible.libord.serialization.JsonConverter
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.extensions.setFromOption
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.optional.*
import com.atomic_crucible.libord.types.EntryType

class ShowWordActivity : AppCompatActivity() {

    private lateinit var textViewArticle: TextView
    private lateinit var textViewEntry: TextView
    private lateinit var textViewPlural: TextView
    private lateinit var btnNextWord : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_word)

        textViewEntry = findViewById(R.id.textViewSelectedWord)
        textViewArticle = findViewById(R.id.textViewArticle)
        textViewPlural = findViewById(R.id.textViewPLural)

        btnNextWord = findViewById(R.id.buttonReturn)
        val entry = fromNullable(intent.getStringExtra("ENTRY"))
            .map { JsonConverter.fromJson<Entry>(it, Entry::class.java)}
        showWord(entry);

        btnNextWord.setOnClickListener {
            val currentCategory = WordLibrary.getLastSelectedCategory()
            val nextWord = WordLibrary.getRandomWord(currentCategory.getOrElse { CATEGORY_ALL })
            showWord(nextWord)
        }
    }

    private fun showWord(entry : Optional<Entry>)
    {
        entry.executeIfSet {
            textViewEntry.text = it.value

            when(it.entryType)
            {
                EntryType.Noun ->
                {
                    textViewArticle.setFromOption(it.article)
                    textViewPlural.setFromOption(it.plural)
                }
                else ->
                {
                    textViewArticle.visibility = GONE
                    textViewPlural.visibility = GONE
                }
            }

        }
    }
}