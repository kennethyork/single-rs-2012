package com.rs.game.content.world.areas.port_phasmatys.npcs;

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.ectofuntus.Ectofuntus;
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.GhostSpeakResponse
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys.Companion.hasGhostSpeak
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player;
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

class GhostDisciple(p: Player, npc: NPC) {
    init {
        p.startConversation {
            player(HeadE.CALM_TALK, "What is this strange fountain?")
            npc(npc, HeadE.CALM_TALK, "This is the Ectofuntus, the most marvellous creation of Necrovarus, our glorious leader.")
            label("initialOps")
            options {
                op("What is the Ectofuntus for?") {
                    player(HeadE.CALM_TALK, "What is the Ectofuntus for?")
                    npc(npc, HeadE.CALM_TALK, "It provides the power to keep us ghosts from passing over into the next plane of existence.")
                    player(HeadE.CALM_TALK, "And how does it work?")
                    npc(npc, HeadE.CALM_TALK, "You have to pour a bucket of Ectoplasm into the fountain, a pot of ground bones, and then worship at the Ectofuntus. A unit of unholy power will then be created.")
                    player(HeadE.CALM_TALK, "Can you do it yourself?")
                    npc(npc, HeadE.CALM_TALK, "No, we must rely upon the living, as the worship of the undead no longer holds any inherent power.")
                    player(HeadE.CALM_TALK, "Why would people waste their time helping you out?")
                    npc(npc, HeadE.CALM_TALK, "For every unit of power produced we will give you five Ectotokens. These tokens can be used in Port Phasmatys to purchase various services, not least of which includes access through the main toll gates.")
                    goto("initialOps")                }
                op("Where do I get Ectoplasm from?") {
                    player(HeadE.CALM_TALK, "Where do I get Ectoplasm from?")
                    npc(npc, HeadE.CALM_TALK, "Necrovarus sensed the power bubbling beneath our feet, and we delved long and deep beneath Port Phasmatys, until we found a pool of natural Ectoplasm. You may find it using the trapdoor over there.")
                    goto("initialOps")                }
                op("How do I grind bones?") {
                    player(HeadE.CALM_TALK, "How do I grind bones?")
                    npc(npc, HeadE.CALM_TALK, "There is a bone grinding machine upstairs. Put bones of any type into the machine's hopper and then turn the handle to grind them. You will need a pot to empty the machine of ground up bones.")
                    goto("initialOps")                }
                op("How do I receive Ectotokens?") {
                    player(HeadE.CALM_TALK, "How do I receive Ectotokens?")
                    npc(npc, HeadE.CALM_TALK, "We disciples keep track of how many units of power have been produced. Just talk to us once you have generated some and we will reimburse you with the correct amount of Ectotokens.")
                    player(HeadE.CALM_TALK, "How do I generate units of power?")
                    npc(npc, HeadE.CALM_TALK, "You have to pour a bucket of Ectoplasm into the fountain, a pot of ground bones, and then worship at the Ectofuntus. A unit of unholy power will then be created.")
                    goto("initialOps")
                }
                op("Thanks for your time.") {
                    player(HeadE.CALM_TALK, "Thanks for your time.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapghostDisciple() {
    onNpcClick(1686, options = arrayOf("Talk-to")) { (player, npc) ->
        if (!hasGhostSpeak(player))
            GhostSpeakResponse(player, npc)
        else
            GhostDisciple(player, npc)
    }
    onNpcClick(1686, options = arrayOf("Collect")) { (player, npc) ->
        if (!hasGhostSpeak(player))
            GhostSpeakResponse(player, npc)
        else
        if (player.inventory.hasFreeSlots() && player.unclaimedEctoTokens > 0) {
            val tokens = player.unclaimedEctoTokens;
            player.inventory.addItem(Ectofuntus.ECTO_TOKEN, player.unclaimedEctoTokens);
            player.unclaimedEctoTokens = 0;
            player.npcDialogue(npc, HeadE.HAPPY_TALKING, "Receive ${if(tokens == 1) "this" else "these"} $tokens Ectotoken${if(tokens == 1) "" else "s"} with our thanks.")
        }
        else
            player.npcDialogue(npc, HeadE.HAPPY_TALKING, "Mortal, you must worship the Ectofuntus before we will consider giving you any tokens.")
    }
}
