package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun gruvaPatrullInit() {
    onNpcClick(5500) { (player, npc) ->
        player.startConversation {
            npc(npc, CHEERFUL, "Ho! Outlander.")
            player(CALM_TALK, "What's down the scary-looking staircase?")
            npc(npc, CHEERFUL, "These are the stairs down to the mining caves. There are rich veins of many types down there, and miners too. Though be careful; some of the trolls occasionally sneak into the far end of the cave.")
            player(CALM_TALK, "Thanks. I'll look out for them.")
        }
    }
}
