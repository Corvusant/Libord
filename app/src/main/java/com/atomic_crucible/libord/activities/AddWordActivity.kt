package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.Category
import com.atomic_crucible.libord.R

import com.atomic_crucible.libord.Word
import com.atomic_crucible.libord.WordLibrary

class AddWordActivity : AppCompatActivity() {

    private lateinit var wordEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        wordEditText = findViewById(R.id.editTextWord)
        categoryEditText = findViewById(R.id.editTextCategory)
        saveButton = findViewById(R.id.buttonSave)

        saveButton.setOnClickListener {
            val word = wordEditText.text.toString()
            val category = categoryEditText.text.toString()

            val categories = category.splitToSequence(",")
                .toList()
                .map { it.trim() }
                .map { Category(it) }

            if (word.isNotBlank() && category.isNotBlank()) {
                WordLibrary.addWord(Word(word,categories), this)
                Toast.makeText(this, "Word Added", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
