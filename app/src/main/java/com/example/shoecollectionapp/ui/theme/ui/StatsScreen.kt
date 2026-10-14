package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Piece
import java.io.File

@Composable
fun StatsScreen(
    pieces: List<Piece>,
    allChaussures: List<Chaussure>
) {
    val totalPieces = pieces.size
    val totalChaussures = allChaussures.size

    // Répartition par marque
    val brandCounts = allChaussures
        .filter { it.marque.isNotBlank() }
        .groupBy { it.marque.trim() }
        .mapValues { it.value.size }
        .entries
        .sortedByDescending { it.value }

    // Répartition par pièce
    val pieceCounts = pieces.map { piece ->
        piece.nom to allChaussures.count { it.idPiece == piece.id }
    }.sortedByDescending { it.second }

    // Favoris / Meilleures chaussures
    val favoriteShoes = allChaussures.filter { it.isFavorite }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Statistiques de la Collection",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cartes résumé
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Pièces", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalPieces", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Paires", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalChaussures", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Liste avec les sections défilantes
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Favoris / Meilleures chaussures
            item {
                Text(
                    text = "⭐ Meilleures chaussures (Favoris)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (favoriteShoes.isEmpty()) {
                    Text(
                        text = "Aucune chaussure en favori pour l'instant. Marquez vos paires préférées avec l'icône cœur dans la collection !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(favoriteShoes, key = { it.id }) { shoe ->
                val pieceName = pieces.find { it.id == shoe.idPiece }?.nom ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = File(shoe.photoPath),
                            contentDescription = shoe.nom,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = shoe.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (shoe.marque.isNotBlank()) {
                                Text(text = shoe.marque, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            if (pieceName.isNotBlank()) {
                                Text(text = "Pièce : $pieceName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Section Répartition par pièce
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏠 Répartition par Pièce",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(pieceCounts) { (pieceNom, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = pieceNom, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(text = "$count paire(s)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Section Répartition par marque
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏷️ Répartition par Marque",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(brandCounts) { (brand, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = brand, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(text = "$count paire(s)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
