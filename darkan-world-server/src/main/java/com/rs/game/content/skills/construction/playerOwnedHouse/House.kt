package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.cache.loaders.ObjectDefinitions
import com.rs.cache.loaders.ObjectType
import com.rs.cache.loaders.interfaces.IFEvents
import com.rs.engine.dialogue.Dialogue
import com.rs.engine.dialogue.startConversation
import com.rs.engine.dialogue.statements.Statement
import com.rs.game.World
import com.rs.game.content.skills.construction.playerOwnedHouse.HouseObjects.Companion.getDynamicObjectId
import com.rs.game.map.Chunk
import com.rs.game.map.ChunkManager
import com.rs.game.map.instance.Instance
import com.rs.game.map.instance.InstancedChunk
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.game.tasks.Task
import com.rs.game.tasks.WorldTasks
import com.rs.lib.Constants
import com.rs.lib.game.Animation
import com.rs.lib.game.Item
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.MapUtils
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.PluginEventHandler
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.pow
import com.rs.engine.variables.poh_building_mode
import com.rs.engine.variables.poh_servant_type

@PluginEventHandler
class House {
    // ========================================================================
    //                         PROPERTY DECLARATIONS
    // ========================================================================
    private var roomsR: MutableList<RoomReference> = mutableListOf()
    var location: EntranceLocations

    // House properties
    private var look: Byte = 0
    private var buildMode = true
    private var arriveInPortal = false
    var houseServants: HouseServants? = null
        private set
    private var paymentStage: Byte = 0

    // Transient references
    @Transient
    private var player: Player? = null
    @Transient
    var houseNPCs: HouseNPCs? = null
    @Transient
    private var pets: CopyOnWriteArrayList<NPC>? = null
    @Transient
    private var npcs: CopyOnWriteArrayList<NPC>? = null
    @Transient
    var instance: Instance? = null
    @Transient
    var isLoaded: Boolean = false
        private set
    @Transient
    private var challengeMode = false
    @Transient
    var servantInstance: ServantNPC? = null
        private set

    // Additional references
    @JvmField
    var petHouse: PetHouse? = null
    private var build: Byte = 0
    @Transient
    var playerManager: HousePlayerManager? = null

    // ========================================================================
    //                           HOUSE INITIALIZATION
    // ========================================================================
    init {
        // Initialize managers
        playerManager = HousePlayerManager(this)
        houseNPCs = HouseNPCs(this)
        petHouse = PetHouse()
        roomsR = ArrayList()
        location = EntranceLocations.TAVERLY
        // Add a default GARDEN room (3,3,0) with an arrival portal.
        addRoom(HouseRooms.GARDEN, 3, 3, 0, 0)
        getRoom(3, 3, 0)?.addObject(HouseBuilds.CENTREPIECE, 0)
    }

    fun init() {
        if (build.toInt() == 0) {
            reset()
        }
        houseNPCs = HouseNPCs(this)
        refreshBuildMode()
        refreshArriveInPortal()
        refreshNumberOfRooms()
    }

    /****
     * Updates the client (VarC 944) with the current number of rooms.
     * This helps the interface or counters track how many rooms the player’s house has.
     ****/
    private fun refreshNumberOfRooms() {
        player?.packets?.sendVarc(944, roomsR.size)
    }

    /**
     * Resets the house to a default state (GARDEN room).
     */
    fun reset() {
        build = 1
        buildMode = true
        roomsR = ArrayList()
        addRoom(HouseRooms.GARDEN, 3, 3, 1, 0)
        getRoom(3, 3, 1)!!.addObject(HouseBuilds.CENTREPIECE, 0)
    }

    // ========================================================================
    //                               ROOM MANAGEMENT
    // ========================================================================

    /****
     * Retrieves the room at the given x, y coordinates and plane.
     ****/
    fun getRoom(x: Int, y: Int, plane: Int): RoomReference? {
        return roomsR.find { it.x.toInt() == x && it.y.toInt() == y && it.plane.toInt() == plane }
    }

    /****
     * Finds the room associated with the specified GameObject.
     ****/
    fun getRoomFromObject(`object`: GameObject): RoomReference? {
        val roomX = `object`.tile.chunkX - instance!!.baseChunkX
        val roomY = `object`.tile.chunkY - instance!!.baseChunkY
        return getRoom(roomX, roomY, `object`.plane)
    }

    /**
     * Retrieves the first [RoomReference] matching the given [HouseRooms].
     */
    fun getRoom(room: HouseRooms): RoomReference? {
        return roomsR.find { it.room == room }
    }

    /**
     * Creates a new room
     */
    private fun createRoom(room: RoomReference) {
        if (!player?.inventory!!.hasCoins(room.room.price)) {
            player?.sendMessage("You don't have enough coins to build this room.")
            return
        }
        player?.inventory?.removeCoins(room.room.price)
        player?.tempAttribs?.setO<Any>("CRef", room)
        roomsR.add(room)
        refreshNumberOfRooms()
        refreshHouse()
        player?.setCloseChatboxInterfaceEvent(null)
        player?.setCloseInterfacesEvent(null)
    }

    /****
     * Initiates the creation of a new house room based on the selected slot.
     * Validates room type, player level, and available resources before adding the room.
     ****/
    fun createRoom(slot: Int) {
        val rooms: Array<HouseRooms> = HouseRooms.entries.toTypedArray()
        if (slot >= rooms.size) return
        val position = player?.tempAttribs?.getO<IntArray>("CreationRoom")
        player?.closeInterfaces()
        if (position == null) return
        val room = rooms[slot]
        if ((room == HouseRooms.TREASURE_ROOM || room == HouseRooms.DUNGEON_CORRIDOR || room == HouseRooms.DUNGEON_JUNCTION || room == HouseRooms.DUNGEON_PIT || room == HouseRooms.DUNGEON_STAIRS) && position[2] != 0) {
            player?.sendMessage("That room can only be built underground.")
            return
        }
        if (room == HouseRooms.THRONE_ROOM) if (position[2] != 1) {
            player?.sendMessage("This room cannot be built on a second level or underground.")
            return
        }
        if (room == HouseRooms.OUTBLIETTE) {
            player?.sendMessage("That room can only be built using a throne room trapdoor.")
            return
        }
        if ((room == HouseRooms.GARDEN || room == HouseRooms.FORMAL_GARDEN || room == HouseRooms.MENAGERIE) && position[2] != 1) {
            player?.sendMessage("That room can only be built on ground.")
            return
        }
        if (room == HouseRooms.MENAGERIE && hasRoom(HouseRooms.MENAGERIE)) {
            player?.sendMessage("You can only build one menagerie.")
            return
        }
        if (room == HouseRooms.GAMES_ROOM && hasRoom(HouseRooms.GAMES_ROOM)) {
            player?.sendMessage("You can only build one game room.")
            return
        }
        if (room.level > player?.skills!!.getLevel(Constants.CONSTRUCTION)) {
            player?.sendMessage("You need a Construction level of " + room.level + " to build this room.")
            return
        }
        if (player?.inventory!!.coins < room.price) {
            player?.sendMessage("You don't have enough coins to build this room.")
            return
        }
        if (roomsR.size >= maxQuantityRooms) {
            player?.sendMessage("You have reached the maximum quantity of rooms.")
            return
        }

        val roomRef = RoomReference(room, position[0], position[1], position[2], 0)
        player?.setFinishConversationEvent { player?.house?.previewRoom(roomRef, true) }
        player?.house?.previewRoom(roomRef, true)
        roomRef.setRotation((roomRef.rotation + 1) and 0x3)
        player?.house?.previewRoom(roomRef, false)
        roomDialogue(player!!, roomRef)
    }

