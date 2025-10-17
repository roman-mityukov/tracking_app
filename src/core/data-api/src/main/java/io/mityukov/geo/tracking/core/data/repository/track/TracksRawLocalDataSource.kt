package io.mityukov.geo.tracking.core.data.repository.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation
import java.io.File

interface TracksRawLocalDataSource {
    fun writeGeolocation(geolocation: Geolocation)
    fun clear()
    fun readCapturedGeolocations(): List<Geolocation>
    fun writeCapturedGeolocationsAsTrack(fileName: String): String
    fun readTrackGeolocations(trackFile: File): List<Geolocation>
}
