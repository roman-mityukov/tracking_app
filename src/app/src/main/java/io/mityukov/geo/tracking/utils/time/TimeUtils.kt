package io.mityukov.geo.tracking.utils.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    fun getFormattedUtcTime(milliseconds: Long): String {
        val instant = Instant.ofEpochMilli(milliseconds)
        val formatter = DateTimeFormatter.ISO_INSTANT
        val formattedString = formatter.format(instant)
        return formattedString
    }

    fun getFormattedLocalFromUTC(utc: Long, formatter: DateTimeFormatter): String {
        val local = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(utc),
            ZoneId.systemDefault()
        ).format(formatter)
        return local
    }

    fun getUtcMilliseconds(string: String): Long {
        return Instant.parse(string).toEpochMilli()
    }
}
