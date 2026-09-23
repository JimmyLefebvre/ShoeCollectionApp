package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pieces")
data class Piece(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nom: String
)