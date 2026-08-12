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
package com.rs.game.content.skills.smithing

import com.rs.cache.loaders.interfaces.ComponentType
import com.rs.cache.loaders.interfaces.IComponentDefinitions
import com.rs.game.content.skills.util.Category
import com.rs.game.content.skills.util.CreationAction
import com.rs.game.content.skills.util.CreationActionD
import com.rs.game.content.skills.util.ReqItem
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.lib.util.Utils.clampI
import com.rs.lib.util.Utils.toInterfaceHash
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onChunkEnter
import com.rs.plugin.kts.onNpcClick
import com.rs.plugin.kts.onObjectClick
import com.rs.plugin.kts.onXpDrop
import com.rs.utils.Areas
import com.rs.engine.variables.artisan_temp_bar_number

private enum class Metal(val itemId: Int, val varBit: Int, val cap: Int, val furnaceComp: Int, val barSet: Array<ReqItem>) {
    Iron(441, 8857,  4000, 201, arrayOf(ReqItem.IRON_INGOT_I, ReqItem.IRON_INGOT_II, ReqItem.IRON_INGOT_III, ReqItem.IRON_INGOT_IV)),
    Coal(454, 8865, 8000, 213, arrayOf(ReqItem.STEEL_INGOT_I, ReqItem.STEEL_INGOT_II, ReqItem.STEEL_INGOT_III, ReqItem.STEEL_INGOT_IV)),
    Mithril(448, 8859, 4000, 225, arrayOf(ReqItem.MITHRIL_INGOT_I, ReqItem.MITHRIL_INGOT_II, ReqItem.MITHRIL_INGOT_III, ReqItem.MITHRIL_INGOT_IV)),
    Adamantite(450, 8862, 4000, 237, arrayOf(ReqItem.ADAMANT_INGOT_I, ReqItem.ADAMANT_INGOT_II, ReqItem.ADAMANT_INGOT_III, ReqItem.ADAMANT_INGOT_IV)),
    Runite(452, 8863, 4000, 249, arrayOf(ReqItem.RUNE_INGOT_I, ReqItem.RUNE_INGOT_II, ReqItem.RUNE_INGOT_III, ReqItem.RUNE_INGOT_IV));

    fun amountStored(player: Player) = player.vars.getVarBit(varBit)
    fun add(player: Player, amount: Int) = player.vars.saveVarBit(varBit, clampI(amountStored(player) + amount, 0, cap))
    fun remove(player: Player, amount: Int) = player.vars.saveVarBit(varBit, clampI(amountStored(player) - amount, 0, cap))
}

@ServerStartupEvent
fun mapArtisansPlugins() {
    onXpDrop { e ->
        if (e.skillId != Skills.SMITHING || e.player.actionManager.action !is CreationAction)
            return@onXpDrop
        val currXp = e.player.xpCount
        if ((currXp + e.xp) > 10000) {
            e.player.xpCount = 0
            e.player.reputation++
        } else
            e.player.xpCount += e.xp.toInt()
    }

    onObjectClick(29396) { it.player.depositAllArmor() }

    onObjectClick(4046) { (player) ->
        val highestIngot = player.getHighestIngot()
        if (highestIngot != -1) player.startConversation(CreationActionD(player, Category.ARTISANS, highestIngot, 898, 15, true))
        else player.sendMessage("You don't have any ingots to smith.")
    }

    onObjectClick(29394, 29395) { (player, obj, option) ->
        when(option) {
            "Withdraw-ingots" -> player.openFurnace(burial = obj.id == 29395)
            "Return-ingots" -> player.returnAllIngots()
            "Deposit-ores" -> player.handleDepositOres()
            "Withdraw-ores" -> player.handleWithdrawOres()
        }
    }

    onButtonClick(1072) { (player, _, componentId) ->
        val ingotMode = IngotMode.entries.find { it.comp == componentId }
        if (ingotMode != null) {
            player.tempAttribs.setO<IngotMode>("lastIngotMode", ingotMode)
            player.packets.sendRunScript(4183, toInterfaceHash(1072, componentId))
            return@onButtonClick
        }
        val metalType = Metal.entries.find { it.furnaceComp == componentId }
        if (metalType != null) {
            val lastMode = player.tempAttribs.getO<IngotMode>("lastIngotMode") ?: return@onButtonClick player.sendMessage("Please choose a bar type.")
            val ingot = metalType.barSet[lastMode.ordinal]
            player.withdrawBars(ingot)
            player.closeInterfaces()
            return@onButtonClick
        }
        when(componentId) {
            31, 32 -> player.ingotQuantity += (if (componentId == 31) 1 else -1)
            else -> player.sendMessage("Ingot button: $componentId")
        }
    }

    onNpcClick(6653) { (player, _, option) ->
        when(option) {
            "Rewards" -> player.openReputationShop()
        }
    }

    onButtonClick(825) { (player, _, componentId) ->
        val reward = Reward.byComponentId[componentId]
        if (reward != null) return@onButtonClick reward.select(player)

        when(componentId) {
            87 -> Reward.entries[player.selectedRewardIndex].let {
                if (player.reputation < it.price || !it.meetsRequirements(player)) return@onButtonClick
                player.unlockReward(it)
                it.markUnlocked(player)
                player.reputation -= it.price
            }
            else -> player.sendMessage("Reward component: $componentId")
        }
    }

    onChunkEnter { (entity, chunkId) ->
        if (entity !is Player) return@onChunkEnter
        val inBurialZone = Areas.withinArea("artisans_burial", chunkId)
        val hasOverlay = entity.interfaceManager.topOpen(1073)
        when {
            inBurialZone && !hasOverlay -> entity.interfaceManager.sendOverlay(1073)
            !inBurialZone && hasOverlay -> entity.interfaceManager.removeOverlay()
        }
    }
}

