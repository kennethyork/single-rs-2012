package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun kuliMyrkaInit() {
    onNpcClick(5486) { (player, npc) ->
        player.startConversation {
            npc(npc, CHEERFUL, "Oh, hello again. Want to buy some weapons?")
            options("Select an Option") {
                op("I'll have a look.") {
                    exec { ShopsHandler.openShop(player, "weapons_galore") }
                }
                op("Not right now...") {
                    player(CALM_TALK, "Not right now.")
                }
            }
        }
    }
}
