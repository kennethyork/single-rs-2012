package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun flosiDalkssonInit() {
    onNpcClick(5484) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Oh, hello again. Want some fish?")
            options("Select an Option") {
                op("Yes, show me the fish.") {
                    exec {
                        ShopsHandler.openShop(player, "flosis_fishmongers")
                    }
                }
                op("Not right now.")
            }
        }
    }
}