private var Player.selectedRewardIndex
    get() = vars.getVarBit(8902).coerceAtLeast(1) - 1 //minimum of 1
    set(value) = vars.setVarBit(8902, clampI(value, 1, 12))

private var Player.ingotQuantity
    get() = vars.getVarBit(artisan_temp_bar_number).let { if (it <= 0) inventory.freeSlots else it }
    set(value) = vars.setVarBit(artisan_temp_bar_number, clampI(value, 1, 28))

private var Player.xpCount
    get() = getI("artisansXpCount", 0)
    set(value) = set("artisansXpCount", clampI(value, 0, 10000))

private var Player.reputation
    get() = getI("artisansRep", 0)
    set(value) = set("artisansRep", clampI(value, 0, 100))

private var Player.currentInstruction
    get() = tempAttribs.getO<String>("currentArtisanInstruction") ?: "Helmets"
    set(value) {
        tempAttribs.setO<String>("currentArtisanInstruction", value)
        if (interfaceManager.topOpen(1073))
            packets.setIFText(1073, 11, value)
    }

private fun Player.unlockReward(reward: Reward) = set("artisans$reward", true)
private fun Player.unlocked(reward: Reward) = getBool("artisans$reward")

private enum class IngotMode(val comp: Int) {
    I(138),
    II(150),
    III(162),
    IV(174)
}

private val ceremonialBarHiddenComps = intArrayOf(129, 133, 136, 138, 150, 152, 162)

private fun Player.openFurnace(burial: Boolean = true) {
    ingotQuantity = ingotQuantity
    if (burial) {
        packets.setIFHidden(1072, 164, true)
        packets.setIFHidden(1072, 165, true)
        packets.setIFHidden(1072, 172, true)
    } else
        ceremonialBarHiddenComps.forEach { packets.setIFHidden(1072, it, true) }
    interfaceManager.sendInterface(1072)
    packets.sendRunScript(4182)
    val lastMode = tempAttribs.getO<IngotMode>("lastIngotMode") ?: IngotMode.I
    tempAttribs.setO<IngotMode>("lastIngotMode", lastMode)
    if (!burial)
        tempAttribs.setO<IngotMode>("lastIngotMode", IngotMode.IV)
    if (!burial)
        packets.sendRunScript(4183, toInterfaceHash(1072, (tempAttribs.getO("lastIngotMode") ?: IngotMode.IV).comp))
    else
        packets.sendRunScript(4183, toInterfaceHash(1072, (tempAttribs.getO("lastIngotMode") ?: lastMode).comp))
    packets.sendRunScript(4184, 1, 0)
}

