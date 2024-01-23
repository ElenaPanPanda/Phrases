package com.example.phrases

import android.app.Application
import androidx.room.Room



class PhrasesApplication : Application() {

    val database: PhraseDatabase by lazy {
        Room.databaseBuilder(
            this,
            PhraseDatabase::class.java,
            "database.db"
        ).allowMainThreadQueries().build()
    }

    companion object {
        const val NAME_PREFERENCES = "my preferences"
        const val REMINDER_KEY = "reminderTime"
    }

}