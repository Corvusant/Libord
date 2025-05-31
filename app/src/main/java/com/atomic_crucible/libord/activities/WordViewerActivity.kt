package com.atomic_crucible.libord.activities

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.Category
import com.atomic_crucible.libord.JsonConverter

import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.activities.Components.WordsAdapter
import com.atomic_crucible.libord.createFile
import com.atomic_crucible.libord.optional.*
import java.io.File
import java.io.FileOutputStream

class WordViewerActivity : AppCompatActivity() {


    private lateinit var spinnerCategory: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteAllButton: Button
    private lateinit var exportButton : Button
    private lateinit var adapter: WordsAdapter

    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_viewer)

        spinnerCategory = findViewById(R.id.spinnerFilterCategory)
        recyclerView = findViewById(R.id.recyclerViewWords)
        deleteAllButton = findViewById(R.id.buttonDeleteAll)
        exportButton = findViewById(R.id.exportButton)

        setupCategorySpinner()
        setupRecyclerView()

        deleteAllButton.setOnClickListener {
            if (currentCategory == "All") {
                Toast.makeText(this, "Select a specific category to delete.", Toast.LENGTH_SHORT).show()
            } else {
                WordLibrary.deleteAllInCategory(Category(currentCategory), this)
                updateWordList(currentCategory)
                Toast.makeText(this, "Deleted all words in $currentCategory", Toast.LENGTH_SHORT).show()
            }
        }

        exportButton.setOnClickListener {
            createFile(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1)
        {
            data?.data?.also {
                uri ->
                    val words = WordLibrary.getAllWords();
                    val json = JsonConverter.toJson(words)

                    contentResolver.openFileDescriptor(uri,"w")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(json.toByteArray())
                        }
                    }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        WordLibrary.onCategoryCleared = null
    }

    private fun setupCategorySpinner() {
        val categories = mutableListOf("All") + WordLibrary.getCategories()
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterSpinner

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategory = categories[position]
                updateWordList(currentCategory)
                WordLibrary.setLastSelectedCategory(Some(Category(currentCategory)))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                WordLibrary.setLastSelectedCategory(None)
            }
        }

        WordLibrary.onCategoryCleared = { removedCategory ->
            Toast.makeText(this, "Last word in Category \"$removedCategory\" has been removed, removing Category", Toast.LENGTH_SHORT).show()
            adapterSpinner.remove(removedCategory.value) }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WordsAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter
        updateWordList("All")
    }

    private fun updateWordList(category: String) {
        val words = if (category == "All") {
            WordLibrary.getAllWords()
        } else {
            WordLibrary.getWordsByCategory(Category(category))
        }
        adapter.updateWords(words)
    }
}