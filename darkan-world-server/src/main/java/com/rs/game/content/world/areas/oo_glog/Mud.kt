package com.rs.game.content.world.areas.oo_glog;

import com.rs.game.model.entity.player.Equipment;
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onItemEquip

@ServerStartupEvent
fun mapMudMask() {
    onItemClick(12558, options = arrayOf("Apply")) { e ->
        Equipment.sendWear(e.player, e.item.slot, e.item.id, true)
    }
    onItemEquip(12558) { e ->
        if(e.dequip()) {
            e.player.sendMessage("The mud mask crumbles as you rub it off your face.")
            e.player.tasks.schedule(1) {
                e.player.inventory.deleteItem(e.item)
            }
        }
    }
}
