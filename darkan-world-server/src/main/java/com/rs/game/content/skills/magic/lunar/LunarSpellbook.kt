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
package com.rs.game.content.skills.magic.lunar

import com.rs.cache.loaders.ItemDefinitions
import com.rs.engine.dialogue.startConversation
import com.rs.engine.dialogue.statements.MakeXStatement
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.Effect
import com.rs.game.content.Potion
import com.rs.game.content.combat.CombatDefinitions
import com.rs.game.content.combat.getDefenceEmote
import com.rs.game.content.items.liquid_containers.FillAction
import com.rs.game.content.minigames.castlewars.CastleWarsPlayingController
import com.rs.game.content.minigames.lividfarm.LividFarmRewards
import com.rs.game.content.skills.construction.SawmillOperator
import com.rs.game.content.skills.cooking.Cooking
import com.rs.game.content.skills.cooking.Foods
import com.rs.game.content.skills.crafting.Tanning
import com.rs.game.content.skills.farming.FarmPatch
import com.rs.game.content.skills.farming.PatchLocation
import com.rs.game.content.skills.farming.PatchType
import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.Magic.sendLunarTeleportGroupSpell
import com.rs.game.content.skills.magic.Magic.sendLunarTeleportSpell
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Inventory
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.Constants
import com.rs.lib.game.Animation
import com.rs.lib.game.Item
import com.rs.lib.game.Rights
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.*
import com.rs.utils.Ticks
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

