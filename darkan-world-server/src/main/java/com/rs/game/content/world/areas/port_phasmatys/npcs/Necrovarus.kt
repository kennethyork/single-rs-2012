package com.rs.game.content.world.areas.port_phasmatys.npcs;

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.GhostSpeakResponse
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.hasGhostSpeak
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val PETITION = 4283
private const val BONE_KEY = 4272


class Necrovarus(p: Player, npc: NPC) {
    init {
        if(p.isQuestComplete(Quest.GHOSTS_AHOY))
            dialogueStage8(p, npc)
        if(!p.isQuestStarted(Quest.GHOSTS_AHOY))
            p.startConversation {
                label("initialOps")
                options {
                    op("What is this place?") {
                        player(HeadE.CALM, "What is this place?")
                        exec(defaultResponses(p, npc))
                    }
                    op("What happened to everyone here?") {
                        player(HeadE.CALM_TALK, "What happened to everyone here?")
                        exec(defaultResponses(p, npc))
                    }
                    op("How do I get into town?") {
                        player(HeadE.CALM_TALK, "How do I get into town?")
                        exec(defaultResponses(p, npc))
                    }
                }
            }
        else
            when (p.getQuestStage(Quest.GHOSTS_AHOY)) {
                1 -> dialogueStage1(p, npc)
                in 2..4 -> dialogueStage2(p, npc)
                5 -> dialogueStage5(p, npc)
                6 -> dialogueStage7(p, npc)
                else -> dialogueStage8(p, npc)
            }
    }
}

private fun defaultResponses(p: Player, npc: NPC): Runnable {
    return Runnable {
        p.startConversation {
            when (Utils.random(5)) {
                0 -> npc(npc, HeadE.CALM_TALK, "You dare speak to me??? Have you lost your wits???")
                1 -> npc(npc, HeadE.CALM_TALK, "I will answer questions when you are dead!!")
                2 -> npc(npc, HeadE.CALM_TALK, "Nobody speaks to me unless I speak to them!!")
                3 -> npc(npc, HeadE.CALM_TALK, "Speak to me again and I will rend the soul from your flesh.")
                4 -> npc(npc, HeadE.CALM_TALK, "I do not answer questions mortal fool.")
            }
        }
    }
}

private fun dialogueStage1(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "I must speak with you on behalf of Velorina.")
        npc(npc, HeadE.CALM_TALK, "You dare to speak that name in this place?????")
        player(HeadE.CALM_TALK, "She wants to pass-")
        npc(npc, HeadE.CALM_TALK,"Silence!<br>Or I will incinerate the flesh from your bones!!")
        player(HeadE.CALM_TALK,"But she-")
        npc(npc, HeadE.CALM_TALK,"Get out of my sight!! Or I promise you that you will regret your insolence for the rest of eternity!!")
        exec { p.setQuestStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_2_PLEAD_WITH_NECROVARUS) }
    }
}

private fun dialogueStage2(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Please, listen to me-")
        npc(npc, HeadE.CALM_TALK, "No - listen to me. Go from this place and do not return, or I will remove your head.")
    }
}

private fun dialogueStage4(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Wheels have been set in motion, Necrovarus; wheels that will set the citizens of Port Phasmatys free.")
        npc(npc, HeadE.CALM_TALK, "Oh goody goody. I just can't wait.")
    }
}

