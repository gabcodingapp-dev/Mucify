/*
 * Mucify (2026)
 * Dependency Injection Module for Offline Features
 * © Gabriel — github.com/gabcodingapp-dev
 * GPL-3.0 License
 */

package moe.rukamori.archivetune.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.lyrics.LyricsHelper
import moe.rukamori.archivetune.lyrics.OfflineLyricsSyncManager
import moe.rukamori.archivetune.offline.OfflineDownloadManager
import moe.rukamori.archivetune.playback.DownloadUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineModule {
    
    @Provides
    @Singleton
    fun provideOfflineDownloadManager(
        @ApplicationContext context: Context,
        downloadUtil: DownloadUtil
    ): OfflineDownloadManager {
        return OfflineDownloadManager(context, downloadUtil)
    }
    
    @Provides
    @Singleton
    fun provideOfflineLyricsSyncManager(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        lyricsHelper: LyricsHelper
    ): OfflineLyricsSyncManager {
        return OfflineLyricsSyncManager(context, database, lyricsHelper)
    }
}
