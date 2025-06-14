package com.atomic_crucible.libord.extensions

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Spinner
import com.atomic_crucible.libord.optional.Optional
import com.atomic_crucible.libord.optional.flatten

inline fun <reified T> Spinner.setFromOption(o: Optional<T>) where T:Enum<T> {
    o.flatten(
        {
            this.visibility = VISIBLE
            this.setSelection(enumValues<T>().indexOf(it))
        },
        {
            this.visibility = GONE
        }
    )
}