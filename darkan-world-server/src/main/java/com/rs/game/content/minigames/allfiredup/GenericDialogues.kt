package com.rs.game.content.minigames.allfiredup

import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.engine.dialogue.HeadE
import com.rs.game.content.quests.allfiredup.npcs.blazeSharpeyeD
import com.rs.game.content.quests.allfiredup.npcs.SquireFyreD
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.engine.variables.firedup_whereami

private object NPCs {
    const val BLAZE = 8065
    const val FYRE = 8042
    const val OORY = 8067
    const val UBE = 8068
    const val WALDO = 8069
    const val GJALP = 8070
    const val FINTAN = 8071
    const val STUBTHUMB = 8072
    const val DORONBOL = 8073
    const val CRATE = 8074
    const val EGG = 8075
    const val NANUQ = 8076

    val ALL = arrayOf(BLAZE, FYRE, OORY, UBE, WALDO, GJALP, FINTAN, STUBTHUMB, DORONBOL, CRATE, EGG, NANUQ)
}

private const val MIN_FIREMAKING_LEVEL = 43
private const val MIN_SUMMONING_LEVEL = 81
private const val MIN_PRAYER_LEVEL = 53
private const val AMULET_OF_CATSPEAK = 4677

fun allFiredUpDialogue(player: Player, npc: NPC, headE: HeadE) {
    player.startConversation {
        npc(npc.id, headE, "Hello!")
        options {
            op("Can you tell me about the other beacons?") {
                npc(npc.id, headE, "Sure!")
                exec {
                    Beacon.getBeaconForNPC(npc.id)?.ordinal?.plus(1)?.let { current ->
                        player.vars.setVarBit(firedup_whereami, current, true)
                        player.interfaceManager.sendInterface(575, false)
                        player.packets.sendRunScript(803)
                    }
                }
            }
            op("Nevermind.") {
                player(HeadE.CALM_TALK, "Nevermind.")
            }
        }
    }
}

private fun handleBlaze(player: Player, npc: NPC) {
    if (!player.isQuestStarted(Quest.ALL_FIRED_UP)) {
        player.startConversation {
            player(HeadE.SKEPTICAL, "Hi there! Would you mind my asking what exactly it is you're doing... and what's that big contraption over there?")
            npc(npc, HeadE.CALM_TALK, "Oh, I'm afraid it's top secret, really hush-hush important stuff. I can't tell you what it's all about, but if i could...")
            npc(npc, HeadE.CALM_TALK, "I'd tell you about this amazing network of beacons we're creating to guard against the mounting threats in Morytania and the Wilderness. Oops, did I say that out loud?!")
            player(HeadE.CONFUSED, "Yes, yes you did.")
            npc(npc, HeadE.CALM_TALK, "Well, pretend you heard nothing, eh? As a favour to your old friend Blaze. Meanwhile, if you meet anyone with a particular knack for Firemaking, would you send 'em my way? I could use a hand, here.")

            options {
                op("Could I help?") {
                    player(HeadE.CALM_TALK, "Could I help?")
                    if (player.skills.getLevel(Skills.FIREMAKING) >= MIN_FIREMAKING_LEVEL) {
                        npc(npc, HeadE.HAPPY_TALKING, "You know, you look like you're pretty skilled with the old tinderbox yourself. Tell me, do you have a burning desire to set things alight?")
                        player(HeadE.CONFUSED, "Uh...maybe?")
                        npc(npc, HeadE.HAPPY_TALKING, "Well then, 'maybe' you're the ${player.genderTerm("man", "woman")} for us!")
                        player(HeadE.CONFUSED, "Um, okay...")
                        npc(npc, HeadE.HAPPY_TALKING, "If you're ever looking for a job, why don't you have a chat with King Roald in Varrock. Tell 'im I sent you, and he might just sort you out with something.")
                        player(HeadE.CALM_TALK, "Thanks for that; I'll keep it in mind.")
                    } else {
                        npc(npc, HeadE.SHAKING_HEAD, "Not until you have a bit more of a pyromanical look in your eye. Go rub some sticks together and get a bit more practice. If you still want to help after that go and talk to King Roald.")
                        simple("You need Firemaking level of at least $MIN_FIREMAKING_LEVEL to start All Fired Up.")
                    }
                }
                op("Okay, Sure.") {
                    player(HeadE.CALM_TALK, "Okay, Sure.")
                }
            }
        }
    } else {
        blazeSharpeyeD(player, npc)
    }
}

private fun checkFiredUpDialogue(player: Player, npc: NPC, dialogue: Player.() -> Unit) {
    if (!player.isQuestStarted(Quest.ALL_FIRED_UP))
        player.dialogue()
    else
        allFiredUpDialogue(player, npc, HeadE.HAPPY_TALKING)
}

