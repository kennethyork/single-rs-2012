package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

private const val OSMAN = 5282

fun Player.osmanPrinceAliRescueDialogue(npc: NPC) {
    val stage = questManager.getStage(Quest.PRINCE_ALI_RESCUE)
    startConversation {
        when (stage) {
            PrinceAliRescue.NOT_STARTED -> {
                npc(npc, HeadE.HAPPY_TALKING, "I have no reason to trust you regarding these affairs.")
            }

            PrinceAliRescue.STARTED -> {
                player(HeadE.HAPPY_TALKING, "The chancellor trusts me. I have come for instructions.")
                npc(npc, HeadE.TALKING_ALOT, "Our prince is captive by the Lady Keli. We just need to make the rescue. There are two things we need you to do.")
                options("Choose an option:") {
                    op("What is the first thing I must do?") {
                        exec { osmanFirstThing(npc) }
                    }
                    op("What is the second thing you need?") {
                        exec { osmanSecondThing(npc) }
                    }
                }
            }

            PrinceAliRescue.GEAR_CHECK -> {
                if (inventory.containsItem(PrinceAliRescue.KEY_PRINT, 1) && inventory.containsItem(PrinceAliRescue.BRONZE_BAR, 1)) {
                    npc(npc, HeadE.HAPPY_TALKING, "Well done, we can make the key now.")
                    simple("Osman takes the key imprint and the bronze bar.") {
                        inventory.deleteItem(PrinceAliRescue.KEY_PRINT, 1)
                        inventory.deleteItem(PrinceAliRescue.BRONZE_BAR, 1)
                        questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).setB("Leela_has_key", true)
                    }
                    npc(npc, HeadE.HAPPY_TALKING, "Pick the key up from Leela.")
                    player(HeadE.HAPPY_TALKING, "Thank you. I will try to find the other items.")
                } else {
                    player(HeadE.TALKING_ALOT, "Can you tell me what I still need to get?")
                    if (questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).getB("Leela_has_key"))
                        npc(npc, HeadE.CALM_TALK, "Make sure to have the bronze key from Leela.")
                    else
                        npc(npc, HeadE.CALM_TALK, "A print of the key in soft clay and a bronze bar. Then, collect the key from Leela.")
                    npc(npc, HeadE.CALM_TALK, "You need to make a blonde wig somehow. Leela may help.")
                    npc(npc, HeadE.CALM_TALK, "You will need a skirt that looks the same as Keli's")
                    npc(npc, HeadE.CALM_TALK, "Something to make the prince's skin appear lighter.")
                    npc(npc, HeadE.CALM_TALK, "A rope with which to tie Keli up.")
                    npc(npc, HeadE.CALM_TALK, "Once you have everything, go to Leela. She must be ready to get the prince away.")
                }
            }

            else -> {
                npc(npc, HeadE.HAPPY_TALKING, "Well done. A great rescue. I will remember you if I have anything dangerous to do.")
            }
        }
    }
}

private fun Player.osmanFirstThing(npc: NPC) {
    startConversation {
        player(HeadE.SKEPTICAL_THINKING, "What is the first thing I must do?")
        npc(npc, HeadE.CALM_TALK, "The prince is guarded by some stupid guards and a clever woman. The woman is our only way to get the prince out. Only she can walk freely about the area.")
        npc(npc, HeadE.CALM_TALK, "I think you will need to tie her up. One coil of rope should do for that. Then, disguise the prince as her to get him out without suspicion.")
        player(HeadE.SKEPTICAL_THINKING, "How good must the disguise be?")
        npc(npc, HeadE.CALM_TALK, "Only enough to fool the guards at a distance. Get a skirt like hers. Same colour, same style. We will only have a short time.")
        npc(npc, HeadE.CALM_TALK, "Get a blonde wig, too. That is up to you to make or find. Something to colour the skin of the prince.")
        npc(npc, HeadE.CALM_TALK, "My daughter and top spy, Leela, can help you. She has sent word that she has discovered where they are keeping the prince.")
        npc(npc, HeadE.CALM_TALK, "It's near Draynor Village. She is lurking somewhere near there now.")
        options("Choose an option:") {
            op("Explain the first thing again.") {
                exec { osmanFirstThing(npc) }
            }
            op("What is the second thing you need?") {
                exec { osmanSecondThing(npc) }
            }
            op("Okay, I better go find some things.") {
                npc(npc, HeadE.TALKING_ALOT, "May good luck travel with you. Don't forget to find Leela. It can't be done without her help.")
            }
        }
    }
}

private fun Player.osmanSecondThing(npc: NPC) {
    startConversation {
        npc(npc, HeadE.TALKING_ALOT, "We need the key, or we need a copy made. If you can get some soft clay then you can copy the key...")
        npc(npc, HeadE.TALKING_ALOT, "...If you can convince Lady Keli to show it to you for a moment. She is very boastful. It should not be too hard.")
        npc(npc, HeadE.TALKING_ALOT, "Bring the imprint to me, with a bar of bronze.") {
            questManager.setStage(Quest.PRINCE_ALI_RESCUE, PrinceAliRescue.GEAR_CHECK)
        }
        player(HeadE.HAPPY_TALKING, "Sounds like a mouthful, okay ill get on it!")
    }
}