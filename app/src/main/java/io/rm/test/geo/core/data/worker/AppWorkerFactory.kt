package io.rm.test.geo.core.data.worker

import androidx.work.DelegatingWorkerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWorkerFactory @Inject constructor(backgroundGeoWorkerFactory: BackgroundGeoWorkerFactory) :
    DelegatingWorkerFactory() {
    init {
        addFactory(backgroundGeoWorkerFactory)
    }
}