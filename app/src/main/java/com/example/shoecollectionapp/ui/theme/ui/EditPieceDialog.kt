package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
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
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Modifier la pièce",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nomPiece,
                    onValueChange = { nomPiece = it },
                    label = { Text("Nom de la pièce") },
                    leadingIcon = { Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Supprimer cette pièce", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomPiece.isNotBlank()) {
                        onConfirmSave(nomPiece)
                    }
                },
                shape = CircleShape
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
