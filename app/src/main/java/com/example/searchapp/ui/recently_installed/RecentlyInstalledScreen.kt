package com.example.searchapp.ui.recently_installed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.searchapp.ui.search.AppResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyInstalledScreen(
    onBackClick: () -> Unit,
    viewModel: RecentlyInstalledViewModel = hiltViewModel()
) {
    val apps by viewModel.recentlyInstalledApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Installed (1h)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No apps installed in the last hour.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(
                    items = apps,
                    key = { it.packageName }
                ) { app ->
                    AppResultItem(
                        app = app,
                        onClick = { viewModel.onAppClicked(app) },
                        onHideClick = { viewModel.onHideApp(app) },
                        onUninstallClick = { viewModel.onUninstallApp(app) },
                        onPlayStoreClick = { viewModel.onOpenInPlayStore(app) }
                    )
                }
            }
        }
    }
}
