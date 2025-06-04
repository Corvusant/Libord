package com.atomic_crucible.libord

import android.content.Intent
import android.app.Activity

const val WRITE_LIB_FILE = 1
const val READ_LIB_FILE = 2

fun createFile(sourceActivity: Activity) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
        putExtra(Intent.EXTRA_TITLE, "Libord_Library.json")
    }
    sourceActivity.startActivityForResult(intent, WRITE_LIB_FILE)
}

fun openFile(sourceActivity: Activity) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
    }
    sourceActivity.startActivityForResult(intent,READ_LIB_FILE)
}