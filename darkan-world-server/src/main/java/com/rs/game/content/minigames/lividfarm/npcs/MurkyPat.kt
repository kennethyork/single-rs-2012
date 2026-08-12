package com.rs.game.content.minigames.lividfarm.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.lividfarm.LividFarmController
import com.rs.game.content.minigames.lividfarm.producePoints
import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.Rune
import com.rs.game.content.skills.magic.RuneSet
import com.rs.lib.game.SpotAnim
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun initMurkyPat() {
    onNpcClick(LividFarmController.MurkyPat, checkDistance = false) { e ->
        if (e.player.tempAttribs.getB("LividFarm.MurkyPat.Claimed")) {
            e.player.startConversation {
                npc(e.npc, HeadE.ANGRY, "That'll help a great deal when my delivering secret revenge next time I'm aggravated by them!")
            }
            return@onNpcClick
        }
        if (e.option.equals("Talk-to", true)) {
            e.player.startConversation {
                player(HeadE.WORRIED, "What's the matter?")
                npc(e.npc, HeadE.ANGRY, "The ${LividFarmController.activeTarget} keeps bothering me! They really don't like me.")
                npc(e.npc, HeadE.SKEPTICAL, "If you cast Vengeance Other on me, it should help me a great deal when delivering secret revenge next time I'm aggravated by them!")
            }
            return@onNpcClick
        }
        else {
            val player = e.player
            val correct = LividFarmController.activeTarget ?: return@onNpcClick
            if (!player.canCastSpell() || !Magic.checkMagicAndRunes(player, 93, false, RuneSet(Rune.ASTRAL, 3, Rune.DEATH, 3, Rune.EARTH, 10))) {
                return@onNpcClick
            }
            player.startConversation {
                options("Who should Murky Pat target?") {
                    LividFarmController.targets.forEach { target ->
                        op(target) {
                            exec {
                                if (target == correct) {
                                    Magic.checkMagicAndRunes(player, 93, true, RuneSet(Rune.ASTRAL, 3, Rune.DEATH, 3, Rune.EARTH, 10))
                                    player.spotAnim(SpotAnim(725, 0, 100))
                                    player.anim(4411)
                                    player.producePoints += 2
                                    player.sendMessage("You are awarded 20 Produce Points. Current total: ${player.producePoints * 10}")
                                    e.npc.forceTalk("Vengeance!")
                                    player.inventory.addItem(Rune.WATER.id(), 15)
                                    player.inventory.addItem(Rune.EARTH.id(), 20)
                                    player.inventory.addItem(Rune.DEATH.id(), 2)
                                    player.inventory.addItem(Rune.ASTRAL.id(), 3)
                                    e.player.tempAttribs.setB("LividFarm.MurkyPat.Claimed", true)
                                }
                                else {
                                    e.npc.forceTalk("That doesn't sound right. I have no quarrel with them!")
                                    return@exec
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
