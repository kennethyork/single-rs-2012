package com.rs.game.content.quests.ghosts_ahoy.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.content.world.areas.port_phasmatys.npcs.ghostInnkeeperPreQuest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick

class Netty(p: Player, npc: NPC) {
    init {
        if(p.isQuestComplete(Quest.GHOSTS_AHOY))
            dialoguePostQuest(p, npc)
        if(!p.isQuestStarted(Quest.GHOSTS_AHOY))
            defaultResponses(p, npc)
        else
            when(p.getQuestStage(Quest.GHOSTS_AHOY)) {
                in 1 .. 2 -> defaultResponses(p, npc)
                3 -> dialogueStage3(p, npc)
                4 -> dialogueStage4(p, npc)
                5 -> dialogueStage5(p, npc)
                6 -> dialogueStage6(p, npc)
                else -> dialoguePostQuest(p, npc)
            }
    }
}
private const val PORCELAIN_CUP = 4244
private const val GHOSTSPEAK = 552
private const val NETTLE_WATER_B = 4237
private const val NETTLE_TEA_B = 4239
private const val NETTLE_TEA_BM = 4240
private const val NETTLE_TEA_C = 4242
private const val NETTLE_TEA_CM = 4243
private const val NETTLE_TEA_P = 4245
private const val NETTLE_TEA_PM = 4246
private const val TRANSLATION_MANUAL = 4249
private const val NECROVARUS_ROBE = 4247
private const val BOOK_OF_HARICANTO = 4248
private const val GHOSTSPEAK_E = 4250

private val nettleTeaItems = mutableListOf(
    NETTLE_WATER_B,
    NETTLE_TEA_B,
    NETTLE_TEA_BM,
    NETTLE_TEA_C,
    NETTLE_TEA_CM,
    NETTLE_TEA_P,
    NETTLE_TEA_PM
)

private val nettleTeaBasic = mutableListOf(
    NETTLE_WATER_B,
    NETTLE_TEA_B,
    NETTLE_TEA_BM,
    NETTLE_TEA_C,
    NETTLE_TEA_CM,
)
private fun dialogueStage3(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "I'm here about Necrovarus. Were you once a disciple of Necrovarus in the Temple of Phasmatys, old woman?")
        npc(npc, HeadE.CALM_TALK, "I don't remember; I am so old and my memory goes back only so far...")
        player(HeadE.CALM_TALK, "Is there anything that can help to refresh your memory?")
        npc(npc, HeadE.CALM_TALK,"Yes, I would love a nice hot cup of nettle tea.")
        player(HeadE.CALM_TALK,"Do you know where I can find nettles around here?")
        npc(npc, HeadE.CALM_TALK,"I believe they grow wild in the Haunted Forest.")
        exec { p.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_4_SEEK_OLD_WOMAN) }
    }
}

private fun dialogueStage4(p: Player, npc: NPC) {
    p.startConversation {
        player(
            HeadE.CALM_TALK,
            "I'm here about Necrovarus. Were you once a disciple of Necrovarus in the Temple of Phasmatys, old woman?"
        )
        npc(npc, HeadE.CALM_TALK, "I don't remember; I am so old and my memory goes back only so far...")
        player(HeadE.CALM_TALK, "Is there anything that can help to refresh your memory?")
        npc(npc, HeadE.CALM_TALK, "Yes, I would love a nice hot cup of nettle tea.")
        exec {
            if (p.inventory.containsOneItem(*nettleTeaItems.toTypedArray().toIntArray())) {
                if (p.inventory.containsItem(NETTLE_TEA_PM))
                    teaFinal(p, npc)
                if (p.inventory.containsItem(NETTLE_TEA_P))
                    teaMilk(p, npc)
                if (p.inventory.containsOneItem(*nettleTeaBasic.toTypedArray().toIntArray()))
                    teaBasic(p, npc)
            }
        }
    }
}

private fun teaBasic(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Here's some tea for you, like you asked.")
        npc(npc, HeadE.CALM_TALK,"Yes, but it's not in my special cup! I only ever drink from my special cup!")
        player(HeadE.CALM_TALK, "I see. Can I have this special cup, then?")
        if (p.inventory.containsItem(PORCELAIN_CUP))
            npc(npc,HeadE.CALM_TALK, "I already gave you a cup - what did you do with it?")
        else if (!p.inventory.hasFreeSlots())
            npc(npc,HeadE.CALM_TALK, "You don't have the space for my special cup.")
        else
            exec {
                p.inventory.addItem(PORCELAIN_CUP)
                p.itemDialogue(PORCELAIN_CUP, "The old crone hands you her porcelain cup")
            }
    }
}

