package com.example.googleclass.feature.taskdetail.presentation

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class FilePickerDelegate(
    registry: ActivityResultRegistry,
    private val contentResolver: ContentResolver,
    private val context: android.content.Context,
) {

    var onFilePicked: ((uri: Uri, displayName: String) -> Unit)? = null

    private val documentPickerLauncher: ActivityResultLauncher<Array<String>> =
        registry.register(
            DOCUMENT_PICKER_KEY,
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                val displayName = resolveFileName(uri)
                onFilePicked?.invoke(uri, displayName)
            }
        }

    private val galleryPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        registry.register(
            GALLERY_PICKER_KEY,
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) {
                val displayName = resolveFileName(uri)
                onFilePicked?.invoke(uri, displayName)
            }
        }

    private var pendingSource: PickerSource? = null

    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        registry.register(
            PERMISSION_KEY,
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                when (pendingSource) {
                    PickerSource.DOCUMENTS -> documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
                    PickerSource.GALLERY -> launchGalleryPicker()
                    null -> Unit
                }
            }
            pendingSource = null
        }

    fun launchDocuments() {
        launchWithPermissions(PickerSource.DOCUMENTS)
    }

    fun launchGallery() {
        launchWithPermissions(PickerSource.GALLERY)
    }

    private fun launchWithPermissions(source: PickerSource) {
        val permissions = requiredPermissions()

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            when (source) {
                PickerSource.DOCUMENTS -> documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
                PickerSource.GALLERY -> launchGalleryPicker()
            }
        } else {
            pendingSource = source
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun launchGalleryPicker() {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    fun unregister() {
        documentPickerLauncher.unregister()
        galleryPickerLauncher.unregister()
        permissionLauncher.unregister()
    }

    private fun requiredPermissions(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
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

    private enum class PickerSource { DOCUMENTS, GALLERY }

    private companion object {
        const val DOCUMENT_PICKER_KEY = "file_picker"
        const val GALLERY_PICKER_KEY = "gallery_picker"
        const val PERMISSION_KEY = "storage_permission"

        /** docx, png, pdf, txt */
        val ALLOWED_MIME_TYPES = arrayOf(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "application/pdf",
            "text/plain",
        )
    }
}
