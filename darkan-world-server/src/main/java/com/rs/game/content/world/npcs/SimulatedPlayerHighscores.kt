package com.rs.game.content.world.npcs

import com.google.gson.GsonBuilder
import com.rs.db.local.LocalFileStore
import com.rs.game.World
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import java.io.File
import java.time.Instant

private data class WebsiteHighscoreEntry(
    val displayName: String,
    val username: String,
    val bot: Boolean,
    val combatLevel: Int,
    val totalLevel: Int,
    val totalXp: Long,
    val levels: IntArray,
    val xp: LongArray
)

private data class WebsiteHighscoreExport(
    val formatVersion: Int = 1,
    val generatedAt: String,
    val skills: List<String>,
    val entries: List<WebsiteHighscoreEntry>
)

object SimulatedPlayerHighscores {
    private const val EXPORT_FILE = "highscores-export.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    @Synchronized
    fun export(): File {
        val livePlayers = World.players
            .filter { !it.hasFinished() && it.hasStarted() }
            .plus(SimulatedPlayerPopulationManager.activeBots())
            .distinctBy { it.username.lowercase() }

        val entries = livePlayers.map { player ->
            val levels = IntArray(Constants.SKILL_NAME.size) { player.skills.getLevelForXp(it) }
            val xp = LongArray(Constants.SKILL_NAME.size) { player.skills.getXp(it).toLong() }
            WebsiteHighscoreEntry(
                displayName = player.displayName,
                username = player.username,
                bot = player.isHeadless,
                combatLevel = player.skills.combatLevelWithSummoning,
                totalLevel = levels.sum(),
                totalXp = xp.sum(),
                levels = levels,
                xp = xp
            )
        }.sortedWith(compareByDescending<WebsiteHighscoreEntry> { it.totalLevel }.thenByDescending { it.totalXp })

        val export = WebsiteHighscoreExport(
            generatedAt = Instant.now().toString(),
            skills = Constants.SKILL_NAME.toList(),
            entries = entries
        )
        LocalFileStore.writeAtomic(EXPORT_FILE, gson.toJson(export))
        return LocalFileStore.file(EXPORT_FILE)
    }

    fun export(requestingPlayer: Player): File {
        // Keep the command API explicit while the normal 30-second save now
        // refreshes this same file automatically.
        check(requestingPlayer.hasStarted()) { "Only a started player can request a highscore export" }
        return export()
    }
}
