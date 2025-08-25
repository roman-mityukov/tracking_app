package io.mityukov.geo.tracking.core.data.repository.settings.app

import android.content.Context
import android.location.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationSettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocationSettingsRepository {
    override val locationEnabled: Boolean
        get() {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isLocationEnabled
        }
}
