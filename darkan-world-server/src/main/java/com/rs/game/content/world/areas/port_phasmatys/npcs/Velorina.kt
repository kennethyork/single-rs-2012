package com.rs.game.content.world.areas.port_phasmatys.npcs;

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.minigames.ectofuntus.Ectofuntus
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.GhostSpeakResponse
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.hasGhostSpeak
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick

class Velorina(p: Player, npc: NPC) {
    init {
        if (p.isQuestComplete(Quest.GHOSTS_AHOY))
            dialoguePostQuest(p, npc)
        if (!p.isQuestStarted(Quest.GHOSTS_AHOY))
            dialoguePreQuest(p, npc)
        else
            when (p.getQuestStage(Quest.GHOSTS_AHOY)) {
                GhostsAhoy.STAGE_1_BEGIN_QUEST -> dialogueStage1(p, npc)
                GhostsAhoy.STAGE_2_PLEAD_WITH_NECROVARUS -> dialogueStage2(p, npc)
                GhostsAhoy.STAGE_3_NECROVARUS_REFUSES, GhostsAhoy.STAGE_4_SEEK_OLD_WOMAN -> dialogueStage3(p, npc)
                GhostsAhoy.STAGE_5_GATHER_ITEMS -> dialogueStage5(p, npc)
                GhostsAhoy.STAGE_6_AMULET_ENCHANTED -> dialogueStage7(p, npc)
                GhostsAhoy.STAGE_7_COMMAND_NECROVARUS -> dialogueStage8(p, npc)
            }
    }
}

private fun dialoguePostQuest(p: Player, npc: NPC) {
    p.startConversation {
        options {
            op("I thought you were going to pass over to the next world.") {
                player(HeadE.CALM_TALK, "I thought you were going to pass over to the next world.")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "All in good time, ${p.displayName}. We stand forever in your debt, and will certainly put in a good word for you when we pass over."
                )
            }
            op("Can I have another Ectophial?") {
                player(HeadE.CALM_TALK, "Can I have another Ectophial?")
                if (!p.inventory.containsItem(4251)) {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Of course you can, you have helped us more than we could ever have hoped."
                    )
                    exec {
                        p.inventory.addItem(4251)
                        p.itemDialogue(4251, "Velorina gives you a vial of bright green ectoplasm.")
                    }
                } else
                    npc(npc, HeadE.CALM_TALK, "You already have an Ectophial.")
            }
        }
    }
}

