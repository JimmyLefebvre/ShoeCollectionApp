package com.example.shoecollectionapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoecollectionapp.data.AppDatabase
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.data.Section
import com.example.shoecollectionapp.data.SortOption
import com.example.shoecollectionapp.data.repository.CollectionRepository
import com.example.shoecollectionapp.data.repository.CollectionRepositoryImpl
import com.example.shoecollectionapp.utils.BrandUtils
import com.example.shoecollectionapp.utils.ImageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CollectionRepository = CollectionRepositoryImpl(
        AppDatabase.getDatabase(application).appDao()
    )
) : AndroidViewModel(application) {

    // Pièce actuellement sélectionnée (null = aucune pièce sélectionnée)
    val selectedPieceId = MutableStateFlow<Long?>(null)

    // Recherche, Tri et Filtre par marque
    val searchQuery = MutableStateFlow("")
    val sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val selectedBrandFilter = MutableStateFlow<String?>(null)

    // Liste de toutes les pièces
    val pieces: StateFlow<List<Piece>> = repository.getAllPieces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sections de la pièce sélectionnée
    @OptIn(ExperimentalCoroutinesApi::class)
    val sections: StateFlow<List<Section>> = selectedPieceId.flatMapLatest { pieceId ->
        if (pieceId == null) {
            flowOf(emptyList())
        } else {
            repository.getSectionsByPiece(pieceId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Liste de toutes les chaussures (pour les statistiques et filtres globaux)
    val allChaussures: StateFlow<List<Chaussure>> = repository.getAllChaussures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistiques pour l'écran d'accueil
    val totalPiecesCount: StateFlow<Int> = repository.getTotalPiecesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalChaussuresCount: StateFlow<Int> = repository.getTotalChaussuresCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentChaussures: StateFlow<List<Chaussure>> = repository.getRecentChaussures(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Liste dynamique des chaussures combinant réactivement la pièce, la recherche, le filtre marque et le tri
    @OptIn(ExperimentalCoroutinesApi::class)
    val chaussures: StateFlow<List<Chaussure>> = combine(
        selectedPieceId,
        searchQuery,
        selectedBrandFilter,
        sortOption
    ) { pieceId, query, brand, sort ->
        if (pieceId == null) {
            flowOf(emptyList())
        } else {
            repository.getChaussuresByPiece(pieceId, query, brand, sort)
        }
    }.flatMapLatest { it }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACTIONS PIÈCES ---
    fun addPiece(nom: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(nom)
            val newId = repository.insertPiece(Piece(nom = normalizedName))
            if (selectedPieceId.value == null) {
                selectedPieceId.value = newId
            }
        }
    }

    fun updatePiece(pieceId: Long, newNom: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(newNom)
            repository.updatePiece(Piece(id = pieceId, nom = normalizedName))
        }
    }

    fun deletePiece(pieceId: Long) {
        viewModelScope.launch {
            repository.deletePiece(pieceId)
            if (selectedPieceId.value == pieceId) {
                val remainingPieces = pieces.value.filter { it.id != pieceId }
                selectedPieceId.value = remainingPieces.firstOrNull()?.id
            }
        }
    }

    fun selectPiece(pieceId: Long?) {
        selectedPieceId.value = pieceId
    }

    // --- ACTIONS SECTIONS ---
    fun addSection(pieceId: Long, nom: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(nom)
            repository.insertSection(Section(idPiece = pieceId, nom = normalizedName, width = 360f, height = 180f))
        }
    }

    fun updateSection(section: Section, newName: String) {
        viewModelScope.launch {
            val normalizedName = BrandUtils.normalizeBrand(newName)
            repository.updateSection(section.copy(nom = normalizedName))
        }
    }

    fun updateSectionPosition(id: Long, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updateSectionPosition(id, x, y)
        }
    }

    fun updateSectionSize(id: Long, width: Float, height: Float) {
        viewModelScope.launch {
            repository.updateSectionSize(id, width, height)
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            repository.deleteSection(sectionId)
        }
    }

    // --- ACTIONS CHAUSSURES ---
    fun addChaussure(
        nom: String,
        marque: String,
        description: String,
        imageUri: Uri?,
        pieceId: Long
    ) {
        viewModelScope.launch {
            val localPath = imageUri?.let {
                ImageUtils.saveImageToInternalStorage(getApplication(), it)
            } ?: ""

            val normalizedMarque = BrandUtils.normalizeBrand(marque)

            val chaussure = Chaussure(
                nom = nom,
                marque = normalizedMarque,
                description = description,
                photoPath = localPath,
                idPiece = pieceId,
                posX = 0f,
                posY = 0f,
                positionIndex = chaussures.value.size
            )
            repository.insertChaussure(chaussure)
        }
    }

    fun updateChaussure(
        chaussure: Chaussure,
        newNom: String,
        newMarque: String,
        newDescription: String,
        newImageUri: Uri?,
        newPieceId: Long
    ) {
        viewModelScope.launch {
            val newPhotoPath = if (newImageUri != null) {
                ImageUtils.saveImageToInternalStorage(getApplication(), newImageUri) ?: chaussure.photoPath
            } else {
                chaussure.photoPath
            }

            val normalizedMarque = BrandUtils.normalizeBrand(newMarque)

            val updated = chaussure.copy(
                nom = newNom,
                marque = normalizedMarque,
                description = newDescription,
                photoPath = newPhotoPath,
                idPiece = newPieceId
            )
            repository.updateChaussure(updated)
        }
    }

    fun updateShoePosition(id: Long, x: Float, y: Float, sectionId: Long?) {
        viewModelScope.launch {
            repository.updateChaussurePosition(id, x, y, sectionId)
        }
    }

    fun toggleFavorite(chaussure: Chaussure) {
        viewModelScope.launch {
            val updated = chaussure.copy(isFavorite = !chaussure.isFavorite)
            repository.updateChaussure(updated)
        }
    }

    fun deleteChaussure(chaussureId: Long) {
        viewModelScope.launch {
            repository.deleteChaussure(chaussureId)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    fun setSelectedBrandFilter(brand: String?) {
        selectedBrandFilter.value = brand
    }
}
