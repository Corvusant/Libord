package com.atomic_crucible.libord.activities.Components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.Word
import com.atomic_crucible.libord.WordLibrary
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WordsAdapter(
    private var words: MutableList<Word>,
    private val context: Context
) : RecyclerView.Adapter<WordsAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordTextView: TextView = itemView.findViewById(R.id.textViewItemWord)
        val categoryTextView : TextView = itemView.findViewById(R.id.textViewCategory)
        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.buttonDeleteWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.wordTextView.text = word.value
        holder.categoryTextView.text = word.category.value
        holder.deleteButton.setOnClickListener {
            WordLibrary.deleteWord(word, context)
            words.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords.toMutableList()
            .sortedBy { w -> w.category.value }
            .toMutableList()

        notifyDataSetChanged()
    }
}

