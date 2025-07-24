package io.mityukov.cicd.yandex

import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.CoroutineContext

@Serializable
data class GetUploadUrlResponse(val href: String)

@Serializable
data class GetPublicUrlResponse(
    @SerialName("public_url")
    val publicUrl: String
)

class YandexDiskUploaderImpl(
    private val okHttpClient: OkHttpClient,
    private val coroutineContext: CoroutineContext,
    private val json: Json,
) : YandexDiskUploader {
    override suspend fun uploadFile(filePath: String): String = withContext(coroutineContext) {
        val fileName = filePath.split("/").last()
        val uploadUrl = getUploadUrl(fileName = fileName)
        uploadFileToUrl(filePath = filePath, uploadUrl = uploadUrl)
        publishFile(fileName)
        val publishUrl = getFilePublishUrl(fileName)
        publishUrl
    }

    private suspend fun getUploadUrl(fileName: String): String = withContext(coroutineContext) {
        val request = Request.Builder()
            .get()
            .url("https://cloud-api.yandex.net/v1/disk/resources/upload/?path=app:/$fileName&overwrite=true")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val getUploadUrlResponse = json.decodeFromString<GetUploadUrlResponse>(response.body.string())
        getUploadUrlResponse.href
    }

    private suspend fun uploadFileToUrl(filePath: String, uploadUrl: String) = withContext(coroutineContext) {
        "application/octet-stream".toMediaTypeOrNull()
        File(filePath)
        var request = Request.Builder()
            .put(File(filePath).asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .url(uploadUrl)
            .build()
        okHttpClient.newCall(request).execute()
    }

    private suspend fun publishFile(fileName: String) = withContext(coroutineContext) {
        val request = Request.Builder()
            .put(RequestBody.EMPTY)
            .url("https://cloud-api.yandex.net/v1/disk/resources/publish?path=app:/$fileName")
            .build()
        okHttpClient.newCall(request).execute()
    }

    private suspend fun getFilePublishUrl(fileName: String): String = withContext(coroutineContext) {
        val request = Request.Builder()
            .get()
            .url("https://cloud-api.yandex.net/v1/disk/resources?path=app:/$fileName")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val getPublicUrlResponse = json.decodeFromString<GetPublicUrlResponse>(response.body.string())
        getPublicUrlResponse.publicUrl
    }
}