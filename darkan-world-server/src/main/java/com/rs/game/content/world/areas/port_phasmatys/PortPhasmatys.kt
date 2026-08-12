// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.world.areas.port_phasmatys

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.handleInsideTownMovement
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.handleOutsideTownMovement
import com.rs.game.content.world.areas.port_phasmatys.npcs.GhostGuard
import com.rs.game.content.world.doors.Doors
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onNpcClick
import com.rs.plugin.kts.onObjectClick
import kotlin.math.pow
import kotlin.math.sqrt

val westBarrier: Tile = Tile.of(3652, 3485, 0)
val northBarrier: Tile = Tile.of(3659, 3508, 0)

@ServerStartupEvent
fun mapPortPhasmatys() {
    val DISCIPLE = 1686
    val BONEKEY = 4272
    //Handle Bill Teach
    onNpcClick(3157) { e ->
        if (!e.player.isQuestComplete(Quest.CABIN_FEVER, "to travel to Mos' Le Harmless.")) return@onNpcClick
        e.player.sendOptionDialogue { ops ->
            if (e.player.regionId == 14638) {
                ops.add("Travel to Port Phasmatys.") {
                    e.player.tele(Tile.of(3713, 3497, 1))
                }
            } else {
                ops.add("Travel to Mos' Le Harmless.") {
                    e.player.tele(Tile.of(3682, 2949, 1))
                }
            }
            ops.add("Nevermind.")
        }
    }
    //handle Inn Trap Door
    onObjectClick(7433, 7434) { e ->
        when (e.objectId) {
            7433 -> e.player.useStairs(828, Tile.of(3681, 3497, 0), 1, 2)
            7434 -> e.player.useStairs(828, Tile.of(3682, 9961, 0), 1, 2)
        }
    }
    //handle Town Doors
    onObjectClick(5244) { e ->
        if (e.obj.tile != Tile.of(3656, 3514, 1)) {
            Doors.handleDoor(e.player, e.obj)
            return@onObjectClick
        }
        if(e.player.x >= e.obj.x ) {
            e.player.tasks.schedule(1) {
                val door = GameObject(
                    e.obj.id + 1,
                    e.obj.type,
                    e.obj.rotation + 3,
                    e.obj.x,
                    e.obj.y,
                    e.obj.plane
                )
                World.spawnObjectTemporary(door, 2)
                e.player.addWalkSteps(if (e.player.x >= e.obj.x) e.obj.x - 1 else e.obj.x + 1, 3514)
            }
            return@onObjectClick
        }
        if (!PortPhasmatys.hasGhostSpeak(e.player)) {
            e.player.npcDialogue(DISCIPLE, HeadE.CALM_TALK, "Woooo wooooo woooo")
        } else {
            e.player.startConversation {
                npc(DISCIPLE, HeadE.CALM_TALK, "What are you doing going in there?")
                player(HeadE.CALM_TALK, "Err, I was just curious...")
                npc(DISCIPLE, HeadE.CALM_TALK, "Inside that room is a coffin, inside which lie the mortal remains of our most glorious master, Necrovarus. None may enter.")
            }
        }
    }
    //Handle key on Coffin Door
    onItemOnObject(arrayOf(5244), arrayOf(BONEKEY)) { e ->
        if (e.`object`.tile != Tile.of(3656, 3514, 1)) {
            return@onItemOnObject
        }
        if (e.player.inventory.containsItem(BONEKEY) || e.player.x >= e.`object`.x) {
            e.player.tasks.schedule(1) {
                val door = GameObject(
                    e.`object`.id + 1,
                    e.`object`.type,
                    e.`object`.rotation + 3,
                    e.`object`.x,
                    e.`object`.y,
                    e.`object`.plane
                )
                World.spawnObjectTemporary(door, 2)
                e.player.addWalkSteps(if (e.player.x >= e.`object`.x) e.`object`.x - 1 else e.`object`.x + 1, 3514)
            }
        }
    }
    //Handle Energy Barriers
    onObjectClick(5259) { e ->
        val BEDSHEET = 4284
        val ECTOBEDSHEET = 4285
        val p = e.player
        val insideTown = PortPhasmatys.isInsidePhasmatys(p)
        if (p.equipment.hatId == BEDSHEET || p.equipment.hatId == ECTOBEDSHEET) {
            if (!p.inventory.hasFreeSlots()) {
                p.simpleDialogue("You must remove the bedsheet before leaving Port Phasmatys.")
                return@onObjectClick
            }
            val HAT = p.equipment.hatId
            p.equipment.deleteSlot(Equipment.HEAD)
            p.inventory.addItem(HAT)
            p.appearance.transformIntoNPC(-1)
        }
        if (insideTown) {
            handleInsideTownMovement(p)
        } else {
            handleOutsideTownMovement(p)
        }
    }
    //Handle Gangplanks
    onObjectClick(11209, 11210, 17392, 17393) { e ->
        val entering = e.player.x < e.obj.x
        e.player.useStairs(
            -1,
            e.player.transform(if (entering) 3 else -3, 0, if (entering) 1 else -1),
            0,
            1
        )
    }
}
class PortPhasmatys {
companion object {
        fun hasGhostSpeak(player: Player?): Boolean {
            if (player != null) {
                return player.equipment.neckId == 552 || player.equipment.neckId == 4250
            }
            return false
        }

        fun GhostSpeakResponse(player: Player, npc: NPC?) {
            player.npcDialogue(npc, HeadE.FRUSTRATED, "Woooo wooo wooooo woooo")
            player.sendMessage("You cannot understand the ghost.")
        }

        fun isInsidePhasmatys(p: Player): Boolean {
            val boxMinX = 3653
            val boxMaxX = 3709
            val boxMinY = 3458
            val boxMaxY = 3507
            return (p.x in boxMinX..boxMaxX) && (p.y in boxMinY..boxMaxY)
        }


        fun handleInsideTownMovement(p: Player) {
            val closestBarrier = if ((distance(p, northBarrier) < distance(p, westBarrier))) northBarrier else westBarrier

            if (closestBarrier == northBarrier) {
                p.addWalkSteps(northBarrier.x, northBarrier.y + 1, 5, false)
            } else {
                p.addWalkSteps(westBarrier.x - 1, westBarrier.y, 5, false)
            }
        }

        fun handleOutsideTownMovement(p: Player) {
            GhostGuard(p)
        }

        private fun distance(p: Player, barrier: Tile): Double {
            return sqrt((p.x - barrier.x).toDouble().pow(2.0) + (p.y - barrier.y).toDouble().pow(2.0))
        }
    }
}