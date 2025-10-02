package io.mityukov.geo.tracking.core.sync

const val SYNC_TOPIC = "sync"

interface SyncSubscriber {
    suspend fun subscribe()
}
