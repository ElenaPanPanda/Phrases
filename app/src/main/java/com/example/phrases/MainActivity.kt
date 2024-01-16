package com.example.phrases

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phrases.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: PhraseDatabase
    private lateinit var adapter: Adapter



    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()

        db = (applicationContext as PhrasesApplication).database

        val dbDao = db.getPhraseDataDao()

        if (dbDao.getAllPhrases().isEmpty()) {
            PhrasesList.forEach { quote ->
                dbDao.insert(quote)
            }
        }

        adapter = Adapter(dbDao.getAllPhrases())

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val fab = binding.fab

        fab.setOnClickListener {
            showAlertDialog()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })


        val myCallback = object : SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val phraseToDelete = dbDao.getAllPhrases()[viewHolder.adapterPosition]

                db.getPhraseDataDao().delete(phraseToDelete)
                adapter.data = dbDao.getAllPhrases()

                /*adapter.data.removeAt(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)*/
            }
        }
        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(recyclerView)


        binding.cardViewReminderSet.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1)
                }

                val reminderTime = SimpleDateFormat("HH:mm").format(calendar.time)
                binding.reminderTextView.text = getString(R.string.reminder_time, reminderTime)

                createNotification(calendar)
            }
            TimePickerDialog(
                this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun createNotification(calendar: Calendar) {
        val intent = Intent(applicationContext, BroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val chosenTime = calendar.timeInMillis

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, chosenTime, pendingIntent)
    }

    private fun showAlertDialog() {
        val alertDialogView =
            LayoutInflater.from(this).inflate(R.layout.alert_dialog_view, null, false)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Add your phrase")
            .setView(alertDialogView)
            .setPositiveButton(android.R.string.ok, null)
            .show()

        val positiveButton = alertDialogBuilder.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val phraseEditText = alertDialogView.findViewById<EditText>(R.id.phrase_dialog_et)
            val authorEditText = alertDialogView.findViewById<EditText>(R.id.author_dialog_et)

            if (phraseEditText.text.toString() != "") {
                val newPhrase = Phrase(
                    getString(R.string.quotes, phraseEditText.text),
                    authorEditText.text.toString()
                )
                db.getPhraseDataDao().insert(newPhrase)
                adapter.data = db.getPhraseDataDao().getAllPhrases()



                /*adapter.data.add(
                    Phrase(
                        getString(R.string.quotes, phraseEditText.text),
                        authorEditText.text.toString()
                    )
                )*/
                alertDialogBuilder.dismiss()
            } else
                alertDialogBuilder.dismiss()
        }
    }
}










