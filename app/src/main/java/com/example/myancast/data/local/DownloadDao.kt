package com.example.myancast.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE episodeId = :episodeId")
    suspend fun get(episodeId: String): DownloadEntity?

    @Upsert
    suspend fun upsert(entity: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress, updatedAt = :now WHERE episodeId = :episodeId")
    suspend fun updateProgress(episodeId: String, status: String, progress: Int, now: Long)

    @Query(
        "UPDATE downloads SET status = 'done', progress = 100, localPath = :localPath, " +
            "downloadedBytes = :sizeBytes, updatedAt = :now WHERE episodeId = :episodeId"
    )
    suspend fun markDone(episodeId: String, localPath: String, sizeBytes: Long, now: Long)

    @Query("DELETE FROM downloads WHERE episodeId = :episodeId")
    suspend fun delete(episodeId: String)
}
