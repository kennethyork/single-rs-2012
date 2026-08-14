package com.rs.game.content.world.npcs

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.annotations.ServerStartupEvent

@PluginEventHandler
object SimulatedPlayerSocialCommands {
    @JvmStatic
    @ServerStartupEvent
    fun register() {
        if (!Settings.getConfig().isSinglePlayer) return
        Commands.add(Rights.PLAYER, "ollamastatus,botchatstatus", "Shows the local bot conversation status.") { player, _ ->
            SimulatedPlayerOllama.sendStatus(player)
        }
        Commands.add(Rights.PLAYER, "ollamaforget,forgetbotchat", "Erases your saved local bot conversation memory.") { player, _ ->
            val removed = SimulatedPlayerOllama.forget(player)
            player.sendMessage("Erased $removed saved bot conversation${if (removed == 1) "" else "s"} from this computer.")
        }
        Commands.add(Rights.PLAYER, "ollamamodel,botmodel [model/reset/list]", "Changes the local bot conversation model.") { player, args ->
            val choice = args.firstOrNull()?.trim().orEmpty()
            when (choice.lowercase()) {
                "" -> SimulatedPlayerOllama.sendModelHelp(player)
                "list" -> SimulatedPlayerOllama.sendModelHelp(player)
                "reset", "default" -> SimulatedPlayerOllama.resetModel(player)
                else -> SimulatedPlayerOllama.selectModel(player, choice)
            }
        }
        Commands.add(Rights.PLAYER, "clanparty,clanpk [dismiss/status]", "Summons recruited clan members for bossing and Wilderness PKing.") { player, args ->
            when (args.firstOrNull()?.lowercase()) {
                "dismiss", "leave", "off" -> {
                    val removed = SimulatedPlayerCompanionManager.dismissClanParty(player)
                    player.sendMessage("Dismissed $removed clan combat companion${if (removed == 1) "" else "s"}.")
                }
                "status", "list" -> SimulatedPlayerCompanionManager.sendClanPartyStatus(player)
                else -> SimulatedPlayerCompanionManager.summonClanParty(player)
            }
        }
        Commands.add(Rights.PLAYER, "botgroups", "Lists nearby simulated-player activity groups.") { player, _ ->
            SimulatedPlayerActivityManager.sendNearbyGroups(player)
        }
        Commands.add(Rights.PLAYER, "exporthighscores,websitehighscores", "Exports local player and bot highscores for the website.") { player, _ ->
            val file = SimulatedPlayerHighscores.export(player)
            player.sendMessage("Exported ${SimulatedPlayerPopulationManager.activeCount() + 1} highscore entries to <col=00ffff>${file.absolutePath}</col>.")
            player.sendMessage("The website can remember this local file and refresh it automatically. Nothing is uploaded.")
        }
        Commands.add(Rights.PLAYER, "botgroup,activitygroup [create/skill/status/leave/disband]", "Manages your simulated-player activity group.") { player, args ->
            when (args.firstOrNull()?.lowercase()) {
                "create", "form" -> SimulatedPlayerActivityManager.createPlayerGroup(player, args.drop(1).joinToString(" "))
                "skill", "activity", "train" -> SimulatedPlayerActivityManager.setPlayerGroupSkill(player, args.drop(1).joinToString(" "))
                "leave" -> SimulatedPlayerActivityManager.leaveJoinedGroup(player)
                "disband", "delete" -> SimulatedPlayerActivityManager.disbandPlayerGroup(player)
                "status", "list", null -> SimulatedPlayerActivityManager.sendPlayerGroupStatus(player)
                else -> player.sendMessage("Use ::botgroup create [name], ::botgroup skill [skill], ::botgroup status, ::botgroup leave, or ::botgroup disband.")
            }
        }
    }
}
