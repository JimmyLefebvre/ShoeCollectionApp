package com.example.shoecollectionapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.ui.theme.ShoeCollectionAppTheme
import com.example.shoecollectionapp.ui.theme.ui.AddChaussureDialog
import com.example.shoecollectionapp.ui.theme.ui.AddEtiquetteDialog
import com.example.shoecollectionapp.ui.theme.ui.AddPieceDialog
import com.example.shoecollectionapp.ui.theme.ui.CollectionScreen
import com.example.shoecollectionapp.ui.theme.ui.ConfirmDeleteDialog
import com.example.shoecollectionapp.ui.theme.ui.EditChaussureDialog
import com.example.shoecollectionapp.ui.theme.ui.EditEtiquetteDialog
import com.example.shoecollectionapp.ui.theme.ui.EditPieceDialog
import com.example.shoecollectionapp.ui.theme.ui.OptionsScreen
import com.example.shoecollectionapp.ui.theme.ui.StatsScreen
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Collection, 1 = Stats, 2 = Options

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val pieces by viewModel.pieces.collectAsState()
    val etiquettes by viewModel.etiquettes.collectAsState()
    val chaussures by viewModel.chaussures.collectAsState()
    val allChaussures by viewModel.allChaussures.collectAsState()
    val selectedPieceId by viewModel.selectedPieceId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedBrandFilter by viewModel.selectedBrandFilter.collectAsState()

    val existingBrands = remember(allChaussures) {
        allChaussures.map { it.marque }.filter { it.isNotBlank() }.distinct()
    }

    // Positions de création au centre de l'écran courant
    // Étiquette : position en dp. Boîte : case (colonne, rangée).
    var creationPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var creationCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Gestion des dialogues
    var showAddPieceDialog by remember { mutableStateOf(false) }
    var showAddEtiquetteDialog by remember { mutableStateOf(false) }
    var editingPiece by remember { mutableStateOf<Piece?>(null) }
    var editingEtiquette by remember { mutableStateOf<Etiquette?>(null) }

    var showAddChaussureDialog by remember { mutableStateOf(false) }
    var editingChaussure by remember { mutableStateOf<Chaussure?>(null) }
    var deletingChaussureId by remember { mutableStateOf<Long?>(null) }

    // Dialogue : Ajouter une pièce
    if (showAddPieceDialog) {
        AddPieceDialog(
            onDismiss = { showAddPieceDialog = false },
            onConfirm = { nom ->
                viewModel.addPiece(nom)
                showAddPieceDialog = false
            }
        )
    }

    // Dialogue : Ajouter une étiquette
    if (showAddEtiquetteDialog && selectedPieceId != null) {
        val pos = creationPosition ?: Pair(0f, 0f)
        AddEtiquetteDialog(
            onDismiss = { showAddEtiquetteDialog = false },
            onConfirm = { texte ->
                viewModel.addEtiquette(selectedPieceId!!, texte, pos.first, pos.second)
                showAddEtiquetteDialog = false
            }
        )
    }

    // Dialogue : Modifier / Supprimer une étiquette
    editingEtiquette?.let { etiquette ->
        EditEtiquetteDialog(
            etiquette = etiquette,
            onDismiss = { editingEtiquette = null },
            onConfirmSave = { newTexte ->
                viewModel.updateEtiquette(etiquette, newTexte)
                editingEtiquette = null
            },
            onDelete = {
                viewModel.deleteEtiquette(etiquette.id)
                editingEtiquette = null
            }
        )
    }

    // Dialogue : Modifier / Supprimer une pièce
    editingPiece?.let { piece ->
        EditPieceDialog(
            piece = piece,
            onDismiss = { editingPiece = null },
            onConfirmSave = { newName ->
                viewModel.updatePiece(piece.id, newName)
                editingPiece = null
            },
            onDeletePiece = {
                viewModel.deletePiece(piece.id)
                editingPiece = null
            }
        )
    }

    // Dialogue : Ajouter une chaussure
    if (showAddChaussureDialog) {
        val targetPieceId = selectedPieceId ?: pieces.firstOrNull()?.id
        val cell = creationCell ?: Pair(0, 0)
        if (targetPieceId != null) {
            AddChaussureDialog(
                existingBrands = existingBrands,
                onDismiss = { showAddChaussureDialog = false },
                onConfirm = { nom, marque, description, uri ->
                    viewModel.addChaussure(nom, marque, description, uri, targetPieceId, cell.first, cell.second)
                    showAddChaussureDialog = false
                }
            )
        }
    }

    // Dialogue : Modifier / Déplacer une chaussure
    editingChaussure?.let { chaussure ->
        EditChaussureDialog(
            chaussure = chaussure,
            pieces = pieces,
            existingBrands = existingBrands,
            onDismiss = { editingChaussure = null },
            onConfirmSave = { nom, marque, description, newUri, newPieceId ->
                viewModel.updateChaussure(chaussure, nom, marque, description, newUri, newPieceId)
                editingChaussure = null
            },
            onDelete = {
                viewModel.deleteChaussure(chaussure.id)
                editingChaussure = null
            }
        )
    }

    // Dialogue : Confirmation de suppression directe de chaussure
    deletingChaussureId?.let { id ->
        val itemToDelete = chaussures.find { it.id == id }
        val titleName = itemToDelete?.nom ?: "cette chaussure"
        ConfirmDeleteDialog(
            title = "Supprimer la chaussure",
            message = "Voulez-vous vraiment supprimer \"$titleName\" ?",
            onConfirm = {
                viewModel.deleteChaussure(id)
                deletingChaussureId = null
            },
            onDismiss = { deletingChaussureId = null }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = "Collection") },
                    label = { Text("Collection") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Statistiques") },
                    label = { Text("Stats") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Options") },
                    label = { Text("Options") }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> CollectionScreen(
                    pieces = pieces,
                    etiquettes = etiquettes,
                    chaussures = chaussures,
                    allChaussures = allChaussures,
                    selectedPieceId = selectedPieceId,
                    searchQuery = searchQuery,
                    selectedBrandFilter = selectedBrandFilter,
                    onSelectPiece = { pieceId -> viewModel.selectPiece(pieceId) },
                    onOpenAddPiece = { showAddPieceDialog = true },
                    onOpenEditPiece = { piece -> editingPiece = piece },
                    onOpenAddEtiquette = { x, y ->
                        creationPosition = Pair(x, y)
                        showAddEtiquetteDialog = true
                    },
                    onEditEtiquette = { etiquette -> editingEtiquette = etiquette },
                    onOpenAddChaussure = { col, row ->
                        creationCell = Pair(col, row)
                        showAddChaussureDialog = true
                    },
                    onOpenEditChaussure = { chaussure -> editingChaussure = chaussure },
                    onToggleFavorite = { chaussure -> viewModel.toggleFavorite(chaussure) },
                    onDeleteChaussure = { id -> deletingChaussureId = id },
                    onUpdatePosition = { id, x, y, sectionId -> viewModel.updateShoePosition(id, x, y, sectionId) },
                    onUpdateEtiquettePosition = { id, x, y -> viewModel.updateEtiquettePosition(id, x, y) },
                    onUpdateSearchQuery = { query -> viewModel.updateSearchQuery(query) },
                    onSelectBrandFilter = { brand -> viewModel.setSelectedBrandFilter(brand) }
                )

                1 -> StatsScreen(
                    pieces = pieces,
                    allChaussures = allChaussures
                )

                2 -> OptionsScreen(
                    pieces = pieces,
                    chaussures = allChaussures
                )
            }
        }
    }
}
