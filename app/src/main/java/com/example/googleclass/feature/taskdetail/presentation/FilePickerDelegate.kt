package com.example.googleclass.feature.taskdetail.presentation

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

class FilePickerDelegate(
    registry: ActivityResultRegistry,
    private val contentResolver: ContentResolver,
) {

    var onFilePicked: ((uri: Uri, displayName: String) -> Unit)? = null

    private val filePickerLauncher: ActivityResultLauncher<Array<String>> =
        registry.register(
            KEY,
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                val displayName = resolveFileName(uri)
                onFilePicked?.invoke(uri, displayName)
            }
        }

    fun launch() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    fun unregister() {
        filePickerLauncher.unregister()
    }

    private fun resolveFileName(uri: Uri): String {
        var name = "file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = cursor.getString(index)
                }
            }
        }
        return name
    }

    private companion object {
        const val KEY = "file_picker"
    }
}
