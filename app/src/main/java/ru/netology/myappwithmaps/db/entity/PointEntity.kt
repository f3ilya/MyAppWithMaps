package ru.netology.myappwithmaps.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String,
)
