package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.fremennik_isles.getFremennikName
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun fridleifShieldsonInit() {
    onNpcClick(5505) { (player, npc) ->
            player.startConversation {
               npc(npc, HAPPY_TALKING, "Congratulations, Champion ${getFremennikName(player)}. Thank you for defeating the Troll King.")
        }
    }
}
