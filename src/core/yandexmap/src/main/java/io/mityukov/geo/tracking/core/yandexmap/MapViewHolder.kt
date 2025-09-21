package io.mityukov.geo.tracking.core.yandexmap

import android.content.Context
import android.graphics.PointF
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.ui.R

class MapViewHolder(val mapView: MapView, private val applicationContext: Context) {
    fun currentLocationPlacemark(geolocation: Geolocation) {
        mapView.map.mapObjects.clear()
        val placemark = mapView.map.mapObjects.addPlacemark()
        placemark.apply {
            geometry = Point(geolocation.latitude, geolocation.longitude)
            setIcon(ImageProvider.fromResource(applicationContext, R.drawable.core_ui_pin_my_location))
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

    fun updateTrack(geolocations: List<Geolocation>, moveCamera: Boolean = false) {
        if (geolocations.isNotEmpty()) {
            mapView.showTrack(applicationContext, geolocations, moveCamera)
        }
    }

    fun clearMap() {
        mapView.map.mapObjects.clear()
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
