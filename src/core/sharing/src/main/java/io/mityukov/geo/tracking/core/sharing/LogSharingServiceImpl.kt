package io.mityukov.geo.tracking.core.sharing

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.geo.tracking.core.common.di.ApplicationId
import io.mityukov.geo.tracking.core.common.di.DispatcherIO
import io.mityukov.geo.tracking.core.common.di.LogsDirectory
import io.mityukov.geo.tracking.log.logd
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

internal class LogSharingServiceImpl @Inject constructor(
    @param:LogsDirectory private val logsDirectory: File,
    @param:ApplicationContext private val context: Context,
    @param:DispatcherIO private val coroutineDispatcher: CoroutineDispatcher,
    @param:ApplicationId private val applicationId: String,
) : LogSharingService {
    override suspend fun prepareLogsFile(): String = withContext(coroutineDispatcher) {
        val zipDirFile = File(context.getExternalFilesDir(null), "logs.zip")
        zipDirectory(logsDirectory, zipDirFile.absolutePath)

        val uri =
            FileProvider.getUriForFile(
                context,
                "$applicationId.fileprovider",
                zipDirFile
            )

        uri.toString()
    }

    private fun zipDirectory(dir: File, zipDirName: String) {
        val logFilePathList: MutableList<String> = ArrayList()
        fun populateFilesList(dir: File) {
            val files = dir.listFiles() ?: return
            for (file in files) {
                logd("file extension ${file.extension}")
                if (file.isFile) {
                    if (file.extension != "lck") {
                        logFilePathList.add(file.absolutePath)
                    }
                } else {
                    populateFilesList(file)
                }
            }
        }

        populateFilesList(dir)
        //now zip files one by one
        //create ZipOutputStream to write to the zip file
        val fos = FileOutputStream(zipDirName)
        val zos = ZipOutputStream(fos)
        for (filePath in logFilePathList) {
            logd("Zipping $filePath")
            //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
            val ze = ZipEntry(filePath.substring(dir.absolutePath.length + 1, filePath.length))
            zos.putNextEntry(ze)
            //read the file and write to ZipOutputStream
            val fis = FileInputStream(filePath)
            val buffer = ByteArray(1024)
            var len: Int
            while ((fis.read(buffer).also { len = it }) > 0) {
                zos.write(buffer, 0, len)
            }
            zos.closeEntry()
            fis.close()
        }
        zos.close()
        fos.close()
    }
}
