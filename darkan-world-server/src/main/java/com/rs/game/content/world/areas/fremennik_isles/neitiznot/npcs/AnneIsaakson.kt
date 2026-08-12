package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun anneIsaaksonInit() {
    onNpcClick(5512) { (player, npc) ->
        player.startConversation {
            npc(npc, CHEERFUL, "Hello visitor, how are you?")
            player(CALM_TALK, "Better than expected. It's a lot...nicer...here than I was expecting. Everyone seems pretty happy.")
            npc(npc, HAPPY_TALKING, "Of course, the Burgher is strong and wise, and looks after us well.")
            player(CALM_TALK, "I think some of those Jatizso citizens have got the wrong idea about this place.")
        }
    }
}
