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
package com.rs.game.content.minigames.herblorehabitat

import com.rs.game.World.sendSpotAnim
import com.rs.game.content.minigames.herblorehabitat.HabitatFeature.entries
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.checkJadinkoAttraction
import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.TeleType
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick
import com.rs.engine.variables.ecosystem_other

enum class HabitatFeature(val id: Int, val level: Int, val button: Int, val mamboCost: Int) {
    BONEYARD(7, 56, 75, 56000),
    ABANDONED_HOUSE(3, 57, 71, 57000),
    THERMAL_VENT(4, 59, 72, 59000),
    TALL_GRASS(2, 62, 70, 62000),
    POND(1, 65, 68, 65000),
    STANDING_STONES(5, 70, 73, 70000),
    DARK_PIT(6, 80, 74, 80000);

    companion object {
        private val idMap = entries.associateBy { it.id }
        operator fun get(id: Int) = idMap[id]
    }
}

var Player.habitatFeature: HabitatFeature?
    get() = HabitatFeature[vars.getVarBit(ecosystem_other)]
    set(value) {
        vars.saveVarBit(ecosystem_other, value?.id ?: 0)
        this.checkJadinkoAttraction()
    }
private val BUTTON_MAP: Map<Int, HabitatFeature> = entries.associateBy { it.button }

fun forButton(buttonId: Int): HabitatFeature? = BUTTON_MAP[buttonId]


@ServerStartupEvent
fun handleFeatureClicks() {
    onLogin { e ->
        val id = e.player.vars.getVarBit(ecosystem_other)
        val feature = HabitatFeature.entries.find { it.id == id }
        e.player.habitatFeature = feature
    }
    onObjectClick(56803) { e -> e.player.interfaceManager.sendInterface(459) }

    onButtonClick(459) { e ->
        if (e.componentId in 68..75) {
            val toBuild = forButton(e.componentId)
            if (toBuild == null) {
                e.player.habitatFeature = null
            } else {
                if (e.player.skills.getLevel(Constants.CONSTRUCTION) < toBuild.level) {
                    e.player.sendMessage("You need a Construction level of ${toBuild.level} to build a ${toBuild.name.replace('_', ' ')}.")
                    return@onButtonClick
                }
                e.player.checkJadinkoAttraction()
                e.player.habitatFeature = toBuild
            }
            sendSpotAnim(Tile.of(2952, 2908, 0), SpotAnim(1605))
            e.player.closeInterfaces()
        }
    }

    onItemClick(20046, options = arrayOf("Teleport")) { e ->
        Magic.sendTeleportSpell(e.player, 7082, 7084, 1229, 1229, 1, 0.0, Tile.of(2952, 2933, 0), 4, true, TeleType.ITEM, null)
    }
}
