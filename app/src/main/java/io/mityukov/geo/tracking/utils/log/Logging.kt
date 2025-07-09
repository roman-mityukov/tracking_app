package io.mityukov.geo.tracking.utils.log

import android.content.Context
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

fun initLogs(context: Context) {
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
}