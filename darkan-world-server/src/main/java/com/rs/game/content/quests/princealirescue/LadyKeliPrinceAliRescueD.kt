package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val LADY_KELI = 919

@ServerStartupEvent
fun mapLadyKeliPrinceAliRescue() {
    onNpcClick(LADY_KELI, options = arrayOf("Talk-to")) { (player, npc) ->
        player.ladyKeliDialogue(npc)
    }
}

fun Player.ladyKeliDialogue(npc: NPC) {
    startConversation {
        player(HeadE.HAPPY_TALKING, "Are you the famous Lady Keli?")
        player(HeadE.HAPPY_TALKING, "Leader of the toughest gang of mercenary killers around?")
        npc(npc, HeadE.HAPPY_TALKING, "I am Keli, you have heard of me then")
        exec { keliOriginalOptions(npc, isFirst = true) }
    }
}

private fun Player.keliOriginalOptions(npc: NPC, isFirst: Boolean) {
    startConversation {
        options("Choose an option:") {
            if (isFirst) {
                op("Heard of you? You are famous in Gielinor!") {
                    player(HeadE.AMAZED_MILD, "Heard of you? You are famous in Gielinor!")
                    npc(npc, HeadE.HAPPY_TALKING, "That's very kind of you to say. Reputations are not easily earned. I have managed to succeed where many fail.")
                    exec { keliOriginalOptions(npc, isFirst = false) }
                }
            }
            op("I have heard a little, but I think Katrine is tougher.") {
                player(HeadE.CALM_TALK, "I think Katrine is still tougher.")
                npc(npc, HeadE.FRUSTRATED, "Well you can think that all you like. I know those blackarm cowards dare not leave the city. Out here, I am toughest. You can tell them that! Now get out of my sight, before I call my guards.")
            }
            if (!isFirst) {
                op("What is your latest plan then?") {
                    player(HeadE.SECRETIVE, "What is your latest plan then?")
                    player(HeadE.SKEPTICAL_THINKING, "Of course you need not go into specific details")
                    npc(npc, HeadE.TALKING_ALOT, "Well, I can tell you, I have a valuable prisoner here in my cells")
                    npc(npc, HeadE.TALKING_ALOT, "I can expect a high reward to be paid very soon for this guy")
                    npc(npc, HeadE.SECRETIVE, "I can't tell you who he is, but he is a lot colder now")
                    exec { keliLatestPlanOptions(npc, isFirst = true) }
                }
                op("You must have trained a lot for this work") {
                    exec { keliTrainedALot(npc) }
                }
            }
            if (isFirst) {
                op("I have heard rumours that you kill people.") {
                    player(HeadE.SECRETIVE, "I have heard rumours that you kill people.")
                    npc(npc, HeadE.CALM_TALK, "There's always someone ready to spread rumours. I hear all sort of ridiculous things these days.")
                    npc(npc, HeadE.FRUSTRATED, "I heard a rumour the other day, that some men are wearing skirts")
                    npc(npc, HeadE.VERY_FRUSTRATED, "If one of my men wore a skirt, he would wish he hadn't")
                    exec { keliOriginalOptions(npc, isFirst = false) }
                }
            }
            if (isFirst) {
                op("No I have never really heard of you.") {
                    player(HeadE.CALM_TALK, "No I have never really heard of you.")
                    npc(npc, HeadE.FRUSTRATED, "You must be new to this land then")
                    npc(npc, HeadE.FRUSTRATED, "EVERYONE knows of Lady Keli and her prowess with the sword")
                    options("Choose an option:") {
                        op("No, still doesn't ring a bell") {
                            player(HeadE.CALM_TALK, "No, your name still doesn't ring a bell")
                            npc(npc, HeadE.ANGRY, "Well, you know of me now")
                            npc(npc, HeadE.FRUSTRATED, "I will ring your bell if you do not show respect")
                            options("Choose an option:") {
                                op("I do not show respect to killers and hoodlums") {
                                    player(HeadE.FRUSTRATED, "I do not show respect to killers and hoodlums")
                                    npc(npc, HeadE.FRUSTRATED, "You should, you really should")
                                    npc(npc, HeadE.SECRETIVE, "I am wealthy enough to place a bounty on your head")
                                    npc(npc, HeadE.ANGRY, "Or just remove your head myself")
                                    npc(npc, HeadE.VERY_FRUSTRATED, "Now go, I am busy, too busy to fight a would-be hoodlum")
                                }
                                op("You must have trained a lot for this work") {
                                    exec { keliTrainedALot(npc) }
                                }
                                op("I should not disturb someone as tough as you, great lady") {
                                    exec { keliGoodbye(npc) }
                                }
                            }
                        }
                        op("Yes, of course I have heard of you") {
                            player(HeadE.HAPPY_TALKING, "Heard of you? You are famous in Gielinor!")
                            npc(npc, HeadE.HAPPY_TALKING, "That's very kind of you to say. Reputations are not easily earned. I have managed to succeed where many fail.")
                            exec { keliOriginalOptions(npc, isFirst = false) }
                        }
                        op("You must have trained a lot for this work") {
                            exec { keliTrainedALot(npc) }
                        }
                        op("I should not disturb someone as tough as you") {
                            exec { keliGoodbye(npc) }
                        }
                    }
                }
            }
            if (!isFirst) {
                op("I should not disturb someone as tough as you") {
                    exec { keliGoodbye(npc) }
                }
            }
        }
    }
}

