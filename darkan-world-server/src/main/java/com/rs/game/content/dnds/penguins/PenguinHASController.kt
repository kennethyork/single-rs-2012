package com.rs.game.content.dnds.penguins

import com.google.gson.Gson
import com.rs.Settings
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.dnds.penguins.PenguinServices.penguinHideAndSeekManager
import com.rs.game.content.dnds.penguins.PenguinServices.penguinSpawnService
import com.rs.game.content.dnds.penguins.PenguinServices.polarBearManager
import com.rs.game.tasks.WorldTasks
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.getInteractionDistance
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onNpcClick
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.WorldPersistentData
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import com.rs.engine.variables.peng_spy_week_peng
import com.rs.engine.variables.peng_spy_bear

const val PENGUIN_POINTS = "PenguinPoints"
const val MAX_PENGUIN_POINTS = 50
const val UNLOCKED_PENGUIN_HAS = "Unlocked_PenguinHAS"
const val PENGUINHAS_LAST_RESET_WORLD_ATTR = "penguinHAS_LastReset"
const val PENGUINS_WORLD_ATTR = "penguinHAS_Penguins"
const val POLAR_BEAR_WORLD_ATTR = "penguinHAS_PolarBear"
const val PENGUIN_COUNT_WEEKLY_ATTR = "penguinsSpotted"
const val POLAR_BEAR_WEEKLY_ATTR = "polarBearSpotted"

object PenguinServices {
    val penguinHideAndSeekManager = PenguinManager()
    val penguinSpawnService = PenguinSpawnService()
    val polarBearManager = PolarBearManager()
}

@ServerStartupEvent
fun mapPenguinHideAndSeekInteractions() {

    // Penguin interactions
    instantiateNpc(8104, 8105, 8107, 8108, 8109, 8110, 14415, 14766) { id, tile -> PenguinNPC(id, tile) }
    getInteractionDistance(8104, 8105, 8107, 8108, 8109, 8110, 14415, 14766) { _, _ -> 3 }
    onNpcClick(8104, 8105, 8107, 8108, 8109, 8110, 14415, 14766) { (player, npc) ->
        if (player.get(UNLOCKED_PENGUIN_HAS) as Boolean) {

            if (player.getWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR) == 10) {
                player.sendMessage("You've already spotted all the penguin spies this week!")
                return@onNpcClick
            }

            val penguins = penguinSpawnService.getPenguins()
            val penguin = penguins.find { it.location == npc.respawnTile }

            if (penguin != null) {
                synchronized(penguin) {
                    if (penguin.spotters.contains(player.username)) {
                        player.sendMessage("You've already spotted this penguin spy.")
                        return@onNpcClick
                    }

                    penguin.addSpotter(player.username)
                    World.data.attribs.setO<String>(PENGUINS_WORLD_ATTR, Gson().toJson(penguins))
                    player.incWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR, 1)

                    player.lock(3)
                    player.anim(10355)
                    player.jingle(345)
                    player.incrementCount("Penguin Agents spied", 1)

                    player.vars.saveVarBit(peng_spy_week_peng, player.getWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR))

                    player.sendMessage("You spy on the penguin.")

                    val currentPoints = player.getI(PENGUIN_POINTS).coerceAtLeast(0)
                    val pointsToAdd = if (player.isQuestComplete(Quest.COLD_WAR)) penguin.points else 1
                    val newPoints = (currentPoints + pointsToAdd).coerceAtMost(MAX_PENGUIN_POINTS)

                    player.set(PENGUIN_POINTS, newPoints)
                    if (newPoints == MAX_PENGUIN_POINTS) {
                        player.sendMessage("<col=FF0000>You have reached a maximum of $MAX_PENGUIN_POINTS Penguin Points. You should spend them with Larry to continue earning more.</col>")
                    }

                    if (player.getWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR) == 10) {
                        player.sendMessage("You've spotted all the penguin spies this week!")
                    }
                }
            } else {
                player.simpleDialogue("You don't recognise this penguin.")
            }
        } else {
            player.startConversation {
                player(HeadE.NERVOUS, "What in ${Settings.getConfig().serverName} is this?!")
                player(HeadE.CONFUSED, "I wonder if Larry at Ardougne Zoo knows anything about this...")
            }
        }
    }

    // Polar bear interactions
    onLogin { (player) ->
        player.vars.setVarBit(peng_spy_bear, polarBearManager.getCurrentLocationId())
        player.vars.saveVarBit(peng_spy_week_peng, player.getWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR))
    }
    onObjectClick(43094, 43095, 43096, 43097, 43098, 43099) { (player) ->
        if (player.get(UNLOCKED_PENGUIN_HAS) as Boolean) {
            if (player.isQuestComplete(Quest.HUNT_FOR_RED_RAKTUBER)) {

                if (player.getWeeklyI(POLAR_BEAR_WEEKLY_ATTR) == 1) {
                    player.sendMessage("You've already spotted the polar bear agent!")
                    return@onObjectClick
                }

                val polarBearEnum = PolarBearLocation.entries.find { it.id == polarBearManager.getCurrentLocationId() }

                if (polarBearEnum != null) {
                    val polarBear = getPolarBear()
                    if (polarBear != null) {
                        synchronized(polarBear) {
                            if (polarBear.spotters.contains(player.username)) {
                                player.sendMessage("You've already spotted the polar bear agent!")
                                return@onObjectClick
                            }

                            polarBear.addSpotter(player.username)
                            World.data.attribs.setO<String>(POLAR_BEAR_WORLD_ATTR, Gson().toJson(polarBear))
                            player.setWeeklyI(POLAR_BEAR_WEEKLY_ATTR, 1)

                            player.lock(5)
                            player.anim(10355)
                            player.jingle(345)
                            player.incrementCount("Polar Bear Agents found", 1)
                            player.sendMessage("You found the polar bear agent.")

                            val currentPoints = player.getI(PENGUIN_POINTS).coerceAtLeast(0)
                            val newPoints = (currentPoints + polarBear.points).coerceAtMost(MAX_PENGUIN_POINTS)

                            player.set(PENGUIN_POINTS, newPoints)
                            if (newPoints == MAX_PENGUIN_POINTS) {
                                player.sendMessage("<col=FF0000>You have reached a maximum of $MAX_PENGUIN_POINTS Penguin Points. You should spend them with Larry to continue earning more.</col>")
                            }
                        }
                    } else {
                        player.simpleDialogue("You don't recognise this polar bear.")
                    }
                } else {
                    player.simpleDialogue("You don't recognise this polar bear.")
                }
            } else {
                player.sendMessage("You need to have completed Hunt For Red Raktuber to spot this agent.")
            }
        } else {
            player.startConversation {
                player(HeadE.NERVOUS, "Is that a polar bear in the well?!")
                player(HeadE.CONFUSED, "I wonder if Larry at Ardougne Zoo knows anything about this...")
            }
        }
    }

    // Spy Notebook
    onItemClick(13732, options = arrayOf("Read")) { (player) ->
        val penguinsSpottedCount = player.getWeeklyI(PENGUIN_COUNT_WEEKLY_ATTR)
        val polarBearSpottedCount = player.getWeeklyI(POLAR_BEAR_WEEKLY_ATTR)

        var dialogueMessage = "You have spotted $penguinsSpottedCount ${if (penguinsSpottedCount == 1) "penguin" else "penguins"} this week."

        if (player.isQuestComplete(Quest.HUNT_FOR_RED_RAKTUBER)) {
            dialogueMessage += "<br><br>You have ${if (polarBearSpottedCount == 1) "spotted the polar bear" else "not yet spotted the polar bear"} this week."
        }

        dialogueMessage += "<br><br>You have ${player.getI(PENGUIN_POINTS).coerceAtLeast(0)} Penguin ${if (player.getI(PENGUIN_POINTS) == 1) "Point" else "Points"} to spend with Larry."

        player.simpleDialogue(dialogueMessage)
    }

}

