package com.atomic_crucible.libord.extensions

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.flatten

fun <T> TextView.setFromOption(o: Optional<T>, prefix : String = "")  {
    o.flatten(
        {
            this.visibility = VISIBLE
            this.text = "$prefix${it.toString()}"
        },
        {
            this.visibility = GONE
        }
    )
}