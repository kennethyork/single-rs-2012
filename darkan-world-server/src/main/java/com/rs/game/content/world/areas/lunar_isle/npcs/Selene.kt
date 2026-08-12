package com.rs.game.content.world.areas.lunar_isle.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.lunar_isle.LunarIsle.SealOfPassageCheck
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun seleneInit() {
    onNpcClick(4517) { (player, npc) ->
        if (!SealOfPassageCheck(player, npc)) return@onNpcClick

        player.startConversation {
            player(CALM_TALK, "Can you tell me a bit about your people?")
            npc(npc, CALM_TALK, "Ok. Like what?")
            player(CALM_TALK, "How about the values of the Moon clan!")
            npc(npc, HAPPY_TALKING, "Let me see... We value knowledge of self because it is this that gives us our strength! It is most important!")
            player(SKEPTICAL_THINKING, "Well... I know things about myself. I know I like hot chocolate!")
            npc(npc, SKEPTICAL, "I was meaning something a little deeper than that. We also like to see someone listen. You know how they say a wise man listens?")
            player(NOTHING_LISTENING, "....")
            npc(npc, ANGRY, "Did you hear me?")
            player(NERVOUS, ".... I'm listening.")
            npc(npc, CHEERFUL, "Most wise.")
            player(CONFUSED, "Huh?")
        }
    }
}



