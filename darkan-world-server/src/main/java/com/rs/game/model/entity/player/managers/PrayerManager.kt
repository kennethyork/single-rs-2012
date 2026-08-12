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
package com.rs.game.model.entity.player.managers

import com.rs.cache.loaders.Bonus
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.skills.prayer.Leech
import com.rs.game.content.skills.prayer.Prayer
import com.rs.game.content.skills.prayer.Sap
import com.rs.game.content.skills.prayer.Turmoil
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.interactions.PlayerCombatInteraction
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.floor
import com.rs.engine.variables.curse_perm2
import com.rs.engine.variables.prayer_points_var

@ServerStartupEvent
fun mapPrayerInterface() {
    onButtonClick(271) { (player, _, componentId, slotId) ->
        if (componentId == 8 || componentId == 42) player.prayer.switchPrayer(slotId)
        else if (componentId == 43 && player.prayer.settingQuickPrayers) player.prayer.switchSettingQuickPrayer()
    }
}

class PrayerManager {
    enum class StatMod {
        ATTACK, STRENGTH, DEFENSE, RANGE, MAGE
    }

    @Transient
    private lateinit var player: Player

    @Transient
    private var active = CopyOnWriteArraySet<Prayer>()

    @Transient
    private lateinit var statMods: IntArray

    @Transient
    var settingQuickPrayers = false

    @Transient
    private var quickPrayersOn = false

    var points: Double = 1.0
        set(value) {
            field = value
            refreshPoints()
        }
    var isCurses: Boolean = false
        private set
    private var quickPrays: CopyOnWriteArraySet<Prayer> = CopyOnWriteArraySet<Prayer>()
    private var quickCurses: CopyOnWriteArraySet<Prayer> = CopyOnWriteArraySet<Prayer>()

    fun switchPrayer(prayerId: Int) {
        val prayer = Prayer.forSlot(prayerId, this.isCurses)
        if (prayer == null) return
        if (active.contains(prayer) || (settingQuickPrayers && (quickPrays.contains(prayer) || quickCurses.contains(prayer)))) closePrayer(prayer)
        else activatePrayer(prayer)
    }

    private fun canUsePrayer(prayer: Prayer): Boolean {
        if (points <= 0.0) {
            player.sendMessage("You should recharge your prayer at an altar.")
            return false
        }
        if (player.skills.getLevelForXp(Constants.PRAYER) < prayer.req) {
            player.sendMessage("You need a prayer level of at least " + prayer.req + " to use this prayer.")
            return false
        }
        if (prayer.isCurse && !player.isQuestComplete(Quest.TEMPLE_AT_SENNTISTEN, "to use ancient curses.")) return false
        when (prayer) {
            Prayer.RAPID_RENEWAL -> if (!player.hasRenewalPrayer) {
                player.sendMessage("You must unlock this prayer as a dungeoneering reward.")
                return false
            }

            Prayer.RIGOUR -> if (!player.hasRigour) {
                player.sendMessage("You must unlock this prayer as a dungeoneering reward.")
                return false
            }

            Prayer.AUGURY -> if (!player.hasAugury) {
                player.sendMessage("You must unlock this prayer as a dungeoneering reward.")
                return false
            }

            Prayer.CHIVALRY -> if (player.skills.getLevelForXp(Constants.DEFENSE) < 65) {
                player.sendMessage("You need a defence level of at least 65 to use this prayer.")
                return false
            }

            Prayer.PIETY -> if (player.skills.getLevelForXp(Constants.DEFENSE) < 70) {
                player.sendMessage("You need a defence level of at least 70 to use this prayer.")
                return false
            }

            Prayer.PROTECT_MAGIC, Prayer.PROTECT_RANGE, Prayer.PROTECT_SUMMONING, Prayer.PROTECT_MELEE, Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_RANGE, Prayer.DEFLECT_SUMMONING, Prayer.DEFLECT_MELEE -> if (player.isProtectionPrayBlocked()) {
                player.sendMessage("You are currently injured and cannot use protection prayers!")
                return false
            }

            else -> {}
        }
        return true
    }

