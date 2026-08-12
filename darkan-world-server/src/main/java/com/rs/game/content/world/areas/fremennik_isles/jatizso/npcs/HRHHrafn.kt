package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.cache.loaders.ItemDefinitions
import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun hrafnInit() {
    onNpcClick(5479) { (player, npc) ->
        if (ItemDefinitions.getDefs(player.equipment.neckId).name.contains("Catspeak")) {
            player.startConversation {
                player(CALM_TALK, "Hello cat.")
                npc(npc, CAT_SHOUTING, "One does not wish to be disturbed.")
                player(CONFUSED, "What?")
            }
        } else {
            player.startConversation {
                player(CALM_TALK, "Hello cat.")
                npc(npc, CAT_CALM_TALK, "Meow!")
            }
        }
    }
}
