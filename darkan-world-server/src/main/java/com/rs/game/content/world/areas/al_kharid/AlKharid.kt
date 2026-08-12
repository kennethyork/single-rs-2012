package com.rs.game.content.world.areas.al_kharid

import com.rs.engine.pathfinder.Direction
import com.rs.game.content.skills.agility.Agility
import com.rs.game.content.world.areas.global.AgilityShortcuts
import com.rs.game.content.world.doors.Doors
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick
import com.rs.plugin.kts.onPlayerStep

@ServerStartupEvent
fun handleAlKharidArea() {

	// shantayPass
	onPlayerStep(
			Tile.of(3303, 3116, 0),
			Tile.of(3303, 3117, 0),
			Tile.of(3305, 3116, 0),
			Tile.of(3305, 3117, 0)
	) { (player, step) ->
		if (step.y == 3116 && step.dir == Direction.SOUTH) {
			if (!player.inventory.containsItem(1854, 1)) {
				player.sendMessage("You should check with Shantay for a pass.")
				return@onPlayerStep
			}
			player.inventory.deleteItem(1854, 1)
		}
		step.setCheckClip(false)
		player.setRunHidden(false)
		WorldTasks.delay(3) {
			player.setRunHidden(true)
		}
	}

	// clickShantayPass
	onObjectClick(12774) { (player) ->
			player.sendMessage("Walk on through with a pass!")
	}

	// handleGates
	onObjectClick(35549, 35551) { (player, obj) ->
		if (player.inventory.hasCoins(10)) {
			player.inventory.removeCoins(10)
			Doors.handleDoubleDoor(player, obj)
		} else {
			player.sendMessage("You need 10 gold to pass through this gate.")
		}
	}

	// handleStrykewyrmStile
	onObjectClick(48208) { (player, obj) ->
		val dx = if (player.x < obj.x) 3 else -3
		AgilityShortcuts.climbOver(player, player.transform(dx, 0, 0))
	}

	// handleMiningSiteShortcut
	onObjectClick(9331, 9332) { (player, obj) ->
		if (!Agility.hasLevel(player, 38)) {
			player.sendMessage("You need 38 agility")
			return@onObjectClick
		}
		when {
			obj.tile.matches(Tile.of(3306, 3315, 0)) -> {
				player.forceMove(Tile.of(3303, 3315, 0), 2050, 10, 60)
			}
			obj.tile.matches(Tile.of(3304, 3315, 0)) -> {
				player.forceMove(Tile.of(3307, 3315, 0), 2049, 10, 60)
			}
		}
	}

	// handleBrimhavenStairs
	onObjectClick(45, 46) { (player, obj) ->
		val (dx, dy, dz) = when (obj.id) {
			45 -> Triple(
				0,
					when (obj.rotation) {
						2 -> -4
						0 -> 4
                    	else -> 0
					},
				1
            	)
			46 -> Triple(
				0,
					when (obj.rotation) {
						2 -> 4
						0 -> -4
                    	else -> 0
					},
				-1
				)
            else -> Triple(0, 0, 0)
		}
		player.tele(player.transform(dx, dy, dz))
	}
}
