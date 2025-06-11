package com.atomic_crucible.libord.activities.Components

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.Entry
import com.atomic_crucible.libord.JsonConverter
import com.atomic_crucible.libord.WordLibrary
import com.atomic_crucible.libord.activities.EditWordActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WordsAdapter(
    private var entries: MutableList<Entry>,
    private val context: Context
) : RecyclerView.Adapter<WordsAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordTextView: TextView = itemView.findViewById(R.id.textViewItemWord)
        val categoryTextView : TextView = itemView.findViewById(R.id.textViewCategory)
        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.buttonDeleteWord)
        val editButton : FloatingActionButton = itemView.findViewById(R.id.buttonEditWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = entries[position]
        holder.wordTextView.text = word.value
        holder.categoryTextView.text = word.categories.fold("", {
            acc, c -> when (acc){
                "" -> c.value
                else -> "$acc, ${c.value}"
            }
        })

        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditWordActivity::class.java)
            intent.putExtra("ENTRY", JsonConverter.toJson(word))
            startActivity(context, intent, null)
        }

        holder.deleteButton.setOnClickListener {
            WordLibrary.deleteWord(word, context)
            entries.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = entries.size

    fun updateWords(newEntries: List<Entry>) {
        entries = newEntries.toMutableList()
            .sortedBy { w -> w.categories.first().value }
            .toMutableList()

        notifyDataSetChanged()
    }
}

