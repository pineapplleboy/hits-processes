package com.example.googleclass.feature.taskdetail.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.feature.taskdetail.data.api.FileApi
import com.example.googleclass.feature.taskdetail.data.model.FileModel
import com.example.googleclass.feature.taskdetail.data.progress.ProgressRequestBody
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FileRepositoryImpl(
    private val fileApi: FileApi,
) : FileRepository {

    override suspend fun uploadFile(
        uri: Uri,
        contentResolver: ContentResolver,
        onProgress: (percent: Int) -> Unit,
    ): Result<FileModel> = withContext(Dispatchers.IO) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Cannot open input stream for $uri"))

            val fileName = resolveFileName(uri, contentResolver)
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            val rawBody = bytes.toRequestBody(mimeType.toMediaType())
            val progressBody = ProgressRequestBody(rawBody) { written, total ->
                if (total > 0) {
                    onProgress(((written * 100) / total).toInt().coerceIn(0, 100))
                }
            }

            val part = MultipartBody.Part.createFormData("file", fileName, progressBody)

            safeApiCall(
                apiCall = { fileApi.uploadFile(part) },
                converter = { it },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadFile(
        fileId: String,
        destinationDir: File,
        onProgress: (percent: Int) -> Unit,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val response = fileApi.downloadFile(fileId)
            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Download failed: ${response.code()}")
            }

            val body = response.body()!!
            val totalBytes = body.contentLength()

            val contentDisposition = response.headers()["Content-Disposition"]
            val serverFileName = contentDisposition
                ?.substringAfter("filename=", "")
                ?.trim('"')
                ?.takeIf { it.isNotEmpty() }
                ?: fileId

            val outputFile = File(destinationDir, serverFileName)
            var bytesRead = 0L

            body.byteStream().use { input ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        if (totalBytes > 0) {
                            onProgress(
                                ((bytesRead * 100) / totalBytes).toInt().coerceIn(0, 100),
                            )
                        }
                    }
                }
            }
            outputFile
        }
    }

    private fun resolveFileName(uri: Uri, contentResolver: ContentResolver): String {
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
}
