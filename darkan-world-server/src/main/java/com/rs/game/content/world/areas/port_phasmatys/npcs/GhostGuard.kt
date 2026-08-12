package com.rs.game.content.world.areas.port_phasmatys.npcs
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import kotlin.math.pow

class GhostGuard(p: Player) {
    val npc = 1706
    init {
        if (!PortPhasmatys.hasGhostSpeak(p))
            p.simpleDialogue("The ghost guards wail at you incomprehensibly, and thought you cannot understand their exact words you understand their meaning - you may not pass the barriers of Port Phasmatys.")
        if(p.isQuestComplete(Quest.GHOSTS_AHOY)) {
            p.startConversation {
                npc(npc, HeadE.CALM_TALK, "All visitors to Port Phasmatys must pay a toll charge of 2 Ectotokens. However, you have done the ghosts of our town a service that surpasses all value, so you may pass without charge.")
                exec {
                    passBarrier(p)
                }
            }
        }
        else
            p.startConversation {
                npc(npc, HeadE.CALM_TALK, "All visitors to Port Phasmatys must pay a toll charge of 2 Ectotokens.")
                label("initialOps")
                options {
                    if (p.inventory.containsItem(4278, 2)) {
                        op("I'd like to enter Port Phasmatys - here's 2 Ectotokens") {
                            player(HeadE.CALM_TALK, "I'd like to enter Port Phasmatys - here's 2 ectotokens")
                            exec {
                                passBarrier(p)
                            }
                        }
                    }
                    else {
                        p.sendMessage("You do not have enough Ectotokens to pay the toll.")
                    }
                    op("I'm not paying you Ectotokens just to go through a gate.") {
                        player(HeadE.SHAKING_HEAD, "I'm not paying you Ectotokens just to go through a gate.")
                        npc(npc, HeadE.CALM_TALK, "Sorry - it's Town Policy")
                    }
                    op("Where do I get Ectotokens?") {
                        player(HeadE.CALM_TALK, "Where do I get Ectotokens?")
                        npc(npc, HeadE.CALM_TALK, "You need to go to the Temple and earn some. Talk to the disciples - they will tell you how.")
                    }
                }
            }
    }
    private fun passBarrier(p: Player) {
        if(!playerCharged(p)) {
            p.sendMessage("You do not have enough Ectotokens to pay the toll.")
            return
        }
        val westBarrier = Tile.of(3652, 3485, 0)
        val northBarrier = Tile.of(3659, 3508, 0)
        if (distance(p, northBarrier) < distance(p, westBarrier)) {
            p.addWalkSteps(northBarrier.x, northBarrier.y - 1, 5,false)
        } else {
            p.addWalkSteps(westBarrier.x + 1, westBarrier.y, 5, false)
        }
    }

    private fun distance(p: Player, barrier: Tile): Double {
        return kotlin.math.sqrt((p.x - barrier.x).toDouble().pow(2.0) + (p.y - barrier.y).toDouble().pow(2.0))
    }

    private fun playerCharged(p: Player): Boolean {
        if(p.isQuestComplete(Quest.GHOSTS_AHOY))
            return true
        if (p.inventory.containsItem(4278, 2)) {
            p.inventory.deleteItem(4278, 2);
            return true
        }
        return false
    }
}

@ServerStartupEvent
fun mapghostGuard() {
    onNpcClick(1706, options = arrayOf("Talk-To")) { (player, npc) ->
        GhostGuard(player)
    }
}