package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "etiquettes",
    foreignKeys = [
        ForeignKey(
            entity = Piece::class,
            parentColumns = ["id"],
            childColumns = ["idPiece"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["idPiece"])]
)
data class Etiquette(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idPiece: Long,
    val texte: String,
    val x: Float = 0f,
    val y: Float = 0f
)
