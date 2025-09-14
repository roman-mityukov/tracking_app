package io.mityukov.geo.tracking.log

import android.util.Log
import fr.bipi.treessence.file.FileLoggerTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

object Logger {
    private var isInitialized: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = Any()
    private var initializationJob: Job? = null
    private lateinit var tag: String
    fun logd(message: String) {
        if (isInitialized.not()) {
            error("logd before initialization")
        }
        coroutineScope.launch {
            Timber.tag(tag).log(Log.DEBUG, message)
        }
    }

    fun logw(message: String) {
        if (isInitialized.not()) {
            error("logw before initialization")
        }
        coroutineScope.launch {
            Timber.tag(tag).log(Log.WARN, message)
        }
    }

    fun initLogs(logsDirectory: File, tag: String = "GEO_APP") {
        synchronized(lock) {
            if (isInitialized || (initializationJob != null && initializationJob!!.isActive)) {
                return
            }
            initializationJob = coroutineScope.launch {
                this@Logger.tag = tag
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

fun logd(message: String) {
    Logger.logd(message)
}

fun logw(message: String) {
    Logger.logw(message)
}
