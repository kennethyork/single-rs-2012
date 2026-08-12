package com.rs.game.content.world.areas.canifis.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Barker(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            player(CALM_TALK, "Hello.")
            npc(npc, CALM_TALK, "You are looking for clothes, yes? You look at my products! I have very many nice clothes, yes?")
            label("initialOps")
            options {
                op("Yes, please.") {
                    player(CALM_TALK, "Yes, please.")
                    exec { ShopsHandler.openShop(player, "barkers_haberdashery") }
                }
                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                    npc(npc, CALM_TALK, "Unfortunate for you, yes? Many bargains, won't find elsewhere!")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBarker() {
    onNpcClick(1039, options = arrayOf("Talk-to")) { (player, npc) -> Barker(player, npc) }
    onNpcClick(1039, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "barkers_haberdashery")
    }
}