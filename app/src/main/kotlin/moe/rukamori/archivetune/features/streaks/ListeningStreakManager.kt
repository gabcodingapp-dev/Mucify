/*
 * Mucify (2026)
 * Listening Streaks & Achievements
 * NEW FEATURE — gamified listening experience
 * © Gab — github.com/gabcodingapp-dev
 */

package moe.rukamori.archivetune.features.streaks

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalDaysListened: Int = 0,
    val totalMinutesListened: Long = 0L,
    val todayMinutes: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean = false,
    val unlockedDate: String? = null
)

@Singleton
class ListeningStreakManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mucify_streaks", Context.MODE_PRIVATE)
    
    private val _streakData = MutableStateFlow(loadStreakData())
    val streakData: StateFlow<StreakData> = _streakData.asStateFlow()
    
    fun recordListeningSession(minutes: Int) {
        val today = LocalDate.now().toString()
        val lastDate = prefs.getString("last_date", null)
        
        var currentStreak = prefs.getInt("current_streak", 0)
        var longestStreak = prefs.getInt("longest_streak", 0)
        var totalDays = prefs.getInt("total_days", 0)
        var totalMinutes = prefs.getLong("total_minutes", 0L)
        var todayMinutes = prefs.getInt("today_minutes", 0)
        
        // Update today's minutes
        if (lastDate == today) {
            todayMinutes += minutes
        } else {
            // New day
            if (lastDate != null) {
                val lastDay = LocalDate.parse(lastDate)
                val daysDiff = ChronoUnit.DAYS.between(lastDay, LocalDate.now())
                
                if (daysDiff == 1L) {
                    currentStreak++
                } else if (daysDiff > 1L) {
                    currentStreak = 1
                }
            } else {
                currentStreak = 1
            }
            
            totalDays++
            todayMinutes = minutes
        }
        
        totalMinutes += minutes
        longestStreak = maxOf(longestStreak, currentStreak)
        
        // Calculate XP and level
        val xpGained = minutes * 2
        var xp = prefs.getInt("xp", 0) + xpGained
        var level = prefs.getInt("level", 1)
        var xpNeeded = level * 100
        
        while (xp >= xpNeeded) {
            xp -= xpNeeded
            level++
            xpNeeded = level * 100
        }
        
        // Check achievements
        val achievements = checkAchievements(currentStreak, totalDays, totalMinutes, level)
        
        // Save
        prefs.edit {
            putString("last_date", today)
            putInt("current_streak", currentStreak)
            putInt("longest_streak", longestStreak)
            putInt("total_days", totalDays)
            putLong("total_minutes", totalMinutes)
            putInt("today_minutes", todayMinutes)
            putInt("xp", xp)
            putInt("level", level)
        }
        
        _streakData.value = StreakData(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalDaysListened = totalDays,
            totalMinutesListened = totalMinutes,
            todayMinutes = todayMinutes,
            achievements = achievements,
            level = level,
            xp = xp,
            xpToNextLevel = xpNeeded
        )
    }
    
    private fun checkAchievements(streak: Int, days: Int, minutes: Long, level: Int): List<Achievement> {
        val all = listOf(
            Achievement("first_day", "First Listen", "Start your music journey", "🎵", days >= 1),
            Achievement("week_streak", "Week Warrior", "7-day listening streak", "🔥", streak >= 7),
            Achievement("month_streak", "Monthly Master", "30-day listening streak", "⭐", streak >= 30),
            Achievement("100_hours", "Centurion", "Listen for 100 hours total", "💎", minutes >= 6000),
            Achievement("level_5", "Rising Star", "Reach level 5", "🌟", level >= 5),
            Achievement("level_10", "Music Addict", "Reach level 10", "🎶", level >= 10),
            Achievement("500_hours", "Audiophile", "Listen for 500 hours total", "🎧", minutes >= 30000),
            Achievement("year_streak", "Legendary", "365-day listening streak", "👑", streak >= 365),
        )
        return all
    }
    
    private fun loadStreakData(): StreakData {
        return StreakData(
            currentStreak = prefs.getInt("current_streak", 0),
            longestStreak = prefs.getInt("longest_streak", 0),
            totalDaysListened = prefs.getInt("total_days", 0),
            totalMinutesListened = prefs.getLong("total_minutes", 0L),
            todayMinutes = prefs.getInt("today_minutes", 0),
            achievements = checkAchievements(
                prefs.getInt("current_streak", 0),
                prefs.getInt("total_days", 0),
                prefs.getLong("total_minutes", 0L),
                prefs.getInt("level", 1)
            ),
            level = prefs.getInt("level", 1),
            xp = prefs.getInt("xp", 0),
            xpToNextLevel = prefs.getInt("level", 1) * 100
        )
    }
    
    fun resetToday() {
        prefs.edit {
            putInt("today_minutes", 0)
        }
    }
}
