package com.rs.game.content.world.npcs

import com.rs.lib.file.JsonFileManager
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.plugin.annotations.ServerStartupEvent
import java.io.File
import java.util.Locale

private const val CONFIG_PATH = "./data/npcs/simulated-players.json"

enum class SimulatedPlayerMode {
    SOCIAL,
    PK
}

data class SimulatedPlayerDefinition(
    val name: String = "",
    val x: Int = 3222,
    val y: Int = 3218,
    val plane: Int = 0,
    val style: Attack = Attack.MELEE,
    val mode: SimulatedPlayerMode = SimulatedPlayerMode.SOCIAL,
    val wander: Boolean = true,
    val dropsEquipment: Boolean = false,
    val combatLevel: Int = 126,
    val location: String = "",
    val clan: String = ""
)

data class SimulatedPlayerRegion(
    val name: String = "region",
    val x: Int = 3222,
    val y: Int = 3218,
    val plane: Int = 0,
    val radius: Int = 8,
    val count: Int = 0,
    val mode: SimulatedPlayerMode = SimulatedPlayerMode.SOCIAL,
    val minCombatLevel: Int = 20,
    val maxCombatLevel: Int = 138
)

data class SimulatedPlayerPopulation(
    val enabled: Boolean = false,
    val bots: List<SimulatedPlayerDefinition> = emptyList(),
    val regions: List<SimulatedPlayerRegion> = emptyList(),
    val companions: SimulatedPlayerCompanionSettings = SimulatedPlayerCompanionSettings(),
    val economy: SimulatedPlayerEconomySettings = SimulatedPlayerEconomySettings(),
    val activities: SimulatedPlayerActivitySettings = SimulatedPlayerActivitySettings(),
    val ollama: SimulatedPlayerOllamaSettings = SimulatedPlayerOllamaSettings()
)

data class SimulatedPlayerOllamaSettings(
    val enabled: Boolean = false,
    val baseUrl: String = "http://127.0.0.1:11434",
    val model: String = "qwen3.5:4b",
    val timeoutSeconds: Int = 30,
    val historyMessages: Int = 8,
    val publicCooldownSeconds: Int = 4,
    val clanCooldownSeconds: Int = 5,
    val minimumResponseDelayTicks: Int = 2,
    val maximumResponseDelayTicks: Int = 4,
    val persistHistory: Boolean = true
)

object SimulatedPlayerPopulationManager {
	private val spawnedBots = mutableListOf<SimulatedPlayerBot>()
    var economySettings = SimulatedPlayerEconomySettings()
        private set
    var companionSettings = SimulatedPlayerCompanionSettings()
        private set
    var activitySettings = SimulatedPlayerActivitySettings()
        private set
    var ollamaSettings = SimulatedPlayerOllamaSettings()
        private set

    fun load() {
        val configFile = File(CONFIG_PATH)
        if (!configFile.exists()) {
            Logger.info(javaClass, "load", "No simulated-player config found at $CONFIG_PATH")
            return
        }

        val population = try {
            JsonFileManager.loadJsonFile(configFile, SimulatedPlayerPopulation::class.java) as? SimulatedPlayerPopulation
        } catch (error: Throwable) {
            Logger.handle(javaClass, "load", "Unable to load $CONFIG_PATH", error)
            return
        }

        if (population == null || !population.enabled) {
            Logger.info(javaClass, "load", "Simulated-player population is disabled")
            return
        }

        economySettings = population.economy
        companionSettings = population.companions
        activitySettings = population.activities
        ollamaSettings = population.ollama
        SimulatedPlayerOllama.configure()

        val usedNames = HashSet<String>()
        val definitions = population.bots + population.regions.flatMap(::generateRegion)
        definitions.forEach { definition ->
            val normalizedName = definition.name.trim().lowercase()
            if (normalizedName.isEmpty()) {
                Logger.info(javaClass, "load", "Skipped a simulated player with a blank name")
                return@forEach
            }
            if (!usedNames.add(normalizedName)) {
                Logger.info(javaClass, "load", "Skipped duplicate simulated-player name: ${definition.name}")
                return@forEach
            }
            if (definition.plane !in 0..3) {
                Logger.info(javaClass, "load", "Skipped ${definition.name}: plane must be between 0 and 3")
                return@forEach
            }

			spawnedBots += SimulatedPlayerBot(definition)
        }

        SimulatedPlayerActivityManager.initialize(spawnedBots)
        SimulatedPlayerSocial.initializeClans()

        Logger.info(javaClass, "load", "Spawned ${spawnedBots.size} simulated players")
    }

