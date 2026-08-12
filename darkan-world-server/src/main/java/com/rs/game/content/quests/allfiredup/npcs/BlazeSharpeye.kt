package com.rs.game.content.quests.allfiredup.npcs

import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.engine.dialogue.HeadE
import com.rs.game.content.minigames.allfiredup.allFiredUpDialogue
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills

private const val Blaze = 8065

fun blazeSharpeyeD(player: Player, npc: NPC) {
    if (!player.isQuestStarted(Quest.ALL_FIRED_UP)) {
        allFiredUpDialogue(player, npc, HeadE.HAPPY_TALKING)
    }

    if (player.questManager.isComplete(Quest.ALL_FIRED_UP)) {
        allFiredUpDialogue(player, npc, HeadE.HAPPY_TALKING)
    }

    when (player.questManager.getStage(Quest.ALL_FIRED_UP)) {
        0 -> stage0(player)
        1 -> stage1(player)
        2 -> stage2(player)
        3 -> stage3(player)
        4 -> stage4(player)
        5 -> stage5(player)
        6 -> stage6(player)
    }

}

private fun stage0(player: Player) {
        player.startConversation {
            player(
                HeadE.SKEPTICAL,
                "Hi there! Would you mind my asking what exactly it is you're doing... and what's that big contraption over there?"
            )
            npc(
                Blaze,
                HeadE.CALM_TALK,
                "Oh, I'm afraid it's top secret, really hush-hush important stuff. I can't tell you what it's all about, but if i could..."
            )
            npc(
                Blaze,
                HeadE.CALM_TALK,
                "I'd tell you about this amazing network of beacons we're creating to guard against the mounting threats in Morytania and the Wilderness. Oops, did I say that out loud?!"
            )
            player(HeadE.CONFUSED, "Yes, yes you did.")
            npc(
                Blaze,
                HeadE.CALM_TALK,
                "Well, pretend you heard nothing, eh? As a favour to your old friend Blaze. Meanwhile, if you meet anyone with a particular knack for Firemaking, would you send 'em my way? I could use a hand, here. "
            )
            options {
                if (player.skills.getLevel(Skills.FIREMAKING) <= 42)
                    op("Could I help?") {
                        player(HeadE.CALM_TALK, "Could I help?")
                        npc(
                            Blaze,
                            HeadE.SHAKING_HEAD,
                            "Not until you have a bit more of a pyromanical look in your eye. Go rub some sticks together and get a bit more practice. If you still want to help after that go and talk to King Roald."
                        )
                        simple("You need Firemaking level of at least 43 to start All Fired Up.")
                    }
                if (player.skills.getLevel(Skills.FIREMAKING) >= 43)
                    op("Could I help?") {
                        player(HeadE.CALM_TALK, "Could I help?")
                        npc(Blaze, HeadE.HAPPY_TALKING, "You know, you look lik you're pretty skilled with the old tinderbox yourself. Tell me, do you have a burning desire to set things alight?")
                        player(HeadE.CONFUSED, "Uh...maybe?")
                        npc(Blaze, HeadE.HAPPY_TALKING, "Well then, 'maybe' you're the ${player.genderTerm("man", "woman")} for us!")
                        player(HeadE.CONFUSED, "Um, okay...")
                        npc(Blaze, HeadE.HAPPY_TALKING, "If you're ever looking for a job, why don't you have a chat with King Roald in Varrock. Tell 'im I sent you, and he might just sort you out with something.")
                        player(HeadE.CALM_TALK, "Thanks for that; I'll keep it in mind.")
                    }
                op("Okay, Sure.") {
                    player(HeadE.CALM_TALK, "Okay, Sure.")
                }
                return@options
            }
        }
    }

private fun stage1(player: Player) {
        player.startConversation {
            player(
                HeadE.HAPPY_TALKING,
                "Hi there! King Roald sent me - he said you could use a bit of help testing out this network of beacons."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Indeed? This is excellent news! We desperately need someone to help us out!"
            )
            player(HeadE.HAPPY_TALKING, "So, what exactly is going on?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Well, King Roald has commissioned these lovely beacons to serve as an early warning system."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "We'll be able to see their huge, majestic flames from great distances. There are fourteen beacons in total, spanning the south and west borders of the Wilderness."
            )
            player(
                HeadE.HAPPY_TALKING,
                "It sounds like you've already got everything figured out. How do I fit into this?"
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Well, to test these beacons, we need someone to light a couple of them for us, so that we can determine whether or not they'll actually be visible."
            )
            player(
                HeadE.HAPPY_TALKING,
                "You mean, you went out and built these huge things without knowing that they'd work?"
            )
            npc(
                Blaze,
                HeadE.VERY_FRUSTRATED,
                "Don't interrupt. Now, yours truly has recruited some of the sharpest eyes this side of the River Salve to look after these beacons and to watch the horizon for signs of danger."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "In case of emergency, we've got a supply of logs and can start a fire faster than a cat can sneeze."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "As soon as we spot a threat from the Wilderness or the glow from another beacon, we can light our own beacons and send a warning right across Misthalin, faster than any courier could even run."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "We've agreed to use gnomish firelighters to colour the flames of our beacons' fires in an actual emergency, so it's safe to test the network with normal fires."
            )
            player(HeadE.HAPPY_TALKING, "So, what do you want me to actually do?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "I need you to light a couple of beacons for me to make sure we can see the glow from the flames over the horizon. So! First of all, you need to know how to light a beacon. Our technique is super-secret, but quite effective."
            )
            npc(
                Blaze,
                HeadE.LOSING_IT_LAUGHING,
                "All you do is put twenty logs of the same type on a beacon and... SET IT ON FIRE WITH A TINDERBOX!"
            )
            player(HeadE.HAPPY_TALKING, "You really enjoy your job, don't you?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Yes. Yes I do. Now, why don't you go over there and try lighting that beacon. Show us what you've got."
            ) { player.questManager.setStage(Quest.ALL_FIRED_UP, 2) }
            options {
                op("Does it matter what type of logs I use?") {
                    player(HeadE.HAPPY_TALKING, "Does it matter what type of log I use?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Good question! Once you become more experienced at tending the beacons, you'll find that higher-level logs burn for a longer time."
                    )
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "However for the purpose of this training, it doesn't make any difference at all - you might as well use normal logs."
                    )
                }
                op("Okay.") {
                    player(HeadE.HAPPY_TALKING, "Okay.")
                }
            }
        }
    }

