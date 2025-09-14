package io.mityukov.geo.tracking.core.sharing

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.common.di.ApplicationId
import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.model.track.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class TrackShareServiceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
    @param:ApplicationId private val applicationId: String,
) : TrackShareService {
    override suspend fun prepareTrackFile(track: Track) = withContext(coroutineDispatcher) {
        val file = File(track.filePath)
        val uri =
            FileProvider.getUriForFile(context, "$applicationId.fileprovider", file)
        uri.toString()
    }

//    private fun importGpx(inputStream: InputStream): List<Geolocation> {
//        val points = mutableListOf<Geolocation>()
//        val factory = XmlPullParserFactory.newInstance()
//        factory.isNamespaceAware = true
//        val parser = factory.newPullParser()
//        parser.setInput(inputStream, null)
//
//        var eventType = parser.eventType
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
//                val lat = parser.getAttributeValue(null, "lat").toDouble()
//                val lon = parser.getAttributeValue(null, "lon").toDouble()
//                var ele: Double? = null
//                var time: Long? = null
//
//                // Чтение вложенных элементов (ele, time)
//                while (!(eventType == XmlPullParser.END_TAG && parser.name == "trkpt")) {
//                    if (eventType == XmlPullParser.START_TAG) {
//                        when (parser.name) {
//                            "ele" -> ele = parser.nextText().toDouble()
//                            "time" -> time = parser.nextText().toLong()
//                        }
//                    }
//                    eventType = parser.next()
//                }
//                points.add(Geolocation(lat, lon, ele ?: 0.0, time ?: 0))
//            }
//            eventType = parser.next()
//        }
//        return points
//    }
}
