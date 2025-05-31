package io.rm.test.geo

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import io.rm.test.geo.utils.log.logw
import java.io.File

class CurrentGeolocationWorker(applicationContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(applicationContext, workerParameters) {
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val directoryName = "logs"
        val directory = File(applicationContext.getExternalFilesDir(null), directoryName)

        if (directory.exists().not()) {
            val isDirectoryCreated = directory.mkdir()
            if (isDirectoryCreated.not()) {
                error("Can not create directory with name $directoryName")
            }
        }
        initLogs(directory)

        val powerManager =
            this.applicationContext.getSystemService(PowerManager::class.java) as PowerManager
        logd("powerManager.isPowerSaveMode " + powerManager.isPowerSaveMode)
        logd("powerManager.locationPowerSaveMode " + powerManager.locationPowerSaveMode)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.applicationContext)
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener {
            logd("onSuccess getCurrentLocation ${it?.toString()}")
        }.addOnFailureListener {
            logw("onFailure getCurrentLocation $it")
        }
        return Result.success()
    }
}