@ServerStartupEvent
fun registerLunarInventorySpells() {
//    onSpellCast() { e ->
//        //if(!e.player.hasEffect(Effect.LUNARS_SPELLBOOK_SWAP))
//            return@onSpellCast
//        //Effect.LUNARS_SPELLBOOK_SWAP.expire(e.player)
//    }
    onInterfaceOnPlayer(
        checkDistance = true,
        fromInterfaceIds = intArrayOf(430),
        fromComponentIds = intArrayOf(23, 27, 28, 31, 42, 52)
    ) { e ->
        when (e.componentId) {
            23 -> LunarSpellbook.handleCureOther(e.player, e.target)
            27 -> e.player.sendMessage("Spell not yet implemented.") //LunarSpellbook.handleEnergyTransfer(e.player, e.target)
            28 -> e.player.sendMessage("This spell can only be cast on monsters.")
            31 -> LunarSpellbook.handleStatSpy(e.player, e.target)
            42 -> LunarSpellbook.handleVengeanceOther(e.player, e.target)
            52 -> LunarSpellbook.handleHealOther(e.player, e.target)
            else -> {
                if (e.player.hasRights(Rights.DEVELOPER))
                    e.player.sendMessage("Unhandled option for onInterfaceOnPlayer Component ${e.componentId}")
                else
                    e.player.sendMessage("Spell not yet implemented.")
            }
        }
    }
    onInterfaceOnNPC(
        checkDistance = true,
        fromInterfaceIds = intArrayOf(430),
        fromComponentIds = intArrayOf(23,27,31,42,52,28)
    ) { e ->
        when (e.componentId) {
            23, 27, 31, 42, 52 -> e.player.sendMessage("This spell can only be cast on other players.")
            28 -> LunarSpellbook.handleMonsterExamine(e.player, e.target)
            else -> {
                if (e.player.hasRights(Rights.DEVELOPER))
                    e.player.sendMessage("Unhandled option for onInterfaceOnNPC Component ${e.componentId}")
                else
                    e.player.sendMessage("Spell not yet implemented.")
            }
        }
    }
    onInterfaceOnInterface(
        fromInterfaceIds = intArrayOf(430),
        fromComponentIds = intArrayOf(33, 35, 38, 50, 72, 49),
        toInterfaceIds = intArrayOf(Inventory.INVENTORY_INTERFACE),
        toComponentIds = null,
        biDirectional = true,
    ) { e ->
        val item = e.player.inventory.getItem(e.toSlotId) ?: return@onInterfaceOnInterface
        when (e.fromComponentId) {
            33 -> LunarSpellbook.handlePlankMake(e.player, item)
            34 -> e.player.sendMessage("Spell not yet implemented.") //Borrowed Power
            35 -> LunarSpellbook.handleTuneBanite(e.player, item)
            50 -> LunarSpellbook.handleRestorePotionShare(e.player, item)
            72 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.MAKE_LEATHER)) {
                    e.player.sendMessage("You have not yet learned this spell.")
                    return@onInterfaceOnInterface
                }
                LunarSpellbook.handleLeatherMake(e.player, item)
            }
            49 -> LunarSpellbook.handleBoostPotionShare(e.player, item)
            71 -> LunarSpellbook.handleSpiritualiseFood(e.player, item, e.toSlotId)
            else -> {
                if (e.player.hasRights(Rights.DEVELOPER))
                    e.player.sendMessage("Unhandled option for onInterfaceOnInterface Component ${e.fromComponentId}")
                else
                    e.player.sendMessage("Spell not yet implemented.")
            }
        }
    }
    onInterfaceOnObject(
        fromInterfaceIds = intArrayOf(430), checkDistance = true, fromComponentIds = intArrayOf(24, 55)
    ) { e ->
        when(e.componentId) {
            24 -> LunarSpellbook.handleFertileSoil(e.player, e.`object`)
            55 -> LunarSpellbook.handleCurePlant(e.player, e.`object`)
        }
    }
    onButtonClick(interfaceIds = intArrayOf(430)) { e ->
        when (e.componentId) {
            22 -> sendLunarTeleportSpell(e.player, LunarSpell.BARBARIAN_TELEPORT.level, 76.0, Tile.of(2542, 3574, 0), LunarSpell.BARBARIAN_TELEPORT.runes);
            26 -> e.player.sendMessage("Not yet implemented.");  //LunarSpellbook.handleNPCContact(player);
            29 -> LunarSpellbook.handleHumidify(e.player);
          25 -> LunarSpellbook.handleCureGroup(e.player);
            30 -> LunarSpellbook.handleHunterKit(e.player);
            32 -> LunarSpellbook.handleDream(e.player);
            34 -> e.player.sendMessage("Spell not yet implemented.") //LunarSpellbook.handleSpellbookSwap(player);
            36 -> LunarSpellbook.handleMagicImbue(e.player);
          37 -> LunarSpellbook.handleVengeance(e.player);
            38 -> LunarSpellbook.handleBakePie(e.player)
            40 -> sendLunarTeleportSpell(e.player, LunarSpell.FISHING_GUILD_TELEPORT.level, 89.0, Tile.of(2614, 3382, 0), LunarSpell.FISHING_GUILD_TELEPORT.runes);
            41 -> sendLunarTeleportSpell(e.player, LunarSpell.KHAZARD_TELEPORT.level, 80.0, Tile.of(2630, 3167, 0), LunarSpell.KHAZARD_TELEPORT.runes);
            43 -> sendLunarTeleportSpell(e.player, LunarSpell.MOONCLAN_TELEPORT.level, 66.0, Tile.of(2112, 3914, 0), LunarSpell.MOONCLAN_TELEPORT.runes);
            44 -> sendLunarTeleportSpell(e.player, LunarSpell.CATHERBY_TELEPORT.level, 92.0, Tile.of(2804, 3434, 0), LunarSpell.CATHERBY_TELEPORT.runes);
            45 -> LunarSpellbook.handleStringJewelry(e.player);
            46 -> LunarSpellbook.handleCureMe(e.player);
            47 -> sendLunarTeleportSpell(e.player, LunarSpell.WATERBIRTH_TELEPORT.level, 71.0, Tile.of(2546, 3757, 0), LunarSpell.WATERBIRTH_TELEPORT.runes);
            48 -> LunarSpellbook.handleSuperGlassMake(e.player);
            51 -> sendLunarTeleportSpell(e.player, LunarSpell.ICE_PLATEAU_TELEPORT.level, 96.0, Tile.of(2977, 3924, 0), LunarSpell.ICE_PLATEAU_TELEPORT.runes);
          53 -> LunarSpellbook.handleHealGroup(e.player);
            54 -> sendLunarTeleportSpell(e.player, LunarSpell.OURANIA_TELEPORT.level, 69.0, Tile.of(2466, 3248, 0), LunarSpell.OURANIA_TELEPORT.runes);
            56 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_MOONCLAN.level, 67.0, Tile.of(2112, 3915, 0), LunarSpell.TELE_GROUP_MOONCLAN.runes);
            57 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_WATERBIRTH.level, 72.0, Tile.of(2546, 3757, 0), LunarSpell.TELE_GROUP_WATERBIRTH.runes);
            58 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_BARBARIAN.level, 77.0, Tile.of(2542, 3574, 0), LunarSpell.TELE_GROUP_BARBARIAN.runes);
            59 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_KHAZARD.level, 81.0, Tile.of(2630, 3167, 0), LunarSpell.TELE_GROUP_KHAZARD.runes);
            60 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_FISHING_GUILD.level, 90.0, Tile.of(2614, 3382, 0), LunarSpell.TELE_GROUP_FISHING_GUILD.runes);
            61 -> sendLunarTeleportSpell(e.player, LunarSpell.CATHERBY_TELEPORT.level, 93.0, Tile.of(2804, 3434, 0), LunarSpell.CATHERBY_TELEPORT.runes);
            62 -> sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_ICE_PLATEAU.level, 99.0, Tile.of(2977, 3924, 0), LunarSpell.TELE_GROUP_ICE_PLATEAU.runes);
            67 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.TELEPORT_SOUTH_FALADOR)) {
                    e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
                }
                sendLunarTeleportSpell(e.player, LunarSpell.SOUTH_FALADOR_TELEPORT.level, 70.0, Tile.of(3005, 3327, 0), LunarSpell.SOUTH_FALADOR_TELEPORT.runes);
            }
            69 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.TELEPORT_NORTH_ARDOUGNE)) {
                    e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
                }
                sendLunarTeleportSpell(e.player, LunarSpell.NORTH_ARDOUGNE_TELEPORT.level, 76.0, Tile.of(2613, 3345, 0), LunarSpell.NORTH_ARDOUGNE_TELEPORT.runes);
            }
            70 -> LunarSpellbook.handleRemoteFarm(e.player);
          73 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.DISRUPTION_SHIELD)) {
					e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
				}
                    LunarSpellbook.handleDisruptionShield(e.player);
            }
            74 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.VENGEANCE_GROUP)) {
					e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
				}
                    LunarSpellbook.handleGroupVengeance(e.player);
            }
            75 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.TELEPORT_TROLLHEIM) || !e.player.isQuestComplete(Quest.EADGARS_RUSE)) {
                    e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
                }
                sendLunarTeleportSpell(e.player, LunarSpell.TELEPORT_TO_TROLLHEIM.level, 101.0, Tile.of(2814, 3677, 0), LunarSpell.TELEPORT_TO_TROLLHEIM.runes);
            }
            76 -> {
                if (!LividFarmRewards.checkUnlocked(e.player, LividFarmRewards.LividSpell.GROUP_TELEPORT_TROLLHEIM)) {
                    e.player.sendMessage("You have not yet learned this spell.");
                    return@onButtonClick;
                }
                sendLunarTeleportGroupSpell(e.player, LunarSpell.TELE_GROUP_TROLLHEIM.level, 102.0, Tile.of(2814, 3677, 0), LunarSpell.TELE_GROUP_TROLLHEIM.runes);
            }
            // Filter and Sort Buttons
            2 -> e.player.combatDefinitions.switchDefensiveCasting()
            5 -> e.player.combatDefinitions.switchShowCombatSpells()
            7 -> e.player.combatDefinitions.switchShowTeleportSkillSpells()
            9 -> e.player.combatDefinitions.switchShowMiscSpells()
            11 -> e.player.combatDefinitions.switchShowSkillSpells()
            in 13..15 -> e.player.combatDefinitions.setSortSpellBook(e.componentId - 13)
            //Home Teleport
            39 -> {
                e.player.stopAll()
                e.player.interfaceManager.sendInterface(1092)
            }
            else -> {
                if (e.player.hasRights(Rights.DEVELOPER))
                    e.player.sendMessage("Unhandled onButtonClick option for Component ${e.componentId}")
                else
                    e.player.sendMessage("Spell not yet implemented.")
            }
        }
    }
}

object LunarSpellbook {

    @JvmStatic
    var unstrung: IntArray = intArrayOf(1673, 1675, 1677, 1679, 1681, 1683, 1714, 1720, 6579)

    @JvmStatic
    var strung: IntArray = intArrayOf(1692, 1694, 1696, 1698, 1700, 1702, 1716, 1722, 6581)

    @JvmStatic
    fun getNearPlayers(player: Player, distance: Int, maxTargets: Int): Array<Player> {
        val possibleTargets: MutableList<Player> = ArrayList()
        for (p2 in player.queryNearbyPlayersByTileRange(distance) { p2: Player -> !p2.isDead && p2 !== player && p2.withinDistance(player.tile, distance) }) {
            possibleTargets.add(p2)
            if (possibleTargets.size == maxTargets) break
        }
        return possibleTargets.toTypedArray()
    }

