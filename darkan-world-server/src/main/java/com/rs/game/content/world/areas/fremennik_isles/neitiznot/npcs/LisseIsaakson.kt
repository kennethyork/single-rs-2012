package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun lisseIsaaksonInit() {
    onNpcClick(5513) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Hello, visitor!")
            player(CALM_TALK, "Hello. What are you up to?")
            npc(npc, CALM_TALK, "Ah, I was about to collect some yak's milk to make yak cheese.")
            player(CALM_TALK, "Eughr! Though I am curious. Can I try some?")
            npc(npc, CALM_TALK, "Sorry, no. The last outlander who ate my cheese was ill for a month.")
            player(CALM_TALK, "So why don't you get ill as well?")
            npc(npc, CALM_TALK, "Well, we eat yak milk products every day, from when we're born. So I suppose we're used to it. Anyway I should stop yakking - haha - and get on with my work.")
            player(CALM_TALK, "I'm glad to see that puns are common everywhere in Gielinor; even here.")
        }
    }
}
