package com.rs.game.content.commands.debug

import com.rs.cache.loaders.ObjectDefinitions
import com.rs.cache.loaders.ObjectType
import com.rs.cache.loaders.map.ClipFlag
import com.rs.engine.command.Commands
import com.rs.engine.pathfinder.Direction
import com.rs.engine.pathfinder.WorldCollision
import com.rs.engine.pathfinder.routeEntityToTile
import com.rs.game.World
import com.rs.game.content.world.doors.Doors
import com.rs.game.map.ChunkManager
import com.rs.game.map.instance.InstancedChunk
import com.rs.game.model.gameobject.GameObject
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.net.packets.encoders.HintTrail
import com.rs.lib.util.Logger
import com.rs.lib.util.Utils.clampI
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadDebugCommands() {
    Commands.add(Rights.DEVELOPER, "areaobj [(radius)]", "Lists out nearby objects.") { p, args ->
        val radius = if (args.isNotEmpty()) args[0].toInt() else 0
        val objects = mutableListOf<GameObject>()

        if (radius == 0) {
            val objs = World.getBaseObjects(p.tile)
            objs.toList().let { objects.addAll(it) }
        } else {
            for (z in -3 until 3) {
                for (x in -radius until radius) {
                    for (y in -radius until radius) {
                        val tile = p.transform(x, y, z)
                        if (tile.plane >= 4 || tile.plane < 0) continue

                        val objs = World.getBaseObjects(tile)
                        objs.toList().let { objects.addAll(it) }
                    }
                }
            }
        }

        objects.forEach { obj ->
            Logger.debug(Commands::class.java, "areaobj", obj.toString())
            Logger.debug(Commands::class.java, "areaobj", "vb: ${obj.definitions.varpBit}")
        }
    }

    Commands.add(Rights.DEVELOPER, "npcwalkdir", "Spawn npc walking dir.") { p, args ->
        val dir = Direction.entries.firstOrNull {
            it.name.equals(args[0], ignoreCase = true)
        } ?: return@add

        val npc = World.spawnNPC(1, p.tile, true, false, null)
        npc.addWalkSteps(p.tile.x + (dir.dx * 20), p.tile.y + (dir.dy * 20))
    }

    Commands.add(Rights.DEVELOPER, "sd", "Search for a door pair.") { _, args ->
        Doors.searchDoors(args[0].toInt())
    }

    Commands.add(Rights.DEVELOPER, "vischunks", "Toggles the visualization of chunks.") { p, _ ->
        val current = !p.nsv.getB("visChunks")
        p.nsv.setB("visChunks", current)
        p.sendMessage("Visualizing chunks: $current")
    }

    Commands.add(Rights.DEVELOPER, "reloadlocalmap", "Forces your local map to reload") { p, _ ->
        ChunkManager.getChunk(p.chunkId).getAllBaseObjects(true).forEach { obj ->
            p.sendMessage(obj.toString())
        }

        val chunk = ChunkManager.getChunk(p.chunkId)
        if (chunk is InstancedChunk) {
            p.sendMessage("${chunk.originalBaseX}, ${chunk.originalBaseY} - ${chunk.rotation}")
        }

        p.isForceNextMapLoadRefresh = true
        p.loadMapRegions()
    }

    Commands.add(Rights.DEVELOPER, "proj [id]", "Sends a projectile over the player.") { p, args ->
        val projId = args[0].toInt()
        p.tempAttribs.setI("tempProjCheck", projId)
        World.sendProjectile(
            Tile.of(p.x + 5, p.y, p.plane),
            Tile.of(p.x - 5, p.y, p.plane),
            projId,
            Pair(40, 40),
            0, 30, 0
        )
    }

    Commands.add(Rights.DEVELOPER, "projrot [id next/prev]", "Rotates through projectiles.") { p, args ->
        var projId = p.tempAttribs.getI("tempProjCheck", 0)

        World.sendProjectile(
            Tile.of(p.x + 5, p.y, p.plane),
            Tile.of(p.x - 5, p.y, p.plane),
            projId,
            Pair(40, 40),
            0, 30, 0
        )

        p.packets.sendDevConsoleMessage("Projectile: $projId")

        projId = when (args[0]) {
            "next" -> clampI(projId + 1, 0, 5000)
            else -> clampI(projId - 1, 0, 5000)
        }

        p.tempAttribs.setI("tempProjCheck", projId)
    }

    Commands.add(Rights.DEVELOPER, "object [id (type) (rotation)]", "Spawns an object south of the player's tile.") { p, args ->
        val objectId = args[0].toInt()
        val rotation = if (args.size > 2) args[2].toInt() else 0
        val type = if (args.size > 1) {
            ObjectType.forId(args[1].toInt()) ?: ObjectDefinitions.getDefs(objectId).types[0]
        } else {
            ObjectDefinitions.getDefs(objectId).types[0]
        }

        World.spawnObject(GameObject(objectId, type, rotation, p.x, p.y - 1, p.plane))
    }

    Commands.add(Rights.DEVELOPER, "objecttmp [id (type) (rotation)]", "Spawns an object south of the player's tile for 1 tick.") { p, args ->
        val objectId = args[0].toInt()
        val rotation = if (args.size > 2) args[2].toInt() else 0
        val type = if (args.size > 1) {
            ObjectType.forId(args[1].toInt()) ?: ObjectDefinitions.getDefs(objectId).types[0]
        } else {
            ObjectDefinitions.getDefs(objectId).types[0]
        }

        val targetTile = p.transform(0, -1, 0)
        val before = World.getSpawnedObject(targetTile)

        if (before != null) {
            WorldTasks.schedule(3) { World.spawnObject(before) }
        }

        World.spawnObjectTemporary(
            GameObject(objectId, type, rotation, p.x, p.y - 1, p.plane),
            1
        )
    }

    Commands.add(Rights.DEVELOPER, "script", "Runs a clientscript with no arguments.") { p, args ->
        p.packets.sendRunScriptBlank(args[0].toInt())
    }

    Commands.add(Rights.DEVELOPER, "scriptargs", "Runs a clientscript with arguments.") { p, args ->
        val scriptArgs = Array<Any>(args.size - 1) { i ->
            try {
                args[i + 1].toInt()
            } catch (e: NumberFormatException) {
                args[i + 1]
            }
        }
        p.packets.sendRunScript(args[0].toInt(), *scriptArgs)
    }

    Commands.add(Rights.DEVELOPER, "tileflags", "Get the tile flags for the tile you're standing on.") { p, _ ->
        p.sendMessage("${ClipFlag.getFlags(WorldCollision.getFlags(p.tile))}")
    }

    Commands.add(Rights.DEVELOPER, "hinttrail [x y modelId]", "Sets a hint trail from the player to the specified location.") { p, args ->
        val x = args[0].toInt()
        val y = args[1].toInt()
        val modelId = args[2].toInt()

        val route = routeEntityToTile(p, Tile.of(x, y, p.plane), 25)
        val bufferX = IntArray(route.size)
        val bufferY = IntArray(route.size)

        route.coords.forEachIndexed { i, tile ->
            bufferX[i] = tile.packed and 0xFFFF
            bufferY[i] = (tile.packed shr 16) and 0xFFFF
        }

        p.session.writeToQueue(HintTrail(Tile.of(p.tile), modelId, bufferX, bufferY, route.size))
    }
}