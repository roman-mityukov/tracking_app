package io.rm.test.geo

import android.annotation.SuppressLint
import android.location.GnssAntennaInfo
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.rm.test.geo.ui.theme.GeoAppTheme
import io.rm.test.geo.utils.log.initLogs
import io.rm.test.geo.utils.log.logd
import io.rm.test.geo.utils.log.logw
import java.io.File
import java.time.Duration

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val directoryName = "logs"
        val directory = File(applicationContext.getExternalFilesDir(null), directoryName)

        if (directory.exists().not()) {
            val isDirectoryCreated = directory.mkdir()
            if (isDirectoryCreated.not()) {
                error("Can not create directory with name $directoryName")
            }
        }
        initLogs(directory)
        logd(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this).toString())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LocationManager::class.java) as LocationManager

        enableEdgeToEdge()
        setContent {
            GeoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        onPermissionsGranted = {
                            logd("All permissions granted")
                            val requestBuilder =
                                PeriodicWorkRequestBuilder<CurrentGeolocationWorker>(
                                    repeatInterval = Duration.ofMinutes(5)
                                )
                            val workManager = WorkManager.getInstance(this.applicationContext)
                            workManager.enqueueUniquePeriodicWork(
                                "CurrentGeolocationWorker",
                                ExistingPeriodicWorkPolicy.REPLACE,
                                requestBuilder.build()
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Greeting(modifier: Modifier = Modifier, onPermissionsGranted: () -> Unit) {
    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    Box(modifier) {
        if (multiplePermissionsState.allPermissionsGranted) {
            LaunchedEffect(true) {
                onPermissionsGranted()
            }
            Text("All permissions granted")
        } else {
            Column {
                Text("Grant location permissions")
                Button(onClick = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Request permissions")
                }
            }
        }
    }
}