package com.atomic_crucible.libord.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.CATEGORY_ALL
import com.atomic_crucible.libord.Category
import com.atomic_crucible.libord.JsonConverter

import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.Word
import com.atomic_crucible.libord.activities.Components.WordsAdapter
import com.atomic_crucible.libord.createFile
import com.atomic_crucible.libord.openFile
import com.atomic_crucible.libord.optional.*
import com.google.gson.reflect.TypeToken
import java.io.FileInputStream
import java.io.FileOutputStream

class WordViewerActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteAllButton: Button
    private lateinit var exportButton : Button
    private lateinit var importButton : Button
    private lateinit var adapter: WordsAdapter

    private var currentCategory: Category = CATEGORY_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_viewer)

        categorySpinner = findViewById(R.id.spinnerFilterCategory)
        recyclerView = findViewById(R.id.recyclerViewWords)
        deleteAllButton = findViewById(R.id.buttonDeleteAll)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)

        setupCategorySpinner()
        setupRecyclerView()

        deleteAllButton.setOnClickListener {
            if (currentCategory == CATEGORY_ALL) {
                WordLibrary.getCategories()
                .forEach { WordLibrary.deleteAllInCategory(it,this) }
                updateWordList(CATEGORY_ALL)
            } else {
                WordLibrary.deleteAllInCategory(currentCategory, this)
                updateWordList(CATEGORY_ALL)
                Toast.makeText(this, "Deleted all words in $currentCategory", Toast.LENGTH_SHORT).show()
            }
        }

        exportButton.setOnClickListener {
            createFile(this)
        }
        importButton.setOnClickListener {
            openFile(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK)
        {

            when(requestCode)
            {
                 1 -> data?.data?.also {
                     uri ->
                        val words = WordLibrary.getAllWords();
                        val json = JsonConverter.toJson(words)

                        contentResolver.openFileDescriptor(uri,"w")?.use {
                            FileOutputStream(it.fileDescriptor).use {
                                it.write(json.toByteArray())
                            }
                    }
                }
                2 -> data?.data?.also {
                    uri->
                    contentResolver.openFileDescriptor(uri,"r")?.use {
                        FileInputStream(it.fileDescriptor).use {
                            val json = it.bufferedReader().readText();
                            val type = object : TypeToken<MutableList<Word>>() {}.type
                            val words = JsonConverter.fromJson<List<Word>>(json, type)
                            WordLibrary.loadNewWords(words.toList())
                            WordLibrary.save(this, JsonConverter)
                            updateWordList(CATEGORY_ALL)
                        }
                    }
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        WordLibrary.onCategoryCleared = null
    }

    private fun setupCategorySpinner() {
        val categories =
            mutableListOf(CATEGORY_ALL.value) + WordLibrary.getCategories().map { it.value }
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapterSpinner

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentCategory = Category(categories[position])
                updateWordList(currentCategory)
                WordLibrary.setLastSelectedCategory(Some(currentCategory))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                WordLibrary.setLastSelectedCategory(None)
            }
        }

        WordLibrary.onCategoryCleared = { removedCategory ->
            when (removedCategory) {
                CATEGORY_ALL -> adapterSpinner.clear()
                else -> adapterSpinner.remove(removedCategory.value)
            }
        }

        WordLibrary.onCategoryAdded = {
            addedCategory -> adapterSpinner.add(addedCategory.value)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WordsAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter
        updateWordList(CATEGORY_ALL)
    }

    private fun updateWordList(category: Category) {
        val words = when(category) {
            CATEGORY_ALL -> WordLibrary.getAllWords()
            else -> WordLibrary.getWordsByCategory(category)
        }
        adapter.updateWords(words)
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
}