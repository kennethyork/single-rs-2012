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
package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.cache.loaders.ItemDefinitions
import com.rs.cache.loaders.ObjectDefinitions
import com.rs.engine.dialogue.Dialogue
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.Options
import com.rs.engine.dialogue.statements.MakeXStatement
import com.rs.engine.pathfinder.Direction
import com.rs.engine.quest.Quest
import com.rs.game.content.PlayerLook
import com.rs.game.content.dnds.shootingstar.ShootingStars
import com.rs.game.content.items.liquid_containers.FillAction
import com.rs.game.content.skills.construction.SitChair
import com.rs.game.content.skills.construction.TabletMaking
import com.rs.game.content.skills.cooking.Cooking
import com.rs.game.content.skills.cooking.CookingD
import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.Rune
import com.rs.game.content.skills.magic.RuneSet
import com.rs.game.content.skills.magic.TeleType
import com.rs.game.content.transportation.ItemTeleports
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Controller
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.game.tasks.Task
import com.rs.lib.Constants
import com.rs.lib.game.Animation
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.lib.net.ClientPacket
import com.rs.plugin.events.InputIntegerEvent

class HouseController(@field:Transient val house: House?) : Controller() {
    override fun start() {
        player.setForceMultiArea(true)
    }

    override fun sendDeath(): Boolean {
        player.lock(7)
        player.stopAll()
        player.tasks.scheduleLooping(object : Task() {
            var loop: Int = 0
            override fun run() {
                player.stopAll()
                when (loop) {
                    0 -> player.anim(836)
                    1 -> player.sendMessage("Oh dear, you have died.")
                    3 -> {
                        player.anim(Animation(-1))
                        house?.teleportPlayer(player)
                        player.reset()
                        player.isCanPvp = false
                        stop()
                    }
                }
                loop++
            }
        }, 0, 1)
        return false
    }

    override fun processNPCClick1(npc: NPC): Boolean {
        if (npc is ServantNPC) {
            npc.faceEntityTile(player)
            if (!house!!.isOwner(player)) {
                player.npcDialogue(npc.getId(), HeadE.CALM_TALK, "Sorry, I only serve my master.")
                return false
            }
            player.startConversation(ServantHouseD(player, npc, false))
            return false
        }
        return true
    }

    override fun processNPCClick2(npc: NPC): Boolean {
        if (npc is ServantNPC) {
            npc.faceEntityTile(player)
            if (!house!!.isOwner(player)) {
                player.npcDialogue(npc.getId(), HeadE.CALM_TALK, "The servant ignores your request.")
                return false
            }
            player.startConversation(ServantHouseD(player, npc, true))
            return false
        }
        return true
    }

    override fun processItemOnNPC(npc: NPC, item: Item): Boolean {
        if (npc is ServantNPC) {
            npc.faceEntityTile(player)
            if (!house!!.isOwner(player)) {
                player.npcDialogue(npc.getId(), HeadE.CALM_TALK, "The servant ignores your request.")
                return false
            }
            player.startConversation(ItemOnServantD(player, npc, item.id, house.houseServants!!.isSawmill))
            return false
        }
        return false
    }

    override fun processObjectClick5(`object`: GameObject): Boolean {
        if (`object`.definitions.containsOption(4, "Build")) {
            if (!house!!.isOwner(player)) {
                player.sendMessage("You can only do that in your own house.")
                return false
            }
            if (house.isDoor(`object`)) house.openRoomCreationMenu(`object`)
            else for (build in HouseBuilds.entries) if (build.containsId(`object`.id)) {
                house.openBuildInterface(`object`, build)
                return false
            }
        } else if (`object`.definitions.containsOption(4, "Remove")) {
            if (!house!!.isOwner(player)) {
                player.sendMessage("You can only do that in your own house.")
                return false
            }
            house.openRemoveBuild(`object`)
        }
        return false
    }

