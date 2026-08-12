package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

//TODO Rouge Trader miniquest and add shops

@ServerStartupEvent
fun aliMorrisan() {
    onNpcClick(1862, options = arrayOf("Talk-to")) { e ->
        val npcId = 1862
        e.player.startConversation {
            npc(npcId, HeadE.SAD_CRYING, "My friend!")
            npc(npcId, HeadE.SAD_CRYING, "Can you help me?")
            options {
                op("Sure, how can I help?") {
                    player(HeadE.HAPPY_TALKING, "Sure, how can I help?")
                    npc(npcId, HeadE.SAD_CRYING, "I have no more stock left!")
                    npc(npcId, HeadE.SAD_CRYING, "If you can find me a supplier of Blackjacks, Clothes, and Runes I will give you a discount!")
                    player(HeadE.HAPPY_TALKING, "I'll see what I can do.")
                }
                op("Not right now.") {
                    player(HeadE.CALM_TALK, "Not right now.")
                }
            }
        }
    }
}
