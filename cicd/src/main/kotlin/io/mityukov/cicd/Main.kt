package io.mityukov.cicd

import io.mityukov.cicd.yandex.YandexDiskUploaderImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = runBlocking {
    var yandexDiskApiTokenEnv: String? = null
    var fileToUploadEnv: String? = null

    try {
        yandexDiskApiTokenEnv = args[0]
        fileToUploadEnv = args[1]

        val okHttpClient = OkHttpClient.Builder()
            .authenticator(object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    return response.request.newBuilder().header("Authorization", "OAuth $yandexDiskApiTokenEnv").build()
                }
            })
            .build()
        val coroutineContext = Dispatchers.IO
        val json = Json {
            ignoreUnknownKeys = true
        }

        val yandexDiskUploader = YandexDiskUploaderImpl(okHttpClient, coroutineContext, json)
        val filePublishUrl = yandexDiskUploader.uploadFile(fileToUploadEnv)
        println(filePublishUrl)
        exitProcess(0)
    } catch (e: Exception) {
        System.err.println(e)
        exitProcess(1)
    }
}