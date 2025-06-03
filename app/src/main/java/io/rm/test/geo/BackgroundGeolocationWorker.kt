package io.rm.test.geo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import io.rm.test.geo.utils.log.logd
import io.rm.test.geo.utils.log.logw
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BackgroundGeolocationWorker(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val currentLocationRequest: CurrentLocationRequest,
    applicationContext: Context,
    workerParameters: WorkerParameters
) :
    CoroutineWorker(applicationContext, workerParameters) {
    override suspend fun doWork(): Result {
        if (ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val location =
                    getCurrentLocation(fusedLocationProviderClient, currentLocationRequest)
                logd("BackgroundGeolocationWorker location ${location?.toString()}")
                return Result.success()
            } catch (e: GetCurrentLocationException) {
                logw("BackgroundGeolocationWorker GetCurrentLocationException ${e.cause}")
                return Result.failure()
            }
        } else {
            logw("BackgroundGeolocationWorker no permissions")
            return Result.failure()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        currentLocationRequest: CurrentLocationRequest,
    ): Location? = suspendCoroutine { continuation ->
        fusedLocationProviderClient.getCurrentLocation(
            currentLocationRequest,
            null
        ).addOnSuccessListener { location: Location? ->
            continuation.resume(location)
        }.addOnFailureListener { e: Exception ->
            continuation.resumeWithException(GetCurrentLocationException(cause = e))
        }
    }

    class GetCurrentLocationException(cause: Exception) : Exception(cause)
}