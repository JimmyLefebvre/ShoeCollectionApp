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
import com.example.shoecollectionapp.utils.ImageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        _selectedPieceId.value = pieceId
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
            _selectedPieceId.value = id
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
            repository.deletePiece(pieceId)
            if (_selectedPieceId.value == pieceId) {
                _selectedPieceId.value = pieces.value.firstOrNull { it.id != pieceId }?.id
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
        posX: Float = 0f,
        posY: Float = 0f
    ) {
        viewModelScope.launch {
            val localPath = imageUri?.let {
                ImageUtils.saveImageToInternalStorage(getApplication(), it)
            } ?: ""

            val normalizedBrand = BrandUtils.normalizeBrand(marque)

            repository.insertChaussure(
                Chaussure(
                    idPiece = pieceId,
                    nom = nom.trim(),
                    marque = normalizedBrand,
                    description = description.trim(),
                    photoPath = localPath,
                    posX = posX,
                    posY = posY
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
            if (newImageUri != null) {
                photoPath = ImageUtils.saveImageToInternalStorage(getApplication(), newImageUri) ?: ""
            }

            val normalizedBrand = BrandUtils.normalizeBrand(marque)

            repository.updateChaussure(
                chaussure.copy(
                    idPiece = newPieceId,
                    nom = nom.trim(),
                    marque = normalizedBrand,
                    description = description.trim(),
                    photoPath = photoPath
                )
            )
        }
    }

    fun toggleFavorite(chaussure: Chaussure) {
        viewModelScope.launch {
            repository.updateChaussure(chaussure.copy(isFavorite = !chaussure.isFavorite))
        }
    }

    fun deleteChaussure(chaussureId: Long) {
        viewModelScope.launch {
            repository.deleteChaussure(chaussureId)
        }
    }

    fun updateShoePosition(id: Long, x: Float, y: Float, sectionId: Long?) {
        viewModelScope.launch {
            repository.updateChaussurePosition(id, x, y, sectionId)
        }
    }
}
