package com.atomic_crucible.libord.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.types.Category
import com.atomic_crucible.libord.serialization.JsonConverter
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.optional.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var btnNewWord: FloatingActionButton
    private lateinit var btnLibView: FloatingActionButton
    private lateinit var btnPickWord: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WordLibrary.load(this, JsonConverter)
        setContentView(R.layout.activity_main)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnNewWord = findViewById(R.id.buttonNewWord)
        btnPickWord = findViewById(R.id.buttonPickWord)
        btnLibView = findViewById(R.id.buttonLibrary)

        populateCategories()
        setCategorySelection()

        btnNewWord.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }

        btnPickWord.setOnClickListener {
            val category = Category(spinnerCategory.selectedItem.toString())
            WordLibrary.getRandomWord(category)
                .flatten(
                { // for some reason we get option of Word here instead of word
                    val word = JsonConverter.toJson(it)
                    val intent = Intent(this, ShowWordActivity::class.java)
                    intent.putExtra("ENTRY", word )
                    startActivity(intent)
                },
                {
                    Toast.makeText(this, "No word found in selected category", Toast.LENGTH_SHORT).show()
                })
        }

        btnLibView.setOnClickListener {
            val category = spinnerCategory.selectedItem.toString()
            val intent = Intent(this,
                WordViewerActivity::class.java)
            intent.putExtra("CATEGORY", category)
            startActivity(intent)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        setCategorySelection()
    }

    private fun setCategorySelection() {
        WordLibrary.getLastSelectedCategory()
            .executeIfSet { c ->
                for (i in 0 until spinnerCategory.adapter.count) {
                    if (spinnerCategory.adapter.getItem(i) == c.value) {
                        spinnerCategory.setSelection(i)
                        break
                    }
                }
            }
    }

    private fun populateCategories() {
        val categories = WordLibrary.getCategories().map { it.value }.ifEmpty { listOf("None") }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currentCategory = Category(categories[position])
                WordLibrary.setLastSelectedCategory(Some(currentCategory))
                btnPickWord.isEnabled = WordLibrary.hasItems(currentCategory)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                WordLibrary.setLastSelectedCategory(None)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        populateCategories()
    }
}
