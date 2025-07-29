package io.mityukov.geo.tracking.core.data.repository.track

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class TrackShareServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : TrackShareService {
    override suspend fun prepareTrackFile(track: Track) = withContext(coroutineDispatcher) {
        val gpxContent = exportToGpx(track)

        val file = File(context.getExternalFilesDir(null), "shared_track.gpx")
        file.writeText(gpxContent)

        val uri =
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        uri.toString()
    }

    private fun exportToGpx(track: Track): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val gpxBuilder = StringBuilder()

        // GPX Header
        gpxBuilder.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        gpxBuilder.append("\n<gpx version=\"1.1\" creator=\"GPS Track Exporter\" ")
        gpxBuilder.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ")
        gpxBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
        gpxBuilder.append("xsi:schemaLocation=" +
                "\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")

        // Track
        gpxBuilder.append("  <trk>\n")
        gpxBuilder.append("    <name>trackName</name>\n")
        gpxBuilder.append("    <trkseg>\n")

        // Track Points
        track.points.forEach { point ->
            val geolocation = point.geolocation
            gpxBuilder.append("      <trkpt lat=\"${geolocation.latitude}\" lon=\"${geolocation.longitude}\">\n")
            gpxBuilder.append("        <ele>${geolocation.altitude}</ele>\n")
            gpxBuilder.append("        <time>${geolocation.time}</time>\n")
            gpxBuilder.append("        <name>noname</name>\n")
            gpxBuilder.append("      </trkpt>\n")
        }

        // Close tags
        gpxBuilder.append("    </trkseg>\n")
        gpxBuilder.append("  </trk>\n")
        gpxBuilder.append("</gpx>")

        return gpxBuilder.toString()
    }
}