private fun Player.keliLatestPlanOptions(npc: NPC, isFirst: Boolean) {
    startConversation {
        options("Choose an option:") {
            if (isFirst) {
                op("Ah, I see. You must have been very skillful") {
                    player(HeadE.AMAZED, "You must have been very skilful")
                    npc(npc, HeadE.HAPPY_TALKING, "Yes, I did most of the work, we had to grab the Pr...")
                    npc(npc, HeadE.SKEPTICAL_THINKING, "er, we had to grab him under cover of ten of his bodyguards")
                    npc(npc, HeadE.SECRETIVE, "It was a stroke of genius")
                    exec { keliLatestPlanOptions(npc, isFirst = false) }
                }
            }
            op("Thats great, are you sure they will pay?") {
                player(HeadE.CALM_TALK, "Are you sure they will pay?")
                npc(npc, HeadE.EVIL_LAUGH, "They will pay, or we will cut his hair off and send it to them")
                player(HeadE.HAPPY_TALKING, "Don't you think that something tougher, maybe cut his finger off would work better?")
                npc(npc, HeadE.HAPPY_TALKING, "Thats a good idea. I could use talented people like you")
                npc(npc, HeadE.SKEPTICAL_THINKING, "I may call on you if I need work doing")
            }
            op("Can you be sure they will not try to get him out?") {
                player(HeadE.HAPPY_TALKING, "Can you be sure they will not try to get him out?")
                npc(npc, HeadE.CALM_TALK, "There is no way to release him. The only key to the door is on a chain around my neck")
                npc(npc, HeadE.CALM_TALK, "And the locksmith who made the lock, died suddenly when he had finished")
                npc(npc, HeadE.HAPPY_TALKING, "There is not another key like this in the world")
                options("Choose an option:") {
                    op("Could I see the key please") {
                        player(HeadE.CALM_TALK, "Could I see the key please, just for a moment")
                        player(HeadE.CALM_TALK, "It would be something I can tell my grandchildren")
                        player(HeadE.CALM_TALK, "When you are even more famous than you are now")
                        npc(npc, HeadE.HAPPY_TALKING, "When you put it that way, I am sure you can see it")
                        npc(npc, HeadE.HAPPY_TALKING, "You cannot steal the key, it is on an Adamantite chain")
                        npc(npc, HeadE.HAPPY_TALKING, "I cannot see the harm")
                        simple("Keli shows you a small key on a stronglooking chain")
                        options("Choose an option:") {
                            op("Could I touch the key for a moment please") {
                                player(HeadE.CALM_TALK, "Could I touch the key for a moment please")
                                npc(npc, HeadE.CALM_TALK, "Only for a moment then")
                                if (inventory.containsItem(PrinceAliRescue.SOFT_CLAY, 1)) {
                                    simple("(You put a piece of your soft clay in your hand)")
                                    simple("(As you touch the key, you take an imprint of it)") {
                                        inventory.deleteItem(PrinceAliRescue.SOFT_CLAY, 1)
                                        inventory.addItem(PrinceAliRescue.KEY_PRINT, 1)
                                    }
                                } else {
                                    simple("You look at the key and give it back")
                                }
                                player(HeadE.HAPPY_TALKING, "Thank you so much, you are too kind, o great Keli")
                                npc(npc, HeadE.HAPPY_TALKING, "There, run along now, I am very busy")
                            }
                            op("I should not disturb someone as tough as you") {
                                exec { keliGoodbye(npc) }
                            }
                        }
                    }
                    op("That is a good way to keep secrets") {
                        player(HeadE.HAPPY_TALKING, "That is a good way to keep secrets")
                        npc(npc, HeadE.SKEPTICAL_HEAD_SHAKE, "It is the best way I know")
                        npc(npc, HeadE.SECRETIVE, "Dead men tell no tales")
                    }
                    op("I should not disturb someone as tough as you") {
                        exec { keliGoodbye(npc) }
                    }
                }
            }
            op("I should not disturb someone as tough as you") {
                exec { keliGoodbye(npc) }
            }
        }
    }
}

private fun Player.keliTrainedALot(npc: NPC) {
    startConversation {
        player(HeadE.AMAZED_MILD, "You must have trained a lot for this work")
        npc(npc, HeadE.TALKING_ALOT, "I have used a sword since I was a small girl")
        npc(npc, HeadE.EVIL_LAUGH, "stabbed three people before I was 6 years old")
    }
}

private fun Player.keliGoodbye(npc: NPC) {
    startConversation {
        player(HeadE.SCARED, "I should not disturb someone as tough as you")
        npc(npc, HeadE.CALM_TALK, "I need to do a lot of work, goodbye")
        npc(npc, HeadE.CALM_TALK, "When you get a little tougher, maybe I will give you a job")
    }
}