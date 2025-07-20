package io.mityukov.geo.tracking.core.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import io.mityukov.geo.tracking.BackgroundGeolocationWorker
import io.mityukov.geo.tracking.core.data.repository.settings.app.proto.ProtoLocalTrackCaptureStatus
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.di.TrackCaptureStatusDataStore
import javax.inject.Inject

class BackgroundGeoWorkerFactory @Inject constructor(
//    private val fusedLocationProviderClient: FusedLocationProviderClient,
//    private val currentLocationRequest: CurrentLocationRequest,
//    private val trackDao: TrackDao,
//    @TrackCaptureStatusDataStore
//    private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            BackgroundGeolocationWorker::class.java.name -> BackgroundGeolocationWorker(
//                trackDao,
//                dataStore,
//                fusedLocationProviderClient,
//                currentLocationRequest,
                appContext,
                workerParameters
            )

            else -> null
        }
    }
}
