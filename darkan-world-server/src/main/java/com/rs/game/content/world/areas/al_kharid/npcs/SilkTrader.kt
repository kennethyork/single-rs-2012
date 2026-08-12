package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick


private const val npcId = 539

@ServerStartupEvent
fun handleSilkTrader() {
    onNpcClick(npcId, options = arrayOf("Talk-to")) { (player) ->
        player.startConversation {
            npc(npcId, HeadE.AMAZED, "Do you want to buy any fine silks?")
            options {
                op("How much are they?") {
                    player(HeadE.HAPPY_TALKING, "How much are they?")
                    npc(npcId, HeadE.HAPPY_TALKING, "3 coins.")
                    options {
                        op("3 coins sounds good.") {
                            exec {
                                if (player.inventory.hasCoins(3)) {
                                    player.inventory.removeCoins(3)
                                    player.inventory.addItem(950, 1)
                                } else {
                                    player(HeadE.CALM_TALK, "Oh dear. I don't have enough money.")
                                    npc(npcId, HeadE.SCARED, "Well, come back when you do have some money!")
                                }
                            }
                        }
                        op("No. That's too much for me.") {
                            player(HeadE.SHAKING_HEAD, "No. That's too much for me.")
                            npc(npcId, HeadE.SAD, "2 gp and that's as low as I'll go.")
                            npc(npcId, HeadE.SKEPTICAL, " I'm not selling it for any less. You'll probably go and sell it in Varrock for a profit, anyway.")
                            options {
                                op("2 coins sounds good.") {
                                    exec {
                                        if (player.inventory.hasCoins(2)) {
                                            player.inventory.removeCoins(2)
                                            player.inventory.addItem(950, 1)
                                        } else {
                                            player(HeadE.CALM_TALK, "Oh dear. I don't have enough money.")
                                            npc(npcId, HeadE.SCARED, "Well, come back when you do have some money!")
                                        }
                                    }
                                }
                                op("No, really. I don't want it.") {
                                    player(HeadE.SHAKING_HEAD, "No, really. I don't want it..")
                                    npc(npcId, HeadE.SAD, "Okay, but that's the best price you're going to get.")
                                }
                            }
                        }
                    }
                }
                op("No. Silk doesn't suit me.") {
                    player(HeadE.HAPPY_TALKING, "No. Silk doesn't suit me.")
                }
            }
        }
    }
}