package com.rs.game.content.commands.development

import com.rs.engine.command.Commands
import com.rs.game.content.combat.CombatDefinitions
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent

@ServerStartupEvent
fun loadCombatCommands() {
    Commands.add(Rights.ADMIN, "spellbook [modern/lunar/ancient]", "Switches to modern, lunar, or ancient spellbooks.") { p, args ->
        when (args[0].lowercase()) {
            "modern", "normal" -> p.combatDefinitions.setSpellbook(CombatDefinitions.Spellbook.MODERN)
            "ancient", "ancients" -> p.combatDefinitions.setSpellbook(CombatDefinitions.Spellbook.ANCIENT)
            "lunar", "lunars" -> p.combatDefinitions.setSpellbook(CombatDefinitions.Spellbook.LUNAR)
            "dung" -> p.combatDefinitions.setSpellbook(CombatDefinitions.Spellbook.DUNGEONEERING)
            else -> p.sendMessage("Invalid spellbook. Spellbooks are modern, lunar, ancient, and dung")
        }
    }

    Commands.add(Rights.ADMIN, "prayers [normal/curses]", "Switches to curses, or normal prayers.") { p, args ->
        when (args[0].lowercase()) {
            "normal", "normals" -> p.prayer.setPrayerBook(false)
            "curses", "ancients" -> p.prayer.setPrayerBook(true)
            else -> p.sendMessage("Invalid prayer book. Prayer books are normal and curses.")
        }
    }
}