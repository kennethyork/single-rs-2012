package com.rs.game.content.minigames.lividfarm.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.lividfarm.LividFarmController
import com.rs.game.content.minigames.lividfarm.producePoints
import com.rs.game.content.skills.magic.Rune
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.player.Skills
import com.rs.lib.Constants
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.Ticks

@ServerStartupEvent
fun initMeteora() {
    onNpcClick(LividFarmController.Meteora) { e ->
        if (e.player.tempAttribs.getB("LividFarm.meteoraIbis.Claimed")) {
            e.player.startConversation {
                npc(e.npc, HeadE.SAD, "Thank you so much! I know just the remedy!")
            }
            return@onNpcClick
        }
        if (!e.player.tempAttribs.getB("LividFarm.meteoraIbis.IbisPrompted")) {
            e.player.startConversation {
                player(HeadE.WORRIED, "What's the matter?")
                npc(e.npc, HeadE.SAD, "Please, my ibis won't stop complaining. There's something wrong with it, but I can't figure out what. Perhaps you can use your magic to tell me what's wrong with him?")
                simple("You can use the Monster Examine spell on the ibis to determine what's the matter with it.")
                exec {
                    e.player.tempAttribs.setB("LividFarm.meteoraIbis.IbisPrompted", true)
                    return@exec
                }
            }
            return@onNpcClick
        }
        val player = e.player
        val correct = LividFarmController.activeAilment

        val pages = listOf(
            listOf("Wing rash", "Indigestion", "Bruised ego", "Cracked beak"),
            listOf("Crumpled feathers", "Fishy throat", "Blistered feet"),
            listOf("Scratched eyelids", "Extreme headache", "Trapped wind")
        )

        fun showPage(index: Int) {
            player.startConversation {
                options("The ibis has...") {
                    if (index > 0) {
                        op("- Previous list -") { exec { showPage(index - 1) } }
                    }
                    pages[index].forEach { ailment ->
                        op(ailment) {
                            exec {
                                if (ailment == correct) {
                                    player.tempAttribs.removeB("LividFarm.meteoraIbis.IbisExamined")
                                    player.tempAttribs.removeB("LividFarm.meteoraIbis.IbisPrompted")
                                    player.tempAttribs.setB("LividFarm.meteoraIbis.Claimed", true)
                                    e.npc.anim(4413)
                                    LividFarmController.activeMeteora?.forceTalk("Yeah? Thank you so much! I know just the remedy!")
                                    player.inventory.addItem(Rune.WATER.id(), 10)
                                    player.inventory.addItem(Rune.EARTH.id(), 16)
                                    player.inventory.addItem(Rune.MIND.id(), 1)
                                    player.inventory.addItem(Rune.COSMIC.id(), 1)
                                    player.inventory.addItem(Rune.ASTRAL.id(), 10)
                                    player.sendMessage("You are awarded 20 Produce Points. Current total: ${player.producePoints * 10}")
                                    player.producePoints += 2
                                    player.skills.addXp(Constants.MAGIC, if (player.skills.getLevel(Skills.MAGIC) >= 66) 61.0 else 0.0)
                                } else {
                                    LividFarmController.activeMeteora?.forceTalk("No, that can't be it. I already had him checked for that.")
                                    player.tempAttribs.removeB("IbisExamined")
                                }
                            }
                        }
                    }
                    if (index < pages.lastIndex) {
                        op("More.") { exec { showPage(index + 1) } }
                    }
                }
            }
        }
        showPage(0)
    }
}