private fun teaMilk(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Here's a lovely cup of tea for you, in your own special cup.")
        npc(npc, HeadE.CALM_TALK, "Oh no, it hasn't got milk in it. I only drink tea with milk, I'm afraid.")
    }
}

private fun teaFinal(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Here's a lovely cup of milky tea for you, in your own special cup.")
        exec {
            p.inventory.deleteItem(NETTLE_TEA_PM, 1)
            p.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_5_GATHER_ITEMS)
        }
        simple("As the old woman drinks the cup of milky tea, enlightenment glows from within her eyes.")
        npc(npc, HeadE.CALM_TALK, "Ah, that's better. Now, let me see... Yes, I was once a disciple of Necrovarus.")
        player(HeadE.CALM_TALK, "I have come from Velorina. She needs your help.")
        npc(npc, HeadE.CALM_TALK, "Velorina? That name sounds familiar... Yes, Velorina was once a very good friend! It has been many years since we spoke last. How is she?")
        player(HeadE.CALM_TALK, "She's a ghost, I'm afraid.")
        npc(npc, HeadE.CALM_TALK, "They are all dead, then. Even Velorina. I should have done something to stop what was happening, instead of running away.")
        player(HeadE.CALM_TALK, "She and many others want to pass on into the next world, but Necrovarus will not let them. Do you know of any way to help them?")
        npc(npc, HeadE.CALM_TALK, "There might be one way... Do you have such a thing as a ghostspeak amulet?")
        if(p.equipment.neckId == GHOSTSPEAK)
            player(HeadE.CALM_TALK, "Yes, I'm wearing one right now.")
        else if(p.inventory.containsItems(GHOSTSPEAK))
            player(HeadE.CALM_TALK, "Yes, I'm carrying one in my bag.")
        else {
            player(HeadE.CALM_TALK, "No I don't; I'm afraid.")
            npc(npc,HeadE.CALM_TALK, "Well, you'll need to find one.")
            npc(npc,HeadE.CALM_TALK, "There is an enchantment that I can perform on such an amulet that will give it the power of command over ghosts.")
            npc(npc,HeadE.CALM_TALK, "It will work only once, but it will enable you to command Necrovarus to let the ghosts pass on.")
            player(HeadE.CALM_TALK, "What do you need to perform the enchantment?")
            npc(npc, HeadE.CALM_TALK, "Necrovarus had a magical robe for which he must have no more use. Only these robes hold the power needed to perform the enchantment.")
            npc(npc, HeadE.CALM_TALK, "All his rituals come from a book written by an ancient sorcerer from the East called Haricanto. Bring me this strange book.")
            npc(npc, HeadE.CALM_TALK, "I cannot read the strange letters of the eastern lands. I will need something to help me translate the book.")
            exec {
                dialogueStage5(p, npc)
            }
        }
        npc(npc, HeadE.CALM_TALK, "Well, that's a stroke of luck. There is an enchantment that I can perform on such an amulet that will give it the power of command over ghosts.")
        npc(npc, HeadE.CALM_TALK, "It will work only once, but it will enable you to command Necrovarus to let the ghosts pass on.")
        player(HeadE.CALM_TALK, "What do you need to perform the enchantment?")
        npc(npc, HeadE.CALM_TALK, "Necrovarus had a magical robe for which he must have no more use. Only these robes hold the power needed to perform the enchantment.")
        npc(npc, HeadE.CALM_TALK, "All his rituals come from a book written by an ancient sorcerer from the East called Haricanto. Bring me this strange book.")
        npc(npc, HeadE.CALM_TALK, "I cannot read the strange letters of the eastern lands. I will need something to help me translate the book.")
        exec {
            dialogueStage5(p, npc)
        }
    }
}

