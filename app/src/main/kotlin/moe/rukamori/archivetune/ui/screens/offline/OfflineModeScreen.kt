/*
 * Mucify (2026)
 * Offline Mode Screen with Download, Lyrics Sync & Spotify Export
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.archivetune.ui.screens.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OfflineModeScreen(
    navController: NavController,
    viewModel: OfflineModeViewModel = hiltViewModel()
) {
    val offlineStats by viewModel.offlineStats.collectAsStateWithLifecycle()
    val lyricsSyncState by viewModel.lyricsSyncState.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = "Offline Mode",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                moe.rukamori.archivetune.R.drawable.arrow_back
                            ),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline Mode Toggle Card
            item {
                OfflineModeCard(
                    isEnabled = isOfflineMode,
                    onToggle = { viewModel.toggleOfflineMode() }
                )
            }
            
            // Download Stats Card
            item {
                DownloadStatsCard(stats = offlineStats)
            }
            
            // Lyrics Sync Card
            item {
                LyricsSyncCard(
                    syncState = lyricsSyncState,
                    onSyncAll = { viewModel.syncAllLyrics() }
                )
            }
            
            // Spotify Export Card
            item {
                SpotifyExportCard(
                    onExportAlbum = { viewModel.exportAlbum() }
                )
            }
            
            // Quick Actions
            item {
                QuickActionsCard(
                    onPauseAll = { viewModel.pauseAllDownloads() },
                    onResumeAll = { viewModel.resumeAllDownloads() },
                    onClearCache = { viewModel.clearCache() }
                )
            }
        }
    }
}

@Composable
private fun OfflineModeCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnabled) "Playing downloaded music only" else "Online mode active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun DownloadStatsCard(stats: moe.rukamori.archivetune.offline.OfflineDownloadStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Download Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.downloadedSongs.toString(),
                    label = "Downloaded",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = stats.pendingDownloads.toString(),
                    label = "Pending",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    value = "${stats.totalSizeBytes / (1024 * 1024)} MB",
                    label = "Storage",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LyricsSyncCard(
    syncState: moe.rukamori.archivetune.lyrics.LyricsSyncState,
    onSyncAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Offline Lyrics Sync",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Download synced lyrics for offline playback. Lyrics will be available even without internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (syncState.isSyncing) {
                LinearProgressIndicator(
                    progress = { syncState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = syncState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Button(
                onClick = onSyncAll,
                modifier = Modifier.fillMaxWidth(),
                enabled = !syncState.isSyncing
            ) {
                Text(text = if (syncState.isSyncing) "Syncing..." else "Sync All Lyrics Offline")
            }
        }
    }
}

@Composable
private fun SpotifyExportCard(
    onExportAlbum: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Spotify Album Export",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text("NEW")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Export entire albums from your Spotify library for offline listening. All tracks will be downloaded and cached locally.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onExportAlbum,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Export Spotify Album")
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onClearCache: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPauseAll,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pause All")
                }
                OutlinedButton(
                    onClick = onResumeAll,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resume All")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onClearCache,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear All Downloads")
            }
        }
    }
}
