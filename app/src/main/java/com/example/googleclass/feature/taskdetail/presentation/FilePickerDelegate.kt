package com.example.googleclass.feature.taskdetail.presentation

import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberFilePicker(
    onFilePicked: (Uri, String) -> Unit,
): FilePickerState {
    val context = LocalContext.current
    var pendingSource by remember { mutableStateOf<PickerSource?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val displayName = context.contentResolver.resolveFileName(uri)
            onFilePicked(uri, displayName)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val displayName = context.contentResolver.resolveFileName(uri)
            onFilePicked(uri, displayName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.all { it }) {
            when (pendingSource) {
                PickerSource.DOCUMENTS -> documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
                PickerSource.GALLERY -> galleryPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
                null -> Unit
            }
        }
        pendingSource = null
    }

    return remember {
        object : FilePickerState {
            override fun launchDocuments() {
                launchWithPermissions(PickerSource.DOCUMENTS) {
                    documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
                }
            }

            override fun launchGallery() {
                launchWithPermissions(PickerSource.GALLERY) {
                    galleryPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }
            }

            private fun launchWithPermissions(source: PickerSource, onGranted: () -> Unit) {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                val allGranted = permissions.all {
                    ContextCompat.checkSelfPermission(context, it) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                if (allGranted) {
                    onGranted()
                } else {
                    pendingSource = source
                    permissionLauncher.launch(permissions.toTypedArray())
                }
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

private enum class PickerSource { DOCUMENTS, GALLERY }

private val ALLOWED_MIME_TYPES = arrayOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "image/png",
    "application/pdf",
    "text/plain",
)
