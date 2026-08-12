package com.rs.game.content.world.areas.taverley.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Bettamax(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Hello there.")
            player(CALM_TALK, "Hello. Who are you?")
            npc(npc, CALM_TALK, "My name is Bettamax, I’m a botanist in training.")
            label("initialOps")
            options {
                op("What’s a botanist?") {
                    player(CONFUSED, "What's a botanist?")
                    npc(npc, CALM_TALK, "Someone that studies and identifies different plants and their properties.")
                    player(CALM_TALK, "What are you studying?")
                    npc(npc, CALM_TALK, "Currently nothing. I’m waiting for Astlayrix, my mentor, to come back from the herblore habitat.")
                    player(SKEPTICAL, "Habitat?")
                    npc(npc, CALM_TALK, "Oh, it’s fascinating. There are all these new creatures and plants and, well, I’m probably not the best person to explain it…")
                    npc(npc, CALM_TALK, "If you want to go see for yourself, I have a small stock of these weird teleport-bag things. They’ll take you straight there.")
                    npc(npc, CALM_TALK, "Would you like to buy one?")
                    options {
                        op("Yes.") {
                            player(CALM_TALK, "Yes.")
                            exec { ShopsHandler.openShop(player, "bettamaxs_shop") }
                        }
                        op("No.") {
                            player(CALM_TALK, "No.")
                            player(CALM_TALK, "Maybe another time.")
                        }
                    }
                }
                op("Can I see your shop?") {
                    player(CALM_TALK, "Can I see your shop?")
                    exec { ShopsHandler.openShop(player, "bettamaxs_shop") }
                }
                op("What’s that walking next to you?") {
                    player(CONFUSED, "What’s that walking next to you?")
                    npc(npc, CALM_TALK, "Oh, that’s Wilbur. He’s a jadinko.")
                    player(CALM_TALK, "What’s a jadinko?")
                    npc(npc, CALM_TALK, "I have no idea.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBettamax() {
    onNpcClick(13106, options = arrayOf("Talk-to")) { (player, npc) -> Bettamax(player, npc) }
    onNpcClick(13106, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "bettamaxs_shop")
    }
}