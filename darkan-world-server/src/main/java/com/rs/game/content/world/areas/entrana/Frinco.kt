package com.rs.game.content.world.areas.entrana

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Frinco(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Hello, how can I help you?")
            label("initialOps")
            options {
                op("What are you selling?") {
                    player(CALM_TALK, "What are you selling?")
                    exec { ShopsHandler.openShop(player, "frincos_fabulous_herb_shop") }
                }
                op("You can't. I'm beyond help.") {
                    player(CALM_TALK, "You can't. I'm beyond help.")
                }
                op("I'm okay, thank you.") {
                    player(CALM_TALK, "I'm okay, thank you.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapFrinco() {
    onNpcClick(578, options = arrayOf("Talk-to")) { (player, npc) -> Frinco(player, npc) }
    onNpcClick(578, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "frincos_fabulous_herb_shop")
    }
}