package com.rs.game.content.items

import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnItem

enum class Dye(
    val id: Int,
    val inputID: Int?,
    val inputQuantity: Int?,
    val capeId: Int?
) {
    RED(1763, 1951, 3, 1007),
    YELLOW(1765, 1957, 2, 1023),
    BLUE(1767, 1793, 2, 1021),
    ORANGE(1769, null, null, 1031),
    PURPLE(1773, null, null, 1029),
    GREEN(1771, null, null, 1027),
    PINK(6955, null, null, 6959),
    BLACK(4622, null, null, 1019),
    ;

    companion object {
        fun fromId(id: Int): Dye? = entries.find { it.id == id }
    }
}

@ServerStartupEvent
fun mapDyeMixing() {
    val dyeCombinations = mapOf(
        setOf(Dye.RED.id, Dye.YELLOW.id) to Dye.ORANGE.id,
        setOf(Dye.BLUE.id, Dye.YELLOW.id) to Dye.GREEN.id,
        setOf(Dye.BLUE.id, Dye.RED.id) to Dye.PURPLE.id
    )
    val dyeIds = IntArray(Dye.entries.size) { index -> Dye.entries[index].id }
    onItemOnItem(dyeIds, dyeIds) { e ->
        val result = dyeCombinations[setOf(e.item1.id, e.item2.id)] ?: return@onItemOnItem
        e.player.inventory.deleteItem(e.item1.id, 1)
        e.player.inventory.deleteItem(e.item2.id, 1)
        e.player.inventory.addItem(result)
    }
}

@ServerStartupEvent
fun mapCapeDyeing() {
    val dyeIds = IntArray(Dye.entries.size) { index -> Dye.entries[index].id }
    val capeIdList = buildList {
        addAll(Dye.entries.mapNotNull { it.capeId })
    }
    val capeIds = IntArray(capeIdList.size) { index -> capeIdList[index] }
    onItemOnItem(dyeIds, capeIds) { e ->
        val dye = Dye.fromId(e.item1.id) ?: Dye.fromId(e.item2.id) ?: return@onItemOnItem
        val cape = if (e.item1.id == dye.id) e.item2 else e.item1
        val resultCapeId = dye.capeId ?: return@onItemOnItem
        if (cape.id == resultCapeId) {
            e.player.sendMessage("That cape is already that colour.")
            return@onItemOnItem
        }
        e.player.inventory.deleteItem(dye.id, 1)
        e.player.inventory.replaceItem(resultCapeId, 1, cape.slot)
    }
}