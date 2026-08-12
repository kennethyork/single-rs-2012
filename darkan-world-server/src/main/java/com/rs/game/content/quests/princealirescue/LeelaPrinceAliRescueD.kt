package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val LEELA = 915

@ServerStartupEvent
fun mapLeelaPrinceAliRescue() {
    onNpcClick(LEELA, options = arrayOf("Talk-to")) { (player, npc) ->
        player.leelaDialogue(npc)
    }
}

fun Player.leelaDialogue(npc: NPC) {
    val stage = questManager.getStage(Quest.PRINCE_ALI_RESCUE)
    startConversation {
        when {
            isQuestComplete(Quest.PRINCE_ALI_RESCUE) -> {
                npc(npc, HeadE.HAPPY_TALKING, "Thank you, Al-Kharid will forever owe you for your help. I think that if there is ever anything that needs to be done, you will be someone they can rely on.")
            }

            stage <= PrinceAliRescue.STARTED -> {
                player(HeadE.HAPPY_TALKING, "What are you waiting here for?")
                npc(npc, HeadE.FRUSTRATED, "That is no concern of yours, adventurer.")
            }

            stage == PrinceAliRescue.GEAR_CHECK -> {
                val hasKey = questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).getB("Leela_has_key")
                val hasKeyInInventory = inventory.containsItem(PrinceAliRescue.BRONZE_KEY, 1)

                when {
                    hasKey && !hasKeyInInventory -> {
                        if (questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).getB("Leela_gave_key")) {
                            npc(npc, HeadE.CALM_TALK, "You lost the key?")
                            npc(npc, HeadE.CALM_TALK, "I am going to need 15 coins from you to pay for the bronze.")
                            if (inventory.hasCoins(15)) {
                                simple("Leela gives you a copy of the key to the prince's door.") {
                                    inventory.removeCoins(15)
                                    inventory.addItem(PrinceAliRescue.BRONZE_KEY, 1)
                                }
                            } else {
                                npc(npc, HeadE.CALM_TALK, "Do you have that?")
                                player(HeadE.SAD, "No...")
                            }
                        } else {
                            npc(npc, HeadE.CALM_TALK, "My father sent this key for you. Be careful not to lose it.")
                            simple("Leela gives you a copy of the key to the prince's door.") {
                                inventory.addItem(PrinceAliRescue.BRONZE_KEY, 1)
                                questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).setB("Leela_gave_key", true)
                            }
                            npc(npc, HeadE.CALM_TALK, "Don't forget to deal with the guard on the door. He is talkative, try to find a weakness in him.")
                        }
                    }

                    inventory.containsItem(PrinceAliRescue.KEY_PRINT, 1) -> {
                        npc(npc, HeadE.CALM_TALK, "You can give the key print and a bronze bar to Osman in Al-kharid.")
                        npc(npc, HeadE.CALM_TALK, "Then come back to me to get the key")
                    }

                    inventory.containsItem(PrinceAliRescue.BEER, 3)
                            && inventory.containsItem(PrinceAliRescue.BRONZE_KEY, 1)
                            && inventory.containsItem(PrinceAliRescue.PASTE, 1)
                            && inventory.containsItem(PrinceAliRescue.BLONDE_WIG, 1)
                            && inventory.containsItem(PrinceAliRescue.PINK_SKIRT, 1)
                            && inventory.containsItem(PrinceAliRescue.ROPE, 1) -> {
                        npc(npc, HeadE.HAPPY_TALKING, "You have everything you need to start the breakout.")
                        npc(npc, HeadE.HAPPY_TALKING, "Don't forget the plan")
                        npc(npc, HeadE.SECRETIVE, "Get Joe the guard drunk with 3 beers, tie Lady Keli up with a rope, open the prison with the jail key, put the disguise on Prince Ali and escape.")
                        npc(npc, HeadE.HAPPY_TALKING, "Got it?")
                        player(HeadE.HAPPY_TALKING, "Got it.")
                    }

                    else -> {
                        player(HeadE.HAPPY_TALKING, "I am here to help you free the prince.")
                        npc(npc, HeadE.CALM_TALK, "Your employment is known to me. Now, do you know all that we need to make the break?")
                        options("Choose an option:") {
                            op("I must make a disguise. What do you suggest?") {
                                npc(npc, HeadE.CALM_TALK, "Only the lady Keli, can wander about outside the jail. The guards will shoot to kill if they see the prince out so we need a disguise good enough to fool them at a distance.")
                                npc(npc, HeadE.CALM_TALK, "You need a wig, maybe made from wool. If you find someone who can work with wool ask them about it. There's a witch nearby may be able to help you dye it.")
                                npc(npc, HeadE.CALM_TALK, "We will need a skirt like hers, a pink one should be fine.")
                                npc(npc, HeadE.CALM_TALK, "We still need something to colour the Prince's skin lighter. There's a witch close to here. She knows about many things. She may know some way to make the skin lighter.")
                                npc(npc, HeadE.CALM_TALK, "You have rope I see, to tie up Keli. That will be the most dangerous part of the plan.")
                            }
                            op("I need to get the key made.") {
                                npc(npc, HeadE.CALM_TALK, "Yes, that is most important. There is no way you can get the real key. It is on a chain around Keli's neck. Almost impossible to steal.")
                                npc(npc, HeadE.CALM_TALK, "Get some soft clay and get her to show you the key somehow. Then take the print, with bronze, to my father.")
                            }
                            op("What can I do with the guards?") {
                                npc(npc, HeadE.CALM_TALK, "Most of the guards will be easy. The disguise will get past them. The only guard who will be a problem will be the one at the door.")
                                npc(npc, HeadE.CALM_TALK, "We can discuss this more when you have the rest of the escape kit.")
                            }
                            op("I will go and get the rest of the escape equipment.") {
                                npc(npc, HeadE.CALM_TALK, "Good, I shall await your return with everything.")
                            }
                        }
                    }
                }
            }
        }
    }
}
