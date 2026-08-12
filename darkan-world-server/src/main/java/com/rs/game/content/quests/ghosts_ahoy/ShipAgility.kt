package com.rs.game.content.quests.ghosts_ahoy

import com.rs.game.content.skills.agility.Agility
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.events.ObjectClickEvent
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapGangPlank() {
    fun handleGangPlankClick(e: ObjectClickEvent, startTile: Tile, endTile: Tile) {
        if (e.player.getLevel(Constants.AGILITY) >= 25) {
            e.player.lock()
            Agility.crossLedge(e.player, startTile, endTile, 10.0, false)
            e.player.schedule {
                wait(4)
                e.player.fadeScreen {
                    e.player.tele(
                        e.player.x,
                        e.player.y,
                        if (e.player.plane == 1) e.player.plane - 1 else e.player.plane + 1
                    )
                    e.player.unlock()
                }
            }
        } else {
            e.player.simpleDialogue("You do not have the required agility level to walk the plank.")
        }
    }
    onObjectClick(5285) { e ->
        handleGangPlankClick(e, Tile.of(3605, 3545, 1), Tile.of(3605, 3548, 0))
    }
    onObjectClick(5286) { e ->
        handleGangPlankClick(e, Tile.of(3605, 3548, 0), Tile.of(3605, 3545, 1))
    }
}
@ServerStartupEvent
fun mapShipRocks() {
    onObjectClick(5269) { e ->
        var endTile = Tile.of(3604,3550,0)
        e.player.walkToAndExecute(e.obj) {
            when(e.obj.tile) {
                Tile.of(3604,3550,0) -> endTile = Tile.of(3602,3550,0)
                Tile.of(3602,3550,0) -> endTile = Tile.of(3604,3550,0)

                Tile.of(3599,3552,0) -> endTile = Tile.of(3597,3552,0)
                Tile.of(3597,3552,0) -> endTile = Tile.of(3599,3552,0)

                Tile.of(3595,3554,0) -> endTile = Tile.of(3595,3556,0)
                Tile.of(3595,3556,0) -> endTile = Tile.of(3595,3554,0)

                Tile.of(3597,3559,0) -> endTile = Tile.of(3597,3561,0)
                Tile.of(3597,3561,0) -> endTile = Tile.of(3597,3559,0)

                Tile.of(3599,3564,0) -> endTile = Tile.of(3601,3564,0)
                Tile.of(3601,3564,0) -> endTile = Tile.of(3599,3564,0)
            }
            jumpGap(e.player, endTile)
        }
    }
}


fun jumpGap(player: Player, endTile: Tile) {
    player.lock()
    player.anim(3067)
    player.drainRunEnergy(player.runEnergy*0.05)
    player.forceMove(endTile, 5, 60) {
        player.skills.addXp(Constants.AGILITY, 10.0)
        player.unlock()
    }
}
