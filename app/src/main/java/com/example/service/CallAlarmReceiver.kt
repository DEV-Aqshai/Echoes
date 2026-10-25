package com.example.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.EchoesApplication
import com.example.MainActivity
import com.example.R
import com.example.data.db.EchoDatabase
import com.example.data.model.EchoCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getLongExtra(EXTRA_CALL_ID, -1L)
        val title = intent.getStringExtra(EXTRA_CALL_TITLE) ?: "Incoming Call from Future Self"

        // Acquire partial wake lock briefly to ensure screen turns on
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Echoes:IncomingCallWakeLock"
        )
        wakeLock?.acquire(30000)

        // Show full screen notification
        showIncomingCallNotification(context, callId, title)

        // Handle recurring call update in background
        if (callId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = EchoDatabase.getDatabase(context)
                    val call = db.echoCallDao().getCallById(callId)
                    if (call != null) {
                        if (call.repeatType != EchoCall.REPEAT_ONCE) {
                            val nextTime = CallAlarmScheduler.calculateNextOccurrence(call)
                            if (nextTime != null) {
                                val updated = call.copy(
                                    scheduledTimestamp = nextTime,
                                    isTriggered = false
                                )
                                db.echoCallDao().updateCall(updated)
                                CallAlarmScheduler(context).scheduleCall(updated)
                            }
                        } else {
                            db.echoCallDao().updateCall(call.copy(isTriggered = true))
                        }
                    }
                } catch (e: Exception) {
                    // Ignore DB errors
                }
            }
        }
    }

    private fun showIncomingCallNotification(context: Context, callId: Long, title: String) {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_INCOMING_CALL
            putExtra(EXTRA_CALL_ID, callId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            callId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, EchoesApplication.CHANNEL_INCOMING_CALL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming Echo Call")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID_OFFSET + callId.toInt(), notificationBuilder.build())
        } catch (e: SecurityException) {
            // Notification permission might be revoked
        }
    }

    companion object {
        const val ACTION_TRIGGER_CALL = "com.example.ACTION_TRIGGER_CALL"
        const val ACTION_INCOMING_CALL = "com.example.ACTION_INCOMING_CALL"
        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_CALL_TITLE = "extra_call_title"
        private const val NOTIFICATION_ID_OFFSET = 1000
    }
}
