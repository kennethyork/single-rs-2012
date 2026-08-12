package com.rs.game.content.quests.ghosts_ahoy.npcs;

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.SailColorManager
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player;
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

class OldMan(p: Player, npc: NPC) {
    init {
        if(p.isQuestComplete(Quest.GHOSTS_AHOY))
            dialogueStageFinal(p, npc)
        if (!p.isQuestStarted(Quest.GHOSTS_AHOY))
            defaultResponses(p, npc)
        else
        when (p.getQuestStage(Quest.GHOSTS_AHOY)) {
            in 1..4 -> defaultResponses(p, npc)
            5 -> dialogueStage5(p, npc)
            else -> dialogueStageFinal(p, npc)
        }
    }
}

private fun defaultResponses(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "What are you doing on this shipwreck?")
        npc(npc, HeadE.CALM_TALK, "Shipwreck?!? Not shipwreck, surely not! Just in port, that's all.")
        player(HeadE.CALM_TALK, "Don't be silly - half the ship's missing!")
        npc(npc, HeadE.CALM_TALK, "No no no - the captain's just waiting for the wind to change, then we're off!")
        player(HeadE.CALM_TALK, "You mean the skeleton sitting here in this chair?")
        npc(npc, HeadE.CALM_TALK, "You must show more respect to the captain")
    }
}

private fun dialogueStage5(p: Player, npc: NPC) {
    val sailColors = SailColorManager.getAllSailColors(p)
    val sailColour1 = sailColors["sailColour1"]
    val sailColour1Player = sailColors["sailColour1Player"]
    val sailColour2 = sailColors["sailColour2"]
    val sailColour2Player = sailColors["sailColour2Player"]
    val sailColour3 = sailColors["sailColour3"]
    val sailColour3Player = sailColors["sailColour3Player"]
    val allMatch = sailColour1 == sailColour1Player && sailColour2 == sailColour2Player && sailColour3 == sailColour3Player
        p.startConversation {
            label("initialOptions")
            options {
                op("What is your name?") {
                    player(HeadE.CALM_TALK, "What is your name?")
                    npc(npc, HeadE.SHAKING_HEAD, "I don't remember. Everyone around here just calls me 'boy'.")
                    player(HeadE.SCARED, "You're the cabin boy?!?")
                    npc(npc, HeadE.FRUSTRATED, "Yes, and proud of it.")
                    goto("initialOptions")
                }
                op("Can I have the key to the chest?") {
                    player(HeadE.CALM_TALK, "Can I have the key to the chest?")
                    npc(npc, HeadE.SHAKING_HEAD, "Hang on, let me ask the Captain...")
                    simple("The old man cocks an ear towards the Pirate Captain's skeleton.")
                    npc(npc, HeadE.SHAKING_HEAD, "The Captain says no.")
                    goto("initialOptions")

                }
                if(p.inventory.containsOneItem(4253, 4254))
                    op("Is this your toy boat?") {
                        simple("The old man inspects the toy boat.")
                        if(p.inventory.containsItems(4253)) {
                            npc(
                                npc,
                                HeadE.SHAKING_HEAD,
                                "No - I made a toy boat a long while ago, but that one had a flag."
                            )
                            return@op
                        }
                        else if(p.inventory.containsItems(4254) && !allMatch) {
                            npc(
                                npc,
                                HeadE.SHAKING_HEAD,
                                "No - I made a toy boat a long while ago, but the colours on the flag were different."
                            )
                            return@op
                        }
                        else 
                            npc(npc, HeadE.CALM_TALK, "My word - so it is! I never thought I would see it again! Where did you get it from?")
                        player(HeadE.CALM_TALK, "Your mother gave it to me to pass on to you.")
                        npc(npc, HeadE.CALM_TALK, "My mother? She still lives?")
                        player(HeadE.CALM_TALK, "Yes, in a shack to the west of here.")
                        npc(npc, HeadE.CALM_TALK, "After all these years...")
                        player(HeadE.CALM_TALK, "Can I have the key to the chest, then?")
                        npc(npc, HeadE.CALM_TALK, "Hang on, let me ask the Captain...")
                        simple("The old man cocks an ear towards the Pirate Captain's skeleton.")
                        npc(npc, HeadE.CALM_TALK, "The Captain says yes.")
                        exec {
                            if(!p.inventory.hasFreeSlots()) {
                                p.sendMessage("You don't have the space in your inventory.")
                                return@exec
                            }
                            p.inventory.deleteItem(4254, 1)
                            p.inventory.addItem(4273)
                            p.itemDialogue(4273,"The old man gives you the chest key.")
                            p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("FOUND_SON", true)
                        }
                    }
            }
        }
    }

private fun dialogueStageFinal(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "How is it going?")
        npc(npc, HeadE.CALM_TALK, "Wonderful, wonderful! Mother's coming to get me!")
    }
}
@ServerStartupEvent
fun mapOldManGA() {
    onNpcClick(1696, options = arrayOf("Talk-To")) { (player, npc) ->
            OldMan(player, npc)
    }
}