private fun dialoguePreQuest(p: Player, npc: NPC) {
    p.startConversation {
        npc(
            npc,
            HeadE.CALM_TALK,
            "Take pity on me, please - eternity stretches out before me and I am helpless in its grasp."
        )
        label("initialOps")
        options {
            op("Why, whats the matter?") {
                player(HeadE.CALM_TALK, "Why, whats the matter?")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Oh, I'm sorry - I was just wailing out loud. I didn't mean to scare you."
                )
                player(
                    HeadE.CALM_TALK,
                    "No, That's okay - it takes more than a ghost to scare me. What is wrong?"
                )
                npc(npc, HeadE.CALM_TALK, "Do you know the history of our town?")
                label("S2")
                options {
                    op("Yes, I do. It's a very sad story.") {
                        player(HeadE.CALM_TALK, "Yes, I do. It's a very sad story.")
                        npc(npc, HeadE.CALM_TALK, "Would you help us obtain our release into the next world?")
                        label("Quest")
                        options {
                            op("Yes. Start Quest.") {
                                if (!p.isQuestComplete(Quest.PRIEST_IN_PERIL))
                                    p.sendMessage("You must complete Priest in Peril before you can accept this quest.")
                                else {
                                    questStart(Quest.GHOSTS_AHOY)
                                    p.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_1_BEGIN_QUEST)
                                    player(
                                        HeadE.CALM_TALK,
                                        "Yes, of course I will. Tell me what you want me to do."
                                    )
                                    npc(
                                        npc,
                                        HeadE.CALM_TALK,
                                        "Oh, thank you! Necrovarus will not listen to those of us who are already dead. He might rethink his position if someone with a mortal soul pleaded our cause. If he declines, there may yet be another way."
                                    )
                                }
                            }
                            op("No.") {
                                player(HeadE.SHAKING_HEAD, "No.")
                            }
                        }
                    }
                    op("No - Could you tell me?") {
                        player(HeadE.CALM_TALK, "No - Could you tell me?")
                        npc(npc, HeadE.CALM_TALK, "Do you know why ghosts exist?")
                        player(
                            HeadE.CALM_TALK,
                            "A ghost is a soul left in limbo, unable to pass over to the next world; they might have something left to do in this world that torments them, or the might just have died in a state of confusion."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "Yes, that is normally the case. But here in Port Phasmatys, we of this town once chose freely to become ghosts!"
                        )
                        player(HeadE.SCARED, "Why on earth would you do such a thing?!")
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "It is a long story. Many years ago, this was a thriving port, a trading centre to the eastern lands. We became rich on the profits made from the traders that came from across the eastern seas."
                        )
                        npc(npc, HeadE.CALM_TALK, "We were very happy...<br>Until Lord Drakan noticed us.")
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "He sent unholy creatures to demand that a blood-tithe be paid to the Lord Vampyre, is is required from all in the domain of Mortyania. We had no choice but to agree to his demands."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "As the years went by, our numbers dwindled and many spoke of abandoning the town for safer lands. Then Necrovarus came to us."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "He came from the eastern lands, but of more than that, little is known. Some say he was once a mage, some say a priest. Either way, he was in possession of knowledge totally unknown to even the most learned among us."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "Necrovarus told us that he had been brought by a vision he'd had of an underground source of power. He inspired us to delve beneath the town, promising us the power to repel the vampyres."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "Deep underneath Phasmatys, we found a pool of green slime that Necrovarus called Ectoplasm. He showed us how to build the Ectofuntus, which would turn the ectoplasm into the power he had promised us."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "Indeed, this Ectopower did repel the Vampyres; the would not enter Phasmatys once the Ectofuntus began working. But little did we know that we had exchanged one evil for yet another - Ectopower."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "Little by little, we began to lose any desire for food or water, and our desire for the Ectopower grew until it dominated our thoughts entirely. Our bodies shrivelled and, one by one, we died."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "The Ectofuntus and the power it emanates keeps us here as ghosts - some, like myself, content to remain in this world; some becoming tortured souls who we do not allow to pass our gates."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "We would be able to pass over into the next world but Necrovarus has used his power to create a psychic barrier, preventing us."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "We must remain her for all eternity, even unto the very end of the world."
                        )
                        player(HeadE.CALM_TALK, "That's a very sad story.")
                        npc(npc, HeadE.CALM_TALK, "Would you help us obtain our release into the next world?")
                        label("Quest")
                        options {
                            op("Yes. Start Quest.") {
                                questStart(Quest.GHOSTS_AHOY)
                                player(
                                    HeadE.CALM_TALK,
                                    "Yes, of course I will. Tell me what you want me to do."
                                )
                                npc(
                                    npc,
                                    HeadE.CALM_TALK,
                                    "Oh, thank you! Necrovarus will not listen to those of us who are already dead. He might rethink his position if someone with a mortal soul pleaded our cause. If he declines, there may yet be another way."
                                )
                            }
                            op("No.") {
                                player(HeadE.SHAKING_HEAD, "No.")
                            }
                        }
                    }
                    op("Sorry, I'm scared of ghosts.") {
                        player(HeadE.SCARED, "Leave me alone - I'm scared of ghosts!!!")
                    }
                }
            }

        }
    }
}

private fun dialogueStage1(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, HeadE.CALM_TALK, "I sense that you have not yet spoken to Necrovarus.")
        player(HeadE.CALM_TALK, "No, I was just getting to that.")
        npc(npc, HeadE.CALM_TALK, "Well, I suppose we do have all the time in the world.")
    }
}
private fun dialogueStage2(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "I'm sorry, but Necrovarus will not let you go.")
        npc(npc, HeadE.CALM_TALK, "I feared as much. His spirit is a thing of fire and wrath.")
        player(HeadE.CALM_TALK, "You spoke of another way?")
        npc(npc, HeadE.CALM, "It is only a small chance. During the building of the Ectofuntus one of Necrovarus's disciples spoke out against him.")
        npc(npc, HeadE.CALM, "It is such a long time ago I cannot remember her name, although I knew her as a friend.")
        npc(npc, HeadE.CALM, "She fled before the Ectofuntus took control over us, but being a disciple of Necrovarus she would have been privy to many of his darkest secrets.")
        npc(npc, HeadE.CALM, "She may know of a way to aid us without Necrovarus.")
        p.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_3_NECROVARUS_REFUSES)
        options {
            op("Do you know where this woman can be found?") {
                player(HeadE.CALM_TALK, "Do you know where this woman can be found?")
                npc(npc, HeadE.CALM_TALK, "I have a vision of a small wooden shack, the land it was built on the unholy soil of Morytania. I sense the sea is very close, and that there looms castles to the west and the east.")
            }
            op("If it was such a long time ago, won't she be dead already?") {
                npc(npc, HeadE.SHAKING_HEAD, "She was a friend of mine. Had she died, I would have felt her spirit pass over to the next world, though I may not follow.")
            }
        }
    }
}

