package com.rs.game.content.world.areas.nardah.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Kazemde(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Can I help you at all?")
            label("initialOps")
            options {
                op("Yes please. What are you selling?") {
                    player(CALM_TALK, "Yes please. What are you selling?")
                    exec { ShopsHandler.openShop(player, "nardah_general_store") }
                }
                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapKazemde() {
    onNpcClick(3039, options = arrayOf("Talk-to")) { (player, npc) -> Kazemde(player, npc) }
    onNpcClick(3039, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(
            player,
            "nardah_general_store"
        )
    }
}