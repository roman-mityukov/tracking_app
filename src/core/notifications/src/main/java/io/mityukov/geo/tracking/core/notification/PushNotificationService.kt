package io.mityukov.geo.tracking.core.notification

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.mityukov.geo.tracking.log.logd

private const val NEWS_NOTIFICATION_CHANNEL_ID =
    "io.mityukov.geo.tracking.core.notification.NEWS_NOTIFICATION_CHANNEL_ID"
private const val NEWS_NOTIFICATION_ID = 987

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
internal class PushNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        logd(
            "onMessageReceived RemoteMessage " +
                    "\nnotification ${message.notification}" +
                    "\ndata ${message.data}"
        )

        val notification = message.notification
        if (notification == null) return

        if (ContextCompat.checkSelfPermission(
                this,
                permission.POST_NOTIFICATIONS
            ) == PERMISSION_GRANTED
        ) {
            val notificationManager = NotificationManagerCompat.from(this)

            val channel = NotificationChannel(
                NEWS_NOTIFICATION_CHANNEL_ID,
                resources.getString(R.string.core_notifications_news_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat
                .Builder(
                    this,
                    NEWS_NOTIFICATION_CHANNEL_ID,
                )
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setSmallIcon(R.drawable.core_notifications_ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify(NEWS_NOTIFICATION_ID, notification)
        }
    }
}
