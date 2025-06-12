package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.types.CATEGORY_ALL
import com.atomic_crucible.libord.serialization.JsonConverter
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.optional.*

class ShowWordActivity : AppCompatActivity() {

    private lateinit var textViewArticle: TextView
    private lateinit var textViewEntry: TextView
    private lateinit var btnNextWord : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_word)

        textViewEntry = findViewById(R.id.textViewSelectedWord)
        textViewArticle = findViewById(R.id.textViewArticle)
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
            it.article.flatten(
            { a ->
                textViewArticle.visibility = VISIBLE
                textViewArticle.text = a.toString()
            },
            { textViewArticle.visibility = GONE }
            )
        }
    }
}