package io.mityukov.cicd

import io.mityukov.cicd.telegram.TelegramReleaseInfoImpl
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

enum class CiCdCommand(val command: String) {
    UPLOAD_TO_YANDEX_DISK("UPLOAD_TO_YANDEX_DISK"),
    SEND_RELEASE_INFO_TO_TELEGRAM("SEND_RELEASE_INFO_TO_TELEGRAM")
}

fun main(args: Array<String>): Unit = runBlocking {
    val command = args.first()

    val ciCdCommand = CiCdCommand.valueOf(command)

    val json = Json {
        ignoreUnknownKeys = true
    }

    when (ciCdCommand) {
        CiCdCommand.UPLOAD_TO_YANDEX_DISK -> {
            val yandexDiskApiTokenEnv = args[1]
            val fileToUploadEnv = args[2]

            val okHttpClient = OkHttpClient.Builder()
                .authenticator(object : Authenticator {
                    override fun authenticate(route: Route?, response: Response): Request? {
                        return response.request.newBuilder().header("Authorization", "OAuth $yandexDiskApiTokenEnv")
                            .build()
                    }
                })
                .build()

            val yandexDiskUploader = YandexDiskUploaderImpl(okHttpClient, Dispatchers.IO, json)
            val filePublishUrl = yandexDiskUploader.uploadFile(fileToUploadEnv)
            println(filePublishUrl)
            exitProcess(0)
        }

        CiCdCommand.SEND_RELEASE_INFO_TO_TELEGRAM -> {
            val version = args[1]
            val apkUrl = args[2]
            val changelogUrl = args[3]
            val telegramChatId = args[4]
            val telegramBotToken = args[5]

            val okHttpClient = OkHttpClient.Builder().build()

            val telegramReleaseInfoImpl = TelegramReleaseInfoImpl(okHttpClient, json)
            telegramReleaseInfoImpl.sendReleaseInfo(
                version = version,
                apkUrl = apkUrl,
                changelogUrl = changelogUrl,
                chatId = telegramChatId,
                botToken = telegramBotToken,
            )
        }
    }
}