package com.rs.game.content.world.areas.al_kharid.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class AlKharidShopkeeper(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Can I help you at all?")
            label("initialOps")
            options {
                op("Yes please, what are you selling?") {
                    player(HAPPY_TALKING, "Yes please, what are you selling?")
                    exec { ShopsHandler.openShop(player, "alkharid_general_store") }
                }
                op("How should I use your shop?") {
                    player(HAPPY_TALKING, "How should I use your shop?")
                    npc(npc, HAPPY_TALKING, "I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
                }
                op("No thanks.") {
                    player(HAPPY_TALKING, "No thanks.")
                }
                op("What do you think of Ali Morrisane?") {
                    player(HAPPY_TALKING, "What do you think of Ali Morrisane?")
                    npc(npc, HAPPY_TALKING, "I don't trust him, not after he's sent his men round to threaten us. Did you know, we've had four different thugs come round and threaten us if we don't join them. I've had hammers, rakes, swords and even scissors waved at me!")
                    player(HAPPY_TALKING, "What will you do about it?")
                    npc(npc, HAPPY_TALKING, "We'll carry on as normal with the store. Business as usual! Can I interest you in anything?")
                    goto("initialOps")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapAlKharidShopkeeper() {
    onNpcClick(525, 524, options = arrayOf("Talk-to")) { (player, npc) -> AlKharidShopkeeper(player, npc) }
    onNpcClick(525, 524, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "alkharid_general_store")
    }
}