    @JvmStatic
    fun hasUnstrungs(player: Player): Boolean {
        for (item in player.inventory.items.array()) {
            if (item == null) continue
            if (getStrungIndex(item.id) != -1) return true
        }
        return false
    }

    @JvmStatic
    fun getStrungIndex(ammy: Int): Int {
        for (i in unstrung.indices) if (unstrung[i] == ammy) return i
        return -1
    }

    @JvmStatic
    fun getPlankIdx(logId: Int): Int {
        for (i in SawmillOperator.logs.indices) if (SawmillOperator.logs[i] == logId) return i
        return -1
    }

    private val DRAGON_ITEMS: Set<Int> = IntOpenHashSet(intArrayOf(534, 536, 243, 1753, 1751, 1749, 1747, 24372, 7980, 7987, 8265))
    private val ABYSSAL_ITEMS: Set<Int> = IntOpenHashSet(intArrayOf(7979, 7986, 8264))
    private val WALLASALKI_ITEMS: Set<Int> = IntOpenHashSet(intArrayOf(6163, 6165, 6167))
    private val BASILISK_ITEMS: Set<Int> = IntOpenHashSet(intArrayOf(7977, 7984, 8262))

    @JvmStatic
    fun handleTuneBanite(player: Player, item: Item) {
        if (!player.isQuestComplete(Quest.RITUAL_OF_MAHJARRAT, "to tune banite ores.")) return
        if (!player.canCastSpell() || !Magic.checkMagicAndRunes(player, 87, false, LunarSpell.TUNE_BANE_ORE.runes)) return
        if (!player.inventory.containsItem(21778)) {
            player.sendMessage("You need some banite ore to cast this spell.")
            return
        }
        val targetOre = if (DRAGON_ITEMS.contains(item.id)) 21779
        else if (ABYSSAL_ITEMS.contains(item.id)) 21782
        else if (WALLASALKI_ITEMS.contains(item.id)) 21780
        else if (BASILISK_ITEMS.contains(item.id)) 21781
        else -1
        if (targetOre == -1) {
            player.sendMessage("The spell fails to react with the targeted item.")
            return
        }
        player.stopAll(false, false, true)
        player.startConversation {
            makeX(targetOre, player.inventory.getNumberOf(21778))
            exec {
                for (i in 0 until MakeXStatement.getQuantity(player)) {
                    if (!player.inventory.containsItem(21778) ||
                        !Magic.checkMagicAndRunes(player, 87, true, LunarSpell.TUNE_BANE_ORE.runes)
                    ) return@exec
                    player.sync(11706, 1344)
                    LunarSpell.TUNE_BANE_ORE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
                    player.inventory.deleteItem(21778, 1)
                    player.inventory.addItem(targetOre, 1)
                    //Magic.triggerSpellCastEvent(player, LunarSpell.TUNE_BANE_ORE.name)
                }
            }
        }
    }

    @JvmStatic
    fun handlePlankMake(player: Player, item: Item) {
        player.interfaceManager.openTab(InterfaceManager.Sub.TAB_MAGIC)
        if (!player.canCastSpell()) return
        val index = getPlankIdx(item.id)
        if (index == -1) {
            player.sendMessage("You can only cast this spell on a log.")
            return
        }
        val price = (SawmillOperator.prices[index] * 0.7).toInt()
        if (!player.inventory.hasCoins(price)) {
            player.sendMessage("You need " + Utils.formatNumber(price) + " gold to convert this log.")
            return
        }
        if (!player.inventory.containsItem(SawmillOperator.logs[index], 1) || !Magic.checkMagicAndRunes(player, 86, true, LunarSpell.PLANK_MAKE.runes)) return
        player.anim(6298)
        player.spotAnim(SpotAnim(1063, 0, 50))
        player.inventory.removeCoins(price)
        player.inventory.deleteItem(SawmillOperator.logs[index], 1)
        player.inventory.addItem(SawmillOperator.planks[index], 1)
        if (player.hasEffect(Effect.LIVID_PLANKS_BOOST) && Utils.random(10) == 1)
            player.inventory.addItem(SawmillOperator.planks[index] + 1, 1)
        LunarSpell.PLANK_MAKE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.addSpellDelay(2)
        player.soundEffect(3617, true)
//        Magic.triggerSpellCastEvent(player, LunarSpell.PLANK_MAKE.name)
    }

    @JvmStatic
    fun handleVengeance(player: Player) {
        if (!player.canCastSpell()) return
        val lastVengeanceCast = player.tempAttribs.getL("LAST_VENGEANCE_CAST")
        if (lastVengeanceCast != -1L && lastVengeanceCast + 30000 > System.currentTimeMillis()) {
            player.sendMessage("You may only cast vengeance once every 30 seconds.")
            return
        }
        if (!Magic.checkMagicAndRunes(player, 94, true, LunarSpell.VENGEANCE.runes)) return
        player.spotAnim(SpotAnim(726, 0, 100))
        player.anim(4410)
        player.isCastVeng = true
        player.soundEffect(2907, true)
        LunarSpell.VENGEANCE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.tempAttribs.setL("LAST_VENGEANCE_CAST", System.currentTimeMillis())
//        Magic.triggerSpellCastEvent(player, LunarSpell.VENGEANCE.name)
    }

    @JvmStatic
    fun handleVengeanceOther(player: Player, target: Player) {
        if (!player.canCastSpell()) return
        val lastVengeanceCast = player.tempAttribs.getL("LAST_VENGEANCE_CAST")
        if (lastVengeanceCast != -1L && lastVengeanceCast + 30000 > System.currentTimeMillis()) {
            player.sendMessage("You may only cast vengeance once every 30 seconds.")
            return
        }
        if (!Magic.checkMagicAndRunes(player, 94, true, LunarSpell.VENGEANCE_OTHER.runes)) return
        target.spotAnim(SpotAnim(726, 0, 100))
        player.anim(4411)
        target.isCastVeng = true
        player.soundEffect(2907, true)
        LunarSpell.VENGEANCE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.tempAttribs.setL("LAST_VENGEANCE_CAST", System.currentTimeMillis())
//        Magic.triggerSpellCastEvent(player, LunarSpell.VENGEANCE.name)
    }

