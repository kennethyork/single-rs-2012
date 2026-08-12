package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mortenHoldstromInit() {
    onNpcClick(5510) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Good day to you.")
            player(CALM_TALK, "Hello. What are you up to?")
            npc(npc, CALM_TALK, "Ah, today is a surströmming day! The herring I buried six months ago is ready to be dug up.")
            player(CALM_TALK, "Eughr! What are you going to do with it?")
            npc(npc, CALM_TALK, "Eat it, of course! It will be fermented just-right by now.")
            player(CALM_TALK, "Fermented? You eat rotten fish?")
            npc(npc, CALM_TALK, "Hmmm, tasty. I'm guessing you don't want to come round and try it?")
            player(CALM_TALK, "You guess correctly.")
        }
    }
}
