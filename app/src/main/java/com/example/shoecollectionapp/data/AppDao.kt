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
    suspend fun insertPiece(piece: Piece)

    // --- CHAUSSURES ---
    @Query("""
        SELECT * FROM chaussures 
        WHERE idPiece = :pieceId 
        AND (nom LIKE '%' || :searchQuery || '%' OR :searchQuery = '')
        ORDER BY 
            CASE WHEN :sortAscending = 1 THEN age END ASC,
            CASE WHEN :sortAscending = 0 THEN age END DESC
    """)
    fun getChaussuresByPiece(pieceId: Long, searchQuery: String, sortAscending: Boolean): Flow<List<Chaussure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChaussure(chaussure: Chaussure)

    @Update
    suspend fun updateChaussure(chaussure: Chaussure)

    @Query("DELETE FROM chaussures WHERE id = :chaussureId")
    suspend fun deleteChaussure(chaussureId: Long)
}