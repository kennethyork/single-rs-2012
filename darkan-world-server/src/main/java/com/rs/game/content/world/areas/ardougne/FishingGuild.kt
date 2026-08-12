package com.rs.game.content.world.areas.ardougne

import com.rs.game.content.world.doors.Doors
import com.rs.lib.Constants
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapFishingGuildInteractions() {

    onObjectClick(49016, 49014) { (player, obj) ->
        if (player.skills.getLevelForXp(Constants.FISHING) < 68) {
            player.sendMessage("You need a Fishing level of at least 68 in order to pass through this gate.")
            return@onObjectClick
        }
        Doors.handleDoubleDoor(player, obj)
    }

}
