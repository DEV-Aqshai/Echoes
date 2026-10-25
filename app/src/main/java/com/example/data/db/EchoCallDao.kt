package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EchoCall
import kotlinx.coroutines.flow.Flow

@Dao
interface EchoCallDao {
    @Query("SELECT * FROM echo_calls ORDER BY scheduledTimestamp ASC")
    fun getAllCalls(): Flow<List<EchoCall>>

    @Query("SELECT * FROM echo_calls WHERE id = :id LIMIT 1")
    suspend fun getCallById(id: Long): EchoCall?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: EchoCall): Long

    @Update
    suspend fun updateCall(call: EchoCall)

    @Delete
    suspend fun deleteCall(call: EchoCall)

    @Query("DELETE FROM echo_calls WHERE id = :id")
    suspend fun deleteCallById(id: Long)

    @Query("SELECT * FROM echo_calls WHERE scheduledTimestamp <= :now AND isCompleted = 0 ORDER BY scheduledTimestamp ASC")
    suspend fun getPendingCallsDue(now: Long): List<EchoCall>
}
