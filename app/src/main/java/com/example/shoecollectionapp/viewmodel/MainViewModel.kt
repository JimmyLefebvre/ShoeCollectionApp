package com.example.shoecollectionapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoecollectionapp.data.AppDatabase
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.utils.ImageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    // Pièce actuellement sélectionnée (null = aucune pièce sélectionnée)
    val selectedPieceId = MutableStateFlow<Long?>(null)

    // Recherche et Tri
    val searchQuery = MutableStateFlow("")
    val sortAscending = MutableStateFlow(true)

    // Liste de toutes les pièces
    val pieces: StateFlow<List<Piece>> = dao.getAllPieces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Liste dynamique des chaussures selon la pièce, le filtre et le tri
    @OptIn(ExperimentalCoroutinesApi::class)
    val chaussures: StateFlow<List<Chaussure>> = selectedPieceId.flatMapLatest { pieceId ->
        if (pieceId == null) {
            MutableStateFlow(emptyList())
        } else {
            dao.getChaussuresByPiece(pieceId, searchQuery.value, sortAscending.value)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACTIONS PIÈCES ---
    fun addPiece(nom: String) {
        viewModelScope.launch {
            dao.insertPiece(Piece(nom = nom))
        }
    }

    fun selectPiece(pieceId: Long) {
        selectedPieceId.value = pieceId
    }

    // --- ACTIONS CHAUSSURES ---
    fun addChaussure(
        nom: String,
        age: String,
        imageUri: Uri?,
        pieceId: Long
    ) {
        viewModelScope.launch {
            val localPath = imageUri?.let {
                ImageUtils.saveImageToInternalStorage(getApplication(), it)
            } ?: ""

            val chaussure = Chaussure(
                nom = nom,
                age = age,
                photoPath = localPath,
                idPiece = pieceId,
                positionIndex = chaussures.value.size
            )
            dao.insertChaussure(chaussure)
        }
    }

    fun deleteChaussure(chaussureId: Long) {
        viewModelScope.launch {
            dao.deleteChaussure(chaussureId)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleSortOrder() {
        sortAscending.value = !sortAscending.value
    }
}