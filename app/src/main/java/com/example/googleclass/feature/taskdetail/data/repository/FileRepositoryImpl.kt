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
import java.net.URLDecoder
import java.util.regex.Pattern

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
            val contentType = response.headers()["Content-Type"]
            val serverFileName = parseFileNameFromContentDisposition(
                contentDisposition = contentDisposition,
                contentType = contentType,
                fallback = fileId,
            ).sanitizeFileName()

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

    /**
     * Парсит имя файла из Content-Disposition.
     * Поддерживает filename* (RFC 5987, UTF-8) и filename (в кавычках).
     */
    private fun parseFileNameFromContentDisposition(
        contentDisposition: String?,
        contentType: String?,
        fallback: String,
    ): String {
        if (!contentDisposition.isNullOrBlank()) {
            // Приоритет 1: filename*=UTF-8''urlencoded (RFC 5987)
            val filenameStarRegex = Pattern.compile(
                "filename\\*\\s*=\\s*(?:[^'\\s]*'')([^;\\s]+)",
                Pattern.CASE_INSENSITIVE
            )
            val starMatcher = filenameStarRegex.matcher(contentDisposition)
            if (starMatcher.find()) {
                return runCatching {
                    URLDecoder.decode(starMatcher.group(1), Charsets.UTF_8.name())
                }.getOrElse { fallbackWithExtension(fallback, contentType) }
            }

            // Приоритет 2: filename="..."
            val filenameRegex = Pattern.compile(
                "filename\\s*=\\s*\"([^\"]*)\"",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = filenameRegex.matcher(contentDisposition)
            if (matcher.find()) {
                val value = matcher.group(1)?.trim() ?: return fallbackWithExtension(fallback, contentType)
                if (value.isEmpty()) return fallbackWithExtension(fallback, contentType)
                // RFC 2047 (=?UTF-8?Q?...?=) — не декодируем
                if (value.startsWith("=?") && value.contains("?Q?") && value.endsWith("?=")) {
                    return fallbackWithExtension(fallback, contentType)
                }
                return value
            }
        }

        return fallbackWithExtension(fallback, contentType)
    }

    private fun fallbackWithExtension(fileId: String, contentType: String?): String {
        val ext = when {
            contentType?.contains("wordprocessingml") == true -> ".docx"
            contentType?.contains("spreadsheetml") == true -> ".xlsx"
            contentType?.contains("presentationml") == true -> ".pptx"
            contentType?.contains("pdf") == true -> ".pdf"
            contentType?.contains("image/jpeg") == true -> ".jpg"
            contentType?.contains("image/png") == true -> ".png"
            else -> ""
        }
        return if (fileId.contains(".")) fileId else "$fileId$ext"
    }

    private fun String.sanitizeFileName(): String {
        return this
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .takeIf { it.isNotBlank() }
            ?: "download"
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
