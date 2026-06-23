package com.habittrackerapp.viewmodel

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habittrackerapp.model.Habit
import java.util.Calendar

object NotificationHelper {

    private const val CHANNEL_ID = "habit_reminders"
    private const val CHANNEL_NAME = "Habit Reminders"
    private const val AFFIRMATION_CHANNEL_ID = "daily_affirmation"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        )
        nm.createNotificationChannel(
            NotificationChannel(AFFIRMATION_CHANNEL_ID, "Daily Affirmation", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    fun schedule(context: Context, habit: Habit) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = habit.id.hashCode()
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Time for: ${habit.icon} ${habit.name}")
            putExtra("body", randomMotivation())
            putExtra("id", requestCode)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!habit.hasReminder) {
            am.cancel(pending)
            return
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            set(Calendar.MINUTE, habit.reminderMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }

    fun cancel(context: Context, habit: Habit) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, habit.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pending)
    }

    fun rescheduleAll(context: Context, habits: List<Habit>) {
        habits.forEach { schedule(context, it) }
        scheduleAffirmations(context)
    }

    fun scheduleAffirmations(context: Context, hour: Int = 8, minute: Int = 0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val affirmations = listOf(
            "I attract abundance and financial freedom into my life. 💰",
            "Money flows to me easily and effortlessly. 💵",
            "I am worthy of financial success and prosperity. ✨",
            "Every day I am building wealth and creating the life I deserve. 💎",
            "My income grows as I add more value to the world. 📈",
            "I am worthy of deep, genuine love and connection. ❤️",
            "I radiate love and attract loving relationships into my life. 💫",
            "My body is strong, healthy, and full of energy today. 💪",
            "I love and respect my body every single day. 🌿",
            "Every choice I make brings me closer to my healthiest self. 🥗",
            "I am capable of achieving everything I set my mind to. 🏆",
            "Every small step I take today leads to extraordinary results. 🎯",
            "Today I choose growth, gratitude, and greatness. 🌱",
            "I believe in myself and my ability to succeed. 🦋",
            "I am grateful for this beautiful day and all it brings. 🙏",
            "My mindset shapes my reality — today I choose positivity. ✨",
            "I am becoming the best version of myself, one habit at a time. 📚",
            "I wake up with intention and go to sleep with satisfaction. 🌙",
            "I am in charge of my story, and I write it boldly. ✍️",
            "Every morning is a fresh opportunity to become better. 🌅",
            "I show up for myself because I am worth showing up for. 💪",
            "My future self thanks me for the habits I build today. 🙏",
        ).shuffled()

        for (day in 0 until 30) {
            val requestCode = "affirmation-$day".hashCode()
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Daily Affirmation ✨")
                putExtra("body", affirmations[day % affirmations.size])
                putExtra("id", requestCode)
                putExtra("channel", AFFIRMATION_CHANNEL_ID)
            }
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            if (cal.after(Calendar.getInstance())) {
                try {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
                } catch (_: SecurityException) {
                    am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
                }
            }
        }
    }

    private fun randomMotivation(): String {
        val lines = listOf(
            "Don't break the streak! 🔥",
            "Small steps, big results. 💪",
            "You've got this! ✅",
            "Keep showing up for yourself. 🌟",
            "One more check-off today! 🎯",
        )
        return lines.random()
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "HabitTracker"
        val body = intent.getStringExtra("body") ?: ""
        val id = intent.getIntExtra("id", 0)
        val channel = intent.getStringExtra("channel") ?: "habit_reminders"

        NotificationHelper.createChannels(context)

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.scheduleAffirmations(context)
        }
    }
}
