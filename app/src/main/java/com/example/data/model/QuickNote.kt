package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_notes")
data class QuickNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "General",
    val colorHex: String = "#06B6D4",
    val updatedAt: Long = System.currentTimeMillis()
)
