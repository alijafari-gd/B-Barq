package com.aliJafari.bbarq

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aliJafari.bbarq.data.Outage
import saman.zamani.persiandate.PersianDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReminderReceiver : BroadcastReceiver() {
    val channelId = "outage_reminder"
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("TAG", "onReceive: received shit", )
        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")

        val titles = context.resources.getStringArray(R.array.power_reminder_titles)
        val randomTitle = titles.random()

        val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Blackout Checker", NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        notificationManager.notify(intent.hashCode(),NotificationCompat.Builder(context, channelId).setContentTitle(randomTitle)
            .setContentText("Power outage from $startTime to $endTime").setSilent(false).setGroup("outage")
            .setSmallIcon(R.drawable.electricity_caution_svgrepo_com).build())
    }
}

fun scheduleReminder(context: Context, outage: Outage) {

    fun getTimestampFromPersianDate(pDate: PersianDate): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(pDate.grgYear, pDate.grgMonth - 1, pDate.grgDay, pDate.hour, pDate.minute, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, ReminderReceiver::class.java).also {
        it.putExtra(
            "startTime" , outage.startTime
        )
        it.putExtra(
            "endTime" , outage.endTime
        )
    }
    val pDate = PersianDate().also {
        it.shYear = outage.date!!.split('/')[0].toInt()
        it.shMonth = outage.date.split('/')[1].toInt()
        it.shDay = outage.date.split('/')[2].toInt()
        it.hour = outage.startTime!!.split(':')[0].toInt()
        it.minute = outage.startTime.split(':')[1].toInt()
    }.subMinutes(30)
    val time = getTimestampFromPersianDate(pDate)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        outage.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms().not()) return
    }
    Log.e("TAG", "scheduleReminder: ${time}", )
    alarmManager.cancel(pendingIntent)
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        time,
        pendingIntent
    )
}
