package com.example.phrases

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Phrase::class], version = 1)
abstract class PhraseDatabase : RoomDatabase()  {
    abstract fun getPhraseDataDao(): PhraseDataDao
}