/*
 * Mucify (2026)
 * Offline Lyrics Sync & Download Manager
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

package moe.rukamori.archivetune.lyrics

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.db.MusicDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class OfflineLyricsInfo(
    val songId: String,
    val hasLyrics: Boolean,
    val isSynced: Boolean,
    val lyricsText: String?
)

data class LyricsSyncState(
    val isSyncing: Boolean = false,
    val progress: Float = 0f,
    val syncedCount: Int = 0,
    val totalCount: Int = 0,
    val message: String = ""
)

@Singleton
class OfflineLyricsSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    private val lyricsHelper: LyricsHelper
) {
    private val lyricsDir: File by lazy {
        File(context.filesDir, "offline_lyrics").apply { mkdirs() }
    }
    
    private val _syncState = MutableStateFlow(LyricsSyncState())
    val syncState: StateFlow<LyricsSyncState> = _syncState.asStateFlow()
    
    fun getOfflineLyrics(songId: String): OfflineLyricsInfo? {
        val lyricsFile = File(lyricsDir, "$songId.lrc")
        return if (lyricsFile.exists()) {
            val content = lyricsFile.readText()
            OfflineLyricsInfo(
                songId = songId,
                hasLyrics = true,
                isSynced = content.contains("["), // Simple check for timestamp format
                lyricsText = content
            )
        } else {
            null
        }
    }
    
    fun saveLyricsOffline(songId: String, lyrics: String, isSynced: Boolean) {
        val lyricsFile = File(lyricsDir, "$songId.lrc")
        lyricsFile.writeText(lyrics)
        
        // Also save metadata
        val metaFile = File(lyricsDir, "$songId.meta")
        metaFile.writeText(if (isSynced) "synced" else "plain")
    }
    
    fun deleteOfflineLyrics(songId: String) {
        File(lyricsDir, "$songId.lrc").delete()
        File(lyricsDir, "$songId.meta").delete()
    }
    
    fun hasOfflineLyrics(songId: String): Boolean {
        return File(lyricsDir, "$songId.lrc").exists()
    }
    
    fun syncAllDownloadedSongs(): Flow<LyricsSyncState> = flow {
        emit(LyricsSyncState(isSyncing = true, message = "Starting lyrics sync..."))
        
        val downloadedSongs = withContext(Dispatchers.IO) {
            database.downloadedSongsList()
        }
        
        val totalCount = downloadedSongs.size
        var syncedCount = 0
        
        downloadedSongs.forEachIndexed { index, song ->
            val progress = (index + 1).toFloat() / totalCount
            
            if (!hasOfflineLyrics(song.id)) {
                try {
                    // Attempt to fetch lyrics and save offline
                    val lyrics = withContext(Dispatchers.IO) {
                        lyricsHelper.getLyrics(song)
                    }
                    
                    if (lyrics != null) {
                        saveLyricsOffline(song.id, lyrics, true)
                        syncedCount++
                    }
                } catch (e: Exception) {
                    // Continue with next song
                }
            } else {
                syncedCount++
            }
            
            emit(
                LyricsSyncState(
                    isSyncing = true,
                    progress = progress,
                    syncedCount = syncedCount,
                    totalCount = totalCount,
                    message = "Syncing: ${song.title}"
                )
            )
        }
        
        emit(
            LyricsSyncState(
                isSyncing = false,
                progress = 1f,
                syncedCount = syncedCount,
                totalCount = totalCount,
                message = "Sync complete: $syncedCount/$totalCount songs"
            )
        )
    }.flowOn(Dispatchers.IO)
    
    fun getOfflineLyricsCount(): Int {
        return lyricsDir.listFiles()?.count { it.extension == "lrc" } ?: 0
    }
    
    fun clearAllOfflineLyrics() {
        lyricsDir.listFiles()?.forEach { it.delete() }
    }
}
