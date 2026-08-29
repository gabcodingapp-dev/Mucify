/*
 * Mucify (2026)
 * DI Module for All Mucify Features
 * © Gab — github.com/gabcodingapp-dev
 */

package moe.rukamori.archivetune.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.features.sleeptimer.SleepTimerManager
import moe.rukamori.archivetune.features.streaks.ListeningStreakManager
import moe.rukamori.archivetune.features.taste.MusicTasteDna
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
        database: MusicDatabase
    ): OfflineLyricsSyncManager {
        return OfflineLyricsSyncManager(context, database)
    }
    
    @Provides
    @Singleton
    fun provideSleepTimerManager(
        @ApplicationContext context: Context
    ): SleepTimerManager {
        return SleepTimerManager(context)
    }
    
    @Provides
    @Singleton
    fun provideListeningStreakManager(
        @ApplicationContext context: Context
    ): ListeningStreakManager {
        return ListeningStreakManager(context)
    }
    
    @Provides
    @Singleton
    fun provideMusicTasteDna(
        database: MusicDatabase
    ): MusicTasteDna {
        return MusicTasteDna(database)
    }
}
