package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun JatizsoGatekeepersInit() {
    onNpcClick(5492) { (player, npc) ->
        player.startConversation {
            npc(5492, CALM_TALK, "Are you all right? Leftie?")
            npc(5492, CALM_TALK, "No, I'm on the left.")
            npc(5492, CALM_TALK, "Only from your perspective. Someone entering the gate should call you Rightie, right Leftie?")
            npc(5492, CALM_TALK, "That's right Leftie, that's right.")
            npc(5492, CALM_TALK, "No, Rightie's fine Leftie.")
            player(ANGRY, "Aaagh! Enough! If either of you mention left or right in my presence I'll have to scream! Can I come through the gate?")
            npc(5492, CALM_TALK, "Don't let us stop you.")
            npc(5492, CALM_TALK, "Yes, head right on in, sir.")
            player(ANGRY, "You said it! You said it! ARRRRRRRRGGH!")
        }
    }
}


