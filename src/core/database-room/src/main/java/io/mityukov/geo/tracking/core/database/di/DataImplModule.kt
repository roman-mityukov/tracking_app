package io.mityukov.geo.tracking.core.database.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.data.impl.TracksLocalDataSourceImpl
import io.mityukov.geo.tracking.core.data.repository.track.TracksLocalDataSource

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataImplModule {
    @Binds
    internal abstract fun bindsTracksLocalDataSource(impl: TracksLocalDataSourceImpl): TracksLocalDataSource
}
