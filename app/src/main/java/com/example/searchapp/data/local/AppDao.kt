package com.example.searchapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY usageCount DESC, label ASC")
    fun getAllVisibleApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY lastOpenedTime DESC, label ASC")
    fun getLastOpenedVisibleApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 AND installedAt > :sinceTime ORDER BY installedAt DESC")
    fun getRecentlyInstalledApps(sinceTime: Long): Flow<List<AppEntity>>

    @Query("UPDATE apps SET isHidden = :isHidden WHERE packageName = :packageName")
    suspend fun setHidden(packageName: String, isHidden: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("UPDATE apps SET usageCount = usageCount + 1, lastOpenedTime = :currentTime WHERE packageName = :packageName")
    suspend fun incrementUsage(packageName: String, currentTime: Long)

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getAppCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewApps(apps: List<AppEntity>)

    @Query("SELECT packageName FROM apps")
    suspend fun getAllPackageNames(): List<String>

    @Query("DELETE FROM apps WHERE packageName IN (:packageNames)")
    suspend fun deleteApps(packageNames: List<String>)

    @Query("DELETE FROM apps")
    suspend fun clearAll()

    @Query("SELECT * FROM apps WHERE label LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    suspend fun searchApps(query: String): List<AppEntity>
}
