package io.mityukov.geo.tracking.feature.map

import android.content.Context
import android.graphics.PointF
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.yandex.TrackAppearanceSettings
import io.mityukov.geo.tracking.yandex.YandexMapSettings
import io.mityukov.geo.tracking.yandex.navigateTo
import io.mityukov.geo.tracking.yandex.showTrack
import io.mityukov.geo.tracking.yandex.zoom

class MapViewHolder(val mapView: MapView, private val context: Context) {
    private val geolocations = mutableListOf<Geolocation>()
    fun currentLocationPlacemark(geolocation: Geolocation) {
        mapView.map.mapObjects.clear()
        val placemark = mapView.map.mapObjects.addPlacemark()
        placemark.apply {
            geometry = Point(geolocation.latitude, geolocation.longitude)
            setIcon(ImageProvider.fromResource(context, R.drawable.pin_my_location))
        }
        placemark.setIconStyle(
            IconStyle().apply {
                anchor = PointF(
                    TrackAppearanceSettings.PLACEMARK_ANCHOR_X,
                    TrackAppearanceSettings.PLACEMARK_ANCHOR_Y
                )
                scale = TrackAppearanceSettings.PLACEMARK_SCALE
            }
        )
    }

    fun updateTrack(geolocation: Geolocation) {
        geolocations.add(geolocation)
        mapView.showTrack(context, geolocations, false)
    }

    fun zoomIn() {
        mapView.map.zoom(YandexMapSettings.ZOOM_STEP)
    }

    fun zoomOut() {
        mapView.map.zoom(-YandexMapSettings.ZOOM_STEP)
    }

    fun navigateTo(geolocation: Geolocation?) {
        if (geolocation != null) {
            mapView.navigateTo(geolocation)
        }
    }

    fun onStart() {
        mapView.onStart()
    }

    fun onStop() {
        mapView.onStop()
    }
}
