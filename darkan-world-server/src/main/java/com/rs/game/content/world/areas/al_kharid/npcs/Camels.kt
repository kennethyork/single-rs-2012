package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.cache.loaders.ObjectType
import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.World.spawnObjectTemporary
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onNpcClick
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.Ticks

private object Camels {
    const val AL = 2809
    const val ELLY = 2810
    const val OLLIE = 2811
    const val CAM = 2812
    const val GENERIC = 2813
    const val ALICE = 2814
    const val NEFERTI = 2815

    val all = intArrayOf(AL, ELLY, OLLIE, CAM, GENERIC, ALICE, NEFERTI).toTypedArray()
}

@ServerStartupEvent
fun handleCamels() {
    onObjectClick(6257) { (player) ->
        player.sendMessage("I'm not putting my hands in that!")
    }
    onItemOnObject(objectNamesOrIds = arrayOf(6257), itemNamesOrIds = arrayOf(952),null, true) { (player) ->
        if(player.inventory.containsItem(1925)) {
            player.anim(830)
            player.inventory.replace(1925, 4601)
            player.inventory.refresh()
        }
        else {
            player.sendMessage("I'll need a bucket to collect some Dung.")
        }
    }
    onNpcClick(*Camels.all, options = arrayOf("Talk-to")) { (player, npc) ->
        val hasCamulate = player.inventory.containsItem(6707, 1) || player.equipment.neckId == 6707
        player.startConversation {
            options("What would you like to do?") {
                op("Ask the camel about its dung.") {
                    if (!hasCamulate) {
                        player(HeadE.CALM_TALK, "I'm sorry to bother you, but could you spare me a little dung?")
                        simple("The camel didn't seem to appreciate that question.")
                    } else {
                        player(HeadE.CALM_TALK, "I'm sorry to bother you, but could you spare me a little dung?")
                        npc(npc.id, HeadE.CAT_DISAPPOINTED, "Are you serious?")
                        player(HeadE.CALM_TALK, "Oh yes. If you'd be so kind...")
                        npc(npc.id, HeadE.CAT_DISAPPOINTED, "Well, just you close your eyes first. I'm not doing it while you're watching me!")
                        exec {
                            player.fadeScreen {
                                player.sendMessage("You close your eyes...")
                                spawnObjectTemporary(GameObject(6257, ObjectType.GROUND_INTERACT, 0, npc.tile), Ticks.fromMinutes(1), false)
                                player.startConversation {
                                    npc(npc.id, HeadE.CAT_LAUGH, "I hope that's what you wanted!")
                                    player(HeadE.CHEERFUL, "Ohhh yes. Lovely.")
                                }
                            }
                        }
                    }
                }
                op("Say something unpleasant.") {
                    when (Utils.random(0, 2)) {
                        0 -> player(HeadE.SKEPTICAL, "If I go near that camel, it'll probably bite my hand off.")
                        1 -> player(HeadE.SKEPTICAL, "Mmm... looks like that camel would make a nice kebab.")
                        2 -> player(HeadE.SKEPTICAL, "I wonder if that camel has fleas...")
                    }
                    if (hasCamulate) {
                        exec {
                            when (npc.id) {
                                Camels.AL -> al(npc.id)
                                Camels.OLLIE -> ollie(npc.id)
                                Camels.ELLY -> elly(npc.id)
                                Camels.CAM -> cam(npc.id)
                                Camels.ALICE -> alice(npc.id)
                                Camels.NEFERTI -> neferti(npc.id)
                            }
                        }
                    }
                    else {
                        exec {
                            when (Utils.random(0, 2)) {
                                0 -> player.sendMessage("The camel tries to stamp on your foot, but you pull it back quickly.")
                                1 -> player.sendMessage("The camel spits at you, and you jump back hurriedly.")
                                2 -> player.sendMessage("The camel turns its head and glares at you.")
                            }
                        }
                    }
                }
                op("Neither - I'm a polite person.") {
                    simple("You decide not to be rude.")
                }
            }
        }
    }
}

