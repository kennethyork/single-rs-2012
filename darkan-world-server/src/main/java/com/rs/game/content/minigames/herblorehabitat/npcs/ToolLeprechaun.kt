package com.rs.game.content.minigames.herblorehabitat.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.herblorehabitat.openPotionStorage
import com.rs.game.content.minigames.herblorehabitat.openToolStorage
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun toolLeprechaunHerbloreHabitat() {
    onNpcClick(3121) { e ->
        when (e.option) {
            "Exchange", "Exchange-tools" -> openToolStorage(e.player)
            "Exchange-potions" -> openPotionStorage(e.player)
            "Talk-to" -> e.player.startConversation {
                npc(e.npcId, HeadE.CHEERFUL, "Ah, 'tis a foine day, to be sure! Can I help ye with tool storage, or potion storage, or what?")
                options {
                    op("Tool storage.") {
                        npc(e.npcId, HeadE.CHEERFUL, "We'll hold onto yer rake, seed dibber, spade, secateurs, waterin' can and trowel - but mind it's not one of them fancy trowels only archaeologists use.")
                        npc(e.npcId, HeadE.CHEERFUL, "We'll take a few buckets an' scarecrows off yer hands too, and even yer compost and supercompost. There's room in our shed for plenty of compost, so bring it on.")
                        npc(e.npcId, HeadE.CHEERFUL, "Also, if ye hands us yer Farming produce, we might be able to change it into banknotes.")
                        npc(e.npcId, HeadE.CONFUSED, "So, do ye want to be using the store?")
                        options {
                            opExec("Yes, please.") { openToolStorage(e.player) }
                            op("Nevermind.")
                        }
                    }
                    op("Potion storage.") {
                        npc(e.npcId, HeadE.CHEERFUL, "We'll hold onto yer potions and vines while yer here.")
                        npc(e.npcId, HeadE.CONFUSED, "So, do ye want to be using the store?")
                        options {
                            opExec("Yes, please.") { openPotionStorage(e.player) }
                            op("Nevermind.")
                        }
                    }
                }
            }
        }
    }
}