package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.EchoCall
import java.util.Calendar

class CallAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleCall(call: EchoCall) {
        if (alarmManager == null) return

        val intent = Intent(context, CallAlarmReceiver::class.java).apply {
            action = CallAlarmReceiver.ACTION_TRIGGER_CALL
            putExtra(CallAlarmReceiver.EXTRA_CALL_ID, call.id)
            putExtra(CallAlarmReceiver.EXTRA_CALL_TITLE, call.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            call.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = call.scheduledTimestamp

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("CallAlarmScheduler", "Scheduled call ${call.id} for $triggerTime")
        } catch (e: SecurityException) {
            Log.e("CallAlarmScheduler", "SecurityException scheduling exact alarm", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (err: Exception) {
                Log.e("CallAlarmScheduler", "Fallback alarm failed", err)
            }
        }
    }

    fun cancelCall(callId: Long) {
        if (alarmManager == null) return
        val intent = Intent(context, CallAlarmReceiver::class.java).apply {
            action = CallAlarmReceiver.ACTION_TRIGGER_CALL
            putExtra(CallAlarmReceiver.EXTRA_CALL_ID, callId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            callId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        fun calculateNextOccurrence(call: EchoCall): Long? {
            val cal = Calendar.getInstance().apply {
                timeInMillis = call.scheduledTimestamp
            }
            val now = Calendar.getInstance()

            when (call.repeatType) {
                EchoCall.REPEAT_DAILY -> {
                    while (cal.before(now)) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    return cal.timeInMillis
                }
                EchoCall.REPEAT_WEEKLY -> {
                    while (cal.before(now)) {
                        cal.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    return cal.timeInMillis
                }
                EchoCall.REPEAT_MONTHLY -> {
                    while (cal.before(now)) {
                        cal.add(Calendar.MONTH, 1)
                    }
                    return cal.timeInMillis
                }
                else -> return null
            }
        }
    }
}