private fun DialogueBuilder.al(npcId: Int) {
    npc(npcId, HeadE.CAT_CHEERFUL, "Oh, it's you. Have you come to listen to my poems?")
    options("Select an Option") {
        op("I'd love to.") {
            npc(npcId, HeadE.CAT_CHEERFUL, "That's so kind of you. Which one would you like to hear? 'Shall I compare thee to a Desert's Day' is my finest yet, but I've also composed others.")
            options("Select an Option") {
                op("Listen to 'Shall I compare thee to a Desert's Day'.") {
                    npc(npcId, HeadE.CAT_CHEERFUL, "That's my favourite poem. Ahem...")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Shall I compare thee to a desert's day? Thou art drier and more rough-skinned;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Rough sandstorms shake the cacti away And summer's heat defers to autumn wind;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Sometimes too hot, the eye of heaven shines, With Guthix's gold complexion often dimmed;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "And every fair from fair sometime declines, By chance or desert's changing course untrimmed;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "But thine eternal desert shall not fade Nor lose possession of that sand thou owest;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Nor Zamorak brag thou art in his shades, When in eternal lines to sand thou growest;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "So long as camels breathe or eyes can see, So long lives this, and this gives life to thee.")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Ah, Elly, how beautiful you are.")
                }
                op("Listen to 'This is just to say'.") {
                    npc(npcId, HeadE.CAT_CHEERFUL, "I wrote this poem when I went to the oasis to nibble at a tree, then discovered I'd left nothing for Elly to nibble. I was distraught.")
                    npc(npcId, HeadE.CAT_CHEERFUL, "This is just to say")
                    npc(npcId, HeadE.CAT_CHEERFUL, "I have nibbled / the cacti / that were by / the oasis;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "and which / you were probably / saving / for lunch;")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Forgive me, / they were delicious, / so crunchy / and so cold.")
                    npc(npcId, HeadE.CAT_CHEERFUL, "I wonder if she's forgiven me for eating her snack.")
                }
                op("No, thank you.") {
                    player(HeadE.SHAKING_HEAD, "No, thank you.")
                    npc(npcId, HeadE.CAT_CHEERFUL, "Ah, well. I shall return to writing poems to Elly's beauty.")
                }
            }
        }
    }
}

private fun DialogueBuilder.elly(npcId: Int) {
    npc(npcId, HeadE.CAT_SHOUTING, "What did you say?")
    player(HeadE.SKEPTICAL, "Er, nothing important.")
    npc(npcId, HeadE.CAT_HAPPY, "Well, it's nice to have someone to talk to. Ollie is so remote, and I can't get up the courage to talk to him.")
    player(HeadE.CALM_TALK, "Who's Ollie?")
    npc(npcId, HeadE.CAT_CHEERFUL, "He's the camel wandering around near that glider. Isn't he so strong and handsome?")
    player(HeadE.CONFUSED, "Er, he just looks like any other camel to me.")
    npc(npcId, HeadE.CAT_CHEERFUL, "How can you not see how wonderful he is? Why, he can cross the Kharidian Desert in three bounds, and he's so strong that he could haul the brick for the Duel Arena in only a day!")
    player(HeadE.SKEPTICAL, "You're a camel, I'm not. Maybe you can see these things better.")
    npc(npcId, HeadE.CAT_SAD, "For someone who understands camel speech, you're not good at these things.")
}

private fun DialogueBuilder.ollie(npcId: Int) {
    npc(npcId, HeadE.CAT_SHOUTING, "What did you say?")
    player(HeadE.SKEPTICAL, "Er, nothing important.")
    npc(npcId, HeadE.CAT_HAPPY, "Ah, a human. Do you know anything about flying?")
    player(HeadE.CONFUSED, "Flying?")
    npc(npcId, HeadE.CAT_HAPPY, "Yes, flying. It's a wonderful thing. I've seen this glider fly, and it's so elegant and fast. If I could fly, I wouldn't want to do anything else. I'd just fly around all day, swooping and soaring.")
}