    override fun processObjectClick1(`object`: GameObject): Boolean {
        if (`object`.definitions.getOption(1).equals("Pray", ignoreCase = true) || (`object`.definitions.getOption(1).equals("Renew-points", ignoreCase = true) && `object`.definitions.name.contains("obelisk"))) return true
        if (`object`.id == HouseObjects.EXIT_PORTAL.getId()) house!!.leaveHouse(player, House.KICKED)
        else if (`object`.id == HouseObjects.CLAY_FIREPLACE.getId() || `object`.id == HouseObjects.STONE_FIREPLACE.getId() || `object`.id == HouseObjects.MARBLE_FIREPLACE.getId()) player.sendMessage("Use some logs on the fireplace in order to light it.")
        else if ((`object`.id in 13581..13587) || (`object`.id in 13300..13306) || (`object`.id in 13665..13671) || (`object`.id in 13694..13696)) player.actionManager.setAction(
            SitChair(
                player,
                `object`
            )
        )
        else if (house!!.isOwner(player) && (`object`.id == 13640 || `object`.id == 13641 || `object`.id == 13639)) directPortals(player, `object`)
        else if (`object`.id in 13615..13635) telePlayer(`object`.id)
        else if (HouseBuilds.DRESSERS.containsObject(`object`)) PlayerLook.openHairdresserSalon(player)
        else if (HouseBuilds.WARDROBE.containsObject(`object`)) PlayerLook.openThessaliasMakeOver(player)
        else if (`object`.id == HouseObjects.GLORY.getId()) ItemTeleports.transportationDialogue(player, 1712)
        else if (HouseBuilds.TELESCOPE.containsObject(`object`)) ShootingStars.viewTelescope(player)
        else if (HouseBuilds.LARDER.containsObject(`object`)) handleLarders(`object`)
        else if (HouseBuilds.SHELVES.containsObject(`object`)) handleShelves(`object`)
        else if (HouseBuilds.TOOL1.containsObject(`object`)) handleTools(`object`)
        else if (HouseBuilds.WEAPONS_RACK.containsObject(`object`)) handleWeapons(`object`)
        else if (HouseBuilds.LEVER.containsObject(`object`)) handleLever(`object`)
        else if (HouseBuilds.ROPE_BELL_PULL.containsObject(`object`)) {
            if (!house.isOwner(player)) {
                player.packets
                    .sendGameMessage("I'd better not do this...")
                return false
            }
            house.callServant(true)
            return false
        } else if (HouseBuilds.LECTURN.containsObject(`object`)) {
            if (house.isBuildMode()) {
                player.sendMessage("You cannot do this while in building mode.")
                return false
            }
            TabletMaking.openTabInterface(player, `object`.id - 13642)
        } else if (HouseBuilds.BOOKCASE.containsObject(`object`)) player.sendMessage("You search the bookcase but find nothing.")
        else if (HouseBuilds.STAIRCASE.containsObject(`object`) || HouseBuilds.STAIRCASE_DOWN.containsObject(`object`)) {
            if (`object`.definitions.getOption(1) == "Climb") player.sendOptionDialogue("Would you like to climb up or down?") { ops: Options ->
                ops.add("Climb up") { house.climbStaircase(player, `object`, true) }
                ops.add("Climb down") { house.climbStaircase(player, `object`, false) }
            }
            else house.climbStaircase(player, `object`, `object`.definitions.getOption(1) == "Climb-up")
        } else if (HouseBuilds.PETHOUSE.containsObject(`object`)) {
            if (house.isOwner(player)) {
                assert(house.petHouse != null)
                house.petHouse!!.open()
            } else player.sendMessage("This isn't your pet house.")
        } else if (HouseBuilds.OUB_LADDER.containsObject(`object`) || HouseBuilds.TRAPDOOR.containsObject(`object`)) house.climbLadder(player, `object`, `object`.definitions.getOption(1) == "Climb")
        else if (HouseBuilds.DOOR.containsObject(`object`) || HouseBuilds.OUB_CAGE.containsObject(`object`)) handleDoor(`object`)
        else if (HouseBuilds.COMBAT_RING.containsObject(`object`)) handleCombatRing(`object`)
        return false
    }

