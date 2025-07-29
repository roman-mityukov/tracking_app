package io.mityukov.geo.tracking.app

import java.time.format.DateTimeFormatter

object GeoAppProps {
    const val TRACK_CAPTURE_CHANNEL_ID: String = "TRACK_CAPTURE_CHANNEL_ID"
    const val LOCATION_REQUEST_DURATION: Long = 30 * 1000
    const val LOCATION_MAX_UPDATE_AGE: Long = 10 * 60 * 1000
    const val LOCATION_REQUEST_INTERVAL: Long = 60000

    val UI_DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    const val EXTRA_INTENT_PAUSE = "EXTRA_INTENT_PAUSE"
}
