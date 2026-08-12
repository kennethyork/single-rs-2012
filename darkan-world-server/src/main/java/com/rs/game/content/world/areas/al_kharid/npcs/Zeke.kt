package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

private fun zeke(player: Player) {
    val npcId = 541
    player.startConversation {
        npc(npcId, HeadE.CALM_TALK, "A thousand greetings, ${if (player.appearance.isMale) "sir" else "madam"}.")
        options {
            op("Do you want to trade?") {
                player(HeadE.CALM_TALK, "Do you want to trade?")
                npc(npcId, HeadE.CALM_TALK, "Yes, certainly. I deal in scimitars.")
                exec { ShopsHandler.openShop(player, "zekes_superior_scimitars") }
            }
            op("Nice cloak.") {
                player(HeadE.CALM_TALK, "Nice cloak.")
                npc(npcId, HeadE.CHEERFUL, "Thank you.")
            }
            op("Could you sell me a dragon scimitar?") {
                player(HeadE.CALM_TALK, "Could you sell me a dragon scimitar?")
                npc(npcId, HeadE.LAUGH, "A dragon scimitar? A DRAGON scimitar?")
                npc(npcId, HeadE.LAUGH, "No way, ${if (player.appearance.isMale) "man" else "lady"}!")
                npc(npcId, HeadE.LAUGH, "The banana-brained nitwits who make them would never dream of selling any to me.")
                npc(npcId, HeadE.CALM_TALK, "Seriously, you'll be a monkey's uncle before you'll ever hold a dragon scimitar.")
                if (!player.questManager.isComplete(Quest.MONKEY_MADNESS)) {
                    player(HeadE.SKEPTICAL, "Oh well, thanks anyway.")
                } else {
                    player(HeadE.SKEPTICAL_THINKING, "Hmmm, funny you should say that...")
                }
                npc(npcId, HeadE.CALM_TALK, "Perhaps you'd like to take a look at my stock?")
                options {
                    op("Yes, please, Zeke.") {
                        exec { ShopsHandler.openShop(player, "zekes_superior_scimitars") }
                    }
                    op("Not today, thank you.") {
                        player(HeadE.CALM_TALK, "Not today, thank you.")
                    }
                }
            }
            op("What do you think of Ali Morrisane?") {
                player(HeadE.CALM_TALK, "What do you think of Ali Morrisane?")
                npc(npcId, HeadE.SKEPTICAL, "He is a dangerous man.")
                npc(npcId, HeadE.SKEPTICAL, "Although he does not appear to be dangerous, he has brought several men to this town who have threatened me and several others.")
                npc(npcId, HeadE.ANGRY, "One man even threatened me with a hammer, saying that when he set up his smithy, my shoddy workmanship would be revealed!")
                player(HeadE.CALM_TALK, "What will you do about these threats?")
                npc(npcId, HeadE.HAPPY_TALKING, "Oh, I am quite confident in the quality of my work... as will you be if you take a look at my wares.")
                npc(npcId, HeadE.CALM_TALK, "Would you like to see them?")
                options {
                    op("Yes, please, Zeke.") {
                        exec { ShopsHandler.openShop(player, "zekes_superior_scimitars") }
                    }
                    op("Not today, thank you.") {
                        player(HeadE.CALM_TALK, "Not today, thank you.")
                    }
                }
            }
            op("No thanks.") {
                player(HeadE.CALM_TALK, "No thanks.")
            }
        }
    }
}

private fun zekeFlyer(player: Player) {
    val npcId = 541
    player.startConversation {
        player(HeadE.CALM_TALK, "Hi. I have this money off voucher.")
        npc(npcId, HeadE.SAD, "So I see. Unfortunately, it seems to have expired... yesterday. Never mind.")
        player(HeadE.ANGRY, "But I only just got it!")
        npc(npcId, HeadE.CALM_TALK, "I'm sorry. There's nothing I can do. Goodbye.")
    }
}

@ServerStartupEvent
fun handleZeke() {
    onNpcClick(541) { e ->
        if (e.option == "Talk-to") {
            zeke(e.player)
        }
    }
    onItemOnNpc(541) { e ->
        if(e.item.id != 7922)
            e.player.startConversation {
                player(HeadE.SHAKING_HEAD, "I have no use for this.")
            }
        else
            zekeFlyer(e.player)
    }
}
