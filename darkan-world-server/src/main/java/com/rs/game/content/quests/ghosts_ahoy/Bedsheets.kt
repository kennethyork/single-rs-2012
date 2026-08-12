package com.rs.game.content.quests.ghosts_ahoy

import com.rs.game.content.world.areas.desert.HEAD_SLOT
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.game.model.entity.player.Equipment
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemAddedToInventory
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onItemEquip
import com.rs.plugin.kts.onItemOnItem

private const val BEDSHEET = 4284
private const val ECTOBEDSHEET = 4285
@ServerStartupEvent
fun mapBedsheets() {
    onItemClick(BEDSHEET, ECTOBEDSHEET, options = arrayOf("Wear")) { e ->
        if(!PortPhasmatys.isInsidePhasmatys(e.player)) {
            e.player.sendMessage("You can't wear this here.")
            return@onItemClick
        }
        Equipment.sendWear(e.player, e.slotId, e.item.id, true)
        e.player.appearance.transformIntoNPC(if (e.item.id == BEDSHEET) 1707 else 1708)
    }
    onItemEquip(BEDSHEET, ECTOBEDSHEET) { e ->
        if(e.dequip()) {
            e.player.appearance.transformIntoNPC(-1)
        }
    }
    onItemOnItem(BEDSHEET, 4286) { e ->
        e.player.inventory.replace(BEDSHEET, ECTOBEDSHEET)
        e.player.inventory.replace(4286, 1925)
        e.player.inventory.refresh()
    }
}
