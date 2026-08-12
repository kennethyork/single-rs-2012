package com.rs.game.content.world.areas.daemonheim.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.skills.dungeoneering.DamonheimController
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mapFremennikShipmaster() {
    onNpcClick(9707, 9708, 14847) { (player, npc, option) ->
        when (option) {
            "Talk-to" -> talkFremShipmaster(player, npc.id, npc.id == 9707)
            "Sail" -> sail(player, npc.id == 9707)
        }
    }
}

private fun talkFremShipmaster(player: Player, npcId: Int, backing: Boolean) {
    player.startConversation {
        npc(npcId, HeadE.CONFUSED, if (backing) "Do you want a lift back to the south?" else "You want passage to Daemonheim?")
        label("startOps")
        options {
            opExec("Yes, please.") { sail(player, backing) }
            op("Not right now, thanks.")
            if (npcId == 9708) {
                op("Daemonheim?") {
                    player(HeadE.CONFUSED, "Daemonheim?")
                    npc(npcId, HeadE.FRUSTRATED, "Yes, the icy peninsula far to the north of here.")
                    npc(npcId, HeadE.CALM_TALK, "Ice, snow, harsh winds...")
                    npc(npcId, HeadE.SAD_MILD, "...and no sand or swamp sludge, clogging up every orifice.")
                    npc(npcId, HeadE.SAD_MILD, "Are you done with questions? Can we go now?")
                    goto("startOps")
                }
                op("Why are you so grumpy?") {
                    player(HeadE.CONFUSED, "Why are you so grumpy?")
                    npc(npcId, HeadE.ANGRY, "Grumpy? I should kill you where you stand!")
                    npc(npcId, HeadE.SAD_MILD, "But that wouldn't help with this damned humidity.")
                    npc(npcId, HeadE.SAD_MILD, "I need the snow in my boots, the sea wind stinging my face...")
                    npc(npcId, HeadE.SAD_MILD, "That's why I want to leave. Are you ready to go to Daemonheim?")
                    goto("startOps")
                }
            }
        }
    }
}

fun sail(player: Player, backing: Boolean) {
    player.useStairs(-1, if (backing) Tile.of(3254, 3171, 0) else Tile.of(3511, 3692, 0), 2, 3)
    if (backing) player.controllerManager.forceStop()
    else player.controllerManager.startController(DamonheimController())
}