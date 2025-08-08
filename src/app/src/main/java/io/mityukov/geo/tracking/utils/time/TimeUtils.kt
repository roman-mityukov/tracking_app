package io.mityukov.geo.tracking.utils.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TimeUtils {
    fun getCurrentUtcTime(): String {
        val instant = Instant.now()
        val formatter = DateTimeFormatter.ISO_INSTANT
        val startTime = formatter.format(instant)
        return startTime
    }

    fun getFormattedLocalFromUTC(utc: String, formatter: DateTimeFormatter): String {
        val local = LocalDateTime.ofInstant(
            Instant.parse(utc),
            ZoneId.systemDefault()
        ).format(formatter)
        return local
    }

//    fun durationSince(startTime: String): Duration {
//        val start = Instant.parse(startTime)
//        val current = Instant.now()
//        val diff = current.epochSecond - start.epochSecond
//        return diff.seconds
//    }

    fun durationBetween(startTime: String, endTime: String): Duration {
        val start = Instant.parse(startTime)
        val end = Instant.parse(endTime)
        val diff = end.epochSecond - start.epochSecond
        return diff.seconds
    }
}