    /****
     * Initiates a dialogue for the player to rotate the room clockwise or anticlockwise, build the room, or cancel the action.
     ****/
    private fun roomDialogue(player: Player, roomRef: RoomReference) {
        player.startConversation {
            label("start")
            options("What would you like to do?") {
                ops("Rotate clockwise") {
                    exec {
                        player.house.previewRoom(roomRef, true)
                        roomRef.setRotation((roomRef.rotation + 1) and 0x3)
                        player.house.previewRoom(roomRef, false)
                        roomDialogue(player, roomRef)
                    }
                }
                ops("Rotate anticlockwise.") {
                    exec {
                        player.house.previewRoom(roomRef, true)
                        roomRef.setRotation((roomRef.rotation - 1) and 0x3)
                        player.house.previewRoom(roomRef, false)
                        roomDialogue(player, roomRef)
                    }
                }
                ops("Build.") {
                    exec {
                        player.house.createRoom(roomRef)
                        player.house.refreshHouse()
                    }
                }
                ops("Cancel")
            }
        }
    }

    /****
     * Previews the specified room by spawning or removing its objects based on the 'remove' flag.
     ****/
    fun previewRoom(reference: RoomReference, remove: Boolean) {
        if (!isLoaded)
            return
        val boundX = instance!!.getLocalX(reference.x.toInt(), 0)
        val boundY = instance!!.getLocalY(reference.y.toInt(), 0)
        val realChunkX = reference.room.chunkX
        val realChunkY = reference.room.chunkY
        val chunk: Chunk = ChunkManager.getChunk(
            MapUtils.encode(
                MapUtils.Structure.CHUNK,
                reference.room.chunkX,
                reference.room.chunkY,
                look.toInt() and 0x3
            ), true
        )
        if (reference.plane.toInt() == 0)
            (0..7).forEach { x ->
                (0..7).forEach { y ->
                    val objectR = GameObject(
                        -1,
                        ObjectType.SCENERY_INTERACT,
                        reference.rotation.toInt(),
                        boundX + x,
                        boundY + y,
                        reference.plane.toInt()
                    )
                    if (remove) World.removeObject(objectR)
                    else World.spawnObject(objectR)
                }
            }
        (0..7).forEach { x ->
            (0..7).forEach { y ->
                val objects: Array<GameObject>? =
                    chunk.getBaseObjects(Tile.of((realChunkX shl 3) + x, (realChunkY shl 3) + y, look.toInt() and 0x3))
                if (objects != null) for (`object` in objects) {
                    @Suppress("SENSELESS_COMPARISON")
                    if (`object` == null) continue
                    val defs: ObjectDefinitions = `object`.definitions
                    if (reference.plane.toInt() == 0 || defs.containsOption(4, "Build")) {
                        val objectR = GameObject(`object`)
                        val coords: IntArray = InstancedChunk.transform(
                            x,
                            y,
                            reference.rotation.toInt(),
                            defs.sizeX,
                            defs.sizeY,
                            `object`.rotation
                        )
                        objectR.tile = Tile.of(boundX + coords[0], boundY + coords[1], reference.plane.toInt())
                        objectR.rotation = (`object`.rotation + reference.rotation) and 0x3
                        // just a preview. they're not really there.
                        if (remove) World.removeObject(objectR)
                        else World.spawnObject(objectR)
                    }
                }
            }
        }
    }

    /**
     * Adds a new room to the house at the specified coordinates.
     */
    private fun addRoom(room: HouseRooms, x: Int, y: Int, plane: Int, rotation: Int): Boolean {
        val roomRef = RoomReference(room, x, y, plane, rotation)
        roomsR.add(roomRef)
        return true
    }

    /**
     * Removes a room from the house, refreshing the layout afterward.
     */
    private fun removeRoom(room: RoomReference) {
        if (roomsR.remove(room)) {
            refreshNumberOfRooms()
            refreshHouse()
        }
    }

    /****
     * Removes the current room after verifying it doesn't support other rooms and navigates to the connected staircase room.
     ****/
    fun removeRoom() {
        val roomX = player?.chunkX?.minus(instance?.baseChunkX ?: return) ?: return
        val roomY = player?.chunkY?.minus(instance?.baseChunkY ?: return) ?: return
        val room = getRoom(roomX, roomY, player?.plane ?: return) ?: return
        if (room.z() != 1) {
            player?.simpleDialogue("You cannot remove a building that is supporting this room.")
            return
        }
        val above = getRoom(roomX, roomY, 2)
        val below = getRoom(roomX, roomY, 0)
        val roomTo = when {
            above?.staircaseSlot != null && above.staircaseSlot != -1 -> above
            below?.staircaseSlot != null && below.staircaseSlot != -1 -> below
            else -> null
        }
        if (roomTo == null) {
            player?.simpleDialogue("These stairs do not lead anywhere.")
            return
        }
        openRoomCreationMenu(roomTo.x.toInt(), roomTo.y.toInt(), roomTo.z())
    }

    /**
     * Checks if the house already has a particular room type.
     */
    private fun hasRoom(room: HouseRooms): Boolean {
        return roomsR.any { it.room == room }
    }

    /**
     * Returns the maximum number of rooms the player can have based on Construction level.
     */
    private val maxQuantityRooms: Int
        get() {
            val consLvl = player?.skills?.getLevelForXp(Constants.CONSTRUCTION) ?: 1
            var maxRoom = 40
            if (consLvl >= 38) {
                maxRoom += (consLvl - 32) / 6
                if (consLvl == 99) maxRoom++
            }
            return maxRoom
        }

    /****
     * Gets the current list of RoomReferences in the house.
     ****/
    val rooms: List<RoomReference>
        get() = roomsR

    // ========================================================================
    //                               BUILD MODE
    // ========================================================================

    /****
     * Enables or disables build mode, rebuilding the house to update build hotspots if it's loaded.
     ****/
    fun setBuildMode(buildMode: Boolean) {
        if (this.buildMode == buildMode) return
        this.buildMode = buildMode
        if (isLoaded) {
            expelGuests()
            if (isOwnerInside) refreshHouse()
        }
        refreshBuildMode()
    }

    /**
     * Updates the build mode varbit so the client can reflect the new state.
     */
    private fun refreshBuildMode() {
        player?.vars?.setVarBit(poh_building_mode, if (buildMode) 1 else 0)
    }

    /****
     * Opens the build interface for the specified GameObject and build type, setting up required items and conditions.
     ****/
    fun openBuildInterface(`object`: GameObject, build: HouseBuilds) {
        if (!buildMode) {
            player?.simpleDialogue("You can only do that in building mode.")
            return
        }
        val itemArray = build.pieces.mapIndexed { index, piece ->
            if ((build in listOf(HouseBuilds.PORTALS1, HouseBuilds.PORTALS2, HouseBuilds.PORTALS3)) && index > 2) null
            else Item(piece.itemId, 1)
        }.toTypedArray()
        if(build.isWater && !hasWaterCan() || !hasTools())
            player?.sendMessage(if (build.isWater) "You will need a watering can with some water in it instead of hammer and saw to build plants." else "You will need a hammer and saw to build furniture.")
        val requirementsValue = build.pieces.indices.sumOf { index ->
            if ((build in listOf(HouseBuilds.PORTALS1, HouseBuilds.PORTALS2, HouseBuilds.PORTALS3)) && index > 2) 0
            else if (hasRequirementsToBuild(false, build, build.pieces[index])) (2.0.pow(index + 1)).toInt()
            else 0
        }
        val dialogue = Dialogue().apply {
            addNext(object : Statement {
                override fun send(player: Player) {
                    player.packets.sendVarc(841, requirementsValue)
                    player.packets.sendItems(398, itemArray)
                    player.packets.setIFEvents(IFEvents(1306, 55, -1, -1).enableContinueButton())
                    itemArray.forEachIndexed { index, _ ->
                        player.packets.setIFEvents(IFEvents(1306, 8 + 7 * index, 4, 4).enableContinueButton())
                    }
                    player.interfaceManager.sendInterface(1306)
                    player.tempAttribs?.setO<Any>("OpenedBuild", build)
                    player.tempAttribs?.setO<Any>("OpenedBuildObject", `object`)
                }

                override fun getOptionId(componentId: Int): Int {
                    return if (componentId == 55) Int.MAX_VALUE else (componentId - 8) / 7
                }

                override fun close(player: Player) {
                    player.closeInterfaces()
                }
            })
            build.pieces.forEachIndexed { index, _ ->
                addNext {
                    player?.house?.build(index)
                }
            }
        }
        player?.startConversation(dialogue)
    }

