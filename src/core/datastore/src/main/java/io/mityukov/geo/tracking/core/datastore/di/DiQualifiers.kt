package io.mityukov.geo.tracking.core.datastore.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TrackCaptureStatusDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettingsDataStore
