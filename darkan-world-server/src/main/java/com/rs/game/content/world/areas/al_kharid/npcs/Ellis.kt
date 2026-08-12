package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

fun ellisDialogue(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, CALM_TALK, "Greetings, friend. I am a manufacturer of leather.")
        options {
            op("Can I buy some leather then?") {
                player(CALM_TALK, "Can I buy some leather then?")
                npc(npc, CALM_TALK, "I make leather from animal hides. Bring me some cowhides and I'll tan them into soft leather for you.")
                player(CALM_TALK, "Leather is rather weak stuff.")
                npc(npc, CALM_TALK, "Normal leather may be quite weak, but it's so easy to craft that anyone can work with it.")
                npc(npc, CALM_TALK, "Alternatively you could try hard leather. It's not so easy to craft, but I only charge 3 gp per cowhide to prepare it, and it makes much sturdier armour.")
                npc(npc, CALM_TALK, "I can also tan snake hides and dragonhides, suitable for crafting into the highest quality armour for rangers.")
                player(CALM_TALK, "Thanks, I'll bear it in mind.")
            }
            op("What can you tell me about Ali Morrisane?") {
                player(CALM_TALK, "What can you tell me about Ali Morrisane?")
                npc(npc, CALM_TALK, "I wouldn't mind him setting up shop, but he's brought others in.")
                npc(npc, CALM_TALK, "Two of them came here and said they were going to put me out of business if I didn't listen to them!")
                npc(npc, CALM_TALK, "Hah, as if they think they could threaten me with a hammer and a pair of scissors.")
                player(CALM_TALK, "What will you do about the threats?")
                npc(npc, CALM_TALK, "The other shopkeepers and I won't give in to their threats. Osman's also told us that he'll make sure we're protected.")
                npc(npc, CALM_TALK, "I'll keep tanning leather for people like you to make into clothes and armour.")
            }
            op("No thanks.") {
                player(CALM_TALK, "No thanks.")
            }
        }
    }
}

@ServerStartupEvent
fun mapEllis() {
    onNpcClick(2824, options = arrayOf("Talk to")) { (player, npc) ->
        ellisDialogue(player, npc)
    }
}
