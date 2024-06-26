package com.example.phrases

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences(PhrasesApplication.NAME_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(PhrasesApplication.REMINDER_KEY).apply()

        val db = (context.applicationContext as PhrasesApplication).database
        val listPhrases = db.getPhraseDataDao().getAllPhrases()

        val myIntent = Intent(context, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_IMMUTABLE)

        val randomPhrase = takeRandomPhraseAndAuthor(listPhrases)

        val title =
            randomPhrase.author.ifEmpty { context.getString(R.string.your_phrase_today) }

        val notificationBuilder =
            NotificationCompat.Builder(context, createNotificationChannel(context))
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title)
                .setContentText(randomPhrase.quote)
                .setStyle(NotificationCompat.BigTextStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pIntent)

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context): String {
        var channelID = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Quote"
            val descriptionText = "quote notification channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            channelID = "quote.channel"
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        return channelID
    }

    private fun takeRandomPhraseAndAuthor(list: List<Phrase>): Phrase {
        return if (list.isEmpty()) PhrasesList.random()
        else list.random()
    }
}