package com.rs.game.content.world.areas.catherby.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Harry(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Welcome! You can buy Fishing equipment at my store. We'll also give you a good price for any fish that you catch.")
            label("initialOps")
            options {
                op("Let's see what you've got, then.") {
                    player(CALM_TALK, "Let's see what you've got, then.")
                    exec { ShopsHandler.openShop(player, "harrys_fishing_shop(qol_feathers_added)") }
                }
                op("Sorry, I'm not interested.") {
                    player(CALM_TALK, "Sorry, I'm not interested.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapHarry() {
    onNpcClick(576, options = arrayOf("Talk-to")) { (player, npc) -> Harry(player, npc) }
    onNpcClick(576, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "harrys_fishing_shop(qol_feathers_added)")
    }
}