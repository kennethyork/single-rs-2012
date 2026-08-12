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
package com.rs.game.content.world.areas.wilderness.forinthry

import com.rs.game.content.Effect
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.handlers.NPCInstanceHandler
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.onItemClick
import com.rs.utils.drop.Drop
import com.rs.utils.drop.DropSet
import com.rs.utils.drop.DropTable
import kotlin.math.sqrt

private val FORINTHRY_BRACE_IDS = intArrayOf(11095, 11097, 11099, 11101, 11103)
private val REVENANT_IDS = intArrayOf(
	13465, 13466, 13467, 13468, 13469, 13470, 13471, 13472,
	13473, 13474, 13475, 13476, 13477, 13478, 13479, 13480, 13481
)

private val SPAWN_ANIMATIONS = mapOf(
	13465 to 7410,
	13466 to 7447, 13467 to 7447, 13468 to 7447, 13469 to 7447,
	13470 to 7485, 13471 to 7485,
	13472 to 7424,
	13473 to 7426,
	13474 to 7403,
	13475 to 7457,
	13476 to 7464,
	13477 to 7478,
	13478 to 7417,
	13479 to 7471,
	13480 to 7440,
	13481 to 3643
)

@ServerStartupEvent
fun mapRevenantAndForinthryBrace() {
	instantiateNpc(*REVENANT_IDS.toTypedArray()) { npcId, tile -> Revenant(npcId, tile, false) }

	onItemClick(*FORINTHRY_BRACE_IDS.toTypedArray(), options = arrayOf("Repel", "Check")) { (player, item, option, equipped) ->
		when (option) {
			"Check" -> {
				val aggroProtected = player.hasEffect(Effect.REV_AGGRO_IMMUNE)
				val damageProtected = player.hasEffect(Effect.REV_IMMUNE)

				player.sendMessage("You are currently ${if (aggroProtected) "" else "not "}protected from revenant aggression.")
				player.sendMessage("You are currently ${if (damageProtected) "" else "not "}protected from revenant damage.")
			}
			"Repel" -> {
				player.refreshForinthry()
				if (item.id == 11103) {
					if (equipped)
						player.equipment.deleteItem(11103, 1)
					else
						player.inventory.deleteItem(11103, 1)
					return@onItemClick
				}
				item.id += 2
				if (equipped)
					player.equipment.refresh(item.slot)
				else
					player.inventory.refresh(item.slot)
			}
		}
	}
}

class Revenant(id: Int, tile: Tile, spawned: Boolean) : NPC(id, tile, spawned) {

	init {
		setForceAggroDistance(4)
	}

	override fun spawn() {
		super.spawn()
		anim(getSpawnAnimation())
	}

	override fun drop(killer: Player, verifyCombatDefs: Boolean) {
		try {
			val name = definitions.name
			if (name != "null")
				killer.sendNPCKill(name)

			if (killer.hasSlayerTask() && killer.slayer.isOnTaskAgainst(this))
				killer.slayer.sendKill(killer, this)

			val drops = genDrop(killer, name, definitions.combatLevel)
			drops.forEach { item -> sendDrop(killer, item) }
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun getSpawnAnimation(): Int = SPAWN_ANIMATIONS[id] ?: -1
}

private fun genDrop(killer: Player, name: String, combatLevel: Int): List<Item> {
	val drops = mutableListOf<Item>()
	val g = Utils.clampD(sqrt(combatLevel.toDouble()), 1.0, 12.0)
	val r = 60000.0 / g

	// Brawling gloves with weighted distribution
	val brawlingGloves = buildList {
		add(Drop(13845)); add(Drop(13846)); add(Drop(13847)); add(Drop(13848))
		add(Drop(13849)); add(Drop(13850)); add(Drop(13851)); add(Drop(13852))
		add(Drop(13854)); add(Drop(13856)); add(Drop(13857))
		// Smithing and Hunter gloves (2/15 chance each)
		add(Drop(13855)); add(Drop(13853))
	}.toTypedArray()

	val corruptDragonItems = arrayOf(
		Drop(13958), Drop(13961), Drop(13964), Drop(13967), Drop(13970),
		Drop(13973), Drop(13976), Drop(13979), Drop(13982), Drop(13985), Drop(13988)
	)

	val ancientWarriorsEquipment = arrayOf(
		Drop(13858), Drop(13861), Drop(13864), Drop(13867), Drop(13870),
		Drop(13873), Drop(13876), Drop(13879, 15, 50), Drop(13883, 15, 50),
		Drop(13884), Drop(13887), Drop(13890), Drop(13893), Drop(13896),
		Drop(13899), Drop(13902), Drop(13905)
	)

	val corruptAncientWarriorsEquipment = arrayOf(
		Drop(13908), Drop(13911), Drop(13914), Drop(13917), Drop(13920),
		Drop(13923), Drop(13926), Drop(13929), Drop(13932), Drop(13935),
		Drop(13938), Drop(13941), Drop(13944), Drop(13947), Drop(13950),
		Drop(13953, 15, 50), Drop(13957, 15, 50)
	)

	Utils.add(drops, DropTable.calculateDrops(killer, DropSet(
		// 1/R chance for rare items
		DropTable(1.0, r, Drop(14876)), DropTable(1.0, r, Drop(14877)),
		DropTable(1.0, r, Drop(14878)), DropTable(1.0, r, Drop(14879)),
		DropTable(1.0, r, Drop(14880)), DropTable(1.0, r, Drop(14881)),
		DropTable(1.0, r, *brawlingGloves),

		// 2/R chance for medium rare items
		DropTable(2.0, r, Drop(14882)), DropTable(2.0, r, Drop(14883)),
		DropTable(2.0, r, Drop(14884)), DropTable(2.0, r, Drop(14885)),
		DropTable(2.0, r, Drop(14886)), DropTable(2.0, r, Drop(14887)),
		DropTable(2.0, r, *corruptDragonItems),

		// 3/R chance for uncommon items
		DropTable(3.0, r, Drop(14888)), DropTable(3.0, r, Drop(14889)),
		DropTable(3.0, r, Drop(14890)), DropTable(3.0, r, Drop(14891)),
		DropTable(3.0, r, Drop(14892)),

		// 10/R chance for Ancient Warriors' equipment
		DropTable(1.00, r, *ancientWarriorsEquipment),
		DropTable(1.00, r, *corruptAncientWarriorsEquipment)
	)))

	// If no unique drops, give coins
	if (drops.isEmpty()) {
		drops.add(Item(995, (50.0 * g).toInt()))
	}

	return drops
}