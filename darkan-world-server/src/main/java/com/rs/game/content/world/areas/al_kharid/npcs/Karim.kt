package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.events.NPCClickEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun handleKarim() {
    onNpcClick(543, options = arrayOf("Talk-to")){ e: NPCClickEvent ->
        val npcId = 543
        e.player.startConversation {
            npc(npcId, HeadE.CALM_TALK, "Would you like to buy a nice kebab? Only one gold.")
            options {
                op("I think I'll give it a miss.") {
                    player(HeadE.CALM_TALK, "I think I'll give it a miss.")
                }
                op("Yes please.") {
                    exec {
                        if (e.player.inventory.hasCoins(1)) {
                            e.player.inventory.removeCoins(1)
                            e.player.inventory.addItem(1971, 1)
                        } else {
                            player(HeadE.CALM_TALK, "Oops, I forgot to bring any money with me.")
                            npc(npcId, HeadE.SCARED, "Come back when you have some.")
                        }
                    }
                }
            }
        }
    }
}