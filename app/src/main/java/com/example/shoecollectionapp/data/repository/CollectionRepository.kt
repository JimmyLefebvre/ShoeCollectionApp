package com.example.shoecollectionapp.data.repository

import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.data.Section
import com.example.shoecollectionapp.data.SortOption
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    // Pièces
    fun getAllPieces(): Flow<List<Piece>>
    fun getTotalPiecesCount(): Flow<Int>
    suspend fun insertPiece(piece: Piece): Long
    suspend fun updatePiece(piece: Piece)
    suspend fun deletePiece(pieceId: Long)

    // Étiquettes Flottantes
    fun getEtiquettesByPiece(pieceId: Long): Flow<List<Etiquette>>
    suspend fun insertEtiquette(etiquette: Etiquette): Long
    suspend fun updateEtiquette(etiquette: Etiquette)
    suspend fun updateEtiquettePosition(id: Long, x: Float, y: Float)
    suspend fun deleteEtiquette(id: Long)

    // Sections (Compatibilité)
    fun getSectionsByPiece(pieceId: Long): Flow<List<Section>>
    suspend fun insertSection(section: Section): Long
    suspend fun updateSection(section: Section)
    suspend fun updateSectionPosition(id: Long, x: Float, y: Float)
    suspend fun updateSectionSize(id: Long, width: Float, height: Float)
    suspend fun deleteSection(sectionId: Long)

    // Chaussures
    fun getChaussuresByPiece(pieceId: Long, searchQuery: String, brandFilter: String?, sortOption: SortOption): Flow<List<Chaussure>>
    fun getAllChaussures(): Flow<List<Chaussure>>
    fun getRecentChaussures(limit: Int = 5): Flow<List<Chaussure>>
    fun getTotalChaussuresCount(): Flow<Int>
    suspend fun insertChaussure(chaussure: Chaussure)
    suspend fun updateChaussure(chaussure: Chaussure)
    suspend fun updateChaussurePosition(id: Long, x: Float, y: Float, sectionId: Long?)
    suspend fun deleteChaussure(chaussureId: Long)
}