private fun dialogueStage3(p: Player, npc: NPC) {
    p.startConversation {
        options {
            op("Do you know where this woman can be found?") {
                player(HeadE.CALM_TALK, "Do you know where this woman can be found?")
                npc(npc, HeadE.CALM_TALK, "I have a vision of a small wooden shack, the land it was built on the unholy soil of Morytania. I sense the sea is very close, and that there looms castles to the west and the east.")
            }
            op("If it was such a long time ago, won't she be dead already?") {
                npc(npc, HeadE.SHAKING_HEAD, "She was a friend of mine. Had she died, I would have felt her spirit pass over to the next world, though I may not follow.")
            }
        }
    }
}

private fun dialogueStage5(p: Player, npc: NPC) {
    p.startConversation {
        label("initialOptions")
        options {
            op("Do you know where I can find the Book of Haricanto?") {
                player(HeadE.CALM_TALK, "Do you know where I can find the Book of Haricanto?")
                npc(
                    npc,
                    HeadE.SHAKING_HEAD,
                    "Nobody knows what has happened to the Book. It was stolen when our port was raided by pirates many years ago, and never seen since."
                )
                goto("initialOptions")

            }
            op("Do you know where I can find the Robes of Necrovarus?") {
                player(HeadE.CALM_TALK, "Do you know where I can find the Robes of Necrovarus?")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "I imagine they are still worn by his mortal body, which now lies in a coffin inside the Temple."
                )
                goto("initialOptions")
            }
            op("I need something to translate the Book of Haricanto.") {
                player(HeadE.CALM_TALK, "I need something to translate the Book of Haricanto.")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "I don't really know. You could try asking some of the traders from the East - they might be able to help you."
                )
                goto("initialOptions")
            }
        }
    }
}
private fun dialogueStage7(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, HeadE.CALM_TALK, "How is it going?")
        player(HeadE.CALM_TALK, "I have had the Amulet of Ghostspeak enchanted, which I shall use to command Necrovarus to set you free.")
        npc(npc, HeadE.CALM_TALK, "Oh, kind ${p.genderTerm("sir", "lady")} - you are the answer to all our prayers!")
    }
}

private fun dialogueStage8(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, HeadE.CALM_TALK, "You don't need to tell me ${p.displayName} - I sensed the removal of Necrovarus's psychic barrier!")
        player(HeadE.CALM_TALK, "Only happy to help out.")
        npc(npc, HeadE.CALM_TALK, "Here, take this as a thank you for the service that you have given us.")
        item(Ectofuntus.FULL_ECTOPHIAL, "Velorina gives you a vial of bright green ectoplasm.")
        npc(npc, HeadE.CALM_TALK, "This is an Ectophial. If you ever want to come back to Port Phasmatys, empty this on the floor beneath your feet, and you will be instantly teleported to the temple - the source of its power.")
        npc(npc, HeadE.CALM_TALK, "Remember that once the Ectophial has been used you need to refill it from the Ectofuntus.")
        exec {
            p.completeQuest(Quest.GHOSTS_AHOY)
        }
    }
}
@ServerStartupEvent
fun mapVelorina() {
    val TRANSLATION_MANUAL = 4249
    val NECROVARUS_ROBE = 4247
    val BOOK_OF_HARICANTO = 4248
    val GHOSTSPEAK_E = 4250
    onNpcClick(1683, options = arrayOf("Talk-To")) { (player, npc) ->
        if (!hasGhostSpeak(player))
            GhostSpeakResponse(player, npc)
        else
            Velorina(player, npc)
    }
    //Temporary method to allow players to repair their quest stage
    onItemOnNpc(1683) { e ->
        if(e.player.isQuestComplete(Quest.GHOSTS_AHOY) || !e.player.isQuestStarted(Quest.GHOSTS_AHOY))
            return@onItemOnNpc
        when (e.item.id) {
            GHOSTSPEAK_E -> {
                e.player.sendMessage("Reset to STAGE_6_AMULET_ENCHANTED")
                e.player.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_6_AMULET_ENCHANTED)
            }
            NECROVARUS_ROBE, BOOK_OF_HARICANTO, TRANSLATION_MANUAL -> {
                e.player.sendMessage("Reset to STAGE_5_GATHER_ITEMS")
                e.player.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_5_GATHER_ITEMS)
            }
            else -> {
                e.player.startConversation {
                    options {
                        if(!e.player.questManager.isComplete(Quest.GHOSTS_AHOY) && e.player.questManager.getStage(Quest.GHOSTS_AHOY) >= 8)
                            op("Force Complete Ghosts Ahoy") {
                                exec {
                                    e.player.questManager.completeQuest(Quest.GHOSTS_AHOY)
                                }
                            }
                        op("Reset Ghosts Ahoy?") {
                            exec {
                                e.player.questManager.resetQuest(Quest.GHOSTS_AHOY)
                                e.player.sendMessage("Reset quest")
                            }
                        }
                        op("Nevermind") {
                            exec {
                                return@exec
                            }
                        }
                    }
                }
            }
        }
    }
}


