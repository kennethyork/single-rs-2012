package com.rs.game.content.minigames.allfiredup

import com.rs.engine.quest.Quest
import com.rs.game.World.getServerTicks
import com.rs.game.content.Toolbelt
import com.rs.game.content.minigames.allfiredup.Beacons.getBeaconByTile
import com.rs.game.content.minigames.allfiredup.Beacons.lightBeacon
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick

fun Player.isBeaconEmpty(beacon: Beacon) = vars.getVarBit(beacon.varBit) == 0
fun Player.isBeaconFilled(beacon: Beacon) = vars.getVarBit(beacon.varBit) == 1
fun Player.isBeaconLit(beacon: Beacon) = vars.getVarBit(beacon.varBit) == 2
fun Player.isBeaconDyingOut(beacon: Beacon) = vars.getVarBit(beacon.varBit) == 3
fun Player.isBeaconEmergencyColour(beacon: Beacon) = vars.getVarBit(beacon.varBit) == 4 //Not used in minigame but changes flame to green.


object Beacons {
    private fun getBeaconXP(tile: Tile): Double {
        val beacon = getBeaconByTile(tile)
        if (beacon != null) {
            return beacon.xp
        }
        return 0.0
    }

    fun getBeaconByTile(tile: Tile): Beacon? {
        for (beacon in Beacon.entries) {
            if (beacon.tile == tile) {
                return beacon
            }
        }
        return null
    }

    private fun getLitBeacons(player: Player): List<Beacon> {
        val litBeacons = mutableListOf<Beacon>()
        for (beacon in Beacon.entries) {
            if (player.isBeaconLit(beacon)) {
                litBeacons.add(beacon)
            }
        }
        return litBeacons
    }

    private fun checkAchievements(player: Player) {
        if (!player.questManager.isComplete(Quest.ALL_FIRED_UP))
            return
        if (getLitBeacons(player).size == 6) {
            if (!player.getBool("AllFiredUpI.claimed")) {
                player.set("AllFiredUpI", true)
                player.sendMessage("You can now collect a Ring of Fire from King Roald")
                return
            }
        }
        if (getLitBeacons(player).size == 10) {
            if (!player.getBool("AllFiredUpII.claimed")) {
                player.set("AllFiredUpII", true)
                player.sendMessage("You can now collect a pair of Flame Gloves from King Roald")
                return
            }
        }
        if (getLitBeacons(player).size == 14) {
            if (!player.getBool("AllFiredUpIII.claimed")) {
            player.set("AllFiredUpIII", true)
            player.sendMessage("You can now collect the Inferno Adze from King Roald")
            }
        }
    }

    fun lightBeacon(player: Player, obj: GameObject) {
        val beacon = getBeaconByTile(obj.tile) ?: return
        if (player.skills.getLevel(Skills.FIREMAKING) < beacon.fireLevel) {
            player.sendMessage("You do not meet the required level of Firemaking (" + beacon.fireLevel + ") to use this beacon.")
            return
        }
        if(beacon == Beacon.GOD_WARS && !player.getBool("AllFiredUp.GodWars")) {
            player.sendMessage("The wind is too strong to light this beacon.")
            return
        }
        if (player.isBeaconFilled(beacon)) {
            player.anim(16703)
            val bonusXP = doubleArrayOf(
                608.4,
                1622.4,
                1987.4,
                2149.6,
                2149.6,
                2514.6,
                2555.2,
                2758.0,
                2839.1,
                3041.9,
                3123.0,
                3244.7,
                3366.4,
                4867.1
            )
            player.skills.addXp(Skills.FIREMAKING, getBeaconXP(obj.tile))
            val litCount = getLitBeacons(player).size
            if (litCount <= bonusXP.size && player.get("Beacon_$beacon") != null) {
                player.skills.addXp(Skills.FIREMAKING, bonusXP[litCount])
            }

            val burnExpiry: Long
            val dyingExpiry: Long
            val logType = player.getI("${beacon.name}LogType", -1)
            if (logType != -1) {
                val log = Log.getLogFromID(logType) ?: Log.LOG
                val now = getServerTicks()
                burnExpiry = now + log.burnTime
                dyingExpiry = now + log.dyingTime
            } else {
                burnExpiry = player.getL(beacon.name + "BurnTime")
                dyingExpiry = player.getL(beacon.name + "DyingTime")
            }
            player.set("${beacon.name}BurnTime", burnExpiry)
            player.set("${beacon.name}DyingTime", dyingExpiry)

            var initialized = false
            player.tasks.scheduleLooping("LIGHT_BEACON_${beacon.name}", 0, 0) {
                val now = getServerTicks()
                if (!initialized) {
                    player.set("Beacon_$beacon", true)
                    player.vars.saveVarBit(beacon.varBit, 2)
                    checkAchievements(player)
                    initialized = true
                }
                if (now >= dyingExpiry) {
                    player.vars.saveVarBit(beacon.varBit, 3)
                }
                if (now >= burnExpiry) {
                    player.vars.saveVarBit(beacon.varBit, 0)
                    player.set("Beacon_$beacon", null)
                    player.set("${beacon.name}LogType", null)
                    player.tasks.remove("LIGHT_BEACON_${beacon}")
                }
            }
        } else {
            player.sendMessage("The beacon is empty.")
        }
    }

}