    /****
     * Checks if the player has the required construction level and materials to build the specified house object.
     ****/
    private fun hasRequirementsToBuild (warn: Boolean, build: HouseBuilds, piece: HouseObjects): Boolean {
        var level = player?.skills?.getLevel(Constants.CONSTRUCTION)
        if (level != null) {
            if (!build.isWater && player?.inventory?.containsOneItem(9625) == true) level += 3
        }
        if (level != null) {
            if (level < piece.level) {
                if (warn) player?.sendMessage("Your construction level is too low.")
                return false
            }
        }
        if (!player?.hasRights(Rights.ADMIN)!!) {
            if (!player?.inventory?.containsItems(*piece.getRequirements(player!!))!!) {
                if (warn) player?.sendMessage("You don't have the right materials.")
                return false
            }
            if (if (build.isWater) !hasWaterCan() else !hasTools()) {
                if (warn) {
                    val message = if (build.isWater) {
                        "You will need a watering can with some water in it instead of hammer and saw to build plants."
                    } else {
                        "You will need a hammer and saw to build furniture."
                    }
                    player?.sendMessage(message)
                }
                return false
            }
        }
        return true
    }

    /****
     * Builds the selected house piece by validating requirements, adding the object to the room,
     * deducting necessary materials, awarding experience, and updating the room state.
     ****/
    fun build(slot: Int) {
        val build: HouseBuilds = player?.tempAttribs?.getO<HouseBuilds>("OpenedBuild") ?: return
        val `object`: GameObject = player?.tempAttribs?.getO<GameObject>("OpenedBuildObject") ?: return
        @Suppress("SENSELESS_COMPARISON")
        if (build == null || `object` == null || build.pieces.size <= slot) return
        val room = getLocalRoomReference(`object`) ?: return
        val piece = build.pieces[slot]
        if (!hasRequirementsToBuild(true, build, piece)) return
        val oref: ObjectReference = room.addObject(build, slot)
        if (isRelevantForDynamicId(oref.piece)) {
            oref.finalId = HouseObjects.getDynamicObjectId(player, oref.piece)
        }
        player?.closeInterfaces()
        player?.lock()
        player?.anim(Animation(if (build.isWater) 2293 else 3683))
        if (!player?.hasRights(Rights.ADMIN)!!) {
            for (item in piece.getRequirements(player!!)) {
                player?.inventory?.deleteItem(item)
            }
        }
        player?.tempAttribs?.removeO<HouseBuilds>("OpenedBuild");
        player?.tempAttribs?.removeO<GameObject>("OpenedBuildObject");
        player!!.tasks.schedule(object : Task() {
            override fun run() {
                player?.skills?.addXp(Constants.CONSTRUCTION, piece.xp)
                if (build.isWater) player?.skills?.addXp(Constants.FARMING, piece.xp)
                refreshObject(room, oref, false)
                player?.unlock()
            }
        }, 0)
    }

    /****
     * Refreshes the specified object in the room, updating or removing it based on the 'remove' flag.
     ****/
    private fun refreshObject(rref: RoomReference, oref: ObjectReference, remove: Boolean) {
        val chunk = ChunkManager.getChunk(instance!!.getChunkId(rref.x.toInt(), rref.y.toInt(), rref.plane.toInt()))
        for (x in 0..7) {
            for (y in 0..7) {
                val objects = chunk.getBaseObjects(Tile.of(chunk.baseX + x, chunk.baseY + y, rref.plane.toInt()))
                if (objects != null) {
                    for (obj in objects) {
                        if (obj == null)
                            continue
                        val slot = oref.build.getIdSlot(obj.id)
                        if (slot == -1)
                            continue
                        if (remove) {
                            World.spawnObject(obj)
                        } else {
                            val objectR = GameObject(obj)
                            val targetId = oref.getId(slot)
                            val houseObject = HouseObjects.values().find { it.ordinal == oref.piece.ordinal }
                            if (targetId == -1) {
                                World.spawnObject(GameObject(-1, obj.type, obj.rotation, obj.tile))
                            } else {
                                if (houseObject != null && isRelevantForDynamicId(houseObject)) {
                                    val dynamicId = oref.finalId
                                    val actualId = if (dynamicId > 0 && dynamicId != targetId) dynamicId else targetId
                                    objectR.setId(actualId)
                                }
                                else {
                                    objectR.setId(oref.getId(slot))
                                }
                                World.spawnObject(objectR)
                            }
                        }
                    }
                }
            }
        }
    }

    /****
     * Opens the room creation menu based on the provided door GameObject.
     ****/
    fun openRoomCreationMenu(door: GameObject) {
        var roomX = player?.chunkX?.minus(instance!!.baseChunkX) // current room
        var roomY = player?.chunkY?.minus(instance!!.baseChunkY) // current room
        val xInChunk = player?.xInChunk
        val yInChunk = player?.yInChunk
        if (roomX != null) {
            if (xInChunk == 7) roomX += 1
            else if (xInChunk == 0) roomX -= 1
            else if (roomY != null) {
                if (yInChunk == 7) roomY += 1
                else if (yInChunk == 0) roomY -= 1
            }
        }
        if (roomX != null) {
            if (roomY != null) {
                openRoomCreationMenu(roomX, roomY, door.plane)
            }
        }
    }

    /****
     * Opens the room creation menu at the specified coordinates and plane.
     ****/
    private fun openRoomCreationMenu(roomX: Int, roomY: Int, plane: Int) {
        if (!buildMode) {
            player?.simpleDialogue("You can only do that in building mode.")
            return
        }
        val room = getRoom(roomX, roomY, plane)
        if (room != null) {
            if (room.plane.toInt() == 1 && getRoom(roomX, roomY, room.plane + 1) != null) {
                player?.simpleDialogue("You can't remove a room that is supporting another room.")
                return
            }
            if (room.room == HouseRooms.THRONE_ROOM && room.plane.toInt() == 1) {
                val bellow = getRoom(roomX, roomY, room.plane - 1)
                if (bellow != null && bellow.room == HouseRooms.OUTBLIETTE) {
                    player?.simpleDialogue("You can't remove a throne room that is supporting a oubliette.")
                    return
                }
            }
            if ((room.room == HouseRooms.GARDEN || room.room == HouseRooms.FORMAL_GARDEN) && this.portalCount < 2) {
                if (room === portalRoom) {
                    player?.simpleDialogue("Your house must have at least one exit portal.")
                    return
                }
            }
            player?.startConversation {
                options("Do you really want to remove the room?") {
                    ops("Yes") {
                        options("You can't get anything back? Remove room?") {
                            ops("Yes, get rid of my money already!") {
                                exec {
                                    player?.house?.removeRoom(room)
                                }
                            }
                            ops("No.")
                        }
                    }
                    ops("No")
                }
            }
        } else {
            if (roomX == 0 || roomY == 0 || roomX == 7 || roomY == 7) {
                player?.simpleDialogue("You can't create a room here.")
                return
            }
            if (plane == 2) {
                val r = getRoom(roomX, roomY, 1)
                if (r == null || (r.room == HouseRooms.GARDEN || r.room == HouseRooms.FORMAL_GARDEN || r.room == HouseRooms.MENAGERIE)) {
                    player?.simpleDialogue("You can't create a room here.")
                    return
                }
            }
            for (index in 0 until HouseRooms.entries.toTypedArray().size - 2) {
                val refRoom: HouseRooms =
                    HouseRooms.entries.toTypedArray()[index]
                if (player?.skills!!.getLevel(Constants.CONSTRUCTION) >= refRoom.level && player?.inventory!!.hasCoins(
                        refRoom.price
                    )
                ) player?.packets!!.setIFText(
                    402,
                    index + (if (refRoom == HouseRooms.DUNGEON_STAIRS || refRoom == HouseRooms.DUNGEON_PIT) 69 else if (refRoom == HouseRooms.TREASURE_ROOM) 70 else 68),
                    "<col=008000> " + refRoom.price + " coins"
                )
            }
            player?.interfaceManager!!.sendInterface(402)
            player?.tempAttribs?.setO<Any>("CreationRoom", intArrayOf(roomX, roomY, plane))
            player?.setCloseInterfacesEvent { player?.tempAttribs?.removeO<Any>("CreationRoom") }
        }
    }

