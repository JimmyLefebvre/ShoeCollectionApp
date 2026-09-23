package com.example.shoecollectionapp.ui.theme.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

@Composable
fun AddChaussureDialog(
    onDismiss: () -> Unit,
    onConfirm: (nom: String, age: String, imageUri: Uri?) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    if (showImagePicker) {
        ImagePickerSheet(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri -> selectedImageUri = uri }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ajouter une paire de chaussures") },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Âge") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = { showImagePicker = true }) {
                    Text(
                        if (selectedImageUri == null) "Sélectionner une photo"
                        else "Photo sélectionnée ✓"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isNotBlank()) {
                        // Passe directement ageText (String) sans conversion toIntOrNull()
                        onConfirm(nom, ageText, selectedImageUri)
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