private fun stage2(player: Player) {
        player.startConversation {
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Hmm. You're talking to me, and yet, the beacon remains unlit. Odd..."
            )
            options {
                op("I haven't got around to it yet.") {
                    player(HeadE.HAPPY_TALKING, "I haven't got around to it yet.")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Well, hurry, " + player.displayName + ". Let the fire of inspiration ignite your soul!"
                    )
                    player(HeadE.HAPPY_TALKING, "Uh, right. Will do.")
                }
                op("Where is this beacon?") {
                    player(HeadE.HAPPY_TALKING, "Where is this beacon?")
                    npc(
                        Blaze,
                        HeadE.VERY_FRUSTRATED,
                        "Right in front of you, " + player.getPronoun(
                            "man",
                            "woman"
                        ) + "! Can you not see that big structure right next to me?"
                    )
                    player(HeadE.HAPPY_TALKING, "Oh, *that* beacon!")
                }
                op("Could you remind me how to light the beacon?") {
                    player(HeadE.HAPPY_TALKING, "Could you remind me how to light the beacon?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "But of course. All you need to do is load up the beacon with twenty logs. It doesn't matter which you use, any standard log will do."
                    )
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Then comes the best part - you take your tinderbox and...and..and..."
                    )
                    player(HeadE.HAPPY_TALKING, "And?")
                    npc(
                        Blaze,
                        HeadE.LOSING_IT_LAUGHING,
                        "...AND YOU SET IT ON FIRE! Mwahahahaha!"
                    )
                    player(HeadE.HAPPY_TALKING, "Okay, okay, already! Calm down.")
                    npc(Blaze, HeadE.LOSING_IT_LAUGHING, "FIRE!")
                    player(HeadE.CONFUSED, "Yes, yes! I get the point.")
                }
            }
        }
    }

private fun stage3(player: Player) {
        player.startConversation {
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Ah, I see you've managed to successfully light the beacon and have mastered our super-secret beacon lighting technique. Well done!"
            )
            player(HeadE.HAPPY_TALKING, "Well, it's not like it was all that complicated.")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Well, apparently, not for someone of your Firemaking caliber and expertise. Now that you've got the hang of things, we can get this show on the road."
            )
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "If you'd be so kind as to light the beacon to the west and report back to me, I can make sure I can clearly see its glow on the horizon."
            )
            player(HeadE.HAPPY_TALKING, "Where can I find this beacon?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "It's near the limestone quarry, north-east of Varrock, west of the Rag and Bone Man's hovel. My colleague, Squire Fyre, is tending that beacon."
            )
            npc(Blaze, HeadE.HAPPY_TALKING, "She'll help you out if you run into any trouble.")
            player(HeadE.HAPPY_TALKING, "Okay.")
        }
    }

private fun stage4(player: Player) {
        player.startConversation {
            npc(
                Blaze,
                HeadE.VERY_FRUSTRATED,
                "You're back, but I don't see a beacon blazing on the horizon! Did you light it?"
            )
            player(HeadE.HAPPY_TALKING, "Um, not exactly")
            npc(
                Blaze,
                HeadE.LOSING_IT_LAUGHING,
                "Well, hurry, " + player.displayName + ". Let the fire of inspiration ignite your soul!"
            )
        }
    }

