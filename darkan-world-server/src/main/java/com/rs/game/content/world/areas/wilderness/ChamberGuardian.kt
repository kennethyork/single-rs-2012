package com.rs.game.content.world.areas.wilderness

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class ChamberGuardian(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            player(CALM_TALK, "Hello again.")
            npc(npc, CALM_TALK, "Hello adventurer, are you looking for another staff?")
            label("initialOps")
            options {
                op("What do you have to offer?") {
                    player(CALM_TALK, "What do you have to offer?")
                    exec { ShopsHandler.openShop(player, "chamber_guardian_shop") }
                }
                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                    npc(npc, CALM_TALK, "Well let me know if you need one.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapChamberGuardian() {
    onNpcClick(904, options = arrayOf("Talk-to")) { (player, npc) -> ChamberGuardian(player, npc) }
    onNpcClick(904, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "chamber_guardian_shop")
    }
}