package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.CATEGORY_ALL
import com.atomic_crucible.libord.JsonConverter
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.Entry
import com.atomic_crucible.libord.optional.*
import com.google.gson.reflect.TypeToken

class ShowWordActivity : AppCompatActivity() {

    private lateinit var wordTextView: TextView
    private lateinit var btnNextWord : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_word)

        wordTextView = findViewById(R.id.textViewSelectedWord)
        btnNextWord = findViewById(R.id.buttonReturn)
        val entry = fromNullable(intent.getStringExtra("ENTRY"))
            .map {
                JsonConverter.fromJson<Entry>(
                    it,
                    object : TypeToken<Entry>() {}.type)
            }
        showWord(entry);

        btnNextWord.setOnClickListener {
            val currentCategory = WordLibrary.getLastSelectedCategory()
            val nextWord = WordLibrary.getRandomWord(currentCategory.getOrElse { CATEGORY_ALL })
            showWord(nextWord)
        }
    }

    private fun showWord(entry : Optional<Entry>)
    {
        wordTextView.text = entry.flatten({it.value},{ getString(R.string.no_word_in_library)})
    }
}