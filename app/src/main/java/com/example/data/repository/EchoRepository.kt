package com.example.data.repository

import android.content.Context
import com.example.data.db.EchoDatabase
import com.example.data.model.EchoCall
import com.example.service.CallAlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.io.File

class EchoRepository(private val context: Context) {
    private val db = EchoDatabase.getDatabase(context)
    private val dao = db.echoCallDao()
    private val alarmScheduler = CallAlarmScheduler(context)

    val allCalls: Flow<List<EchoCall>> = dao.getAllCalls()

    suspend fun getCallById(id: Long): EchoCall? {
        return dao.getCallById(id)
    }

    suspend fun saveCall(call: EchoCall): Long {
        val id = dao.insertCall(call)
        val insertedCall = call.copy(id = id)
        alarmScheduler.scheduleCall(insertedCall)
        return id
    }

    suspend fun updateCall(call: EchoCall) {
        dao.updateCall(call)
        alarmScheduler.cancelCall(call.id)
        alarmScheduler.scheduleCall(call)
    }

    suspend fun deleteCall(call: EchoCall) {
        alarmScheduler.cancelCall(call.id)
        dao.deleteCall(call)
        try {
            val audioFile = File(call.audioFilePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }
        } catch (e: Exception) {
            // Ignore file deletion error
        }
    }
}
