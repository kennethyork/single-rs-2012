package com.rs.game.content.world.areas.ardougne.npcs.east_ardougne

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class SilverMerchant(val player: Player, val npc:NPC) {
    init {
        player.startConversation {
            npc(npc, HAPPY_TALKING, "Silver! Silver! Best prices for buying and selling in all Kandarin!")
            label("initialOps")
            options {
                op("Yes please.") {
                    player(CALM_TALK, "Yes please.")
                    exec { ShopsHandler.openShop(player, "ardougne_silver_stall") }
                }
                op("No, thank you.") {
                    player(CALM_TALK, "No, thank you.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapSilverMerchant() {
    onNpcClick(569, options = arrayOf("Talk-to")) { (player, npc) -> SilverMerchant(player, npc) }
    onNpcClick(569, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "ardougne_silver_stall")
    }
}