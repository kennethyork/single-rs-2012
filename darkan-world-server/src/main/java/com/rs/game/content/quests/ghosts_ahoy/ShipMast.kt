package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.quest.Quest
import com.rs.game.content.skills.magic.TeleType
import com.rs.game.model.entity.Teleport
import com.rs.game.model.entity.player.Controller
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapPirateCaptainGA() {
    onObjectClick(5274) { (player) ->
        if(!player.isQuestStarted(Quest.GHOSTS_AHOY)||player.isQuestComplete(Quest.GHOSTS_AHOY))
            player.simpleDialogue("You can see a tattered flag blowing in the wind. The wind is blowing too hard to make out the details.")
    }
}

@ServerStartupEvent
fun mapPirateShipLaddersGA() {
    onObjectClick(5265) { (player, obj) ->
        if(obj.x == 3615)
            player.controllerManager.startController(ShipMastController())
        if (player.y < obj.y)
            player.useLadder(Tile.of(obj.x + 1, obj.y, obj.plane + 1))
        else
            player.useLadder(Tile.of(obj.x + 1, obj.y, obj.plane + 1))
    }
    onObjectClick(5266) { (player, obj) ->
        if (player.y < obj.y)
            player.useLadder(Tile.of(obj.x + 1, obj.y , obj.plane - 1))
        else
            player.useLadder(Tile.of(obj.x -1, obj.y, obj.plane - 1))
    }

}

class ShipMastController() : Controller() {
    private var highWindSpeed = false
    private var tickCounter = 0

    override fun start() {
        player.interfaceManager.sendOverlay(10,true)
    }

    override fun processObjectClick1(obj: GameObject): Boolean {
        if (obj.id == 5266) {
            exitShip()
            return false
        }
        if (obj.id == 5274) {
            if(player.hasRights(Rights.ADMIN)) {
                player.sendMessage("You can see a tattered flag blowing in the wind. The top half of the flag is coloured ${SailColorManager.getSailColor(player, "sailColour1")}.")
                player.sendMessage("You can see a tattered flag blowing in the wind. The skull emblem is coloured ${SailColorManager.getSailColor(player, "sailColour2")}.")
                player.sendMessage("You can see a tattered flag blowing in the wind. The bottom half of the flag is coloured ${SailColorManager.getSailColor(player, "sailColour3")}.")
            }
            if(highWindSpeed){
                player.simpleDialogue("You can see a tattered flag blowing in the wind. The wind is blowing too hard to make out the details.")
            }
            else {
                when (Utils.random(3)) {
                    0 -> player.simpleDialogue("You can see a tattered flag blowing in the wind. The top half of the flag is coloured ${SailColorManager.getSailColor(player, "sailColour1")}.")
                    1 -> player.simpleDialogue("You can see a tattered flag blowing in the wind. The skull emblem is coloured ${SailColorManager.getSailColor(player, "sailColour2")}.")
                    2 -> player.simpleDialogue("You can see a tattered flag blowing in the wind. The bottom half of the flag is coloured ${SailColorManager.getSailColor(player, "sailColour3")}.")
                }
            }
            return false
        }
        return true
    }

    override fun process() {
        if (!player.isQuestStarted(Quest.GHOSTS_AHOY) || player.isQuestComplete(Quest.GHOSTS_AHOY)) {
            highWindSpeed = true
            return
        } else {
            tickCounter++
            if (tickCounter % 3 == 0) {
                highWindSpeed = Utils.random(0, 100) >= 50
            }
            player.packets.setIFText(10, 2, if (highWindSpeed) "high" else "low")
        }
    }

    override fun onTeleported(type: TeleType) {
        exitShip()
    }

    override fun processTeleport(tele: Teleport): Boolean {
        exitShip()
        return true
    }

    private fun exitShip() {
        player.interfaceManager.removeOverlay(true)
        removeController()
        player.useLadder(Tile.of(3614, 3541,1))
    }

    override fun logout(): Boolean {
        exitShip()
        return false
    }

    override fun forceClose() {
        exitShip()
    }
}