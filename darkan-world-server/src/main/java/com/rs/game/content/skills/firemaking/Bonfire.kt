package com.rs.game.content.skills.firemaking

import com.rs.cache.loaders.ItemDefinitions
import com.rs.game.content.Effect
import com.rs.game.content.skills.firemaking.Bonfire.Companion.addLog
import com.rs.game.content.skills.firemaking.Bonfire.Companion.addLogs
import com.rs.game.content.skills.firemaking.Bonfire.Log
import com.rs.game.map.ChunkManager
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.game.model.gameobject.GameObject
import com.rs.game.tasks.Task
import com.rs.game.tasks.WorldTasks
import com.rs.lib.Constants
import com.rs.lib.game.Item
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick

class Bonfire(private val log: Log, private val obj: GameObject) : PlayerAction() {

    private var count = 0

    enum class Log(val logId: Int, val gfxId: Int, val level: Int, val xp: ClosedRange<Double>, val boostTimeMins: Int) {
        LOG(1511, 3098, 1, 50.0..50.0, 6),
        ACHEY(2862, 3098, 1, 50.0..50.0,  6),
        OAK(1521, 3099, 15, 85.0..85.0,  12),
        WILLOW(1519, 3101, 30, 105.0..105.0,  18),
        TEAK(6333, 3098, 35, 120.0..120.0, 18),
        ARCTIC_PINE(10810, 3098, 42, 130.0..130.0, 18),
        MAPLE(1517, 3100, 45, 135.0..175.0, 36),
        MAHOGANY(6332, 3098, 50, 180.0..180.0, 36),
        EUCALYPTUS(12581, 3112, 58, 195.0..195.0, 48),
        YEWS(1515, 3111, 60, 260.0..260.0, 54),
        MAGIC(1513, 3135, 75, 309.5..309.5, 60),
        BLISTERWOOD(21600, 3113, 76, 309.5..309.5, 60),
        CURSED_MAGIC(13567, 3116, 82, 309.5..309.5, 60)
    }

    private fun checkAll(player: Player): Boolean {
        if (!ChunkManager.getChunk(obj.tile.chunkId).objectExists(obj) || !player.inventory.containsItem(log.logId, 1)) {
            return false
        }
        if (player.skills.getLevel(Constants.FIREMAKING) < log.level) {
            player.simpleDialogue("You need level ${log.level} Firemaking to add these logs to a bonfire.")
            return false
        }
        return true
    }

    override fun start(player: Player): Boolean {
        return if (checkAll(player)) {
            val participants: MutableSet<String> = synchronized(obj) {
                obj.attribs.getO("bonfire_participants_set") as? MutableSet<String>
                    ?: mutableSetOf<String>().also { obj.attribs.setO("bonfire_participants_set", it) }
            }
            synchronized(participants) {
                if (participants.add(player.username)) {
                    obj.attribs.incI("bonfire_participants")
                }
            }
            player.appearance.setBAS(2498)
            true
        } else {
            false
        }
    }

    override fun process(player: Player): Boolean {
        if (checkAll(player)) {
            if (Utils.random(500) == 0) {
                val tile = player.getNearestTeleTile(1)
                if (tile != null) {
                    FireSpirit(tile, player)
                    player.sendMessage("<col=ff0000>A fire spirit emerges from the bonfire.")
                } else {
                    player.sendMessage("<col=ff0000>A fire spirit struggles to escape the bonfire. Try moving elsewhere.")
                }
            }
            return true
        }
        return false
    }

    override fun processWithDelay(player: Player): Int {
        player.faceObject(obj)
        player.incrementCount("${ItemDefinitions.getDefs(log.logId).name} burned in bonfire")
        player.inventory.deleteItem(log.logId, 1)

        val baseXp = if (log == Log.MAPLE) calculateXpBasedOnLevel(player, log, log.xp.start, log.xp.endInclusive) else log.xp.start
        val finalXp = Firemaking.increasedExperience(player, baseXp, true)
        val boostedXp = synchronized(obj) {
            val bonfireParticipants = obj.attribs.getI("bonfire_participants")
            finalXp * (1 + when (bonfireParticipants) {
                1 -> 0.00
                2 -> 0.01
                3 -> 0.02
                4 -> 0.03
                else -> 0.04
            })
        }

        player.skills.addXp(Constants.FIREMAKING, boostedXp)
        player.sync(16703, log.gfxId)
        player.sendMessage("You add a log to the fire.", true)
        if (count++ == 4 && !player.hasEffect(Effect.BONFIRE)) {
            player.addEffect(Effect.BONFIRE, log.boostTimeMins * 100L)
            val percentage = (getBonfireBoostMultiplier(player) * 100).toInt()
            player.sendMessage("<col=00ff00>The bonfire's warmth increases your maximum health by $percentage%. This will last ${log.boostTimeMins} minutes.")
        }
        return 5
    }

    override fun stop(player: Player) {
        player.emotesManager.setNextEmoteEnd(4)
        WorldTasks.schedule(object : Task() {
            override fun run() {
                player.anim(16702)
                player.appearance.setBAS(-1)
            }
        }, 3)
    }

    companion object {
        fun addLog(player: Player, obj: GameObject, item: Item): Boolean {
            for (log in Log.entries) {
                if (log.logId == item.id) {
                    player.actionManager.setAction(Bonfire(log, obj))
                    return true
                }
            }
            return false
        }

        fun addLogs(player: Player, obj: GameObject) {
            val possibilities = Log.entries.filter { player.inventory.containsItem(it.logId, 1) }
            when {
                possibilities.isEmpty() -> player.sendMessage("You do not have any logs to add to this fire.")
                possibilities.size == 1 -> player.actionManager.setAction(Bonfire(possibilities.first(), obj))
                else -> player.startConversation(BonfireD(player, obj, possibilities.toTypedArray()))
            }
        }

        fun calculateXpBasedOnLevel(player: Player, log: Log, minXp: Double, maxXp: Double): Double {
            val fmLvl = player.skills.getLevel(Constants.FIREMAKING)
            val maxLvl = 99
            val minLvl = log.level
            return when {
                fmLvl < minLvl -> minXp
                fmLvl >= maxLvl -> maxXp
                else -> minXp + (fmLvl - minLvl) * (maxXp - minXp) / (maxLvl - minLvl)
            }
        }
    }
}

fun getBonfireBoostMultiplier(player: Player): Double {
    val fmLvl = player.skills.getLevel(Constants.FIREMAKING)
    return when {
        fmLvl >= 90 -> 0.1
        fmLvl >= 80 -> 0.09
        fmLvl >= 70 -> 0.08
        fmLvl >= 60 -> 0.07
        fmLvl >= 50 -> 0.06
        fmLvl >= 40 -> 0.05
        fmLvl >= 30 -> 0.04
        fmLvl >= 20 -> 0.03
        fmLvl >= 10 -> 0.02
        else -> 0.01
    }
}

@ServerStartupEvent
fun mapBonfireInteractions() {
    onObjectClick("Fire") { (player, obj, option) -> if (option == "Add-logs") addLogs(player, obj) }

    onItemOnObject(arrayOf("Fire"), Log.entries.map { it.logId }.toTypedArray()) { e ->
        if (e.`object`.getDefinitions(e.player).containsOption(4, "Add-logs")) {
            addLog(e.player, e.`object`, e.item)
        }
    }
}