private fun dialogueStage5(p: Player, npc: NPC) {
    p.startConversation {
        label("initialOptions")
        options {
            if (p.getQuestStage(Quest.GHOSTS_AHOY) == GhostsAhoy.STAGE_5_GATHER_ITEMS) {
                if (!p.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("FOUND_SON")) {
                    op("You are doing so much for me - is there anything I can do for you?") {
                        player(HeadE.CALM_TALK, "You are doing so much for me - is there anything I can do for you?")
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "I have lived here on my own for many years, but not always. When I left Port Phasmatys, I took my son with me. He grew up to be a fine boy, always in love with the sea."
                        )
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "He was about twelve years old when he ran away with some pirates to be a cabin boy. I never saw him again."
                        )

                        player(HeadE.CALM_TALK, "That's the second saddest story I have heard today.")
                        npc(
                            npc,
                            HeadE.CALM_TALK,
                            "If you ever see him, please give him this...and tell him that his mother still loves him."
                        )
                        exec {
                            if (p.inventory.hasFreeSlots()) {
                                p.inventory.addItem(4253)
                                item(4253, "The old woman gives you a toy boat.")
                                player(HeadE.CALM_TALK, "Was this his boat?")
                                npc(
                                    npc,
                                    HeadE.CALM_TALK,
                                    "Yes, he made it himself. It is a model of the very ship in which he sailed away. The paint has peeled off and it has lost its flag, but I could never throw it away."
                                )
                                player(HeadE.CALM_TALK, "If I find him, I will pass it on.")
                            } else
                                npc(
                                    npc,
                                    HeadE.CALM_TALK,
                                    "Come back to me when you have some room to take it."
                                )
                        }
                    }
                } else {
                    op("Good news! I have found your son!") {
                        player(HeadE.CALM_TALK, "Good news! I have found your son!")
                        npc(npc, HeadE.CALM_TALK, "Goodness! Where is he?")
                        player(
                            HeadE.CALM_TALK,
                            "He lives on a shipwreck to the east of here. He remembers you and wishes you well."
                        )
                        npc(npc, HeadE.CALM_TALK, "Oh thank you! I will travel to see him as soon as I am able!!")
                    }

                }
                if (p.inventory.containsItem(NECROVARUS_ROBE) ||
                    p.inventory.containsItem(BOOK_OF_HARICANTO) ||
                    p.inventory.containsItem(TRANSLATION_MANUAL) ||
                    p.inventory.containsItem(GHOSTSPEAK)
                ) {
                    op("I have something for you.") {
                        exec {
                            handInItems(p, npc)
                        }
                    }
                }
                op("Remind me - what can I do about Necrovarus?") {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "There might be one way... Do you have such a thing as a ghostspeak amulet?"
                    )
                    if (p.equipment.neckId == GHOSTSPEAK)
                        player(HeadE.CALM_TALK, "Yes, I'm wearing one right now.")
                    else
                        player(HeadE.CALM_TALK, "Yes, I have one of those somewhere.")
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Well, that's a stroke of luck. There is an enchantment that I can perform on such an amulet that will give it the power of command over ghosts."
                    )
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "It will work only once, but it will enable you to command Necrovarus to let the ghosts pass on."
                    )

                    goto("initialOptions")
                }
                op("What did you want me to get for you?") {
                    player(HeadE.CALM_TALK, "What did you want me to get for you?")
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Necrovarus had a magical robe for which he must have no more use. Only these robes hold the power needed to perform the enchantment."
                    )
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "All his rituals come from a book written by an ancient sorcerer from the East called Haricanto. Bring me this strange book."
                    )
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "I cannot read the strange letters of the eastern lands. I will need something to help me translate the book."
                    )
                    goto("initialOptions")
                }
                op("I'll go and search for those items you asked for.") {
                    player(HeadE.CALM_TALK, "I'll go and search for those items you asked for.")
                }
            }
        }
    }
}

