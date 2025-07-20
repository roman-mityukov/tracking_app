package io.mityukov.geo.tracking.yandex

import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map

fun Map.zoom(value: Float) {
    with(cameraPosition) {
        move(
            CameraPosition(target, zoom + value, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, YandexMapSettings.ZOOM_ANIMATION_DURATION),
            null,
        )
    }
}
