package com.rs.game.content.quests.the_golem

import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.engine.variables.golem_clay

private const val SOFT_CLAY_ID = 1761
private const val GOLEM_ID = 1907
private const val STRANGE_IMPLEMENT_ID = 4619
private const val GOLEM_PROGRAM_ID = 4624
private var isHeadOpen = false
private var instructionsLoaded = false


private fun DialogueBuilder.defaultGolemDialog(player: Player, npc: NPC) {
    npc(npc, HeadE.SAD, "Damage... severe... ")
}

private fun golemQuestFinishDialog(player:Player, npc: NPC){
    player.startConversation {
        npc(npc, HeadE.CALM_TALK, "New instructions... Updating program...")
        npc(npc, HeadE.HAPPY_TALKING, "Task complete!")
        npc(npc, HeadE.HAPPY_TALKING, "Thank you. Now my mind is at rest.")
    }
    player.sendMessage("Congratulations! You have completed: 'The Golem' - Complete this quest.")
    player.completeQuest(Quest.THE_GOLEM)
}

private fun DialogueBuilder.postQuestGolemDialog(player: Player, npc: NPC){
        npc(npc, HeadE.HAPPY_TALKING, "Thank you for helping me. A golem can have no greater satisfaction than knowing that its task is complete.")
        player(HeadE.CONFUSED, "But the whole city is destroyed! Doesn't that bother you?")
        npc(npc, HeadE.HAPPY_TALKING, "I was never programmed to appreciate the city. My only purpose was the destruction of the demon, and that is achieved!")
}

private fun DialogueBuilder.postRepairGolemDialog(player: Player, npc: NPC) {
    npc(npc, HeadE.SAD, "My task is incomplete. You must open the portal so I can defeat the great demon.")
    label("main")
    if (player.questManager.getStage(Quest.THE_GOLEM) != STAGE_TELL_GOLEM && player.questManager.getStage(Quest.THE_GOLEM) != STAGE_CONVINCE_GOLEM) {
        options("Select an option") {
            op("How do I open the portal?") {
                player(HeadE.CONFUSED, "How do I open the portal?")
                npc(npc, HeadE.CALM, "The four statuettes in the temple must be turned to the correct pattern.")
                npc(npc, HeadE.CALM, "I do not know the pattern. Golems are not permitted to open the portal.")
                goto("main")
            }
            op("What makes you think you can defeat the demon?") {
                player(HeadE.CONFUSED, "What makes you think you can defeat the demon?")
                npc(npc, HeadE.CALM, "If not I, then who else? No living being can destroy the demon. That is why the golems were created in the first place.")
                npc(npc, HeadE.CALM, "But the demon was badly wounded and elder-demons heal very slowly indeed. It was almost dead when it retreated to its own dimension.")
                npc(npc, HeadE.HAPPY_TALKING, "Now that I am repaired, I will be able to destroy it easily!")
                goto("main")
            }
            op("I'll get right on it.") {
                player(HeadE.CONFUSED, "I'll get right on it.")
                if (player.questManager.getStage(Quest.THE_GOLEM) == STAGE_ADD_CLAY) {
                    exec { player.questManager.setStage(Quest.THE_GOLEM, STAGE_CONTINUE_INVESTIGATION) }
                }
            }
        }
    } else {
        if (player.questManager.getStage(Quest.THE_GOLEM) != STAGE_CONVINCE_GOLEM) {
            player(HeadE.HAPPY_TALKING, "It's okay, the demon is dead!")
            npc(npc, HeadE.SAD, "The demon must be defeated...")
            player(HeadE.FRUSTRATED, "No, you don't understand. I saw the demon's skeleton. It must have died of its wounds.")
            npc(npc, HeadE.SAD, "Demon must be defeated! Task incomplete.")
            player.questManager.setStage(Quest.THE_GOLEM, STAGE_CONVINCE_GOLEM)
        } else {
            player(HeadE.FRUSTRATED, "I already told you, he's dead!")
            npc(npc, HeadE.SAD, "Task incomplete.")
            player(HeadE.CONFUSED, "Oh, how am I going to convince you?")
        }
    }
}

