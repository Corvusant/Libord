package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.Category
import com.atomic_crucible.libord.R

import com.atomic_crucible.libord.Entry
import com.atomic_crucible.libord.EntryType
import com.atomic_crucible.libord.JsonConverter
import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.fromNullable
import com.atomic_crucible.libord.optional.getOrElse
import com.atomic_crucible.libord.optional.map
import com.google.gson.reflect.TypeToken

const val EDITED_WORD = 3

class EditWordActivity : AppCompatActivity() {

    private lateinit var wordEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var EditedEntry: Entry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)

        wordEditText = findViewById(R.id.editTextWord)
        categoryEditText = findViewById(R.id.editTextCategory)
        saveButton = findViewById(R.id.buttonSave)

        EditedEntry = fromNullable(intent.getStringExtra("ENTRY"))
            .map {
                JsonConverter.fromJson<Entry>(
                    it,
                    object : TypeToken<Entry>() {}.type)
            }
            .getOrElse { WordLibrary.any() }

        wordEditText.setText(EditedEntry.value)
        categoryEditText.setText( EditedEntry.categories.fold("", {
                acc, c -> when (acc){
            "" -> c.value
            else -> "$acc, ${c.value}"
        }
        }))

        saveButton.setOnClickListener {
            val word = wordEditText.text.toString()
            val category = categoryEditText.text.toString()

            val categories = category.splitToSequence(",")
                .toList()
                .map { it.trim() }
                .map { Category(it) }

            if (word.isNotBlank() && category.isNotBlank()) {
                WordLibrary.updateEntry(EditedEntry ,Entry(word,categories, EntryType.Noun, None), this)
                Toast.makeText(this, "Word edited", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
