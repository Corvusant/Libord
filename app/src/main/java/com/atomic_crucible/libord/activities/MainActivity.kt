package com.atomic_crucible.libord.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.Category
import com.atomic_crucible.libord.JsonConverter
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.optional.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var wordTextView: TextView
    private lateinit var newWordButton: FloatingActionButton
    private lateinit var libViewButton: FloatingActionButton
    private lateinit var pickWordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WordLibrary.load(this, JsonConverter)
        setContentView(R.layout.activity_main)

        categorySpinner = findViewById(R.id.spinnerCategory)
        wordTextView = findViewById(R.id.textViewWord)
        newWordButton = findViewById(R.id.buttonNewWord)
        pickWordButton = findViewById(R.id.buttonPickWord)
        libViewButton = findViewById(R.id.buttonLibrary)

        populateCategories()
        setCategorySelection()

        newWordButton.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }

        pickWordButton.setOnClickListener {
            val category = categorySpinner.selectedItem.toString()
            fromNullable(WordLibrary.getRandomWord(category))
                .bind { w -> fromNullable(w.value) }
                .flatten(
                { v ->
                    val intent = Intent(this, ShowWordActivity::class.java)
                    intent.putExtra("SELECTED_WORD", v)
                    startActivity(intent)
                },
                {
                    Toast.makeText(this, "No word found in selected category", Toast.LENGTH_SHORT).show()
                })
        }

        libViewButton.setOnClickListener {
            val category = categorySpinner.selectedItem.toString()
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
                for (i in 0 until categorySpinner.adapter.count) {
                    if (categorySpinner.adapter.getItem(i) == c.value) {
                        categorySpinner.setSelection(i)
                        break
                    }
                }
            }
    }

    private fun populateCategories() {
        val categories = WordLibrary.getCategories().map { it.value }.ifEmpty { listOf("None") }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currentCategory = Category(categories[position])
                WordLibrary.setLastSelectedCategory(Some(currentCategory))
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
