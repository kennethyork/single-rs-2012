package com.rs.game.content.world.areas.wilderness

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Noterazzo(val player: Player, npc:NPC) {
    init{
        player.startConversation {
            npc(npc, CALM_TALK, " Hey, wanna trade? I'll give the best deals you can find.")
            label("initialOps")
            options {
                ops("Yes, please.") {
                    player(CALM_TALK, "Yes, please.")
                    exec { ShopsHandler.openShop(player, "bandit_camp_free") }
                }
                op("No, thanks.") {
                    player(CALM_TALK, "No, thanks.")
                }
                op("How can you afford to give such good deals?") {
                    player(CALM_TALK, "How can you afford to give such good deals?")
                    npc(
                        npc,
                        CALM_TALK,
                        "The general stores in Asgarnia and Misthalin are heavily taxed. It really makes it hard for them to run an effective business. For some reason taxmen don't visit my store."
                    )
                }
                op("Goodbye.") {
                    player(CALM_TALK, "Goodbye.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapNoterazzo() {
    onNpcClick(597, options = arrayOf("Talk-to")) { (player, npc) -> Noterazzo(player, npc) }
    onNpcClick(597, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "bandit_camp_free")
    }
}