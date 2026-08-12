// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.quests

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.World.spawnNPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mapMosul() {
    fun spawns(player: Player){
        val spawnednpc = spawnNPC(502, Tile.of(2888, 2953, 0), true, false)
        spawnednpc.forceTalk("You cannot escape me, prepare to die.")
        spawnednpc.isForceAgressive
        spawnednpc.combatTarget = player
    }
    val MOSUL_NPC_ID = 500
    onNpcClick(MOSUL_NPC_ID, options = arrayOf("Enter-village")) { (player, n) ->
        when (player.getQuestStage(Quest.SHILO_VILLAGE)) {
            0 -> player.startConversation {
                npc(n, HeadE.CALM, "Sorry Bwana, I cannot allow you enter the village at this time.")
            }
            1-> player.startConversation {
                npc(n, HeadE.CALM, "Sorry Bwana, I cannot allow you enter the village at this time.")
            }
            2-> player.startConversation {
                npc(n, HeadE.CALM, "Sorry Bwana, I cannot allow you enter the village at this time.")
                npc(n, HeadE.CALM, "But I can reset the quest stage if you'd like.")
                options("Reset Quest:Shilo Village?") {
                    op("YES"){
                        player(HeadE.HAPPY_TALKING, "Yes, please.")
                        npc(n, HeadE.HAPPY_TALKING, "OK, all done!"){
                            player.setQuestStage(Quest.SHILO_VILLAGE, 0)
                        }
                    }
                    op("NO"){
                        player(HeadE.ANGRY, "UMM. No.")
                        npc(n, HeadE.CALM_TALK, "Okay then, I wont."){
                        }
                    }
                }
            }
            9 -> {
                player.tele(2865,2955,0)
                player.sendMessage("Mosol leaves you by the gate and walks back out into the jungle.")
            }
        }
    }
    onNpcClick(MOSUL_NPC_ID, options = arrayOf("Talk-to")) { (player, n) ->
        when (player.getQuestStage(Quest.SHILO_VILLAGE)) {
            0 -> player.startConversation {
                player.sendMessage("Mosol seems to be looking around very cautiously. He jumps a little when you approach and talk to him.")
                npc(n,HeadE.SCARED,"Run! Run for your life! Save yourself! I'll keep them back as long as I can...")
                options {
                    op("Why do I need to run?") {
                        player(HeadE.WORRIED, "Why do I need to run?")
                        npc(n,HeadE.SCARED,"Your very life is in danger. Rashiliyia has returned and we are all doomed.")
                        options {
                            op("Rashiliyia? Who is she?") {
                                player(HeadE.CONFUSED, "Rashiliyia? Who is she?")
                                npc(n,HeadE.SCARED,"Rashiliyia is the Queen of the dead. She has returned and has brought a plague of undead with her. They now occupy our village and we have them trapped. I warn people like yourself to stay away.")
                                options {
                                    op("What can we do?") {
                                        label("whatcanwedo")
                                        player(HeadE.WORRIED, "What can we do?")
                                        npc(n,HeadE.WORRIED,"We're doing all we can to keep the undead at bay. The village is covered in a deadly green mist. If you go into the village, a terrible sickness will befall you. And the undead are even stronger beyond the gates.")
                                        npc(n,HeadE.WORRIED,"My guess is that this has something to do with the legend of Rashiliyia. But you need to speak to the Shaman in 'Tai Bwo Wannai' village to get more details about that. I really have to fight these undead now Bwana, before they take over the world!")
                                        label("canDo")
                                        options {
                                            op("Why are the undead here?") {
                                                player(HeadE.WORRIED, "Why are the undead here?")
                                                npc(n,HeadE.WORRIED,"Rashiliyia! The Queen of the undead has risen! She is the mother of the undead creatures that roam this land. But alas I know nothing of the legend that surrounds her.")
                                                options {
                                                    op("Legend you say?") {
                                                        player(HeadE.CONFUSED, "Legend you say?")
                                                        npc(n,HeadE.CALM_TALK,"Yes.... I said it is a legend that I know nothing about.")
                                                        options {
                                                            op("Oh, ok, sorry for bothering you.") {
                                                                label("sorry")
                                                                player(HeadE.SAD_MILD,"Oh, Ok, sorry for bothering you.")
                                                                npc(n,HeadE.CALM_TALK,"Ok, perhaps you'd like to be on your way now?")
                                                            }
                                                            op("Oh come on, you must know something.") {
                                                                player(HeadE.CONFUSED,"Oh come on, you must know something."){
                                                                    player.sendMessage("Mosol lowers his head in deep concentration.") }
                                                                npc(n, HeadE.CALM, "Well, let me think now...") {
                                                                    player.sendMessage("He scratches his head...") }
                                                                npc(n,HeadE.CALM_TALK,"Hmmm, there was something I think that might help..."){
                                                                    player.sendMessage("Mosol strains to remember something...") }
                                                                npc(n, HeadE.CALM_TALK, "Nope, sorry. It's gone.")
                                                                options {
                                                                    ops("Maybe you know someone who does know something?") {
                                                                        label("knowSomeone")
                                                                        player(HeadE.CONFUSED,"Maybe you know someone who does know something?")
                                                                        npc(n,HeadE.WORRIED,"My guess is that this has something to do with the legend of Rashiliyia. But you need to speak to the Shaman in 'Tai Bwo Wannai' village to get more details about that. I really have to fight these undead now Bwana, before they take over the world!")
                                                                        options {
                                                                            ops("Oh, ok, sorry for bothering you.") {
                                                                                player(HeadE.SAD_MILD,"Oh, Ok, sorry for bothering you.")
                                                                                npc(n,HeadE.CALM_TALK,"Ok, perhaps you'd like to be on your way now?")
                                                                            }
                                                                            ops("I'll go to see the Shaman.") {
                                                                                player(HeadE.CALM_TALK, "I'll go to see the Shaman.")
                                                                                npc(n,HeadE.CALM_TALK,"Well, that would be helpful Bwana. If you're sure you want to go, you can take a Wampum belt to him for me. It will give the Shaman, Trufitus the story of our problems down here. Are you sure you want to go?")
                                                                                options {
                                                                                    op("Not Right Now") {
                                                                                        player(HeadE.CALM_TALK,"Errr, I'm having second thoughts now.")
                                                                                        npc(n,HeadE.CALM_TALK,"That's Ok Bwana, it's a big responsibility. Come back and see me if you change your mind.")
                                                                                    }
                                                                                    op("Accept Quest") {
                                                                                        player(HeadE.CALM_TALK,"Yes, I'm sure and I'll take the Wampum belt to Trufitus.")
                                                                                        npc(n,HeadE.CALM_TALK,"I would be very grateful if you did. Here take this...") {
                                                                                            exec {
                                                                                                if (player.inventory.hasFreeSlots()) {
                                                                                                    player.sendMessage("Mosul hands you the Wampum belt.")
                                                                                                    player.inventory.addItem(
                                                                                                        625,
                                                                                                        1,
                                                                                                        true
                                                                                                    )
                                                                                                    player.setQuestStage(
                                                                                                        Quest.SHILO_VILLAGE,
                                                                                                        1
                                                                                                    )
                                                                                                } else {
                                                                                                    player.sendMessage("You do not have a free inventory space for the Wampum belt.")
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            ops("Ok. Thanks for your help.") {
                                                                                player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                                                                npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                                                                            }
                                                                        }
                                                                    }
                                                                    ops("Oh, Ok, sorry for bothering you.") {
                                                                        player(HeadE.SAD_MILD,"Oh, Ok, sorry for bothering you.")
                                                                        npc(n,HeadE.CALM_TALK,"Ok, perhaps you'd like to be on your way now?")
                                                                    }
                                                                    ops("Ok. Thanks for your help.") {
                                                                        player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                                                        npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                                                                    }
                                                                }
                                                            }
                                                            op("Maybe you know someone who does know something?") {
                                                                goto("knowSomeone")
                                                            }
                                                            op("Ok. Thanks for your help.") {
                                                                player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                                                npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            op("Can I help in any way?") {
                                                player(HeadE.WORRIED, "Can I help in any way?")
                                                npc(n,HeadE.WORRIED,"I don't think so Bwana, but you may want to consult with Trufitus the Shaman in Tai Bwo Wannai village, he may have some information which could help.")
                                                goto("canDo")
                                            }
                                            op("I'll go to see the Shaman.") {
                                                label("shaman")
                                                player(HeadE.CALM_TALK, "I'll go to see the Shaman.")
                                                npc(n,HeadE.CALM_TALK,"Well, that would be helpful Bwana. If you're sure you want to go, you can take a Wampum belt to him for me. It will give the Shaman, Trufitus the story of our problems down here. Are you sure you want to go?")
                                                options {
                                                    op("Not Right Now") {
                                                        player(HeadE.CALM_TALK,"Errr, I'm having second thoughts now.")
                                                        npc(n,HeadE.CALM_TALK,"That's Ok Bwana, it's a big responsibility. Come back and see me if you change your mind.")
                                                    }
                                                    op("Accept Quest") {
                                                        player(HeadE.CALM_TALK,"Yes, I'm sure and I'll take the Wampum belt to Trufitus.")
                                                        npc(n,HeadE.CALM_TALK,"I would be very grateful if you did. Here take this...") {
                                                            if (player.inventory.hasFreeSlots()) {
                                                                player.sendMessage("Mosul hands you the Wampum belt.")
                                                                player.inventory.addItem(625, 1, true)
                                                                player.setQuestStage(Quest.SHILO_VILLAGE, 1)
                                                            } else {
                                                                player.sendMessage("You do not have a free inventory space for the Wampum belt.")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            op("Ok. Thanks for your help") {
                                                player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                                npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                                            }
                                        }
                                    }
                                    op("Uh, it sounds nasty, just the kind of thing I want to avoid.") {
                                        player(HeadE.WORRIED,"Uh, it sounds nasty, just the kind of thing I want to avoid!")
                                        npc(n,HeadE.WORRIED,"Quite right, Bwana, please make all haste! Before your spine turns to water as we speak.")
                                    }
                                    op("Ok. Thanks for your help") {
                                        player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                        npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                                    }
                                }
                            }
                            op("What danger is there around here?") {
                                player(HeadE.WORRIED, "What danger is there around here?")
                                npc(n,HeadE.WORRIED,"Can you not see Bwana? This whole area is infested with the living dead."){
                                    spawns(player)
                                }
                                npc(n, HeadE.WORRIED, "Arrggghhh, here are some now!")

                            }
                            op("Ok. Thanks for your help") {
                                player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                                npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                            }
                        }
                    }
                    op("Yeah... Ok, I'm running!") {
                        player(HeadE.SCARED, "Yeah... Ok, I'm running!")
                        npc(n, HeadE.CALM_TALK, "God speed to you my friend.")
                    }
                    op("Who are you?") {
                        player(HeadE.CONFUSED, "Who are you?")
                        npc(n,HeadE.CALM_TALK,"I am Mosol Rei, I am a Jungle Warrior. I used to live in this village, but it is too dangerous for you to stay around here.")
                        options {
                            op("Mosol Rei, that's a nice name.") {
                                player(HeadE.HAPPY_TALKING, "Mosol Rei, that's a nice name.") {
                                    player.sendMessage("Mosol looks at you and shakes his head in bewilderment.")
                                }
                                npc(n, HeadE.SHAKING_HEAD, "Thanks, but you really should leave.")
                            }
                            op("What danger is there around here?") {
                                player(HeadE.WORRIED, "What danger is there around here?")
                                npc(n,HeadE.WORRIED,"Can you not see Bwana? This whole area is infested with the living dead."){
                                    spawns(player)
                                }
                                npc(n, HeadE.WORRIED, "Arrggghhh, here are some now!")
                            }
                            op("Ok. Thanks for your help.") {
                                goto("okEnd")
                            }
                        }
                    }
                    op("Ok. Thanks for your help.") {
                        label("okEnd")
                        player(HeadE.SAD_MILD, "Ok. Thanks for your help.")
                        npc(n, HeadE.CALM_TALK, "You're welcome Bwana, good luck.")
                    }
                }
            }
            1 -> player.startConversation {
                npc(n, HeadE.CALM_TALK, "Hey there Bwana, have you delivered that Wampum Belt to Trufitus yet?")
                options("Select an option") {
                    op("Yes, of course!"){
                        player(HeadE.HAPPY_TALKING, "Yes, of course!")
                        npc(n, HeadE.CALM_TALK, "Well hopefully Trufitus will come up with some good ideas on how to resolve this problem with Rashiliyia. You should talk to him if you really want to help.")
                    }
                    if(!player.inventory.containsItem(625) && !player.bank.containsItem(625)) {
                        op("I've, lost the belt.") {
                            player(HeadE.SAD_MILD_LOOK_DOWN, "I've, lost the belt.")
                            npc(n,HeadE.CALM_TALK,"One of my scouts found it while out exploring. Hopefully you keep a closer eye on this one. They don't grow on trees you know."
                            )
                            item(625, "Mosul hands you back the Wampum belt.")
                            exec {
                                player.inventory.addItem(625, 1, true)
                            }
                        }
                    }
                    op("Not yet I've been busy."){
                        player(HeadE.HAPPY_TALKING, "Not yet I've been busy.")
                        npc(n, HeadE.CALM_TALK, "If you can take it to him quickly, it would help me out a lot. If not, please can you give it back to me so that I can send a scout to deliver it.")
                    }
                }
            }
            9 -> player.startConversation {
                player(HeadE.HAPPY_TALKING, "Greetings!")
                npc(n, HeadE.CALM_TALK, "Greetings Bwana! We've removed the threat of Rashiliyia and though there are still some random outbreaks of undead activity we are more than able to deal with it. You're welcome to enter the village now Bwana, shall I show you the way?")
                options("Enter Shilo Village?"){
                    op("Yes, Ok, I'll go into the village"){
                        simple("Molsul leads you into the village.")
                        exec{
                            player.tele(2865,2955,0)
                            player.sendMessage("Mosol leaves you by the gate and walks back out into the jungle.")
                        }
                    }
                    op("I think I'll see it some other time."){
                        exec{player.sendMessage("You decide not to visit the village.")}
                    }
                }

            }
        }
    }
}
