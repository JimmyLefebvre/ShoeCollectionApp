package com.example.shoecollectionapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoecollectionapp.data.AppDatabase
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.data.Section
import com.example.shoecollectionapp.data.SortOption
import com.example.shoecollectionapp.data.repository.CollectionRepositoryImpl
import com.example.shoecollectionapp.utils.BrandUtils
import com.example.shoecollectionapp.utils.GridUtils
import com.example.shoecollectionapp.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CollectionRepositoryImpl

    val pieces: StateFlow<List<Piece>>
    val allChaussures: StateFlow<List<Chaussure>>

    // Messages ponctuels pour l'UI (échec d'enregistrement de photo, etc.)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _selectedPieceId = MutableStateFlow<Long?>(null)
    val selectedPieceId: StateFlow<Long?> = _selectedPieceId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _selectedBrandFilter = MutableStateFlow<String?>(null)
    val selectedBrandFilter: StateFlow<String?> = _selectedBrandFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val etiquettes: StateFlow<List<Etiquette>> = _selectedPieceId.flatMapLatest { pieceId ->
        if (pieceId != null) repository.getEtiquettesByPiece(pieceId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val sections: StateFlow<List<Section>> = _selectedPieceId.flatMapLatest { pieceId ->
        if (pieceId != null) repository.getSectionsByPiece(pieceId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chaussures: StateFlow<List<Chaussure>> = _selectedPieceId.flatMapLatest { pieceId ->
        if (pieceId != null) repository.getChaussuresByPiece(pieceId, "", null, SortOption.DATE_DESC)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CollectionRepositoryImpl(database.appDao())

        pieces = repository.getAllPieces().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allChaussures = repository.getAllChaussures().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch {
            pieces.collect { list ->
                if (_selectedPieceId.value == null && list.isNotEmpty()) {
                    _selectedPieceId.value = list.first().id
                }
            }
        }
    }

    fun selectPiece(pieceId: Long) {
        switchToPiece(pieceId)
    }

    // Changer de pièce réinitialise recherche et filtre marque : sinon un filtre sur une
    // marque absente de la nouvelle pièce grise toutes les boîtes sans puce sélectionnée.
    private fun switchToPiece(pieceId: Long?) {
        _selectedPieceId.value = pieceId
        _searchQuery.value = ""
        _selectedBrandFilter.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setSelectedBrandFilter(brand: String?) {
        _selectedBrandFilter.value = brand
    }

    // --- ACTIONS PIÈCES ---
    fun addPiece(nom: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(nom)
            val id = repository.insertPiece(Piece(nom = normalizedName))
            switchToPiece(id)
        }
    }

    fun updatePiece(id: Long, newName: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(newName)
            repository.updatePiece(Piece(id = id, nom = normalizedName))
        }
    }

    fun deletePiece(pieceId: Long) {
        viewModelScope.launch {
            val photoPaths = repository.getChaussuresSnapshot(pieceId).map { it.photoPath }
            repository.deletePiece(pieceId)
            photoPaths.forEach { deletePhoto(it) }
            if (_selectedPieceId.value == pieceId) {
                switchToPiece(pieces.value.firstOrNull { it.id != pieceId }?.id)
            }
        }
    }

    // --- ACTIONS ÉTIQUETTES FLOTTANTES (Anti-doublon) ---
    fun addEtiquette(pieceId: Long, texte: String, x: Float = 0f, y: Float = 0f) {
        viewModelScope.launch {
            val trimmed = texte.trim()
            val existing = etiquettes.value
            val isDuplicate = existing.any { it.texte.equals(trimmed, ignoreCase = true) }
            if (trimmed.isNotBlank() && !isDuplicate) {
                repository.insertEtiquette(Etiquette(idPiece = pieceId, texte = trimmed, x = x, y = y))
            }
        }
    }

    fun updateEtiquette(etiquette: Etiquette, newTexte: String) {
        viewModelScope.launch {
            val trimmed = newTexte.trim()
            val existing = etiquettes.value
            val isDuplicate = existing.any { it.id != etiquette.id && it.texte.equals(trimmed, ignoreCase = true) }
            if (trimmed.isNotBlank() && !isDuplicate) {
                repository.updateEtiquette(etiquette.copy(texte = trimmed))
            }
        }
    }

    fun updateEtiquettePosition(id: Long, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updateEtiquettePosition(id, x, y)
        }
    }

    fun deleteEtiquette(id: Long) {
        viewModelScope.launch {
            repository.deleteEtiquette(id)
        }
    }

    // --- ACTIONS CHAUSSURES ---
    fun addChaussure(
        nom: String,
        marque: String,
        description: String,
        imageUri: Uri?,
        pieceId: Long,
        gridCol: Int = 0,
        gridRow: Int = 0
    ) {
        viewModelScope.launch {
            val localPath = if (imageUri != null) {
                savePhoto(imageUri) ?: run {
                    _events.tryEmit("Photo non enregistrée : la paire est créée sans photo.")
                    ""
                }
            } else {
                ""
            }

            val normalizedBrand = BrandUtils.normalizeBrand(marque)

            // Évite de créer une boîte par-dessus une autre
            val occupied = repository.getChaussuresSnapshot(pieceId)
                .map { Pair(it.gridCol, it.gridRow) }
                .toSet()
            val (freeCol, freeRow) = GridUtils.nearestFreeCell(gridCol, gridRow, occupied)
                ?: Pair(gridCol, gridRow)

            repository.insertChaussure(
                Chaussure(
                    idPiece = pieceId,
                    nom = nom.trim(),
                    marque = normalizedBrand,
                    description = description.trim(),
                    photoPath = localPath,
                    gridCol = freeCol,
                    gridRow = freeRow
                )
            )
        }
    }

    fun updateChaussure(
        chaussure: Chaussure,
        nom: String,
        marque: String,
        description: String,
        newImageUri: Uri?,
        newPieceId: Long
    ) {
        viewModelScope.launch {
            var photoPath = chaussure.photoPath
            var replacedPhotoPath: String? = null
            if (newImageUri != null) {
                val saved = savePhoto(newImageUri)
                if (saved != null) {
                    replacedPhotoPath = chaussure.photoPath
                    photoPath = saved
                } else {
                    _events.tryEmit("Nouvelle photo non enregistrée : l'ancienne est conservée.")
                }
            }

            val normalizedBrand = BrandUtils.normalizeBrand(marque)

            // Changement de pièce : la section de l'ancienne pièce n'existe plus ici et la case
            // d'origine peut être occupée dans la pièce de destination.
            var gridCol = chaussure.gridCol
            var gridRow = chaussure.gridRow
            var idSection = chaussure.idSection
            if (newPieceId != chaussure.idPiece) {
                val occupied = repository.getChaussuresSnapshot(newPieceId)
                    .map { Pair(it.gridCol, it.gridRow) }
                    .toSet()
                GridUtils.nearestFreeCell(gridCol, gridRow, occupied)?.let { (col, row) ->
                    gridCol = col
                    gridRow = row
                }
                idSection = null
            }

            repository.updateChaussure(
                chaussure.copy(
                    idPiece = newPieceId,
                    idSection = idSection,
                    gridCol = gridCol,
                    gridRow = gridRow,
                    nom = nom.trim(),
                    marque = normalizedBrand,
                    description = description.trim(),
                    photoPath = photoPath
                )
            )
            // L'ancien fichier n'est supprimé qu'une fois la base à jour
            replacedPhotoPath?.let { deletePhoto(it) }
        }
    }

    fun toggleFavorite(chaussure: Chaussure) {
        viewModelScope.launch {
            repository.updateChaussure(chaussure.copy(isFavorite = !chaussure.isFavorite))
        }
    }

    fun deleteChaussure(chaussureId: Long) {
        viewModelScope.launch {
            val photoPath = repository.getChaussureById(chaussureId)?.photoPath
            repository.deleteChaussure(chaussureId)
            photoPath?.let { deletePhoto(it) }
        }
    }

    // --- PHOTOS (I/O hors du thread principal) ---
    private suspend fun savePhoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        ImageUtils.saveImageToInternalStorage(getApplication(), uri)
    }

    private suspend fun deletePhoto(path: String) {
        withContext(Dispatchers.IO) {
            ImageUtils.deleteInternalImage(getApplication(), path)
        }
    }

    fun updateShoePosition(id: Long, col: Int, row: Int, sectionId: Long?) {
        viewModelScope.launch {
            repository.updateChaussurePosition(id, col, row, sectionId)
        }
    }
}
