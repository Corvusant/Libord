package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.types.Category
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.extensions.setFromOption

import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.types.EntryType
import com.atomic_crucible.libord.serialization.JsonConverter
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.optional.None
import com.atomic_crucible.libord.optional.Some
import com.atomic_crucible.libord.optional.executeIfSet
import com.atomic_crucible.libord.optional.fromNullable
import com.atomic_crucible.libord.optional.getOrElse
import com.atomic_crucible.libord.optional.map
import com.atomic_crucible.libord.types.Article
import com.atomic_crucible.libord.types.getLocalizedName
import com.google.gson.reflect.TypeToken

class EditWordActivity : AppCompatActivity() {

    private lateinit var editTextEntry: EditText
    private lateinit var editTextCategory: EditText
    private lateinit var editTextPlural : EditText
    private lateinit var btnSave: Button
    private lateinit var spinnerEntryType : Spinner
    private lateinit var spinnerArticle : Spinner

    private lateinit var editedEntry: Entry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)

        editTextEntry = findViewById(R.id.editTextWord)
        editTextCategory = findViewById(R.id.editTextCategory)
        editTextPlural = findViewById(R.id.editTextPlural)
        btnSave = findViewById(R.id.buttonSave)
        spinnerEntryType = findViewById(R.id.spinnerEntryType)
        spinnerArticle = findViewById(R.id.spinnerArticle)

        editedEntry = fromNullable(intent.getStringExtra("ENTRY"))
            .map {
                JsonConverter.fromJson<Entry>(
                    it,
                    object : TypeToken<Entry>() {}.type)
            }
            .getOrElse { WordLibrary.any() }

        editTextEntry.setText(editedEntry.value)
        editTextCategory.setText( editedEntry.categories.fold("", {
                acc, c -> when (acc){
            "" -> c.value
            else -> "$acc, ${c.value}"
        }
        }))

        populateSpinners()

        spinnerEntryType.setSelection(EntryType.entries.indexOf(editedEntry.entryType))
        when(editedEntry.entryType)
        {
            EntryType.Noun ->
            {
                spinnerArticle.setFromOption(editedEntry.article)
                editTextPlural.setFromOption(editedEntry.plural)
            }
            else ->
            {
                spinnerArticle.visibility = GONE
                editTextPlural.visibility = GONE
            }
        }

        btnSave.setOnClickListener {
            val word = editTextEntry.text.toString()
            val category = editTextCategory.text.toString()

            val categories = category.splitToSequence(",")
                .toList()
                .map { it.trim() }
                .map { Category(it) }

            val entryType = EntryType.entries[spinnerEntryType.selectedItemPosition]
            val article = when (entryType)
            {
                EntryType.Noun -> Some(Article.entries[spinnerArticle.selectedItemPosition])
                else -> None
            }

            val plural = when (entryType)
            {
                EntryType.Noun -> Some(editTextPlural.text.toString())
                else -> None
            }

            if (word.isNotBlank() && category.isNotBlank() && entryType != EntryType.None) {
                WordLibrary.updateEntry(editedEntry ,
                    Entry(word,categories, entryType , article, plural),
                    this)
                Toast.makeText(this, "Word edited", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateSpinners() {
        val entryTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, EntryType.entries.map { it.getLocalizedName(this) })
        entryTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEntryType.adapter = entryTypeAdapter

        spinnerEntryType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val entryType = EntryType.entries[position]
                when (entryType) {
                    EntryType.Noun -> {
                        spinnerArticle.visibility = VISIBLE
                        editTextPlural.visibility = VISIBLE
                    }
                    else -> {
                        spinnerArticle.visibility = GONE
                        editTextPlural.visibility = GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinnerArticle.visibility = GONE
                editTextPlural.visibility = GONE
            }
        }

        val articleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Article.entries)
        articleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArticle.adapter = articleAdapter
    }
}
