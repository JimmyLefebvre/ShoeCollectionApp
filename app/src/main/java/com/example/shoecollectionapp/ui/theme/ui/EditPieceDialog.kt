package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoecollectionapp.data.Piece

@Composable
fun EditPieceDialog(
    piece: Piece,
    onDismiss: () -> Unit,
    onConfirmSave: (newName: String) -> Unit,
    onDeletePiece: () -> Unit
) {
    var nomPiece by remember { mutableStateOf(piece.nom) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Supprimer la pièce",
            message = "Voulez-vous vraiment supprimer la pièce \"${piece.nom}\" ? Toutes les chaussures associées seront également supprimées.",
            onConfirm = {
                showDeleteConfirm = false
                onDeletePiece()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Modifier la pièce") },
        text = {
            Column {
                OutlinedTextField(
                    value = nomPiece,
                    onValueChange = { nomPiece = it },
                    label = { Text("Nom de la pièce") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer cette pièce")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomPiece.isNotBlank()) {
                        onConfirmSave(nomPiece)
                    }
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