private fun DialogueBuilder.cam(npcId: Int) {
    npc(npcId, HeadE.CAT_SHOUTING, "What was that?")
    player(HeadE.SKEPTICAL, "Nothing important.")
    npc(npcId, HeadE.CALM_TALK, "The weather's really hot, don't you think?")
    player(HeadE.CONFUSED, "Not hot for camels, surely?")
    npc(npcId, HeadE.CAT_SAD, "Are you joking? It's burning hot out here! Sitting by the oasis is the only way to stay cool.")
    player(HeadE.CALM_TALK, "I thought camels carried their own water in their humps.")
    npc(npcId, HeadE.CAT_SAD, "It's never enough. And it's always far too hot to go anywhere in the desert.")
    npc(npcId, HeadE.CAT_SAD, "Sometimes I wish I could go travelling in cooler places, like all those adventurers I see passing through here.")
    player(HeadE.CHEERFUL, "Well, adventuring's thirsty work as well.")
    npc(npcId, HeadE.CAT_DISAPPOINTED, "Oh, that's a pity.")
    npc(npcId, HeadE.CAT_HAPPY, "In that case, I'll just stay by this nice cool oasis.")
}

private fun DialogueBuilder.alice(npcId: Int) {
    npc(npcId, HeadE.CAT_SHOUTING, "What did you say?")
    player(HeadE.SKEPTICAL, "Nothing, nothing.")
    npc(npcId, HeadE.CAT_SHOUTING, "Well, don't interrupt me, I've got work to do.")
    player(HeadE.CALM_TALK, "Really? What sort of work?")
    npc(npcId, HeadE.CALM_TALK, "I'm participating in the construction of a shopping and sales extravaganza.")
    player(HeadE.CONFUSED, "What?")
    npc(npcId, HeadE.CALM_TALK, "My masters have me hauling boxes around. There's not much to do at the moment, though.")
    player(HeadE.CALM_TALK, "Who are your masters, then?")
    npc(npcId, HeadE.CALM_TALK, "They're traders from Pollnivneach. Their boss is called Ali Morrisane.")
    player(HeadE.CALM_TALK, "So, what's it like working for your masters?")
    npc(npcId, HeadE.CAT_SAD, "It's hard work sometimes, but it could be worse. I've heard about a pair of camels back in Pollnivneach who were poisoned! They say someone put spicy sauce in their food.")
    player(HeadE.ANGRY, "Really? That's very cruel of whoever did it.")
}

private fun DialogueBuilder.neferti(npcId: Int) {
    npc(npcId, HeadE.CAT_SHOUTING, "What was that?")
    player(HeadE.SKEPTICAL, "Er, nothing important.")
    npc(npcId, HeadE.CAT_HAPPY, "Wait, you're a human, aren't you? Have you come from outside this town?")
    player(HeadE.CALM_TALK, "Yes, I have.")
    npc(npcId, HeadE.CAT_HAPPY, "Oh, how wonderful! Does that mean I can finally leave this place and see the world outside?")
    player(HeadE.SKEPTICAL, "Wouldn't your master have to take you?")
    npc(npcId, HeadE.CAT_SAD, "My master has the plague, so he can't leave this town. Nor can anyone else. It's so boring here, and I'd like to travel.")
    npc(npcId, HeadE.CAT_HAPPY, "Travelling's what camels do best, you know.")
    npc(npcId, HeadE.CAT_HAPPY, "I don't suppose you could take me with you, could you?")
    player(HeadE.SKEPTICAL, "Don't you have the plague as well?")
    npc(npcId, HeadE.CAT_HAPPY, "Well, I haven't turned funny colours yet, so probably not. Will you take me with you?")
    player(HeadE.CALM_TALK, "I don't really have space for a camel, sorry.")
    npc(npcId, HeadE.CAT_DISAPPOINTED, "Oh, well. Come back some time and bring me news of the rest of the world.")
}