    @JvmStatic
    fun handleHumidify(player: Player) {
        if (!player.canCastSpell()) return
        if (hasFillables(player)) {
            if (Magic.checkMagicAndRunes(player, 68, true, LunarSpell.HUMIDIFY.runes)) {
                LunarSpell.HUMIDIFY.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
                player.spotAnim(SpotAnim(1061))
                player.anim(6294)
                fillFillables(player)
                player.soundEffect(3614, true)
            }
        } else player.sendMessage("You need to have something to humidify before using this spell.")
    }

    @JvmStatic
    fun fillFillables(player: Player) {
        for (item in player.inventory.items.array()) {
            if (item == null) continue
            val fill = FillAction.Filler.forEmpty(item.id.toShort().toInt())
            if (fill != null) if (player.inventory.containsItem(fill.emptyItem.id, 1)) {
                player.inventory.deleteItem(fill.emptyItem)
                player.inventory.addItem(fill.filledItem)
            }
        }
    }

    @JvmStatic
    fun hasFillables(player: Player): Boolean {
        for (item in player.inventory.items.array()) {
            if (item == null) continue
            val fill = FillAction.Filler.forEmpty(item.id.toShort().toInt())
            if (fill != null) return true
        }
        return false
    }

    @JvmStatic
    fun handleStringJewelry(player: Player) {
        if (!player.canCastSpell()) return
        if (hasUnstrungs(player)) {
            if (Magic.checkMagicAndRunes(player, 80, true, LunarSpell.STRING_JEWELLERY.runes)) {
                player.spotAnim(SpotAnim(728, 0, 100))
                player.anim(4412)
                LunarSpell.STRING_JEWELLERY.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
                for (item in player.inventory.items.array()) {
                    if (item == null) continue
                    val strungId = getStrungIndex(item.id)
                    if (strungId != -1) {
                        player.inventory.deleteItem(item.id, 1)
                        player.inventory.addItem(strung[strungId], 1)
                        player.soundEffect(2903, true)
//                        Magic.triggerSpellCastEvent(player, LunarSpell.STRING_JEWELLERY.name)
                    }
                }
            }
        } else player.sendMessage("You need to have unstrung jewelry to cast this spell.")
    }

    @JvmStatic
    fun handleFertileSoil(player: Player, obj: GameObject) {
        if (!player.canCastSpell()) return
        val loc = PatchLocation.forObject(obj.id)
        if (loc == null) {
            player.sendMessage("Um...I don't want to fertilise that!")
            return
        }
        if (loc.type === PatchType.COMPOST) {
            player.sendMessage("Composting the compost??")
            return
        }
        var spot = player.getPatch(loc)
        if (spot == null) spot = FarmPatch(loc)
        if (spot.fullyGrown()) {
            player.sendMessage("Composting it isn't going to make it get any bigger.")
            return
        }
        if (spot.compostLevel == 2) {
            player.sendMessage("This patch has already been treated with supercompost.")
            return
        }
        if (!Magic.checkMagicAndRunes(player, 83, true, LunarSpell.FERTILE_SOIL.runes)) return
        player.nextFaceTile = obj.tile
        player.skills.addXp(Constants.FARMING, 18.0)
        LunarSpell.FERTILE_SOIL.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.anim(4413)
        World.sendSpotAnim(obj.tile, 724)
        spot.compostLevel = 2
        player.putPatch(spot)
//        Magic.triggerSpellCastEvent(player, LunarSpell.FERTILE_SOIL.name)
    }

    @JvmStatic
    fun handleCurePlant(player: Player, obj: GameObject) {
        if (!player.canCastSpell()) return
        val loc = PatchLocation.forObject(obj.id)
        if (loc == null) {
            player.sendMessage("There's nothing there to cure!")
            return
        }
        var spot = player.getPatch(loc)
        if (spot == null) spot = FarmPatch(loc)
        if (spot.dead) {
            player.sendMessage("It says 'Cure' not 'Resurrect'. Although death may arise from disease, it is not in itself a disease and hence cannot be cured. So there.")
            return
        }
        if (!spot.diseased) {
            player.sendMessage("It is growing just fine.")
            return
        }
        if (!Magic.checkMagicAndRunes(player, 66, true, LunarSpell.CURE_PLANT.runes)) return
        player.nextFaceTile = obj.tile
        player.skills.addXp(Constants.FARMING, 90.0)
        LunarSpell.CURE_PLANT.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.anim(4432)
        player.spotAnim(SpotAnim(728, 0, 100))
        spot.diseased = false
        spot.updateVars(player)
        player.lock(3)
//        Magic.triggerSpellCastEvent(player, LunarSpell.CURE_PLANT.name)
    }

