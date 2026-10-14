package com.example.shoecollectionapp.ui.theme.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.utils.BrandUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChaussureDialog(
    chaussure: Chaussure,
    pieces: List<Piece>,
    existingBrands: List<String>,
    onDismiss: () -> Unit,
    onConfirmSave: (nom: String, marque: String, description: String, newImageUri: Uri?, newPieceId: Long) -> Unit,
    onDelete: () -> Unit
) {
    var nom by remember { mutableStateOf(chaussure.nom) }
    var marque by remember { mutableStateOf(chaussure.marque) }
    var description by remember { mutableStateOf(chaussure.description) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPieceId by remember { mutableStateOf(chaussure.idPiece) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isPieceDropdownExpanded by remember { mutableStateOf(false) }

    val brandSuggestion = remember(marque) {
        BrandUtils.findSuggestion(marque, existingBrands)
    }

    if (showImagePicker) {
        ImagePickerSheet(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri -> selectedImageUri = uri }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Supprimer la chaussure",
            message = "Voulez-vous vraiment supprimer \"${chaussure.nom}\" ?",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Modifier la chaussure") },
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
                    label = { Text("Marque") },
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
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Sélection de la pièce / pièce de destination
                ExposedDropdownMenuBox(
                    expanded = isPieceDropdownExpanded,
                    onExpandedChange = { isPieceDropdownExpanded = !isPieceDropdownExpanded }
                ) {
                    val currentPieceNom = pieces.find { it.id == selectedPieceId }?.nom ?: "Sélectionner une pièce"
                    OutlinedTextField(
                        value = currentPieceNom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pièce de rangement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPieceDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isPieceDropdownExpanded,
                        onDismissRequest = { isPieceDropdownExpanded = false }
                    ) {
                        pieces.forEach { piece ->
                            DropdownMenuItem(
                                text = { Text(piece.nom) },
                                onClick = {
                                    selectedPieceId = piece.id
                                    isPieceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aperçu et modification de la photo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val imageModel: Any = selectedImageUri ?: File(chaussure.photoPath)
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Photo de la chaussure",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { showImagePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (selectedImageUri == null) "Changer la photo" else "Nouvelle photo ✓")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer la chaussure")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isNotBlank()) {
                        onConfirmSave(nom, marque, description, selectedImageUri, selectedPieceId)
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
