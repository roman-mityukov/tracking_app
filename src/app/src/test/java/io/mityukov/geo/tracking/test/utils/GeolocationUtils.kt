@file:Suppress("MagicNumber")
package io.mityukov.geo.tracking.test.utils

import io.mityukov.geo.tracking.core.model.geo.Geolocation

object GeolocationUtils {
    val mockedGeolocation = Geolocation(
        latitude = 53.696453,
        longitude = 87.439633,
        altitude = 391.0,
        speed = 1f,
        time = System.currentTimeMillis() - 10000
    )
}
