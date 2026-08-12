package com.rs.game.content.world.areas.desert.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class BanditShopkeeper(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            player(CALM_TALK, "Hello.")
            npc(npc, CALM_TALK, "Stuff for sale. You buying?")
            label("initialOps")
            options {
                op("Yes.") {
                    player(CALM_TALK, "Yes.")
                    exec { ShopsHandler.openShop(player, "bandit_bargains") }
                }
                op("No.") {
                    player(CALM_TALK, "No.")
                    npc(npc, CALM_TALK, "No? 'Bye then.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBanditShopkeeper() {
    onNpcClick(1917, options = arrayOf("Talk-to")) { (player, npc) -> BanditShopkeeper(player, npc) }
    onNpcClick(1917, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "bandit_bargains")
    }
}