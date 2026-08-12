package com.rs.game.content.world.areas.canifis.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Fidelio(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            player(CALM_TALK, "Hello there.")
            npc(npc, NERVOUS, "H-hello. You l-look like a s-stranger to these p-parts. Would you l-like to buy something? I h-have some s-special offers at the m-minute...some s-sample bottles for s- storing s-snail slime.")
            label("initalOps")
            options {
                op("Yes, please.") {
                    player(CALM_TALK, "Yes, please.")
                    exec { ShopsHandler.openShop(player, "canifis_general_store") }
                }
                op("No thanks.") {
                    player(CALM_TALK, "No thanks.")
                    npc(npc, SAD, "Th-that's okay. Nobody ever w-wants to buy my wares. Oh, s-sure, if it was food or c-clothes they would though!")
                }
                op("Why are your prices so high?") {
                    player(CALM_TALK, "Why are your prices so high?")
                    npc(npc, SAD, "As you p-probably know, the h-humans hate our kind and k-keep us trapped here. To get my s-stocks I have to sneak into the human lands in s-secret! All the s-secrecy and d-danger has played h-havoc with my nerves!")
                    player(FRUSTRATED, "But all your stuff is overpriced junk!")
                    npc(npc, UPSET, "N-not at all! They are v-valuable human artefacts smuggled here at g-great personal risk!")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapFidelio() {
    onNpcClick(1040, options = arrayOf("Talk-to")) { (player, npc) -> Fidelio(player, npc) }
    onNpcClick(1040, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "canifis_general_store")
    }
}