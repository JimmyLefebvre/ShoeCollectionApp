package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.layout.Column
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

@Composable
fun AddSectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nomSection by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nouvelle Section de rangement") },
        text = {
            Column {
                OutlinedTextField(
                    value = nomSection,
                    onValueChange = { nomSection = it },
                    label = { Text("Nom (ex: Mur de droite, Près du radiateur)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomSection.isNotBlank()) {
                        onConfirm(nomSection)
                    }
                }
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
