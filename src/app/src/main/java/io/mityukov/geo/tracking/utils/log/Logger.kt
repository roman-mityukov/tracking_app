package io.mityukov.geo.tracking.utils.log

import android.content.Context
import android.util.Log
import fr.bipi.treessence.file.FileLoggerTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

object Logger {
    private var isInitialized: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = Any()
    private var initializationJob: Job? = null
    fun logd(message: String) {
        coroutineScope.launch {
            Timber.tag(TAG).log(Log.DEBUG, message)
        }
    }

    fun logw(message: String) {
        coroutineScope.launch {
            Timber.tag(TAG).log(Log.WARN, message)
        }
    }

    fun initLogs(context: Context) {
        synchronized(lock) {
            if (isInitialized || (initializationJob != null && initializationJob!!.isActive)) {
                return
            }
            initializationJob = coroutineScope.launch {
                delay(1000)
                val directoryName = "logs"
                val logsDirectory = File(context.getExternalFilesDir(null), directoryName)

                if (logsDirectory.exists().not()) {
                    val isDirectoryCreated = logsDirectory.mkdir()
                    if (isDirectoryCreated.not()) {
                        error("Can not create directory with name $directoryName")
                    }
                }

                Timber.plant(Timber.DebugTree())
                val fileLoggerTree = FileLoggerTree.Builder()
                    .withFileName("file%g.log")
                    .withDirName(logsDirectory.absolutePath)
                    .withSizeLimit(nbBytes = 1_000_000)
                    .withFileLimit(f = 3)
                    .withMinPriority(Log.DEBUG)
                    .appendToFile(true)
                    .build()
                Timber.plant(fileLoggerTree)
                initializationJob = null
                isInitialized = true
            }
        }
    }
}

private const val TAG = "GEO_APP"

fun logd(message: String) {
    Logger.logd(message)
}

fun logw(message: String) {
    Logger.logw(message)
}
