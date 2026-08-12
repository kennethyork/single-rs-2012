package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation

private fun louie(player: Player) {
    val npcId = 542
    player.startConversation {
        npc(npcId, HeadE.CALM_TALK, "Hey, wanna buy some armour?")
        options {
            op("What have you got?") {
                player(HeadE.CALM_TALK, "What have you got?")
                npc(npcId, HeadE.HAPPY_TALKING, "I provide items to help you keep your legs!")
                exec { ShopsHandler.openShop(player, "louies_armoured_legs_bazaar") }
            }
            op("No, thank you.") {
                player(HeadE.CALM_TALK, "No, thank you.")
            }
            op("What do you think of Ali Morrisane?") {
                player(HeadE.CALM_TALK, "What do you think of Ali Morrisane?")
                npc(npcId, HeadE.SKEPTICAL, "I don't like these men he's brought in from the southern desert. They're up to no good, I'm sure.")
                npc(npcId, HeadE.ANGRY, "One of them even came round here and threatened me with a hammer! He said that it'd be better for us if we worked for Ali Morrisane and that it was our fault if we didn't and... bad things started to happen.")
                player(HeadE.SKEPTICAL, "What are you going to do about it?")
                npc(npcId, HeadE.CHEERFUL, "Keep selling armour, of course! Would you like to buy some?")
                options {
                    op("What have you got?") {
                        exec { ShopsHandler.openShop(player, "louies_armoured_legs_bazaar") }
                    }
                    op("No, thank you.") {
                        player(HeadE.CALM_TALK, "No, thank you.")
                    }
                }
            }
        }
    }
}

private fun louieFlyer(player: Player) {
    val npcId = 542
    player.startConversation {
        player(HeadE.CALM_TALK, "Hi. I have this money-off voucher.")
        npc(npcId, HeadE.SAD, "So I see. Unfortunately, it seems to have expired yesterday. Never mind.")
        player(HeadE.ANGRY, "But I only just got it!")
        npc(npcId, HeadE.CALM_TALK, "I'm sorry. There's nothing I can do. Goodbye.")
    }
}

@ServerStartupEvent
fun handleLouie() {
    onNpcClick(542) { e ->
        if (e.option == "Talk-to") {
            louie(e.player)
        }
    }
    onItemOnNpc(542) { e ->
        if (e.item.id != 7922) {
            e.player.startConversation {
                player(HeadE.SHAKING_HEAD, "I have no use for this.")
            }
        } else {
            louieFlyer(e.player)
        }
    }
}
