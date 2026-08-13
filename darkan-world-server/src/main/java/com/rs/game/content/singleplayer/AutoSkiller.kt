package com.rs.game.content.singleplayer

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent

private const val ENABLED_KEY = "singlePlayerAutoSkiller"
private const val SKILL_KEY = "singlePlayerAutoSkillerSkill"
private const val LOCKED_KEY = "singlePlayerAutoSkillerLocked"

private const val TARGET_LEVEL = 99

private fun skillIndexOrNull(name: String?): Int? {
    if (name == null) return null
    val query = when (name.lowercase()) {
        "hp", "hitpoints", "life", "lifepoints" -> "constitution"
        "range", "rangedd" -> "ranged"
        "def" -> "defence"
        "att", "atk" -> "attack"
        "str" -> "strength"
        "rc", "runecraft" -> "runecrafting"
        "wc" -> "woodcutting"
        "fm" -> "firemaking"
        "dg", "dung" -> "dungeoneering"
        "summ" -> "summoning"
        "con" -> "construction"
        else -> name.lowercase()
    }
    val index = Skills.SKILL_NAME.indexOfFirst { it.equals(query, ignoreCase = true) }
    return if (index == -1) null else index
}

/** First skill still below the target level, or null once every skill is finished. */
private fun nextUnfinishedSkill(player: Player, from: Int): Int? {
    for (offset in 0 until Skills.SIZE) {
        val skill = Math.floorMod(from + offset, Skills.SIZE)
        if (player.skills.getLevelForXp(skill) < TARGET_LEVEL) return skill
    }
    return null
}

private fun currentSkill(player: Player) = Math.floorMod(player.getI(SKILL_KEY, 0), Skills.SIZE)

private fun startSkill(player: Player, skill: Int, locked: Boolean) {
    player.set(SKILL_KEY, skill)
    player.set(LOCKED_KEY, locked)
    player.set(ENABLED_KEY, true)
    player.sendMessage("Autopilot is now training ${Skills.SKILL_NAME[skill]} (level ${player.skills.getLevelForXp(skill)}).")
}

/**
 * Lightweight local autopilot. It trains one skill at a time rather than rotating
 * every tick: a chosen skill is trained on its own, and in sequential mode the
 * autopilot only moves to the next skill once the current one reaches 99.
 */
@ServerStartupEvent
fun loadSinglePlayerAutoSkiller() {
    if (!Settings.getConfig().isSinglePlayer()) return

    Commands.add(Rights.PLAYER, "autobot [skill/on/off/next/status]", "Trains one skill at a time in single-player mode.") { player, args ->
        when (val arg = args.firstOrNull()?.lowercase()) {
            "off", "stop" -> {
                player.set(ENABLED_KEY, false)
                player.sendMessage("Autopilot disabled.")
            }
            "on", "all", "start" -> {
                val skill = nextUnfinishedSkill(player, currentSkill(player))
                if (skill == null) {
                    player.set(ENABLED_KEY, false)
                    player.sendMessage("Every skill is already $TARGET_LEVEL. Nothing left to train.")
                } else {
                    startSkill(player, skill, locked = false)
                    player.sendMessage("It will move on to the next skill once this one reaches $TARGET_LEVEL.")
                }
            }
            "next", "skip" -> {
                val skill = nextUnfinishedSkill(player, currentSkill(player) + 1)
                if (skill == null) {
                    player.set(ENABLED_KEY, false)
                    player.sendMessage("Every skill is already $TARGET_LEVEL. Nothing left to train.")
                } else
                    startSkill(player, skill, locked = false)
            }
            null, "status" -> {
                if (!player.getBool(ENABLED_KEY))
                    player.sendMessage("Autopilot is disabled. Use ::autobot <skill>, or ::autobot on to train every skill one at a time.")
                else {
                    val skill = currentSkill(player)
                    val mode = if (player.getBool(LOCKED_KEY)) "this skill only" else "then the next skill"
                    player.sendMessage("Autopilot is training ${Skills.SKILL_NAME[skill]} (level ${player.skills.getLevelForXp(skill)}) — $mode.")
                }
            }
            else -> {
                val skill = skillIndexOrNull(arg)
                if (skill == null)
                    player.sendMessage("No such skill: '$arg'. Use ::autobot <skill name>, ::autobot on, ::autobot next, or ::autobot off.")
                else
                    startSkill(player, skill, locked = true)
            }
        }
    }

    // Every 6 seconds, add XP to the single skill the player is currently on. XP passes
    // through Skills.addXp, so the selected 25x/50x world rate and normal level-up
    // behavior both apply.
    WorldTasks.scheduleTimer(10, 10) {
        World.players.forEach { player ->
            if (player != null && player.hasStarted() && !player.hasFinished() && player.getBool(ENABLED_KEY)) {
                val skill = currentSkill(player)
                val locked = player.getBool(LOCKED_KEY)

                if (!locked && player.skills.getLevelForXp(skill) >= TARGET_LEVEL) {
                    val next = nextUnfinishedSkill(player, skill + 1)
                    if (next == null) {
                        player.set(ENABLED_KEY, false)
                        player.sendMessage("Autopilot finished: every skill has reached $TARGET_LEVEL.")
                        return@forEach
                    }
                    player.set(SKILL_KEY, next)
                    player.sendMessage("Autopilot is moving on to ${Skills.SKILL_NAME[next]}.")
                    return@forEach
                }

                val baseXp = 10.0 + player.skills.getLevelForXp(skill)
                player.skills.addXp(skill, baseXp)
            }
        }
        true
    }
}
