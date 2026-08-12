// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.bosses.godwars.factions.zaros

import com.rs.cache.loaders.Bonus
import com.rs.game.content.bosses.godwars.GWDFactionNPC
import com.rs.game.content.bosses.godwars.factions.GodFaction
import com.rs.game.content.skills.summoning.Familiar
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.handlers.NPCInstanceHandler
import com.rs.plugin.kts.instantiateNpc
import java.util.function.BiFunction

private const val CAP_BONUS = 200

private val BONUSES = arrayOf(
        arrayOf(Bonus.STAB_DEF, Bonus.SLASH_DEF, Bonus.CRUSH_DEF),
        arrayOf(Bonus.RANGE_DEF),
        arrayOf(),
        arrayOf(Bonus.MAGIC_DEF)
    )

@ServerStartupEvent
fun mapInteractions() {
    instantiateNpc(13456, 13457, 13458, 13459) { id, tile -> ZarosFactionNPC(id, tile) }
}

class ZarosFactionNPC(id: Int, tile: Tile) : GWDFactionNPC(id, tile, false, GodFaction.ZAROS) {
    override fun getPossibleTargets(): MutableList<Entity> {
        val targets = getPossibleTargets(true)
        val targetsCleaned = ArrayList<Entity>()
        for (t in targets) {
            if (t is ZarosFactionNPC || t is Familiar || hasSuperiorBonuses(t)) continue
            targetsCleaned.add(t)
        }
        return targetsCleaned
    }

    private fun hasSuperiorBonuses(t: Entity?): Boolean {
        if (t !is Player) return false
        for (bonus in BONUSES[id - 13456]) if (t.combatDefinitions.getBonus(bonus) >= (if (bonus == Bonus.RANGE_DEF) 100 else CAP_BONUS)) return true
        return false
    }
}