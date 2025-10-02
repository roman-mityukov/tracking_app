package io.mityukov.geo.tracking.core.sync

import com.google.firebase.messaging.FirebaseMessaging
import io.mityukov.geo.tracking.log.logd
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseSyncSubscriber @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) : SyncSubscriber {
    override suspend fun subscribe() {
        val token = firebaseMessaging.token.await()
        logd("FirebaseSyncSubscriber fcm token $token")
        firebaseMessaging
            .subscribeToTopic(SYNC_TOPIC)
            .await()
    }
}
