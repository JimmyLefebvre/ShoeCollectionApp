package com.example.shoecollectionapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- PIÈCES ---
    @Query("SELECT * FROM pieces")
    fun getAllPieces(): Flow<List<Piece>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPiece(piece: Piece): Long

    @Update
    suspend fun updatePiece(piece: Piece)

    @Query("DELETE FROM pieces WHERE id = :pieceId")
    suspend fun deletePiece(pieceId: Long)

    @Query("SELECT COUNT(*) FROM pieces")
    fun getTotalPiecesCount(): Flow<Int>

    // --- ÉTIQUETTES FLOTTANTES ---
    @Query("SELECT * FROM etiquettes WHERE idPiece = :pieceId")
    fun getEtiquettesByPiece(pieceId: Long): Flow<List<Etiquette>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEtiquette(etiquette: Etiquette): Long

    @Update
    suspend fun updateEtiquette(etiquette: Etiquette)

    @Query("UPDATE etiquettes SET x = :x, y = :y WHERE id = :id")
    suspend fun updateEtiquettePosition(id: Long, x: Float, y: Float)

    @Query("DELETE FROM etiquettes WHERE id = :id")
    suspend fun deleteEtiquette(id: Long)

    @Query("DELETE FROM etiquettes WHERE idPiece = :pieceId")
    suspend fun deleteEtiquettesByPiece(pieceId: Long)

    // --- SECTIONS (Compatibilité) ---
    @Query("SELECT * FROM sections WHERE idPiece = :pieceId")
    fun getSectionsByPiece(pieceId: Long): Flow<List<Section>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: Section): Long

    @Update
    suspend fun updateSection(section: Section)

    @Query("UPDATE sections SET x = :x, y = :y WHERE id = :id")
    suspend fun updateSectionPosition(id: Long, x: Float, y: Float)

    @Query("UPDATE sections SET width = :width, height = :height WHERE id = :id")
    suspend fun updateSectionSize(id: Long, width: Float, height: Float)

    @Query("DELETE FROM sections WHERE id = :sectionId")
    suspend fun deleteSection(sectionId: Long)

    @Query("DELETE FROM sections WHERE idPiece = :pieceId")
    suspend fun deleteSectionsByPiece(pieceId: Long)

    // --- CHAUSSURES ---
    @Query("""
        SELECT * FROM chaussures 
        WHERE idPiece = :pieceId 
        AND (:brandFilter IS NULL OR LOWER(marque) = LOWER(:brandFilter))
        AND (
            nom LIKE '%' || :searchQuery || '%' 
            OR marque LIKE '%' || :searchQuery || '%' 
            OR description LIKE '%' || :searchQuery || '%' 
            OR :searchQuery = ''
        )
        ORDER BY 
            CASE WHEN :sortOption = 'NAME_ASC' THEN LOWER(nom) END ASC,
            CASE WHEN :sortOption = 'NAME_DESC' THEN LOWER(nom) END DESC,
            CASE WHEN :sortOption = 'BRAND_ASC' THEN LOWER(marque) END ASC,
            CASE WHEN :sortOption = 'BRAND_DESC' THEN LOWER(marque) END DESC,
            CASE WHEN :sortOption = 'DATE_DESC' THEN createdDate END DESC,
            CASE WHEN :sortOption = 'DATE_ASC' THEN createdDate END ASC,
            id DESC
    """)
    fun getChaussuresByPiece(pieceId: Long, searchQuery: String, brandFilter: String?, sortOption: String): Flow<List<Chaussure>>

    @Query("SELECT * FROM chaussures ORDER BY id DESC LIMIT :limit")
    fun getRecentChaussures(limit: Int = 5): Flow<List<Chaussure>>

    @Query("SELECT COUNT(*) FROM chaussures")
    fun getTotalChaussuresCount(): Flow<Int>

    @Query("SELECT * FROM chaussures")
    fun getAllChaussures(): Flow<List<Chaussure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChaussure(chaussure: Chaussure)

    @Update
    suspend fun updateChaussure(chaussure: Chaussure)

    @Query("UPDATE chaussures SET posX = :x, posY = :y, idSection = :sectionId WHERE id = :id")
    suspend fun updateChaussurePosition(id: Long, x: Float, y: Float, sectionId: Long?)

    @Query("DELETE FROM chaussures WHERE id = :chaussureId")
    suspend fun deleteChaussure(chaussureId: Long)

    @Query("DELETE FROM chaussures WHERE idPiece = :pieceId")
    suspend fun deleteChaussuresByPiece(pieceId: Long)
}
