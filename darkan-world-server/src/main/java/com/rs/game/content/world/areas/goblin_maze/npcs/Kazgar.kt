package com.rs.game.content.world.areas.goblin_maze.npcs

import com.rs.engine.cutscenekt.cutscene
import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

class Kazgar(val player: Player, val npc: NPC) {
    fun chat() {
        if (player.questManager.isComplete(Quest.LOST_TRIBE)) {
            player.startConversation {
                npc(npc, CALM_TALK, "Hello, friend?")
                options {
                    op("Why do the Dorgeshuun live underground?") {
                        npc(npc, CALM_TALK, "Our ancient legends say that goblins were created by an evil god in order to fight in a huge way. This god sent the Dorgeshuun tribe to fight a battle where all would die. But our ancestors escaped by hiding in a deep hole in the ground where our god could not find us.")
                        npc(npc, CALM_TALK, "Eventually an earthquake sealed the hole and they were forever safe.")
                        options {
                            op("The war is over now, so you can return to the surface.") {
                                npc(npc, CALM_TALK, "Even if the gods are no longer at war, I find it hard to believe that a tribe of peaceful goblins would be safe above.")
                                npc(npc, CALM_TALK, "I have heard of humans coming down into the daves to slaughter our people! But even if we could return, we would not want to. We are adapted to living underground, and we have built a home for ourselves that we do not want to leave.")
                            }
                            op("What was the name of your god?") {
                                npc(npc, CALM_TALK, "Our ancestors did not speak his name, thinking that to do so would attract his attention. His name has been unspoken for so long that it is now entirely forgotten. That is for the best.")
                                npc(npc, CALM_TALK, "We have survived perfectly well without the gods, and we do not want a return to the old ways.")
                            }
                        }
                    }
                    op("Can you show me the way to the mines?") {
                        npc(npc, CALM_TALK, "Certainly!")
                        exec { enterMines() }
                    }
                }
            }
        } else {
            player.sendMessage("Kazgar isn't interested in talking right now.")
        }
    }

    fun enterMines() {
        if (player.questManager.isComplete(Quest.LOST_TRIBE)) {
            player.cutscene {
                endTile = Tile.of(3313, 9613, 0)
                fadeIn()
                wait(4)
                entityTeleTo(player, 3311, 9613)
                fadeOut()
                wait(2)
                player.addWalkSteps(3313, 9613)
                wait(1)
            }
        } else {
            player.sendMessage("Kazgar isn't interested in talking right now.")
        }
    }
}

@ServerStartupEvent
fun mapKazgar() {
    onNpcClick(2085, options=arrayOf("Talk-to")) { (player, npc) -> Kazgar(player, npc).chat() }
    onNpcClick(2085, options=arrayOf("Follow")) { (player, npc) -> Kazgar(player, npc).enterMines() }
}
