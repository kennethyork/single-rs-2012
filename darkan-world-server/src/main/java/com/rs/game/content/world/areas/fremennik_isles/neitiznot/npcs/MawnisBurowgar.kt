package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

@ServerStartupEvent
fun mawnisBurowgarInit() {
    onNpcClick(5503) { (player, npc) ->
        player.startConversation {
            npc(npc, HAPPY_TALKING, "It makes me proud to know that the helm of my ancestors will be worn in battle.")
            npc(npc, HAPPY_TALKING, "I thank you on behalf of all my kinsmen Dallim Far-strider.")
            player(WORRIED, "Ah yes, about that beautiful helmet.")
            npc(npc, CONFUSED, "You mean the priceless heirloom that I gave to you as a sign of my trust and gratitude?")
            player(WORRIED, "Err yes, that one. I may have mislaid it.")
            npc(npc, CONFUSED, "It's a good job I have alert and loyal men who notice when something like this is left lying around and picks it up.")
            npc(npc, CONFUSED, "I'm afraid I'm going to have to charge you a 50,000GP handling cost.")
            options("Pay 50,000GP to recover your helmet?") {
                op("Yes, that would be fine.") {
                    nextIf({ player.inventory.hasCoins(50000) }) {
                        npc(npc, HAPPY_TALKING, "Please be more careful with it in the future.")
                        exec {
                            player.inventory.removeCoins(50000)
                            player.inventory.addItem(10828, 1, true)
                        }
                    }
                    nextIf({ !player.inventory.hasCoins(50000) }) {
                        npc(npc, HAPPY_TALKING, "You don't have enough gold right now.")
                    }
                }
                op("No, that's too much.") {
                    npc(npc, HAPPY_TALKING, "Okay. Come back later if you change your mind.")
                }
            }
        }
    }
}
