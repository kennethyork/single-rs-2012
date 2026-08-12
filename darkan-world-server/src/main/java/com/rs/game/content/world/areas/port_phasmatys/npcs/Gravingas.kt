package com.rs.game.content.world.areas.port_phasmatys.npcs
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onNpcClick

private const val PETITION = 4283

class Gravingas(player: Player, npc: NPC) {
    init {
        if(!player.isQuestStarted(Quest.GHOSTS_AHOY))
            preQuestDialogue(player, npc)
        else if(player.getQuestStage(Quest.GHOSTS_AHOY) <= GhostsAhoy.STAGE_4_SEEK_OLD_WOMAN)
            preQuestDialogue(player, npc)
        else if (!player.isQuestComplete(Quest.GHOSTS_AHOY) && player.getQuestStage(Quest.GHOSTS_AHOY) >= GhostsAhoy.STAGE_5_GATHER_ITEMS)
            questDialogue(player, npc)
        else
            postQuestDialogue(player, npc)
    }
}

private fun preQuestDialogue(player: Player, npc: NPC) {
    player.startConversation {
        npc(
            npc,
            HeadE.CALM_TALK,
            "Will you join with me and protest against the evil ban of Necrovarus and his disciples?"
        )
        player(HeadE.CALM_TALK, "I'm sorry, I don't really think I should get involved.")
        npc(npc, HeadE.CALM, "Ah, the youth of today - so apathetic to politics.")
    }
}

private fun questDialogue(player: Player, npc: NPC) {
    player.startConversation {
        if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("BurnedPetition")) {
            npc(npc, HeadE.CALM_TALK, "So have you presented the petition to Necrovarus?")
            player(HeadE.CALM_TALK, "Yes. He burned it.")
            npc(npc, HeadE.CALM_TALK, "That's exactly what I thought he would do.")
            player(HeadE.CALM_TALK, "Well, if you knew that he would do that, why have I been wasting my time running around after ghosts for signatures?")
            npc(npc, HeadE.CALM_TALK, "It never hurts to get involved in politics.")
            return@startConversation
        }
        if(!player.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("StartedPetition")) {
            npc(
                npc,
                HeadE.CALM_TALK,
                "Will you join with me and protest against the evil ban of Necrovarus and his disciples?"
            )
            options {
                op("After hearing Velorina's story I will be happy to help out.") {
                    player(HeadE.CALM_TALK, "After hearing Velorina's story I will be happy to help out.")
                    npc(npc, HeadE.CALM_TALK, "Excellent, excellent! Here - take this petition form, and try and get 10 signatures from the townsfolk.")
                    item(PETITION, "The ghost hands you a petition.")
                    exec {
                        player.inventory.addItem(PETITION)
                        player.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("StartedPetition", true)
                    }
                }
                op("I'm sorry, I don't really think I should get involved.") {
                    player(HeadE.SHAKING_HEAD, "I'm sorry, I don't really think I should get involved.")
                }
            }
        }
        if (player.inventory.containsItem(PETITION) && player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") == 10) {
            npc(npc, HeadE.CALM_TALK, "You've got them all! Now go and present them to Necrovarus!!")
            return@startConversation
        }
        if (player.inventory.containsItem(PETITION)) {
            npc(npc, HeadE.CALM_TALK, "You've got ${player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures")} out of the 10 signatures so far. Keep going!!")
            return@startConversation
        }
        if (!player.inventory.containsItem(PETITION) && player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") > 0) {
            player(HeadE.CALM_TALK, "I seem to have lost my petition, do you have a spare?")
            npc(npc, HeadE.CALM_TALK, "One of the villagers found it, here it is.")
            item(PETITION, "The ghost hands you the petition.")
            exec { (player.inventory.addItem(PETITION)) }
            return@startConversation
        }
    }
}

private fun postQuestDialogue(player: Player, npc: NPC) {
    player.startConversation {
        player(HeadE.CALM_TALK, "Why are you still protesting? I have commanded Necrovarus to let you and your friends do as you like! There's no need for this anymore!")
        npc(npc, HeadE.CALM_TALK, "There's always a need for a healthy interest in politics.")
        player(HeadE.CALM_TALK, "Okay, suit yourself then.")
    }
}

@ServerStartupEvent
fun mapGravingasGA() {
    onNpcClick(6075, options = arrayOf("Talk-To")) { (player, npc) ->
        if (!PortPhasmatys.hasGhostSpeak(player))
            PortPhasmatys.GhostSpeakResponse(player, npc)
        else
            Gravingas(player, npc)
    }
    instantiateNpc(6075) { id, tile ->
        var tickCounter = 0;
        object: NPC(id, tile) {
            override fun processNPC() {
                super.processNPC()
                tickCounter++
                if(tickCounter % 10 == 0)
                    when (Utils.random(8)) {
                        0 -> forceTalk("Down with Necrovarus!!")
                        1 -> forceTalk("Power to the Ghosts!!")
                        2 -> forceTalk("United we conquer, divided we fall!!")
                        3 -> forceTalk("Rise up my fellow Ghosts, and we shall be victorious!!")
                        4 -> forceTalk("Don't stay silent, Victory in numbers!!")
                        5 -> forceTalk("Let Necrovarus know that we want out!!")
                        6 -> forceTalk("Rise together, Ghosts without a cause!!")
                        7 -> forceTalk("We shall overcome!!")
                    }
            }
        }
    }
    onItemClick(PETITION, options = arrayOf("Count")) { e ->
        e.player.itemDialogue(PETITION, "You have obtained ${e.player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures")} signatures so far.")
    }
}