private enum class Reward(val componentId: Int, val price: Int, val desc: String, val meetsRequirements: (Player) -> Boolean = { true }) {
    QUICK_REPAIRS(93, 5, "Repairing burst pipes in the Workshop's ceremonial swords area will become 50% faster."),
    REPAIR_EXPERT(97, 15, "Repairing the pipes gives 50 more Experience."),
    QUICK_LEARNER(101, 20, "Experience gained for creation of assigned burial armour is increased by 2%."),
    BUDDING_STUDENT(105, 40, "Experience gained for creation of assigned burial armour is increased by 2%. Requires and stacks with Quick Learner for a 4% bonus.", { it.unlocked(QUICK_LEARNER)}),
    MASTER_STUDENT(109, 60, "Experience gained for creation of assigned burial armour is increased by 1%. Requires and stacks with Quick Learner and Budding Student for a 5% bonus.", { it.unlocked(MASTER_STUDENT)}),
    CEREMONIAL_SWORD_I(113, 30, "Allows keeping a perfect iron ceremonial sword that looks close to the sword of the plans you have bought."),
    CEREMONIAL_SWORD_II(117, 30, "Allows keeping a perfect steel ceremonial sword that looks close to the sword of the plans you have bought.", { it.unlocked(CEREMONIAL_SWORD_I)}),
    CEREMONIAL_SWORD_III(121, 30, "Allows keeping a perfect mithril ceremonial sword that looks close to the sword of the plans you have bought.", { it.unlocked(CEREMONIAL_SWORD_II)}),
    CEREMONIAL_SWORD_IV(125, 30, "Allows keeping a perfect adamant ceremonial sword that looks close to the sword of the plans you have bought.", { it.unlocked(CEREMONIAL_SWORD_III)}),
    CEREMONIAL_SWORD_V(129, 30, "Allows keeping a perfect rune ceremonial sword that looks close to the sword of the plans you have bought.", { it.unlocked(CEREMONIAL_SWORD_IV)}),
    GOLDEN_CANNON(133, 50, "Allows turning the Dwarf multicannon to Golden version that can hold more cannonballs."),
    ROYALE_CANNON(136, 100, "Allows turning the Dwarf multicannon to Royale version that can hold more cannonballs. Requires Golden Cannon.", { it.unlocked(GOLDEN_CANNON)});

    val defs = IComponentDefinitions.getInterfaceComponent(825, componentId) ?: error("Invalid artisans reward component id")
    val spriteChild = defs.children.first { it.type == ComponentType.SPRITE } ?: error("Sprite not found")
    val textChild = defs.children.first { it.type == ComponentType.TEXT && it.text.contains("%") } ?: error("Text not found")

    fun select(player: Player) {
        player.selectedRewardIndex = ordinal+1
        player.packets.sendRunScript(205, toInterfaceHash(825, spriteChild.componentId))
        player.packets.setIFText(825, 66, name.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } })
        player.packets.setIFText(825, 67, desc)
        player.packets.setIFText(825, 69, if (player.unlocked(this)) "Earned" else "$price%")
        player.packets.setIFHidden(825, 70, player.unlocked(this) || player.reputation < price || !this.meetsRequirements(player))
    }

    fun markUnlocked(player: Player) {
        player.packets.setIFGraphic(825, spriteChild.componentId, 5457)
        player.packets.setIFText(825, textChild.componentId, "<col=00FF00>Earned")
        select(player)
    }

    fun update(player: Player) {
        if (player.unlocked(this)) markUnlocked(player)
    }

    companion object {
        val byComponentId = entries.associateBy { it.componentId }
    }
}

private fun Player.updateReputation() {
    packets.setIFText(825, 55, "${reputation}%")
}

private fun Player.openReputationShop() {
    interfaceManager.sendInterface(825)
    updateReputation()
    Reward.entries.forEach { it.update(this) }
}

private fun Player.depositOres(metal: Metal, amount: Int) {
    var amount = amount
    if (amount > inventory.getAmountOf(metal.itemId))
        amount = inventory.getAmountOf(metal.itemId)
    if (amount == 0) {
        sendMessage("You don't have any ${metal.name} to deposit.")
        return
    }
    if (amount + metal.amountStored(this) > metal.cap)
        amount = metal.cap - metal.amountStored(this)
    if (amount == 0) {
        sendMessage("You already have ${metal.cap} ${metal.name} stored in the furnace. You can't fit any more in.")
        return
    }
    inventory.deleteItem(metal.itemId, amount)
    metal.add(this, amount)
}

private fun Player.withdrawOres(metal: Metal, amount: Int) {
    var amount = amount
    if (amount > metal.amountStored(this)) amount = metal.amountStored(this)
    if (amount == 0) {
        sendMessage("You don't have any ${metal.name} to withdraw.")
        return
    }
    inventory.addItem(metal.itemId, amount)
    metal.remove(this, amount)
}