    /****
     * Initiates the process to remove a build from the specified GameObject.
     ****/
    fun openRemoveBuild(`object`: GameObject) {
        if (!buildMode) {
            player?.sendMessage("Building mode is not active.")
            player?.simpleDialogue("You can only do that in building mode.")
            return
        }
        if (`object`.id == HouseObjects.getDynamicObjectId(player, HouseObjects.EXIT_PORTAL) && portalCount <= 1) {
            player?.sendMessage("Cannot remove this portal. Only $portalCount portals left.")
            player?.simpleDialogue("Your house must have at least one exit portal.")
            return
        }
        val room = getRoomFromObject(`object`) ?: return
        @Suppress("SENSELESS_COMPARISON")
        if (room == null) {
            player?.sendMessage("Room is null for coordinates: roomX=${room.x()}, roomY=${room.y()}, plane=${room.plane}")
            return
        }
        val ref = room.getObject(`object`, player)
        if (ref != null) {
            if (ref.build.name.contains("STAIRCASE", ignoreCase = true)) {
                if (`object`.plane != 1) {
                    val above = getRoom(room.x(), room.y(), 2)
                    val below = getRoom(room.x(), room.y(), 0)
                    if ((above != null && above.staircaseSlot != -1) || (below != null && below.staircaseSlot != -1)) {
                        player?.sendMessage("Room is supporting another room.")
                        player?.simpleDialogue("You cannot remove a building that is supporting this room.")
                        return
                    }
                }
            }
            player?.startConversation {
                options("Really remove it?") {
                    ops("Yes.") {
                        exec {
                            player?.house?.removeBuild(`object`)
                        }
                    }
                    ops("No.")
                }
            }
        } else {
            player?.sendMessage("No reference found for the given object.")
        }
    }


    /****
     * Removes the specified build from the GameObject.
     ****/
    private fun removeBuild(`object`: GameObject) {
        if (!buildMode) {
            player?.simpleDialogue("You can only do that in building mode.")
            return
        }
        val room = getLocalRoomReference(`object`) ?: return
        val oref = room.removeObject(`object`, player) ?: return
        if (isRelevantForDynamicId(oref.piece)) {
            val dynamicId = HouseObjects.getDynamicObjectId(player, oref.piece)
            if (`object`.id != dynamicId) {
                player?.sendMessage("Object ID: ${`object`.id} did not match $dynamicId ${ObjectDefinitions.getDefs(dynamicId)}")
                return
            }
        }
        player?.lock()
        player?.anim(Animation(3685))
        player?.tasks?.schedule(object : Task() {
            override fun run() {
                World.removeObject(`object`)
                refreshObject(room, oref, remove = true)
                player?.unlock()
            }
        })
    }

    /****
     * Handles interactions when a player uses a lever.
     ****/
    fun handleLever(player: Player, `object`: GameObject?) {
        if (buildMode || player.isLocked) return
        val room = `object`?.let { getLocalRoomReference(it) } ?: return
        val trap = room.trapObject
        player.anim(Animation(9497))
        if (trap == -1 || trap == HouseObjects.FLOORDECORATION.getId()) return
        player.lock(7)
        when (trap) {
            HouseObjects.TRAPDOOR.getId() -> player.sendOptionDialogue("What would you like to do?") { ops ->
                ops.add("Drop into oubliette") { dropPlayers(room.x(), room.y(), 13681, 13681, 13681, 13681) }
                ops.add("Nothing.")
            }
            HouseObjects.STEELCAGE.getId() -> {
                trapPlayers(room.x(), room.y(), 13681, 13681, 13681, 13681)
                player.sendOptionDialogue("What would you like to do?") { ops ->
                    ops.add("Release players") { releasePlayers(room.x(), room.y(), 13681, 13681, 13681, 13681) }
                    ops.add("Nothing.")
                }
            }
            HouseObjects.LESSERMAGICCAGE.getId() -> {
                trapPlayers(room.x(), room.y(), 13682)
                player.sendOptionDialogue("What would you like to do?") { ops ->
                    ops.add("Release players") { releasePlayers(room.x(), room.y(), 13682) }
                    ops.add("Drop into oubliette") { dropPlayers(room.x(), room.y(), 13682) }
                }
            }
            HouseObjects.GREATERMAGICCAGE.getId() -> {
                trapPlayers(room.x(), room.y(), 13683)
                player.sendOptionDialogue("What would you like to do?") { ops ->
                    ops.add("Release players") { releasePlayers(room.x(), room.y(), 13683) }
                    ops.add("Drop into oubliette") { dropPlayers(room.x(), room.y(), 13683) }
                    ops.add("Kick from house") { kickTrapped(room.x(), room.y(), 13683) }
                }
            }
        }
    }

