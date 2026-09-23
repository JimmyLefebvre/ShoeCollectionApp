package com.example.shoecollectionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoecollectionapp.ui.theme.ui.AddChaussureDialog
import com.example.shoecollectionapp.ui.theme.ui.AddPieceDialog
import com.example.shoecollectionapp.ui.theme.ui.ChaussureItem
import com.example.shoecollectionapp.ui.theme.ShoeCollectionAppTheme
import com.example.shoecollectionapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoeCollectionAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val pieces by viewModel.pieces.collectAsState()
    val chaussures by viewModel.chaussures.collectAsState()
    val selectedPieceId by viewModel.selectedPieceId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    var showAddPieceDialog by remember { mutableStateOf(false) }
    var showAddChaussureDialog by remember { mutableStateOf(false) }

    if (showAddPieceDialog) {
        AddPieceDialog(
            onDismiss = { showAddPieceDialog = false },
            onConfirm = { nom ->
                viewModel.addPiece(nom)
                showAddPieceDialog = false
            }
        )
    }

    if (showAddChaussureDialog && selectedPieceId != null) {
        AddChaussureDialog(
            onDismiss = { showAddChaussureDialog = false },
            onConfirm = { nom, age, uri ->
                viewModel.addChaussure(nom, age, uri, selectedPieceId!!)
                showAddChaussureDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestionnaire de Collection",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- BARRE DES PIÈCES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Pièces", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showAddPieceDialog = true }) {
                Text("+ Pièce")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pieces) { piece ->
                FilterChip(
                    selected = piece.id == selectedPieceId,
                    onClick = { viewModel.selectPiece(piece.id) },
                    label = { Text(piece.nom) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedPieceId != null) {
            // --- BARRERE DE RECHERCHE ET TRI ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Rechercher une chaussure...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { viewModel.toggleSortOrder() }) {
                    Text(if (sortAscending) "Tri : Âge ↗" else "Tri : Âge ↘")
                }

                Button(onClick = { showAddChaussureDialog = true }) {
                    Text("+ Chaussure")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LISTE DES CHAUSSURES ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chaussures) { chaussure ->
                    ChaussureItem(
                        chaussure = chaussure,
                        onDelete = { viewModel.deleteChaussure(chaussure.id) }
                    )
                }
            }
        } else {
            Text(
                text = "Sélectionnez ou créez une pièce pour voir vos chaussures.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}