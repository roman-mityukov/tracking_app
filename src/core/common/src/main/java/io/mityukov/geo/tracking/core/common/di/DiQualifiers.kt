package io.mityukov.geo.tracking.core.common.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefault

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefaultLimitedParallelism

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LogsDirectory
