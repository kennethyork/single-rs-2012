package com.rs.game.content.commands.development

import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.content.dnds.penguins.PenguinServices
import com.rs.game.content.dnds.penguins.Penguins
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.utils.Ticks
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.time.Duration
import com.rs.engine.variables.peng_spy_week_peng

@ServerStartupEvent
fun loadPenguinCommands() {
    Commands.add(Rights.ADMIN, "penguin_status", "Returns the location of the active polar bear.") { p, _ ->
        val penguinSpawnService = PenguinServices.penguinSpawnService
        var activePenguinCount = 0

        Penguins.entries
            .filter { penguin ->
                penguinSpawnService.spawnedNPCs.values.any { npc ->
                    npc.respawnTile == penguin.tile
                }
            }
            .forEach { penguin ->
                activePenguinCount++
                p.packets.sendDevConsoleMessage("$activePenguinCount. $penguin - ${penguin.tile} - ${penguin.wikiLocation}")
            }

        p.packets.sendDevConsoleMessage("Total active penguins: $activePenguinCount")

        val polarBearManager = PenguinServices.polarBearManager
        p.packets.sendDevConsoleMessage("Polar Bear currently located in: ${polarBearManager.getLocationName(polarBearManager.getCurrentLocationId())}")

        Commands.processCommand(p, "penguin_next_reset", true, false)
    }

    Commands.add(Rights.ADMIN, "penguin_participants", "Returns a list of all Penguin Hide and Seek participants for the current Penguin/Polar Bear spawns.") { p, _ ->
        try {
            val allPenguins = PenguinServices.penguinSpawnService.getPenguins()

            if (allPenguins.isEmpty()) {
                p.packets.sendDevConsoleMessage("No penguins found.")
            } else {
                val uniqueSpotters = mutableSetOf<String>()
                val participantsInfo = StringBuilder()

                allPenguins.forEach { penguin ->
                    uniqueSpotters.addAll(penguin.spotters)
                }

                if (uniqueSpotters.isNotEmpty()) {
                    participantsInfo.append("Penguin Hide and Seek Participants (Spotters):\n")
                    uniqueSpotters.forEach { spotter ->
                        participantsInfo.append("$spotter\n")
                    }
                    p.packets.sendDevConsoleMessage(participantsInfo.toString())
                } else {
                    p.packets.sendDevConsoleMessage("No participants found.")
                }

                p.packets.sendDevConsoleMessage("Total spotters for week ${PenguinServices.penguinHideAndSeekManager.getCurrentWeek()}: ${uniqueSpotters.size}")
            }
        } catch (e: Exception) {
            p.packets.sendDevConsoleMessage("Couldn't retrieve participants.")
        }
    }

    Commands.add(Rights.ADMIN, "penguin_respawn", "Respawns both penguins and polar bear.") { p, _ ->
        val polarBearManager = PenguinServices.polarBearManager
        polarBearManager.setLocation(false)
        p.packets.sendDevConsoleMessage("Polar Bear respawned at: ${polarBearManager.getLocationName(polarBearManager.getCurrentLocationId())}")

        val penguinSpawnService = PenguinServices.penguinSpawnService
        val penguinManager = PenguinServices.penguinHideAndSeekManager

        penguinSpawnService.spawnedNPCs.values.forEach(NPC::finish)
        penguinSpawnService.spawnedNPCs.clear()
        penguinManager.checkAndSpawn()

        p.packets.sendDevConsoleMessage("Penguins respawned.")
        Commands.processCommand(p, "penguin_status", true, false)
    }

    Commands.add(Rights.ADMIN, "penguin_reset [type]", "Resets penguins or polar bear and spawns a new set. Type can be 'penguins' or 'polarbear'.") { p, args ->
        if (args.isEmpty() || (!args[0].equals("penguins", ignoreCase = true) && !args[0].equals("polarbear", ignoreCase = true))) {
            p.packets.sendDevConsoleMessage("Usage: ::penguin_reset [penguins|polarbear]")
            return@add
        }

        when (args[0].lowercase()) {
            "penguins" -> {
                val penguinSpawnService = PenguinServices.penguinSpawnService
                if (penguinSpawnService.removeAllSpawns()) {
                    penguinSpawnService.prepareNew()
                    World.players.forEach { player ->
                        player.vars.saveVarBit(peng_spy_week_peng, 0)
                    }
                }
                Commands.processCommand(p, "penguin_status", true, true)
            }
            "polarbear" -> {
                val polarBearManager = PenguinServices.polarBearManager
                polarBearManager.setLocation(true)
                p.packets.sendDevConsoleMessage("Polar Bear manually changed to: ${polarBearManager.getLocationName(polarBearManager.getCurrentLocationId())}")
            }
        }
    }

    Commands.add(Rights.ADMIN, "penguin_next_reset", "Returns the date/time of the next reset.") { p, _ ->
        val penguinManager = PenguinServices.penguinHideAndSeekManager

        val nextResetTime = penguinManager.getLastReset().plusWeeks(1)
        val millisUntilReset = ChronoUnit.MILLIS.between(penguinManager.getCurrentDayAndTime(), nextResetTime)

        val resetDate = Date.from(nextResetTime.toInstant())
        val formatter = SimpleDateFormat("EEE, MMM d, yyyy 'at' HH:mm:ss z")
        val formattedResetTime = formatter.format(resetDate)

        p.packets.sendDevConsoleMessage("Next reset is scheduled for: $formattedResetTime, or in ${Ticks.breakDownOfTicks((millisUntilReset / 600).toInt())}.")
    }
}