@ServerStartupEvent
fun handleBeacons() {
    onLogin { (player) ->
        if (!player.isQuestStarted(Quest.ALL_FIRED_UP))
            return@onLogin
        val now = getServerTicks()
        for (beacon in Beacon.entries) {
            if (player.get("Beacon_${beacon.name}") == null) continue
            if (!player.isBeaconLit(beacon) && !player.isBeaconDyingOut(beacon)) {
                player.set("Beacon_${beacon.name}", null)
                continue
            }
            val burnExpiry  = player.getL("${beacon.name}BurnTime")
            val dyingExpiry = player.getL("${beacon.name}DyingTime")
            if (player.hasInvalidBeaconTimers(beacon, burnExpiry, now)) {
                player.clearInvalidBeaconState(beacon)
                continue
            }
            when {
                now >= burnExpiry -> {
                    player.vars.saveVarBit(beacon.varBit, 0)
                    player.set("Beacon_${beacon.name}", null)
                    player.set("${beacon.name}LogType", null)
                    continue
                }
                now < dyingExpiry -> {
                    if (!player.isBeaconLit(beacon)) {
                        player.vars.saveVarBit(beacon.varBit, 2)
                    }
                }
                else -> {
                    if (!player.isBeaconDyingOut(beacon)) {
                        player.vars.saveVarBit(beacon.varBit, 3)
                    }
                }
            }
            player.tasks.scheduleLooping("LIGHT_BEACON_${beacon.name}", 0, 0) {
                val ticks = getServerTicks()
                when {
                    ticks < dyingExpiry && !player.isBeaconLit(beacon) -> {
                        player.vars.saveVarBit(beacon.varBit, 2)
                    }
                    ticks >= dyingExpiry && !player.isBeaconDyingOut(beacon) -> {
                        player.vars.saveVarBit(beacon.varBit, 3)
                    }
                    ticks >= burnExpiry -> {
                        player.vars.saveVarBit(beacon.varBit, 0)
                        player.set("Beacon_${beacon.name}", null)
                        player.set("${beacon.name}LogType", null)
                        player.tasks.remove("LIGHT_BEACON_${beacon.name}")
                    }
                }
            }
        }
    }
    onObjectClick(*Beacon.entries.map { it.objID }.toTypedArray()) { e ->
        val beacon = getBeaconByTile(e.obj.tile) ?: return@onObjectClick
        val player = e.player

        if (!player.canInteractWithBeacon(beacon)) return@onObjectClick
        if (player.tryFillBeacon(beacon)) return@onObjectClick
        if (!player.hasTinderbox()) {
            player.sendMessage("You need a tinderbox to light this beacon.")
            return@onObjectClick
        }

        player.questGate(beacon)

        if (beacon == Beacon.GOD_WARS && !player.getBool("AllFiredUp.GodWars")) {
            player.sendMessage("The wind is too high, perhaps you should repair the windbreak.")
            return@onObjectClick
        }

        lightBeacon(player, e.obj)
    }
    onItemOnObject(Beacon.entries.map { it.objID }.toTypedArray(), arrayOf(590)) { e ->
        val beacon = getBeaconByTile(e.`object`.tile) ?: return@onItemOnObject
        val player = e.player
        if (!player.canInteractWithBeacon(beacon)) return@onItemOnObject
        when (player.vars.getVarBit(beacon.varBit)) {
            0 -> {
                player.sendMessage("The beacon is empty.")
                return@onItemOnObject
            }
            2, 3 -> {
                player.sendMessage("The beacon is already lit.")
                return@onItemOnObject
            }
        }
        if (e.item.id != 590) {
            player.sendMessage("You cannot use this item on a beacon.")
            return@onItemOnObject
        }
        player.questGate(beacon)

        lightBeacon(player, e.`object`)
    }
    onItemOnObject(objectNamesOrIds = Beacon.entries.map { it.objID }.toTypedArray(), itemNamesOrIds = Log.entries.map { it.logItemID }.toTypedArray()) { e ->
        val player = e.player
        val beacon = getBeaconByTile(e.`object`.tile) ?: return@onItemOnObject
        if (!player.canInteractWithBeacon(beacon)) return@onItemOnObject
        if(player.isBeaconEmpty(beacon))
            if (player.tryFillBeacon(beacon)) return@onItemOnObject
        if (player.isBeaconLit(beacon)) {
            player.sendMessage("The beacon is already lit.")
            return@onItemOnObject
        }
        if (player.isBeaconFilled(beacon)) {
            player.sendMessage("The beacon is already full of logs.")
            return@onItemOnObject
        }
        if (player.isBeaconDyingOut(beacon)) {
            val chosenLog = Log.entries.firstOrNull { player.inventory.getAmountOf(it.logItemID) >= 5 }
            if (chosenLog == null) {
                player.sendMessage("You need five logs to shore up this dying beacon.")
                return@onItemOnObject
            }
            player.inventory.deleteItem(chosenLog.logItemID, 5)
            Log.getLogFromID(e.item.id)?.let { player.set("${beacon.name}BurnTime", it.burnTime + getServerTicks()) }
            Log.getLogFromID(e.item.id)?.let { player.set("${beacon.name}DyingTime", it.dyingTime + getServerTicks()) }
            player.sendMessage("You add five logs to the dying beacon, restoring it to full strength.")
            player.tasks.remove("LIGHT_BEACON_${beacon.name}")
            player.vars.saveVarBit(beacon.varBit, 1)
            lightBeacon(player, e.`object`)
        }
    }
}