    @JvmStatic
    fun handleLeatherMake(player: Player, item: Item?) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.MAKE_LEATHER.level, false, LunarSpell.MAKE_LEATHER.runes)) return
        if (!LividFarmRewards.checkUnlocked(player, LividFarmRewards.LividSpell.MAKE_LEATHER)) {
            player.sendMessage("You have not yet learned this spell.")
            return
        }
        val rawId = item?.id ?: return
        val leather = Tanning.Leather.entries.find { it.raw == rawId } ?: run {
            player.sendMessage("You can only make leather from hides.")
            return
        }
        val available = player.inventory.getAmountOf(rawId)
        if (available == 0) {
            player.sendMessage("You have no hides to tan.")
            return
        }
        val toMake = minOf(available, 5)
        fun convert(outputId: Int) {
            if (!Magic.checkMagicAndRunes(player, LunarSpell.MAKE_LEATHER.level, true, LunarSpell.MAKE_LEATHER.runes)) return
//            Magic.triggerSpellCastEvent(player, LunarSpell.MAKE_LEATHER.name)
            player.anim(4413)
            player.spotAnim(SpotAnim(745, 0, 0))
            LunarSpell.MAKE_LEATHER.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
            repeat(toMake) {
                player.inventory.deleteItem(rawId, 1)
                player.inventory.addItem(outputId, 1)
            }
            player.inventory.refresh()
            player.sendMessage("You make $toMake ${ItemDefinitions.getDefs(rawId).name.lowercase()} into ${ItemDefinitions.getDefs(outputId).name.lowercase()}.")
        }
        if (leather == Tanning.Leather.SOFT || leather == Tanning.Leather.HARD) {
            player.startConversation {
                options("Select leather type to make:") {
                    op("Soft leather") {
                        exec { convert(Tanning.Leather.SOFT.tanned) }
                    }
                    op("Hard leather") {
                        exec { convert(Tanning.Leather.HARD.tanned) }
                    }
                    op("Cancel")
                }
            }
        } else {
            convert(leather.tanned)
        }
    }

    private val BARBARIAN_MIXES = setOf(
        Potion.ATTACK_MIX, Potion.STRENGTH_MIX, Potion.DEFENCE_MIX, Potion.RANGING_MIX,
        Potion.MAGIC_MIX, Potion.COMBAT_MIX, Potion.ANTIFIRE_MIX, Potion.ENERGY_MIX,
        Potion.FISHING_MIX, Potion.HUNTING_MIX, Potion.SUPER_ATTACK_MIX,
        Potion.SUPER_STRENGTH_MIX, Potion.SUPER_RESTORE_MIX, Potion.ZAMORAK_MIX,
        Potion.PRAYER_MIX
    )

    private val CW_POTIONS = setOf(
        Potion.CW_SUPER_RANGING_POTION, Potion.CW_SUPER_MAGIC_POTION, Potion.CW_SUPER_ATTACK_POTION,
        Potion.CW_SUPER_ENERGY_POTION, Potion.CW_SUPER_DEFENCE_POTION, Potion.CW_SUPER_STRENGTH_POTION
    )

    private fun isRestorePotion(p: Potion): Boolean = when (p) {
        Potion.RESTORE_POTION, Potion.RESTORE_FLASK, Potion.SUPER_RESTORE,
        Potion.SUPER_RESTORE_FLASK, Potion.PRAYER_POTION, Potion.PRAYER_FLASK,
        Potion.PRAYER_RENEWAL, Potion.PRAYER_RENEWAL_FLASK, Potion.SANFEW_SERUM,
        Potion.SANFEW_SERUM_FLASK, Potion.ENERGY_POTION, Potion.SUPER_ENERGY
            -> true
        else -> false
    }

    private fun isBoostPotion(p: Potion): Boolean = when (p) {
        Potion.ATTACK_POTION, Potion.SUPER_ATTACK, Potion.SUPER_ATTACK_FLASK,
        Potion.STRENGTH_POTION, Potion.SUPER_STRENGTH, Potion.SUPER_STRENGTH_FLASK,
        Potion.DEFENCE_POTION, Potion.SUPER_DEFENCE, Potion.SUPER_DEFENCE_FLASK,
        Potion.RANGING_POTION, Potion.MAGIC_POTION, Potion.AGILITY_POTION,
        Potion.COMBAT_POTION, Potion.HUNTER_POTION, Potion.FISHING_POTION,
        Potion.CRAFTING_POTION,
            -> true
        else -> false
    }

    private fun sharePotionSpell(
        player: Player,
        item: Item?,
        spell: LunarSpell,
        validPotionCheck: (Potion) -> Boolean
    ) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, spell.level, spell === LunarSpell.BOOST_POTION_SHARE, spell.runes)) return
        val potion = Potion.forId(item?.id ?: return) ?: run {
            player.sendMessage("You can't share that potion.")
            return
        }
        if (!validPotionCheck(potion) || potion in BARBARIAN_MIXES) {
            player.sendMessage("You can't share that potion.")
            return
        }
        if ( potion in CW_POTIONS) {
            return
        }
        val idx = potion.ids.indexOf(item.id).takeIf { it >= 0 } ?: run {
            player.sendMessage("Invalid potion.")
            return
        }
        val doses = potion.ids.size - idx
        if (doses <= 0) {
            player.sendMessage("Your potion has no doses left.")
            return
        }
        val targets = getNearPlayers(player, 3, Int.MAX_VALUE)
//            .filter { it.assistStatus == 1 || it.isIronMan } //TODO Unable to toggle aid on in interface
            .filter { it.isIronMan }
            .take(doses)
        if (targets.isEmpty()) {
            player.sendMessage("No one here can accept aid.")
            return
        }