    private fun activatePrayer(prayer: Prayer): Boolean {
        if (!canUsePrayer(prayer)) return false
        when (prayer) {
            Prayer.ATK_T1, Prayer.ATK_T2, Prayer.ATK_T3 -> closePrayers(
                Prayer.ATK_T1,
                Prayer.ATK_T2,
                Prayer.ATK_T3,
                Prayer.RNG_T1,
                Prayer.RNG_T2,
                Prayer.RNG_T3,
                Prayer.MAG_T1,
                Prayer.MAG_T2,
                Prayer.MAG_T3,
                Prayer.CHIVALRY,
                Prayer.PIETY,
                Prayer.RIGOUR,
                Prayer.AUGURY
            )

            Prayer.STR_T1, Prayer.STR_T2, Prayer.STR_T3 -> closePrayers(
                Prayer.STR_T1,
                Prayer.STR_T2,
                Prayer.STR_T3,
                Prayer.RNG_T1,
                Prayer.RNG_T2,
                Prayer.RNG_T3,
                Prayer.MAG_T1,
                Prayer.MAG_T2,
                Prayer.MAG_T3,
                Prayer.CHIVALRY,
                Prayer.PIETY,
                Prayer.RIGOUR,
                Prayer.AUGURY
            )

            Prayer.DEF_T1, Prayer.DEF_T2, Prayer.DEF_T3 -> closePrayers(Prayer.DEF_T1, Prayer.DEF_T2, Prayer.DEF_T3, Prayer.CHIVALRY, Prayer.PIETY, Prayer.RIGOUR, Prayer.AUGURY)
            Prayer.RNG_T1, Prayer.RNG_T2, Prayer.RNG_T3 -> closePrayers(
                Prayer.ATK_T1,
                Prayer.ATK_T2,
                Prayer.ATK_T3,
                Prayer.STR_T1,
                Prayer.STR_T2,
                Prayer.STR_T3,
                Prayer.RNG_T1,
                Prayer.RNG_T2,
                Prayer.RNG_T3,
                Prayer.MAG_T1,
                Prayer.MAG_T2,
                Prayer.MAG_T3,
                Prayer.CHIVALRY,
                Prayer.PIETY,
                Prayer.RIGOUR,
                Prayer.AUGURY
            )

            Prayer.MAG_T1, Prayer.MAG_T2, Prayer.MAG_T3 -> closePrayers(
                Prayer.ATK_T1,
                Prayer.ATK_T2,
                Prayer.ATK_T3,
                Prayer.STR_T1,
                Prayer.STR_T2,
                Prayer.STR_T3,
                Prayer.RNG_T1,
                Prayer.RNG_T2,
                Prayer.RNG_T3,
                Prayer.MAG_T1,
                Prayer.MAG_T2,
                Prayer.MAG_T3,
                Prayer.CHIVALRY,
                Prayer.PIETY,
                Prayer.RIGOUR,
                Prayer.AUGURY
            )

            Prayer.CHIVALRY, Prayer.PIETY, Prayer.RIGOUR, Prayer.AUGURY -> {
                closePrayers(Prayer.ATK_T1, Prayer.ATK_T2, Prayer.ATK_T3, Prayer.STR_T1, Prayer.STR_T2, Prayer.STR_T3)
                closePrayers(Prayer.RNG_T1, Prayer.RNG_T2, Prayer.RNG_T3, Prayer.MAG_T1, Prayer.MAG_T2, Prayer.MAG_T3)
                closePrayers(Prayer.DEF_T1, Prayer.DEF_T2, Prayer.DEF_T3, Prayer.CHIVALRY, Prayer.PIETY, Prayer.RIGOUR, Prayer.AUGURY)
            }

            Prayer.RAPID_RENEWAL, Prayer.RAPID_HEAL -> closePrayers(Prayer.RAPID_RENEWAL, Prayer.RAPID_HEAL)
            Prayer.PROTECT_MAGIC, Prayer.PROTECT_RANGE, Prayer.PROTECT_MELEE -> closePrayers(
                Prayer.PROTECT_MAGIC,
                Prayer.PROTECT_RANGE,
                Prayer.PROTECT_MELEE,
                Prayer.RETRIBUTION,
                Prayer.REDEMPTION,
                Prayer.SMITE
            )

            Prayer.PROTECT_SUMMONING -> closePrayers(Prayer.PROTECT_SUMMONING, Prayer.RETRIBUTION, Prayer.REDEMPTION, Prayer.SMITE)
            Prayer.SMITE, Prayer.REDEMPTION, Prayer.RETRIBUTION -> closePrayers(
                Prayer.PROTECT_MAGIC,
                Prayer.PROTECT_RANGE,
                Prayer.PROTECT_MELEE,
                Prayer.PROTECT_SUMMONING,
                Prayer.RETRIBUTION,
                Prayer.REDEMPTION,
                Prayer.SMITE
            )

            Prayer.PROTECT_ITEM_C -> if (!settingQuickPrayers) player.sync(12567, 2213)

            Prayer.BERSERKER -> if (!settingQuickPrayers) {
                player.sync(12589, 2266)
            }

            Prayer.SAP_WARRIOR -> {
                if (!settingQuickPrayers) {
                    player.sync(12569, 2214)
                    player.soundEffect(8115, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.LEECH_ATTACK, Prayer.LEECH_STRENGTH, Prayer.LEECH_DEFENSE, Prayer.LEECH_RANGE, Prayer.LEECH_MAGIC, Prayer.LEECH_ENERGY, Prayer.LEECH_SPECIAL)
            }

            Prayer.SAP_MAGE -> {
                if (!settingQuickPrayers) {
                    player.sync(12569, 2220)
                    player.soundEffect(8115, true)
                }
                closePrayers( Prayer.TURMOIL, Prayer.LEECH_ATTACK, Prayer.LEECH_STRENGTH, Prayer.LEECH_DEFENSE, Prayer.LEECH_RANGE, Prayer.LEECH_MAGIC, Prayer.LEECH_ENERGY, Prayer.LEECH_SPECIAL)
            }

            Prayer.SAP_RANGE -> {
                if (!settingQuickPrayers) {
                    player.sync(12569, 2217)
                    player.soundEffect(8115, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.LEECH_ATTACK, Prayer.LEECH_STRENGTH, Prayer.LEECH_DEFENSE, Prayer.LEECH_RANGE, Prayer.LEECH_MAGIC, Prayer.LEECH_ENERGY, Prayer.LEECH_SPECIAL)
            }

            Prayer.SAP_SPIRIT -> {
                if (!settingQuickPrayers) {
                    player.sync(12569, 2223)
                    player.soundEffect(8115, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.LEECH_ATTACK, Prayer.LEECH_STRENGTH, Prayer.LEECH_DEFENSE, Prayer.LEECH_RANGE, Prayer.LEECH_MAGIC, Prayer.LEECH_ENERGY, Prayer.LEECH_SPECIAL)
            }

            Prayer.LEECH_ATTACK -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2232)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_WARRIOR, Prayer.SAP_RANGE, Prayer.SAP_MAGE, Prayer.SAP_SPIRIT)
            }

            Prayer.LEECH_STRENGTH -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2250)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_WARRIOR, Prayer.SAP_RANGE, Prayer.SAP_MAGE, Prayer.SAP_SPIRIT)
            }

            Prayer.LEECH_DEFENSE -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2246)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_WARRIOR, Prayer.SAP_RANGE, Prayer.SAP_MAGE, Prayer.SAP_SPIRIT)
            }

            Prayer.LEECH_RANGE -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2238)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_WARRIOR, Prayer.SAP_RANGE, Prayer.SAP_MAGE, Prayer.SAP_SPIRIT)
            }

            Prayer.LEECH_MAGIC -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2242)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_WARRIOR, Prayer.SAP_RANGE, Prayer.SAP_MAGE, Prayer.SAP_SPIRIT)
            }

            Prayer.LEECH_SPECIAL -> if (!settingQuickPrayers) {
                player.sync(12575, 2258)
                player.soundEffect(8110, true)
            }

            Prayer.LEECH_ENERGY -> {
                if (!settingQuickPrayers) {
                    player.sync(12575, 2254)
                    player.soundEffect(8110, true)
                }
                closePrayers(Prayer.TURMOIL, Prayer.SAP_SPIRIT)
            }

            Prayer.DEFLECT_MAGIC -> {
                if (!settingQuickPrayers)
                    player.sync(12573, 2228)
                closePrayers(Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_MELEE, Prayer.DEFLECT_RANGE, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.DEFLECT_RANGE -> {
                if (!settingQuickPrayers)
                    player.sync(12573, 2229)
                closePrayers(Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_MELEE, Prayer.DEFLECT_RANGE, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.DEFLECT_MELEE -> {
                if (!settingQuickPrayers)
                    player.sync(12573, 2230)
                closePrayers(Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_RANGE, Prayer.DEFLECT_MELEE, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.DEFLECT_SUMMONING -> {
                if (!settingQuickPrayers)
                    player.sync(12573, 2227)
                closePrayers(Prayer.DEFLECT_SUMMONING, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.WRATH -> {
                if (!settingQuickPrayers)
                    player.anim(12575)
                closePrayers(Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_MELEE, Prayer.DEFLECT_RANGE, Prayer.DEFLECT_SUMMONING, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.SOUL_SPLIT -> {
                player.sync(12575, 2264)
                player.soundEffect(8113, true)
                closePrayers(Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_MELEE, Prayer.DEFLECT_RANGE, Prayer.DEFLECT_SUMMONING, Prayer.WRATH, Prayer.SOUL_SPLIT)
            }

            Prayer.TURMOIL -> {
                if (!settingQuickPrayers)
                    player.sync(12565, 2226)
                closePrayers(Prayer.SAP_WARRIOR, Prayer.SAP_MAGE, Prayer.SAP_RANGE, Prayer.SAP_SPIRIT)
                closePrayers(Prayer.LEECH_ATTACK, Prayer.LEECH_STRENGTH, Prayer.LEECH_DEFENSE, Prayer.LEECH_MAGIC, Prayer.LEECH_RANGE, Prayer.LEECH_SPECIAL, Prayer.LEECH_ENERGY)
            }

            else -> {}
        }
        if (settingQuickPrayers) {
            if (this.isCurses) quickCurses.add(prayer)
            else quickPrays.add(prayer)
        } else {
            active.add(prayer)
            if (isOverhead(prayer)) player.appearance.generateAppearanceData()
            if (prayer.activateSound != -1) player.soundEffect(prayer.activateSound, false)
            else player.soundEffect(2662, false)
        }
        refresh()
        return true
    }

    fun closePrayer(prayer: Prayer) {
        if (settingQuickPrayers) {
            if (this.isCurses) quickCurses.remove(prayer)
            else quickPrays.remove(prayer)
            return
        }
        if (!active.contains(prayer)) return
        when (prayer) {
            Prayer.LEECH_ATTACK -> {
                if (getStatMod(StatMod.ATTACK) > 0) player.sendMessage("Your Attack is now unaffected by sap and leech curses.", true)
                setStatMod(StatMod.ATTACK, 0)
                Leech.clearLeechBoost(player, 0)
            }

            Prayer.LEECH_STRENGTH -> {
                if (getStatMod(StatMod.STRENGTH) > 0) player.sendMessage("Your Strength is now unaffected by sap and leech curses.", true)
                setStatMod(StatMod.STRENGTH, 0)
                Leech.clearLeechBoost(player, 2)
            }

            Prayer.LEECH_DEFENSE -> {
                if (getStatMod(StatMod.DEFENSE) > 0) player.sendMessage("Your Defense is now unaffected by sap and leech curses.", true)
                setStatMod(StatMod.DEFENSE, 0)
                Leech.clearLeechBoost(player, 1)
            }

            Prayer.LEECH_RANGE -> {
                if (getStatMod(StatMod.RANGE) > 0) player.sendMessage("Your Range is now unaffected by sap and leech curses.", true)
                setStatMod(StatMod.RANGE, 0)
                Leech.clearLeechBoost(player, 4)
            }

            Prayer.LEECH_MAGIC -> {
                if (getStatMod(StatMod.MAGE) > 0) player.sendMessage("Your Magic is now unaffected by sap and leech curses.", true)
                setStatMod(StatMod.MAGE, 0)
                Leech.clearLeechBoost(player, 6)
            }

            Prayer.TURMOIL -> {
                Turmoil.resetTurmoil(player);
            }

            else -> {}
        }
        active.remove(prayer)
        if (isOverhead(prayer)) player.appearance.generateAppearanceData()
        player.soundEffect(2663, false)
        if (active.isEmpty()) setQuickPrayersOn(false)
        refresh()
    }

    fun closePrayers(vararg prayers: Prayer) {
        for (p in prayers) closePrayer(p)
    }

    val prayerHeadIcon: Int
        get() {
            if (active.isEmpty()) return -1
            if (active.contains(Prayer.PROTECT_SUMMONING)) {
                if (active.contains(Prayer.PROTECT_MELEE)) return 8
                if (active.contains(Prayer.PROTECT_RANGE)) return 9
                if (active.contains(Prayer.PROTECT_MAGIC)) return 10
                return 7
            }
            if (active.contains(Prayer.DEFLECT_SUMMONING)) {
                if (active.contains(Prayer.DEFLECT_MELEE)) return 16
                if (active.contains(Prayer.DEFLECT_RANGE)) return 17
                if (active.contains(Prayer.DEFLECT_MAGIC)) return 18
                return 15
            }
            if (active.contains(Prayer.PROTECT_MELEE)) return 0
            if (active.contains(Prayer.PROTECT_RANGE)) return 1
            if (active.contains(Prayer.PROTECT_MAGIC)) return 2
            else if (active.contains(Prayer.RETRIBUTION)) return 3
            else if (active.contains(Prayer.SMITE)) return 4
            else if (active.contains(Prayer.REDEMPTION)) return 5
            else if (active.contains(Prayer.DEFLECT_MELEE)) return 12
            else if (active.contains(Prayer.DEFLECT_MAGIC)) return 13
            else if (active.contains(Prayer.DEFLECT_RANGE)) return 14
            else if (active.contains(Prayer.WRATH)) return 19
            else if (active.contains(Prayer.SOUL_SPLIT)) return 20
            return -1
        }

    fun processPrayer() {
        Sap.tickSapDecay(player)
        Leech.tickLeechDecay(player)
        Leech.tickLeechBoostDecay(player)
        Turmoil.tickTurmoilDecay(player)
        if (player.isDead || !player.isRunning || active.isEmpty()) return
        var drain = 0.0
        for (p in active) drain += p.drain
        drain /= 1.0 + ((1.0 / 30.0) * player.combatDefinitions.getBonus(Bonus.PRAYER))
        if (drain > 0) {
            drainPrayer(drain)
            if (!checkPrayer()) {
                closeAllPrayers()
                player.soundEffect(2673, false)
            }
        }
    }

    public fun curseProjectile(player: Player, target: Entity, projectileId: Int, targetSpotAnim: Int, isLeech: Boolean) {
        val soundId = if (isLeech) 8110 else 8116
        World.sendProjectile(player, target, projectileId, 35 to 35, 20, 10, 0, 0) { p ->
            target.spotAnim(targetSpotAnim)
            player.soundEffect(target, soundId, true)
        }
    }

    fun closeAllPrayers() {
        active.clear()
        statMods = IntArray(5)
        setQuickPrayersOn(false)
        player.vars.setVar(if (this.isCurses) 1582 else 1395, 0)
        player.appearance.generateAppearanceData()
        resetStatMods()
    }

    fun switchSettingQuickPrayer() {
        settingQuickPrayers = !settingQuickPrayers
        refreshSettingQuickPrayers()
        unlockPrayerBookButtons()
        if (settingQuickPrayers) player.interfaceManager.openTab(InterfaceManager.Sub.TAB_PRAYER)
    }

    fun switchQuickPrayers() {
        if (!checkPrayer()) return
        val wasOn = quickPrayersOn
        var turnedOn = false
        closeAllPrayers()
        if (!wasOn) {
            if (this.isCurses) {
                for (curse in quickCurses) if (activatePrayer(curse)) turnedOn = true
            } else for (prayer in quickPrays) if (activatePrayer(prayer)) turnedOn = true
            setQuickPrayersOn(turnedOn)
        }
    }

    fun setQuickPrayersOn(on: Boolean) {
        quickPrayersOn = on
        player.packets.sendVarc(182, if (quickPrayersOn) 1 else 0)
    }

    fun checkPrayer(): Boolean {
        if (points <= 0) {
            player.soundEffect(2672, false)
            player.sendMessage("Please recharge your prayer at the Lumbridge Church.")
            return false
        }
        return true
    }

    fun refresh() {
        for (p in Prayer.entries) {
            player.vars.setVarBit(p.varBit, if (active.contains(p)) 1 else 0)
            player.vars.setVarBit(p.qpVarBit, if (quickPrays.contains(p) || quickCurses.contains(p)) 1 else 0)
        }
        player.vars.setVar(curse_perm2, if (this.isCurses) 1 else 0)
    }

    fun refreshSettingQuickPrayers() {
        player.packets.sendVarc(181, if (settingQuickPrayers) 1 else 0)
    }

    fun init() {
        player.vars.setVar(if (this.isCurses) 1582 else 1395, 0)
        resetStatMods()
        refresh()
        refreshSettingQuickPrayers()
        unlockPrayerBookButtons()
    }

    fun unlockPrayerBookButtons() {
        player.packets.setIFRightClickOps(271, if (settingQuickPrayers) 42 else 8, 0, 29, 0)
    }

    fun setPrayerBook(curses: Boolean) {
        if (curses && !player.isQuestComplete(Quest.TEMPLE_AT_SENNTISTEN, "to use ancient curses.")) return
        closeAllPrayers()
        this.isCurses = curses
        player.interfaceManager.sendSubDefault(InterfaceManager.Sub.TAB_PRAYER)
        refresh()
        unlockPrayerBookButtons()
    }

    fun setPlayer(player: Player) {
        this.player = player
        active = CopyOnWriteArraySet<Prayer>()
        statMods = IntArray(5)
    }

    private fun resetStatMods() {
        for (mod in StatMod.entries) setStatMod(mod, 0)
    }

    public fun getStatMod(mod: StatMod): Int {
        return statMods[mod.ordinal]
    }

    public fun setStatMod(mod: StatMod, bonus: Int) {
        statMods[mod.ordinal] = bonus
        updateStatMod(mod)
    }

    public fun decreaseStatMod(mod: StatMod, amount: Int) {
        statMods[mod.ordinal] -= amount
        updateStatMod(mod)
    }

    fun decreaseStatModifier(mod: StatMod, bonus: Int, max: Int): Boolean {
        if (statMods[mod.ordinal] > max) {
            statMods[mod.ordinal]--
            updateStatMod(mod)
            return true
        }
        return false
    }

    fun increaseStatModifier(mod: StatMod, bonus: Int, max: Int): Boolean {
        if (statMods[mod.ordinal] < max) {
            statMods[mod.ordinal]++
            updateStatMod(mod)
            return true
        }
        return false
    }

    private fun updateStatMod(mod: StatMod) {
        player.vars.setVarBit(6857 + mod.ordinal, 30 + statMods[mod.ordinal])
    }

    private fun updateStatMods() {
        for (m in StatMod.entries) updateStatMod(m)
    }

    val mageMultiplier: Double
        get() {
            if (active.isEmpty()) return 1.0
            var value = 1.0

            if (active(Prayer.MAG_T1)) value += 0.05
            else if (active(Prayer.MAG_T2)) value += 0.10
            else if (active(Prayer.MAG_T3)) value += 0.15
            else if (active(Prayer.AUGURY)) value += 0.20
            else if (active(Prayer.LEECH_MAGIC)) {
                val d = (5 + getStatMod(StatMod.MAGE)).toDouble()
                value += d / 100
            }
            return value
        }

    val rangeMultiplier: Double
        get() {
            if (active.isEmpty()) return 1.0
            var value = 1.0

            if (active(Prayer.RNG_T1)) value += 0.05
            else if (active(Prayer.RNG_T2)) value += 0.10
            else if (active(Prayer.RNG_T3)) value += 0.15
            else if (active(Prayer.RIGOUR)) value += 0.20
            else if (active(Prayer.LEECH_RANGE)) {
                val d = (5 + getStatMod(StatMod.RANGE)).toDouble()
                value += d / 100
            }
            return value
        }

    val attackMultiplier: Double
        get() {
            if (active.isEmpty()) return 1.0
            var value = 1.0

            if (active(Prayer.ATK_T1)) value += 0.05
            else if (active(Prayer.ATK_T2)) value += 0.10
            else if (active(Prayer.ATK_T3)) value += 0.15
            else if (active(Prayer.CHIVALRY)) value += 0.15
            else if (active(Prayer.PIETY)) value += 0.20
            else if (active(Prayer.LEECH_ATTACK)) {
                val d = (5 + getStatMod(StatMod.ATTACK)).toDouble()
                value += d / 100
            } else if (active(Prayer.TURMOIL)) {
                val d = (15 + getStatMod(StatMod.ATTACK)).toDouble()
                value += d / 100
            }
            return value
        }

    val strengthMultiplier: Double
        get() {
            if (active.isEmpty()) return 1.0
            var value = 1.0

            if (active(Prayer.STR_T1)) value += 0.05
            else if (active(Prayer.STR_T2)) value += 0.10
            else if (active(Prayer.STR_T3)) value += 0.15
            else if (active(Prayer.CHIVALRY)) value += 0.18
            else if (active(Prayer.PIETY)) value += 0.23
            else if (active(Prayer.LEECH_STRENGTH)) {
                val d = (5 + getStatMod(StatMod.STRENGTH)).toDouble()
                value += d / 100
            } else if (active(Prayer.TURMOIL)) {
                val d = (23 + getStatMod(StatMod.STRENGTH)).toDouble()
                value += d / 100
            }
            return value
        }

    val defenceMultiplier: Double
        get() {
            if (active.isEmpty()) return 1.0
            var value = 1.0

            if (active(Prayer.DEF_T1)) value += 0.05
            else if (active(Prayer.DEF_T2)) value += 0.10
            else if (active(Prayer.DEF_T3)) value += 0.15
            else if (active(Prayer.CHIVALRY)) value += 0.20
            else if (active(Prayer.PIETY) || active(Prayer.RIGOUR) || active(Prayer.AUGURY)) value += 0.25
            else if (active(Prayer.LEECH_DEFENSE)) {
                val d = (6 + getStatMod(StatMod.DEFENSE)).toDouble()
                value += d / 100
            } else if (active(Prayer.TURMOIL)) {
                val d = (15 + getStatMod(StatMod.DEFENSE)).toDouble()
                value += d / 100
            }
            return value
        }

    fun refreshPoints() {
        player.vars.setVar(prayer_points_var, points.toInt())
    }

    fun hasFullPoints(): Boolean {
        return points >= player.skills.getLevelForXp(Constants.PRAYER) * 10
    }

    fun drainPrayer(amount: Double) {
        if (player.nsv.getB("infPrayer")) return
        points -= amount
        if (points <= 0) points = 0.0
        refreshPoints()
    }

    fun drainPrayer() {
        if (player.nsv.getB("infPrayer")) return
        points = 0.0
        refreshPoints()
    }

    fun restorePrayer(amount: Double) {
        var amount = amount
        val maxPrayer = player.skills.getLevelForXp(Constants.PRAYER) * 10
        amount *= player.auraManager.getPrayerResMul()
        if ((points + amount) <= maxPrayer) points += amount
        else points = maxPrayer.toDouble()
        refreshPoints()
    }

    fun active(vararg prayers: Prayer?): Boolean {
        for (prayer in prayers) if (active.contains(prayer)) return true
        return false
    }

    val isProtectingItem: Boolean
        get() = active(Prayer.PROTECT_ITEM_C, Prayer.PROTECT_ITEM_N)

    val isProtectingMage: Boolean
        get() = active(Prayer.PROTECT_MAGIC, Prayer.DEFLECT_MAGIC)

    val isProtectingRange: Boolean
        get() = active(Prayer.PROTECT_RANGE, Prayer.DEFLECT_RANGE)

    val isProtectingMelee: Boolean
        get() = active(Prayer.PROTECT_MELEE, Prayer.DEFLECT_MELEE)

    fun hasProtectionPrayersOn(): Boolean {
        return active(
            Prayer.PROTECT_MAGIC,
            Prayer.PROTECT_MELEE,
            Prayer.PROTECT_RANGE,
            Prayer.PROTECT_SUMMONING,
            Prayer.DEFLECT_MAGIC,
            Prayer.DEFLECT_MELEE,
            Prayer.DEFLECT_RANGE,
            Prayer.DEFLECT_SUMMONING
        )
    }

    fun reset() {
        closeAllPrayers()
        points = (player.skills.getLevelForXp(Constants.PRAYER) * 10).toDouble()
        refreshPoints()
    }

    val isUsingProtectionPrayer: Boolean
        get() = this.isProtectingMage || this.isProtectingRange || this.isProtectingMelee

    fun hasPrayersOn(): Boolean {
        return !active.isEmpty()
    }

    @JvmOverloads
    fun worshipAltar(multiplier: Double = 1.0) {
        var maxPray = player.skills.getLevelForXp(Constants.PRAYER) * 10.0
        maxPray = (maxPray * multiplier).toInt().toDouble()
        if (points < maxPray) {
            player.lock()
            player.sendMessage("You pray to the gods...", true)
            player.anim(645)
            val finalMaxPray = maxPray
            player.tasks.schedule(2) {
                player.unlock()
                restorePrayer(finalMaxPray)
                player.sendMessage("...and recharged your prayer.", true)
            }
        }
    }

    companion object {
        fun isOverhead(p: Prayer): Boolean {
            return when (p) {
                Prayer.PROTECT_MAGIC, Prayer.PROTECT_SUMMONING, Prayer.PROTECT_RANGE, Prayer.PROTECT_MELEE, Prayer.RETRIBUTION, Prayer.REDEMPTION, Prayer.SMITE, Prayer.DEFLECT_MELEE, Prayer.DEFLECT_SUMMONING, Prayer.DEFLECT_MAGIC, Prayer.DEFLECT_RANGE, Prayer.WRATH, Prayer.SOUL_SPLIT -> true
                else -> false
            }
        }
    }
}
