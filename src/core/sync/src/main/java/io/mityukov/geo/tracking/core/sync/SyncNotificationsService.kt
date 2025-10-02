package io.mityukov.geo.tracking.core.sync

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.mityukov.geo.tracking.log.logd

internal const val SYNC_TOPIC_SENDER = "/topics/sync"

internal class SyncNotificationsService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        logd(
            "SyncNotificationsService sync RemoteMessage " +
                    "\nnotification ${message.notification}" +
                    "\ndata ${message.data}" +
                    "\nmessageId ${message.messageId}" +
                    "\nmessageType ${message.messageType}"
        )
    }
}
