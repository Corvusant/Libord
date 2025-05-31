package com.atomic_crucible.libord

import android.content.Intent
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult

const val CREATE_FILE = 1

@RequiresApi(Build.VERSION_CODES.O)
fun createFile(sourceActivity: Activity) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, "Libord_Library.txt")

        //putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
    }
    sourceActivity.startActivityForResult(intent, CREATE_FILE)
}