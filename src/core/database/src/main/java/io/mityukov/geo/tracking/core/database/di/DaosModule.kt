package io.mityukov.geo.tracking.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun providesUserTaskDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }
}
