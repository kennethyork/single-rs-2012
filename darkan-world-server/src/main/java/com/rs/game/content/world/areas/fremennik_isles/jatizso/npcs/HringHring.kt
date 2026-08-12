package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun hringHringInit() {
    onNpcClick(5483) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Oh, hello again. Want some ore?")
            options("Select an Option") {
                op("I'll have a look.") {
                    exec { ShopsHandler.openShop(player, "ore_store") }
                }
                op("Not right now.")
            }
        }
    }
}
