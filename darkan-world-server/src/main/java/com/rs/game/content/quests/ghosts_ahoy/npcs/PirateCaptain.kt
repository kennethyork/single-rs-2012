package com.rs.game.content.quests.ghosts_ahoy.npcs

import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapPirateCaptainGA() {
    onObjectClick(5287) { (player) ->
        player.simpleDialogue("The pirate captain ignores you and continues to stare lifelessly at nothing, as he has clearly been dead for some time.")
    }
}