    private fun handleLever(`object`: GameObject) {
        house!!.handleLever(player, `object`)
    }

    private fun handleWeapons(`object`: GameObject) {
        when (`object`.id) {
            HouseObjects.GLOVE_RACK.getId() -> sendTakeItemsDialogue(7671, 7673)
            HouseObjects.WEAPON_RACK.getId() -> sendTakeItemsDialogue(7671, 7673, 7675, 7676)
            HouseObjects.EXTRA_WEAPON_RACK.getId() -> sendTakeItemsDialogue(7671, 7673, 7675, 7676, 7679)
        }
    }

    private fun handleLarders(`object`: GameObject) {
        when (`object`.id) {
            HouseObjects.WOODEN_LARDER.getId() -> sendTakeItemsDialogue(7738, 1927)
            HouseObjects.OAK_LARDER.getId() -> sendTakeItemsDialogue(7738, 1927, 1944, 1933)
            HouseObjects.TEAK_LARDER.getId() -> sendTakeItemsDialogue(7738, 1927, 1944, 1933, 6701, 1550, 1957, 1985)
        }
    }

    private fun handleShelves(`object`: GameObject) {
        when (`object`.id) {
            HouseObjects.WOODEN_SHELVES_1.getId() -> sendTakeItemsDialogue(7688, 7702, 7728)
            HouseObjects.WOODEN_SHELVES_2.getId() -> sendTakeItemsDialogue(7688, 7702, 7728, 7742)
            HouseObjects.WOODEN_SHELVES_3.getId() -> sendTakeItemsDialogue(7688, 7714, 7732, 7742, 1887)
            HouseObjects.OAK_SHELVES_1.getId() -> sendTakeItemsDialogue(7688, 7714, 7732, 7742, 1923)
            HouseObjects.OAK_SHELVES_2.getId() -> sendTakeItemsDialogue(7688, 7714, 7732, 7742, 1923, 1887)
            HouseObjects.TEAK_SHELVES_1.getId() -> sendTakeItemsDialogue(7688, 7726, 7735, 7742, 1923, 2313, 1931)
            HouseObjects.TEAK_SHELVES_2.getId() -> sendTakeItemsDialogue(7688, 7726, 7735, 7742, 1923, 2313, 1931, 1949)
        }
    }

    private fun handleTools(`object`: GameObject) {
        when (`object`.id) {
            HouseObjects.TOOL_STORE_1.getId() -> sendTakeItemsDialogue(8794, 2347, 1755, 1735)
            HouseObjects.TOOL_STORE_2.getId() -> sendTakeItemsDialogue(1925, 946, 952, 590)
            HouseObjects.TOOL_STORE_3.getId() -> sendTakeItemsDialogue(1757, 1785, 1733)
            HouseObjects.TOOL_STORE_4.getId() -> sendTakeItemsDialogue(1592, 1595, 1597, 1599, 11065, 5523)
            HouseObjects.TOOL_STORE_5.getId() -> sendTakeItemsDialogue(5341, 5343, 5329, 5331, 5325, 952)
        }
    }

    private fun handleDoor(`object`: GameObject) {
        var target: Tile? = null
        var direction = Direction.NORTH
        when (`object`.rotation) {
            0 -> if (player.x < `object`.x) {
                target = `object`.tile.transform(1, 0, 0)
                direction = Direction.EAST
            } else {
                target = `object`.tile.transform(-1, 0, 0)
                direction = Direction.WEST
            }

            1 -> if (player.y <= `object`.y) {
                target = `object`.tile.transform(0, 1, 0)
                direction = Direction.NORTH
            } else {
                target = `object`.tile.transform(0, -1, 0)
                direction = Direction.SOUTH
            }

            2 -> if (player.x > `object`.x) {
                target = `object`.tile.transform(-1, 0, 0)
                direction = Direction.WEST
            } else {
                target = `object`.tile.transform(1, 0, 0)
                direction = Direction.EAST
            }

            3 -> if (player.y >= `object`.y) {
                target = `object`.tile.transform(0, -1, 0)
                direction = Direction.SOUTH
            } else {
                target = `object`.tile.transform(0, 1, 0)
                direction = Direction.NORTH
            }
        }
        if (target == null) return
        player.forceMove(target, 741, 1, 35)
    }

