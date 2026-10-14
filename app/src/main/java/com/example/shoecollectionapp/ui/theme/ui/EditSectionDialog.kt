package com.example.shoecollectionapp.ui.theme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoecollectionapp.data.Section
import kotlin.math.roundToInt

@Composable
fun EditSectionDialog(
    section: Section,
    boxWidthDp: Float = 90f,
    boxHeightDp: Float = 45f,
    onDismiss: () -> Unit,
    onConfirmSave: (newName: String, newCols: Int, newRows: Int) -> Unit,
    onStartMoving: () -> Unit,
    onDeleteSection: () -> Unit
) {
    var nomSection by remember { mutableStateOf(section.nom) }

    val initialCols = (section.width / boxWidthDp).roundToInt().coerceIn(1, 25)
    val initialRows = (section.height / boxHeightDp).roundToInt().coerceIn(1, 25)

    var cols by remember { mutableIntStateOf(initialCols) }
    var rows by remember { mutableIntStateOf(initialRows) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Options de la section", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nomSection,
                    onValueChange = { nomSection = it },
                    label = { Text("Nom de la section") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Taille en nombre de cases",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contrôle de la largeur en colonnes (1 à 25)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Colonnes :")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (cols > 1) cols-- },
                            enabled = cols > 1
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Moins")
                        }
                        Text(
                            text = "$cols case${if (cols > 1) "s" else ""}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { if (cols < 25) cols++ },
                            enabled = cols < 25
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Plus")
                        }
                    }
                }

                // Contrôle de la hauteur en rangées (1 à 25)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rangées :")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (rows > 1) rows-- },
                            enabled = rows > 1
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Moins")
                        }
                        Text(
                            text = "$rows case${if (rows > 1) "s" else ""}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { if (rows < 25) rows++ },
                            enabled = rows < 25
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Plus")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onStartMoving()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Déplacer cette section")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDeleteSection,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer cette section")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomSection.isNotBlank()) {
                        onConfirmSave(nomSection, cols, rows)
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
