package io.mityukov.geo.tracking

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.uuid.ExperimentalUuidApi

class BackgroundGeolocationWorker(
//    private val trackDao: TrackDao,
//    private val dataStore: DataStore<ProtoLocalTrackCaptureStatus>,
//    private val fusedLocationProviderClient: FusedLocationProviderClient,
//    private val currentLocationRequest: CurrentLocationRequest,
    applicationContext: Context,
    workerParameters: WorkerParameters
) :
    CoroutineWorker(applicationContext, workerParameters) {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun doWork(): Result {
//        if (ActivityCompat.checkSelfPermission(
//                this.applicationContext,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                this.applicationContext,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            try {
//                val location =
//                    getCurrentLocation(fusedLocationProviderClient, currentLocationRequest)
//                logd("BackgroundGeolocationWorker location ${location?.toString()}")
//
//                val currentTrackId = dataStore.data.first().trackId
//
//                if (currentTrackId != null) {
//                    if (location != null) {
//                        trackDao.insertTrackPoint(
//                            TrackPointEntity(
//                                id = Uuid.random().toString(),
//                                trackId = currentTrackId,
//                                latitude = location.latitude,
//                                longitude = location.longitude,
//                                altitude = location.altitude,
//                                time = location.time,
//                            )
//                        )
//                    }
//                }
//
//                return Result.success()
//            } catch (e: GetCurrentLocationException) {
//                logw("BackgroundGeolocationWorker GetCurrentLocationException ${e.cause}")
//                return Result.failure()
//            }
//        } else {
//            logw("BackgroundGeolocationWorker no permissions")
//            return Result.failure()
//        }
        return Result.success()
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @SuppressLint("MissingPermission")
//    private suspend fun getCurrentLocation(
//        fusedLocationProviderClient: FusedLocationProviderClient,
//        currentLocationRequest: CurrentLocationRequest,
//    ): Location? = suspendCoroutine { continuation ->
//        fusedLocationProviderClient.getCurrentLocation(
//            currentLocationRequest,
//            null
//        ).addOnSuccessListener { location: Location? ->
//            continuation.resume(location)
//        }.addOnFailureListener { e: Exception ->
//            continuation.resumeWithException(GetCurrentLocationException(cause = e))
//        }
//    }
//
//    class GetCurrentLocationException(cause: Exception) : Exception(cause)
}
