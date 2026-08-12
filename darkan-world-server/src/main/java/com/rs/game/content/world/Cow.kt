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
//  Copyright © 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.world

import com.rs.game.World.data
import com.rs.game.content.skills.cooking.CowMilkingAction
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcDeath
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapCowPlugins() {
    onItemOnNpc("Cow") { (player) -> player.sendMessage("The cow doesn't want that.") }

    onNpcDeath("Cow") { data.attribs.incI("cowTrackerKills") }

    onObjectClick(31297) { (player) ->
        player.sendMessage("So far, ${data.attribs.getI("cowTrackerKills")} cows have been killed by adventurers.")
    }

    onObjectClick(8689) { (player) -> player.actionManager.setAction(CowMilkingAction()) }

    instantiateNpc("Cow") { id, tile -> Cow(id, tile) }
}

class Cow(id: Int, tile: Tile) : NPC(id, tile) {
    override fun processNPC() {
        if (Utils.random(100) == 0) forceTalk("Moo")
        super.processNPC()
    }
}