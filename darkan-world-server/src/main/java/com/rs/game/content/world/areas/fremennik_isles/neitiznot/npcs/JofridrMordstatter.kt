package com.rs.game.content.world.areas.fremennik_isles.neitiznot.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun jofridrMordstatterInit() {
    onNpcClick(5509) { (player, npc) ->
        player.startConversation {
            npc(npc, CALM_TALK, "Hello there. Would you like to see the goods I have for sale?")
            options("Select an option") {
                op("Yes please, Jofridr") {
                    player(CALM_TALK, "Yes please, Jofridr.")
                    exec { ShopsHandler.openShop(player, "neitiznot_supplies") }
                }
                op("No thank you, Jofridr") {
                    player(CALM_TALK, "No thank you, Jofridr.")
                    npc(npc, CALM_TALK, "Fair thee well.")
                    player(CALM_TALK, "Why do you have so much wool in your store? I haven't seen any sheep anywhere.")
                    npc(npc, CALM_TALK, "Ah, I have contacts on the mainland. I have a sailor friend who brings me crates of wool on a regular basis.")
                    player(CALM_TALK, "What do you trade for it?")
                    npc(npc, CALM_TALK, "Rope of course! What else can we sell? Fish would go off before it got so far south.")
                    player(CALM_TALK, "Where does all this rope go?")
                    npc(npc, CALM_TALK, "Err, I don't remember the name of the place very well. Dreinna? Drennor? Something like that.")
                    player(CALM_TALK, "That's very interesting. Thanks Jofridr.")
                }
            }
        }
    }
}
