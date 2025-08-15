package io.mityukov.geo.tracking.core.data.repository.settings.app

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.app.AppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.di.AppSettingsDataStore
import io.mityukov.geo.tracking.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class LocalAppSettingsRepositoryImpl @Inject constructor(
    @param:AppSettingsDataStore private val dataStore: DataStore<ProtoLocalAppSettings>,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
) : LocalAppSettingsRepository {
    override val localAppSettings: Flow<LocalAppSettings> = dataStore.data.map { proto ->
        LocalAppSettings(
            showOnboarding = proto.showOnboarding == 0,
            geolocationUpdatesInterval = if (proto.geolocationUpdatesRateSeconds == 0) {
                AppProps.Defaults.GEOLOCATION_UPDATES_INTERVAL
            } else {
                proto.geolocationUpdatesRateSeconds.seconds
            },
        )
    }
    private val mutex = Mutex()

    override suspend fun switchOnboarding() = withContext(coroutineDispatcher) {
        mutex.withLock {
            val proto = dataStore.data.first()

            val newLocalAppSettings = ProtoLocalAppSettings
                .newBuilder(proto)
                .setShowOnboarding(
                    if (proto.showOnboarding == 0) {
                        1
                    } else {
                        0
                    }
                )
                .build()

            dataStore.updateData {
                newLocalAppSettings
            }
            Unit
        }
    }

    override suspend fun setGeolocationUpdatesRate(duration: Duration) =
        withContext(coroutineDispatcher) {
            mutex.withLock {
                val proto = dataStore.data.first()

                val newLocalAppSettings = ProtoLocalAppSettings
                    .newBuilder(proto)
                    .setGeolocationUpdatesRateSeconds(duration.inWholeSeconds.toInt())
                    .build()

                dataStore.updateData {
                    newLocalAppSettings
                }
                Unit
            }
        }

    override suspend fun resetToDefaults() = withContext(coroutineDispatcher) {
        mutex.withLock {
            dataStore.updateData {
                ProtoLocalAppSettings
                    .newBuilder()
                    .setShowOnboarding(0)
                    .setGeolocationUpdatesRateSeconds(
                        AppProps.Defaults.GEOLOCATION_UPDATES_INTERVAL.inWholeSeconds.toInt()
                    )
                    .build()
            }
            Unit
        }
    }
}
