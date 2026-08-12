package com.rs.game.content.commands.development

import com.rs.cache.ArchiveType
import com.rs.cache.Cache
import com.rs.cache.IndexType
import com.rs.engine.command.Commands
import com.rs.lib.game.Rights
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadVarCommands() {
    Commands.add(Rights.DEVELOPER, "varcstr [id value]", "Sets a varc string value.") { p, args ->
        p.packets.sendVarcString(args[0].toInt(), Utils.concat(args, 1))
    }

    Commands.add(Rights.DEVELOPER, "varcstrloop [startId endId]", "Sets varc string values in a range.") { p, args ->
        val startId = args[0].toInt()
        val endId = args[1].toInt()

        for (i in startId until endId) {
            if (i >= Cache.STORE.getIndex(IndexType.CONFIG).getValidFilesCount(ArchiveType.VARC_STRING.id)) {
                break
            }
            p.packets.sendVarcString(i, "str$i")
        }
    }

    Commands.add(Rights.DEVELOPER, "varc [id value]", "Sets a varc value.") { p, args ->
        p.packets.sendVarc(args[0].toInt(), args[1].toInt())
    }

    Commands.add(Rights.DEVELOPER, "varcloop [startId endId value]", "Sets varc value for all varcs between 2 ids.") { p, args ->
        val startId = args[0].toInt()
        val endId = args[1].toInt()
        val value = args[2].toInt()

        for (i in startId until endId) {
            if (i >= Cache.STORE.getIndex(IndexType.CONFIG).getValidFilesCount(ArchiveType.VARC.id)) {
                break
            }
            p.packets.sendVarc(i, value)
        }
    }

    Commands.add(Rights.DEVELOPER, "var [id value]", "Sets a var value.") { p, args ->
        p.vars.setVar(args[0].toInt(), args[1].toInt())
    }

    Commands.add(Rights.DEVELOPER, "getvar [id]", "Gets a var value.") { p, args ->
        val varId = args[0].toInt()
        p.packets.sendDevConsoleMessage("Var: $varId -> ${p.vars.getVar(varId)}")
    }

    Commands.add(Rights.DEVELOPER, "varloop [startId endId value]", "Sets var value for all vars between 2 ids.") { p, args ->
        val startId = args[0].toInt()
        val endId = args[1].toInt()
        val value = args[2].toInt()

        for (i in startId until endId) {
            if (i >= Cache.STORE.getIndex(IndexType.CONFIG).getValidFilesCount(ArchiveType.VARS.id)) {
                break
            }
            p.vars.setVar(i, value)
        }
    }

    Commands.add(Rights.DEVELOPER, "getvarbit [id]", "Gets a varbit value.") { p, args ->
        val varbitId = args[0].toInt()
        p.packets.sendDevConsoleMessage("Varbit: $varbitId -> ${p.vars.getVarBit(varbitId)}")
    }

    Commands.add(Rights.DEVELOPER, "varbit [id value]", "Sets a varbit value.") { p, args ->
        p.vars.setVarBit(args[0].toInt(), args[1].toInt())
    }

    Commands.add(Rights.DEVELOPER, "savevarbit [id value]", "Saves a varbit value.") { p, args ->
        p.vars.saveVarBit(args[0].toInt(), args[1].toInt())
    }

    Commands.add(Rights.DEVELOPER, "varbitloop [startId endId value]", "Sets varbit value for all varbits between 2 ids.") { p, args ->
        val startId = args[0].toInt()
        val endId = args[1].toInt()
        val value = args[2].toInt()

        for (i in startId until endId) {
            if (i >= Utils.getVarbitDefinitionsSize()) {
                break
            }
            p.vars.setVarBit(i, value)
        }
    }
}