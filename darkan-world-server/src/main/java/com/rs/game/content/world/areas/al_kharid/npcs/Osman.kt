package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.princealirescue.osmanPrinceAliRescueDialogue
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.lib.Constants

@ServerStartupEvent
fun handleOsman() {
    onNpcClick(5282, options = arrayOf("Talk-to")) { (player, npc) ->
        player.startConversation {
            options {
                if (player.isQuestStarted(Quest.PRINCE_ALI_RESCUE) || player.isQuestComplete(Quest.PRINCE_ALI_RESCUE)) {
                    op("Talk about Prince Ali To The Rescue") {
                        exec {
                            player.osmanPrinceAliRescueDialogue(npc)
                        }
                    }
                }
                op("Talk about Sorcerer's Garden") {
                    if (player.inventory.containsOneItem(10848, 10849, 10850, 10851)) {
                        player(HeadE.CHEERFUL, "I have some sq'irk juice for you.")
                        simple("Osman imparts some Thieving advice to you as a reward for the sq'irk juice.")
                        exec {
                            var totalXp = player.inventory.getAmountOf(10851) * 350
                            totalXp += player.inventory.getAmountOf(10848) * 1350
                            totalXp += player.inventory.getAmountOf(10850) * 2350
                            totalXp += player.inventory.getAmountOf(10849) * 3000
                            player.inventory.deleteItem(10848, Int.MAX_VALUE)
                            player.inventory.deleteItem(10849, Int.MAX_VALUE)
                            player.inventory.deleteItem(10850, Int.MAX_VALUE)
                            player.inventory.deleteItem(10851, Int.MAX_VALUE)
                            player.skills.addXp(Constants.THIEVING, totalXp.toDouble())
                        }
                    }
                    else {
                        player(HeadE.CHEERFUL, "Hi, I'd like to talk about sq'irks.")
                        npc(npc.id, HeadE.CHEERFUL, "Alright, what would you like to know about sq'irks?")
                        label("sqirkOptions")
                        options {
                            op("Where can I find sq'irks?") {
                                player(HeadE.CONFUSED, "Where can I find sq'irks?")
                                npc(npc.id, HeadE.FRUSTRATED, "There is a sorceress near the south eastern edge of Al Kharid who grows them. Once upon a time we considered each other friends.")
                                player(HeadE.CONFUSED, "What happened?")
                                npc(npc.id, HeadE.FRUSTRATED, "We fell out, and now she won't give me any more fruit.")
                                player(HeadE.CONFUSED, "So all I have to do is ask her for some fruit for you?")
                                npc(npc.id, HeadE.NERVOUS, "I doubt it will be that easy. She is not renowned for her generosity and is very secretive about her garden's location.")
                                player(HeadE.LAUGH, "Oh come on, it should be easy enough to find!")
                                npc(npc.id, HeadE.SHAKING_HEAD, "Her garden has remained hidden even to me - the chief spy of Al Kharid. I believe her garden must be hidden by magical means.")
                                player(HeadE.CHEERFUL, "This should be an interesting task. How many sq'irks do you want?")
                                npc(npc.id, HeadE.CHEERFUL, "I'll reward you as many as you can get your hands on but could you please squeeze the fruit into a glass first?")
                                goto("sqirkOptions")
                            }
                            op("Why can't you get the sq'irks yourself?") {
                                player(HeadE.CONFUSED, "Why can't you get the sq'irks yourself?")
                                npc(npc.id, HeadE.FRUSTRATED, "I may have mentioned that I had a falling out with Sorceress. Well, unsurprisingly, she refuses to give me any more of her garden's produce.")
                                goto("sqirkOptions")
                            }
                            op("How should I squeeze the fruit?") {
                                player(HeadE.CONFUSED, "How should I squeeze the fruit?")
                                npc(npc.id, HeadE.CHEERFUL, "Use a pestle and mortar to squeeze the sq'irks. Make sure you have an empty glass with you to collect the juice.")
                                goto("sqirkOptions")
                            }
                            op("Is there a reward for getting these sq'irks?") {
                                player(HeadE.CONFUSED, "Is there a reward for getting these sq'irks?")
                                npc(npc.id, HeadE.LAUGH, "Of course there is. I am a generous man. I'll teach you the art of Thieving for your troubles.")
                                player(HeadE.CONFUSED, "How much training will you give?")
                                npc(npc.id, HeadE.CHEERFUL, "That depends on the quantity and ripeness of the sq'irks you put into the juice.")
                                goto("sqirkOptions")
                            }
                            op("What's so good about sq'irk juice then?") {
                                player(HeadE.CONFUSED, "What's so good about sq'irk juice then?")
                                npc(npc.id, HeadE.LAUGH, "Ah it's sweet, sweet nectar for a thief or spy; it makes light fingers lighter, fleet fleet flightier and comes in four different colours for those who are easily amused.")
                                simple("Osman starts salivating at the thought of sq'irk juice.")
                                player(HeadE.SKEPTICAL, "It wouldn't have any addictive properties, would it?")
                                npc(npc.id, HeadE.CHEERFUL, "It only holds power over those with poor self-control, something which I have an abundance of.")
                                player(HeadE.SKEPTICAL, "I see.")
                                goto("sqirkOptions")
                            }
                        }
                    }
                }
            }
        }
    }
}

