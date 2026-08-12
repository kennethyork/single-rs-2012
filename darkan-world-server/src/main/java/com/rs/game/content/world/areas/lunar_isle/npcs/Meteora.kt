package com.rs.game.content.world.areas.lunar_isle.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.lunar_isle.LunarIsle.SealOfPassageCheck
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun meteoraInit() {
    onNpcClick(4515) { (player, npc) ->
        if (!SealOfPassageCheck(player, npc)) return@onNpcClick

        player.startConversation {
            player(CALM_TALK, "Take me to your leader.")
            npc(npc, CONFUSED, "Wot?")
            player(CONFUSED, "Isn't that what you're supposed to say when you meet a new race of people?")
            npc(npc, SHAKING_HEAD, "Well, mate, we don't av no leader, so I ain't gonna take ya to um. We auw av eqwuw right's un stuff, yeah?")
            npc(npc, CALM_TALK, "But if ya wont an intro go live it up wiv da Onerimancer.")
            player(CONFUSED, "OK. Is he rich and powerful?")
            npc(npc, SHAKING_HEAD, "No man, SHE, is dead-wise yeah? She is responsible for initiation wituals an stuff.")
            player(SKEPTICAL_THINKING, "Great. Thanks. And where is she?")
            npc(npc, SKEPTICAL_THINKING, "She angs out int souf east of ouw island.")
            player(CONFUSED, "Okey dokey....yeah.")
            npc(npc, CONFUSED, "Huh?")
        }
    }
}


