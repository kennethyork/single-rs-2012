package com.rs.game.content.minigames.lividfarm.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.lividfarm.LividFarm
import com.rs.game.content.minigames.lividfarm.LividFarmController
import com.rs.game.content.minigames.lividfarm.producePoints
import com.rs.game.content.skills.magic.Rune
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.Ticks
import com.rs.engine.variables.lunarfm_pauline_state

fun encouragePauline(player: Player, npc: NPC) {
    val dialogues = listOf(
        listOf(
            "Lazybones!" to false,
            "Slow-coach!" to false,
            "Sleepy!" to false,
            "Extraordinary!" to true
        ),
        listOf(
            "Come on, you're doing so well." to true,
            "Come on or I'm going home." to false,
            "Come on, you're rubbish." to false,
            "Come on and help, lazy." to false
        ),
        listOf(
            "You're doing a slow job." to false,
            "You're not doing a fantastic job." to false,
            "You're doing a fantastic job." to true,
            "You're doing okay, I suppose." to false
        ),
        listOf(
            "Lokar won't appreciate this." to false,
            "Lokar will really appreciate this." to true,
            "I don't know why you bother." to false,
            "Hello? Anybody there?" to false
        ),
        listOf(
            "Everyone's working except you." to false,
            "Care to join me?" to false,
            "Look at all the produce being made." to true,
            "Wakey wakey." to false
        ),
        listOf(
            "Keep going! I'm not doing this." to false,
            "Keep going! We can do this." to true,
            "Keep away! I'll do this." to false,
            "Today would be nice!" to false
        )
    )

    val optionSet = dialogues.random()

    player.startConversation {
        options("Choose a line to encourage Pauline.") {
            optionSet.forEach { (line, isCorrect) ->
                op(line) {
                    exec {
                        player.forceTalk(line)
                        player.schedule {
                            if (isCorrect) {
                                wait(Ticks.fromSeconds(1))
                                npc.forceTalk("Thank you!")
                                player.sendMessage("You encourage Pauline with your inspiring words!")
                                LividFarmController.activeSuqah?.let {
                                    player.inventory.addItem(Rune.WATER.id(), 10)
                                    player.inventory.addItem(Rune.EARTH.id(), 16)
                                    player.inventory.addItem(Rune.NATURE.id(), 1)
                                    player.inventory.addItem(Rune.LAW.id(), 2)
                                    player.inventory.addItem(Rune.ASTRAL.id(), 3)
                                    player.tempAttribs.setB("LividFarm.Suqah.Claimed", true)
                                }
                                player.vars.setVarBit(lunarfm_pauline_state, 0)
                                player.producePoints += 10
                                player.skills.addXp(Constants.AGILITY, 254.5)
                                player.sendMessage("You are awarded 100 Produce Points. Current total: ${player.producePoints * 10}")
                            } else {
                                player.sendMessage("Pauline didn't seem inspired.")
                            }
                        }
                    }
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapPaulinePolaris() {
    onNpcClick(LividFarmController.Pauline) { e ->
        if (e.option.equals("Talk-to", true)) {
            e.player.startConversation {
                player(HeadE.CALM_TALK, "Hi Pauline.")
                npc(e.npc, HeadE.CALM_TALK, "Oh, hi. How can I help you?")
                options("Can you tell me how to work on your farm?") {
                    op("Can you tell me how to work on your farm?") {
                        player(HeadE.CALM_TALK, "Can you tell me how to work on your farm?")
                        npc(e.npc, HeadE.CALM_TALK, "Sure, what part of the farm do you want to know about?")
                        options {
                            op("How can I fertilise the soil?") {
                                player(HeadE.CALM_TALK, "How can I fertilise the soil?")
                                npc(e.npc, HeadE.CALM_TALK, "After I've harvested a patch, I will replant a livid seed and it will need fertilising for it to appear.")
                                npc(e.npc, HeadE.CALM_TALK, "I'll get around to this eventually, but, if you cast the Fertilise Soil spell on the patch, you'll get further knowledge in Magic and Farming.")
                            }
                            op("How can I cure the diseased plants?") {
                                player(HeadE.CALM_TALK, "How can I cure the diseased plants?")
                                npc(e.npc, HeadE.CALM_TALK, "You will see my plants get diseased quite a lot. Cast the Cure Plant spell on the crop and then refine it to the strain of livid.")
                                npc(e.npc, HeadE.CALM_TALK, "You'll get Magic and Farming experience if you do. Otherwise, I'll cure them, but you won't be rewarded.")
                            }
                            op("How can I re-energise you?") {
                                player(HeadE.CALM_TALK, "How can I re-energise you?")
                                npc(e.npc, HeadE.CALM_TALK, "Give me some encouraging words. You'll earn Magic and Agility XP.")
                                npc(e.npc, HeadE.CALM_TALK, "I can recover on my own, but your help is appreciated.")
                            }
                            op("What do I do with the pile of produce?") {
                                player(HeadE.CALM_TALK, "What do I do with the pile of produce?")
                                npc(e.npc, HeadE.CALM_TALK, "Grab up to ten livid, bunch them with string using the String Jewellery spell, then place in the trade wagon.")
                                npc(e.npc, HeadE.CALM_TALK, "You'll get Magic and Crafting XP. Be quick, or I'll do it myself.")
                            }
                            op("How do I fix the fences?") {
                                player(HeadE.CALM_TALK, "How do I fix the fences?")
                                npc(e.npc, HeadE.CALM_TALK, "Pick up Lunar lumber from the corner of the farm and use Plank Make to turn it into fence posts.")
                                npc(e.npc, HeadE.CALM_TALK, "Then, use them on the broken fences for Magic and Construction XP.")
                            }
                        }
                    }
                    op("What rewards will I get?") {
                        player(HeadE.CALM_TALK, "What rewards will I get?")
                        npc(e.npc, HeadE.CHEERFUL, "Mainly experience in Magic, Farming, Crafting, Agility, and Construction. Also, some new spells if you help enough.")
                        npc(e.npc, HeadE.CHEERFUL, "Let me show you.")
                        exec { e.player.interfaceManager.sendInterface(1083) }
                    }
                    op("Can I claim some rewards now?") {
                        exec { e.player.interfaceManager.sendInterface(1083) }
                    }
                    op("Can you tell me more about why you started this farm?") {
                        player(HeadE.CALM_TALK, "Can you tell me more about why you started this farm?")
                        npc(e.npc, HeadE.CHEERFUL, "Lokar brought me this strain of vine and asked me to enhance it for protection. It might work as a fence!")
                        player(HeadE.SKEPTICAL, "I get that, but why help Lokar? He's a pirate. How can you trust him?")
                        npc(e.npc, HeadE.CHEERFUL, "A pirate? I don't think so. He travels the seas for treasure—such an adventurer! He's exciting.")
                        options {
                            op("I'm not sure that's true.") {
                                player(HeadE.SKEPTICAL, "I'm not sure that's true. Innocents he attacks wouldn't agree.")
                                npc(e.npc, HeadE.ANGRY, "Don't be silly. He's a fabulous man who's explored distant lands!")
                                goto("moon-clan-magic")
                            }
                            op("I'm glad you've found someone that makes you happy.") {
                                player(HeadE.CALM_TALK, "I'm glad you've found someone that makes you happy.")
                                npc(e.npc, HeadE.CHEERFUL, "Thank you. I hope he visits me soon.")
                                label("moon-clan-magic")
                                player(HeadE.SKEPTICAL, "What of giving away the secrets of the Moon Clan magic?")
                                npc(e.npc, HeadE.SKEPTICAL, "They only receive the end-product. The knowledge still rests with me and, to some extent, you.")
                                options {
                                    op("Well, you seem to have it all under control.") {
                                        player(HeadE.CALM_TALK, "Well, you seem to have it all under control.")
                                        npc(e.npc, HeadE.CHEERFUL, "Yes. If you see Lokar in Rellekka, tell him to visit me soon!")
                                        goto("options")
                                    }
                                    op("I hope Lokar sees this the same way as you do.") {
                                        player(HeadE.CALM_TALK, "I hope Lokar sees this the same way as you do.")
                                        npc(e.npc, HeadE.CHEERFUL, "I'm sure he does. If you see him, tell him to come visit me soon!")
                                        goto("options")
                                    }
                                }
                            }
                        }
                    }
                    op("See you around!") {
                        player(HeadE.CALM_TALK, "See you around.")
                        npc(e.npc, HeadE.CHEERFUL, "I do hope so.")
                    }
                }
                label("options")
            }
            return@onNpcClick
        }
        if (e.option.equals("Energise", true)) {
            if (e.player.vars.getVarBit(lunarfm_pauline_state) == 1) {
                encouragePauline(e.player, e.npc)
            } else {
                e.player.sendMessage("Pauline's currently fine.")
            }
        }
        if (e.option.equals("Claim-Rewards", true)) {
            e.player.vars.syncVarsToClient()
            e.player.schedule() {
                wait(Ticks.fromSeconds(1))
                e.player.interfaceManager.sendInterface(1083)
                e.player.packets.sendRunScript(4266)
                e.player.packets.sendRunScript(4267)
            }
        }
    }
    instantiateNpc(LividFarmController.Pauline) { id, tile ->
        object : NPC(id, tile) {
            init {
                LividFarm.pauline = this
                this.capDamage = 0
            }
        }
    }
}