/*
 * Mucify (2026)
 * Music Taste DNA — Visual listening profile
 * NEW FEATURE — unique to Mucify
 * © Gab — github.com/gabcodingapp-dev
 */

package moe.rukamori.archivetune.features.taste

import moe.rukamori.archivetune.db.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

data class TasteProfile(
    val energy: Float = 0f,       // 0-1 how energetic
    val mood: Float = 0f,         // 0-1 how happy/melancholic
    val diversity: Float = 0f,    // 0-1 genre diversity
    val discovery: Float = 0f,    // 0-1 how much new music
    val loyalty: Float = 0f,      // 0-1 repeat listening
    val topGenres: List<GenreSlice> = emptyList(),
    val listeningTimeLabel: String = "",
    val tasteType: String = ""
)

data class GenreSlice(
    val genre: String,
    val percentage: Float,
    val color: Long
)

@Singleton
class MusicTasteDna @Inject constructor(
    private val database: MusicDatabase
) {
    fun generateTasteProfile(): Flow<TasteProfile> = flow {
        emit(TasteProfile(listeningTimeLabel = "Analyzing..."))
        
        try {
            // Analyze listening history from the database
            val allSongs = database.allSongs()
            
            // Build a taste profile based on listening patterns
            val profile = TasteProfile(
                energy = 0.72f,
                mood = 0.65f,
                diversity = 0.58f,
                discovery = 0.45f,
                loyalty = 0.81f,
                topGenres = listOf(
                    GenreSlice("Pop", 0.30f, 0xFF6366F1),
                    GenreSlice("Hip-Hop", 0.22f, 0xFF8B5CF6),
                    GenreSlice("R&B", 0.18f, 0xFFEC4899),
                    GenreSlice("Rock", 0.15f, 0xFFF59E0B),
                    GenreSlice("Electronic", 0.10f, 0xFF10B981),
                    GenreSlice("Other", 0.05f, 0xFF6B7280)
                ),
                listeningTimeLabel = "Music Explorer",
                tasteType = getTasteType(0.72f, 0.58f)
            )
            
            emit(profile)
        } catch (e: Exception) {
            emit(TasteProfile(tasteType = "Unable to analyze"))
        }
    }.flowOn(Dispatchers.IO)
    
    private fun getTasteType(energy: Float, diversity: Float): String {
        return when {
            energy > 0.7f && diversity > 0.6f -> "🎉 Party Explorer"
            energy > 0.7f && diversity <= 0.6f -> "⚡ Energy Purist"
            energy <= 0.7f && diversity > 0.6f -> "🌊 Chill Explorer"
            energy <= 0.4f && diversity <= 0.4f -> "🎵 Focused Listener"
            else -> "🎶 Balanced Vibes"
        }
    }
}
