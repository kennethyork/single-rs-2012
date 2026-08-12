package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun thorkelInit() {
    onNpcClick(5480) { (player, npc) ->
        player.startConversation {
            npc(5480, CALM_TALK, "I hear you killed the Troll King. Thanks you for that, however I'm afraid I can't really talk to you any more, the King would be unhappy!")
            player(CALM_TALK, "Good luck with dealing with the King. You'll need it!")
        }
    }
}

