package com.rs.game.content.world.areas.goblin_maze.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Nardok(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Psst... wanna buy some weapons?")
            label("initialOps")
            options {
                op("No thanks.") {
                    player(CALM_TALK, "No thanks")
                    npc(npc, CALM_TALK, "Yeah, you stay out of trouble.")
                }
                op("Why are you whispering?") {
                    player(CONFUSED, "Why are you whispering?")
                    npc(npc, CALM_TALK, "Well, y'know, it's not the most reputable profession, is it? Selling weapons?")
                    player(SKEPTICAL_THINKING, "Why not?")
                    npc(npc, CALM_TALK, "Well, they're used to hurt people, aren't they? That's not considered very, y'know, respectable. Even though the guards use them, and the hunters, they're still considered a bit, y'know, dirty.")
                    npc(npc, CALM_TALK, "Isn't it like that where you come from?")
                    player(CALM_TALK, "No, on the surface weapon-making is considered a noble profession.")
                    npc(npc, CALM_TALK, "I don't know, you surface people are strange!")
                    goto("initialOps")
                }
                op("What have you got?") {
                    player(SKEPTICAL_THINKING, "What have you got?")
                    npc(npc, CALM_TALK, "Well, first up there's the normal bone club and bone spear. Good, solid frog-bone, and the spear has an iron tip. Can withstand a lot of punishment, if you know what I mean!")
                    npc(npc, CALM_TALK, "But if you're after something a bit fancier, a bit more sophisticated, there's the bone crossbow and dagger.")
                    npc(npc, CALM_TALK, "These are specially good for getting your prey unawares, but you're not going to get the best out of them unless you've been taught the special technique.")
                    npc(npc, CALM_TALK, "So, you interested?")
                    options {
                        op("I might be.") {
                            player(CALM_TALK, "I might be.")
                            exec { ShopsHandler.openShop(player, "nardocks_bone_weapons") }
                        }
                        op("No thanks.") {
                            player(CALM_TALK, "No thanks.")
                            npc(npc, CALM_TALK, "Yeah, you stay out of trouble.")
                        }
                    }
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapNardok() {
    onNpcClick(4312, options=arrayOf("Talk-to")) { (player, npc) -> Nardok(player, npc) }
    onNpcClick(4312, options=arrayOf("Trade")) { (player, npc) -> ShopsHandler.openShop(player, "nardocks_bone_weapons") }
}
