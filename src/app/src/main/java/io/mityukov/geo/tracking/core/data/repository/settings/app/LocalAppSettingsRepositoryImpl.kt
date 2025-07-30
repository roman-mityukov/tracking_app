package io.mityukov.geo.tracking.core.data.repository.settings.app

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.app.GeoAppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.di.AppSettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class LocalAppSettingsRepositoryImpl @Inject constructor(
    @AppSettingsDataStore private val dataStore: DataStore<ProtoLocalAppSettings>
) : LocalAppSettingsRepository {
    override suspend fun switchOnboarding() {
        val proto = dataStore.data.first()

        val newLocalAppSettings = ProtoLocalAppSettings
            .newBuilder()
            .setShowOnboarding(!proto.showOnboarding)
            .setGeolocationUpdatesRateSeconds(proto.geolocationUpdatesRateSeconds)
            .build()

        dataStore.updateData {
            newLocalAppSettings
        }
    }

    override suspend fun setGeolocationUpdatesRate(duration: Duration) {
        val proto = dataStore.data.first()

        val newLocalAppSettings = ProtoLocalAppSettings
            .newBuilder()
            .setShowOnboarding(proto.showOnboarding)
            .setGeolocationUpdatesRateSeconds(duration.inWholeSeconds.toInt())
            .build()

        dataStore.updateData {
            newLocalAppSettings
        }
    }

    override val localAppSettings: Flow<LocalAppSettings> = dataStore.data.map { proto ->
        LocalAppSettings(
            showOnboarding = proto.showOnboarding,
            geolocationUpdatesInterval = if (proto.geolocationUpdatesRateSeconds == 0) {
                GeoAppProps.DEFAULT_GEOLOCATION_UPDATES_INTERVAL
            } else {
                proto.geolocationUpdatesRateSeconds.seconds
            },
        )
    }
}
