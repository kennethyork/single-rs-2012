package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.World
import com.rs.game.model.gameobject.GameObject
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import java.util.concurrent.CopyOnWriteArrayList

class HouseNPCs(private val house: House) {

    val pets = CopyOnWriteArrayList<NPC>()
    val npcs = CopyOnWriteArrayList<NPC>()

    fun spawnGuardNPCs(slot: Int, oRef: ObjectReference, obj: GameObject?): Boolean {
        if (house.isBuildMode()) return false
        val targetId = oRef.getId(slot)
        return when (targetId) {
            HouseObjects.DEMON.getId() -> { spawnNPC(3593, obj)
                true
            }
            HouseObjects.KALPHITESOLDIER.getId() -> {
                spawnNPC(3589, obj)
                true
            }
            HouseObjects.TOKXIL.getId() -> {
                spawnNPC(3592, obj)
                true
            }
            HouseObjects.DAGANNOTH.getId() -> {
                spawnNPC(3591, obj)
                true
            }
            HouseObjects.STEELDRAGON.getId() -> {
                spawnNPC(3590, obj)
                true
            }
            HouseObjects.SKELETON.getId() -> {
                spawnNPC(3581, obj)
                true
            }
            HouseObjects.GUARDDOG.getId() -> {
                spawnNPC(3582, obj)
                true
            }
            HouseObjects.HOBGOBLIN.getId() -> {
                spawnNPC(3583, obj)
                true
            }
            HouseObjects.BABYREDDRAGON.getId() -> {
                spawnNPC(3588, obj)
                true
            }
            HouseObjects.HUGESPIDER.getId() -> {
                spawnNPC(3585, obj)
                true
            }
            HouseObjects.HELLHOUND.getId() -> {
                spawnNPC(3586, obj)
                true
            }
            HouseObjects.TROLLGUARD.getId() -> {
                spawnNPC(3584, obj)
                true
            }
            HouseObjects.PITDOG.getId() -> {
                spawnNPC(11585, obj)
                true
            }
            HouseObjects.PITOGRE.getId() -> {
                spawnNPC(11587, obj)
                true
            }
            HouseObjects.PITROCKPROTECTER.getId() -> {
                spawnNPC(11589, obj)
                true
            }
            HouseObjects.PITSCABARITE.getId() -> {
                spawnNPC(11591, obj)
                true
            }
            HouseObjects.PITBLACKDEMON.getId() -> {
                spawnNPC(11593, obj)
                true
            }
            HouseObjects.PITIRONDRAGON.getId() -> {
                spawnNPC(11595, obj)
                true
            }
            HouseObjects.ROCNAR.getId() -> {
                spawnNPC(3594, obj)
                true
            }
            else -> false
        }
    }

    fun spawnNPC(npcId: Int, obj: GameObject?) {
        val tile = obj?.let { Tile.of(it.x, it.y, it.plane) } ?: return
        val npc = NPC(npcId, tile)
        npcs.add(npc)
        npc.setRandomWalk(false)
        npc.setForceMultiArea(true)
        World.removeObject(obj)
    }

    fun spawnPets() {
        if (house.isBuildMode()) return
        val menagerie = house.menagerie
        if (menagerie != null) {
            for (item in house.petHouse?.pets?.array() ?: emptyArray()) {
                if (item != null) {
                    addPet(item, false)
                }
            }
        }
    }

    fun removePet(item: Item, update: Boolean) {
        if (update && !house.isOwnerInside) return
        val menagerie = house.menagerie
        if (!house.isBuildMode() && menagerie != null) {
            val petType = com.rs.game.content.pets.Pets.forId(item.id) ?: return
            val npcId = if (petType.grownItemId == item.id) petType.grownNpcId else petType.babyNpcId
            for (npc in pets) {
                if (npc.id == npcId) {
                    npc.finish()
                    pets.remove(npc)
                    break
                }
            }
        }
    }

    fun addPet(item: Item, update: Boolean) {
        if (update && !house.isOwnerInside) return
        val menagerie = house.menagerie
        if (!house.isBuildMode() && menagerie != null) {
            val petType = com.rs.game.content.pets.Pets.forId(item.id) ?: return
            val spawnTile = Tile.of(
                house.instance!!.getLocalX(menagerie.x.toInt(), 3),
                house.instance!!.getLocalY(menagerie.y.toInt(), 3),
                menagerie.plane.toInt()
            )
            val npc = NPC(
                if (petType.grownItemId == item.id) petType.grownNpcId else petType.babyNpcId,
                spawnTile
            )
            pets.add(npc)
            npc.setRandomWalk(true)
        }
    }
}
