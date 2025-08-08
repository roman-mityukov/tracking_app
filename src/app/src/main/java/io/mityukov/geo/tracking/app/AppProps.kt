package io.mityukov.geo.tracking.app

import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object AppProps {
    const val TRACK_CAPTURE_CHANNEL_ID: String = "TRACK_CAPTURE_CHANNEL_ID"
    const val TRACK_CAPTURE_NOTIFICATION_ID = 1
    const val LOCATION_REQUEST_DURATION: Long = 30 * 1000
    const val LOCATION_MAX_UPDATE_AGE: Long = 10 * 60 * 1000
    val DEFAULT_GEOLOCATION_UPDATES_INTERVAL: Duration = 10.seconds
    const val STOP_TIMEOUT_MILLISECONDS: Long = 5000L
    val UI_DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}
