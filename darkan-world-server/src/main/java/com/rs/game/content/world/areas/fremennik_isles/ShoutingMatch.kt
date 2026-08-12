package com.rs.game.content.world.areas.fremennik_isles

import com.rs.engine.pathfinder.Direction
import com.rs.game.World
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.Ticks
import com.rs.engine.dialogue.startConversation
import com.rs.engine.dialogue.HeadE.*

object ShoutingMatch {
    val insultsJatizso = listOf(
        "YOU CALL THAT A TOWN? I'VE SEEN BETTER HAMLETS!",
        "YOU WOULDN'T KNOW HOW TO FIGHT A TROLL IF IT CAME UP AND BIT YER ARM OFF!",
        "AND YOUR FATHER SMELLED OF WINTERBERRIES!",
        "YAK LOVERS!",
        "WOODEN BRIDGES ARE RUBBISH!",
        "OUR KING'S BETTER THAN YOUR BURGHER!"
    )
    val insultsNeitiznot = listOf(
        "YOU WOULDN'T KNOW HOW TO SHAVE A YAK IF YOUR LIFE DEPENDED ON IT!",
        "MISERABLE LOSERS!",
        "YOUR MOTHER WAS A HAMSTER!",
        "AT LEAST WE CAN COOK!",
        "AT LEAST WE HAVE SOMETHING OTHER THAN SMELLY FISH!",
        "AT LEAST WE DON'T PAY TAXES!"
    )
    const val NeitiznotID = 5490
    const val JatizsoID = 5489
    @Volatile var isMatchRunning = false
    @Volatile var lastShoutCycle: Long = -1L
}

class ShoutingMatchNpc(id: Int, tile: Tile) : NPC(id, tile) {
    override fun processNPC() {
        if (ShoutingMatch.isMatchRunning) {
            super.processNPC()
            return
        }
        val periodTicks = Ticks.fromSeconds(5).toLong()
        if (tickCounter % periodTicks == 0L) {
            val cycle = tickCounter / periodTicks
            if (cycle != ShoutingMatch.lastShoutCycle) {
                val isNeit = id == ShoutingMatch.NeitiznotID
                val shoutTurnIsNeit = (cycle % 2L == 0L)
                if (shoutTurnIsNeit == isNeit) {
                    val shout = if (isNeit) "JATIZSO!" else "NEITIZNOT!"
                    faceDir(if (isNeit) Direction.EAST else Direction.WEST)
                    forceTalk(shout)
                    ShoutingMatch.lastShoutCycle = cycle
                }
            }
        }
        super.processNPC()
    }
}

private fun ShoutingMatchD(player: Player, npc: NPC) {
    player.startConversation {
        player(CALM_TALK, "What are you doing here?")
        npc(npc, ANGRY, "I'M ON SHOUTING DUTY!")
        player(CALM_TALK, "No need to shout.")
        npc(npc, ANGRY, "I'M SORRY I'VE BEEN SHOUTING INSULTS SO LONG I CAN'T HELP IT!")
        player(CALM_TALK, "Who are you insulting?")
        npc(npc, ANGRY, "THE TOWER IN ${if (npc.id == ShoutingMatch.NeitiznotID) "JATIZSO" else "NEITIZNOT"}. THEY SHOUT INSULTS BACK.")
        player(CALM_TALK, "Err, why?")
        npc(npc, ANGRY, "THE ${if (npc.id == ShoutingMatch.NeitiznotID) "BURGHER" else "KING"} TELLS US TO!")
        player(CALM_TALK, "Your King is a strange person.")
    }
}

private fun ShoutingMatchWatch(player: Player, npc: NPC) {
    player.startConversation {
        options("Select an Option") {
            op("Can I Watch? I'm curious.") {
                npc(npc, ANGRY, "IF YOU LIKE!")
                exec {
                    if (ShoutingMatch.isMatchRunning) return@exec
                    ShoutingMatch.isMatchRunning = true
                    val neitiznot = World.getNPCsInChunkRange(8993243, 2)
                        .first { it.id == ShoutingMatch.NeitiznotID }
                    val jatizso = World.getNPCsInChunkRange(8995291, 2)
                        .first { it.id == ShoutingMatch.JatizsoID }
                    val rounds = minOf(
                        ShoutingMatch.insultsNeitiznot.size,
                        ShoutingMatch.insultsJatizso.size
                    )
                    val totalSteps = rounds * 2
                    for (i in 0 until totalSteps) {
                        val delay = Ticks.fromSeconds(3 * i)
                        if (i == 0) {
                            neitiznot.faceTile(jatizso.tile)
                            neitiznot.forceTalk(ShoutingMatch.insultsNeitiznot[0])
                        } else {
                            WorldTasks.delay(delay) {
                                if (i % 2 == 0) {
                                    val idx = i / 2
                                    neitiznot.faceTile(jatizso.tile)
                                    neitiznot.forceTalk(ShoutingMatch.insultsNeitiznot.getOrNull(idx) ?: return@delay)
                                } else {
                                    val idx = i / 2
                                    jatizso.faceTile(neitiznot.tile)
                                    jatizso.forceTalk(ShoutingMatch.insultsJatizso.getOrNull(idx) ?: return@delay)
                                }
                                if (i == totalSteps - 1) {
                                    ShoutingMatch.isMatchRunning = false
                                }
                            }
                        }
                    }
                }
            }
            op("Oh well, I'd better get going.")
        }
    }
}


@ServerStartupEvent
fun ShoutingMatchInit() {
    instantiateNpc(ShoutingMatch.NeitiznotID, ShoutingMatch.JatizsoID) {
            id, tile -> ShoutingMatchNpc(id, tile)
    }

    onNpcClick(ShoutingMatch.NeitiznotID, ShoutingMatch.JatizsoID) { e ->
        ShoutingMatchD(e.player, e.npc)
    }

    onNpcClick(ShoutingMatch.NeitiznotID, ShoutingMatch.JatizsoID, options = arrayOf("Watch-shouting")) { e ->
        ShoutingMatchWatch(e.player, e.npc)
    }
}
