package com.example.shoecollectionapp.ui.theme.ui

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
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
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Modifier la chaussure",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du modèle") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Sell, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = marque,
                    onValueChange = { marque = it },
                    label = { Text("Marque") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Sélection de la pièce
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
                        leadingIcon = { Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
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
                                text = { Text(piece.nom, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedPieceId = piece.id
                                    isPieceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aperçu et modification de la photo
                Surface(
                    onClick = { showImagePicker = true },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imageModel: Any = selectedImageUri ?: File(chaussure.photoPath)
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Photo de la chaussure",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (selectedImageUri == null) "Photo actuelle" else "Nouvelle photo sélectionnée ✓",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Changer la photo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Supprimer la chaussure", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isNotBlank()) {
                        onConfirmSave(nom, marque, description, selectedImageUri, selectedPieceId)
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
