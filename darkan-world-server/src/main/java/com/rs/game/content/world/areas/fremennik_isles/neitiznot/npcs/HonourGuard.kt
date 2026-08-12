package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.fremennik_isles.getFremennikName
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

@ServerStartupEvent
fun honourGuardInit() {
    onNpcClick(5514) { (player, npc) -> HonourGuardDialogue(player, npc, "Location.TOWN") }
    onNpcClick(5515) { (player, npc) -> HonourGuardDialogue(player, npc, "Location.OUTSIDE") }
    onNpcClick(5516) { (player, npc) -> HonourGuardDialogue(player, npc, "Location.WALLS") }
}

fun HonourGuardDialogue(player: Player, npc: NPC, location: String) {
    if (player.equipment.getHatId() == 10836) {
        player.startConversation {
            when (location) {
                "Location.TOWN", "Location.OUTSIDE" -> {
                    npc(npc, CALM_TALK, "Good day, jester! Got any new jokes?")
                    player(CALM_TALK, "No. I only use old, recycled jokes. It's better for the environment.")
                    npc(npc, CALM_TALK, "Haha!")
                }
                "Location.WALLS" -> {
                    npc(npc, CALM_TALK, "Hey jester, this area is not a suitable place for civilians to wander about in. Trolls everywhere.")
                    player(CALM_TALK, "Right you are. I'll be careful.")
                }
            }
        }
    } else {
        player.startConversation {
            when (location) {
                "Location.TOWN" -> {
                    npc(npc, CALM_TALK, "Hail, ${getFremennikName(player)}!")
                    player(CALM_TALK, "Good day! How are you doing?")
                    npc(npc, CALM_TALK, "Pretty good. It's nice to have a day off from troll-killing duty.")
                    player(CALM_TALK, "Have you killed many trolls?")
                    npc(npc, CALM_TALK, "Of course. It's all part of growing up around here. You get used to it.")
                    player(CALM_TALK, "See you later.")
                }
                "Location.OUTSIDE" -> {
                    npc(npc, CALM_TALK, "Hail, ${getFremennikName(player)}!")
                    player(CALM_TALK, "Good day!")
                    npc(npc, CALM_TALK, "I hope you're not heading north. There are lots of trolls, very dangerous.")
                    player(CALM_TALK, "Thanks for the warning. I'll keep my eyes peeled.")
                }
                "Location.WALLS" -> {
                    npc(npc, CALM_TALK, "Hail, ${getFremennikName(player)}!")
                    player(CALM_TALK, "Good day!")
                    npc(npc, CALM_TALK, "Are you here to help with the trolls?")
                    player(CALM_TALK, "I'm afraid not, I have other things to do.")
                    npc(npc, CALM_TALK, "Well, watch yourself. Those rocks that the trolls throw can take your arm off.")
                }
            }
        }
    }
}

