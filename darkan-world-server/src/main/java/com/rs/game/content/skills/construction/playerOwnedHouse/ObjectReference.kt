package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.model.gameobject.GameObject
import com.rs.lib.util.Logger

class ObjectReference(val build: HouseBuilds, slot: Int) {
    var slot: Int
        private set

    init {
        this.slot = slot
    }

    var finalId: Int = -1

    val piece: HouseObjects
        get() {
            if (slot > build.pieces.size - 1) {
                Logger.error(House::class.java, "getPiece", "Error getting piece for ${build.name}")
                return build.pieces[0]
            }
            return build.pieces[slot]
        }

    val id: Int
        get() {
            if (slot > build.pieces.size - 1) {
                Logger.error(House::class.java, "getId", "Error getting ID for ${build.name}")
                return build.pieces[0].getId()
            }
            return build.pieces[slot].getId()
        }

    fun setSlot(slot: Int, `object`: GameObject) {
        this.slot = slot
        `object`.setId(build.pieces[slot].getId())
    }

    val ids: IntArray
        get() {
            if (slot > build.pieces.size - 1) {
                Logger.error(House::class.java, "getIds", "Error getting IDs for ${build.name}")
                return build.pieces[0].getIds()
            }
            return build.pieces[slot].getIds()
        }

    fun retrieveBuild(): HouseBuilds {
        return build
    }

    fun getId(slot2: Int): Int {
        if (slot2 > ids.size - 1) {
            Logger.error(House::class.java, "getId", "Error getting ID for slot $slot2 in ${build.name}")
            return ids[0]
        }
        return ids[slot2]
    }
}