fun Player.questGate(beacon: Beacon) {
    val stage = questManager.getStage(Quest.ALL_FIRED_UP)
    if (stage == 2 && beacon == Beacon.RIVER_SALVE) {
        questManager.setStage(Quest.ALL_FIRED_UP, 3)
    } else if (stage == 4 && beacon == Beacon.RAG_AND_BONE_MAN) {
        questManager.setStage(Quest.ALL_FIRED_UP, 5)
    }
}

fun Player.canInteractWithBeacon(beacon: Beacon): Boolean {
    if (!isQuestStarted(Quest.ALL_FIRED_UP) || questManager.getStage(Quest.ALL_FIRED_UP) <= 0) {
        sendMessage("I don't think I should meddle with this right now.")
        return false
    }
    if (beacon == Beacon.RIVER_SALVE && questManager.getStage(Quest.ALL_FIRED_UP) == 1) {
        sendMessage("I should speak to Blaze Sharpeye before using this beacon.")
        return false
    }
    if (beacon == Beacon.RAG_AND_BONE_MAN && questManager.getStage(Quest.ALL_FIRED_UP) == 3) {
        sendMessage("I should speak to Squire Fyre before using this beacon.")
        return false
    }
    if (skills.getLevel(Skills.FIREMAKING) < beacon.fireLevel) {
        sendMessage("You do not meet the required level of Firemaking ${beacon.fireLevel} to use this beacon.")
        return false
    }
    return true
}

fun Player.tryFillBeacon(beacon: Beacon): Boolean {
    if (!isBeaconEmpty(beacon)) return false

    val selectedLog = Log.entries.firstOrNull { inventory.getAmountOf(it.logItemID) >= 20 }
    if (selectedLog == null) {
        sendMessage("You do not have enough logs.")
        return true
    }
    inventory.deleteItem(selectedLog.logItemID, 20)
    set("${beacon.name}LogType", selectedLog.logItemID)
    vars.saveVarBit(beacon.varBit, 1)
    sendMessage("You fill the beacon with logs.")
    return true
}

