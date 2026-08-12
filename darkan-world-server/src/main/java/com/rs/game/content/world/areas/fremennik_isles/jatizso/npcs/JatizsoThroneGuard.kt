package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.fremennik_isles.getFremennikName
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

@ServerStartupEvent
fun throneGuardInit() {
    onNpcClick(5491) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Welcome, ${getFremennikName(player)}.")
        }
    }
}
