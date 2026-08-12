package com.rs.game.content.world

import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick

const val SWAMP_TAR = 1939
const val BULLSEYE_LANTERN = 1927
const val BULLSEYE_FILLED = 1928
const val SAPPHIRE_LANTERN = 4700
const val SAPPHIRE_FILLED = 4701
const val EMERALD_LANTERN = 9064
const val EMERALD_FILLED = 9065

@ServerStartupEvent
fun mapStills() {
    onObjectClick("Fractionation still") { e -> processStill(e.player) }
    onItemOnObject(arrayOf("Fractionation still"), arrayOf(SWAMP_TAR)) { e -> processStill(e.player) }
}

private fun processStill(p: Player) {
    if (p.inventory.containsItems(Item(SWAMP_TAR), Item(BULLSEYE_LANTERN))) {
        p.inventory.deleteItem(SWAMP_TAR, 1)
        p.inventory.deleteItem(BULLSEYE_LANTERN, 1)
        p.inventory.addItem(BULLSEYE_FILLED, 1)
        return
    }
    if (p.inventory.containsItems(Item(SWAMP_TAR), Item(SAPPHIRE_LANTERN))) {
        p.inventory.deleteItem(SWAMP_TAR, 1)
        p.inventory.deleteItem(SAPPHIRE_LANTERN, 1)
        p.inventory.addItem(SAPPHIRE_FILLED, 1)
        return
    }
    if (p.inventory.containsItems(Item(SWAMP_TAR), Item(EMERALD_LANTERN))) {
        p.inventory.deleteItem(SWAMP_TAR, 1)
        p.inventory.deleteItem(EMERALD_LANTERN, 1)
        p.inventory.addItem(EMERALD_FILLED, 1)
        return
    }
}