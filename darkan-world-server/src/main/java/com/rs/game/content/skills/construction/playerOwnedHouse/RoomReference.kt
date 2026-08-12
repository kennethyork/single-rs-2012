package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject

class RoomReference(val room: HouseRooms, x: Int, y: Int, plane: Int, rotation: Int) {
    val trapObject: Int
        get() {
            for (`object` in objects) if (`object`.build.toString().contains("FLOOR")) return `object`.piece.getId()
            return -1
        }

    val x: Byte = x.toByte()
    val y: Byte = y.toByte()
    val plane: Byte = plane.toByte()
    var rotation: Byte
        private set
    val objects: MutableList<ObjectReference>

    init {
        this.rotation = rotation.toByte()
        objects = ArrayList()
    }

    val ladderTrapSlot: Int
        get() {
            for (objRef in objects) {
                val buildName = objRef.build.toString()
                if (buildName.contains("OUB_LADDER") || buildName.contains("TRAPDOOR")) {
                    return objRef.slot
                }
            }
            return -1
        }

    val staircaseSlot: Int
        get() {
            for (`object` in objects) if (`object`.build.toString().contains("STAIRCASE")) return `object`.slot
            return -1
        }

    val isStaircaseDown: Boolean
        get() {
            for (`object` in objects) if (`object`.build.toString().contains("STAIRCASE_DOWN")) return true
            return false
        }

    /*
     * x,y inside the room chunk
     */
    fun addObject(build: HouseBuilds, slot: Int): ObjectReference {
        val ref = ObjectReference(build, slot)
        objects.add(ref)
        return ref
    }
    fun getObject(obj: GameObject, player: Player?): ObjectReference? {
        for (o in objects) {
            val normalIds = o.piece.getIds()
            val dynamicId = HouseObjects.getDynamicObjectId(player, o.piece)
            if (normalIds.contains(obj.id) || obj.id == dynamicId) {
                return o
            }
        }
        return null
    }

    fun containsBuild(build: HouseBuilds): Boolean {
        return getBuildSlot(build) != -1
    }

    fun getBuildSlot(build: HouseBuilds): Int {
        for (o in objects) {
            @Suppress("SENSELESS_COMPARISON")
            if (o == null) continue
            if (o.retrieveBuild() == build) return o.slot
        }
        return -1
    }

    fun getBuild(build: HouseBuilds): ObjectReference? {
        for (o in objects) {
            @Suppress("SENSELESS_COMPARISON")
            if (o == null) continue
            if (o.retrieveBuild() == build) return o
        }
        return null
    }

    fun removeObject(`object`: GameObject, player: Player?): ObjectReference? {
        val r = getObject(`object`, player)
        if (r != null) {
            objects.remove(r)
            return r
        }
        return null
    }

    fun setRotation(rotation: Int) {
        this.rotation = rotation.toByte()
    }

    fun z(): Int {
        return plane.toInt()
    }

    fun x(): Int {
        return x.toInt()
    }

    fun y(): Int {
        return y.toInt()
    }
}