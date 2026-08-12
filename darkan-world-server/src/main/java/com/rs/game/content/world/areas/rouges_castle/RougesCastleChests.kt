package com.rs.game.content.world.areas.rouges_castle

import com.rs.game.World
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.Constants
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.Ticks

private const val CHEST_CLOSED_ID = 38834
private const val CHEST_OPEN_ID = 38835
private const val ROGUE_NPC_ID = 187
private const val LOCKPICK = 1523
private const val REQUIRED_THIEVING = 65
private const val PICKLOCK_XP = 300.0
private const val SEARCH_XP = 60.0
private const val PICKLOCK_ANIM = 832
private val ROGUES_CASTLE_CHESTS = arrayOf(
    Tile.of(3289, 3929, 1),
    Tile.of(3288, 3929, 1),
    Tile.of(3287, 3929, 1)
)
private val restockTickByChest = HashMap<Int, Long>()
private val lootedChests = HashSet<Int>()
private val chestOwnerByHash = HashMap<Int, String>()

@ServerStartupEvent
fun mapRoguesCastleChests() {
    onObjectClick(CHEST_CLOSED_ID, tiles = ROGUES_CASTLE_CHESTS) { (player, obj, option) ->
        if (isChestRestocking(obj)) {
            player.sendMessage("This chest has not yet been restocked.")
            return@onObjectClick
        }
        when (option) {
            "Open" -> player.sendMessage("The chest is securely locked.")
            "Pick-lock" -> pickLock(player, obj)
        }
    }

    onObjectClick(CHEST_OPEN_ID, tiles = ROGUES_CASTLE_CHESTS) { (player, obj, option) ->
        when (option) {
            "Search" -> searchChest(player, obj)
            "Close" -> closeChest(player, obj)
        }
    }
}

private fun pickLock(player: Player, obj: GameObject) {
    if (player.skills.getLevel(Constants.THIEVING) < REQUIRED_THIEVING) {
        player.sendMessage("You need a Thieving level of at least $REQUIRED_THIEVING to pick this lock.")
        return
    }
    if (!player.inventory.containsOneItem(LOCKPICK)) {
        player.sendMessage("You need a lockpick to be able to pick the lock.")
        return
    }
    player.lock(1)
    player.faceObject(obj)
    player.anim(PICKLOCK_ANIM)
    player.delayLock(1) {
        if (!rollPickLockSuccess(player)) {
            player.sendMessage("You fail to pick the lock.")
            return@delayLock
        }
        player.skills.addXp(Constants.THIEVING, PICKLOCK_XP)
        player.sendMessage("You pick the lock and open the chest.")
        obj.setIdTemporary(CHEST_OPEN_ID, Ticks.fromSeconds(20))
        aggroNearbyRogues(player)
        val hash = obj.tile.tileHash
        restockTickByChest[hash] = World.getServerTicks() + Ticks.fromSeconds(20)
        lootedChests.remove(hash)
        chestOwnerByHash[hash] = player.username
    }
}

private fun searchChest(player: Player, obj: GameObject) {
    val hash = obj.tile.tileHash
    val owner = chestOwnerByHash[hash]
    if (owner == null || !owner.equals(player.username, ignoreCase = true)) {
        player.sendMessage("You didn't unlock this chest.")
        return
    }
    if (lootedChests.contains(hash)) {
        player.sendMessage("You search the chest and find nothing.")
        return
    }
    player.skills.addXp(Constants.THIEVING, SEARCH_XP)
    giveLoot(player)
    lootedChests.add(hash)
}

private fun closeChest(player: Player, obj: GameObject) {
    val remaining = getRemainingRestockTicks(obj)
    obj.setIdTemporary(CHEST_CLOSED_ID, remaining)
    player.sendMessage("You close the chest.")
}

private fun giveLoot(player: Player) {
    when (Utils.random(99)) {
        in 0..69 -> {
            val amount = Utils.random(3, 60)
            player.inventory.addItemDrop(995, amount)
            player.sendMessage("You find some coins in the chest.")
        }
        in 70..94 -> {
            val amount = Utils.random(107, 243)
            player.inventory.addItemDrop(995, amount)
            player.sendMessage("You find some coins in the chest.")
        }
        else -> {
            val amount = Utils.random(2, 5)
            player.inventory.addItemDrop(565, amount)
            player.sendMessage("You find some blood runes in the chest.")
        }
    }
}

private fun aggroNearbyRogues(player: Player) {
    World.getNPCsInChunkRange(player.chunkId, 2)
        .filter { !it.isDead && !it.hasFinished() && it.id == ROGUE_NPC_ID }
        .forEach {
            it.forceTalk("Someone's stealing from us, get them!")
            it.walkToAndExecute(player.tile) { it.combatTarget = player }
        }
}

private fun rollPickLockSuccess(player: Player): Boolean {
    val level = player.skills.getLevel(Constants.THIEVING)
    return Utils.skillSuccess(level, player.auraManager.thievingMul, 8, 72)
}

private fun isChestRestocking(obj: GameObject): Boolean {
    val hash = obj.tile.tileHash
    val restockTick = restockTickByChest[hash] ?: return false
    val now = World.getServerTicks()
    if (now >= restockTick) {
        restockTickByChest.remove(hash)
        lootedChests.remove(hash)
        chestOwnerByHash.remove(hash)
        return false
    }
    return true
}

private fun getRemainingRestockTicks(obj: GameObject): Int {
    val hash = obj.tile.tileHash
    val restockTick = restockTickByChest[hash] ?: return Ticks.fromSeconds(20)
    return (restockTick - World.getServerTicks()).toInt().coerceAtLeast(1)
}
