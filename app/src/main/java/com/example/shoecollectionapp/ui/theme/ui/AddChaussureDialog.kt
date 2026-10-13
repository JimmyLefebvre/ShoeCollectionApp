package com.example.shoecollectionapp.ui.theme.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.example.shoecollectionapp.utils.BrandUtils

@Composable
fun AddChaussureDialog(
    existingBrands: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, marque: String, description: String, imageUri: Uri?) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var marque by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    val brandSuggestion = remember(marque) {
        BrandUtils.findSuggestion(marque, existingBrands)
    }

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
                    label = { Text("Nom du modèle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = marque,
                    onValueChange = { marque = it },
                    label = { Text("Marque (ex: Nike, Adidas...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (brandSuggestion != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    TextButton(onClick = { marque = brandSuggestion }) {
                        Text(
                            "💡 Vouliez-vous dire \"$brandSuggestion\" ? (Cliquer pour corriger)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Remarques") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showImagePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        onConfirm(nom, marque, description, selectedImageUri)
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
