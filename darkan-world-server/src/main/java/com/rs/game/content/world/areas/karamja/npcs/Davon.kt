package com.rs.game.content.world.areas.karamja.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Davon(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, SECRETIVE, "Pssst! Come here if you want to do some amulet trading.")
            label("initialOps")
            options {
                op("Okay, let's trade.") {
                    player(CALM_TALK, "Okay, let's trade.")
                    exec { ShopsHandler.openShop(player, "davons_amulet_store") }
                }
                op("What do you mean by 'pssst'?"){
                    player(CONFUSED, "What do you mean by 'pssst'?")
                    npc(npc, CALM_TALK, "Errr, I was...I was clearing my throat.")
                }
                op("Why don't you ever restock some types of amulets?") {
                    player(CALM_TALK, "Why don't you ever restock some types of amulets?")
                    npc(npc, CALM_TALK, "Some of these amulets are very hard to get. If you've got any, I'd be happy to buy them off you, but I can't normally get them at all.")
                    options {
                        op("Okay, let's trade.") {
                            player(CALM_TALK, "Okay, let's trade.")
                            exec { ShopsHandler.openShop(player, "davons_amulet_store") }
                        }
                        op("No thanks.") {
                            player(CALM_TALK, "No thanks.")
                        }
                    }
                }
                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapDavon() {
    onNpcClick(588, options = arrayOf("Talk-to")) { (player, npc) -> Davon(player, npc) }
    onNpcClick(588, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "davons_amulet_store")
    }
}