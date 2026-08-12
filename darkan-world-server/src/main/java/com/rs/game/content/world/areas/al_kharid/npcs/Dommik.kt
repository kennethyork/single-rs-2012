package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Dommik(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Would you like to buy some crafting equipment?")
            label("initialOps")
            options {
                op("No, thanks, I've got all the crafting equipment I need.") {
                    player(CALM_TALK, "No, thanks, I've got all the crafting equipment I need.")
                    npc(npc, CALM_TALK, "Okay. Fare well on your travels.")
                }
                op("Let's see what you've got, then.") {
                    player(CALM_TALK, "Let's see what you've got, then.")
                    exec { ShopsHandler.openShop(player, "dommiks_crafting_shop") }
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapDommik() {
    onNpcClick(545, options = arrayOf("Talk-to")) { (player, npc) -> Dommik(player, npc) }
    onNpcClick(545, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "dommiks_crafting_shop")
    }
}