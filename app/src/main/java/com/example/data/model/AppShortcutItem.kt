package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_shortcuts")
data class AppShortcutItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val urlOrPackage: String,
    val iconType: String = "web", // "player", "chrome", "web", "tool", "system"
    val category: String = "Favorite",
    val isPinned: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