    private fun generateRegion(region: SimulatedPlayerRegion): List<SimulatedPlayerDefinition> {
        if (region.count <= 0 || region.plane !in 0..3) return emptyList()
        val count = region.count.coerceAtMost(100)
        val radius = region.radius.coerceIn(1, 64)
        val minLevel = region.minCombatLevel.coerceIn(3, 138)
        val maxLevel = region.maxCombatLevel.coerceIn(minLevel, 138)
        val seed = region.name.hashCode().toUInt().toLong()

        return List(count) { index ->
            val angle = Math.toRadians(((index * 137.5) + seed % 360).toDouble())
            val distance = 1 + ((index * 7 + seed.toInt().ushr(4)) % radius)
            val level = minLevel + ((index * 17 + seed.toInt().ushr(8)) and Int.MAX_VALUE) % (maxLevel - minLevel + 1)
            val generatedX = region.x + (Math.cos(angle) * distance).toInt()
            val generatedY = region.y + (Math.sin(angle) * distance).toInt()
            SimulatedPlayerDefinition(
                name = generatedName(region.name, index),
                x = if (region.mode == SimulatedPlayerMode.PK) generatedX.coerceIn(2940, 3395) else generatedX,
                y = if (region.mode == SimulatedPlayerMode.PK) generatedY.coerceIn(3525, 4000) else generatedY,
                plane = region.plane,
                style = Attack.entries[index % Attack.entries.size],
                mode = region.mode,
                combatLevel = level,
                location = region.name,
                clan = if (index % 3 == 0) "" else generatedClan(region.name, region.mode)
            )
        }
    }

    private fun generatedClan(regionName: String, mode: SimulatedPlayerMode): String {
        if (mode == SimulatedPlayerMode.PK) return "Wilderness Guard"
        return when (regionName.lowercase(Locale.ROOT)) {
            "grand exchange", "varrock" -> "Varrock Exchange"
            "lumbridge" -> "Lumbridge Legends"
            "catherby", "seers village", "karamja" -> "Skillers Union"
            "daemonheim" -> "Daemonheim Delvers"
            "edgeville" -> "Edgeville Crew"
            else -> "Gielinor Adventurers"
        }
    }

    private fun generatedName(regionName: String, index: Int): String {
        val first = listOf("Ash", "Blue", "Cinder", "Dawn", "Elm", "Frost", "Gold", "Hazel", "Iron", "Jade", "Kestrel", "Lunar")
        val second = listOf("Arrow", "Bear", "Crow", "Drake", "Ember", "Fox", "Gale", "Hawk", "Ibis", "Jay", "Knight", "Leaf")
        val seed = regionName.lowercase(Locale.ROOT).hashCode() and Int.MAX_VALUE
        return "${first[(seed + index) % first.size]} ${second[(seed / first.size + index * 5) % second.size]} ${index + 1}"
    }

    fun removeAll() {
		SimulatedPlayerActivityManager.flush()
		spawnedBots.forEach { bot -> if (!bot.hasFinished()) bot.finish() }
        spawnedBots.clear()
    }

    fun activeCount(): Int = spawnedBots.count { !it.hasFinished() }

    fun activeBots(): List<SimulatedPlayerBot> = spawnedBots.filterNot { it.hasFinished() }

    fun findByDisplayName(name: String): SimulatedPlayerBot? = activeBots().firstOrNull {
        it.displayName.equals(name.trim(), ignoreCase = true) || it.username.equals(name.trim(), ignoreCase = true)
    }
}

@ServerStartupEvent
fun loadSimulatedPlayerPopulation() {
    SimulatedPlayerPopulationManager.load()
}
