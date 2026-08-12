package com.rs.game.content.world.areas.yanille.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Frenita(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Would you like to buy some Cooking equipment?")
            label("initialOps")
            options {
                op("Yes, please.") {
                    player(CALM_TALK, "Yes, please.")
                    exec { ShopsHandler.openShop(player, "frenitas_fine_cookery_shop") }
                }
                op("No, thank you.") {
                    player(CALM_TALK, "No, thank you.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapFrenita() {
    onNpcClick(593, options = arrayOf("Talk-to")) { (player, npc) -> Frenita(player, npc) }
    onNpcClick(593, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "frenitas_fine_cookery_shop")
    }
}