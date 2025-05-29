package com.atomic_crucible.libord.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atomic_crucible.libord.R

class ShowWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_word)

        val wordTextView: TextView = findViewById(R.id.textViewSelectedWord)
        val returnButton: Button = findViewById(R.id.buttonReturn)

        val word = intent.getStringExtra("SELECTED_WORD")
        wordTextView.text = word ?: "No word provided"

        returnButton.setOnClickListener {
            finish()
        }
    }
}