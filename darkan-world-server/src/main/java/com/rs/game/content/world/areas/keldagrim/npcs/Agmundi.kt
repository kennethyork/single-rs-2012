package com.rs.game.content.world.areas.keldagrim.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Agmundi(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Oh no, not another human... what do you want then?")
            player(CALM_TALK, "Oh, do you get humans here often?")
            npc(npc, CALM_TALK, "Not that often, no, but sometimes. Of course, since you people are too big for dwarven clothes, they typically don't stay very long.")
            player(CALM_TALK, "Why don't you make bigger clothes then?")
            npc(npc, CALM_TALK, "What'd be the point? Besides, I don't make these clothes myself.")
            options {
                op("Who makes these clothes then?") {
                    npc(npc, CALM_TALK, "Oh, my sister, she lives in Keldagrim-East. Has a little stall on the other side of the Kelda. If she only worked a little harder, like me, she wouldn't have to live in the sewers of the city. Shame really.")
                    player(SKEPTICAL, "The sewers? Your sister lives in the sewers?")
                    npc(npc, CALM_TALK, "Keldagrim-East, such a ghastly place. Not civil, polite and clean like we are in the West.")
                    player(CONFUSED, "Uh-huh.")
                }
                op("I still want to buy your clothes.") {
                    player(CALM_TALK,"I still want to buy your clothes.")
                    exec { ShopsHandler.openShop(player, "agmundi_quality_clothes") }
                }
                op("So do you have any quests for me?") {
                    player(CALM_TALK, "So do you have any quests for me?")
                    npc(npc, CONFUSED, "Quests? Why would I have any quests?")
                    player(CALM_TALK, "Oh, just anything to do would be fine.")
                    npc(npc, CALM_TALK, "No, not right now... maybe I'll have something for you to do later, but nothing at the moment.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapAgmundi() {
    onNpcClick(2161, options = arrayOf("Talk-to")) { (player, npc) -> Agmundi(player, npc) }
    onNpcClick(2161, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "agmundi_quality_clothes")
    }
}