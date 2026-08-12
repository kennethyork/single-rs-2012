package com.rs.game.content.world.areas.ranging_guild.npc

import com.rs.engine.dialogue.HeadE.CALM_TALK
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun mapAuthenticThrowingWeaponsDialogue() {
    onNpcClick(692, options = arrayOf("Talk-to")) { e ->
        e.npc.resetDirection()
        e.player.startConversation {
            player(CALM_TALK, "Hello there.")
            npc(e.npcId, CALM_TALK, "Greetings, traveller. Are you interested in any throwing weapons?")
            options {
                op("Yes I am.") {
                    player(CALM_TALK, "Yes I am.")
                    npc(e.npcId, CALM_TALK, "That is a good thing.")
                    exec { ShopsHandler.openShop(e.player, "authentic_throwing_weapons") }
                }

                op("Not really.") {
                    player(CALM_TALK, "Not really.")
                    npc(e.npcId, CALM_TALK, "No bother to me.")
                }
            }
        }
    }
    onNpcClick(692, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "authentic_throwing_weapons")
    }
}