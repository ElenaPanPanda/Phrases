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

class MainActivity : AppCompatActivity(), Adapter.RecyclerViewEvent {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: PhraseDatabase
    private lateinit var adapter: Adapter

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            onRequestNotificationPermissionResult(granted)
        }


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

        adapter = Adapter(dbDao.getAllPhrases(), this)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val fab = binding.fab

        fab.setOnClickListener {
            showDialogAddingPhrase()
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
            }
        }
        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(recyclerView)

        binding.cardViewReminderSet.setOnClickListener {
            when {
                weHavePermission() -> showDialogSetTime()

                shouldShowExplanation() -> showDialogExplanation()

                else -> askForPermission()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun weHavePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForPermission() {
        notificationRequestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showDialogExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage("This app needs permission to access this feature.")
            .setPositiveButton("Grant") { _, _ ->
                askForPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDialogSetTime() {
        val calendar = Calendar.getInstance()

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onRequestNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            showDialogSetTime()
        } else if (!shouldShowExplanation()) {
            showDialogWontWorkWithoutPermission()
        } else {
            //
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun shouldShowExplanation(): Boolean {
        return ActivityCompat
            .shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
    }

    private fun showDialogWontWorkWithoutPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage(
                "This app needs permission to access notifications. " +
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

    private fun showDialogAddingPhrase() {
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

                alertDialogBuilder.dismiss()
            } else
                alertDialogBuilder.dismiss()
        }
    }

    override fun onItemClick(position: Int) {}

    override fun onItemLongClick(position: Int) {
        val dataList = db.getPhraseDataDao().getAllPhrases()
        val phraseToEdit = dataList[position]

        showDialogEditPhrase(phraseToEdit)
    }

    private fun showDialogEditPhrase(phraseToEdit: Phrase) {
        val alertDialogView =
            LayoutInflater.from(this).inflate(R.layout.alert_dialog_view, null, false)

        val adQuote = alertDialogView.findViewById<EditText>(R.id.phrase_dialog_et)
        val adAuthor = alertDialogView.findViewById<EditText>(R.id.author_dialog_et)
        adQuote.setText(phraseToEdit.quote)
        adAuthor.setText(phraseToEdit.author)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Edit phrase")
            .setView(alertDialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        val positiveButton = alertDialogBuilder.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val updatedPhrase = phraseToEdit.copy(
                quote = adQuote.text.toString(),
                author = adAuthor.text.toString()
            )

            db.getPhraseDataDao().update(updatedPhrase)
            adapter.data = db.getPhraseDataDao().getAllPhrases()

            alertDialogBuilder.dismiss()
        }
    }
}










