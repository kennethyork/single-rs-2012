package com.rs.game.content.world.areas.taverley.npcs

import com.rs.engine.dialogue.HeadE.HAPPY_TALKING
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.skills.crafting.tanningD
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

fun JackOvalDialogue(player: Player, npc: NPC) {
        player.startConversation {
            npc(npc, HAPPY_TALKING, "You need to make things. Leather. Pottery. Every piece you make will increase your skill.")
            options {
                op("I need crafting supplies.") {
                    exec { ShopsHandler.openShop(player, "jack_crafting_shop") }
                }
                op("I need you to tan some leather for me.") {
                    tanningD(player, npc)
                }
                op("Farewell.") {
                    player(HAPPY_TALKING, "Farewell.")
                }
            }
    }
}

@ServerStartupEvent
fun mapJackOval() {
    onNpcClick(14877, options = arrayOf("Talk-to")) { (player, npc) ->
        JackOvalDialogue(player, npc)
    }
    onNpcClick(14877, options = arrayOf("Trade")) { (player, _) ->
        ShopsHandler.openShop(player, "jack_crafting_shop")
    }
}


