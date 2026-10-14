package com.example.shoecollectionapp.ui.theme.ui

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Piece

@Composable
fun OptionsScreen(
    pieces: List<Piece>,
    chaussures: List<Chaussure>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Options & Paramètres",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sauvegarde & Export",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val exportText = buildString {
                    appendLine("=== MA COLLECTION DE CHAUSSURES ===")
                    appendLine("Nombre de pièces : ${pieces.size}")
                    appendLine("Nombre total de paires : ${chaussures.size}")
                    appendLine()
                    pieces.forEach { piece ->
                        appendLine("Pièce : ${piece.nom}")
                        val pieceShoes = chaussures.filter { it.idPiece == piece.id }
                        if (pieceShoes.isEmpty()) {
                            appendLine("  (Aucune chaussure)")
                        } else {
                            pieceShoes.forEach { s ->
                                appendLine("  - ${s.nom} (${s.marque}) [${s.description}]")
                            }
                        }
                        appendLine()
                    }
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Export de ma ShoeCollection")
                    putExtra(Intent.EXTRA_TEXT, exportText)
                }
                context.startActivity(Intent.createChooser(intent, "Exporter la collection via"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exporter la collection (Texte / Partager)")
        }
    }
}
