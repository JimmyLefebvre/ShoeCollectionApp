package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sections",
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
data class Section(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val idPiece: Long,
    val nom: String,
    val x: Float = 50f,
    val y: Float = 50f,
    val width: Float = 300f,
    val height: Float = 300f
)
