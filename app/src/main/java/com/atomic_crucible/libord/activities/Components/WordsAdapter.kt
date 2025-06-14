package com.atomic_crucible.libord.activities.Components

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.atomic_crucible.libord.R
import com.atomic_crucible.libord.types.Entry
import com.atomic_crucible.libord.serialization.JsonConverter
import com.atomic_crucible.libord.types.WordLibrary
import com.atomic_crucible.libord.activities.EditWordActivity
import com.atomic_crucible.libord.extensions.setFromOption
import com.atomic_crucible.libord.optional.executeIfSet
import com.atomic_crucible.libord.optional.flatten
import com.atomic_crucible.libord.types.Article
import com.atomic_crucible.libord.types.EntryType
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WordsAdapter(
    private var entries: MutableList<Entry>,
    private val context: Context
) : RecyclerView.Adapter<WordsAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewEntry: TextView = itemView.findViewById(R.id.textViewItemWord)
        val textViewArticle: TextView = itemView.findViewById(R.id.textViewArticle)
        val textViewCategory : TextView = itemView.findViewById(R.id.textViewCategory)
        val btnDelete: FloatingActionButton = itemView.findViewById(R.id.buttonDeleteWord)
        val btnEdit : FloatingActionButton = itemView.findViewById(R.id.buttonEditWord)
        var textViewPlural : TextView = itemView.findViewById(R.id.textViewPlural)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = entries[position]
        holder.textViewEntry.text = word.value
        holder.textViewCategory.text = word.categories.fold("", {
            acc, c -> when (acc){
                "" -> c.value
                else -> "$acc, ${c.value}"
            }
        })

        when(word.entryType)
        {
            EntryType.Noun -> {
                holder.textViewArticle.visibility = VISIBLE
                holder.textViewPlural.visibility = VISIBLE

                word.article.flatten(
                    {
                        when(it) {
                            Article.kein -> holder.textViewArticle.visibility = GONE
                            else -> {
                                holder.textViewArticle.visibility = VISIBLE
                                holder.textViewArticle.text  = it.toString()
                            }
                        }
                    },
                    {
                        holder.textViewArticle.visibility = GONE
                    }
                )

                holder.textViewPlural.setFromOption(word.plural)

            }
            else -> {
                holder.textViewArticle.visibility = GONE
                holder.textViewPlural.visibility = GONE
            }
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditWordActivity::class.java)
            intent.putExtra("ENTRY", JsonConverter.toJson(word))
            startActivity(context, intent, null)
        }

        holder.btnDelete.setOnClickListener {
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

