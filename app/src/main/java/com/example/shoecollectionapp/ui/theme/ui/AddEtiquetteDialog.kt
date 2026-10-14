package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

@Composable
fun AddEtiquetteDialog(
    onDismiss: () -> Unit,
    onConfirm: (texte: String) -> Unit
) {
    var texte by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nouvelle étiquette", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = texte,
                onValueChange = { texte = it },
                label = { Text("Texte de l'étiquette (ex: Étagère Nike, Mur Est...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (texte.isNotBlank()) {
                        onConfirm(texte)
                    }
                }
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
