package com.rs.game.content.items

import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onItemEquip
import com.rs.plugin.kts.onItemOnItem

const val GLOVES_OF_SILENCE = 10075
const val MAX_GLOVES_OF_SILENCE_CHARGES = 62
const val NEEDLE = 1733
const val THREAD = 1734
const val KNIFE = 946
const val DARK_KEBBIT_FUR = 10115

@ServerStartupEvent
fun mapGlovesOfSilence() {

    // Check charges
    onItemClick(GLOVES_OF_SILENCE, options = arrayOf("Check")) { e ->
        val charges = e.player.getI("glovesOfSilenceCharges").takeIf { it > 0 } ?: MAX_GLOVES_OF_SILENCE_CHARGES
        e.player.set("glovesOfSilenceCharges", charges)
        val message = when (charges) {
            62 -> "Your gloves are new."
            in 34..61 -> "Your gloves are in good condition."
            in 20..34 -> "Your gloves are starting to look quite shabby."
            in 10..19 -> "Your gloves are starting to need repair."
            in 2..9 -> "Your gloves are in need of repair!"
            1 -> "<col=FF0000>Your gloves are about to fall apart!</col>"
            else -> "<col=FF0000>Your gloves have fallen apart!</col>"
        }
        e.player.sendMessage(message)
    }

    // Prevent equipping if less than lvl 54 Hunter
    onItemEquip(GLOVES_OF_SILENCE) { e ->
        if (e.equip())
            if (e.player.skills.getLevelForXp(Constants.HUNTER) < 54) {
                e.player.sendMessage("You need a Hunter level of at least 54 to wear these gloves.")
                e.cancel()
            }
    }

    // Repair gloves
    onItemOnItem(intArrayOf(GLOVES_OF_SILENCE), intArrayOf(DARK_KEBBIT_FUR)) { e ->
        val player = e.player

        if (player.getI("glovesOfSilenceCharges") < MAX_GLOVES_OF_SILENCE_CHARGES) {
            if (player.skills.getLevelForXp(Constants.CRAFTING) < 64) {
                player.sendMessage("You need a Crafting level of at least 64 to repair the gloves.")
                return@onItemOnItem
            }
            val hasNeedle = player.inventory.containsItem(NEEDLE) || player.containsTool(NEEDLE)
            val hasKnife = player.inventory.containsItem(KNIFE) || player.containsTool(KNIFE)

            if (hasNeedle && hasKnife && player.inventory.containsItem(THREAD)) {
                player.inventory.deleteItem(THREAD, 1)
                player.inventory.deleteItem(DARK_KEBBIT_FUR, 1)
                player.set("glovesOfSilenceCharges", MAX_GLOVES_OF_SILENCE_CHARGES)
                player.sendMessage("You use up a reel of thread whilst repairing the gloves.")
            } else {
                player.sendMessage("You need a needle, some thread, and a knife in order to repair the gloves.")
            }
        } else {
            player.sendMessage("Your gloves aren't in need of repair just yet.")
        }
    }

}

/*
 * Degrade gloves method.
 * If charges = 0, delete item and let player know.
 */
fun degradeGlovesOfSilence(player: Player) {
    if (player.equipment.glovesId == GLOVES_OF_SILENCE) {
        var charges = player.getI("glovesOfSilenceCharges").takeIf { it > 0 } ?: MAX_GLOVES_OF_SILENCE_CHARGES
        charges -= 1

        if (charges <= 0) {
            player.set("glovesOfSilenceCharges", MAX_GLOVES_OF_SILENCE_CHARGES)
            player.equipment.deleteSlot(Equipment.HANDS)
            player.sendMessage("<col=FF0000>Your gloves have fallen apart!</col>")
        } else {
            player.set("glovesOfSilenceCharges", charges)
            if (charges == 1) {
                player.sendMessage("<col=FF0000>Your gloves are about to fall apart!</col>")
            }
        }
    }
}
