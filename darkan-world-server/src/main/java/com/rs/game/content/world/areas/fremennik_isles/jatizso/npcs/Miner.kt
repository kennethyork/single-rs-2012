package com.rs.game.content.world.areas.fremennik_isles.jatizso.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

@ServerStartupEvent
fun minerInit() {
    onNpcClick(5498) { (player, npc) ->
        when (Utils.random(6)) {
            0 -> MinerDialogue1(player, npc)
            1 -> MinerDialogue2(player, npc)
            2 -> MinerDialogue3(player, npc)
            3 -> MinerDialogue4(player, npc)
            4 -> MinerDialogue5(player, npc)
            5 -> MinerDialogue6(player, npc)
        }
    }
}

private fun MinerDialogue1(player: Player, npc: NPC) = player.startConversation {
    npc(npc, CALM_TALK, "Gah! Trolls everywhere. There's no escape!")
    player(CALM_TALK, "You could just leave the mine.")
    npc(npc, CALM_TALK, "Oh, I'd never thought of that.")
}

private fun MinerDialogue2(player: Player, npc: NPC) = player.startConversation {
    npc(npc, CALM_TALK, "This place rocks!")
    player(CALM_TALK, "No it doesn't, it stays perfectly still.")
    npc(npc, CALM_TALK, "Bah! Be quiet, pedant.")
}

private fun MinerDialogue3(player: Player, npc: NPC) = player.startConversation {
    npc(npc, CALM_TALK, "High hoe, high hoe!")
    player(CALM_TALK, "Why are you singing about farming implements at altitude?")
    npc(npc, CALM_TALK, "I don't know, I've never thought about it. Ask my Dad, he taught me the song.")
}

private fun MinerDialogue4(player: Player, npc: NPC) = player.startConversation {
    player(CALM_TALK, "How's your prospecting going?")
    npc(npc, CALM_TALK, "Mine's going pretty well.")
    player(CALM_TALK, "...")
    npc(npc, CALM_TALK, "Mine? Mine...you get it?")
    player(CALM_TALK, "Oh, I got it. That doesn't make it funny though.")
    npc(npc, CALM_TALK, "Suit yourself.")
}

private fun MinerDialogue5(player: Player, npc: NPC) = player.startConversation {
    npc(npc, CALM_TALK, "I've been here for two days straight. When I close my eyes I see rocks.")
    player(CALM_TALK, "Why would anyone stay here for so long?")
    npc(npc, CALM_TALK, "I fear the outside. I fear the big light.")
    player(CALM_TALK, "Oh dear. Being underground for so long may have driven you mad.")
}

private fun MinerDialogue6(player: Player, npc: NPC) = player.startConversation {
    npc(npc, CALM_TALK, "My back is killing me!")
    player(CALM_TALK, "Could be worse, it could be the trolls killing you.")
    npc(npc, CALM_TALK, "How very droll.")
    player(CALM_TALK, "No, troll.")
    npc(npc, CALM_TALK, "Bah! I resist your attempts at jollity.")
}

