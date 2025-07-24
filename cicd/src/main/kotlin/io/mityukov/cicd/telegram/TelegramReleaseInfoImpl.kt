package io.mityukov.cicd.telegram

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class ReleaseInfoRequest(
    @SerialName("chat_id")
    val chatId: String,
    val text: String
)

class TelegramReleaseInfoImpl(val okHttpClient: OkHttpClient, val json: Json) {
    fun sendReleaseInfo(version: String, apkUrl: String, changelogUrl: String, chatId: String, botToken: String) {

        val requestBody = ReleaseInfoRequest(
            chatId,
            "New release $version is builded \uD83D\uDE80\n" +
                    "\uD83E\uDD16 APK: $apkUrl\n" +
                    "\uD83D\uDCC4 Changelog: $changelogUrl"
        )

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$botToken/sendMessage")
            .post(json.encodeToString(requestBody).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        okHttpClient.newCall(request).execute()
    }
}