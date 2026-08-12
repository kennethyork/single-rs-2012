package com.rs.game.content.commands.development

import com.rs.engine.command.Commands
import com.rs.game.map.ChunkManager
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadCameraCommands() {
    Commands.add(Rights.DEVELOPER, "camlook [localX localY z (speed1 speed2)]", "Points the camera at the specified tile.") { p, args ->
        val chunk = ChunkManager.getChunk(p.sceneBaseChunkId)
        var tile = Tile.of(chunk.baseX + args[0].toInt(), chunk.baseY + args[1].toInt(), p.plane)

        if (p.instancedArea != null) {
            tile = Tile.of(p.instancedArea.getLocalTile(args[0].toInt(), args[1].toInt(), p.plane))
        }

        when (args.size) {
            3 -> p.packets.sendCameraLook(tile, args[2].toInt())
            5 -> p.packets.sendCameraLook(tile, args[2].toInt(), args[3].toInt(), args[4].toInt())
        }
    }

    Commands.add(Rights.DEVELOPER, "campos [localX localY z (speed1 speed2)]", "Locks the camera to a specified tile.") { p, args ->
        val chunk = ChunkManager.getChunk(p.sceneBaseChunkId)
        var tile = Tile.of(chunk.baseX + args[0].toInt(), chunk.baseY + args[1].toInt(), p.plane)

        if (p.instancedArea != null) {
            tile = Tile.of(p.instancedArea.getLocalTile(args[0].toInt(), args[1].toInt(), p.plane))
        }

        when (args.size) {
            3 -> p.packets.sendCameraPos(tile, args[2].toInt())
            5 -> p.packets.sendCameraPos(tile, args[2].toInt(), args[3].toInt(), args[4].toInt())
        }
    }

    Commands.add(Rights.DEVELOPER, "resetcam", "Resets the camera back on the player.") { p, _ ->
        p.packets.sendResetCamera()
    }
}