private fun handInItems(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "I have something for you.")
        exec {
            if (p.inventory.containsItem(NECROVARUS_ROBE)) {
                npc(npc, HeadE.CALM_TALK, "Good - the robes of Necrovarus.")
                p.inventory.deleteItem(NECROVARUS_ROBE, 1)
                p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("HAS_NECROVARUS_ROBE", true)
            }
            if (p.inventory.containsItem(TRANSLATION_MANUAL)) {
                npc(npc, HeadE.CALM_TALK, "A translation manual - yes, this should do the job.")
                p.inventory.deleteItem(TRANSLATION_MANUAL, 1)
                p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("HAS_TRANSLATION_MANUAL", true)
            }
            if (p.inventory.containsItem(BOOK_OF_HARICANTO)) {
                npc(npc, HeadE.CALM_TALK, "The Book of Haricanto! I have no idea how you came by this, but well done!")
                p.inventory.deleteItem(BOOK_OF_HARICANTO, 1)
                p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("HAS_BOOK_OF_HARICANTO", true)
            }

            val attribs = p.questManager.getAttribs(Quest.GHOSTS_AHOY)
            val HAS_TRANSLATION_MANUAL = attribs.getB("HAS_TRANSLATION_MANUAL")
            val HAS_BOOK_OF_HARICANTO = attribs.getB("HAS_BOOK_OF_HARICANTO")
            val HAS_NECROVARUS_ROBE = attribs.getB("HAS_NECROVARUS_ROBE")
            val HAS_GHOSTSPEAK = p.inventory.containsItem(GHOSTSPEAK) || p.equipment.neckId == GHOSTSPEAK
            val HAS_ALL_QUEST_ITEMS = HAS_TRANSLATION_MANUAL && HAS_BOOK_OF_HARICANTO && HAS_NECROVARUS_ROBE

            if (!HAS_GHOSTSPEAK && HAS_ALL_QUEST_ITEMS) {
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Wonderful; all I need now is an amulet of ghostspeak to perform the ritual of enchantment."
                )
                return@exec
            }
            if (HAS_GHOSTSPEAK && HAS_ALL_QUEST_ITEMS) {
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Wonderful; that's everything I need. I will now perform the ritual of enchantment."
                )
                item(GHOSTSPEAK_E, "The ghostspeak amulet emits a green glow from its gem.")
                if(p.inventory.containsItem(GHOSTSPEAK)) {
                    p.inventory.replace(GHOSTSPEAK, GHOSTSPEAK_E)
                }
                if(p.equipment.neckId == GHOSTSPEAK) {
                    p.equipment.replace(Equipment.NECK, Item(GHOSTSPEAK_E))
                }
                p.questManager.setStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_6_AMULET_ENCHANTED)
                return@exec
            }

            val missingItems = mutableListOf<String>()
            if (!HAS_NECROVARUS_ROBE) missingItems.add("the Robes of Necrovarus")
            if (!HAS_BOOK_OF_HARICANTO) missingItems.add("the Book of Haricanto")
            if (!HAS_TRANSLATION_MANUAL) missingItems.add("something to translate the Book of Haricanto")
            if (!HAS_GHOSTSPEAK) missingItems.add("a Ghostspeak Amulet")

            val missingItemsMessage = if (missingItems.isNotEmpty()) {
                "You are still missing: ${missingItems.joinToString(", ")}. The sooner you find them the sooner we can perform the ritual."
            } else ""
            npc(npc, HeadE.CALM_TALK, missingItemsMessage)
        }
    }
}

private fun dialogueStage6(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "I'm here about Necrovarus.")
        npc(npc, HeadE.CALM_TALK, "Did it work?")
        player(HeadE.CALM_TALK, "Actually, I haven't tried it yet.")
    }
}

private fun dialoguePostQuest(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Good news! I have found your son!")
        npc(npc, HeadE.CALM_TALK, "Goodness! Where is he?")
        player(HeadE.CALM_TALK, "He lives on a shipwreck to the east of here. He remembers you and wishes you well.")
        npc(npc, HeadE.CALM_TALK, "Oh thank you! I will travel to see him as soon as I am able!!")
    }
}



private fun defaultResponses(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Hello, Old Woman.")
        when (Utils.random(3)) {
            0 -> npc(npc, HeadE.CALM_TALK, "I'm over 100 years old, you know.")
            1 -> npc(npc, HeadE.CALM_TALK, "I lived here when this was all just fields, you know.")
            2 -> npc(npc, HeadE.CALM_TALK, "When 100 years old you reach, look like a prune you will.")
        }
    }
}

@ServerStartupEvent
fun mapNettyGA() {
    onNpcClick(1695, options = arrayOf("Talk-To")) { (player, npc) ->
        Netty(player, npc)
    }
    onItemOnNpc(1695) { e ->
        val GHOSTSPEAK = 552
        val HAS_TRANSLATION_MANUAL = e.player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("HAS_TRANSLATION_MANUAL")
        val HAS_BOOK_OF_HARICANTO = e.player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("HAS_BOOK_OF_HARICANTO")
        val HAS_NECROVARUS_ROBE = e.player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("HAS_NECROVARUS_ROBE")
        val hasAll = HAS_TRANSLATION_MANUAL && HAS_BOOK_OF_HARICANTO && HAS_NECROVARUS_ROBE
        if(e.item.id == NETTLE_TEA_PM)
            teaFinal(e.player, e.npc)
        if (e.item.id == NETTLE_TEA_P)
            teaMilk(e.player, e.npc)
        if (e.item.id in nettleTeaBasic)
            teaBasic(e.player, e.npc)
        if (e.item.id == NECROVARUS_ROBE || e.item.id == TRANSLATION_MANUAL || e.item.id == BOOK_OF_HARICANTO)
            handInItems(e.player, e.npc)
        if (e.item.id == GHOSTSPEAK && hasAll)
            handInItems(e.player, e.npc)
    }
}

