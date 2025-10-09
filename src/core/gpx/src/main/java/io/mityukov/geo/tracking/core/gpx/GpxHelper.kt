@file:Suppress("NestedBlockDepth")
package io.mityukov.geo.tracking.core.gpx

import io.mityukov.geo.tracking.core.common.time.TimeUtils
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import javax.inject.Inject

class GpxHelper @Inject constructor() {
    fun convertToGpx(inputFile: File, outputFile: File) {
        val gpxBuilder = StringBuilder()
        // GPX Header
        gpxBuilder.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        gpxBuilder.append("\n<gpx version=\"1.1\" creator=\"Tracking app\" ")
        gpxBuilder.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ")
        gpxBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
        gpxBuilder.append(
            "xsi:schemaLocation=" +
                    "\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n"
        )

        // Track
        gpxBuilder.append("  <trk>\n")
        gpxBuilder.append("    <name>name</name>\n")
        gpxBuilder.append("    <trkseg>\n")
        outputFile.writeText(gpxBuilder.toString())

        inputFile.useLines { lines ->
            lines.forEach {
                val parts = it.split(",")
                if (parts[0] == "point") {
                    gpxBuilder.clear()
                    gpxBuilder.append("      <trkpt lat=\"${parts[1]}\" lon=\"${parts[2]}\">\n")
                    gpxBuilder.append("        <ele>${parts[3]}</ele>\n")
                    gpxBuilder.append("        <time>${TimeUtils.getFormattedUtcTime(parts[5].toLong())}</time>\n")
                    gpxBuilder.append("        <extensions speed=\"${parts[4]}\"/>\n")
                    gpxBuilder.append("      </trkpt>\n")
                    outputFile.appendText(gpxBuilder.toString())
                }
            }
        }

        // Close tags
        gpxBuilder.clear()
        gpxBuilder.append("    </trkseg>\n")
        gpxBuilder.append("  </trk>\n")
        gpxBuilder.append("</gpx>")
        outputFile.appendText(gpxBuilder.toString())
    }

    fun geolocationsFromGpx(file: File): List<Geolocation> {
        val points = mutableListOf<Geolocation>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val inputStream = file.inputStream()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
                val lat = parser.getAttributeValue(null, "lat").toDouble()
                val lon = parser.getAttributeValue(null, "lon").toDouble()
                var ele: Double? = null
                var time: Long? = null
                var speed: Float? = null

                // Чтение вложенных элементов (ele, time)
                while (!(eventType == XmlPullParser.END_TAG && parser.name == "trkpt")) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "ele" -> ele = parser.nextText().toDouble()
                            "time" -> time = TimeUtils.getUtcMilliseconds(parser.nextText())
                            "extensions" -> speed =
                                parser.getAttributeValue(null, "speed").toFloat()
                        }
                    }
                    eventType = parser.next()
                }

                points.add(
                    Geolocation(
                        latitude = lat,
                        longitude = lon,
                        altitude = ele ?: 0.0,
                        speed = speed ?: 0f,
                        time = time ?: 0
                    )
                )
            }
            eventType = parser.next()
        }
        inputStream.close()
        return points
    }
}
