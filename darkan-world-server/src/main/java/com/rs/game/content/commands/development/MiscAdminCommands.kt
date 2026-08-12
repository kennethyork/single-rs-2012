package com.rs.game.content.commands.admin

import com.rs.Settings
import com.rs.cache.loaders.ItemDefinitions
import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.content.dnds.eviltree.spawnTree
import com.rs.game.content.dnds.shootingstar.ShootingStars
import com.rs.game.content.minigames.treasuretrails.TreasureTrailsManager
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.MapUtils
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadAdminCommands() {
    Commands.add(Rights.ADMIN, "testcoordclue [deg1, min1, dir1, deg2, min2, dir2]", "teleports you to the coordinates for clues") { p, args ->
        val deg1 = args[0].toInt()
        val min1 = args[1].toInt()
        val dir1 = when (args[2].lowercase()) {
            "north" -> 0
            "south" -> 1
            "west" -> 2
            "east" -> 3
            else -> throw IllegalArgumentException("Must be a direction as a string.")
        }
        val deg2 = args[3].toInt()
        val min2 = args[4].toInt()
        val dir2 = when (args[5].lowercase()) {
            "north" -> 0
            "south" -> 1
            "west" -> 2
            "east" -> 3
            else -> throw IllegalArgumentException("Must be a direction as a string.")
        }
        p.tele(TreasureTrailsManager.getTile(deg1, min1, dir1, deg2, min2, dir2))
    }

    Commands.add(Rights.ADMIN, "shootingstar", "spawn a shooting star") { _, _ ->
        ShootingStars.spawnStar()
    }

    Commands.add(Rights.ADMIN, "eviltree", "spawn an evil tree") { _, _ ->
        spawnTree()
    }

    Commands.add(Rights.ADMIN, "clearbank,emptybank", "Empties the players bank entirely.") { p, _ ->
        p.sendOptionDialogue("Clear bank?") { ops ->
            ops.add("Yes") { p.bank.clear() }
            ops.add("No")
        }
    }

    Commands.add(Rights.ADMIN, "god", "Toggles god mode for the player.") { p, _ ->
        val current = !p.nsv.getB("godMode")
        p.nsv.setB("godMode", current)
        p.sendMessage("GODMODE: $current")
    }

    Commands.add(Rights.ADMIN, "unnullall", "Forces the player out of a controller and unlocks them hopefully freeing any stuck-ness.") { _, _ ->
        World.players.forEach { player ->
            if (player != null) {
                player.unlock()
                player.controllerManager.forceStop()
                if (player.moveTile == null) {
                    player.tele(Settings.getConfig().playerRespawnTile)
                }
            }
        }
    }

    Commands.add(Rights.ADMIN, "infspec", "Toggles infinite special attack for the player.") { p, _ ->
        val current = !p.nsv.getB("infSpecialAttack")
        p.nsv.setB("infSpecialAttack", current)
        p.sendMessage("INFINITE SPECIAL ATTACK: $current")
    }

    Commands.add(Rights.ADMIN, "infpray", "Toggles infinite prayer for the player.") { p, _ ->
        val current = !p.nsv.getB("infPrayer")
        p.nsv.setB("infPrayer", current)
        p.sendMessage("INFINITE PRAYER: $current")
    }

    Commands.add(Rights.ADMIN, "infrun", "Toggles infinite run for the player.") { p, _ ->
        val current = !p.nsv.getB("infRun")
        p.nsv.setB("infRun", current)
        p.sendMessage("INFINITE RUN: $current")
    }

    Commands.add(Rights.ADMIN, "infrunes", "Toggles infinite runes for the player.") { p, _ ->
        val current = !p.nsv.getB("infRunes")
        p.nsv.setB("infRunes", current)
        p.sendMessage("INFINITE RUNES: $current")
    }

    Commands.add(Rights.ADMIN, "maxbank", "Sets all the item counts in the player's bank to 10m.") { p, _ ->
        p.bank.containerCopy.forEach { item ->
            item?.amount = 10_500_000
        }
    }

    Commands.add(Rights.ADMIN, "item,spawn [itemId (amount)]", "Spawns an item with specified id and amount.") { p, args ->
        val amount = if (args.size >= 2) args[1].toInt() else 1
        p.inventory.addItem(args[0].toInt(), amount)
        p.stopAll()
    }

    Commands.add(Rights.ADMIN, "master,max", "Maxes all stats out.") { p, _ ->
        for (skill in 0..24) {
            p.skills.setXp(skill, 105_000_000.0)
        }
        p.reset()
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.ADMIN, "ironman [true/false]", "Changes ironman status of the player.") { p, args ->
        p.setIronMan(args[0].toBoolean())
    }

    Commands.add(Rights.ADMIN, "boostlevel [skillId level]", "Sets a skill to a specified level.") { p, args ->
        val skill = args[0].toInt()
        val level = args[1].toInt()
        p.sendMessage("Boosting ${Skills.SKILL_NAME[skill]} by $level")
        p.skills.set(skill, level)
    }

    Commands.add(Rights.ADMIN, "search,si,itemid [item name]", "Searches for items containing the words searched.") { p, args ->
        p.packets.sendDevConsoleMessage("Searching for items containing: ${args.contentToString()}")
        for (i in 0 until Utils.getItemDefinitionsSize()) {
            val itemDef = ItemDefinitions.getDefs(i)
            val matches = args.all { arg ->
                itemDef.name.contains(arg, ignoreCase = true) && !itemDef.isLended
            }
            if (matches) {
                val noted = if (itemDef.isNoted) "(noted)" else ""
                val lent = if (itemDef.isLended) "(lent)" else ""
                p.packets.sendDevConsoleMessage("Result found: $i - ${itemDef.name} $noted$lent")
            }
        }
    }

    Commands.add(Rights.ADMIN, "hide", "Hides the player from other players.") { p, _ ->
        p.setTrulyHidden(!p.isTrulyHidden)
        p.sendMessage("Hidden: ${p.isTrulyHidden}")
    }

    Commands.add(Rights.ADMIN, "setlevel [skillId level]", "Sets a skill to a specified level.") { p, args ->
        val skill = args[0].toInt()
        val level = args[1].toInt()
        val maxLevel = if (skill == Skills.DUNGEONEERING) 120 else 99

        when {
            level < 0 || level > maxLevel -> {
                p.sendMessage("Please choose a valid level.")
                return@add
            }
            skill < 0 || skill >= Skills.SKILL_NAME.size -> {
                p.sendMessage("Please choose a valid skill.")
                return@add
            }
            else -> {
                p.skills.set(skill, level)
                p.skills.setXp(skill, Skills.getXPForLevel(level).toDouble())
                p.appearance.generateAppearanceData()
                p.reset()
                p.sendMessage("Successfully set ${Skills.SKILL_NAME[skill]} to $level.")
            }
        }
    }

    Commands.add(Rights.ADMIN, "unlockdgfloors", "Unlocks all dungeoneering floors and complexities.") { p, _ ->
        p.dungManager.maxFloor = 60
        p.dungManager.maxComplexity = 6
    }

    Commands.add(Rights.ADMIN, "reset", "Resets all stats to 1.") { p, _ ->
        for (skill in 0..24) {
            p.skills.setXp(skill, 0.0)
        }
        p.skills.init()
    }

    Commands.add(Rights.ADMIN, "tele,tp [x y (z)] or [tileHash] or [z,regionX,regionY,localX,localY]", "Teleports the player to a coordinate.") { p, args ->
        p.resetWalkSteps()
        when {
            args[0].contains(",") -> {
                val coords = args[0].split(",")
                val plane = coords[0].toInt()
                val x = (coords[1].toInt() shl 6) or coords[3].toInt()
                val y = (coords[2].toInt() shl 6) or coords[4].toInt()
                p.tele(Tile.of(x, y, plane))
            }
            args.size == 1 -> {
                p.tele(Tile.of(args[0].toInt()))
            }
            else -> {
                val z = if (args.size >= 3) args[2].toInt() else p.plane
                p.tele(Tile.of(args[0].toInt(), args[1].toInt(), z))
            }
        }
    }

    Commands.add(Rights.ADMIN, "teler,tpr [regionId]", "Teleports the player to a region id.") { p, args ->
        val regionId = args[0].toInt()
        val regionX = (regionId shr 8) * 64 + 32
        val regionY = (regionId and 0xff) * 64 + 32
        p.resetWalkSteps()
        p.tele(Tile.of(regionX, regionY, 0))
    }

    Commands.add(Rights.ADMIN, "telec,tpc [chunkId]", "Teleports the player to chunk coordinates.") { p, args ->
        val coords = MapUtils.decode(MapUtils.Structure.CHUNK, args[0].toInt())
        p.resetWalkSteps()
        p.tele(Tile.of(coords[0] * 8 + 4, coords[1] * 8 + 4, coords[2]))
    }

    Commands.add(Rights.ADMIN, "settitle [new title]", "Sets player title.") { p, args ->
        val title = args.joinToString(" ")
        p.title = title
        p.titleColor = null
        p.titleShading = null
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.ADMIN, "cleartitle,deltitle,removetitle", "Removes player title.") { p, _ ->
        p.title = null
        p.titleColor = null
        p.titleShading = null
        p.appearance.generateAppearanceData()
    }

    Commands.add(Rights.ADMIN, "spec", "Restores special attack energy to full.") { p, _ ->
        p.combatDefinitions.resetSpecialAttack()
    }

    Commands.add(Rights.ADMIN, "bank", "Opens the bank.") { p, _ ->
        p.bank.open()
    }

    Commands.add(Rights.ADMIN, "empty", "Empties the player's inventory.") { p, _ ->
        p.stopAll()
        p.inventory.reset()
    }

    Commands.add(Rights.ADMIN, "camshake [slot, v1, v2, v3, v4]", "Camera shake effect.") { p, args ->
        p.packets.sendCameraShake(
            args[0].toInt(), args[1].toInt(), args[2].toInt(),
            args[3].toInt(), args[4].toInt()
        )
    }

    Commands.add(Rights.ADMIN, "resettask", "Resets current slayer task.") { p, _ ->
        p.slayer.removeTask()
        p.updateSlayerTask()
    }

    Commands.add(Rights.ADMIN, "killme", "Kills yourself.") { p, _ ->
        p.applyHit(Hit(null, p.hitpoints, Hit.HitLook.TRUE_DAMAGE))
    }

    Commands.add(Rights.ADMIN, "update,restart [ticks]", "Restarts the server after specified number of ticks.") { _, args ->
        World.safeShutdown(args[0].toInt())
    }

    Commands.add(Rights.ADMIN, "copy [player name]", "Copies the other player's levels, equipment, and inventory.") { p, args ->
        val targetName = args.joinToString(" ")
        val target = World.getPlayerByDisplay(targetName)
        if (target == null) {
            p.sendMessage("Couldn't find player $targetName.")
            return@add
        }

        // Copy equipment
        val equip = target.equipment.itemsCopy
        equip.forEachIndexed { i, item ->
            if (item != null) {
                p.equipment.setSlot(i, Item(item))
            }
        }

        // Copy inventory
        val inv = target.inventory.items.itemsCopy
        inv.forEachIndexed { i, item ->
            if (item != null) {
                p.inventory.items.set(i, Item(item))
                p.inventory.refresh(i)
            }
        }

        // Copy skills
        for (i in p.skills.levels.indices) {
            p.skills.set(i, target.skills.getLevelForXp(i))
            p.skills.setXp(i, Skills.getXPForLevel(target.skills.getLevelForXp(i)).toDouble())
        }

        p.appearance.generateAppearanceData()
    }
}