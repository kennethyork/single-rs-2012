package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun slugHemligssenInit() {
    onNpcClick(5520) { (player, npc) ->
        player.startConversation {
            npc(npc, SECRETIVE, "Shhh. Go away. I'm not allowed to talk to you.")
            player(CONFUSED, "Fine, whatever...")
        }
    }
}
