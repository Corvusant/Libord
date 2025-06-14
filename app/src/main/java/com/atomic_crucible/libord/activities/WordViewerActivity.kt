package com.atomic_crucible.libord.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.types.CATEGORY_ALL
import com.atomic_crucible.libord.types.Category
import com.atomic_crucible.libord.serialization.JsonConverter

import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.READ_LIB_FILE
import com.atomic_crucible.libord.WRITE_LIB_FILE
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
    private lateinit var wordsAdapter: WordsAdapter
    private lateinit var categoriesAdapter: ArrayAdapter<String>

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
                 WRITE_LIB_FILE -> data?.data?.also {
                     uri ->
                        val words = WordLibrary.getAllWords();
                        val json = JsonConverter.toJson(words)

                        contentResolver.openFileDescriptor(uri,"w")?.use {
                            FileOutputStream(it.fileDescriptor).use {
                                it.write(json.toByteArray())
                            }
                    }
                }
                READ_LIB_FILE -> data?.data?.also {
                    uri->
                    contentResolver.openFileDescriptor(uri,"r")?.use {
                        FileInputStream(it.fileDescriptor).use {
                        fd ->
                            val json = fd.bufferedReader().readText();
                            val entries = JsonConverter.fromJson<List<Entry>>(
                                json,
                                object : TypeToken<List<Entry>>() {}.type) ?: listOf<Entry>()
                            WordLibrary.loadNewWords(entries)
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
        val allCategories = mutableListOf(CATEGORY_ALL) + WordLibrary.getCategories()
        categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allCategories.map { it.value })
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoriesAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentCategory = allCategories[position]
                updateWordList(currentCategory)
                WordLibrary.setLastSelectedCategory(Some(currentCategory))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                WordLibrary.setLastSelectedCategory(None)
            }
        }

        WordLibrary.onCategoryCleared = { removedCategory ->
            when (removedCategory) {
                CATEGORY_ALL -> categoriesAdapter.clear()
                else -> categoriesAdapter.remove(removedCategory.value)
            }
        }

        WordLibrary.onCategoryAdded = {
            addedCategory -> categoriesAdapter.add(addedCategory.value)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        wordsAdapter = WordsAdapter(mutableListOf(), this)
        recyclerView.adapter = wordsAdapter
        updateWordList(CATEGORY_ALL)
    }

    private fun updateWordList(category: Category) {
        val words = when(category) {
            CATEGORY_ALL -> WordLibrary.getAllWords()
            else -> WordLibrary.getWordsByCategory(category)
        }
        wordsAdapter.updateWords(words)
    }

    fun updateCategorySpinner()
    {
        val categories = categoriesAdapter.let {
            var c :MutableList<Category> = mutableListOf()
            for (i in 0 until it.count) {
               c.add(Category(it.getItem(i).toString()))
            }
            c.toList()
        }

        val allCategories = mutableListOf(CATEGORY_ALL) + WordLibrary.getCategories()
        val newCategories = allCategories.subtract(categories.toSet())
        val removedItems = categories.subtract(allCategories.toSet())

        val adapter = categorySpinner.adapter as? ArrayAdapter<String>
        for(item in removedItems.map { it.value }) {
            adapter?.remove(item)
        }
        adapter?.addAll(newCategories.map { it.value })
        adapter?.notifyDataSetChanged()
    }

    override fun onPostResume() {
        super.onPostResume()

        WordLibrary.getLastSelectedCategory()
            .flatten({
                setCategorySelection(it)
                updateWordList(it)
            },
            {
                setCategorySelection(CATEGORY_ALL)
                updateWordList(CATEGORY_ALL)
            })

        updateCategorySpinner()
    }

    private fun setCategorySelection(c : Category) {
        for (i in 0 until categorySpinner.adapter.count) {
            if (categorySpinner.adapter.getItem(i) == c.value) {
                categorySpinner.setSelection(i)
                break
            }
        }
    }
}