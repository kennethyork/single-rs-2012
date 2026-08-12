package com.rs.game.content.world.areas.port_phasmatys.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val BEDSHEET = 4284
private const val ECTOBEDSHEET = 4285
private const val PETITION = 4283
private const val ECTOTOKENS = 4278

@ServerStartupEvent
fun mapGhostVillager() {
    onNpcClick(1697) { (player, npc) ->
        if (!PortPhasmatys.hasGhostSpeak(player))
            PortPhasmatys.GhostSpeakResponse(player, npc)
        if(player.inventory.containsItem(PETITION))
            petitionDialogue(player,npc)
        else
            GhostVillager(player, npc)
    }
}
fun GhostVillager(player: Player, npc: NPC) {
    if (!PortPhasmatys.hasGhostSpeak(player))
        PortPhasmatys.GhostSpeakResponse(player, npc)
    else {
        player.startConversation {
            when (Utils.random(6)) {
                1 -> npc(npc, HeadE.CALM, "What do you want, mortal?")
                2 -> npc(npc, HeadE.CALM, "We do not talk to the warm-bloods.")
                3 -> npc(npc, HeadE.CALM, "Why did we have to listen to that maniacal priest?")
                4 -> npc(npc, HeadE.CALM, "This cold wind blows right through you, doesn't it?")
                5 -> npc(npc, HeadE.CALM, "Worship the Ectofuntus all you want, but don't bother us, human.")
            }
        }
    }
}

fun petitionDialogue(player: Player, npc: NPC) {
    val headSlot = player.equipment.hatId
    if (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") == 10) {
        player.startConversation {
            player(HeadE.CALM_TALK, "Thank you for your support.")
            player.sendMessage("I have all the signatures needed, I should go and see Gravingas.")
        }
        return
    }
    if(headSlot != BEDSHEET && headSlot != ECTOBEDSHEET) {
        player.startConversation {
            player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
            npc(npc, HeadE.SHAKING_HEAD, "I'm sorry, but it's hard to believe that a mortal could be interested in helping us.")
        }
        return
    }
    if(headSlot == BEDSHEET) {
        player.startConversation {
            player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
            npc(npc, HeadE.SHAKING_HEAD, "Why are you wearing that bedsheet? If you're trying to pretend to be one of us, you're not fooling anybody - you're not even green!")
        }
        return
    }
    val lastSigned = player.questManager.getAttribs(Quest.GHOSTS_AHOY).getO<Any?>("LastSigned")
    if (lastSigned == "${npc.respawnTile.x},${npc.respawnTile.y},${npc.respawnTile.plane}") {
        player.startConversation {
            player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
            npc(npc, HeadE.SHAKING_HEAD, "You only just asked me the same thing! Leave me alone - I've had my say!")
        }
        return
    }
    else {
        when (Utils.random(3)) {
            0 -> freeDialogue(player, npc)
            1 -> costDialogue(player, npc)
            2 -> failDialogue(player, npc)
        }
    }
}


fun freeDialogue(player: Player, npc: NPC) {
    player.startConversation {
        val tileAsString = "${npc.respawnTile.x},${npc.respawnTile.y},${npc.respawnTile.plane}"
        player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<Any?>("LastSigned", tileAsString)
        player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
        when(Utils.random(3)) {
            0 -> npc(npc, HeadE.SHAKING_HEAD, "Most certainly, I will.")
            1 -> npc (npc, HeadE.SHAKING_HEAD, "I'll do anything that annoys Necrovarus.")
            2 -> npc(npc, HeadE.SHAKING_HEAD, "Yes, of course.")
        }
        exec {
            player.questManager.getAttribs(Quest.GHOSTS_AHOY).incI("Signatures")
            player.itemDialogue(PETITION, "The ghost signs your petition. You have obtained ${player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures")} signatures so far.")
            if (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") == 10) {
                player.itemDialogue(PETITION, "You have succeeded in obtaining 10 signatures on the petition form!")
            }
        }
    }
}

fun costDialogue(player: Player, npc: NPC) {
    val tileAsString = "${npc.respawnTile.x},${npc.respawnTile.y},${npc.respawnTile.plane}"
    player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<Any?>("LastSigned", tileAsString)
    val cost = Utils.random(1, 3)
    player.startConversation {
        player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
        when (Utils.random(3)) {
            0 -> npc(npc, HeadE.SHAKING_HEAD, "I will if you make it worth my while...")
            1 -> npc(npc, HeadE.SHAKING_HEAD, "You scratch my back and I'll scratch yours...")
            2 -> npc(npc, HeadE.SHAKING_HEAD, "It'll cost you...")
        }
        player(HeadE.CALM_TALK, "How much?")
        npc(npc, HeadE.CALM_TALK, "Oh, it'll cost you $cost ecto-tokens.")
        if (player.inventory.getTotalNumberOf(ECTOTOKENS) < cost) {
            player(HeadE.CALM_TALK, "I don't have that many on me.")
            npc(npc, HeadE.SHAKING_HEAD, "No tokens, no signature.")
        }
        else {
            options {
                op("Okay, if you insist") {
                    player(HeadE.CALM_TALK, "Okay, if you insist.")
                    exec {
                        player.questManager.getAttribs(Quest.GHOSTS_AHOY).incI("Signatures")
                        player.inventory.deleteItem(ECTOTOKENS, cost)
                        player.itemDialogue(PETITION, "The ghost signs your petition. You have obtained ${player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures")} signatures so far.")
                        if (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("Signatures") == 10) {
                            player.itemDialogue(PETITION, "You have succeeded in obtaining 10 signatures on the petition form!")
                        }
                    }
                }
                op("There's no way I'm giving in to corruption.") {
                    player(HeadE.CALM_TALK, "There's no way I'm giving in to corruption.")
                    npc(npc, HeadE.SHAKING_HEAD, "Suit yourself.")
                }
            }
        }
    }
}

fun failDialogue(player: Player, npc: NPC) {
    val tileAsString = "${npc.respawnTile.x},${npc.respawnTile.y},${npc.respawnTile.plane}"
    player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<Any?>("LastSigned", tileAsString)
    player.startConversation {
        player(HeadE.CALM_TALK, "Would you sign this petition form, please?")
        when (Utils.random(5)) {
            0 -> npc(npc, HeadE.SHAKING_HEAD, "I will have you know that I am a fervent supporter of Necrovarus.")
            1 -> npc (npc, HeadE.SHAKING_HEAD, "My answer is no.")
            2 -> npc(npc, HeadE.SHAKING_HEAD, "How dare you accost me in the street?")
            3 -> npc(npc, HeadE.SHAKING_HEAD, "I don't have time for this nonsense.")
            4 -> npc (npc, HeadE.SHAKING_HEAD, "Get lost.")
        }
    }
}

