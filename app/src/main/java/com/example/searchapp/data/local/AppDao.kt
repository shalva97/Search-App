package com.example.searchapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY usageCount DESC, label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY lastOpenedTime DESC, label ASC")
    fun getLastOpenedApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("UPDATE apps SET usageCount = usageCount + 1, lastOpenedTime = :currentTime WHERE packageName = :packageName")
    suspend fun incrementUsage(packageName: String, currentTime: Long)

    @Query("DELETE FROM apps")
    suspend fun clearAll()

    @Query("SELECT * FROM apps WHERE label LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    suspend fun searchApps(query: String): List<AppEntity>
}
