package com.example.util

import android.content.Context
import android.util.Log
import java.io.File

object EchoStorage {
    private const val DIRECTORY_NAME = "echoes/records"
    const val DEFAULT_RECORD_FILENAME = "10029.mp3"

    fun getRecordsDirectory(context: Context): File {
        // Try external files dir first (no runtime permissions needed, accessible)
        val ext = context.getExternalFilesDir(null)
        val targetDir = if (ext != null) {
            File(ext, DIRECTORY_NAME)
        } else {
            File(context.filesDir, DIRECTORY_NAME)
        }

        if (!targetDir.exists()) {
            val created = targetDir.mkdirs()
            Log.d("EchoStorage", "Created records directory: ${targetDir.absolutePath}, success: $created")
        }
        return targetDir
    }

    fun getPrimaryRecordFile(context: Context): File {
        val dir = getRecordsDirectory(context)
        return File(dir, DEFAULT_RECORD_FILENAME)
    }

    fun createNewRecordFile(context: Context): File {
        val dir = getRecordsDirectory(context)
        // Primary user requested file: 10029.mp3
        return File(dir, DEFAULT_RECORD_FILENAME)
    }

    fun resolveAudioFile(context: Context, preferredPath: String?): File? {
        if (!preferredPath.isNullOrBlank()) {
            val f = File(preferredPath)
            if (f.exists() && f.length() > 0) {
                return f
            }
        }

        // Fallback to echoes/records/10029.mp3
        val defaultFile = getPrimaryRecordFile(context)
        if (defaultFile.exists() && defaultFile.length() > 0) {
            return defaultFile
        }

        // Fallback to internal files dir if stored there
        val internalFile = File(File(context.filesDir, DIRECTORY_NAME), DEFAULT_RECORD_FILENAME)
        if (internalFile.exists() && internalFile.length() > 0) {
            return internalFile
        }

        // Fallback to any file in echoes/records/
        val dir = getRecordsDirectory(context)
        val files = dir.listFiles()?.filter { it.length() > 0 }
        if (!files.isNullOrEmpty()) {
            return files.maxByOrNull { it.lastModified() }
        }

        return null
    }
}
