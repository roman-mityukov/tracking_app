package io.rm.test.geo.utils.log

import android.util.Log
import fr.bipi.treessence.file.FileLoggerTree
import timber.log.Timber
import java.io.File

private const val TAG = "GEO_APP"

fun logd(message: String) {
    Timber.tag(TAG).log(Log.DEBUG, message)
}

fun logw(message: String) {
    Timber.tag(TAG).log(Log.WARN, message)
}

fun initLogs(logsDirectory: File) {
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
}