package com.rs.game.content.world

import com.rs.engine.dialogue.startConversation
import com.rs.game.tasks.WorldTasks
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick


@ServerStartupEvent
fun mapTomatoCrates() {

    onObjectClick(6839) { e ->
        e.player.startConversation {
            options("Buy a rotten tomato for 5 gold?") {
                op("Yes, please.") {
                    exec {
                        if (e.player.inventory.hasCoins(5)) {
                            e.player.inventory.removeCoins(5)
                            e.player.anim(827)
                            WorldTasks.schedule(2) {
                                e.player.inventory.addItem(2518, 1)
                                e.player.sendMessage("You retrieve a rotten tomato from the crate. Gross.")
                            }
                        } else {
                            simple("You do not have enough coins.")
                        }
                    }
                }
                op("No, thanks.") {}
            }
        }
    }
}