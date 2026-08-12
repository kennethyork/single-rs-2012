package com.rs.game.content.minigames.herblorehabitat

import com.rs.engine.command.Commands
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.captureJadinko
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.populateHerbloreHabitatDatabase
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun jadinkoDebugCommands() {
    Commands.add(Rights.DEVELOPER, "catchalljadinko", "Simulates catching all Jadinko species") { p, _ ->
        p.populateHerbloreHabitatDatabase()
        for (type in JadinkoType.entries) {
            p.captureJadinko(type)
        }
        p.sendMessage("All Jadinko species have been marked as caught.")
    }

    Commands.add(Rights.DEVELOPER, "resetjadinko", "Resets all weekly Jadinko catch statuses") { p, _ ->
        p.populateHerbloreHabitatDatabase()
        for (type in JadinkoType.entries) {
            val key = "${JadinkoType.CAUGHT_JADINKO_ATTR}${type.name}"
            p.weeklyAttributes[key] = false
            p.weeklyAttributes["HabitatGodRewardClaimed"] = false
            p.weeklyAttributes["HabitatRewardClaimed"] = false
        }
        p.sendMessage("Weekly Jadinko catch statuses have been reset.")
    }

    Commands.add(Rights.DEVELOPER, "catchjadinko", "Catches a specific Jadinko: ::catchjadinko <TYPE>") { p, args ->
        if (args.isEmpty()) {
            p.sendMessage("Usage: ::catchjadinko <TYPE>  (e.g. COMMON, DRACONIC, SARADOMIN)")
            return@add
        }
        val typeName = args[0].uppercase()
        try {
            val type = JadinkoType.valueOf(typeName)
            p.populateHerbloreHabitatDatabase()
            p.captureJadinko(type)
            p.sendMessage("Simulated catching Jadinko: $typeName.")
        } catch (e: IllegalArgumentException) {
            p.sendMessage("Unknown Jadinko type: $typeName. Check your spelling or use ::catchalljadinko.")
        }
    }

    Commands.add(Rights.PLAYER, "jadinkostatus", "Shows the status of Jadinko catches and rewards") { p, _ ->
        p.populateHerbloreHabitatDatabase()
        p.sendMessage("--- Herblore Habitat Jadinko Status ---")
        for (type in JadinkoType.entries) {
            val key = "${JadinkoType.CAUGHT_JADINKO_ATTR}${type.name}"
            val caught = p.weeklyAttributes[key] as? Boolean ?: false
            p.sendMessage("${type.name.lowercase().replaceFirstChar { it.uppercase() }} Captured: ${if (caught) "True" else "False"}")
        }
        val godReward = p.weeklyAttributes["HabitatGodRewardClaimed"] as? Boolean ?: false
        val normalReward = p.weeklyAttributes["HabitatRewardClaimed"] as? Boolean ?: false
        p.sendMessage("God reward claimed: ${if (godReward) "True" else "False"}")
        p.sendMessage("Normal reward claimed: ${if (normalReward) "True" else "False"}")
        p.sendMessage("---------------------------------------")
    }
}
