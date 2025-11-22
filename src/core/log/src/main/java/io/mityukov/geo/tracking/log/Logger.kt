package io.mityukov.geo.tracking.log

import android.annotation.SuppressLint
import android.util.Log
import fr.bipi.treessence.file.FileLoggerTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

object Logger {
    private var isInitialized: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = Any()
    private const val TAG: String = "GEO_APP"

    @SuppressLint("LogNotTimber")
    fun logd(message: String) {
        if (isInitialized) {
            coroutineScope.launch {
                Timber.tag(TAG).log(Log.DEBUG, message)
            }
        } else {
            Log.w(TAG, "log before initialization")
        }
    }

    @SuppressLint("LogNotTimber")
    fun logw(message: String) {
        if (isInitialized) {
            coroutineScope.launch {
                Timber.tag(TAG).log(Log.WARN, message)
            }
        } else {
            Log.w(TAG, "log before initialization")
        }
    }

    fun initLogs(logsDirectory: File) {
        synchronized(lock) {
            if (isInitialized) {
                return
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
            isInitialized = true
        }
    }
}

fun Any.logd(message: String) {
    Logger.logd(composeMessage(this::class.java, message))
}

fun Any.logw(message: String) {
    Logger.logw(composeMessage(this::class.java, message))
}

private fun composeMessage(clazz: Class<*>, message: String): String {
    return "${clazz.simpleName} $message"
}
