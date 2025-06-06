package io.rm.test.geo.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.rm.test.geo.core.data.repository.geo.GeolocationUpdatesRepository
import io.rm.test.geo.core.data.repository.geo.GeolocationUpdatesRepositoryImpl
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettingsRepository
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettingsRepositoryImpl
import io.rm.test.geo.core.data.repository.settings.app.proto.ProtoLocalAppSettings
import io.rm.workorder.datastore.appSettingsDataStore

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindLocalAppSettingsRepository(impl: LocalAppSettingsRepositoryImpl): LocalAppSettingsRepository

    @Binds
    fun bindCurrentLocationRepository(impl: GeolocationUpdatesRepositoryImpl): GeolocationUpdatesRepository

    companion object {
        @Provides
        fun provideDataStore(@ApplicationContext context: Context): DataStore<ProtoLocalAppSettings> {
            return context.appSettingsDataStore
        }
    }
}