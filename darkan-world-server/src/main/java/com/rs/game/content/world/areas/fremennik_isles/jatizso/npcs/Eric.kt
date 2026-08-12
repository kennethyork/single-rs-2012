package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun ericInit() {
    onNpcClick(5499) { e ->
        e.player.startConversation {
            npc(e.npc, CALM_TALK, "Spare us a few coppers, ${e.player.genderTerm("mister", "miss")}.")
            player(CALM_TALK, "No, go away!")
        }
    }
}
