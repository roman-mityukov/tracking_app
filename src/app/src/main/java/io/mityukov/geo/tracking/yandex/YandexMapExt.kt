package io.mityukov.geo.tracking.yandex

import android.content.Context
import android.graphics.PointF
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.LineStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.model.track.TrackPoint
import io.mityukov.geo.tracking.feature.map.TrackInProgress

fun Map.zoom(value: Float) {
    with(cameraPosition) {
        move(
            CameraPosition(target, zoom + value, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, YandexMapSettings.ZOOM_ANIMATION_DURATION),
            null,
        )
    }
}

fun MapView.showTrack(context: Context, trackPoints: List<TrackPoint>, moveCamera: Boolean) {
    map.mapObjects.clear()

    val points = trackPoints.map {
        Point(it.geolocation.latitude, it.geolocation.longitude)
    }
    val startImageProvider =
        ImageProvider.fromResource(context, R.drawable.pin_start)
    val finishImageProvider =
        ImageProvider.fromResource(context, R.drawable.pin_finish)
    val pinsCollection = map.mapObjects.addCollection()
    val placemarkIconStyle = IconStyle().apply {
        anchor = PointF(
            TrackAppearanceSettings.PLACEMARK_ANCHOR_X,
            TrackAppearanceSettings.PLACEMARK_ANCHOR_Y
        )
        scale = TrackAppearanceSettings.PLACEMARK_SCALE
    }

    val startPoint = points.first()
    val startPlacemark = pinsCollection.addPlacemark()
    startPlacemark.apply {
        geometry = Point(startPoint.latitude, startPoint.longitude)
        setIcon(
            if (points.size > 1) {
                startImageProvider
            } else {
                finishImageProvider
            }
        )
    }
    startPlacemark.setIconStyle(placemarkIconStyle)

    if (points.size > 1) {
        val finishPoint = points.last()
        val finishPlacemark = pinsCollection.addPlacemark()
        finishPlacemark.apply {
            geometry = Point(finishPoint.latitude, finishPoint.longitude)
            setIcon(finishImageProvider)
        }
        finishPlacemark.setIconStyle(placemarkIconStyle)
    }

    val geometry = if (points.size > 1) {
        val polyline = Polyline(points)
        val polylineObject = map.mapObjects.addPolyline(polyline)
        polylineObject.apply {
            style = LineStyle().apply {
                strokeWidth = 2f
                setStrokeColor(ContextCompat.getColor(context, R.color.teal_700))
            }
        }

        Geometry.fromPolyline(polyline)
    } else {
        Geometry.fromPoint(points.first())
    }

    if (moveCamera) {
        val position = map.cameraPosition(geometry)
        map.move(
            CameraPosition(
                position.target,
                position.zoom - TrackAppearanceSettings.ZOOM_OUT_CORRECTION_DETAILS,
                position.azimuth,
                position.tilt
            )
        )
    }
}
