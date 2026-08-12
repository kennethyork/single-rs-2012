package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun sassilikInit() {
    onNpcClick(5496) { (player, npc) ->
        when (Utils.random(4)) {
            0 -> SassilikDialogue1(player, npc)
            1 -> SassilikDialogue2(player, npc)
            2 -> SassilikDialogue3(player, npc)
            3 -> SassilikDialogue4(player, npc)
        }
    }
}

private fun SassilikDialogue1(player: Player, npc: NPC) = player.startConversation {
    player(CALM_TALK, "How are you today?")
    npc(npc, SAD, "**sigh**")
    player(CALM_TALK, "That good? Everyone around here seems a little depressed.")
    npc(npc, SAD, "**sigh**")
    player(CALM_TALK, "And not particularly talkative.")
    npc(npc, SAD, "**sigh**")
    player(CALM_TALK, "I'll leave you to your sighing. It looks like you have plenty to do.")
}

private fun SassilikDialogue2(player: Player, npc: NPC) = player.startConversation {
    player(CALM_TALK, "It's a bit grey round here, isn't it?")
    npc(npc, CALM_TALK, "It gets you down after a while, you know. There are 273 shades of grey, you know, and we have them all.")
    player(CALM_TALK, "That's grey-t.")
    npc(npc, SAD, "That attempt at humour merely made me more depressed. Leave me alone.")
}

private fun SassilikDialogue3(player: Player, npc: NPC) = player.startConversation {
    player(CALM_TALK, "How's the King treating you then?")
    npc(npc, CALM_TALK, "Like serfs.")
    player(CALM_TALK, "Serf?")
    npc(npc, CALM_TALK, "Yes, you know - peons, plebs, the downtrodden. He treats us like his own personal possessions.")
    player(CALM_TALK, "You should leave this place.")
    npc(npc, CALM_TALK, "I keep trying to save up enough to leave, but the King keeps taxing us! We have no money left.")
    player(CALM_TALK, "Oh dear.")
}

private fun SassilikDialogue4(player: Player, npc: NPC) = player.startConversation {
    player(CALM_TALK, "Cheer up! It's not the end of the world.")
    npc(npc, CALM_TALK, "I'd prefer that, if it meant I didn't have to talk to people as inanely happy as you.")
    player(CALM_TALK, "Whoa! I think you need to get out more.")
}