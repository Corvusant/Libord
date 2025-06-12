package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.types.Article
import com.atomic_crucible.libord.types.Category
import com.atomic_crucible.libord.R

import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.types.EntryType
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Some

class AddWordActivity : AppCompatActivity() {

    private lateinit var wordEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var spinnerEntryType : Spinner
    private lateinit var spinnerArticle : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        wordEditText = findViewById(R.id.editTextWord)
        categoryEditText = findViewById(R.id.editTextCategory)
        saveButton = findViewById(R.id.buttonSave)
        spinnerEntryType = findViewById(R.id.spinnerEntryType)
        spinnerArticle = findViewById(R.id.spinnerArticle)

        populateSpinners()

        saveButton.setOnClickListener {
            val word = wordEditText.text.toString()
            val category = categoryEditText.text.toString()

            val categories = category.splitToSequence(",")
                .toList()
                .map { it.trim() }
                .map { Category(it) }

            if (word.isNotBlank() && category.isNotBlank()) {
                val entryType = EntryType.entries[spinnerEntryType.selectedItemPosition]

                val article = when (entryType) {
                    EntryType.Noun -> Some(Article.entries[spinnerArticle.selectedItemPosition])
                    else-> None}

                val newEntry = Entry(
                    word,
                    categories,
                    entryType,
                    article)

                WordLibrary.addEntry(newEntry, this)
                Toast.makeText(this, "Word Added", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateSpinners() {
        val entryTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, EntryType.entries)
        entryTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEntryType.adapter = entryTypeAdapter

        spinnerEntryType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val entryType = EntryType.entries[position]
                when (entryType) {
                    EntryType.Noun -> spinnerArticle.visibility = VISIBLE
                    else -> spinnerArticle.visibility = GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinnerArticle.visibility = GONE
            }
        }

        val articleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Article.entries)
        articleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArticle.adapter = articleAdapter
    }

}
