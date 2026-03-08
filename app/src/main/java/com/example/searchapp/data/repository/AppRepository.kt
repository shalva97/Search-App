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
    fun getAllVisibleApps(): Flow<List<AppEntity>> = appDao.getAllVisibleApps()

    fun getAllApps(): Flow<List<AppEntity>> = appDao.getAllApps()

    fun getLastOpenedVisibleApps(): Flow<List<AppEntity>> = appDao.getLastOpenedVisibleApps()
    
    fun getRecentlyInstalledApps(sinceTime: Long): Flow<List<AppEntity>> = 
        appDao.getRecentlyInstalledApps(sinceTime)

    suspend fun setAppHidden(packageName: String, isHidden: Boolean) {
        appDao.setHidden(packageName, isHidden)
    }

    suspend fun refreshAppIndex(force: Boolean = false) {
        val currentAppCount = appDao.getAppCount()
        if (currentAppCount == 0 || force) {
            val apps = getInstalledApps()
            appDao.clearAll()
            appDao.insertApps(apps)
        } else {
            // Incremental update to be faster
            val installedApps = getInstalledApps()
            val installedPackageNames = installedApps.map { it.packageName }.toSet()
            val dbPackageNames = appDao.getAllPackageNames().toSet()

            // Remove uninstalled apps
            val removedPackages = dbPackageNames - installedPackageNames
            if (removedPackages.isNotEmpty()) {
                appDao.deleteApps(removedPackages.toList())
            }

            // Add new apps
            val newApps = installedApps.filter { it.packageName !in dbPackageNames }
            if (newApps.isNotEmpty()) {
                appDao.insertNewApps(newApps)
            }
        }
    }

    private fun getInstalledApps(): List<AppEntity> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        return resolveInfos.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val installedAt = try {
                packageManager.getPackageInfo(packageName, 0).firstInstallTime
            } catch (e: Exception) {
                0L
            }
            AppEntity(
                packageName = packageName,
                label = resolveInfo.loadLabel(packageManager).toString(),
                isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                installedAt = installedAt
            )
        }
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

    fun openInPlayStore(packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("market://details?id=$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if Play Store app is not available
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
