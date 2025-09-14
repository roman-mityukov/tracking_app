package io.mityukov.geo.tracking.core.sharing

interface LogSharingService {
    suspend fun prepareLogsFile(): String
}