    override fun processButtonClick(interfaceId: Int, componentId: Int, slotId: Int, slotId2: Int, packet: ClientPacket): Boolean {
        if (interfaceId == 400)
            when (packet) {
                ClientPacket.IF_OP1 -> TabletMaking.handleTabletCreation(player, componentId, 1)
                ClientPacket.IF_OP2 -> TabletMaking.handleTabletCreation(player, componentId, 5)
                ClientPacket.IF_OP3 -> TabletMaking.handleTabletCreation(player, componentId, 10)
                ClientPacket.IF_OP4 -> player.sendInputInteger("How many would you like to make?", InputIntegerEvent { amount: Int -> TabletMaking.handleTabletCreation(player, componentId, amount) })
                ClientPacket.IF_OP5 -> TabletMaking.handleTabletCreation(player, componentId, player.inventory.getAmountOf(1761))
                else -> return false
            }
        return true
    }

    private fun handleCombatRing(`object`: GameObject) {
        var target: Tile? = null
        var direction = Direction.NORTH
        when (`object`.rotation) {
            0 -> if (player.x < `object`.x) {
                target = `object`.tile.transform(1, 0, 0)
                direction = Direction.EAST
                player.tempAttribs.setB("inBoxingArena", false)
            } else {
                target = `object`.tile.transform(-1, 0, 0)
                direction = Direction.WEST
                player.tempAttribs.setB("inBoxingArena", true)
            }

            1 -> if (player.y <= `object`.y) {
                target = `object`.tile.transform(0, 1, 0)
                direction = Direction.NORTH
                player.tempAttribs.setB("inBoxingArena", true)
            } else {
                target = `object`.tile.transform(0, -1, 0)
                direction = Direction.SOUTH
                player.tempAttribs.setB("inBoxingArena", false)
            }

            2 -> if (player.x > `object`.x) {
                target = `object`.tile.transform(-1, 0, 0)
                direction = Direction.WEST
                player.tempAttribs.setB("inBoxingArena", false)
            } else {
                target = `object`.tile.transform(1, 0, 0)
                direction = Direction.EAST
                player.tempAttribs.setB("inBoxingArena", true)
            }

            3 -> if (player.y >= `object`.y) {
                target = `object`.tile.transform(0, -1, 0)
                direction = Direction.SOUTH
                player.tempAttribs.setB("inBoxingArena", true)
            } else {
                target = `object`.tile.transform(0, 1, 0)
                direction = Direction.NORTH
                player.tempAttribs.setB("inBoxingArena", false)
            }
        }
        if (target == null) return
        player.forceMove(target, 3688, 1, 30)
    }

    override fun processItemOnObject(`object`: GameObject, item: Item): Boolean {
        if (`object`.id == HouseObjects.CLAY_FIREPLACE.getId() || `object`.id == HouseObjects.STONE_FIREPLACE.getId() || `object`.id == HouseObjects.MARBLE_FIREPLACE.getId()) {
            if (item.id != 1511) {
                player.sendMessage("Only ordinary logs can be used to light the fireplace.")
                return false
            }
            if (!player.inventory.containsOneItem(590)) {
                player.sendMessage("You do not have the required items to light this.")
                return false
            }
            player.lock(2)
            player.anim(3658)
            player.skills.addXp(Constants.FIREMAKING, 40.0)
            val objectR: GameObject = GameObject(`object`)
            objectR.setId(`object`.id + 1)
            for (player in house!!.getPlayers()!!) 
                player?.packets?.sendRemoveObject(objectR)
            return false
        }
        if (HouseBuilds.SINK.containsObject(`object`)) {
            val fill: FillAction.Filler = FillAction.isFillable(item)
            @Suppress("SENSELESS_COMPARISON")
            if (fill != null) player.startConversation(Dialogue()
                
                .addNext(
                    MakeXStatement(
                        intArrayOf(fill.filledItem.id),
                        player.inventory.getAmountOf(fill.emptyItem.id)
                    )
                )
                .addNext(Runnable { player.actionManager.setAction(FillAction(MakeXStatement.getQuantity(player), fill)) })
            )
        } else if (HouseBuilds.STOVE.containsObject(`object`)) {
            if (item.id == 7690) {
                player.inventory.deleteItem(7690, 1)
                player.inventory.addItem(7691, 1)
                player.anim(883)
                player.sendMessage("You boil the kettle of water.")
                return false
            }
            player.startConversation(CookingD(player, Cooking.Cookables.forId(item.id), `object`))
            return false
        }
        return true
    }

