package com.rs.game.content.commands.development

import com.rs.Settings
import com.rs.cache.loaders.NPCDefinitions
import com.rs.cache.loaders.ObjectType
import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.map.ChunkManager
import com.rs.game.model.entity.async.schedule
import com.rs.game.tasks.Task
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Animation
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.utils.ObjAnimList

@ServerStartupEvent
fun loadAnimationCommands() {
    val animRights = if (Settings.getConfig().isDebug) Rights.PLAYER else Rights.DEVELOPER

    Commands.add(Rights.DEVELOPER, "spotanim,gfx [id height]", "Creates a spot animation on top of the player.") { p, args ->
        val height = if (args.size == 1) 0 else args[1].toInt()
        p.spotAnim(args[0].toInt(), 0, height)
    }

    Commands.add(animRights, "anim,emote [id]", "Animates the player with specified ID.") { p, args ->
        val animId = args[0].toInt()
        if (animId <= Utils.getAnimationDefinitionsSize())
            p.anim(animId)
    }

    Commands.add(animRights, "sync,animgfx [animId gfxId]", "Animates the player with specified ID and plays a SpotAnim at the same time.") { p, args ->
        val animId = args[0].toInt()
        val gfxId = args[1].toInt()

        if (animId <= Utils.getAnimationDefinitionsSize() && gfxId <= Utils.getSpotAnimDefinitionsSize())
            p.sync(animId, gfxId)
    }

    Commands.add(animRights, "bas,render [id]", "Sets the BAS of the player to specified ID.") { p, args ->
        p.appearance.setBAS(args[0].toInt())
    }

    Commands.add(animRights, "companim [npcId]", "Prints out animations compatible with the npc id.") { p, args ->
        val npcId = args[0].toInt()
        if (npcId <= Utils.getNPCDefinitionsSize()) {
            val defs = NPCDefinitions.getDefs(npcId)
            if (defs != null) {
                val compatAnims = defs.compatibleAnimations.toString()
                p.packets.sendDevConsoleMessage("$npcId: $compatAnims")
                p.sendMessage("$npcId: $compatAnims")
                Logger.debug(Commands::class.java, "companim", compatAnims)
            }
        }
    }

    Commands.add(Rights.DEVELOPER, "setlook [slot id]", "Appearance setting") { p, args ->
        p.appearance.setLook(args[0].toInt(), args[1].toInt())
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.DEVELOPER, "objectanim,oanim [x y animId (objectType)]", "Makes an object play an animation.") { p, args ->
        val x = args[0].toInt()
        val y = args[1].toInt()
        val animId = if (args.size == 3) args[2].toInt() else args[3].toInt()

        val obj = if (args.size == 3) {
            World.getObject(Tile.of(x, y, p.plane))
        } else {
            World.getObject(Tile.of(x, y, p.plane), ObjectType.forId(args[2].toInt()))
        }

        if (obj == null) {
            p.packets.sendDevConsoleMessage("No object was found.")
            return@add
        }

        p.packets.sendObjectAnimation(obj, Animation(animId))
    }

    Commands.add(Rights.DEVELOPER, "objectanimloop,oanimloop [x y (startId) (endId)]", "Loops through object animations.") { p, args ->
        val x = args[0].toInt()
        val y = args[1].toInt()
        val tile = Tile.of(x, y, p.plane)
        val obj = ChunkManager.getChunk(tile.chunkId).getSpawnedObject(tile)

        if (obj == null) {
            p.packets.sendDevConsoleMessage("Could not find object at [x=$x, y=$y, z=${p.plane}].")
            return@add
        }

        if (!ObjAnimList.inited()) ObjAnimList.init()

        val start = if (args.size > 2) args[2].toInt() else 10
        val end = if (args.size > 3) args[3].toInt() else 20000

        p.schedule("loopAnim") {
            var current = start
            while (current < end) {
                World.sendObjectAnimation(obj, Animation(current))
                p.sendMessage("Current animation: $current")
                wait(2)
                for (i in (current + 1) until end) {
                    if (!ObjAnimList.isUsed(i)) {
                        current = i
                        break
                    }
                }
            }
        }
    }

    Commands.add(animRights, "tonpc,pnpc,npcme [npcId]", "Transforms the player into an NPC.") { p, args ->
        val npcId = args[0].toInt()
        if (npcId <= Utils.getNPCDefinitionsSize()) {
            p.appearance.transformIntoNPC(npcId)
        }
    }

    Commands.add(Rights.DEVELOPER, "headicon", "Set custom headicon.") { p, args ->
        p.tempAttribs.setI("customHeadIcon", args[0].toInt())
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.DEVELOPER, "skull", "Set custom skull.") { p, args ->
        p.setSkullInfiniteDelay(args[0].toInt())
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.DEVELOPER, "getapp", "Prints out appearance data") { p, _ ->
        p.appearance.printDebug()
    }
}