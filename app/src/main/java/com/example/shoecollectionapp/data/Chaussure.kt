package com.example.shoecollectionapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chaussures")
data class Chaussure(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nom: String,
    val age: String,              // Âge en mois ou année d'achat
    val photoPath: String,     // Chemin d'accès local vers la photo
    val idPiece: Long,         // Identifiant de la pièce associée
    val positionIndex: Int     // Position dans la grille (pour le glisser-déposer)
)