@ServerStartupEvent
fun mapGolemDialog() {

    onNpcClick(GOLEM_ID) { (player, npc) ->
        player.startConversation {
            if (!player.questManager.isComplete(Quest.THE_GOLEM)) {
                when {
                    !Quest.THE_GOLEM.meetsReqs(player) -> {
                        defaultGolemDialog(player, npc)
                        return@startConversation
                    }

                    else ->

                        if (player.questManager.getAttribs(Quest.THE_GOLEM).getI("HAS_USED_CLAY") != 4) {
                            npc(npc, HeadE.SAD, "Damage... severe... task... incomplete...")
                            if (player.getQuestStage(Quest.THE_GOLEM) < 1) {
                                options("Select an Option") {
                                    op("Shall I try to repair you?") {
                                        player(HeadE.CONFUSED, "Shall I try to repair you?")
                                        questStart(Quest.THE_GOLEM)
                                        npc(npc, HeadE.SAD, "repairs... needed...") { player.questManager.setStage(Quest.THE_GOLEM, STAGE_ADD_CLAY) }
                                    }
                                    op("I'm not going to find a conversation here!") {
                                    }
                                }
                            }
                        } else {
                            postRepairGolemDialog(player, npc)
                        }
                }
            } else {
                postQuestGolemDialog(player, npc)
            }
        }
    }

    onItemOnNpc(GOLEM_ID) { (player, item, npc) ->
        when (player.getQuestStage(Quest.THE_GOLEM)) {
             STAGE_ADD_CLAY -> when (item.id) {
                SOFT_CLAY_ID -> if (player.questManager.getAttribs(Quest.THE_GOLEM).getI("HAS_USED_CLAY") >= 1) {
                    if (player.questManager.getAttribs(Quest.THE_GOLEM).getI("HAS_USED_CLAY") >= 2) {
                        if (player.questManager.getAttribs(Quest.THE_GOLEM).getI("HAS_USED_CLAY") >= 3) {
                            player.inventory.removeItems(Item(item.id, 1))
                            player.questManager.getAttribs(Quest.THE_GOLEM).setI("HAS_USED_CLAY", 4)
                            player.vars.saveVarBit(golem_clay, 4)
                            player.startConversation {
                                item(item.id, "You repair the golem with a final piece of clay.")
                                npc(npc, HeadE.CALM, "Damage repaired... Thank you. My body and mind are fully healed. Now I must complete my task by defeating the great enemy.")
                                player(HeadE.CONFUSED, "What enemy?")
                                npc(npc, HeadE.CALM, "A great demon. It broke through from its dimension to attack the city.")
                                npc(npc, HeadE.CALM, "The golem army was created to fight it. Many were destroyed, but we drove the demon back!")
                                npc(npc, HeadE.CALM, "The demon is still wounded. You must open the portal so that I can strike the final blow and complete my task.")
                                postRepairGolemDialog(player, npc)
                            }

                        } else {
                            player.inventory.removeItems(Item(item.id, 1))
                            player.questManager.getAttribs(Quest.THE_GOLEM).setI("HAS_USED_CLAY", 3)
                            player.startConversation {
                                item(item.id, "The golem is nearly whole.")
                            }
                            player.vars.saveVarBit(golem_clay, 3)
                        }
                    } else {
                        player.inventory.removeItems(Item(item.id, 1))
                        player.questManager.getAttribs(Quest.THE_GOLEM).setI("HAS_USED_CLAY", 2)
                        player.startConversation {
                            item(item.id, "You fix the golem's legs.")
                        }
                        player.vars.saveVarBit(golem_clay, 2)
                    }
                } else {
                    player.inventory.removeItems(Item(item.id, 1))
                    player.questManager.getAttribs(Quest.THE_GOLEM).setI("HAS_USED_CLAY", 1)
                    player.startConversation {
                        item(item.id, "You apply some clay to the golem's wounds. The clay begins to harden in the hot sun.")
                    }
                    player.vars.saveVarBit(golem_clay, 1)
                }
            }

            STAGE_CONVINCE_GOLEM -> when (item.id) {
                GOLEM_PROGRAM_ID -> {
                    if (isHeadOpen) {
                        instructionsLoaded = true
                        player.inventory.removeItems(Item(GOLEM_PROGRAM_ID, 1))
                    } else {
                        player.sendMessage("You can't see a way to put the instructions in the golem's skull.")
                    }
                }

                STRANGE_IMPLEMENT_ID -> {
                    player.sendMessage("You insert the key and the golem's skull hinges open.")
                    isHeadOpen = true
                    player.tasks.schedule(10) {
                        player.sendMessage("The golem's skull shuts automatically.")
                        player.tasks.schedule(1) {
                            if (instructionsLoaded) {
                                golemQuestFinishDialog(player, npc)
                            }
                            isHeadOpen = false
                        }
                    }
                }
            }
        }
    }
}
