package com.example.phrases

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phrases.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: PhraseDatabase
    private lateinit var adapter: Adapter

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            onRequestCameraPermissionResult(granted)
        }

    private val calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            when {
                weHavePermission() -> showSetTimeDialog()

                showShowExplanation() -> showExplanationDialog()

                else -> askForPermission()
            }
        }
    }

    private fun weHavePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermission() {
        cameraRequestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage("This app needs permission to access this feature.")
            .setPositiveButton("Grant") { _, _ ->
                askForPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetTimeDialog() {
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

    private fun onRequestCameraPermissionResult(granted: Boolean) {
        if (granted) {
            showSetTimeDialog()
        } else if (!showShowExplanation()) {
            // "Don't ask again"
            showWontWorkWithoutPermissionDialog()
        } else {
            // The permission was denied without checking "Don't ask again".
            // Do nothing, wait for better times…
        }
    }

    private fun showShowExplanation(): Boolean {
        return ActivityCompat
            .shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
    }

    private fun showWontWorkWithoutPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage(
                "This app needs permission to access this feature. " +
                        "Please grant it in Settings."
            )
            .setPositiveButton("Grant") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        )
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cannot open settings", Toast.LENGTH_LONG)
                .show()
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










