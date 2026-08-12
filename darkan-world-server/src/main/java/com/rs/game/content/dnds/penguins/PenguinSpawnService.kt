package com.rs.game.content.dnds.penguins

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rs.engine.pathfinder.Direction
import com.rs.game.World
import com.rs.game.content.dnds.penguins.PenguinServices.penguinSpawnService
import com.rs.game.map.ChunkManager
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.utils.spawns.NPCSpawn
import java.time.Month
import com.rs.engine.variables.peng_spy_week_peng

const val PUMPKIN_ID = 14415
const val SNOWMAN_ID = 14766

class PenguinSpawnService () {
    val spawnedNPCs: MutableMap<String, NPC> = mutableMapOf()
    var regionIds: HashSet<Int> = hashSetOf()
    private val allSpawns = mutableMapOf<NPCSpawn, Tile>()

    fun loadSpawns() {
        allSpawns.clear()
        val penguins: List<Penguin> = getPenguins()
        if (penguins.isNotEmpty()) {
            penguins.forEach { penguin ->
                val spawn = NPCSpawn(penguin.npcId, penguin.location, "")
                allSpawns[spawn] = penguin.location
                regionIds.add(penguin.location.regionId)
            }
            Logger.debug(PenguinSpawnService::class.java, "loadSpawns", "Loaded ${allSpawns.size} spawns from penguin data.")
        } else {
            Logger.warn(PenguinSpawnService::class.java, "loadSpawns", "No penguins found.")
        }
    }

    fun addSpawn(id: Int, tile: Tile, comment: String): NPCSpawn {
        val spawn = NPCSpawn(id, tile, comment)
        allSpawns[spawn] = tile
        regionIds.add(tile.regionId)
        return spawn
    }

    fun getAllSpawns(): MutableMap<NPCSpawn, Tile> = allSpawns

