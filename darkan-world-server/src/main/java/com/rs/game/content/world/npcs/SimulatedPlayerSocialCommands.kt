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
    }
}