private fun dialogueStage5(p: Player, npc: NPC) {
    if(p.inventory.containsItems(PETITION) && p.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") == 10){
        p.startConversation {
            player(HeadE.CALM_TALK, "Necrovarus, I am presenting you with a petition form that has been signed by 10 citizens of Port Phasmatys.")
            npc(npc, HeadE.CALM_TALK, "A petition you say? Continue, mortal.")
            player(HeadE.CALM_TALK, "It says the citizens of Port Phasmatys should have the right to choose whether they pass over into the next world or not, and not have this decided by the powers that be on their behalf.")
            npc(npc, HeadE.CALM_TALK, "I see.")
            player(HeadE.CALM_TALK, "So you will let them pass over if the wish?")
            npc(npc, HeadE.CALM_TALK, "Oh yes.")
            player(HeadE.CALM_TALK, "Really?")
            npc(npc, HeadE.CALM_TALK, "NO!!!! Get out of my sight before I burn every ounce of flesh from your bones!!!!!")
            simple("The petition form turns to ashes in your hand.")
            simple("In his rage, Necrovarus drops a key on the floor.")
            exec{
                p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("BurnedPetition", true)
                p.inventory.deleteItem(PETITION, 1)
                p.inventory.addItem(592)
                npc.sendDrop(p, Item(BONE_KEY))
            }
        }
        return
    }
    else
        p.startConversation {
            player(HeadE.CALM_TALK, "It matters not that you ignore your citizens' wishes, Necrovarus. Wheels have been set in motion, wheels that will set them free.")
            npc(npc, HeadE.CALM_TALK, "I have almost completely lost patience with you, mortal. Another word, and every threat I have uttered will be made real for you.")
        }
}

private fun dialogueStage7(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, HeadE.CALM_TALK, "You dare to face me again - you must be truly insane!!!")
        player(HeadE.CALM_TALK, "No, Necrovarus, I am not insane. With this enchanted amulet of ghostspeak I have the power to command you to do my will!")
        options (title = "What do you want to command Necrovarus to do?"){
            op("Let any ghost who so wishes pass on into the next world.") {
                player(HeadE.CALM_TALK, "Let any ghost who so wishes pass on into the next world.")
                simple("A beam of intense light radiates out from the amulet of ghostspeak")
                npc(npc, HeadE.CALM_TALK, "I - will - let ...")
                player(HeadE.CALM_TALK, "Carry on...")
                npc(npc, HeadE.CALM_TALK, "...any...")
                player(HeadE.CALM_TALK, "Yes?")
                npc(npc, HeadE.CALM_TALK, "... ghost who so wishes ...")
                player(HeadE.CALM_TALK, "I think we're almost getting there...")
                npc(npc, HeadE.CALM_TALK, "... pass into the next world.")
                exec{
                    p.questManager.setStage(Quest.GHOSTS_AHOY, GhostsAhoy.STAGE_7_COMMAND_NECROVARUS)
                    p.equipment.setSlot(Equipment.NECK, Item(552))
                    dialogueStage8(p, npc)
                }
            }
            op("Tell me a joke.") {
                player(HeadE.CALM_TALK, "Tell me a joke.")
                simple("A beam of intense light radiates out from the amulet of ghostspeak")
                npc(npc, HeadE.CALM_TALK, "Knock knock.")
                player(HeadE.CALM_TALK, "Who's there?")
                npc(npc, HeadE.CALM_TALK, "Egbert.")
                player(HeadE.CALM_TALK, "Egbert who?")
                npc(npc, HeadE.CALM_TALK, "Egbert no bacon.")
                item(4250, "Luckily the amulet of ghostspeak does not seem to have fully discharged.")
            }
            op("Do a chicken impression.") {
                player(HeadE.CALM_TALK, "Do a chicken impression.")
                simple("A beam of intense light radiates out from the amulet of ghostspeak")
                npc(npc, HeadE.CALM_TALK, "Cluck cluck squuuaaaakkkk cluck cluck. I think I've laid an egg...")
                item(4250, "Luckily the amulet of ghostspeak does not seem to have fully discharged.")
            }
        }
    }
}

private fun dialogueStage8(p: Player, npc: NPC) {
    p.startConversation {
        player(
            HeadE.CALM_TALK,
            "Told you I'd defeat you, Necrovarus. My advice to you is to pass over to the next world yourself with everybody else."
        )
        npc(npc, HeadE.ANGRY, "I should fry you for what you have done...")
        player(
            HeadE.ANGRY,
            "Quiet, evil priest!! If you try anything I will command you again, but this time it will be to throw yourself into the Endless Void for the rest of eternity."
        )
        npc(npc, HeadE.SCARED, "Please no! I will do whatever you say!!")
    }
}

@ServerStartupEvent
fun mapNecrovarus() {
    onNpcClick(1684, options = arrayOf("Talk-To")) { (player, npc) ->
        if (!hasGhostSpeak(player))
            GhostSpeakResponse(player, npc)
        else
            Necrovarus(player, npc)
    }
}
