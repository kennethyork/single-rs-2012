package com.rs.game.content.world.areas.port_phasmatys.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.achievements.AchievementSystemD
import com.rs.game.content.achievements.SetReward
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.content.world.areas.port_phasmatys.RuneDrawInvite
import com.rs.game.content.world.areas.port_phasmatys.startRuneDraw
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player

private const val oakLongbowS = 4236
private const val BEDSHEET = 4284

@ServerStartupEvent
fun mapRobin() {
    onNpcClick(1694) { e ->
        if(e.player.isQuestComplete(Quest.GHOSTS_AHOY))
            postQuest(e.player, e.npc)

        if(!e.player.isQuestStarted(Quest.GHOSTS_AHOY) || e.player.getQuestStage(Quest.GHOSTS_AHOY) <= GhostsAhoy.STAGE_4_SEEK_OLD_WOMAN)
            preQuest(e.player, e.npc)

        if(e.player.getQuestStage(Quest.GHOSTS_AHOY) == GhostsAhoy.STAGE_5_GATHER_ITEMS) {
            if (e.player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("SPOKE_WITH_AKHARANU"))
                questDialogue(e.player, e.npc)
            else
                preQuest(e.player, e.npc)
        }
        else
            postQuest(e.player, e.npc)

    }
}

private fun questDialogue(player: Player, npc: NPC){
    if(!player.inventory.containsItem(845) && player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") != 5) {
        player.playerDialogue(HeadE.SKEPTICAL_THINKING, "I should get a longbow for Robin to sign before talking to him.")
        return
    }
    val owed = when (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins")) {
        1 -> "25"
        2 -> "50"
        3 -> "75"
        else -> "0"
    }
    if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") >= 5) {
        player.startConversation {
            player(HeadE.CALM_TALK, "So, still here then.")
            npc(npc, HeadE.CALM_TALK, "Do you want another game of Runedraw?")
            player(HeadE.CALM_TALK, "You have a serious gambling problem, you know.")
        }
        return
    }
    if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") == 4) {
        player.startConversation {
            player(HeadE.CALM_TALK, "I've had enough of you not paying up - you owe me 100 gold coins. I'm going to tell the ghosts what you're doing.")
            npc(npc, HeadE.CALM_TALK, "Please don't do that!!! They will suck the life from my bones!!!")
            player(HeadE.CALM_TALK, "How about you signing my longbow then?")
            npc(npc, HeadE.CALM_TALK, "Yes, anything!!!")
            item(oakLongbowS,"Robin signs the oak longbow for you.")
            exec {
                player.inventory.deleteItem(845, 1)
                player.inventory.addItem(oakLongbowS)
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setI("RobinWins", 5)
            }
        }
        return
    }
    if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") >= 1 && player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("RobinWinLastRound")) {
        player.startConversation {
            player(HeadE.CALM_TALK, "Well done, you beat me. But you still owe me $owed gold pieces.")
            npc(npc, HeadE.CALM_TALK, "Err... how about another game? We can settle up after we've finished playing.")
            options {
                op("Yes, I'll give you a game."){
                    exec{
                        startRuneDraw(player, npc) }
                }
                op("How do you play Runedraw?") {
                    npc(npc, HeadE.CALM_TALK, "Two players take turns to draw a rune from a bag, which contains ten runes in total. Each rune has a different value: an air rune is worth one point, up to a Nature rune which is worth nine points. If a player draws the Death rune then the game is over and they have lost. A player can choose to hold if they wish and not draw any more runes, but this runs the risk of the other player drawing more runes until they have a greater points total and win.")
                    goto("Init")
                }
                op("No, I don't approve of gambling.") {
                    player(HeadE.CALM_TALK,"No, you didn't pay up the last time you lost.")
                }
            }
        }
        return
    }
    if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") >= 1 && !player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("RobinWinLastRound")) {
        player.startConversation {
            player(HeadE.CALM_TALK, "So are you going to pay up then? You owe me $owed gold coins!")
            npc(npc, HeadE.CALM_TALK, "How about another game? I'll pay you back with the winnings.")
            player(HeadE.CALM_TALK, "What if you lose again?")
            npc(npc, HeadE.CALM_TALK, "Er... we'll deal with that when we come to it.")
            options {
                op("Yes, I'll give you a game."){
                    exec{
                        startRuneDraw(player, npc) }
                }
                op("How do you play Runedraw?") {
                    npc(npc, HeadE.CALM_TALK, "Two players take turns to draw a rune from a bag, which contains ten runes in total. Each rune has a different value: an air rune is worth one point, up to a Nature rune which is worth nine points. If a player draws the Death rune then the game is over and they have lost. A player can choose to hold if they wish and not draw any more runes, but this runs the risk of the other player drawing more runes until they have a greater points total and win.")
                    goto("Init")
                }
                op("No, I don't approve of gambling.") {
                    player(HeadE.CALM_TALK,"No, you didn't pay up the last time you lost.")
                }
            }
        }
        return
    }
    if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") == 0)
        player.startConversation {
            player(HeadE.CALM_TALK, "Would you sign this oak longbow for me?")
            npc(npc, HeadE.CALM_TALK, "I'm sorry, I don't sign autographs. While you're here though, why don't you have a game of Runedraw with me? If you've got 25 gold pieces I've got a bag of runes we can use.")
            label("Init")
            options {
                op("Yes, I'll give you a game."){
                    exec{
                        player.inventory.removeCoins(25)
                        startRuneDraw(player, npc) }
                }
                op("How do you play Runedraw?") {
                    npc(npc, HeadE.CALM_TALK, "Two players take turns to draw a rune from a bag, which contains ten runes in total. Each rune has a different value: an air rune is worth one point, up to a Nature rune which is worth nine points.")
                    npc(npc, HeadE.CALM_TALK, "If a player draws the Death rune then the game is over and they have lost. A player can choose to hold if they wish and not draw any more runes, but this runs the risk of the other player drawing more runes until they have a greater points total and win.")
                    goto("Init")
                }
                op("No, I don't approve of gambling.") {
                    player(HeadE.CALM_TALK,"No, I don't approve of gambling.")
                }
            }
        }
}

