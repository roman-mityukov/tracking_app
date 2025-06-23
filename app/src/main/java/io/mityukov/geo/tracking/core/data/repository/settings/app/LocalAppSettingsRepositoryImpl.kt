package io.mityukov.geo.tracking.core.data.repository.settings.app

import androidx.datastore.core.DataStore
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.mityukov.geo.tracking.di.AppSettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalAppSettingsRepositoryImpl @Inject constructor(
    @AppSettingsDataStore private val dataStore: DataStore<ProtoLocalAppSettings>
) : LocalAppSettingsRepository {
    override suspend fun switchOnboarding() {
        val proto = dataStore.data.first()

        val newLocalAppSettings =
            ProtoLocalAppSettings.newBuilder().setShowOnboarding(!proto.showOnboarding).build()
        dataStore.updateData {
            newLocalAppSettings
        }
    }

    override val localAppSettings: Flow<LocalAppSettings> = dataStore.data.map { proto ->
        LocalAppSettings(showOnboarding = proto.showOnboarding)
    }
}