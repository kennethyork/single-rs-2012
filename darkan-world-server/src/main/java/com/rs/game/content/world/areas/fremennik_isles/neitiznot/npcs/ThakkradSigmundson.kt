package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

fun thakkradDialogue(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, HAPPY_TALKING, "Thank you for leading the Burgher's militia against the Troll King.")
        npc(npc, HAPPY_TALKING, "Now that the trolls are leaderless I have repaired the bridge to the central isle for you as best I can.")
        player(HAPPY_TALKING, "Thanks Thakkrad. Does that mean I have access to the runite ores on that island?")
        npc(npc, HAPPY_TALKING, "Yes, you should be able to mine runite there if you wish.")
    }
}

@ServerStartupEvent
fun mapThakkradSigmundson() {
    onNpcClick(5506, options = arrayOf("Talk-to")) { (player, npc) ->
        thakkradDialogue(player, npc)
    }
}