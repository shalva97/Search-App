package com.example.searchapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val iconResId: Int? = null,
    val usageCount: Int = 0,
    val lastOpenedTime: Long = 0,
    val isSystemApp: Boolean = false
)
