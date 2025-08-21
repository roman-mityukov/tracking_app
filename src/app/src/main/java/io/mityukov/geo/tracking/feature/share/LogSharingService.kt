package io.mityukov.geo.tracking.feature.share

interface LogSharingService {
    suspend fun prepareLogsFile(): String
}
