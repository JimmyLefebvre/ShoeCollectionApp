package com.example.shoecollectionapp.data.repository

import com.example.shoecollectionapp.data.AppDao
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import com.example.shoecollectionapp.data.Section
import com.example.shoecollectionapp.data.SortOption
import kotlinx.coroutines.flow.Flow

class CollectionRepositoryImpl(private val dao: AppDao) : CollectionRepository {

    override fun getAllPieces(): Flow<List<Piece>> = dao.getAllPieces()

    override fun getTotalPiecesCount(): Flow<Int> = dao.getTotalPiecesCount()

    override suspend fun insertPiece(piece: Piece): Long = dao.insertPiece(piece)

    override suspend fun updatePiece(piece: Piece) = dao.updatePiece(piece)

    override suspend fun deletePiece(pieceId: Long) {
        dao.deleteEtiquettesByPiece(pieceId)
        dao.deleteSectionsByPiece(pieceId)
        dao.deleteChaussuresByPiece(pieceId)
        dao.deletePiece(pieceId)
    }

    // --- ÉTIQUETTES FLOTTANTES ---
    override fun getEtiquettesByPiece(pieceId: Long): Flow<List<Etiquette>> = dao.getEtiquettesByPiece(pieceId)

    override suspend fun insertEtiquette(etiquette: Etiquette): Long = dao.insertEtiquette(etiquette)

    override suspend fun updateEtiquette(etiquette: Etiquette) = dao.updateEtiquette(etiquette)

    override suspend fun updateEtiquettePosition(id: Long, x: Float, y: Float) = dao.updateEtiquettePosition(id, x, y)

    override suspend fun deleteEtiquette(id: Long) = dao.deleteEtiquette(id)

    // --- SECTIONS ---
    override fun getSectionsByPiece(pieceId: Long): Flow<List<Section>> = dao.getSectionsByPiece(pieceId)

    override suspend fun insertSection(section: Section): Long = dao.insertSection(section)

    override suspend fun updateSection(section: Section) = dao.updateSection(section)

    override suspend fun updateSectionPosition(id: Long, x: Float, y: Float) = dao.updateSectionPosition(id, x, y)

    override suspend fun updateSectionSize(id: Long, width: Float, height: Float) = dao.updateSectionSize(id, width, height)

    override suspend fun deleteSection(sectionId: Long) = dao.deleteSection(sectionId)

    // --- CHAUSSURES ---
    override fun getChaussuresByPiece(
        pieceId: Long,
        searchQuery: String,
        brandFilter: String?,
        sortOption: SortOption
    ): Flow<List<Chaussure>> {
        return dao.getChaussuresByPiece(pieceId, searchQuery, brandFilter, sortOption.name)
    }

    override fun getAllChaussures(): Flow<List<Chaussure>> = dao.getAllChaussures()

    override fun getRecentChaussures(limit: Int): Flow<List<Chaussure>> = dao.getRecentChaussures(limit)

    override fun getTotalChaussuresCount(): Flow<Int> = dao.getTotalChaussuresCount()

    override suspend fun getChaussureById(id: Long): Chaussure? = dao.getChaussureById(id)

    override suspend fun getChaussuresSnapshot(pieceId: Long): List<Chaussure> = dao.getChaussuresSnapshot(pieceId)

    override suspend fun insertChaussure(chaussure: Chaussure) = dao.insertChaussure(chaussure)

    override suspend fun updateChaussure(chaussure: Chaussure) = dao.updateChaussure(chaussure)

    override suspend fun updateChaussurePosition(id: Long, col: Int, row: Int, sectionId: Long?) =
        dao.updateChaussurePosition(id, col, row, sectionId)

    override suspend fun deleteChaussure(chaussureId: Long) = dao.deleteChaussure(chaussureId)
}
