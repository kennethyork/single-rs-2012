package com.rs.game.content.world.areas.desert.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class BedabinNomad(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Hello Effendi! How can I help you?")
            label("initialOps")
            options {
                op("What is this place?") {
                    player(CALM_TALK, "What is this place?")
                    npc(npc, CALM_TALK, "This is the camp of the Bedabin. Talk to our leader, Al Shabim, he'll be happy to chat. We can sell you very reasonably priced water...")
                    goto("initialOps")
                }
                op("Where is the Shantay Pass?") {
                    player(CALM_TALK, "Where is the Shantay Pass?")
                    npc(npc, CALM_TALK, "It is North East of here effendi, across the trackless desert. It will be a thirsty trip, can I interest you in a drink?")
                    goto("initialOps")
                }
                op("What have you got for sale?") {
                    player(CALM_TALK, "What have you got for sale?")
                    exec { ShopsHandler.openShop(player, "bedabin_village_bartering") }
                }
                op("Okay, thanks.") {
                    player(CALM_TALK, "Okay, thanks.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBedabinNomad() {
    onNpcClick(833, options = arrayOf("Talk-to")) { (player, npc) -> BedabinNomad(player, npc) }
    onNpcClick(833, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "bedabin_village_bartering")
    }
}