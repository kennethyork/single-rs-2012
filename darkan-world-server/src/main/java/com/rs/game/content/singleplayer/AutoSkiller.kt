package com.rs.game.content.singleplayer

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.model.entity.player.Skills
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent

private const val ENABLED_KEY = "singlePlayerAutoSkiller"
private const val NEXT_SKILL_KEY = "singlePlayerAutoSkillerNextSkill"

/** Lightweight local autopilot that rotates through every skill while the game is running. */
@ServerStartupEvent
fun loadSinglePlayerAutoSkiller() {
    if (!Settings.getConfig().isSinglePlayer()) return

    Commands.add(Rights.PLAYER, "autobot [on/off/status]", "Automatically trains every skill in single-player mode.") { player, args ->
        when (args.firstOrNull()?.lowercase()) {
            "on", "all", "start" -> {
                player.set(ENABLED_KEY, true)
                player.sendMessage("All-skill autopilot enabled. It will rotate through all 25 skills while you are logged in.")
            }
            "off", "stop" -> {
                player.set(ENABLED_KEY, false)
                player.sendMessage("All-skill autopilot disabled.")
            }
            else -> player.sendMessage("All-skill autopilot is ${if (player.getBool(ENABLED_KEY)) "enabled" else "disabled"}. Use ::autobot on or ::autobot off.")
        }
    }

    // Every 6.6 seconds, advance one skill. XP passes through Skills.addXp, so
    // the selected 25x/50x world rate and normal level-up behavior both apply.
    WorldTasks.scheduleTimer(10, 10) {
        World.players.forEach { player ->
            if (player != null && player.hasStarted() && !player.hasFinished() && player.getBool(ENABLED_KEY)) {
                val skill = Math.floorMod(player.getI(NEXT_SKILL_KEY, 0), Skills.SIZE)
                val baseXp = 10.0 + player.skills.getLevelForXp(skill)
                player.skills.addXp(skill, baseXp)
                player.set(NEXT_SKILL_KEY, (skill + 1) % Skills.SIZE)
            }
        }
        true
    }
}
