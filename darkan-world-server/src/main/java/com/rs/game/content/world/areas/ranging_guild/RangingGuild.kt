package com.rs.game.content.world.areas.ranging_guild

import com.rs.game.World
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

private object RangingGuildTower {
    val LADDER_GROUND: Tile = Tile.of(2668, 3428, 0)
    val LADDER_FIRST: Tile = Tile.of(2668, 3428, 1)
    val LADDER_SECOND: Tile = Tile.of(2668, 3428, 2)
}

@ServerStartupEvent
fun mapRangingGuild() {
    // Loaded ladder only has UP.
    World.getObject(RangingGuildTower.LADDER_FIRST)?.let { existing ->
        if (existing.id == 1747) {
            World.spawnObject(GameObject(1748, existing.type, existing.rotation, Tile.of(existing.tile)))
        }
    }
    // Ground floor -> first floor
    onObjectClick(2511, tiles = arrayOf(RangingGuildTower.LADDER_GROUND)) { (player, obj, option) ->
        if (option == "Climb-up") {
            player.useLadder(RangingGuildTower.LADDER_FIRST)
        }
    }
    // First floor -> second floor (default climb up), or back down to ground
    onObjectClick(1748, tiles = arrayOf(RangingGuildTower.LADDER_FIRST)) { (player, obj, option) ->
        when (option) {
            "Climb", "Climb-up" -> player.useLadder(RangingGuildTower.LADDER_SECOND)
            "Climb-down" -> player.useLadder(RangingGuildTower.LADDER_GROUND)
        }
    }
    // Second floor -> first floor
    onObjectClick(2512, tiles = arrayOf(RangingGuildTower.LADDER_SECOND)) { (player, obj, option) ->
        if (option == "Climb-down") {
            player.useLadder(RangingGuildTower.LADDER_FIRST)
        }
    }
}