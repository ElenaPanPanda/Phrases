package com.example.phrases

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phrases.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val adapter = Adapter(PhrasesList.toMutableList())

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()

        val recyclerView = binding.recyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        val fab = binding.fab

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

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
                adapter.dataList.removeAt(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
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
            TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun createNotification(calendar: Calendar) {
        val intent = Intent(applicationContext, BroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val chosenTime = calendar.timeInMillis

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, chosenTime, pendingIntent)
    }
}