private fun stage5(player: Player) {
        player.startConversation {
            player(HeadE.HAPPY_TALKING, "I've added some logs to the beacon; it's now blazing away.")
            npc(Blaze, HeadE.AMAZED, "Beautiful...mesmerizing...")
            player(HeadE.HAPPY_TALKING, "Uh..Blaze?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "Ah, yes, it's you again. Brilliant work; an incandescent effort."
            )
            player(HeadE.HAPPY_TALKING, "So, what now?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "We're all pretty pleased with the results of the trial run and are gearing up for the more serious testing. There's a lot at stake, so we'll need to test out the entire network as soon as possible."
            )
            options {
                op("When's all this to take place?") {
                    player(HeadE.HAPPY_TALKING, "When's all this to take place?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Imminently, I'm sure - we're just waiting for the word from King Roald. Speaking of which, have you reported back to him about the progress we've made?"
                    )
                    player(HeadE.HAPPY_TALKING, "Not yet, I'm afraid.")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Well, what are you waiting for? This is a serious matter! I'm sure King Roald is on the edge of his throne, waiting for the news."
                    )
                    player(
                        HeadE.HAPPY_TALKING, "I'll get right on that."
                    )
                    exec {
                        player.questManager.setStage(Quest.ALL_FIRED_UP, 6)
                    }
                }
                op("More serious testing?") {
                    player(HeadE.HAPPY_TALKING, "More serious testing?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "But, of course! We now know that the beacons can be clearly seen from afar, but we still need to test the entire network to see if it works."
                    )
                    player(HeadE.HAPPY_TALKING, "What does that involve?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Well, it's quite simple. We'll need someone who's motivated, capable and quick-thinking to light each of the fourteen beacons. Only when we've seen all fourteen beacons burning simultaneously will we know that we've succeeded."
                    )
                    player(HeadE.HAPPY_TALKING, "That sounds like quite a challenge!")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Oh, it is! Many of the other beacons are harder to light than these, and some are in some rather dangerous locations. The rewards, however, are great. King Roald is prepared to give generously in return for help and, of course, the most amazing award is..."
                    )
                    player(HeadE.HAPPY_TALKING, "Yes?")
                    npc(Blaze, HeadE.HAPPY_TALKING, "YOU GET TO SET MORE FIRES!")
                    player(HeadE.HAPPY_TALKING, "Ah, but of course.")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "What are you waiting for? Hurry back to King Roald and tell him that we've completed this first test of the beacon network! Then come back to me and we can talk about... SETTING SOME MORE FIRES! For the safety and security of Misthalin and Asgarnia, of course - it's a noble and selfless cause."
                    )
                    player(
                        HeadE.HAPPY_TALKING, "Of course."
                    ) { player.questManager.setStage(Quest.ALL_FIRED_UP, 6) }
                }
            }

        }
    }

private fun stage6(player: Player) {
        player.startConversation {
            player(HeadE.HAPPY_TALKING, "Hi there. How are things going?")
            npc(Blaze, HeadE.HAPPY_TALKING, "Pretty spectacular, thanks.")
            player(HeadE.HAPPY_TALKING, "So, what now?")
            npc(
                Blaze,
                HeadE.HAPPY_TALKING,
                "We're all pretty pleased with the results of the trial run and are gearing up for the more serious testing. There's a lot at stake, so we'll need to test out the entire network as soon as possible."
            )
            options {
                op("When's all this to take place?") {
                    player(HeadE.HAPPY_TALKING, "When's all this to take place?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Imminently, I'm sure - we're just waiting for the word from King Roald. Speaking of which, have you reported back to him about the progress we've made?"
                    )
                    player(HeadE.HAPPY_TALKING, "Not yet, I'm afraid.")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Well, what are you waiting for? This is a serious matter! I'm sure King Roald is on the edge of his throne, waiting for the news."
                    )
                    player(HeadE.HAPPY_TALKING, "I'll get right on that.")
                }
                op("More serious testing?") {
                    player(HeadE.HAPPY_TALKING, "More serious testing?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "But, of course! We now know that the beacons can be clearly seen from afar, but we still need to test the entire network to see if it works."
                    )
                    player(HeadE.HAPPY_TALKING, "What does that involve?")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Well, it's quite simple. We'll need someone who's motivated, capable and quick-thinking to light each of the fourteen beacons. Only when we've seen all fourteen beacons burning simultaneously will we know that we've succeeded."
                    )
                    player(HeadE.HAPPY_TALKING, "That sounds like quite a challenge!")
                    npc(
                        Blaze,
                        HeadE.HAPPY_TALKING,
                        "Oh, it is! Many of the other beacons are harder to light than these, and some are in some rather dangerous locations. The rewards, however, are great. King Roald is prepared to give generously in return for help and, of course, the most amazing award is..."
                    )
                    player(HeadE.HAPPY_TALKING, "Yes?")
                    npc(Blaze, HeadE.LOSING_IT_LAUGHING, "YOU GET TO SET MORE FIRES!")
                    player(HeadE.HAPPY_TALKING, "Ah, but of course.")
                    npc(
                        Blaze,
                        HeadE.LOSING_IT_LAUGHING,
                        "What are you waiting for? Hurry back to King Roald and tell him that we've completed this first test of the beacon network! Then come back to me and we can talk about... SETTING SOME MORE FIRES! For the safety and security of Misthalin and Asgarnia, of course - it's a noble and selfless cause."
                    )
                    player(HeadE.HAPPY_TALKING, "Of course.")
                }
            }
        }
    }
