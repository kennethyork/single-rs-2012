package com.rs.game.content.world.areas.ardougne.npcs.east_ardougne

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class GemMerchant(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CHEERFUL, "Here, Look at my lovely gems.")
            exec { ShopsHandler.openShop(player, "ardougne_gem_stall") }
        }
    }
}

@ServerStartupEvent
fun mapGemMerchant() {
    onNpcClick(570, options = arrayOf("Talk-to")) { (player, npc) -> GemMerchant(player, npc) }
    onNpcClick(570, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "ardougne_gem_stall")
    }
}