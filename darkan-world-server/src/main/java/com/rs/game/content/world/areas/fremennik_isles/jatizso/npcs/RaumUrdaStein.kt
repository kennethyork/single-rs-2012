package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun raumUrdaSteinInit() {
    onNpcClick(5485) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Oh, hello again. Want to buy some armour?")
            options("Select an Option") {
                op("I'll have a look.") {
                    exec { ShopsHandler.openShop(player, "armour_shop") }
                }
                op("Not right now.") {
                }
            }
        }
    }
}
