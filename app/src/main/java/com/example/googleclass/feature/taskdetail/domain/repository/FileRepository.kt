package com.example.googleclass.feature.taskdetail.domain.repository

import android.content.ContentResolver
import android.net.Uri
import java.io.File

interface FileRepository {

    suspend fun uploadFile(
        uri: Uri,
        contentResolver: ContentResolver,
        onProgress: (percent: Int) -> Unit,
    ): Result<Unit>

    suspend fun downloadFile(
        fileId: String,
        destinationDir: File,
        onProgress: (percent: Int) -> Unit,
    ): Result<File>
}
