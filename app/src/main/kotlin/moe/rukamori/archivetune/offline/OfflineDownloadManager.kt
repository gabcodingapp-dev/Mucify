/*
 * Mucify (2026)
 * Enhanced Offline Download Manager
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

package moe.rukamori.archivetune.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moe.rukamori.archivetune.playback.DownloadUtil
import javax.inject.Inject
import javax.inject.Singleton

data class OfflineDownloadStats(
    val totalSongs: Int = 0,
    val totalSizeBytes: Long = 0L,
    val downloadedSongs: Int = 0,
    val pendingDownloads: Int = 0,
    val isOfflineMode: Boolean = false
)

@Singleton
class OfflineDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadUtil: DownloadUtil
) {
    private val _offlineStats = MutableStateFlow(OfflineDownloadStats())
    val offlineStats: StateFlow<OfflineDownloadStats> = _offlineStats.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    fun isDeviceOffline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun enableOfflineMode() {
        _isOfflineMode.value = true
    }
    
    fun disableOfflineMode() {
        _isOfflineMode.value = false
    }
    
    fun getDownloadedSongCount(): Int {
        return downloadUtil.downloads.value.count { it.value.state == Download.STATE_COMPLETED }
    }
    
    fun getPendingDownloadCount(): Int {
        return downloadUtil.downloads.value.count { 
            it.value.state == Download.STATE_QUEUED || 
            it.value.state == Download.STATE_DOWNLOADING 
        }
    }
    
    fun pauseAllDownloads() {
        downloadUtil.downloads.value.forEach { (id, download) ->
            if (download.state == Download.STATE_DOWNLOADING) {
                downloadUtil.downloadManager.setStopReason(id, 1)
            }
        }
    }
    
    fun resumeAllDownloads() {
        downloadUtil.downloads.value.forEach { (id, download) ->
            if (download.state == Download.STATE_QUEUED || download.state == Download.STATE_STOPPED) {
                downloadUtil.downloadManager.setStopReason(id, 0)
            }
        }
    }
    
    fun removeAllDownloads() {
        downloadUtil.downloadManager.removeAllDownloads()
    }
    
    fun getDownloadsFlow(): Flow<Map<String, Download>> {
        return downloadUtil.downloads
    }
}
