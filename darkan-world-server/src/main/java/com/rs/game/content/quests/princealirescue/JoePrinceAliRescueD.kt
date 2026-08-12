package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val JOE = 916

@ServerStartupEvent
fun mapJoePrinceAliRescue() {
    onNpcClick(JOE, options = arrayOf("Talk-to")) { (player, npc) ->
        player.joeDialogue(npc)
    }
}

fun Player.joeDialogue(npc: NPC) {
    startConversation {
        if (questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).getB("Joe_guard_is_drunk")) {
            npc(npc, HeadE.DRUNK, "Halt! Who goes there?")
            player(HeadE.HAPPY_TALKING, "Hello friend, I am just rescuing the prince, is that ok?")
            npc(npc, HeadE.DRUNK, "Thatsh a funny joke. You are lucky I am shober. Go in peace, friend.")
        } else {
            exec { joeOptions(npc) }
        }
    }
}

private fun Player.joeOptions(npc: NPC) {
    startConversation {
        options("Choose an option:") {
            if (inventory.containsItem(PrinceAliRescue.BEER, 3)) {
                op("I have some beer here, fancy one?") {
                    player(HeadE.HAPPY_TALKING, "I have some beer here, fancy one?")
                    npc(npc, HeadE.CALM_TALK, "Ah, that would be lovely, just one now, just to wet my throat.")
                    player(HeadE.HAPPY_TALKING, "Of course, it must be tough being here without a drink.")
                    simple("You hand a beer to the guard, he drinks it in seconds.")
                    npc(npc, HeadE.HAPPY_TALKING, "That was perfect, I can't thank you enough.")
                    player(HeadE.SECRETIVE, "How are you? Still ok? Not too drunk?")
                    player(HeadE.SECRETIVE, "Would you care for another, my friend?")
                    npc(npc, HeadE.CALM_TALK, "I better not, I don't want to be drunk on duty.")
                    player(HeadE.SECRETIVE, "Here, just keep these for later, I hate to see a thirsty guard.")
                    simple("You hand two more beers to the guard.")
                    simple("He takes a sip of one, and then he drinks them both.")
                    npc(npc, HeadE.DRUNK, "Franksh, that wash just what I need to shtay on guard. No more beersh, I don't want to get drunk.")
                    simple("The guard is drunk, and no longer a problem.") {
                        inventory.deleteItem(PrinceAliRescue.BEER, 3)
                        questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).setB("Joe_guard_is_drunk", true)
                    }
                }
            }
            op("Tell me about the life of a guard.") {
                player(HeadE.TALKING_ALOT, "Tell me about the life of a guard.")
                npc(npc, HeadE.CALM_TALK, "Well, the hours are good.....")
                npc(npc, HeadE.FRUSTRATED, ".... But most of those hours are a drag. If only I had spent more time in Knight school when I was a young boy. Maybe I wouldn't be here now, scared of Keli.")
                options("Choose an option:") {
                    op("Hey, chill out, I won't cause you trouble.") {
                        player(HeadE.CALM_TALK, "Hey, chill out, I won't cause you trouble.")
                        player(HeadE.CALM_TALK, "I was just wondering what you do to relax.")
                        npc(npc, HeadE.TALKING_ALOT, "You never relax with these people, but it's a good career for a young man and some of the shouting I rather like.")
                        npc(npc, HeadE.AMAZED, "RESISTANCE IS USELESS!") {
                            tempAttribs.setB("JoeTheGuardTalksALot", true)
                        }
                        exec { joeOptions(npc) }
                    }
                    op("What did you want to be when you were a boy?") {
                        exec { joeBoyDream(npc) }
                    }
                }
            }
            if (tempAttribs.getB("JoeTheGuardTalksALot")) {
                op("So what do you buy with these great wages?") {
                    player(HeadE.TALKING_ALOT, "So what do you buy with these great wages?")
                    npc(npc, HeadE.TALKING_ALOT, "Really, after working here, there's only time for a drink or three. All us guards go to the same bar and drink ourselves stupid.")
                    npc(npc, HeadE.TALKING_ALOT, "It's what I enjoy these days, that fade into unconciousness. I can't resist the sight of a really cold beer.")
                    exec { joeOptions(npc) }
                }
                op("Would you be interested in making a little more money?") {
                    player(HeadE.SECRETIVE, "Would you be interested in making a little more money?")
                    npc(npc, HeadE.ANGRY, "WHAT?! Are you trying to bribe me? I may not be a great guard, but I am loyal. How DARE you try to bribe me!")
                    player(HeadE.SHAKING_HEAD, "No, no, you got the wrong idea, totally. I just wondered if you wanted some part-time bodyguard work.")
                    npc(npc, HeadE.CALM_TALK, "Oh. Sorry. No, I don't need money. As long as you were not offering me a bribe.")
                    exec { joeOptions(npc) }
                }
            }
            op("What did you want to be when you were a boy?") {
                exec { joeBoyDream(npc) }
            }
            op("I had better leave, I don't want trouble.") {
                npc(npc, HeadE.HAPPY_TALKING, "Thanks, I appreciate that. Talking on duty can be punishable by having your mouth stitched up. These are tough people, no mistake.")
            }
        }
    }
}

private fun Player.joeBoyDream(npc: NPC) {
    startConversation {
        player(HeadE.TALKING_ALOT, "What did you want to be when you were a boy?")
        npc(npc, HeadE.TALKING_ALOT, "Well, I loved to sit by the lake, with my toes in the water and shoot the fish with my bow and arrow.")
        player(HeadE.TALKING_ALOT, "That was a strange hobby for a little boy.")
        npc(npc, HeadE.TALKING_ALOT, "It kept us from goblin hunting, which was what most boys did. What are you here for?")
        exec { joeOptions(npc) }
    }
}