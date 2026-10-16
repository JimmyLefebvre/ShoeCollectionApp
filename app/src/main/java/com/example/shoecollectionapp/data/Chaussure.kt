package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
    ],
    indices = [Index(value = ["idPiece"])]
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
    val gridCol: Int = 0,
    val gridRow: Int = 0,
    val isFavorite: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val positionIndex: Int = 0
)
