package com.wolfpack.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.wolfpack.data.model.Nota

object ReminderScheduler {

    fun schedule(context: Context, nota: Nota) {
        if (nota.fechaRecordatorio <= System.currentTimeMillis()) {
            cancel(context, nota.uuid)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, nota)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            nota.fechaRecordatorio,
            pendingIntent
        )
    }

    fun cancel(context: Context, uuid: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, uuid)
        alarmManager.cancel(pendingIntent)
    }

    private fun pendingIntent(context: Context, nota: Nota): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID, nota.uuid.hashCode())
            putExtra(ReminderReceiver.EXTRA_TITLE, nota.titulo)
            putExtra(ReminderReceiver.EXTRA_CONTENT, nota.contenido)
        }
        return PendingIntent.getBroadcast(
            context,
            nota.uuid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntent(context: Context, uuid: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            uuid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
