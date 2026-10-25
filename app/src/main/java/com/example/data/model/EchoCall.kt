package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "echo_calls")
data class EchoCall(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scheduledTimestamp: Long,
    val repeatType: String = REPEAT_ONCE, // ONCE, DAILY, WEEKLY, MONTHLY
    val repeatDayOfWeek: Int? = null,     // java.util.Calendar day of week (e.g. Calendar.FRIDAY)
    val audioFilePath: String,
    val audioDurationSeconds: Int = 0,
    val customCallerName: String? = null,
    val customCallerPfpUri: String? = null,
    val ringtoneId: String = "echo_pulse",
    val callBgHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isTriggered: Boolean = false,
    val isCompleted: Boolean = false
) {
    companion object {
        const val REPEAT_ONCE = "ONCE"
        const val REPEAT_DAILY = "DAILY"
        const val REPEAT_WEEKLY = "WEEKLY"
        const val REPEAT_MONTHLY = "MONTHLY"
    }
}
