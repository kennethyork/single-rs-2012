package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.model.entity.player.Player

class HousePlayerManager(private val house: House) {

    var locked = false
    private val players: MutableList<Player?> = ArrayList()

    val isOwnerInside: Boolean
        get() = players.contains(house.getPlayer())

    fun isOwner(player: Player?): Boolean {
        return house.getPlayer()?.username.equals(player?.username, ignoreCase = true)
    }

    fun expelGuests() {
        if (!isOwnerInside) {
            house.getPlayer()?.sendMessage("You can only expel guests when you are in your own house.")
            return
        }
        kickGuests()
    }

    fun kickGuests() {
        if (players.isEmpty()) return
        for (p in ArrayList(players)) {
            if (p == null) continue
            if (isOwner(p)) continue
            leaveHouse(p, House.KICKED)
        }
    }

    fun joinHouse(player: Player?): Boolean {
        if (player == null) return false
        if (!isOwner(player)) {
            if (!isOwnerInside || !house.isLoaded) {
                player.sendMessage("That player is offline, or has privacy mode enabled.")
                return false
            }
            if (house.isBuildMode()) {
                player.sendMessage("The owner currently has build mode turned on.")
                return false
            }
        }
        player.lock()
        players.add(player)
        house.sendStartInterface(player)
        player.controllerManager.startController(HouseController(house))
        if (house.isLoaded) {
            house.teleportPlayer(player)
            player.tasks.schedule(4) {
                player.unlock()
                player.interfaceManager.setDefaultTopInterface()
                house.teleportServant()
            }
        } else {
            house.createHouse()
        }
        return true
    }

    fun leaveHouse(player: Player?, type: Int, controllerRemoved: Boolean = false) {
        if (player == null) return
        player.isCanPvp = false
        player.removeHouseOnlyItems()
        if (!controllerRemoved) {
            player.controllerManager.removeControllerWithoutCheck()
        }
        when (type) {
            House.LOGGED_OUT -> player.tile = house.location.tile
            House.KICKED     -> player.useStairs(-1, house.location.tile, 0, 1)
            // House.TELEPORTED or anything else: do nothing special
        }
        players.remove(player)
        if (players.isEmpty()) {
            house.destroyHouse()
        }
        if (player.appearance.renderEmote != -1) {
            player.appearance.setBAS(-1)
        }
        if (isOwner(player) && house.servantInstance != null) {
            house.servantInstance!!.isFollowing = false
        }

        player.tempAttribs?.setB("inBoxingArena", false)
        player.isCanPvp = false
        player.setForceMultiArea(false)
    }

    fun switchLock(player: Player) {
        if (!isOwner(player)) {
            player.sendMessage("You can only lock your own house.")
            return
        }
        locked = !locked
        if (locked) {
            player.simpleDialogue("Your house is now locked to visitors.")
        } else if (house.isBuildMode()) {
            player.simpleDialogue("Visitors will be able to enter your house once you leave building mode.")
        } else {
            player.simpleDialogue("You have unlocked your house.")
        }
    }

    fun getPlayers(): List<Player?> = players.toList()
}