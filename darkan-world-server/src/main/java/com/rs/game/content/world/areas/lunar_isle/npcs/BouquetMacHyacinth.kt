package com.rs.game.content.world.areas.lunar_isle.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.lunar_isle.LunarIsle.SealOfPassageCheck
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun bouquetMacHyacinthInit() {
    onNpcClick(4526) { (player, npc) ->
        if (!SealOfPassageCheck(player, npc)) return@onNpcClick

        player.startConversation {
                player(CALM_TALK, "Hi! What are you up to?")
                npc(npc, HAPPY_TALKING, "Watering the pretty flowers, you want to help?")
                player(SHAKING_HEAD, "I don't have time to water flowers, I have people to save!")
                npc(npc, SHAKING_HEAD, "Pft, you should take time to enjoy the simple things.")
                player(SKEPTICAL_THINKING, "I'm not a simple person.")
                npc(npc, SKEPTICAL, "So it seems.")
            }
        }
    }