    override fun canDropItem(item: Item): Boolean {
        if (house!!.isBuildMode()) {
            player.sendMessage("You cannot drop items while in building mode.")
            return false
        }
        return true
    }

    override fun processObjectClick2(`object`: GameObject): Boolean {
        if (`object`.id == HouseObjects.EXIT_PORTAL.getId()) house!!.switchLock(player)
        else if (HouseBuilds.STAIRCASE.containsObject(`object`) || HouseBuilds.STAIRCASE_DOWN.containsObject(`object`)) house!!.climbStaircase(player, `object`, true)
        else if (HouseBuilds.LEVER.containsObject(`object`)) player.sendOptionDialogue("Would you like to toggle PVP challenge mode?") { ops: Options ->
            ops.add("Yes") { house!!.toggleChallengeMode(player) }
            ops.add("No")
        }
        return false
    }

    override fun processObjectClick3(`object`: GameObject): Boolean {
        if (HouseBuilds.STAIRCASE.containsObject(`object`) || HouseBuilds.STAIRCASE_DOWN.containsObject(`object`)) house!!.climbStaircase(player, `object`, false)
        return false
    }

    override fun processObjectClick4(`object`: GameObject): Boolean {
        if (HouseBuilds.STAIRCASE.containsObject(`object`) || HouseBuilds.STAIRCASE_DOWN.containsObject(`object`)) house!!.removeRoom()
        return false
    }

    override fun checkWalkStep(lastX: Int, lastY: Int, nextX: Int, nextY: Int): Boolean {
        return !house!!.isSky(nextX, nextY, player.plane)
    }

    override fun logout(): Boolean {
        player.tile = house!!.location.tile
        player.tele(house.location.tile)
        house.leaveHouse(player, House.LOGGED_OUT)
        return false
    }

    override fun login(): Boolean {
        removeController()
        player.tele(player.house.location.tile)
        return false
    }

    override fun onTeleported(type: TeleType) {
        house!!.leaveHouse(player, House.TELEPORTED)
    }

    // shouldnt happen
    override fun forceClose() {
        player.tile = house!!.location.tile
        player.tele(house.location.tile)
        player.removeHouseOnlyItems()
        house.leaveHouse(player, House.TELEPORTED, true)
    }

    override fun onRemove() {
        player.removeHouseOnlyItems()
        if (house != null) {
            player.tele(house.location.tile)
            house.leaveHouse(player, House.TELEPORTED, true)
        }
    }

    override fun process() {
        if ((house!!.isChallengeMode() && player.plane == 0) || player.tempAttribs.getB("inBoxingArena")) {
            if (!player.isCanPvp) player.isCanPvp = true
        } else if (player.isCanPvp) player.isCanPvp = false
    }

    fun sendTakeItemsDialogue(vararg itemIds: Int) {
        player.sendOptionDialogue("What would you like to take?") { ops: Options ->
            for (itemId in itemIds) ops.add(ItemDefinitions.getDefs(itemId).getName()) { player.inventory.addItem(itemId, 1) }
            ops.add("Nevermind.")
        }
    }

