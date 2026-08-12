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
package com.rs.game.content.holidayevents.christmas

import com.rs.cache.loaders.ItemDefinitions
import com.rs.game.World.spawnNPC
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick
import com.rs.plugin.kts.onXpDrop
import com.rs.utils.Areas
import com.rs.utils.spawns.NPCSpawn
import com.rs.utils.spawns.NPCSpawns
import com.rs.utils.spawns.ObjectSpawn
import com.rs.utils.spawns.ObjectSpawns
import java.util.*

private const val ACTIVE = false
private val TRADEABLE_REWARDS = setOf(1050, 1053, 1055, 1057, 1959)

private val randomFood: Int
    get() = 15428 + Utils.random(4)

@ServerStartupEvent
fun initObjects() {
    if (!ACTIVE) return
    val center = Tile.of(3212, 3428, 3)
    for (x in -10..9) for (y in -10..9) ObjectSpawns.add(ObjectSpawn(3701, 10, 1, center.transform(x * 5, y * 5, 0)))

    ObjectSpawns.add(ObjectSpawn(12258, 10, 0, Tile.of(3210, 3425, 0), "Cupboard to Christmas event."))

    ObjectSpawns.add(ObjectSpawn(4483, 10, 0, Tile.of(2648, 5667, 0), "Bank chest"))
    ObjectSpawns.add(ObjectSpawn(70761, 10, 0, Tile.of(2650, 5666, 0), "Bonfire"))
    ObjectSpawns.add(ObjectSpawn(4483, 10, 0, Tile.of(2662, 5667, 0), "Bank chest"))
    ObjectSpawns.add(ObjectSpawn(70761, 10, 0, Tile.of(2660, 5666, 0), "Bonfire"))
    ObjectSpawns.add(ObjectSpawn(24887, 10, 0, Tile.of(2665, 5661, 0), "Furnace"))

    ObjectSpawns.add(ObjectSpawn(21273, 10, 0, Tile.of(2645, 5671, 0), "Arctic pine bottom"))
    ObjectSpawns.add(ObjectSpawn(47759, 10, 0, Tile.of(2644, 5671, 0), "Arctic pine presents"))
    ObjectSpawns.add(ObjectSpawn(47761, 10, 0, Tile.of(2647, 5671, 0), "Arctic pine presents"))
    ObjectSpawns.add(ObjectSpawn(21273, 10, 0, Tile.of(2640, 5665, 0), "Arctic pine bottom"))
    ObjectSpawns.add(ObjectSpawn(47759, 10, 0, Tile.of(2641, 5664, 0), "Arctic pine presents"))
    ObjectSpawns.add(ObjectSpawn(47761, 10, 0, Tile.of(2640, 5667, 0), "Arctic pine presents"))

    ObjectSpawns.add(ObjectSpawn(46485, 10, 0, Tile.of(2667, 5670, 0), "Ice fishing"))
    NPCSpawns.add(NPCSpawn(324, Tile.of(2667, 5670, 0), "Fishing spot"))
    NPCSpawns.add(NPCSpawn(327, Tile.of(2667, 5670, 0), "Fishing spot"))

    ObjectSpawns.add(ObjectSpawn(46485, 10, 0, Tile.of(2665, 5670, 0), "Ice fishing"))
    NPCSpawns.add(NPCSpawn(334, Tile.of(2665, 5670, 0), "Fishing spot"))
    NPCSpawns.add(NPCSpawn(328, Tile.of(2665, 5670, 0), "Fishing spot"))

    NPCSpawns.add(NPCSpawn(5080, Tile.of(2666, 5645, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2667, 5644, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2664, 5645, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2665, 5647, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2670, 5646, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2672, 5647, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2673, 5649, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2671, 5650, 0), "Carnivorous chinchompa"))
    NPCSpawns.add(NPCSpawn(5080, Tile.of(2671, 5645, 0), "Carnivorous chinchompa"))

    ObjectSpawns.add(ObjectSpawn(67036, 10, 0, Tile.of(2672, 5679, 0), "Summoning obelisk"))

    val n = spawnNPC(14256, Tile.of(2658, 5671, 0), permaDeath = false, withFunction = true)
    n.setLoadsUpdateZones()
    n.setPermName("Yule-cien")
    n.hitpoints = Int.MAX_VALUE / 2
    n.combatDefinitions.hitpoints = Int.MAX_VALUE / 2
    n.capDamage = 750
    n.setForceMultiArea(true)
    n.isForceMultiAttacked = true
}

@ServerStartupEvent
fun mapLandOfSnowPlugins() {
    onXpDrop { e ->
        if (!ACTIVE) return@onXpDrop
        if (Areas.withinArea("christmasevent", e.player.chunkId)) {
            if (e.player.bonusXpRate != 0.10) {
                e.player.sendMessage("<shad=000000><col=ff0000>You've been granted a 10% experience boost for skilling within the Land of Snow!")
                e.player.bonusXpRate = 0.10
            }
            val chance = e.xp / 75000.0
            if (Math.random() < chance) {
                val reward = TRADEABLE_REWARDS.random()
                e.player.sendMessage("<shad=000000><col=ff0000>You found a " + ItemDefinitions.getDefs(reward).name + " while skilling!")
                if (e.player.inventory.hasFreeSlots()) e.player.inventory.addItemDrop(reward, 1)
                else {
                    e.player.sendMessage("<shad=000000><col=ff0000>as you did not have room in your inventory, it has been added to your bank.")
                    e.player.bank.addItem(Item(reward, 1), true)
                }
            }
        } else e.player.bonusXpRate = 0.0
    }

    onObjectClick(12258, 47766) { (player, obj) ->
        player.fadeScreen { player.tele(if (obj.id == 12258) Tile.of(2646, 5659, 0) else Tile.of(3211, 3424, 0)) }
    }

    onObjectClick(28296) { (player) ->
        if (!ACTIVE) return@onObjectClick
        if (player.inventory.addItem(11951, player.inventory.freeSlots))
            player.anim(2282)
    }

    onObjectClick(47777, 47778) { (player) ->
        if (!ACTIVE) return@onObjectClick
        val food: Int = randomFood
        if (player.inventory.addItem(food, 1))
            player.sendMessage("You take " + Utils.addArticle(ItemDefinitions.getDefs(food).name.lowercase(Locale.getDefault())) + ".")
    }
}