//        Magic.triggerSpellCastEvent(player, spell.name)
        player.anim(4411)
        player.spotAnim(SpotAnim(728, 0, 100))
        if(isRestorePotion(potion))
            LunarSpell.STAT_RESTORE_POT_SHARE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        else
            LunarSpell.BOOST_POTION_SHARE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        potion.effect.invoke(player)
        for (other in targets) {
            other.spotAnim(SpotAnim(728, 0, 100))
            potion.effect.invoke(other)
            other.sendMessage("You receive a dose of ${ItemDefinitions.getDefs(item.id).name.lowercase()}.")
        }
        val remaining = doses - targets.size
        if (remaining > 0) {
            player.inventory.items[item.slot] = Item(potion.getIdForDoses(remaining), 1)
        } else {
            player.inventory.deleteItem(item)
        }
        player.inventory.refresh(item.slot)
    }

    @JvmStatic
    fun handleRestorePotionShare(player: Player, item: Item?) {
        sharePotionSpell(player, item, LunarSpell.STAT_RESTORE_POT_SHARE, ::isRestorePotion)
    }

    @JvmStatic
    fun handleBoostPotionShare(player: Player, item: Item?) {
        if (player.controllerManager.controller is CastleWarsPlayingController) {
            player.sendMessage("You don't want to help your enemies.")
            return
        }
        sharePotionSpell(player, item, LunarSpell.BOOST_POTION_SHARE, ::isBoostPotion)
    }

    @JvmStatic
    fun handleBakePie(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.BAKE_PIE.level, false, LunarSpell.BAKE_PIE.runes)) return
        val availablePies = Cooking.Cookables.entries
            .filter { it.name.startsWith("RAW_") && it.name.endsWith("_PIE") }
            .mapNotNull { cook ->
                val requiredLevel = cook.level
                val playerLevel = player.getLevel(Skills.COOKING)
                if (playerLevel < requiredLevel) {
                    null
                } else {
                    val rawId = cook.rawItem.id
                    val count = player.inventory.getAmountOf(rawId)
                    if (count > 0) cook to count else null
                }
            }
        if (availablePies.isEmpty()) {
            player.sendMessage("You have no uncooked pies you can bake.")
            return
        }
        LunarSpell.BAKE_PIE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.spotAnim(SpotAnim(746, 0, 100))
        player.anim(4413)
        for ((cook, count) in availablePies) {
            repeat(count) {
                if (!player.canCastSpell()) return
                if (!Magic.checkMagicAndRunes(player, LunarSpell.BAKE_PIE.level, true, LunarSpell.BAKE_PIE.runes)) return
//                Magic.triggerSpellCastEvent(player, LunarSpell.BAKE_PIE.name)
                player.inventory.deleteItem(cook.rawItem.id, 1)
                val bakedId = cook.productItem.first()
                player.inventory.addItem(bakedId, 1)
                player.skills.addXp(Constants.COOKING, cook.xp)
                val bakedName = ItemDefinitions.getDefs(bakedId).name.lowercase()
                player.sendMessage("You bake a $bakedName.")
            }
        }
        player.inventory.refresh()
    }

    @JvmStatic
    fun handleCureMe(player: Player) {
        if (!player.canCastSpell()) return
        if (player.poison.isPoisoned) {
            if (Magic.checkMagicAndRunes(player, LunarSpell.CURE_ME.level, true, LunarSpell.CURE_ME.runes)) {
//                Magic.triggerSpellCastEvent(player, LunarSpell.CURE_ME.name)
                player.spotAnim(SpotAnim(729, 0, 100))
                player.anim(4409)
                LunarSpell.CURE_ME.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
                player.poison.reset()
                player.sendMessage("You are cured of poison!")
            }
        } else player.sendMessage("You are not poisoned.")
    }

    @JvmStatic
    fun handleHunterKit(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.HUNTER_KIT.level, true, LunarSpell.HUNTER_KIT.runes)) return
        val kitItems = listOf(
            10150, // Noose wand
            10010, // Butterfly net
            10006, // Bird snare
            10031, // Rabbit snare
            10029, // Teasing stick
            594,   // Unlit torch (item id 594)
            10008, // Box trap
            11260  // Impling jar
        )
        if (kitItems.all { player.inventory.containsItem(it, 1) }) {
            player.sendMessage("Why do you want to create a kit? You already have all the Hunter equipment it contains.")
            return
        }
        player.spotAnim(SpotAnim(1074, 0, 100))
        player.anim(6303)
        LunarSpell.HUNTER_KIT.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//        Magic.triggerSpellCastEvent(player, LunarSpell.HUNTER_KIT.name)
        for (id in kitItems) {
            if (!player.inventory.containsItem(id, 1) && player.inventory.hasFreeSlots()) {
                player.inventory.addItem(id, 1)
            }
        }
        player.sendMessage("You create a Hunter kit, supplying you with all the necessary equipment.")
        player.inventory.refresh()
    }


    @JvmStatic
    fun handleCureGroup(player: Player) {
        if (!player.canCastSpell()) return
        if (Magic.checkMagicAndRunes(player, LunarSpell.CURE_GROUP.level, true, LunarSpell.CURE_GROUP.runes)) {
//            Magic.triggerSpellCastEvent(player, LunarSpell.CURE_GROUP.name)
            player.actionManager.addActionDelay(4)
            player.spotAnim(SpotAnim(729, 0, 100))
            player.anim(4409)
            player.poison.reset()
            player.addSpellDelay(2)
            LunarSpell.CURE_GROUP.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
            for (other in getNearPlayers(player, 3, 10)) if (other.poison.isPoisoned) {
                player.spotAnim(SpotAnim(729, 0, 100))
                player.poison.reset()
                player.sendMessage("Your poison has been cured!")
            }
        }
    }

    @JvmStatic
    fun handleCureOther(player: Player, target: Player) {
        if (!player.canCastSpell()) return
        if (Magic.checkMagicAndRunes(player, LunarSpell.CURE_OTHER.level, true, LunarSpell.CURE_OTHER.runes)) {
//            Magic.triggerSpellCastEvent(player, LunarSpell.CURE_OTHER.name)
            player.actionManager.addActionDelay(4)
            player.spotAnim(SpotAnim(729, 0, 100))
            player.anim(4409)
            player.poison.reset()
            player.addSpellDelay(2)
            LunarSpell.CURE_OTHER.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
            if (target.poison.isPoisoned) {
                target.spotAnim(SpotAnim(729, 0, 100))
                target.poison.reset()
                player.sendMessage("${target.displayName} has been cured of poison!")
                target.sendMessage("${player.displayName} has cured you from poison!")
            }
        }
    }

    @JvmStatic
    fun handleSuperGlassMake(player: Player) {
        val secondary = (if (player.inventory.containsItem(10978)) 10978 else if (player.inventory.containsItem(1781)) 1781 else 401) //Swamp weed
        var number = min(player.inventory.getNumberOf(1783).toDouble(), player.inventory.getNumberOf(secondary).toDouble()).toInt()
        if (number <= 0) {
            player.sendMessage("You need seaweed and buckets of sand to make molten glass.")
            return
        }
        if (Magic.checkMagicAndRunes(player, LunarSpell.SUPERGLASS_MAKE.level, true, LunarSpell.SUPERGLASS_MAKE.runes)) {
            player.spotAnim(SpotAnim(729, 0, 100))
            player.anim(4412)
            player.skills.addXp(Constants.MAGIC, 78.0)
//            Magic.triggerSpellCastEvent(player, LunarSpell.SUPERGLASS_MAKE.name)
            if (number > 0) {
                val chance = (number * 1.30) - floor(number * 1.3)
                number = (number * 1.30).toInt()
                player.inventory.deleteItem(secondary, number)
                player.inventory.deleteItem(1783, number)
                player.skills.addXp(Constants.CRAFTING, (10 * number).toDouble())
                player.inventory.addItem(1775, number)
                if ((chance > 0.0) && Utils.randomD() <= chance) {
                    player.inventory.addItem(1775, 1)
                    player.skills.addXp(Constants.CRAFTING, 10.0)
                    player.soundEffect(2896, true)
                }
            }
        }
    }

    @JvmStatic
    fun handleRemoteFarm(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.REMOTE_FARM.level, true, LunarSpell.REMOTE_FARM.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.REMOTE_FARM.name)
        openRemoteFarm(player)
    }

    @JvmStatic
    fun handleDream(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.DREAM.level, true, LunarSpell.DREAM.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.DREAM.name)
        player.anim(6295)
        LunarSpell.DREAM.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.addEffect(Effect.LUNARS_DREAM, Ticks.fromMinutes(60).toLong())
        player.actionManager.setAction(DreamAction(player.tile))
    }

    @JvmStatic
    fun handleMagicImbue(player: Player) {
        if (!player.canCastSpell()) return
        val lastImbue = player.tempAttribs.getL("LAST_IMBUE")
        if (lastImbue != -1L && lastImbue + 12600 > System.currentTimeMillis()) {
            player.sendMessage("You may only cast magic imbue spells once every 12.6 seconds.")
            return
        }
        if (Magic.checkMagicAndRunes(player, 82, true, LunarSpell.MAGIC_IMBUE.runes)) {
            player.spotAnim(SpotAnim(141, 0, 100))
            player.anim(722)
            player.isCastMagicImbue = true
            player.soundEffect(2888, true)
            LunarSpell.MAGIC_IMBUE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
            player.tempAttribs.setL("LAST_IMBUE", System.currentTimeMillis())
//            Magic.triggerSpellCastEvent(player, LunarSpell.MAGIC_IMBUE.name)
        }
    }

    @JvmStatic
    fun handleDisruptionShield(player: Player) {
        if (!player.canCastSpell()) return
        val lastShield = player.tempAttribs.getL("LAST_SHIELD")
        if (lastShield != -1L && lastShield + 60000 > System.currentTimeMillis()) {
            player.sendMessage("You may only cast disruption shield spells once every 60 seconds.")
            return
        }
        if (!Magic.checkMagicAndRunes(player, LunarSpell.DISRUPTION_SHIELD.level, true, LunarSpell.DISRUPTION_SHIELD.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.DISRUPTION_SHIELD.name)
        player.spotAnim(SpotAnim(1320, 0, 100))
        player.anim(722)
        LunarSpell.DISRUPTION_SHIELD.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.addEffect(Effect.LUNARS_DISRUPTION_SHIELD, Ticks.fromMinutes(60).toLong())
        player.tempAttribs.setL("LAST_SHIELD", System.currentTimeMillis())
    }

    @JvmStatic
    fun handleGroupVengeance(player: Player) {
        if (!player.canCastSpell()) return
        val lastVengeanceCast = player.tempAttribs.getL("LAST_VENGEANCE_CAST")
        if (lastVengeanceCast != -1L && lastVengeanceCast + 30000 > System.currentTimeMillis()) {
            player.sendMessage("You may only cast vengeance spells once every 30 seconds.")
            return
        }
        if (Magic.checkMagicAndRunes(player, 95, true, LunarSpell.VENGEANCE_GROUP.runes)) {
            player.spotAnim(SpotAnim(725, 0, 100))
            player.anim(4411)
            player.isCastVeng = true
            player.soundEffect(2908, true)
            LunarSpell.VENGEANCE_GROUP.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
            player.tempAttribs.setL("LAST_VENGEANCE_CAST", System.currentTimeMillis())
//            Magic.triggerSpellCastEvent(player, LunarSpell.VENGEANCE_GROUP.name)
            for (other in getNearPlayers(player, 3, 10)) {
                val otherVeng = other.tempAttribs.getL("LAST_VENGEANCE_CAST")
                if (otherVeng != -1L && otherVeng + 30000 > System.currentTimeMillis()) continue
                other.spotAnim(SpotAnim(725, 0, 100))
                other.isCastVeng = true
                other.tempAttribs.setL("LAST_VENGEANCE_CAST", System.currentTimeMillis())
            }
        }
    }

    @JvmStatic
    fun handleHealGroup(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.HEAL_GROUP.level, true, LunarSpell.HEAL_GROUP.runes)) return
        val maxHp = player.skills.getLevel(Skills.HITPOINTS)
        val minRequiredHp = ceil(maxHp * 0.11).toInt()
        if (player.hitpoints <= minRequiredHp) {
            player.sendMessage("You need more hitpoints to cast this spell.")
            return
        }
        if (player.hasEffect(Effect.SKULL)) {
            player.sendMessage("You can't cast this spell while Skulled.")
            return
        }
        val targets = getNearPlayers(player, 3, Int.MAX_VALUE)
            .filter { it !== player && it.hitpoints < it.maxHitpoints }
            .take(5)
        if (targets.isEmpty()) {
            player.sendMessage("There is no one here who needs healing.")
            return
        }
        val currentHp = player.hitpoints
        val lost = (currentHp * 0.75).toInt()
        player.hitpoints = currentHp - lost
        val totalHeal = lost + 5
        val healPer = totalHeal / targets.size
        for (t in targets) {
            t.hitpoints = min(t.hitpoints + healPer, t.maxHitpoints)
            t.spotAnim(SpotAnim(729, 0, 100))
            t.sendMessage("${player.displayName} heals you for $healPer hitpoints.")
        }
        player.spotAnim(SpotAnim(729, 0, 100))
        player.anim(4411)
        LunarSpell.HEAL_GROUP.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//        Magic.triggerSpellCastEvent(player, LunarSpell.HEAL_GROUP.name)
        player.sendMessage("You heal ${targets.size} player${if (targets.size > 1) "s" else ""} for $healPer hitpoints each.")
    }


    @JvmStatic
    fun handleHealOther(player: Player, target: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.HEAL_OTHER.level, true, LunarSpell.HEAL_OTHER.runes)) return
        val maxHp = player.skills.getLevel(Skills.HITPOINTS)
        val minRequiredHp = ceil(maxHp * 0.11).toInt()
        if (player.hitpoints <= minRequiredHp) {
            player.sendMessage("You need more hitpoints to be able to cast this spell.")
            return
        }
        if (player.hasEffect(Effect.SKULL)) {
            player.sendMessage("You can't cast this spell while Skulled.")
            return
        }
        val healAmount = (player.hitpoints * 0.75).toInt()
        player.hitpoints -= healAmount
        target.hitpoints = min(target.hitpoints + healAmount, target.maxHitpoints)
        player.spotAnim(SpotAnim(729, 0, 100))
        player.anim(4411)
        LunarSpell.HEAL_OTHER.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//        Magic.triggerSpellCastEvent(player, LunarSpell.HEAL_OTHER.name)
        player.sendMessage("You transfer $healAmount hitpoints to ${target.displayName}.")
        target.sendMessage("${player.displayName} heals you for $healAmount hitpoints.")
    }

    @JvmStatic
    fun handleSpellbookSwap(player: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.SPELLBOOK_SWAP.level, true, LunarSpell.SPELLBOOK_SWAP.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.SPELLBOOK_SWAP.name)
        player.anim(6298)
        player.spotAnim(SpotAnim(1063))
        LunarSpell.SPELLBOOK_SWAP.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        player.startConversation {
            options("Select a spellbook to swap to:") {
                op("Modern Spellbook") {
                    exec {
                        applyTemporarySpellbook(player, CombatDefinitions.Spellbook.MODERN.ordinal)
                    }
                }
                op("Ancient Magicks") {
                    exec(Runnable {
                        if (!player.isQuestComplete(Quest.DESERT_TREASURE)) {
                            player.sendMessage("You must complete Desert Treasure to use Ancient Magicks.")
                            return@Runnable
                        }
                        applyTemporarySpellbook(player, CombatDefinitions.Spellbook.ANCIENT.ordinal)
                    })
                }
                op("Cancel")
            }
        }
    }
    @JvmStatic
    private fun applyTemporarySpellbook(player: Player, spellbookId: Int) {
        player.combatDefinitions.setSpellbook(CombatDefinitions.Spellbook.entries.getOrNull(spellbookId) ?: CombatDefinitions.Spellbook.MODERN)
        player.sendMessage("You have temporarily swapped to another spellbook.")
        player.addEffect(Effect.LUNARS_SPELLBOOK_SWAP, Ticks.fromMinutes(2).toLong())
    }

    @JvmStatic
    fun handleStatSpy(player: Player, target: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.STAT_SPY.level, true, LunarSpell.STAT_SPY.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.STAT_SPY.name)
        player.anim(4409)
        player.spotAnim(SpotAnim(1059))
        LunarSpell.STAT_SPY.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        val interfaceId = 523
        player.interfaceManager.sendInventoryInterface(interfaceId)
        player.packets.setIFText(interfaceId, 103, "Player:<br>" + target.displayName.toString())
        //player.packets.setIFText(interfaceId, 104, target.displayName.toString())
        val skillsOrder = listOf(
            Skills.ATTACK,
            Skills.HITPOINTS,
            Skills.MINING,
            Skills.STRENGTH,
            Skills.AGILITY,
            Skills.SMITHING,
            Skills.DEFENSE,
            Skills.HERBLORE,
            Skills.FISHING,
            Skills.RANGE,
            Skills.THIEVING,
            Skills.COOKING,
            Skills.PRAYER,
            Skills.CRAFTING,
            Skills.FIREMAKING,
            Skills.MAGIC,
            Skills.FLETCHING,
            Skills.WOODCUTTING,
            Skills.RUNECRAFTING,
            Skills.SLAYER,
            Skills.FARMING,
            Skills.CONSTRUCTION,
            Skills.HUNTER,
            Skills.SUMMONING,
            Skills.DUNGEONEERING
        )
        skillsOrder.forEachIndexed { idx, skill ->
            val baseComponent = 1 + idx * 4
            player.packets.setIFText(interfaceId, baseComponent, target.skills.getLevel(skill).toString())
            player.packets.setIFText(interfaceId, baseComponent + 1, "99")
        }
        player.packets.setIFText(interfaceId, 106, "Lifepoints: " + target.hitpoints.toString() + "/" + target.maxHitpoints)
    }

    @JvmStatic
    fun handleMonsterExamine(player: Player, npc: NPC) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.MONSTER_EXAMINE.level, true, LunarSpell.MONSTER_EXAMINE.runes)) return
