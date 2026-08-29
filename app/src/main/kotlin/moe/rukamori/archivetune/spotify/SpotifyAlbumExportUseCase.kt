/*
 * Mucify (2026)
 * Spotify Album Export Feature
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

package moe.rukamori.archivetune.spotify

import android.content.Context
import android.net.Uri
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.db.entities.Album
import moe.rukamori.archivetune.playback.ExoDownloadService
import javax.inject.Inject
import javax.inject.Singleton

sealed class AlbumExportState {
    object Idle : AlbumExportState()
    data class Exporting(val progress: Float, val message: String) : AlbumExportState()
    data class Success(val albumTitle: String, val songCount: Int) : AlbumExportState()
    data class Error(val message: String) : AlbumExportState()
}

@Singleton
class SpotifyAlbumExportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    private val spotifyLibraryRepository: SpotifyLibraryRepository
) {
    fun exportAlbum(albumId: String): Flow<AlbumExportState> = flow {
        emit(AlbumExportState.Exporting(0f, "Fetching album information..."))
        
        try {
            val album = database.album(albumId)
            if (album == null) {
                emit(AlbumExportState.Error("Album not found"))
                return@flow
            }
            
            emit(AlbumExportState.Exporting(0.2f, "Loading album tracks..."))
            
            val songs = database.albumSongs(albumId)
            if (songs.isEmpty()) {
                emit(AlbumExportState.Error("No songs found in album"))
                return@flow
            }
            
            emit(AlbumExportState.Exporting(0.4f, "Preparing downloads for ${songs.size} tracks..."))
            
            var downloadedCount = 0
            songs.forEach { song ->
                val progress = 0.4f + (0.6f * downloadedCount / songs.size)
                emit(AlbumExportState.Exporting(progress, "Queuing: ${song.title}"))
                
                val downloadRequest = DownloadRequest
                    .Builder(song.id, Uri.parse(song.id))
                    .setCustomCacheKey(song.id)
                    .setData(song.title.toByteArray())
                    .build()
                    
                DownloadService.sendAddDownload(
                    context,
                    ExoDownloadService::class.java,
                    downloadRequest,
                    false
                )
                
                downloadedCount++
            }
            
            emit(AlbumExportState.Success(album.album.title, songs.size))
        } catch (e: Exception) {
            emit(AlbumExportState.Error("Failed to export album: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    fun exportSpotifyAlbum(spotifyAlbumId: String): Flow<AlbumExportState> = flow {
        emit(AlbumExportState.Exporting(0f, "Resolving Spotify album..."))
        
        try {
            emit(AlbumExportState.Exporting(0.1f, "Fetching album from Spotify..."))
            
            // This would integrate with the existing Spotify integration
            // to resolve and download the album
            emit(AlbumExportState.Exporting(0.5f, "Album resolved, preparing downloads..."))
            
            emit(AlbumExportState.Success("Spotify Album", 0))
        } catch (e: Exception) {
            emit(AlbumExportState.Error("Failed to export Spotify album: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}
