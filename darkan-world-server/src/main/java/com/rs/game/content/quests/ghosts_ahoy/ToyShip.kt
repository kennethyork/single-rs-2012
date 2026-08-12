package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.dialogue.Options
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onItemOnItem

@ServerStartupEvent
fun mapToyShip() {
    onItemClick(4253, options = arrayOf("Repair")) { (player) ->
        if(player.inventory.containsItems(1733, 1734, 950) && (player.inventory.containsItems(946) || player.containsTool(946))) {
            player.inventory.deleteItem(950, 1)
            player.inventory.deleteItem(1734, 1)
            player.inventory.deleteItem(4253, 1)
            player.inventory.addItem(4254)
            player.itemDialogue(4254, "You replace the toy boat's missing flag.")
        }
        else
        {
            player.itemDialogue(4254,"You need some silk to replace the flag, something to sew it to the boat and something to cut the flag to the right size.")
        }
    }
    onItemClick(4254, options = arrayOf("Inspect")) { (player, item, option) ->
        val sailColour1Player = SailColorManager.getSailColor(player, "sailColour1Player")
        val sailColour2Player = SailColorManager.getSailColor(player, "sailColour2Player")
        val sailColour3Player = SailColorManager.getSailColor(player, "sailColour3Player")
        player.itemDialogue(4254, "The top of the flag is $sailColour1Player.<br>The skull emblem is $sailColour2Player.<br>The bottom of the flag is $sailColour3Player.")
    }
    onItemOnItem(GHOSTS_AHOY_SAIL_DYES.map { it.id }.toIntArray(), intArrayOf(4254)) { e ->
        val dye = GHOSTS_AHOY_SAIL_DYES.firstOrNull { it.id == e.item1.id || it.id == e.item2.id }
        if (dye != null) {
            e.player.sendOptionDialogue("What part of the flag would you like to dye ${dye.name}?") { ops: Options ->
                ops.add("The top of the flag.")
                {
                    SailColorManager.setSailColor(e.player, "sailColour1Player", dye.name)
                    e.player.inventory.deleteItem(dye.id, 1)
                    e.player.itemDialogue(4254,"You dye the top half of the flag ${dye.name}.")
                }
                ops.add("The skull emblem.")
                {
                    SailColorManager.setSailColor(e.player, "sailColour2Player", dye.name)
                    e.player.inventory.deleteItem(dye.id, 1)
                    e.player.itemDialogue(4254,"You dye the skull emblem ${dye.name}.")
                }
                ops.add("The bottom of the flag")
                {
                    SailColorManager.setSailColor(e.player, "sailColour3Player", dye.name)
                    e.player.inventory.deleteItem(dye.id, 1)
                    e.player.itemDialogue(4254,"You dye the bottom half of the flag ${dye.name}.")
                }
                ops.add("Nevermind")
            }
        } else {
            e.player.sendMessage("This item cannot be used here.")
        }
    }
}