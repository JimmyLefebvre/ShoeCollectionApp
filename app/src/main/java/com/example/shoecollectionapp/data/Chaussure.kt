package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "chaussures",
    foreignKeys = [
        ForeignKey(
            entity = Piece::class,
            parentColumns = ["id"],
            childColumns = ["idPiece"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Chaussure(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nom: String,
    val marque: String = "",
    val description: String = "",
    val photoPath: String,
    val idPiece: Long,
    val idSection: Long? = null,
    val posX: Float = 50f,
    val posY: Float = 50f,
    val isFavorite: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val positionIndex: Int = 0
)