    fun prepareNew() {
        val previouslySpawnedPenguins = getAllSpawns()
            .map { (spawn, _) -> spawn }

        removeAllSpawns()

        for (player in World.players) { // Reset Quickchat varbit to 0 for all logged in players
            player.vars.saveVarBit(peng_spy_week_peng, 0)
        }

        val currentMonth = PenguinServices.penguinHideAndSeekManager.getCurrentMonth()
        val usedLocationHints = mutableSetOf<String>()

        val onePointPenguins = Penguins.getPenguinsByPoints(1)
            .filterNot { it.locationHint in usedLocationHints || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
            .take(5)
        onePointPenguins.forEach { usedLocationHints.add(it.locationHint) }
        val twoPointPenguins = Penguins.getPenguinsByPoints(2)
            .filterNot { it.locationHint in usedLocationHints || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
            .take(5)
        twoPointPenguins.forEach { usedLocationHints.add(it.locationHint) }

        val selectedPenguins = (onePointPenguins + twoPointPenguins).toMutableList()
        val uniquePenguins = selectedPenguins.distinctBy { it.locationHint }.toMutableList()

        if (uniquePenguins.size < 10) {
            fun addReplacementPenguins(points: Int, targetList: MutableList<Penguins>, requiredCount: Int) {
                val needed = requiredCount - targetList.count { it.points == points }
                if (needed > 0) {
                    Penguins.getPenguinsByPoints(points).filterNot { it.locationHint in usedLocationHints || it in targetList || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
                        .take(needed)
                        .forEach { penguin ->
                            if (penguin.locationHint !in usedLocationHints) {
                                targetList += penguin
                                usedLocationHints.add(penguin.locationHint)
                            }
                        }
                }
            }

            addReplacementPenguins(1, uniquePenguins, 5)
            addReplacementPenguins(2, uniquePenguins, 5)
        }

        uniquePenguins.forEach { penguin ->
            usedLocationHints.add(penguin.locationHint)
            val idToUse = when (currentMonth) {
                Month.OCTOBER -> PUMPKIN_ID
                Month.DECEMBER -> SNOWMAN_ID
                else -> penguin.npcId
            }
            val newPenguin = createPenguin(idToUse, penguin.name, penguin.tile, penguin.points)
            val spawn = addSpawn(idToUse, newPenguin.location, penguin.wikiLocation)
            spawnPenguins(spawn)
        }
        Logger.debug(PenguinSpawnService::class.java, "prepareNew", "New penguins spawned. Current tracked size: ${spawnedNPCs.size}")
    }

    fun prepareExisting() {
        if (spawnedNPCs.isNotEmpty()) {
            Logger.debug(PenguinSpawnService::class.java, "prepareExisting", "Spawns already exist. No action taken.")
            return
        }

        val existingSpawns = getAllSpawns()
        val alreadySpawnedTiles = spawnedNPCs.values.map { it.respawnTile }.toSet()

        existingSpawns.forEach { spawn ->
            if (spawn.key.tile in alreadySpawnedTiles) {
                return@forEach
            }
            spawnPenguins(spawn.key)
        }
    }

    fun spawnPenguins(spawn: NPCSpawn) {
        if (!regionIds.contains(spawn.tile.regionId))
            regionIds.add(spawn.tile.regionId)
        ChunkManager.permanentlyPreloadRegions(penguinSpawnService.regionIds)
        val npc = spawn.spawnAtCoords(spawn.tile, Direction.random())
        npc.setLoadsUpdateZones()
        trackSpawnedNPC(npc.respawnTile, npc)
    }

    fun trackSpawnedNPC(tile: Tile, npc: NPC) {
        val key = "${tile.x}:${tile.y}:${tile.plane}"
        spawnedNPCs[key] = npc
        playSoundAndAnim(86, npc)
    }

    fun removeAllSpawns(): Boolean {
        val allSpawns = getAllSpawns()

        val removed = allSpawns.isNotEmpty() == true
        allSpawns.clear()
        World.data.attribs.setO<String>(PENGUINS_WORLD_ATTR, "")

        if (removed) {
            spawnedNPCs.values.forEach { npc ->
                playSoundAndAnim(1605, npc)
                npc.finish()
            }
            spawnedNPCs.clear()
            ChunkManager.removePermanentlyPreloadedRegions(regionIds)
            regionIds.clear()
            Logger.debug(PenguinSpawnService::class.java, "removeAllSpawns", "All old penguin spawns removed.")
        }

        return removed
    }

    private fun playSoundAndAnim(spotAnimId: Int, npc: NPC) {
        val soundId = 1930
        val actualSpotAnimId = spotAnimId
        npc.soundEffect(npc, soundId, true)
        World.sendSpotAnim(npc.tile, actualSpotAnimId)
    }

    fun createPenguin(
        npcId: Int,
        name: String,
        location: Tile?,
        points: Int
    ): Penguin {
        val newPenguin = Penguin(
            npcId = npcId,
            name = name,
            location = location ?: Tile(0, 0, 0),
            points = points
        )

        val penguinsJson: String? = World.data.attribs.getO(PENGUINS_WORLD_ATTR)
        val penguins: MutableList<Penguin> = if (penguinsJson.isNullOrEmpty()) {
            mutableListOf()
        } else {
            val type = object : TypeToken<List<Penguin>>() {}.type
            Gson().fromJson(penguinsJson, type) ?: mutableListOf()
        }
        penguins.add(newPenguin)
        World.data.attribs.setO<String>(PENGUINS_WORLD_ATTR, Gson().toJson(penguins))
        return newPenguin
    }

    fun getPenguins(): MutableList<Penguin> {
        val penguinsJson: String? = World.data.attribs.getO(PENGUINS_WORLD_ATTR)
        return if (penguinsJson != null) {
            val type = object : TypeToken<List<Penguin>>() {}.type
            Gson().fromJson(penguinsJson, type)
        } else {
            mutableListOf()
        }
    }

}

data class Penguin(
    val npcId: Int,
    val name: String,
    val location: Tile,
    val points: Int,
    val spotters: MutableList<String> = mutableListOf()
) {
    fun addSpotter(username: String) {
        if (!spotters.contains(username)) {
            spotters.add(username)
        }
    }
}