    fun telePlayer(objectId: Int) {
        when (objectId) {
            13615, 13622, 13629 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(3212, 3424, 0), null, null) //Varrock
            13616, 13623, 13630 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(3222, 3218, 0), null, null) //Lumby
            13617, 13624, 13631 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(2964, 3379, 0), null, null) //Falador
            13618, 13625, 13632 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(2757, 3478, 0), null, null) //Camelot
            13619, 13626, 13633 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(2664, 3305, 0), null, null) //Ardougne
            13620, 13627, 13634 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(2546, 3095, 0), null, null) //Yanille
            13621, 13628, 13635 -> Magic.sendNormalTeleportSpell(player, 1, 0.0, Tile.of(3492, 3471, 0), null, null) //Kharyrll
            else -> player.sendMessage("Uh-oh... This shouldn't have happened (Object: $objectId). Please report to staff.")
        }
    }

    companion object {
        fun directPortals(player: Player, `object`: GameObject?) {
            val currentRoom: RoomReference? = `object`?.let { player.house.getRoomFromObject(it) }

            if (currentRoom != null) {
                val portal1 = currentRoom.getBuild(HouseBuilds.PORTALS1)
                val portal2 = currentRoom.getBuild(HouseBuilds.PORTALS2)
                val portal3 = currentRoom.getBuild(HouseBuilds.PORTALS3)

                if (portal1 == null && portal2 == null && portal3 == null) {
                    player.startConversation(Dialogue().addSimple("You should build portal frames first."))
                    return
                }

                val p1d: Dialogue? = getTeleports(player, currentRoom, portal1)
                val p2d: Dialogue? = getTeleports(player, currentRoom, portal2)
                val p3d: Dialogue? = getTeleports(player, currentRoom, portal3)

                val d: Dialogue = Dialogue()
                d.addOptions("Redirect which portal?", object : Options() {
                    override fun create() {
                        if (portal1 == null) option("1: No portal frame") { player.sendMessage("You must build a portal frame before you can redirect this.") }
                        else option("1: " + (if (ObjectDefinitions.getDefs(portal1.id).name.contains("Portal frame")) "Nowhere" else ObjectDefinitions.getDefs(portal1.id).name), p1d)

                        if (portal2 == null) option("2: No portal frame") { player.sendMessage("You must build a portal frame before you can redirect this.") }
                        else option("2: " + (if (ObjectDefinitions.getDefs(portal2.id).name.contains("Portal frame")) "Nowhere" else ObjectDefinitions.getDefs(portal2.id).name), p2d)

                        if (portal3 == null) option("3: No portal frame") { player.sendMessage("You must build a portal frame before you can redirect this.") }
                        else option("3: " + (if (ObjectDefinitions.getDefs(portal3.id).name.contains("Portal frame")) "Nowhere" else ObjectDefinitions.getDefs(portal3.id).name), p3d)
                    }
                })
                player.startConversation(d)
            }
        }

        fun getTeleports(player: Player, room: RoomReference?, portal: ObjectReference?): Dialogue? {
            portal ?: return null
            val teleportDialogue = Dialogue()
            val portalId = portal.id
            val frameType = getPortalFrameType(portalId)
            data class TeleportOption(
                val name: String,
                val excludePortalId: Int,
                val houseObjectIds: Set<Int>,
                val slot: Int,
                val runes: RuneSet,
                val questRequirement: Pair<Quest, String>? = null
            )
            val teleportsByFrameType = when (frameType) {
                "Teak" -> listOf(
                    TeleportOption(
                        name = "Varrock Teleport",
                        excludePortalId = 13615,
                        houseObjectIds = setOf(13615, 13622, 13629),
                        slot = 3,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.FIRE, 100)
                    ),
                    TeleportOption(
                        name = "Lumbridge Teleport",
                        excludePortalId = 13616,
                        houseObjectIds = setOf(13616, 13623, 13630),
                        slot = 4,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.EARTH, 100)
                    ),
                    TeleportOption(
                        name = "Falador Teleport",
                        excludePortalId = 13617,
                        houseObjectIds = setOf(13617, 13624, 13631),
                        slot = 5,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.WATER, 100)
                    ),
                    TeleportOption(
                        name = "Camelot Teleport",
                        excludePortalId = 13618,
                        houseObjectIds = setOf(13618, 13625, 13632),
                        slot = 6,
                        runes = RuneSet(Rune.AIR, 500, Rune.LAW, 100)
                    ),
                    TeleportOption(
                        name = "Ardougne Teleport",
                        excludePortalId = 13619,
                        houseObjectIds = setOf(13619, 13626, 13633),
                        slot = 7,
                        runes = RuneSet(Rune.LAW, 200, Rune.WATER, 200),
                        questRequirement = Pair(Quest.PLAGUE_CITY, "to tune your portal to Ardougne.")
                    ),
                    TeleportOption(
                        name = "Yanille Teleport",
                        excludePortalId = 13620,
                        houseObjectIds = setOf(13620, 13627, 13634),
                        slot = 8,
                        runes = RuneSet(Rune.LAW, 200, Rune.EARTH, 200),
                        questRequirement = Pair(Quest.WATCHTOWER, "to tune your portal to Yanille.")
                    ),
                    TeleportOption(
                        name = "Kharyrll Teleport",
                        excludePortalId = 13621,
                        houseObjectIds = setOf(13621, 13628, 13635),
                        slot = 9,
                        runes = RuneSet(Rune.BLOOD, 100, Rune.LAW, 200),
                        questRequirement = Pair(Quest.DESERT_TREASURE, "to tune your portal to Kharyrll (Canifis).")
                    )
                )
                "Mahogany" -> listOf(
                    TeleportOption(
                        name = "Varrock Teleport",
                        excludePortalId = 13622,
                        houseObjectIds = setOf(13615, 13622, 13629),
                        slot = 10,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.FIRE, 100)
                    ),
                    TeleportOption(
                        name = "Lumbridge Teleport",
                        excludePortalId = 13623,
                        houseObjectIds = setOf(13616, 13623, 13630),
                        slot = 11,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.EARTH, 100)
                    ),
                    TeleportOption(
                        name = "Falador Teleport",
                        excludePortalId = 13624,
                        houseObjectIds = setOf(13617, 13624, 13631),
                        slot = 12,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.WATER, 100)
                    ),
                    TeleportOption(
                        name = "Camelot Teleport",
                        excludePortalId = 13625,
                        houseObjectIds = setOf(13618, 13625, 13632),
                        slot = 13,
                        runes = RuneSet(Rune.AIR, 500, Rune.LAW, 100)
                    ),
                    TeleportOption(
                        name = "Ardougne Teleport",
                        excludePortalId = 13626,
                        houseObjectIds = setOf(13619, 13626, 13633),
                        slot = 14,
                        runes = RuneSet(Rune.LAW, 200, Rune.WATER, 200),
                        questRequirement = Pair(Quest.PLAGUE_CITY, "to tune your portal to Ardougne.")
                    ),
                    TeleportOption(
                        name = "Yanille Teleport",
                        excludePortalId = 13627,
                        houseObjectIds = setOf(13620, 13627, 13634),
                        slot = 15,
                        runes = RuneSet(Rune.LAW, 200, Rune.EARTH, 200),
                        questRequirement = Pair(Quest.WATCHTOWER, "to tune your portal to Yanille.")
                    ),
                    TeleportOption(
                        name = "Kharyrll Teleport",
                        excludePortalId = 13628,
                        houseObjectIds = setOf(13621, 13628, 13635),
                        slot = 16,
                        runes = RuneSet(Rune.BLOOD, 100, Rune.LAW, 200),
                        questRequirement = Pair(Quest.DESERT_TREASURE, "to tune your portal to Kharyrll (Canifis).")
                    )
                )
                "Marble" -> listOf(
                    TeleportOption(
                        name = "Varrock Teleport",
                        excludePortalId = 13629,
                        houseObjectIds = setOf(13615, 13622, 13629),
                        slot = 17,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.FIRE, 100)
                    ),
                    TeleportOption(
                        name = "Lumbridge Teleport",
                        excludePortalId = 13630,
                        houseObjectIds = setOf(13616, 13623, 13630),
                        slot = 18,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.EARTH, 100)
                    ),
                    TeleportOption(
                        name = "Falador Teleport",
                        excludePortalId = 13631,
                        houseObjectIds = setOf(13617, 13624, 13631),
                        slot = 19,
                        runes = RuneSet(Rune.AIR, 300, Rune.LAW, 100, Rune.WATER, 100)
                    ),
                    TeleportOption(
                        name = "Camelot Teleport",
                        excludePortalId = 13632,
                        houseObjectIds = setOf(13618, 13625, 13632),
                        slot = 20,
                        runes = RuneSet(Rune.AIR, 500, Rune.LAW, 100)
                    ),
                    TeleportOption(
                        name = "Ardougne Teleport",
                        excludePortalId = 13633,
                        houseObjectIds = setOf(13619, 13626, 13633),
                        slot = 21,
                        runes = RuneSet(Rune.LAW, 200, Rune.WATER, 200),
                        questRequirement = Pair(Quest.PLAGUE_CITY, "to tune your portal to Ardougne.")
                    ),
                    TeleportOption(
                        name = "Yanille Teleport",
                        excludePortalId = 13634,
                        houseObjectIds = setOf(13620, 13627, 13634),
                        slot = 22,
                        runes = RuneSet(Rune.LAW, 200, Rune.EARTH, 200),
                        questRequirement = Pair(Quest.WATCHTOWER, "to tune your portal to Yanille.")
                    ),
                    TeleportOption(
                        name = "Kharyrll Teleport",
                        excludePortalId = 13635,
                        houseObjectIds = setOf(13621, 13628, 13635),
                        slot = 23,
                        runes = RuneSet(Rune.BLOOD, 100, Rune.LAW, 200),
                        questRequirement = Pair(Quest.DESERT_TREASURE, "to tune your portal to Kharyrll (Canifis).")
                    )
                )
                else ->
                    emptyList()
            }
            teleportDialogue.addOptions("Direct teleport to", object : Options() {
                override fun create() {
                    // Iterate through each teleport option based on frameType
                    teleportsByFrameType.forEach { teleportOption ->
                        // Check if the current portal ID is not excluded and house does not contain conflicting objects
                        if (portalId != teleportOption.excludePortalId && !player.house.containsAnyObject(*teleportOption.houseObjectIds.toIntArray())) {
                            // Add the teleport option to the dialogue
                            option(
                                teleportOption.name,
                                Dialogue().addOption(
                                    "This will cost 100 teleports to tune",
                                    "Pay runes",
                                    "Nevermind"
                                ).addNext(Runnable {
                                    teleportOption.questRequirement?.let { (quest, message) ->
                                        if (!player.isQuestComplete(quest, message)) {
                                            player.sendMessage("You need to complete the quest $message")
                                            return@Runnable
                                        }
                                    }
                                    if (teleportOption.runes.meetsPortalRequirements(player)) {
                                        teleportOption.runes.deleteRunes(player)
                                        room?.let {
                                            player.house.getWorldObject(it, portal.id)?.let { updatedPortal ->
                                                portal.setSlot(teleportOption.slot, updatedPortal)
                                            }
                                        }
                                    }
                                })
                            )
                        }
                    }
                    option("Nevermind")
                }
            })
            return teleportDialogue
        }


        fun getPortalFrameType(objectId: Int): String {
            if ((objectId in 13615..13621) || objectId == 13636) return "Teak"
            if ((objectId in 13622..13628) || objectId == 13637) return "Mahogany"
            if ((objectId in 13629..13635) || objectId == 13638) return "Marble"
            return ""
        }
    }
}