private fun preQuest(player: Player, npc: NPC) {
    player.startConversation {
        options {
            op("About the Achievement System...") {
                exec { AchievementSystemD(player, npc.id, SetReward.MORYTANIA_LEGS) }
            }
            op("Talk about something else.") {
                if(!player.inventory.containsItem(BEDSHEET)) {
                    player(HeadE.CALM_TALK, "It's nice to see another human face around here.")
                    npc(npc, HeadE.CALM_TALK, "Leave me be, peasant - I am relaxing.")
                    player(HeadE.CALM_TALK, "Well, that's nice!")
                    npc(npc, HeadE.CALM_TALK, "Do you know who I am?")
                    player(HeadE.CALM_TALK, "I'm sorry, I haven't had the privilege.")
                    npc(npc, HeadE.CALM_TALK, "I, peasant, am Robin, Master Bowman. I am very famous you know.")
                    player(HeadE.CALM_TALK, "Oh, Robin, Master Bowman, I see.")
                    npc(npc, HeadE.CALM_TALK, "So have you heard of me?")
                    player(HeadE.CALM_TALK, "No.")
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Would you do me a favour? I appear to have run out of bed linen. Can you run along to the innkeeper and get me a clean bedsheet?"
                    )

                    options {
                        op("Anything for a famous person such as yourself.") {
                            player(HeadE.CALM_TALK, "Anything for a famous person such as yourself.")
                            npc(npc, HeadE.CALM_TALK, "Now that sounds more like it. Run along now, get me my sheet.")
                        }
                        op("Go and get it yourself.") {
                            player(HeadE.CALM_TALK, "Go and get it yourself.")
                            npc(npc, HeadE.CALM_TALK, "Oh charming. You just can't get the staff these days.")
                        }
                    }
                }
                else {
                    player(HeadE.CALM_TALK, "I've brought you a clean bedsheet, Robin.")
                    exec { player.inventory.deleteItem(BEDSHEET, 1)}
                    npc(npc, HeadE.CALM_TALK, "Well it's about time. Run along now, I need some Robin time.")
                }
            }
        }
    }
}

private fun postQuest(player: Player, npc: NPC) {
    player.startConversation {
        options {
            op("About the Achievement System...") {
                exec { AchievementSystemD(player, npc.id, SetReward.MORYTANIA_LEGS) }
            }
            op("Do you fancy a game of Runedraw?") {
                exec { RuneDrawInvite.npcInviteToPlay(player, npc) }
            }
        }
    }
}