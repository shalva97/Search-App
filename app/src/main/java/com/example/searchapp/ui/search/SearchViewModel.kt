package com.example.searchapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchapp.data.local.AppEntity
import com.example.searchapp.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allApps = repository.getAllVisibleApps()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val searchResults: StateFlow<List<AppEntity>> = combine(
        allApps,
        repository.getLastOpenedVisibleApps(),
        _searchQuery
    ) { apps, lastOpened, query ->
        if (query.isBlank()) {
            lastOpened.filter { it.lastOpenedTime > 0 }.take(8)
        } else {
            fuzzyMatch(apps, query)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.refreshAppIndex()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAppClicked(app: AppEntity) {
        viewModelScope.launch {
            repository.incrementUsage(app.packageName)
            repository.launchApp(app.packageName)
            _searchQuery.value = "" // Reset state after opening an app
        }
    }

    fun onDonePressed() {
        val firstApp = searchResults.value.firstOrNull()
        if (firstApp != null) {
            onAppClicked(firstApp)
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

    private fun fuzzyMatch(apps: List<AppEntity>, query: String): List<AppEntity> {
        val lowercaseQuery = query.lowercase().trim()
        if (lowercaseQuery.isEmpty()) return emptyList()

        return apps.mapNotNull { app ->
            val label = app.label.lowercase()
            val score = calculateFuzzyScore(label, lowercaseQuery)
            if (score > 0) {
                // Boost score for exact prefix and usage
                val finalScore = when {
                    label.startsWith(lowercaseQuery) -> score * 2 + 100
                    label.contains(lowercaseQuery) -> score + 50
                    else -> score
                }
                app to (finalScore + app.usageCount)
            } else null
        }.sortedByDescending { it.second }
            .map { it.first }
            .take(20) // Limit results for performance
    }

    private fun calculateFuzzyScore(text: String, query: String): Int {
        if (text.startsWith(query)) return 100
        if (text.contains(query)) return 50
        
        // Simple character-by-character matching for typos/fuzzy
        var score = 0
        var textIdx = 0
        var queryIdx = 0
        
        while (textIdx < text.length && queryIdx < query.length) {
            if (text[textIdx] == query[queryIdx]) {
                score += 1
                queryIdx++
            }
            textIdx++
        }
        
        return if (queryIdx == query.length) score else 0
    }
}
