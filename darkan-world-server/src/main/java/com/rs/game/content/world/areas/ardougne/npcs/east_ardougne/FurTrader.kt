package com.rs.game.content.world.areas.ardougne.npcs.east_ardougne

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class FurTrader(val player: Player, val npc:NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Would you like to trade in fur?")
            label("initialOps")
            options {
                op("Yes.") {
                    player(CALM_TALK, "Yes.")
                    exec { ShopsHandler.openShop(player, "ardougne_fur_stall") }
                }
                op("No.") {
                    player(CALM_TALK, "No.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapFurTrader() {
    onNpcClick(573, options = arrayOf("Talk-to")) { (player, npc) -> FurTrader(player, npc) }
    onNpcClick(573, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "ardougne_fur_stall")
    }
}