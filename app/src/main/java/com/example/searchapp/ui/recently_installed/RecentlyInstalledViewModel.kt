package com.example.searchapp.ui.recently_installed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchapp.data.local.AppEntity
import com.example.searchapp.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyInstalledViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Apps installed in the last 1 hour
    private val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
    
    val recentlyInstalledApps: StateFlow<List<AppEntity>> = repository.getRecentlyInstalledApps(oneHourAgo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAppClicked(app: AppEntity) {
        viewModelScope.launch {
            repository.incrementUsage(app.packageName)
            repository.launchApp(app.packageName)
        }
    }

    fun onHideApp(app: AppEntity) {
        viewModelScope.launch {
            repository.setAppHidden(app.packageName, true)
        }
    }

    fun onUninstallApp(app: AppEntity) {
        repository.uninstallApp(app.packageName)
    }

    fun onOpenInPlayStore(app: AppEntity) {
        repository.openInPlayStore(app.packageName)
    }
}
