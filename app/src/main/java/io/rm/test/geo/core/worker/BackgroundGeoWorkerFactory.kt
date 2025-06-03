package io.rm.test.geo.core.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import io.rm.test.geo.BackgroundGeolocationWorker
import javax.inject.Inject

class BackgroundGeoWorkerFactory @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val currentLocationRequest: CurrentLocationRequest,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            BackgroundGeolocationWorker::class.java.name -> BackgroundGeolocationWorker(
                fusedLocationProviderClient,
                currentLocationRequest,
                appContext,
                workerParameters
            )

            else -> null
        }
    }
}