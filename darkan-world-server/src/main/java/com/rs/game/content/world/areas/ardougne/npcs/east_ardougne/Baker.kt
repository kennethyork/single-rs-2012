package com.rs.game.content.world.areas.ardougne.npcs.east_ardougne

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Baker(val player: Player, npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Good day, monsieur. Would you like ze nice freshly-baked bread? Or perhaps a nice piece of cake?")
            label("initialOps")
            options {
                op("Let's see what you have.") {
                    player(CALM_TALK, "Let's see what you have.")
                    exec { ShopsHandler.openShop(player, "ardougne_bakers_stall") }
                }
                op("No, thank you.") {
                    player(CALM_TALK, "No, thank you.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBaker() {
    onNpcClick(571, options = arrayOf("Talk-to")) { (player, npc) -> Baker(player, npc) }
    onNpcClick(571, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "ardougne_bakers_stall")
    }
}