@ServerStartupEvent
fun handleAllFiredUpNPCs() {
    onNpcClick(*NPCs.ALL) { e ->
        val player = e.player

        when (e.npcId) {
            NPCs.BLAZE -> handleBlaze(player, e.npc)

            NPCs.FYRE -> {
                SquireFyreD(player, e.npc)
                return@onNpcClick
            }

            NPCs.OORY -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    player(HeadE.CALM_TALK, "Hi there! What are you up to today?")
                    npc(e.npcId, HeadE.CHEERFUL, "Man, I'd just be chillin', takin' it easy, like, but there's so much to do.")
                    player(HeadE.CONFUSED, "Oh, like what?")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "Sorry, dude, I know it's a total bummer but I've got to focus right now. Catch ya later, yeah?")
                    player(HeadE.HAPPY_TALKING, "Oh sure. Good luck with everything.")
                }
            }

            NPCs.UBE -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    player(HeadE.CALM_TALK, "Hi there! What are you doing here?")
                    npc(e.npcId, HeadE.CHEERFUL, "I'm afraid I don't have much time to chat right now.")
                    player(HeadE.CONFUSED, "Really, you don't seem to be doing anything but standing around.")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "Well, that's where you eyes deceive you! I'm not only standing around, I'm also scanning the horizon.")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "It takes a lot of energy and concentration to remain prepared for emergencies.")
                    player(HeadE.HAPPY_TALKING, "I see.")
                }
            }

            NPCs.WALDO -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    player(HeadE.CALM_TALK, "So, what are you up to this fine day?")
                    npc(e.npcId, HeadE.CHEERFUL, "Oh, just tending this fire.")
                    player(HeadE.CONFUSED, "What fire? I don't see any fire.")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "Oh, it's not lit yet.")
                    player(HeadE.CONFUSED, "So, you're just standing there watching an empty fire pit?")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "Of course not. When tending a beacon it's much more important to watch your surroundings than the fire itself.")
                    player(HeadE.CONFUSED, "Is it?")
                    npc(e.npcId, HeadE.CALM_TALK, "Of course. One must be constantly vigilant for any signs of danger.")
                    npc(e.npcId, HeadE.CALM_TALK, "In fact, talking to you is distracting me a bit from my duties to the kingdom. I'm afraid I'll have to say goodbye now.")
                    player(HeadE.CALM_TALK, "Oh, right, sorry.")
                }
            }

            NPCs.GJALP -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    npc(e.npcId, HeadE.FRUSTRATED, "I busy here, no time to chat.")
                    player(HeadE.CONFUSED, "You don't look busy to me.")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "How little you understand.")
                    player(HeadE.CONFUSED, "Exactly what don't I understand, then?")
                    npc(e.npcId, HeadE.SHAKING_HEAD, "It take too long to explain.")
                    player(HeadE.CONFUSED, "Well I've got nothing urgent I need to do - tell me.")
                    npc(e.npcId, HeadE.ANGRY, "It not worth it. You annoy me; be off with you!")
                }
            }

            NPCs.FINTAN -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    player(HeadE.CONFUSED, "Hello there, what might you be up to?")
                    npc(e.npcId, HeadE.CALM_TALK, "Oh, I'm looking after this beacon for King Roald.")
                    player(HeadE.CONFUSED, "How is that working out for you?")
                    npc(e.npcId, HeadE.HAPPY_TALKING, "Oh, very well, thank you. My time away from the monastery has given me time to meditate and reflect on my path in life.")
                    player(HeadE.CALM_TALK, "That sounds very spiritually fulfilling.")
                    npc(e.npcId, HeadE.CALM_TALK, "It is, indeed. It's very satisfying to be able to look after one's spiritual needs whilst serving one's kingdom as well.")
                }
            }

            NPCs.STUBTHUMB -> checkFiredUpDialogue(player, e.npc) {
                startConversation {
                    player(HeadE.HAPPY_TALKING, "Hi there!")
                    npc(e.npcId, HeadE.FRUSTRATED, "Humph. Hi yourself.")
                    player(HeadE.SAD, "That doesn't sound very friendly.")
                    npc(e.npcId, HeadE.SAD_MILD, "It hard to be friendly when feet hurt.")
                    player(HeadE.CALM_TALK, "I take your point. Have you tried, you know, just sitting down?")
                    npc(e.npcId, HeadE.SAD_MILD, "Sitting make back hurt.")
                    player(HeadE.SKEPTICAL_THINKING, "Ah. That is a dilemma.")
                }
            }

            NPCs.DORONBOL -> {
                if (player.equipment.neckId == AMULET_OF_CATSPEAK) {
                    allFiredUpDialogue(player, e.npc, HeadE.CAT_HAPPY)
                } else {
                    player.startConversation {
                        npc(NPCs.DORONBOL, HeadE.CAT_SHOUTING, "Meow!")
                    }
                }
            }

            NPCs.NANUQ -> {
                if (player.skills.getLevel(Skills.SUMMONING) >= MIN_SUMMONING_LEVEL) {
                    allFiredUpDialogue(player, e.npc, HeadE.CAT_CHEERFUL)
                } else {
                    player.startConversation {
                        npc(NPCs.NANUQ, HeadE.CHILD_ANGRY, "Roar!")
                    }
                }
            }

            NPCs.CRATE, NPCs.EGG -> {
                allFiredUpDialogue(player, e.npc, HeadE.CHILD_HAPPY_TALK)
            }

            else -> {
                if (e.option.equals("talk-to", ignoreCase = true)) {
                    allFiredUpDialogue(player, e.npc, HeadE.HAPPY_TALKING)
                }
            }
        }
    }
}