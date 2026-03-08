package com.example.searchapp.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.searchapp.data.local.AppDao
import com.example.searchapp.data.local.AppEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao
) {
    fun getAllApps(): Flow<List<AppEntity>> = appDao.getAllApps()

    fun getLastOpenedApps(): Flow<List<AppEntity>> = appDao.getLastOpenedApps()

    suspend fun refreshAppIndex() {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        
        val apps = resolveInfos.map { resolveInfo ->
            AppEntity(
                packageName = resolveInfo.activityInfo.packageName,
                label = resolveInfo.loadLabel(packageManager).toString(),
                isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }
        
        appDao.insertApps(apps)
    }

    suspend fun incrementUsage(packageName: String) {
        appDao.incrementUsage(packageName, System.currentTimeMillis())
    }

    fun launchApp(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
