package com.example.phrases

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class Phrase (
    @ColumnInfo(name = "quote") val quote: String,
    @ColumnInfo(name = "author") val author: String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)