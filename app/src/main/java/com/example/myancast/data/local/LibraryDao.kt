package com.example.myancast.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library WHERE type = :type ORDER BY updatedAt DESC")
    fun byType(type: String): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM library WHERE type = 'history' ORDER BY updatedAt DESC LIMIT 1")
    fun lastPlayed(): Flow<LibraryEntity?>

    @Upsert
    suspend fun upsert(entity: LibraryEntity)

    @Query("DELETE FROM library WHERE id = :id AND type = :type")
    suspend fun delete(id: String, type: String)
}