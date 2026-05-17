package com.example.googleclass.feature.taskdetail.presentation

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberFilePicker(
    onFilePicked: (Uri, String) -> Unit,
): FilePickerState {
    val context = LocalContext.current

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val displayName = context.contentResolver.resolveFileName(uri)
            onFilePicked(uri, displayName)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val displayName = context.contentResolver.resolveFileName(uri)
            onFilePicked(uri, displayName)
        }
    }

    return remember {
        object : FilePickerState {
            override fun launchDocuments() {
                documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
            }

            override fun launchGallery() {
                galleryPickerLauncher.launch("image/*")
            }
        }
    }
}

interface FilePickerState {
    fun launchDocuments()
    fun launchGallery()
}

private fun android.content.ContentResolver.resolveFileName(uri: Uri): String {
    var name = "file"
    query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                name = cursor.getString(index)
            }
        }
    }
    return name
}

private val ALLOWED_MIME_TYPES = arrayOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "image/png",
    "application/pdf",
    "text/plain",
)
