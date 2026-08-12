package com.rs.game.content.world.areas.taverley.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

class Holoy(val player: Player, val npc: NPC) {
    init {
        player.startConversation {
            npc(npc, CALM_TALK, "Oh, hello.")
            player(CALM_TALK, "Hello, what's in those boxes?")
            npc(npc, CALM_TALK, "Crossbows, are you interested?")
            player(SKEPTICAL, "Maybe, are they any good?")
            npc(npc,CHEERFUL_EXPOSITION, "Are they any good?! They're dwarven engineering at its best!")
            label("initialOps")
            options {
                op("How do I make one for myself?") {
                    player(SKEPTICAL, "How do I make one for myself?")
                    npc(npc, CALM_TALK, "Well, firstly you'll need to chop yourself some wood, then use a knife on the wood to whittle out a nice crossbow stock like these here.")
                    player(CALM_TALK, "Wood fletched into stock... check.")
                    npc(npc, CALM_TALK, "Then get yourself some metal and a hammer and smith yourself some limbs for the bow, mind that you use the right metals and woods.")
                    player(CALM_TALK, "Which goes with which?")
                    npc(npc, CALM_TALK, "Wood and Bronze as they're basic materials, Oak and Blurite, Willow and Iron, Steel and Teak, Mithril and Maple, Adamantite and Mahogany and finally Runite and Yew.")
                    player(CALM_TALK, "Ok, so I have my stock and a pair of limbs... what now?")
                    npc(npc, CALM_TALK, "Simply take a hammer and smack the limbs firmly onto the stock. You'll then need a string, only they're not the same as normal bows.")
                    npc(npc, CALM_TALK, "You'll need to dry some large animal's meat to get sinew, then spin that on a spinning wheel, it's the only thing we've found to be strong enough for a crossbow.")
                    options {
                        op("What about magic logs?") {
                            player(CALM_TALK, "What about magic logs?")
                            npc(npc, CALM_TALK, "Well.. I don't rightly know... us dwarves don't work with magic, we prefer gold and rock. Much more stable. I guess you could ask the humans at the rangers guild to see if they can do something.")
                            player(CALM_TALK, "Thanks for telling me. Bye!")
                            npc(npc, CALM_TALK, "Take care, straight shooting.")
                        }
                        op("Thanks for telling me. Bye!") {
                            player(CALM_TALK, "Thanks for telling me. Bye!")
                            npc(npc, CALM_TALK, "Take care, straight shooting.")
                        }
                    }
                }
                op("What about ammo?") {
                    player(CALM_TALK, "What about ammo?")
                    npc(npc, CALM_TALK, "You can smith yourself lots of different bolts, don't forget to flight them with feathers like you do arrows though. This can have some pretty powerful effects.")
                    player(CALM_TALK, "Oh my poor bank, how will I store all those?!")
                    npc(npc, CALM_TALK, "Find Hirko in Keldagrim, he also sells crossbow parts and I'm sure he has something you can use to store bolts in.")
                    player(CALM_TALK, "Thanks for the info.")
                }
                op("Thanks for telling me. Bye!") {
                    player(CALM_TALK, "Thanks for telling me. Bye!")
                    npc(npc, CALM_TALK, "Take care, straight shooting.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapHoloy() {
    onNpcClick(4559, options = arrayOf("Talk-to")) { (player, npc) -> Holoy(player, npc) }
    onNpcClick(4559, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "crossbow_shop_white_wolf_mountain")
    }
}


