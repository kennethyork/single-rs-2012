package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.fremennik_isles.getFremennikName
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun kjedeligUppsenInit() {
    onNpcClick(5518) { (player, npc) ->
        player.startConversation {
            npc(npc, HAPPY_TALKING, "Ho, ${getFremennikName(player)}! The brave warrior returns!")
            player(CALM_TALK, "It was a hard-fought battle, but we Fremennik prevailed!")
            npc(npc, HAPPY_TALKING, "I expect we'll be hearing songs about the 'Slayer of the Troll King' for many years to come.")
            player(CALM_TALK, "It would be an honour to be remembered in such a way! Thanks.")
        }
    }
}
