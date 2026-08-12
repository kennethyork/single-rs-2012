package com.rs.game.content.world.areas.gu_tanoth.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class OgreTrader(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "What the human be wantin'?")
            label("initialOps")
            options {
                op("Can I see what you're selling?") {
                    player(CALM_TALK, "Can I see what you're selling?")
                    npc(npc, CALM_TALK, "I suppose so.")
                    exec { ShopsHandler.openShop(player, "dals_general_ogre_supplies") }
                }
                op("I don't need anything.") {
                    player(CALM_TALK, "I don't need anything.")
                    npc(npc, CALM_TALK, "As you wish.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapOgreTrader() {
    onNpcClick(873, options = arrayOf("Talk-to")) { (player, npc) -> OgreTrader(player, npc) }
    onNpcClick(873, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "dals_general_ogre_supplies")
    }
}