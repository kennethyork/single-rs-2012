package com.rs.game.content.world.areas.karamja.npcs
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc


@ServerStartupEvent

    fun mapAntiques() {
    val YANNI_ID = 515 // NPC ID

    val antiques = mapOf(
        605 to 100,   // BONE_KEY
        606 to 100,   // STONE_PLAQUE
        607 to 100,   // TATTERED_SCROLL
        608 to 100,   // CRUMPLED_SCROLL
        624 to 100,   // BERVIRIUS_NOTES
        611 to 500,   // LOCATING_CRYSTAL
        616 to 1000   // BEADS_OF_THE_DEAD
    )

    onItemOnNpc(YANNI_ID) { (player, item, npc) ->
        antiques[item.id]?.let { value ->
            player.startConversation {
                npc(npc,HeadE.CALM_TALK,"Would you sell me the ${item.name} for $value coins?")
                options("Sell the ${item.name}?"){
                    op("Yes, please."){
                        item(item.id, "You sold the ${item.name} for $value coins.")
                        exec{
                            player.inventory.removeItems(Item(item.id, 1))
                            player.inventory.addCoins(value)
                        }
                    }
                    op("No, thanks."){
                        simple("You decide to keep the ${item.name} for now.")
                    }
                }
            }
        }
    }
}