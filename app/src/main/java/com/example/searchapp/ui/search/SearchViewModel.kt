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

    val searchResults: StateFlow<List<AppEntity>> = combine(
        repository.getAllApps(),
        repository.getLastOpenedApps(),
        _searchQuery
    ) { allApps, lastOpened, query ->
        if (query.isBlank()) {
            lastOpened.filter { it.lastOpenedTime > 0 }.take(8) // Show up to 8 last opened apps
        } else {
            fuzzyMatch(allApps, query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private fun fuzzyMatch(apps: List<AppEntity>, query: String): List<AppEntity> {
        val lowercaseQuery = query.lowercase().trim()
        if (lowercaseQuery.isEmpty()) return apps.take(5)

        return apps.mapNotNull { app ->
            val label = app.label.lowercase()
            val score = calculateFuzzyScore(label, lowercaseQuery)
            if (score > 0) app to score else null
        }.sortedByDescending { it.second + it.first.usageCount }
            .map { it.first }
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
