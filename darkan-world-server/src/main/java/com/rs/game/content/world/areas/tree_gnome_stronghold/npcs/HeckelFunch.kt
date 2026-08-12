package com.rs.game.content.world.areas.tree_gnome_stronghold.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class HeckelFunch(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            player(CALM_TALK, "Hello there.")
            npc(npc, CALM_TALK, "Good day to you, my friend, and a beautiful one at that. Would you like some groceries? I have all sorts. Alcohol also, if you're partial to a drink.")
            label("initialOps")
            options {
                op("No thank you.") {
                    player(CALM_TALK, "No thank you.")
                    npc(npc, CALM_TALK, "Ahh well, all the best to you.")
                }
                op("I'll have a look.") {
                    player(CALM_TALK, "I'll have a look.")
                    npc(npc, CALM_TALK, "There's a good human.")
                    exec { ShopsHandler.openShop(player, "funchs_fine_groceries") }
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapHeckelFunch() {
    onNpcClick(603, options = arrayOf("Talk-to")) { (player, npc) -> HeckelFunch(player, npc) }
    onNpcClick(603, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "funchs_fine_groceries")
    }
}