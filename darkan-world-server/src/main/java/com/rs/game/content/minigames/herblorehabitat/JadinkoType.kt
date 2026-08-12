package com.rs.game.content.minigames.herblorehabitat

import com.rs.game.World.getPlayersInChunkRange
import com.rs.game.content.Effect
import com.rs.game.content.minigames.herblorehabitat.npcs.papaMamboDistractionComplete
import com.rs.game.content.minigames.herblorehabitat.npcs.papaMamboGodDistractionComplete
import com.rs.game.content.skills.farming.PatchLocation
import com.rs.game.content.skills.farming.ProduceType
import com.rs.game.content.skills.hunter.BoxHunterType
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Tile

//null meaning any, requireEmptyXPatch = true meaning must be empty.
enum class JadinkoType(
	val npcId: Int,
	val level: Int,
	val boxHunterType: BoxHunterType?,
	val varbit: Int?,
	val varbitValue: Int,
	val interfaceVarbit: Int?,
	val drop: Int,
	val juju: Boolean,
	val feature: HabitatFeature?,
	val blossom: ProduceType?,
	val bush: ProduceType?,
	val tree: ProduceType?,
	val requireEmptyFeature: Boolean = false,
	val requireEmptyFlowerPatch: Boolean = false,
	val requireEmptyBushPatch: Boolean = false,
	val requireEmptyTreePatch: Boolean = false,
) {
	COMMON(13108, 70, BoxHunterType.COMMON_JADINKO, 8415, 1, 8386, 19971, false, null, null, null, null, requireEmptyFeature = true, requireEmptyBushPatch = true, requireEmptyTreePatch = true),
	SHADOW(-1, 71, null, null, -1, 8393, 19977, false, HabitatFeature.ABANDONED_HOUSE, ProduceType.Red_blossom, null, null, requireEmptyBushPatch = true, requireEmptyTreePatch = true),
	IGNEOUS(13132, 74, BoxHunterType.IGNEOUS_JADINKO, 8435, 2, 8395, 19980, false, HabitatFeature.THERMAL_VENT, ProduceType.Blue_blossom, ProduceType.Lergberry, ProduceType.Orange),
	CANNIBAL(13144, 75, BoxHunterType.CANNIBAL_JADINKO, 8445, 2, 8391, 19975, true, HabitatFeature.TALL_GRASS, ProduceType.Green_blossom, ProduceType.Kalferberry, null, requireEmptyTreePatch = true),
	AQUATIC(13132, 76, BoxHunterType.AQUATIC_JADINKO, 8435, 1, 8387, 19976, true, HabitatFeature.POND, ProduceType.Red_blossom, ProduceType.Kalferberry, ProduceType.Apple),
	AMPHIBIOUS(13120, 77, BoxHunterType.AMPHIBIOUS_JADINKO, 8425, 1, 8388, 19972, false, HabitatFeature.POND, ProduceType.Blue_blossom, ProduceType.Lergberry, null, requireEmptyTreePatch = true),
	CARRION(13144, 78, BoxHunterType.CARRION_JADINKO, 8445, 1, 8390, 19974, false, HabitatFeature.BONEYARD, ProduceType.Green_blossom, ProduceType.Kalferberry, null, requireEmptyTreePatch = true),
	DISEASED(-1, 78, null, null, -1, 8394, 19979, false, HabitatFeature.BONEYARD, null, null, ProduceType.Banana, requireEmptyFlowerPatch = true),
	CAMOUFLAGED(-1, 79, null, null, -1, 8389, 19978, true, HabitatFeature.STANDING_STONES, null, ProduceType.Lergberry, null, requireEmptyTreePatch = true),
	DRACONIC(13120, 80, BoxHunterType.DRACONIC_JADINKO, 8425, 2, 8392, 19973, true, HabitatFeature.DARK_PIT, ProduceType.Red_blossom, ProduceType.Lergberry, null, requireEmptyTreePatch = true),
	SARADOMIN(13156, 81, BoxHunterType.SARADOMIN_JADINKO, 8452, 1, null, 19981, true, null, ProduceType.Blue_blossom, null, null),
	GUTHIX(13156, 81, BoxHunterType.GUTHIX_JADINKO, 8452, 2, null, 19982, true, null, ProduceType.Green_blossom, null, null),
	ZAMORAK(13156, 81, BoxHunterType.ZAMORAK_JADINKO, 8452, 3, null, 19983, true, null, ProduceType.Red_blossom, null, null);

	fun shouldSend(player: Player): Boolean {
		if (player.isWardedAgainst(this))
			return false
		if (!isJujuMet(player))
			return false
		if (!player.meetsHunterLevel(this))
			return false
		return allRequirementsMet(player)
	}

	fun isFlowerMet(player: Player, location: PatchLocation) =
		if (requireEmptyFlowerPatch) {
			player.getPatch(location)?.seed == null
		} else {
			blossom?.let { produce ->
				player.isGrowing(location, produce)
			} ?: true
		}

	fun isBushMet(player: Player, location: PatchLocation) =
		if (requireEmptyBushPatch) {
			player.getPatch(location)?.seed == null
		} else {
			bush?.let { produce ->
				player.isGrowing(location, produce)
			} ?: true
		}

	fun isTreeMet(player: Player, location: PatchLocation) =
		if (requireEmptyTreePatch) {
			player.getPatch(location)?.seed == null
		} else {
			tree?.let { produce ->
				player.isGrowing(location, produce)
			} ?: true
		}

	fun isFeatureMet(player: Player): Boolean =
		if (requireEmptyFeature)
			feature == null && player.habitatFeature == null
		else
			feature?.let { it == player.habitatFeature } ?: true

	fun isJujuMet(player: Player): Boolean =
		if (!juju)
			true
		else
			player.hasEffect(Effect.JUJU_HUNTER)

	fun requirementsMet(player: Player): Int {
		var hasMet = 0
		if (isJujuMet(player)) hasMet++
		if (isFeatureMet(player)) hasMet++
		if (isFlowerMet(player, PatchLocation.Herblore_Habitat_flower)) hasMet++
		if (isBushMet(player, PatchLocation.Herblore_Habitat_bush)) hasMet++
		if (isTreeMet(player, PatchLocation.Herblore_Habitat_fruit_tree)) hasMet++
		return hasMet
	}

	fun allRequirementsMet(player: Player): Boolean {
		val met = requirementsMet(player)
		return met == totalRequired
	}

	val totalRequired = 5

	private fun Player.isWardedAgainst(type: JadinkoType) =
		hasEffect(Effect.MAMBO_WARDING) && savingAttributes["MAMBO_WARDING_TYPE"] == type

	private fun Player.meetsHunterLevel(type: JadinkoType) =
		skills.getLevel(Skills.HUNTER) >= type.level

	companion object {
		val godJadinkos = listOf(SARADOMIN, GUTHIX, ZAMORAK)
		private val trackerJadinkos = listOf(SHADOW, DISEASED, CAMOUFLAGED)

		@JvmStatic
		fun Player.getAllActiveJadinkoTypes(): List<JadinkoType> {
			return JadinkoType.entries.filter { it.shouldSend(this) }
		}

		@JvmStatic
		fun JadinkoType.isGodJadinko(): Boolean =
			this in godJadinkos

		fun JadinkoType.isTrackerJadinko(): Boolean =
			this in trackerJadinkos

		@JvmStatic
		fun Player.checkJadinkoAttraction() {
			for (type in JadinkoType.entries) {
				if (!type.shouldSend(this) && !type.isGodJadinko()) continue
				val met = type.requirementsMet(this)
				val allMet = type.allRequirementsMet(this)
				sendAttractionMessage(this, type, met, allMet)
			}
		}

		@JvmStatic
		private fun sendAttractionMessage(player: Player, type: JadinkoType, met: Int, allMet: Boolean) {
			if (player in getPlayersInChunkRange(Tile.of(2959, 2915, 0).chunkId, 5)) {
				val jadinkoName = type.name.lowercase().replaceFirstChar { it.uppercase() }
				if (type.isGodJadinko() && type.isJujuMet(player)) {
					if (allMet) {
						player.sendMessage("You have attracted some $jadinkoName Jadinko to the nearby island.")
					} else if (met >= 1) {
						player.sendMessage("You have met $met/${type.totalRequired} of the requirements to attract a $jadinkoName jadinko.")
					}
				} else if (allMet) {
					val direction = when (type) {
						COMMON, DRACONIC, AMPHIBIOUS -> "North"
						else -> "South"
					}
					player.vars.saveVarBit(type.interfaceVarbit!!, 1)
					player.sendMessage("You have attracted some $jadinkoName Jadinko to the $direction.")
				}
			}
		}

		@JvmStatic
		fun updateGroup(player: Player, vararg types: JadinkoType) {
			val allVarbits = entries
				.mapNotNull { it.varbit }
				.toSet()
			val desiredStates: Map<Int, Int> = types
				.filter { it.varbit != null && it.shouldSend(player) }
				.associate { it.varbit!! to it.varbitValue }
			allVarbits.forEach { vid ->
				val value = desiredStates[vid] ?: 0
				player.vars.setVarBit(vid, value, true)
			}
		}

		const val CAUGHT_JADINKO_ATTR = "HabitatCaptured_"

		fun Player.populateHerbloreHabitatDatabase() {
			for (type in JadinkoType.entries) {
				val key = CAUGHT_JADINKO_ATTR + type.name
				if (!weeklyAttributes.containsKey(key)) {
					weeklyAttributes[key] = false
					weeklyAttributes["HabitatRewardClaimed"] = false
					weeklyAttributes["HabitatGodRewardClaimed"] = false
				}
			}
		}

		@JvmStatic
		fun Player.captureJadinko(type: JadinkoType) {
			populateHerbloreHabitatDatabase()
			val key = CAUGHT_JADINKO_ATTR + type.name
			if (weeklyAttributes[key] as Boolean) return
			weeklyAttributes[key] = true
			val total = JadinkoType.entries.size
			val caughtCount = JadinkoType.entries.count {
				weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
			}
			sendMessage("You have caught $caughtCount/$total species of Jadinko so far this week.")
			val caughtSet = JadinkoType.entries.filter {
				weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
			}.toSet()
			val allNonGod = JadinkoType.entries.filterNot { it.isGodJadinko() }
			if (allNonGod.all { it in caughtSet })   papaMamboDistractionComplete(this)
			if (JadinkoType.entries.all { it in caughtSet }) papaMamboGodDistractionComplete(this)
		}

		fun Player.getWeeklyCaughtJadinkoCount(): Int {
			populateHerbloreHabitatDatabase()
			return JadinkoType.entries.count {
				weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
			}
		}
	}
}