private fun Player.handleDepositOres() {
    val filteredOres = Metal.entries.filter { inventory.containsItem(it.itemId, 1) }.map { it.name.replace(" ore", " ").trim() }
    if (filteredOres.size < 1) {
        sendMessage("You don't have any ores to return.")
        return
    }
    sendOptionDialogue("Which ore would you like to deposit?") { ops ->
        for (opName in filteredOres) ops.add(opName) { promptForDeposit(Metal.valueOf(opName)) }
        ops.add("None")
    }
}

private fun Player.promptForDeposit(metal: Metal) {
    sendOptionDialogue("How many would you like to deposit?") { ops ->
        ops.add("10") { depositOres(metal, 10) }
        ops.add("100") { depositOres(metal, 100) }
        ops.add("1000") { depositOres(metal, 1000) }
        ops.add("All") { depositOres(metal, Int.Companion.MAX_VALUE) }
        ops.add("X") { sendInputInteger("How many would you like to deposit?") { amount: Int -> depositOres(metal, amount) } }
    }
}

private fun Player.handleWithdrawOres() {
    val filteredOres = Metal.entries.filter { it.amountStored(this) > 0 }.map { it.name.replace(" ore", " ").trim() }
    if (filteredOres.size < 1) {
        sendMessage("You don't have any ores to return.")
        return
    }
    sendOptionDialogue("Which ore would you like to withdraw?") { ops ->
        for (opName in filteredOres) ops.add(opName) { promptForWithdrawal(Metal.valueOf(opName)) }
        ops.add("None")
    }
}

private fun Player.promptForWithdrawal(metal: Metal) {
    sendOptionDialogue("How many would you like to withdraw?") { ops ->
        ops.add("10") { withdrawOres(metal, 10) }
        ops.add("100") { withdrawOres(metal, 100) }
        ops.add("1000") { withdrawOres(metal, 1000) }
        ops.add("All") { withdrawOres(metal, Int.Companion.MAX_VALUE) }
        ops.add("X") { sendInputInteger("How many would you like to deposit?") { amount -> withdrawOres(metal, amount) } }
    }
}

private fun Player.depositAllArmor() {
    for (i in 20572..20631)
        if (inventory.containsItem(i, 1))
            inventory.deleteItem(i, inventory.getAmountOf(i))
    sendMessage("You deposit all your armor.")
}

private fun Player.withdrawBars(ingot: ReqItem) {
    var quantity = ingotQuantity
    if (quantity > inventory.freeSlots)
        quantity = inventory.freeSlots
    val materials = ingot.getMaterialsFor(quantity)
    if (materials.isEmpty()) return
    for (mat in materials) {
        val metal = Metal.valueOf(mat.definitions.name.replace(" ore", " ").trim())
        if (metal.amountStored(this) < mat.getAmount()) {
            sendMessage("You need ${mat.getAmount()} ${mat.definitions.name} to make $quantity ${ingot.product.definitions.name}${if (quantity > 1) "s" else ""}.")
            return
        }
    }
    for (mat in materials) {
        val metal = Metal.valueOf(mat.definitions.name.replace(" ore", " ").trim())
        metal.remove(this, mat.getAmount())
    }
    inventory.addItemDrop(Item(ingot.product.setAmount(quantity)))
}

private fun Player.returnAllIngots() {
    for (i in 20632..20652) if (inventory.containsItem(i, 1)) {
        val defs = ReqItem.getRequirements(i)
        val materials = defs.getMaterialsFor(inventory.getAmountOf(i))
        for (mat in materials) {
            val metal = Metal.valueOf(mat.definitions.name.replace(" ore", " ").trim())
            if (mat.getAmount() + metal.amountStored(this) > metal.cap) {
                sendMessage("You already have " + metal.cap + " ${metal.name} stored in the furnace. You can't fit any more in.")
                return
            }
        }
        for (mat in materials) {
            val metal = Metal.valueOf(mat.definitions.name.replace(" ore", " ").trim())
            metal.add(this, mat.getAmount())
        }
        inventory.deleteItem(i, inventory.getAmountOf(i))
    }
}

private fun Player.getHighestIngot(): Int {
    var highest = 20632
    for (i in 20632..20652) if (inventory.containsItem(i, 1)) highest = i
    return highest
}

private fun Player.openCeremonialSword() {
    interfaceManager.sendInterface(1074)
    interfaceManager.sendInventoryInterface(1071)
    packets.sendRunScript(4215)
    //packets.sendRunScript(4216, animId)
}