fun Player.hasTinderbox(): Boolean {
    if (inventory.containsItem(590)) return true
    val tb = toolbelt
    return tb != null && (tb.entries as? Set<*>)?.contains(Toolbelt.Tools.TINDERBOX) == true
}

private const val MAX_BEACON_BURN_TICKS = 1177 // Magic logs (1077) + generous buffer

fun Player.hasInvalidBeaconTimers(beacon: Beacon, burnExpiry: Long, now: Long): Boolean {
    if (burnExpiry <= 0) return true
    val remaining = burnExpiry - now
    return remaining > MAX_BEACON_BURN_TICKS
}

fun Player.clearInvalidBeaconState(beacon: Beacon) {
    vars.saveVarBit(beacon.varBit, 0)
    set("Beacon_${beacon.name}", null)
    set("${beacon.name}LogType", null)
    set("${beacon.name}BurnTime", null)
    set("${beacon.name}DyingTime", null)
    tasks.remove("LIGHT_BEACON_${beacon.name}")
    sendMessage("The ${beacon.name.lowercase().replace('_', ' ')} beacon had an invalid burn state and has been reset.")
}

enum class Beacon(
    val objID: Int,
    val varBit: Int,
    val tile: Tile,
    val fireLevel: Int,
    val xp: Double,
    val fireCompID: Int,
    val keeperID: Int?
) {
    RIVER_SALVE(38448, 5146, Tile.of(3396, 3464, 0), 43, 216.2, 3, 8065),
    RAG_AND_BONE_MAN(38449, 5147, Tile.of(3343, 3510, 0), 43, 235.8, 4, 8042),
    JOLLY_BOAR_INN(38450, 5148, Tile.of(3278, 3525, 0), 48, 193.8, 5, 8067),
    VARROCK_PALACE(38451, 5149, Tile.of(3236, 3527, 0), 53, 178.5, 6, 8068),
    GRAND_EXCHANGE(38452, 5150, Tile.of(3170, 3536, 0), 59, 194.3, 7, 8069),
    EDGEVILLE(38453, 5151, Tile.of(3087, 3516, 0), 62, 86.7, 8, 8070),
    MONASTERY(38454, 5152, Tile.of(3034, 3518, 0), 68, 224.4, 9, 8071),
    GOBLIN_VILLAGE(38455, 5153, Tile.of(2968, 3516, 0), 72, 194.8, 10, 8072),
    BURTHORPE(38456, 5154, Tile.of(2940, 3565, 0), 76, 195.3, 11, 8073),
    DEATH_PLATEAU(38457, 5155, Tile.of(2944, 3618, 0), 79, 249.9, 12, 8074),
    TROLLHEIM(38458, 5156, Tile.of(2940, 3683, 0), 83, 201.0, 13, 8075),
    GOD_WARS(38459, 5157, Tile.of(2937, 3773, 0), 83, 255.0, 14, 8076),
    WILDERNESS_TEMPLE(38460, 5158, Tile.of(2946, 3836, 0), 87, 198.9, 15, null),
    FROZEN_WASTE_PLATEAU(38461, 5159, Tile.of(2964, 3931, 0), 87, 147.9, 16, null);

    companion object {
        fun getBeaconForNPC(npcID: Int): Beacon? = entries.firstOrNull { it.keeperID == npcID }
    }
}

enum class Log(val logItemID: Int, val burnTime: Int, val dyingTime: Int) {
    LOG(1511, 762, 336),
    OAK_LOGS(1521, 825, 396),
    WILLOW_LOGS(1519, 846, 423),
    MAPLE_LOGS(1517, 930, 507),
    EUCALYPTUS_LOGS(12581, 957, 534),
    YEWS_LOGS(1515, 996, 570),
    MAGIC_LOGS(1513, 1077, 615);
    companion object {
        fun getLogFromID(logItemID: Int): Log? =
            entries.firstOrNull { it.logItemID == logItemID }
    }
}