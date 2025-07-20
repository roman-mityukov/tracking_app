package io.mityukov.cicd.yandex

interface YandexDiskUploader {
    suspend fun uploadFile(filePath: String): String
}