package io.mityukov.geo.tracking.core.sharing.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.geo.tracking.core.sharing.LogSharingService
import io.mityukov.geo.tracking.core.sharing.LogSharingServiceImpl
import io.mityukov.geo.tracking.core.sharing.TrackShareService
import io.mityukov.geo.tracking.core.sharing.TrackShareServiceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class SharingModule {
    @Binds
    internal abstract fun bindTrackShareService(impl: TrackShareServiceImpl): TrackShareService

    @Binds
    internal abstract fun bindLogSharingService(impl: LogSharingServiceImpl): LogSharingService
}
