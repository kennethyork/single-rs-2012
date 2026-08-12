package com.rs.game.content.world.areas.ranging_guild.npc

import com.rs.engine.dialogue.HeadE.CALM_TALK
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun mapDargaudsBowAndArrowsDialogue() {
    onNpcClick(683, options = arrayOf("Talk-to")) { e ->
        e.npc.resetDirection()
        e.player.startConversation {
            player(CALM_TALK, "Hello.")
            npc(e.npcId, CALM_TALK, "A fair day, traveller. Would you like to see my wares?")
            options {
                op("Yes please.") {
                    player(CALM_TALK, "Yes please.")
                    exec { ShopsHandler.openShop(e.player, "dargauds_bow_and_arrows") }
                }

                op("I'd like to ask you about magic crossbows.") {
                    player(CALM_TALK, "I'd like to ask you about magic crossbows.")
                    npc(e.npcId, CALM_TALK, "Ahh crossbows. Not exactly what I'd call skilled ranging, but I guess dwarven engineering is good, what did you want to know?")
                    player(CALM_TALK, "The dwarves don't work with magic logs, I'm wondering if you know how to make a crossbow out of them?")
                    npc(e.npcId, CALM_TALK, "As it happens, a recent discovery of dragon limbs has allowed the creation of such a crossbow.")
                    player(CALM_TALK, "That's fantastic!")
                }

                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                    npc(e.npcId, CALM_TALK, "Okay good day to you.")
                }
            }
        }
    }
    onNpcClick(683, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "dargauds_bow_and_arrows")
    }
}
