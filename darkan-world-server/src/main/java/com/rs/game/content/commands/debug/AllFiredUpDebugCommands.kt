package com.rs.game.content.commands.debug

import com.rs.engine.command.Commands
import com.rs.engine.quest.Quest
import com.rs.game.World.getServerTicks
import com.rs.game.content.minigames.allfiredup.Beacon
import com.rs.game.content.minigames.allfiredup.isBeaconDyingOut
import com.rs.game.content.minigames.allfiredup.isBeaconEmpty
import com.rs.game.content.minigames.allfiredup.isBeaconFilled
import com.rs.game.content.minigames.allfiredup.isBeaconLit
import com.rs.lib.game.Rights
import com.rs.lib.util.Logger
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadAllFiredUpDebugCommands() {
    Commands.add(Rights.DEVELOPER, "dumpbeacons", "Dumps all beacon information for the player to the console.") { p, _ ->
        Logger.debug(Commands::class.java, "dumpbeacons", "Beacon dump for ${p.displayName} (quest stage: ${p.questManager.getStage(Quest.ALL_FIRED_UP)}, server ticks: ${getServerTicks()})")
        for (beacon in Beacon.entries) {
            val state = when {
                p.isBeaconEmpty(beacon) -> "EMPTY"
                p.isBeaconFilled(beacon) -> "FILLED"
                p.isBeaconLit(beacon) -> "LIT"
                p.isBeaconDyingOut(beacon) -> "DYING_OUT"
                else -> "UNKNOWN"
            }
            val burnExpiry = p.getL("${beacon.name}BurnTime")
            val dyingExpiry = p.getL("${beacon.name}DyingTime")
            val active = p.get("Beacon_${beacon.name}") != null
            Logger.debug(
                Commands::class.java,
                "dumpbeacons",
                "${beacon.name} | state=$state | active=$active | burnExpiry=$burnExpiry | dyingExpiry=$dyingExpiry"
            )
        }
        p.sendMessage("Beacon dump printed to console.")
    }
}