    /****
     * Allows the player to climb a staircase either upwards or downwards and allows for room creation on other levels.
     ****/
    fun climbStaircase(player: Player, `object`: GameObject?, up: Boolean) {
        if (`object` == null || instance == null) return
        val room = getLocalRoomReference(`object`) ?: return
        if (room.plane.toInt() == (if (up) 2 else 0)) {
            player.sendMessage("You are on the " + (if (up) "highest" else "lowest") + " possible level so you cannot add a room " + (if (up) "above" else "under") + " here.")
            return
        }
        val roomTo = getRoom(room.x(), room.y(), room.plane + (if (up) 1 else -1))
        if (roomTo == null) {
            if (buildMode) {
                player.startConversation {
                    options("These stairs do not lead anywhere. Do you want to build a room at the " + (if (up) "top" else "bottom") + "?") {
                        ops("Yes.") {
                            options("Select a room") {
                                ops("Skill hall") {
                                    exec {
                                        val newRoom = RoomReference(
                                            if (up) HouseRooms.HALL_SKILL_DOWN else HouseRooms.HALL_SKILL,
                                            room.x.toInt(),
                                            room.y.toInt(),
                                            room.z() + (if (up) 1 else -1),
                                            room.rotation.toInt()
                                        )
                                        val slot = room.staircaseSlot
                                        if (slot != -1) {
                                            newRoom.addObject(
                                                if (up) HouseBuilds.STAIRCASE_DOWN else HouseBuilds.STAIRCASE,
                                                slot
                                            )
                                            player.house.createRoom(newRoom)
                                        }
                                    }
                                }
                                ops("Quest hall") {
                                    exec {
                                        val newRoom = RoomReference(
                                            if (up) HouseRooms.HALL_QUEST_DOWN else HouseRooms.HALL_QUEST,
                                            room.x.toInt(),
                                            room.y.toInt(),
                                            room.z() + (if (up) 1 else -1),
                                            room.rotation.toInt()
                                        )
                                        val slot = room.staircaseSlot
                                        if (slot != -1) {
                                            newRoom.addObject(
                                                if (up) HouseBuilds.STAIRCASE_DOWN_1 else HouseBuilds.STAIRCASE_1,
                                                slot
                                            )
                                            player.house.createRoom(newRoom)
                                        }
                                    }
                                }
                                if (room.z() == 1 && !up) {
                                    ops("Dungeon stairs room") {
                                        exec {
                                            val newRoom = RoomReference(
                                                HouseRooms.DUNGEON_STAIRS,
                                                room.x.toInt(),
                                                room.y.toInt(),
                                                room.z() + (if (up) 1 else -1),
                                                room.rotation.toInt()
                                            )
                                            val slot = room.staircaseSlot
                                            if (slot != -1) {
                                                newRoom.addObject(HouseBuilds.STAIRCASE_2, slot)
                                                player.house.createRoom(newRoom)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ops("No.")
                    }
                }
            } else player.sendMessage("These stairs do not lead anywhere.")
            return
        }
        if (roomTo.staircaseSlot == -1) {
            player.sendMessage("These stairs do not lead anywhere.")
            return
        }
        player.useStairs(-1, Tile.of(player.x, player.y, player.plane + (if (up) 1 else -1)), 0, 1)
    }

    /****
     * Allows the player to climb a ladder either upwards or downwards and allows for room creation on other levels.
     ****/
    fun climbLadder(player: Player, `object`: GameObject?, up: Boolean) {
        if (`object` == null)
            return
        if (instance == null)
            return
        val room = getLocalRoomReference(`object`) ?: return
        if (room.plane.toInt() == (if (up) 2 else 0)) {
            player.sendMessage(
                "You are on the " + (if (up) "highest" else "lowest") +
                        " possible level so you cannot add a room " + (if (up) "above" else "under") + " here."
            )
            return
        }
        val roomTo = getRoom(room.x(), room.y(), room.plane + (if (up) 1 else -1))
        if (roomTo == null) {
            if (buildMode) {
                player.startConversation {
                    options("This " + (if (up) "ladder" else "trapdoor") + " does not lead anywhere. Do you want to build a room at the " + (if (up) "top" else "bottom") + "?") {
                        ops("Yes.") {
                            options {
                                ops(if ((room.z() == 1 && !up)) "Oubliette" else "Throne room") {
                                    exec {
                                        val r = if ((room.z() == 1 && !up)) HouseRooms.OUTBLIETTE else HouseRooms.THRONE_ROOM
                                        val ladderTrap: HouseBuilds = if ((room.z() == 1 && !up)) HouseBuilds.OUB_LADDER else HouseBuilds.TRAPDOOR
                                        val newRoom = RoomReference(
                                            r,
                                            room.x.toInt(),
                                            room.y.toInt(),
                                            room.z() + (if (up) 1 else -1),
                                            room.rotation.toInt()
                                        )
                                        val slot = room.ladderTrapSlot
                                        if (slot != -1) {
                                            newRoom.addObject(ladderTrap, slot)
                                            player.house.createRoom(newRoom)
                                        }
                                    }
                                }
                                ops("Nevermind.")
                            }
                        }
                        ops("No.")
                    }
                }
            } else {
                player.sendMessage("This does not lead anywhere.")
            }
            return
        }
        if (roomTo.ladderTrapSlot == -1) {
            player.sendMessage("This does not lead anywhere.")
            return
        }
        var xOff = 0
        var yOff = 0
        when (roomTo.rotation.toInt()) {
            0 -> {
                yOff = 6
                xOff = 2
            }
            1 -> {
                yOff = 6
                xOff = 5
            }
            2 -> {
                yOff = 1
                xOff = 5
            }
            3 -> {
                yOff = 1
                xOff = 2
            }
        }
        val planeDest = player.plane + (if (up) 1 else -1)
        val destTile = Tile.of(
            instance!!.getLocalX(roomTo.x.toInt(), xOff),
            instance!!.getLocalY(roomTo.y.toInt(), yOff),
            planeDest
        )
        player.ladder(destTile)
    }

    // ========================================================================
    //                               HOUSE REFRESH
    // ========================================================================

    /****
     * Refreshes the house state by destroying and recreating it.
     ****/
    private fun refreshHouse() {
        destroyHouse()
        isLoaded = false
        sendStartInterface(player)
        createHouse()
    }

    /****
     * Finalizes house operations, typically called when a player leaves.
     ****/
    fun finish() {
        kickGuests()
    }

    /****
     * Destroys the current house instance, clearing all NPCs and servants.
     ****/
    fun destroyHouse() {
        isLoaded = false
        houseNPCs?.pets?.forEach { it.finish() }
        houseNPCs?.pets?.clear()
        houseNPCs?.npcs?.forEach { it.finish() }
        houseNPCs?.npcs?.clear()
        removeServant()
        instance?.destroy()
    }

    /****
     * Sends the initial interface to the player when entering the house.
     ****/
    fun sendStartInterface(player: Player?) {
        player?.lock()
        player?.interfaceManager?.setTopInterface(399, false)
        player?.musicsManager?.playSongAndUnlock(454)
        player?.jingle(22)
    }

    // ========================================================================
    //                    CREATING / INITIALIZING HOUSE INSTANCE
    // ========================================================================

    /****
     * Creates the house by preparing chunk data, adding roofs, initializing the instance, and copying chunks.
     ****/
    fun createHouse() {
        val data = prepareChunkData()
        addRoofsIfNeeded(data)
        initializeInstance()
        copyHouseChunksAndBuild(data)
    }

    /**
     * Prepares an array describing the house layout based on [roomsR].
     */
    private fun prepareChunkData(): Array<Array<Array<Array<Any>?>>> {
        val data = Array(4) { Array(8) { arrayOfNulls<Array<Any>?>(8) } }
        for (reference in roomsR) {
            data[reference.plane.toInt()][reference.x.toInt()][reference.y.toInt()] = arrayOf(
                reference.room.chunkX,
                reference.room.chunkY,
                reference.rotation,
                reference.room.isShowRoof
            )
        }
        return data
    }

    /**
     * Optionally adds roofs to upper floors if `!buildMode`.
     */
    private fun addRoofsIfNeeded(data: Array<Array<Array<Array<Any>?>>>) {
        if (!buildMode) {
            for (x in 1..6) {
                skipY@ for (y in 1..6) {
                    for (plane in 2 downTo 1) {
                        val cell = data[plane][x][y]
                        if (cell != null) {
                            val hasRoof = cell[3] as Boolean
                            if (hasRoof) {
                                val rotation = cell[2] as Byte
                                data[plane + 1][x][y] = arrayOf(
                                    HouseRoofs.ROOF1.chunkX,
                                    HouseRoofs.ROOF1.chunkY,
                                    rotation,
                                    true
                                )
                                continue@skipY
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a new [Instance] for this house.
     */
    private fun initializeInstance() {
        if (instance != null && !instance!!.isDestroyed) {
            instance!!.destroy()
        }
        instance = Instance.of(location.tile, 8, 8)
    }

    /**
     * Copies chunks for each [RoomReference] and spawns the house objects.
     */
    private fun copyHouseChunksAndBuild(data: Array<Array<Array<Array<Any>?>>>) {
        instance!!.requestChunkBound().thenAccept { _ ->
            val regionBuilding = mutableListOf<CompletableFuture<Boolean>>()
            for (plane in data.indices) {
                for (x in data[plane].indices) {
                    for (y in data[plane][x].indices) {
                        val cell = data[plane][x][y]

                        if (cell != null) {
                            val chunkX = cell[0] as Int
                            val chunkY = cell[1] as Int
                            val rotation = (cell[2] as Byte).toInt()
                            regionBuilding.add(
                                instance!!.copyChunk(
                                    x,
                                    y,
                                    plane,
                                    chunkX + if (look >= 4) 8 else 0,
                                    chunkY,
                                    look.toInt() and 0x3,
                                    rotation,
                                    false
                                )
                            )
                        } else {
                            if ((x == 0 || x == 7 || y == 0 || y == 7) && plane == 1) {
                                regionBuilding.add(
                                    instance!!.copyChunk(
                                        x, y, plane,
                                        HouseConstants.BLACK[0], HouseConstants.BLACK[1],
                                        0, 0,
                                        false
                                    )
                                )
                            } else if (plane == 1) {
                                regionBuilding.add(
                                    instance!!.copyChunk(
                                        x, y, plane,
                                        HouseConstants.LAND[0] + if (look >= 4) 8 else 0,
                                        HouseConstants.LAND[1],
                                        look.toInt() and 0x3,
                                        0,
                                        false
                                    )
                                )
                            } else if (plane == 0) {
                                regionBuilding.add(
                                    instance!!.copyChunk(
                                        x, y, plane,
                                        HouseConstants.BLACK[0], HouseConstants.BLACK[1],
                                        0, 0,
                                        false
                                    )
                                )
                            } else {
                                regionBuilding.add(instance!!.clearChunk(x, y, plane, false))
                            }
                        }
                    }
                }
            }
            CompletableFuture.allOf(*regionBuilding.toTypedArray()).thenRun {
                removeLeftoverObjects()
                spawnBuildObjects()
                teleportPlayersAndFinalize()
            }
        }
    }

    /****
     * Removes any leftover objects from the house instance.
     ****/
    private fun removeLeftoverObjects() {
        val regionBuilding: MutableList<CompletableFuture<Boolean>> = ObjectArrayList()
        CompletableFuture.allOf(*regionBuilding.toTypedArray<CompletableFuture<*>>()).thenRun {
            instance?.chunkIds?.forEach { chunkId ->
                val chunk = ChunkManager.getChunk(chunkId, true)
                if (chunk is InstancedChunk) {
                    chunk.loadMap(false, false)
                }
                chunk.spawnedObjects.toList().forEach { obj ->
                    chunk.removeObject(obj)
                }
            }
            instance?.chunkIds?.forEach { chunkId ->
                val chunk = ChunkManager.getChunk(chunkId, true)
                chunk.removedObjects.values.forEach { obj ->
                    chunk.removeObject(obj)
                }
            }
        }
    }

    /****
     * Spawns all build-related objects within the house.
     ****/
    private fun spawnBuildObjects() {
        for (reference in roomsR) {
            spawnObjectsInRoom(reference)
        }
        houseNPCs?.spawnPets()
    }

    /****
     * Spawns objects within a specific room reference.
     ****/
    private fun spawnObjectsInRoom(reference: RoomReference) {
        for (x in 0..7) {
            for (y in 0..7) {
                val chunkId = instance!!.getChunkId(
                    reference.x.toInt(),
                    reference.y.toInt(),
                    reference.plane.toInt()
                )
                val chunk = ChunkManager.getChunk(chunkId, true)
                val objects: Array<GameObject>? = chunk.getBaseObjects(x, y)

                if (objects != null) {
                    for (obj in objects) {
                        @Suppress("SENSELESS_COMPARISON")
                        if (obj == null) continue
                        handleIndividualObjects(reference, obj)
                    }
                }
            }
        }
    }

    /****
     * Handles individual objects within a room, managing doors and build slots.
     ****/
    private fun handleIndividualObjects(reference: RoomReference, obj: GameObject) {
        if (obj.definitions.containsOption(4, "Build") ||
            (reference.room == HouseRooms.MENAGERIE && obj.definitions.name.contains("space"))
        ) {
            if (isDoor(obj)) {
                handleDoorObject(obj)
            } else {
                handleBuildSlots(reference, obj)
            }
            if (!buildMode) {
                World.removeObject(obj)
            }

        } else if (obj.id == HouseConstants.WINDOW_SPACE_ID) {
            val windowObj = GameObject(obj)
            windowObj.setId(HouseConstants.WINDOW_IDS[look.toInt()])
            World.spawnObject(windowObj)

        } else if (isDoorSpace(obj)) {
            World.removeObject(obj)
        }
    }

    /****
     * Processes door objects, replacing them with wall objects if necessary.
     ****/
    private fun handleDoorObject(obj: GameObject) {
        if (!buildMode && obj.plane == 2) {
            val room = getRoomFromObject(obj) ?: return
            @Suppress("SENSELESS_COMPARISON")
            if (room == null) {
                val objectR = GameObject(obj)
                objectR.setId(HouseConstants.WALL_IDS[look.toInt()])
                World.spawnObject(objectR)
            }
        }
    }

    /****
     * Manages build slots by updating or spawning dynamic objects.
     ****/
    private fun handleBuildSlots(reference: RoomReference, obj: GameObject) {
        for (o in reference.objects) {
            val slot: Int = o.build.getIdSlot(obj.id)
            if (slot == -1) continue
            val houseObject = HouseObjects.valueOf(o.piece.name)
            val objectR = GameObject(obj)
            if (o.getId(slot) == -1) {
                World.spawnObject(GameObject(-1, obj.type, obj.rotation, obj.tile))
            } else {
                if (isRelevantForDynamicId(houseObject)) {
                    val dynamicId = getDynamicObjectId(player, houseObject)
                    objectR.setId(dynamicId)
                } else {
                    objectR.setId(o.getId(slot))
                }
                World.spawnObject(objectR)
                if (houseNPCs?.spawnGuardNPCs(slot, o, obj) == true) {
                    return
                }
            }
        }
    }

    // ========================================================================
    //                 FINALIZE LOADING & PLAYER TELEPORT
    // ========================================================================

    /****
     * Teleports players and finalizes the house loading process.
     ****/
    private fun teleportPlayersAndFinalize() {
        teleportPlayer(player)
        player?.isForceNextMapLoadRefresh = true
        player?.tasks?.schedule(1) {
            player?.interfaceManager?.setDefaultTopInterface()
            refreshServant()
        }
        if (player?.tempAttribs?.getO<Any?>("CRef") != null
            && player?.tempAttribs?.getO<Any>("CRef") is RoomReference
        ) {
            val toRoom = player?.tempAttribs?.getO<RoomReference>("CRef")
            player?.tempAttribs?.removeO<Any>("CRef")
            teleportPlayer(player, toRoom)
        }
        player?.lock(1)
        isLoaded = true
    }

    // ========================================================================
    //                         MODE SWITCHING / TELEPORTS
    // ========================================================================

    /****
     * Delegates to teleportEntity
     ****/
    @JvmOverloads
    fun teleportPlayer(player: Player?, room: RoomReference? = portalRoom) {
        if(player == null) return
        teleportEntity(player, room)
    }

    /****
     * Delegates to teleportEntity
     ****/
    @JvmOverloads
    fun teleportServant(room: RoomReference? = portalRoom) {
        if (servantInstance == null) return
        teleportEntity(servantInstance!!, room)
    }

    /**
     * Teleports an [Entity] to the specified [RoomReference] plus offsets for positioning.
     */
    fun teleportEntity(entity: Entity, room: RoomReference?) {
        if (room == null) {
            if (entity is Player) {
                entity.sendMessage("Error, tried teleporting to a room that doesn't exist.")
            }
            if (entity === servantInstance) {
                servantInstance?.resetWalkSteps()
            }
            return
        }
        else {
            val rotation = room.rotation
            if (rotation.toInt() == 0) {
                instance!!.teleportChunkLocal(entity, room.x.toInt(), 3, room.y - 1, 3, room.plane.toInt())
            } else {
                instance!!.teleportChunkLocal(entity, room.x.toInt(), 3, room.y.toInt(), 3 + 1, room.plane.toInt())
            }
        }
    }


    // ========================================================================
    //                          SPAWN/REMOVE LOGIC
    // ========================================================================

    /****
     * Checks if the house contains any objects with the specified IDs.
     ****/
    fun containsAnyObject(vararg ids: Int): Boolean {
        instance?.chunkIds?.forEach { chunkId ->
            val chunk = ChunkManager.getChunk(chunkId, true)
            chunk.spawnedObjects.forEach { wo ->
                if (ids.any { it == wo.id }) return true
            }
        }
        return false
    }

    /****
     * Determines if the given ID corresponds to a window object.
     ****/
    fun isWindow(id: Int): Boolean {
        return id == 13830
    }

    // ========================================================================
    //                        GETTERS & SETTERS
    // ========================================================================

    /****
     * Returns whether the house is currently in build mode.
     ****/
    fun isBuildMode(): Boolean = buildMode

    /****
     * Sets the player associated with the house, initializing pets and NPCs if necessary.
     ****/
    fun setPlayer(player: Player?) {
        this.player = player
        if (petHouse == null) petHouse = PetHouse()
        if (pets == null) pets = CopyOnWriteArrayList()
        if (npcs == null) npcs = CopyOnWriteArrayList()
        petHouse!!.setPlayer(player)
        refreshServantVarBit()
    }
    fun changeLook(look: Int) {
        if (look > 6 || look < 0) return
        this.look = look.toByte()
    }
    fun getPlayer(): Player? = player
    // Payment stage logic
    fun getPaymentStage(): Int = paymentStage.toInt()
    fun resetPaymentStage() { paymentStage = 0 }
    fun incrementPaymentStage() { paymentStage++ }
    fun getCenterTile(rRef: RoomReference?): Tile? {
        if (instance == null || rRef == null) return null
        return instance!!.getLocalTile(rRef.x * 8 + 3, rRef.y * 8 + 3)
    }

    /****
     * Gets the local room reference based on the provided GameObject.
     ****/
    private fun getLocalRoomReference(`object`: GameObject): RoomReference? {
        val localX = `object`.tile.chunkX - (instance?.baseChunkX ?: return null)
        val localY = `object`.tile.chunkY - (instance?.baseChunkY ?: return null)
        return getRoom(localX, localY, `object`.plane)
    }
    private fun hasWaterCan(): Boolean {
        for (id in 5333..5340) if (player?.inventory!!.containsOneItem(id)) return true
        return false
    }
    private fun hasTools(): Boolean {
        val inventory = player?.inventory ?: return false
        val hasHammer = inventory.containsItem(HouseConstants.HAMMER, 1)
        val hasSaw = inventory.containsItem(HouseConstants.SAW, 1)
        val hasCrystalSaw = inventory.containsOneItem(9625)
        return hasHammer && (hasSaw || hasCrystalSaw)
    }
    fun isSky(x: Int, y: Int, plane: Int): Boolean {
        return buildMode && plane == 2 && getRoom(
            (x / 8) - instance!!.baseChunkX,
            (y / 8) - instance!!.baseChunkY,
            plane
        ) == null
    }
    fun isDoor(`object`: GameObject?): Boolean {
        return `object`?.definitions?.name?.equals("Door hotspot", ignoreCase = true) ?: false
    }
    private fun isDoorSpace(`object`: GameObject?): Boolean {
        return `object`?.definitions?.name?.equals("Door space", ignoreCase = true) ?: false
    }

    /****
     * Retrieves the GameObject for building within a specific room reference and build type.
     ****/
    fun getWorldObjectForBuild(reference: RoomReference, build: HouseBuilds): GameObject? {
        val boundX = instance!!.getLocalX(reference.x.toInt(), 0)
        val boundY = instance!!.getLocalY(reference.y.toInt(), 0)
        for (x in -1..7) for (y in -1..7) for (piece in build.pieces) {
            val `object`: GameObject? =
                World.getObjectWithId(Tile.of(boundX + x, boundY + y, reference.plane.toInt()), piece.itemId)
            if (`object` != null) return `object`
        }
        return null
    }

    /****
     * Retrieves a specific GameObject within a room based on its ID.
     ****/
    fun getWorldObject(reference: RoomReference, id: Int): GameObject? {
        val boundX = instance!!.getLocalX(reference.x.toInt(), 0)
        val boundY = instance!!.getLocalY(reference.y.toInt(), 0)
        for (x in -1..7) for (y in -1..7) {
            val `object`: GameObject? =
                World.getObjectWithId(Tile.of(boundX + x, boundY + y, reference.plane.toInt()), id)
            if (`object` != null) return `object`
        }
        return null
    }

    fun isRelevantForDynamicId(objectType: HouseObjects): Boolean {
        return when (objectType) {
            HouseObjects.OAK_DECORATION,
            HouseObjects.TEAK_DECORATION,
            HouseObjects.GILDED_DECORATION,
            HouseObjects.ROUND_SHIELD,
            HouseObjects.SQUARE_SHIELD,
            HouseObjects.KITE_SHIELD -> true
            else -> false
        }
    }

    // ========================================================================
    //                           ARRIVE & LOCK LOGIC
    // ========================================================================
    fun setArriveInPortal(arriveInPortal: Boolean) {
        this.arriveInPortal = arriveInPortal
        refreshArriveInPortal()
    }
    fun arriveOutsideHouse(): Boolean = arriveInPortal

    private fun refreshArriveInPortal() {
        player?.vars?.setVarBit(6450, if (arriveInPortal) 1 else 0)
    }

    // ========================================================================
    //                        CHALLENGE MODE LOGIC
    // ========================================================================
    fun isChallengeMode(): Boolean = challengeMode
    fun toggleChallengeMode(player: Player) {
        if (isOwner(player)) {
            setChallengeMode(!challengeMode)
        } else {
            player.sendMessage("Only the house owner can toggle challenge mode on or off.")
        }
    }
    private fun setChallengeMode(challengeMode: Boolean) {
        this.challengeMode = challengeMode
        val msgOnOff = if (challengeMode) "on" else "off"
        val msgOpenClosed = if (challengeMode) "open" else "closed"
        playerManager?.getPlayers()?.forEach { p ->
            p?.sendMessage("<col=FF0000>The owner has turned $msgOnOff PVP dungeon challenge mode.</col>")
            p?.sendMessage("<col=FF0000>The dungeon is now $msgOpenClosed to PVP combat.</col>")
        }
    }

    // ========================================================================
    //                           HOUSE-SERVANT LOGIC
    // ========================================================================
    fun hasServant(): Boolean = houseServants != null
    fun setServantOrdinal(ordinal: Byte) {
        if (ordinal == (-1).toByte()) {
            removeServant()
            houseServants = null
            refreshServantVarBit()
            return
        }
        houseServants = HouseServants.entries.toTypedArray()[ordinal.toInt()]
        refreshServantVarBit()
    }
    private fun removeServant() {
        servantInstance?.finish()
        servantInstance = null
    }
    private fun addServant() {
        if (servantInstance == null && houseServants != null) {
            servantInstance = ServantNPC(this)
        }
    }
    private fun refreshServant() {
        removeServant()
        addServant()
    }
    private fun refreshServantVarBit() {
        var bit = if (houseServants == null) 0 else ((houseServants!!.ordinal * 2) + 1)
        if (houseServants == HouseServants.DEMON_BUTLER) bit = 8
        player?.vars?.setVarBit(poh_servant_type, bit)
    }

    // ========================================================================
    //                           PORTALS & MENAGERIE
    // ========================================================================
    private val portalRoom: RoomReference?
        get() = roomsR.firstOrNull { room ->
            (room.room == HouseRooms.GARDEN || room.room == HouseRooms.FORMAL_GARDEN) &&
                    room.objects.any { it.piece == HouseObjects.EXIT_PORTAL || it.piece == HouseObjects.EXITPORTAL }
        }

    private val portalCount: Int
        get() {
            var count = 0
            roomsR.forEach { roomRef ->
                if (roomRef.room == HouseRooms.GARDEN || roomRef.room == HouseRooms.FORMAL_GARDEN) {
                    count += roomRef.objects.count {
                        it.piece == HouseObjects.EXIT_PORTAL || it.piece == HouseObjects.EXITPORTAL
                    }
                }
            }
            return count
        }

    val menagerie: RoomReference?
        get() = roomsR.firstOrNull { ref ->
            ref.room == HouseRooms.MENAGERIE && ref.objects.any {
                it.piece == HouseObjects.OAKPETHOUSE ||
                        it.piece == HouseObjects.TEAKPETHOUSE ||
                        it.piece == HouseObjects.MAHOGANYPETHOUSE ||
                        it.piece == HouseObjects.CONSECRATEDPETHOUSE ||
                        it.piece == HouseObjects.DESECRATEDPETHOUSE ||
                        it.piece == HouseObjects.NATURALPETHOUSE
            }
        }

    // ========================================================================
    //                               HOUSE NPC LOGIC
    // ========================================================================
    fun callServant(bellPull: Boolean) {
        if (bellPull) {
            player?.anim(Animation(3668))
            player?.lock(2)
        }
        if (servantInstance == null) {
            player?.sendMessage("The house has no servant.")
            return
        }
        servantInstance!!.isFollowing = true
        servantInstance!!.tele(World.getFreeTile(player!!.tile, 1))
        servantInstance!!.anim(Animation(858))
        player?.startConversation(ServantHouseD(player, servantInstance, true))
    }
    fun removePet(item: Item, update: Boolean) {
        houseNPCs?.removePet(item, update)
    }

    fun addPet(item: Item, update: Boolean) {
        houseNPCs?.addPet(item, update)
    }

    // ========================================================================
    //                           PLAYER MANAGER
    // ========================================================================
    fun expelGuests() {
        playerManager?.expelGuests()
    }

    fun kickGuests() {
        playerManager?.kickGuests()
    }

    fun joinHouse(player: Player?): Boolean {
        return playerManager?.joinHouse(player) ?: false
    }

    @JvmOverloads
    fun leaveHouse(player: Player?, type: Int, controllerRemoved: Boolean = false) {
        playerManager?.leaveHouse(player, type, controllerRemoved)
    }

    fun isOwner(player: Player?): Boolean {
        return playerManager?.isOwner(player) ?: false
    }

    val isOwnerInside: Boolean
        get() = playerManager?.isOwnerInside ?: false

    fun switchLock(player: Player) {
        playerManager?.switchLock(player)
    }

    fun getPlayers(): List<Player?>? {
        return playerManager?.getPlayers()
    }

    var locked: Boolean
        get() = playerManager?.locked ?: false
        set(value) {
            playerManager?.locked = value
        }

    private fun getTrappedPlayers(x: Int, y: Int): ArrayList<Player> {
        val list = ArrayList<Player>()
        for (p in playerManager?.getPlayers()!!) if (p != null && p.controllerManager.controller is HouseController) if ((p.x >= x && p.x <= x + 1) && (p.y >= y && p.y <= y + 1)) list.add(
            p
        )
        return list
    }

    private fun kickTrapped(roomX: Int, roomY: Int, vararg trapIds: Int) {
        val x = instance!!.getLocalX(roomX, 3)
        val y = instance!!.getLocalY(roomY, 3)
        for (p in getTrappedPlayers(x, y)) {
            if (isOwner(p)) {
                p.forceTalk("Trying to kick the house owner... Pfft..")
                continue
            }
            leaveHouse(p, KICKED)
        }
        releasePlayers(roomX, roomY, *trapIds)
    }

    private fun dropPlayers(roomX: Int, roomY: Int, vararg trapIds: Int) {
        val roomTo = getRoom(roomX, roomY, 0)
        if (roomTo == null || roomTo.ladderTrapSlot == -1) {
            releasePlayers(roomX, roomY, *trapIds)
            return
        }
        val x = instance!!.getLocalX(roomX, 3)
        val y = instance!!.getLocalY(roomY, 3)
        for (p in getTrappedPlayers(x, y)) {
            p.lock(10)
            p.anim(Animation(1950))
            WorldTasks.schedule(object : Task() {
                override fun run() {
                    p.tele(Tile.of(p.x, p.y, 0))
                    p.anim(Animation(3640))
                }
            }, 5)
        }
        releasePlayers(roomX, roomY, *trapIds)
    }

    private fun releasePlayers(roomX: Int, roomY: Int, vararg trapIds: Int) {
        val x = instance!!.getLocalX(roomX, 3)
        val y = instance!!.getLocalY(roomY, 3)
        World.removeObject(GameObject(trapIds[0], ObjectType.SCENERY_INTERACT, 1,
            player?.plane?.let { Tile.of(x, y, it) }))
        if (trapIds.size > 1) World.removeObject(
            GameObject(
                trapIds[1],
                ObjectType.SCENERY_INTERACT,
                0,
                player?.plane?.let { Tile.of(x + 1, y, it) }
            )
        )
        if (trapIds.size > 2) World.removeObject(
            GameObject(
                trapIds[2],
                ObjectType.SCENERY_INTERACT,
                2,
                player?.plane?.let { Tile.of(x, y + 1, it) }
            )
        )
        if (trapIds.size > 3) World.removeObject(
            GameObject(
                trapIds[3],
                ObjectType.SCENERY_INTERACT,
                3,
                player?.plane?.let { Tile.of(x + 1, y + 1, it) }
            )
        )
        player?.plane?.let { Tile.of(x - 1, y + 1, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x - 1, y, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x, y + 2, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x + 1, y + 2, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x, y - 1, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x + 1, y - 1, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x + 2, y, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        player?.plane?.let { Tile.of(x + 2, y + 1, it) }?.let { it ->
            World.getObjectWithType(it, ObjectType.WALL_STRAIGHT)
                ?.let { World.removeObject(it) }
        }
        for (p in getTrappedPlayers(x, y)) p.resetWalkSteps()
    }

    private fun trapPlayers(roomX: Int, roomY: Int, vararg trapIds: Int) {
        val x = instance!!.getLocalX(roomX, 3)
        val y = instance!!.getLocalY(roomY, 3)
        World.spawnObject(GameObject(trapIds[0], ObjectType.SCENERY_INTERACT, 1,
            player?.plane?.let { Tile.of(x, y, it) }))
        if (trapIds.size > 1) World.spawnObject(
            GameObject(
                trapIds[1],
                ObjectType.SCENERY_INTERACT,
                0,
                player?.plane?.let { Tile.of(x + 1, y, it) }
            )
        )
        if (trapIds.size > 2) World.spawnObject(
            GameObject(
                trapIds[2],
                ObjectType.SCENERY_INTERACT,
                2,
                player?.plane?.let { Tile.of(x, y + 1, it) }
            )
        )
        if (trapIds.size > 3) World.spawnObject(
            GameObject(
                trapIds[3],
                ObjectType.SCENERY_INTERACT,
                3,
                player?.plane?.let { Tile.of(x + 1, y + 1, it) }
            )
        )
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 2, player?.plane?.let { Tile.of(x - 1, y + 1, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 2, player?.plane?.let { Tile.of(x - 1, y, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 3, player?.plane?.let { Tile.of(x, y + 2, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 3, player?.plane?.let { Tile.of(x + 1, y + 2, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 1, player?.plane?.let { Tile.of(x, y - 1, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 1, player?.plane?.let { Tile.of(x + 1, y - 1, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 0, player?.plane?.let { Tile.of(x + 2, y, it) }))
        World.spawnObject(GameObject(13150, ObjectType.WALL_STRAIGHT, 0, player?.plane?.let { Tile.of(x + 2, y + 1, it) }))
        for (p in getTrappedPlayers(x, y)) p.resetWalkSteps()
    }

    // ========================================================================
    //                            ENTER & LEAVE
    // ========================================================================
    fun enterMyHouse() {
        joinHouse(player)
    }

    companion object {
        // House removal types
        @JvmField var LOGGED_OUT: Int = 0
        @JvmField var KICKED: Int = 1
        @JvmField var TELEPORTED: Int = 2

        fun enterHouse(player: Player, username: String?) {
            val owner: Player? = World.getPlayerByDisplay(username)
            if (owner == null || !owner.isRunning || owner.house == null || owner.house.locked) {
                player.sendMessage("That player is offline, or has privacy mode enabled.")
                return
            }
            @Suppress("SENSELESS_COMPARISON")
            if (owner.house.location == null || !player.withinDistance(owner.house.location.tile, 16)) {
                player.sendMessage(
                    "That player's house is at " + Utils.formatPlayerNameForDisplay(owner.house.location.name)
                        .replace("Portal", "") + "."
                )
                return
            }
            owner.house.joinHouse(player)
        }

        fun leaveHouse(player: Player) {
            val controller = player.controllerManager.getController(HouseController::class.java)
            if (controller == null) {
                player.sendMessage("You're not in a house.")
                return
            }
            player.isCanPvp = false
            player.removeHouseOnlyItems()
            controller.house?.leaveHouse(player, KICKED)
        }

        // Offsets for door directions
        val DOOR_DIR_X = intArrayOf(-1, 0, 1, 1)
        val DOOR_DIR_Y = intArrayOf(0, 1, 0, -1)
    }
}