//        Magic.triggerSpellCastEvent(player, LunarSpell.MONSTER_EXAMINE.name)
        player.anim(4409)
        player.spotAnim(SpotAnim(1060))
        LunarSpell.MONSTER_EXAMINE.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
        npc.setNextAnimationNoPriority(Animation(getDefenceEmote(npc)))
        val interfaceId = 522
        player.interfaceManager.sendInventoryInterface(interfaceId)
        player.packets.setIFText(interfaceId, 0, npc.name.toString())
        player.packets.setIFText(interfaceId, 1, "Combat Level: " + npc.combatLevel)
        player.packets.setIFText(interfaceId, 2, "Lifepoints: " + npc.hitpoints)
        player.packets.setIFText(interfaceId, 3, "Max Hit: " + npc.maxHit)
        player.packets.setIFText(interfaceId, 4, "Slayer Targets not yet implemented") //TODO isASlayerTarget
    }

    @JvmStatic
    fun handleEnergyTransfer(player: Player, target: Player) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.ENERGY_TRANSFER.level, true, LunarSpell.ENERGY_TRANSFER.runes)) return
        if (player.hitpoints <= 10) {
            player.sendMessage("You need more hitpoints to cast this spell.")
            return
        }
        if (player.hasEffect(Effect.SKULL)) {
            player.sendMessage("You can't cast this spell while skulled.")
            return
        }
        if(player.combatDefinitions.getSpecialAttackPercentage() != 100) {
            player.sendMessage("You do not have enough special attack energy to perform this spell")
            return
        }
        player.hitpoints -= 10
        player.combatDefinitions.setSpecialAttack(0)
        target.runEnergy = 100.0
        target.combatDefinitions.setSpecialAttack(100)
        player.spotAnim(SpotAnim(1302, 0, 100))
        player.anim(4409)
        LunarSpell.ENERGY_TRANSFER.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//        Magic.triggerSpellCastEvent(player, LunarSpell.ENERGY_TRANSFER.name)
        player.sendMessage("You transfer your energy to ${target.displayName}.")
        target.sendMessage("${player.displayName} casts Energy Transfer on you; your run energy and special attack are fully restored.")
    }

    @JvmStatic
    fun handleSpiritualiseFood(player: Player, item: Item?, slot: Int) {
        if (!player.canCastSpell()) return
        if (!Magic.checkMagicAndRunes(player, LunarSpell.SPIRITUALISE_FOOD.level, true, LunarSpell.SPIRITUALISE_FOOD.runes)) return
        val fam = player.familiar ?: run {
            player.sendMessage("You need to have a familiar summoned to spiritualise food.")
            return
        }
        if (fam.attribs.getB("SPIRIT_FOOD_USED")) {
            player.sendMessage("You can only cast Spiritualise Food once per familiar.")
            return
        }
        val now = System.currentTimeMillis()
        val last = player.tempAttribs.getL("SPIRIT_FOOD_COOLDOWN")
        if (last != -1L && now - last < 120_000) {
            player.sendMessage("You can only cast Spiritualise Food once every two minutes.")
            return
        }
        if (item == null) return
        val food = Foods.Food.forId(item.id) ?: run {
            player.sendMessage("You can only spiritualise edible food.")
            return
        }
        if (food.healAmount < 1200) {
            player.sendMessage("That food isn't powerful enough to spiritualise.")
            return
        }
        player.inventory.deleteItem(item.id, 1)
        player.inventory.refresh(slot)
        fam.attribs.setB("SPIRIT_FOOD_USED", true)
        fam.heal(food.healAmount)
        fam.renewFamiliar()
        fam.spotAnim(SpotAnim(1315))
        player.anim(6294)
        LunarSpell.SPIRITUALISE_FOOD.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//        Magic.triggerSpellCastEvent(player, LunarSpell.SPIRITUALISE_FOOD.name)
        player.tempAttribs.setL("SPIRIT_FOOD_COOLDOWN", now)
        player.sendMessage("You spiritualise the ${ItemDefinitions.getDefs(item.id).name.lowercase()} for your familiar.")
    }
}