@ServerStartupEvent
fun initializePenguinHideAndSeek() {
    penguinHideAndSeekManager.checkAndSpawn() // Penguin & Polar Bear spawning on server startup

    // Reset task
    WorldTasks.scheduleHourly {
        penguinHideAndSeekManager.checkAndSpawn()
    }
}

class PenguinManager() {
    fun checkAndSpawn() {

        penguinSpawnService.loadSpawns()

        val lastResetDateString: String? = World.data.attribs.getO<String>(PENGUINHAS_LAST_RESET_WORLD_ATTR)
        val lastResetDate: ZonedDateTime? = lastResetDateString?.let {
            ZonedDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC)

        val shouldPerformWeeklyReset = when {
            lastResetDate == null -> true
            hasResetTimePassed(lastResetDate, currentDateTime) -> true
            else -> false
        }

        if (shouldPerformWeeklyReset) {
            penguinSpawnService.prepareNew()
            polarBearManager.setLocation(true)

            val lastReset = getLastReset()
            World.data.attribs.setO<String>(PENGUINHAS_LAST_RESET_WORLD_ATTR, lastReset.format(DateTimeFormatter.ISO_INSTANT))
            WorldPersistentData.save()
        } else {
            penguinSpawnService.prepareExisting()
            polarBearManager.setLocation(false)
        }
    }

    fun hasResetTimePassed(lastResetDate: ZonedDateTime, currentDateTime: ZonedDateTime): Boolean {
        val weeksBetween = ChronoUnit.WEEKS.between(lastResetDate, currentDateTime)
        return weeksBetween >= 1
    }

    fun getLastReset(): ZonedDateTime {
        val today = LocalDate.now()
        val lastReset = today.minusDays(((today.dayOfWeek.value - 3 + 7) % 7).toLong())
        return lastReset.atStartOfDay(ZoneOffset.UTC)
    }

    fun getPenguinLocationHint(playerUsername: String): String? {
        val penguins = penguinSpawnService.getPenguins()

        val unspottedHints = penguins.filter { penguin ->
            !penguin.spotters.contains(playerUsername)
        }.mapNotNull { penguin ->
            Penguins.entries.find { it.tile == penguin.location }?.locationHint
        }

        return if (unspottedHints.isNotEmpty()) unspottedHints.random() else null
    }

    fun getCurrentDayAndTime(): ZonedDateTime {
        return ZonedDateTime.now(ZoneOffset.UTC)
    }
    fun getCurrentWeek(): Int {
        return ZonedDateTime.now(ZoneOffset.UTC).get(WeekFields.ISO.weekOfWeekBasedYear())
    }
    fun getCurrentMonth(): Month {
        return ZonedDateTime.now(ZoneOffset.UTC).month
    }

}
