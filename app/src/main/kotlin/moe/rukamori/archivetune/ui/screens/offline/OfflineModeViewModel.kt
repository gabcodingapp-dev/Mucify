/*
 * Mucify (2026)
 * Offline Mode ViewModel
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

package moe.rukamori.archivetune.ui.screens.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.lyrics.LyricsSyncState
import moe.rukamori.archivetune.lyrics.OfflineLyricsSyncManager
import moe.rukamori.archivetune.offline.OfflineDownloadManager
import moe.rukamori.archivetune.offline.OfflineDownloadStats
import javax.inject.Inject

@HiltViewModel
class OfflineModeViewModel @Inject constructor(
    private val offlineDownloadManager: OfflineDownloadManager,
    private val offlineLyricsSyncManager: OfflineLyricsSyncManager
) : ViewModel() {
    
    val offlineStats: StateFlow<OfflineDownloadStats> = offlineDownloadManager.offlineStats
    
    val isOfflineMode: StateFlow<Boolean> = offlineDownloadManager.isOfflineMode
    
    val lyricsSyncState: StateFlow<LyricsSyncState> = offlineLyricsSyncManager.syncState
    
    fun toggleOfflineMode() {
        if (isOfflineMode.value) {
            offlineDownloadManager.disableOfflineMode()
        } else {
            offlineDownloadManager.enableOfflineMode()
        }
    }
    
    fun syncAllLyrics() {
        viewModelScope.launch {
            offlineLyricsSyncManager.syncAllDownloadedSongs().collect()
        }
    }
    
    fun pauseAllDownloads() {
        offlineDownloadManager.pauseAllDownloads()
    }
    
    fun resumeAllDownloads() {
        offlineDownloadManager.resumeAllDownloads()
    }
    
    fun clearCache() {
        offlineDownloadManager.removeAllDownloads()
        offlineLyricsSyncManager.clearAllOfflineLyrics()
    }
    
    fun exportAlbum() {
        // This would be connected to a dialog to select album
        // For now, it's a placeholder
    }
}
