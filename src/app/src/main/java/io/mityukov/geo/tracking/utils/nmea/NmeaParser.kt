@file:Suppress("TooGenericExceptionCaught", "MagicNumber")

package io.mityukov.geo.tracking.utils.nmea

import io.mityukov.geo.tracking.utils.log.logd

object NmeaParser {

    /**
     * Парсит NMEA сообщение и возвращает информацию о GPS
     */
    fun parseNmeaMessage(message: String): NmeaData? {
        return try {
            when {
                message.contains("GGA") -> parseGGA(message)
                message.contains("RMC") -> parseRMC(message)
                else -> null
            }
        } catch (e: Exception) {
            logd("NmeaParser parseNmeaMessage Exception: $e for message: $message")
            null
        }
    }

    /**
     * Парсит GPGGA сообщение (Global Positioning System Fix Data)
     * Формат: $GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47
     */
    private fun parseGGA(message: String): NmeaData.GGA? {
        //logd("parse gga $message")
        val parts = message.split(",")
        if (parts.size < 15) return null

        return try {
            NmeaData.GGA(
                time = parts[1].toDoubleOrNull() ?: 0.0,
                latitude = parseLatitude(parts[2], parts[3]),
                longitude = parseLongitude(parts[4], parts[5]),
                fixQuality = parts[6].toIntOrNull() ?: 0,
                satelliteCount = parts[7].toIntOrNull() ?: 0,
                hdop = parts[8].toDoubleOrNull() ?: 0.0,
                altitude = parts[9].toDoubleOrNull() ?: 0.0,
                altitudeUnit = parts[10],
                geoidHeight = parts[11].toDoubleOrNull() ?: 0.0,
                geoidHeightUnit = parts[12]
            )
        } catch (e: Exception) {
            logd("NmeaParser parseGPGGA Exception: $e")
            null
        }
    }

    /**
     * Парсит GPRMC сообщение (Recommended Minimum sentence C)
     * Формат: $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
     */
    private fun parseRMC(message: String): NmeaData.RMC? {
        val parts = message.split(",")
        if (parts.size < 12) return null

        return try {
            NmeaData.RMC(
                time = parts[1].toDoubleOrNull() ?: 0.0,
                status = parts[2],
                latitude = parseLatitude(parts[3], parts[4]),
                longitude = parseLongitude(parts[5], parts[6]),
                speed = parts[7].toDoubleOrNull() ?: 0.0,
                course = parts[8].toDoubleOrNull() ?: 0.0,
                date = parts[9].toIntOrNull() ?: 0,
                magneticVariation = parts[10].toDoubleOrNull() ?: 0.0,
                magneticVariationDirection = parts[11]
            )
        } catch (e: Exception) {
            logd("NmeaParser parseGPRMC Exception: $e")
            null
        }
    }

    private fun parseLatitude(latString: String, direction: String): Double {
        val lat = latString.toDoubleOrNull() ?: 0.0
        val degrees = (lat / 100).toInt()
        val minutes = lat - (degrees * 100)
        val decimalDegrees = degrees + (minutes / 60.0)
        return if (direction == "S") -decimalDegrees else decimalDegrees
    }

    private fun parseLongitude(lonString: String, direction: String): Double {
        val lon = lonString.toDoubleOrNull() ?: 0.0
        val degrees = (lon / 100).toInt()
        val minutes = lon - (degrees * 100)
        val decimalDegrees = degrees + (minutes / 60.0)
        return if (direction == "W") -decimalDegrees else decimalDegrees
    }
}

sealed interface NmeaData {
    data class GGA(
        val time: Double,
        val latitude: Double,
        val longitude: Double,
        val fixQuality: Int,
        val satelliteCount: Int,
        val hdop: Double,
        val altitude: Double,
        val altitudeUnit: String,
        val geoidHeight: Double,
        val geoidHeightUnit: String
    ) : NmeaData

    data class RMC(
        val time: Double,
        val status: String,
        val latitude: Double,
        val longitude: Double,
        val speed: Double,
        val course: Double,
        val date: Int,
        val magneticVariation: Double,
        val magneticVariationDirection: String
    ) : NmeaData
}
