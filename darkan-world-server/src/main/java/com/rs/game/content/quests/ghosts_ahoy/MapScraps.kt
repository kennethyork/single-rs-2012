package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.quest.Quest
import com.rs.game.World.spawnNPC
import com.rs.game.World.spawnObjectTemporary
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.*
import kotlin.math.abs

private const val GIANT_LOBSTER = 1693
private const val CHEST_KEY = 4273
@ServerStartupEvent
fun mapGhostAhoyMapParts() {
    //Chests
    onItemOnObject(arrayOf(5270, 5271, 5272, 5273), arrayOf(CHEST_KEY)) { (player, obj) ->
        if (!player.isQuestStarted(Quest.GHOSTS_AHOY) || player.isQuestComplete(Quest.GHOSTS_AHOY)) {
            openFakeChest(player, obj)
            return@onItemOnObject
        }
        if (obj.tile == Tile.of(3619, 3545, 1)) {
            player.itemDialogue(CHEST_KEY, "You use the captain's key to open the chest.")
            player.inventory.deleteItem(CHEST_KEY, 1)
            openRewardChest(player, obj, 4274)
            return@onItemOnObject
        }
        else
            openFakeChest(player, obj)
    }
    onObjectClick(5270, 5271, 5272, 5273) { (player, obj, event)->
        var mapFragment = 0
        when (obj.tile) {
            Tile.of(3619, 3545, 1) -> mapFragment = 4274
            Tile.of(3615, 3544, 0) -> mapFragment = 4275
            Tile.of(3606, 3564, 0) -> mapFragment = 4276
        }
        if (event.equals("Open")) {
            if (!player.isQuestStarted(Quest.GHOSTS_AHOY) || player.isQuestComplete(Quest.GHOSTS_AHOY)) {
                openFakeChest(player, obj)
                return@onObjectClick
            }
            if (obj.tile == Tile.of(3619, 3545, 1) && player.inventory.containsItem(CHEST_KEY)) {
                player.itemDialogue(CHEST_KEY, "You use the captain's key to open the chest.")
                player.inventory.deleteItem(CHEST_KEY, 1)
                openRewardChest(player, obj, mapFragment)
                return@onObjectClick
            }
            if (obj.tile == Tile.of(3619, 3545, 1) && !player.inventory.containsItem(CHEST_KEY)) {
                player.sendMessage("The chest is securely locked.")
                return@onObjectClick
            }

            if (obj.tile == Tile.of(3615, 3544, 0)) {
                if (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("LobsterKilled"))
                    openRewardChest(player, obj, mapFragment)
                else
                    openSpawnerChest(player, obj)
                return@onObjectClick
            }
            if (obj.tile == Tile.of(3606, 3564, 0)) {
                openRewardChest(player, obj, mapFragment)
                return@onObjectClick
            } else
                openFakeChest(player, obj)
        }
    }
    //Key on Captain's Chest
    onItemOnObject(arrayOf(5270), arrayOf(4273)) { (player, obj) ->
        val mapFragment = 4274
        openRewardChest(player, obj, mapFragment)
    }
    //Combine fragments into map
    onItemOnItem(intArrayOf(4274, 4275, 4276), intArrayOf(4274, 4275, 4276)) { e ->
        if (e.player.inventory.containsItems(intArrayOf(4274, 4275, 4276), intArrayOf(1, 1, 1))) {
            e.player.inventory.removeItems(intArrayOf(4274, 4275, 4276), intArrayOf(1, 1, 1))
            e.player.inventory.addItem(4277)
            e.player.itemDialogue(4277, "You combine the map scraps into a complete treasure map.")
        } else
            e.player.itemDialogue(e.item1.id, "You attempt to combine the scraps but there is clearly a piece missing.")
    }
    //Using the treasure map
    onItemClick(4277, options = arrayOf("Read", "Follow")) { (player, item, event) ->
        if (event.equals("Read"))
            player.interfaceManager.sendInterface(8)
        if (event.equals("Follow")) {
            if (player.tile == Tile.of(3791, 3556, 0)) {
                player.lock()
                val steps = listOf(
                    Pair(0, -6), // South 6
                    Pair(8, 0),  // East 8
                    Pair(0, 2),  // North 2
                    Pair(4, 0),  // East 4
                    Pair(0, -22) // South 22
                )
                player.schedule {
                    var wasRunning = false
                    if (player.run) {
                        wasRunning = true
                        player.toggleRun(false)
                    }
                    for ((dx, dy) in steps) {
                        val targetX = player.x + dx
                        val targetY = player.y + dy
                        player.addWalkSteps(targetX, targetY, abs(dx).coerceAtLeast(abs(dy)), true)
                        while (player.walkSteps.isNotEmpty()) {
                            wait(1)
                        }
                    }
                    if (wasRunning) {
                        player.toggleRun(true)
                    }
                    player.unlock()
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapLobsterDeath() {
    onNpcDeath(GIANT_LOBSTER) { e ->
        val killer = e.killer
        if (killer is Player) {
            killer.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("LobsterKilled", true)
        }
    }
}

private fun openRewardChest(player: Player, obj: GameObject, mapFragment: Int) {
    if(player.inventory.containsItem(mapFragment)){
        openFakeChest(player, obj)
        return
    }
    if(!player.inventory.hasFreeSlots()) {
        player.sendMessage("You don't have the space in your inventory.")
        return
    }
    player.lock()
    player.anim(536)
    player.tasks.schedule(1) {
        val openedChest = GameObject(
            obj.id + 1,
            obj.type,
            obj.rotation,
            obj.x,
            obj.y,
            obj.plane
        )
        spawnObjectTemporary(openedChest, 100)
        player.inventory.addItem(mapFragment)
        player.itemDialogue(mapFragment,"You find a map scrap in the chest.")
        player.unlock()
    }
}

private fun openFakeChest(player: Player, obj: GameObject) {
    player.lock()
    player.anim(536)
    player.tasks.schedule(1) {
        val openedChest = GameObject(
            obj.id + 1,
            obj.type,
            obj.rotation,
            obj.x,
            obj.y,
            obj.plane
        )
        spawnObjectTemporary(openedChest, 60)
        player.sendMessage("You search the chest and find nothing.")
        player.unlock()
    }
}

private fun openSpawnerChest(player: Player, obj: GameObject) {
    player.lock()
    player.anim(536)
    player.tasks.schedule(1) {
        val openedChest = GameObject(
            obj.id + 1,
            obj.type,
            obj.rotation,
            obj.x,
            obj.y,
            obj.plane
        )
        spawnObjectTemporary(openedChest, 30)
        player.unlock()
        val lobster = spawnNPC(GIANT_LOBSTER, Tile.of(3611, 3543, 0), permaDeath = true, withFunction = true)
        lobster.combatTarget = player
    }
}

