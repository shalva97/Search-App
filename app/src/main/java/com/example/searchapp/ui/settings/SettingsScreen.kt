package com.example.searchapp.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.searchapp.data.local.AppEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onRecentlyInstalledClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val allApps by viewModel.allApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = "Application Management",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Recently Installed Apps") },
                    supportingContent = { Text("Show apps installed in the last hour") },
                    modifier = Modifier.clickable(onClick = onRecentlyInstalledClick)
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                Text(
                    text = "Hide apps from search results",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(
                items = allApps,
                key = { it.packageName }
            ) { app ->
                AppHideItem(
                    app = app,
                    onToggleHidden = { viewModel.toggleAppHidden(app) }
                )
            }
        }
    }
}

@Composable
fun AppHideItem(
    app: AppEntity,
    onToggleHidden: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember(app.packageName, app.label) {
        try {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleHidden)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.firstOrNull()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Checkbox(
            checked = app.isHidden,
            onCheckedChange = { onToggleHidden() }
        )
    }
}
