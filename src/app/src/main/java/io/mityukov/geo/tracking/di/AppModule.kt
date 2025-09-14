package io.mityukov.geo.tracking.di

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.MainActivity
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.common.di.ApplicationId
import io.mityukov.geo.tracking.core.common.di.LogsDirectory
import io.mityukov.geo.tracking.core.model.AppInfo
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    companion object {
        @Provides
        @ApplicationId
        fun provideApplicationId(): String {
            return BuildConfig.APPLICATION_ID
        }

        @Provides
        fun provideAppInfo(): AppInfo {
            return AppInfo(
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
            )
        }

        @Provides
        fun provideNotification(@ApplicationContext context: Context): Notification {
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(
                    context,
                    AppProps.TRACK_CAPTURE_CHANNEL_ID
                )

            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            val notification = builder
                .setContentTitle(context.resources.getString(R.string.track_capture_notification_title))
                .setContentText(context.resources.getString(R.string.track_capture_notification_text))
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setOngoing(true)
                .setSilent(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build()
            return notification
        }

        @Provides
        @LogsDirectory
        fun provideLogsDirectory(@ApplicationContext context: Context): File {
            val logsDirectory = File(context.getExternalFilesDir(null), "logs")

            if (logsDirectory.exists().not()) {
                val isDirectoryCreated = logsDirectory.mkdir()
                if (isDirectoryCreated.not()) {
                    error("Can not create directory with name logs")
                }
            }
            return logsDirectory
        }
    }
}
