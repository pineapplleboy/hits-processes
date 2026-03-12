package com.example.googleclass.feature.taskdetail.data.api

import com.example.googleclass.feature.taskdetail.data.model.FileModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface FileApi {

    @Multipart
    @POST(UPLOAD_FILE)
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
    ): Response<FileModel>

    @Streaming
    @GET(DOWNLOAD_FILE)
    suspend fun downloadFile(
        @Path("fileId") fileId: String,
    ): Response<ResponseBody>

    private companion object {
        const val UPLOAD_FILE = "api/v1/file/upload"
        const val DOWNLOAD_FILE = "api/v1/file/{fileId}"
    }
}
