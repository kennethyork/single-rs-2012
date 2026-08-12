package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

private fun faruq(player: Player) {
    val npcId = 9159
    player.startConversation {
        npc(npcId, HeadE.AMAZED, "Hello! Have you come to sample my marvellous wares?")
        options {
            op("Yes, I'd like to see what you have.") {
                exec { ShopsHandler.openShop(player, "faruqs_tools_for_games") }
            }
            op("Perhaps. Your stall has some odd-looking stuff; what are they for?") {
                player(HeadE.CALM_TALK, "Perhaps. Your stall has some odd-looking stuff; what are they for?")
                npc(npcId, HeadE.HAPPY_TALKING, "I sell them the tools to keep track of time, mark out places and routes, decide things randomly, even to hold great ballots of their group.")
                options {
                    op("Let me see, then.") {
                        exec { ShopsHandler.openShop(player, "faruqs_tools_for_games") }
                    }
                    op("These tools, are they complicated?") {
                        player(HeadE.CALM_TALK, "These tools, are they complicated?")
                        npc(
                            npcId,
                            HeadE.CALM_TALK,
                            "No, " + player.displayName + ", they are not complicated."
                        )
                        npc(npcId, HeadE.CALM_TALK, "I have a book that explains them, should you need.")
                        exec { ShopsHandler.openShop(player, "faruqs_tools_for_games") }
                    }
                    op(
                        "I don't think this is for me.") {
                        player(HeadE.CALM_TALK, "I don't think this is for me.")
                        npc(npcId, HeadE.CALM_TALK, "That is a shame. I shall be here if you change your mind.")
                    }
                }
            }
            op("No, thanks.") {
                player(HeadE.CALM_TALK, "No, thanks.")
            }
        }
    }
}

@ServerStartupEvent
fun handleFaruq() {
    onNpcClick(9159) { e ->
        when (e.option) {
            "Talk-to" -> faruq(e.player)
            "Trade" -> ShopsHandler.openShop(e.player, "faruqs_tools_for_games")
        }
    }
}