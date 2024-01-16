package com.example.phrases

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhraseDataDao {

    @Insert
    fun insert(quoteData: Phrase)

    @Delete
    fun delete(phrase: Phrase)

    @Query("SELECT * FROM phrases")
    fun getAllPhrases(): List<Phrase>



}