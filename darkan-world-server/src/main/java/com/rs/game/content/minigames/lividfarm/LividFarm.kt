package com.rs.game.content.minigames.lividfarm

import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.Rune
import com.rs.game.content.skills.magic.RuneSet
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.Constants
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils.clampI
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.Ticks
import com.rs.engine.variables.lunarfm_produce_points
import com.rs.engine.variables.lunarfm_spells_unlocked

const val IDENTIFY_LIVID_INTERFACE = 1081

private const val TRADE_WAGON = 40443
private const val PRODUCE_PILE_BASE = 40441
private const val PRODUCE_PILE_VARBIT = 9042
private const val LIVID_PLANT = 20704
private const val LIVID_PLANT_BUNCH = 20705

private const val LogPile = 40444
private const val lunarLumber = 20702
private const val lunarFencepost = 20703

private const val MAX_PRODUCE_POINTS = 85000 // CS2 is Varbit/10

var Player.producePoints: Int
    get() = vars.getVarBit(lunarfm_produce_points)
    set(value) = vars.saveVarBit(lunarfm_produce_points, clampI(value, 0, MAX_PRODUCE_POINTS))

var Player.lividClaims: Int
    get() = vars.getVarBit(lunarfm_spells_unlocked)
    set(value) = vars.saveVarBit(lunarfm_spells_unlocked, value)

fun Player.isOnLividFarm(): Boolean {
    return tile.x in 2096..2120 && tile.y in 3941..3953
}

class LividFarm {
    companion object {
        val PATCHES = listOf(
            //A1-5
            Patch(40461, 9044, 1, Tile.of(2098, 3949, 0)),
            Patch(40460, 9043, 1, Tile.of(2100, 3949, 0)),
            Patch(40463, 9046, 1, Tile.of(2102, 3949, 0)),
            Patch(40488, 9050, 2, Tile.of(2104, 3949, 0)),
            Patch(40462, 9045, 1, Tile.of(2106, 3949, 0)),
            //B1-5
            Patch(40533, 9057, 4, Tile.of(2098, 3946, 0)),
            Patch(40499, 9053, 3, Tile.of(2100, 3946, 0)),
            Patch(40487, 9049, 2, Tile.of(2102, 3946, 0)),
            Patch(40464, 9047, 2, Tile.of(2104, 3946, 0)),
            Patch(40486, 9048, 2, Tile.of(2106, 3946, 0)),
            //C1-5
            Patch(40504, 9054, 3, Tile.of(2098, 3943, 0)),
            Patch(40532, 9056, 4, Tile.of(2100, 3943, 0)),
            Patch(40534, 9058, 4, Tile.of(2102, 3943, 0)),
            Patch(40505, 9055, 4, Tile.of(2104, 3943, 0)),
            Patch(40492, 9052, 3, Tile.of(2106, 3943, 0)),
        )

        val PATCH_BY_OBJECT = PATCHES.associateBy { it.objectId }

        var pauline: NPC? = null

        val FENCES = listOf(
            Fence(40435, 9039), // FenceRightSouth
            Fence(40434, 9038), // FenceRightNorth
            Fence(40432, 9036), // FenceTopEast
            Fence(40437, 9041), // FenceTopMiddle
            Fence(40433, 9037), // FenceTopWest
        )
    }
}

data class Patch(val objectId: Int, val varbit: Int, val strain: Int, val tile: Tile)
data class Fence(val objectId: Int, val varbit: Int)


@ServerStartupEvent
fun mapLividFarm() {
    LividFarm.PATCHES.forEach { patch ->
        onObjectClick(patch.objectId, checkDistance = false) { (player, _) ->
            if(!player.isOnLividFarm()) {
                player.sendMessage("You must be on Livid Farm to do that.")
                return@onObjectClick
            }
            val currentState = player.vars.getVarBit(patch.varbit)
            player.tempAttribs.setO<Patch>("LividFarm.currentLividFarmPatch", patch)
            when (currentState) {
                0 -> { //Empty patch
                    if (!player.canCastSpell() || !Magic.checkMagicAndRunes(player, 1, false, RuneSet(Rune.ASTRAL, 3, Rune.EARTH, 15, Rune.NATURE, 2))) {
                        return@onObjectClick
                    }
                    player.lock(Ticks.fromSeconds(3))
                    Magic.checkMagicAndRunes(player, 1, true, RuneSet(Rune.ASTRAL, 3, Rune.EARTH, 15, Rune.NATURE, 2))
                    player.skills.addXp(Constants.MAGIC, if (player.getLevel(Skills.MAGIC) >= 83) 130.5 else 130.5 / 2)
                    player.skills.addXp(Constants.FARMING, 137.7)
                    player.anim(4411)
                    player.spotAnim(SpotAnim(728, 0, 100))
                    player.vars.setVarBit(patch.varbit, 1)
                    player.producePoints += 2
                    player.sendMessage("You are awarded 20 Produce Points. Current total: ${player.producePoints * 10}")
                }
                1, 2 -> { //Bud or Healthy
                    player.sendMessage("Composting it isn't going to make it get any bigger.")
                }
                3 -> { //Diseased
                    if (!player.canCastSpell() || !Magic.checkMagicAndRunes(player, 1, false, RuneSet(Rune.ASTRAL, 1, Rune.EARTH, 8))) {
                        return@onObjectClick
                    }
                    else
                        player.interfaceManager.sendInventoryInterface(IDENTIFY_LIVID_INTERFACE)
                }
                4 -> { // Fully Grown Healthy Plant
                    player.sendMessage("It's fully grown... Composting it isn't going to make it get any bigger.")
                }
            }
        }
    }
    onButtonClick(IDENTIFY_LIVID_INTERFACE) { e ->
        if(!e.player.isOnLividFarm()) {
            e.player.sendMessage("You must be on Livid Farm to do that.")
            return@onButtonClick
        }
        val patch = e.player.tempAttribs.getO<Patch>("LividFarm.currentLividFarmPatch") ?: return@onButtonClick
        if (e.componentId == patch.strain + 2) {
            e.player.lock(Ticks.fromSeconds(3))
            e.player.sendMessage("You identify the correct strain, curing the Livid")
            Magic.checkMagicAndRunes(e.player, 1, true, RuneSet(Rune.ASTRAL, 1, Rune.EARTH, 8))
            e.player.anim(4411)
            e.player.spotAnim(SpotAnim(728, 0, 100))
            e.player.skills.addXp(Constants.MAGIC, if (e.player.getLevel(Skills.MAGIC) >= 83) 130.5 else 130.5 / 2)
            e.player.skills.addXp(Constants.FARMING, 137.7)
            e.player.vars.setVarBit(patch.varbit, 4)
            e.player.producePoints += 2
            e.player.sendMessage("You are awarded 20 Produce Points. Current total: ${e.player.producePoints * 10}")
            e.player.interfaceManager.removeInterface(IDENTIFY_LIVID_INTERFACE)
            return@onButtonClick
        }
        else {
            e.player.sendMessage("That's not the correct strain.")
            e.player.interfaceManager.removeInterface(IDENTIFY_LIVID_INTERFACE)
        }
    }
    onObjectClick(LogPile) { e ->
        if(!e.player.isOnLividFarm()) {
            e.player.sendMessage("You must be on Livid Farm to do that.")
            return@onObjectClick
        }
        if(e.option.equals("Take-1", true)) {
            if(e.player.inventory.freeSlots < 1)
                e.player.sendMessage("You do not have enough inventory space to take the logs.")
            else
                e.player.inventory.addItem(lunarLumber,1)
        }
        if(e.option.equals("Take-5", true)) {
            if(e.player.inventory.freeSlots < 5)
                e.player.sendMessage("You do not have enough inventory space to take the logs.")
            else
                e.player.inventory.addItem(lunarLumber,5)
        }
    }
    onObjectClick(TRADE_WAGON) { e ->
        if(!e.player.isOnLividFarm()) {
            e.player.sendMessage("You must be on Livid Farm to do that.")
            return@onObjectClick
        }
        if(e.option.equals("Deposit", true)) {
            if(!e.player.inventory.containsItem(LIVID_PLANT_BUNCH))
                e.player.sendMessage("You do not have any Livid Plant Bunch's to deposit.")
            else {
                e.player.inventory.deleteItem(LIVID_PLANT_BUNCH, 1)
                e.player.producePoints += 6
                e.player.sendMessage("You are awarded 60 Produce Points. Current total: ${e.player.producePoints * 10}")
            }
        }
    }
    LividFarm.FENCES.forEach { fence ->
        onObjectClick(fence.objectId, checkDistance = true) { (player, _) ->
            if(!player.isOnLividFarm()) {
                player.sendMessage("You must be on Livid Farm to do that.")
                return@onObjectClick
            }
            if(!player.inventory.containsItem(lunarFencepost))
                player.sendMessage("You do not have any Lunar Fenceposts to repair the fence with.")
            else if (player.vars.getVarBit(fence.varbit) == 1 ) {
                player.lock(Ticks.fromSeconds(3))
                player.inventory.deleteItem(lunarFencepost, 1)
                player.skills.addXp(Constants.MAGIC, if (player.getLevel(Skills.MAGIC) >= 86) 135.0 else 135.0 / 2)
                player.skills.addXp(Constants.CONSTRUCTION, 81.9)
                player.vars.setVarBit(fence.varbit, 0)
                player.producePoints += 2
                player.sendMessage("You are awarded 20 Produce Points. Current total: ${player.producePoints * 10}")
            }
        }
    }
    onItemClick(lunarLumber) { e ->
        if (!e.player.canCastSpell() || !Magic.checkMagicAndRunes(e.player, 1, false, RuneSet(Rune.ASTRAL, 2, Rune.EARTH, 15, Rune.NATURE, 1))) {
            return@onItemClick
        }
        Magic.checkMagicAndRunes(e.player, 1, true, RuneSet(Rune.ASTRAL, 2, Rune.EARTH, 15, Rune.NATURE, 1))
        e.player.anim(6298)
        e.player.spotAnim(SpotAnim(1063, 0, 50))
        e.player.inventory.deleteItem(lunarLumber, 1)
        e.player.inventory.addItem(lunarFencepost, 1)
        e.player.addSpellDelay(2)
        e.player.soundEffect(3617, true)
    }
    onObjectClick(PRODUCE_PILE_BASE) { e ->
        if(!e.player.isOnLividFarm()) {
            e.player.sendMessage("You must be on Livid Farm to do that.")
            return@onObjectClick
        }
        if(e.option.equals("Take", true)) {
            if(e.player.inventory.freeSlots < 10)
                e.player.sendMessage("You do not have enough inventory space to take the produce (10).")
            else {
                e.player.inventory.addItem(LIVID_PLANT, 10)
                e.player.vars.setVarBit(PRODUCE_PILE_VARBIT, 0)
            }
        }
    }
    onItemClick(LIVID_PLANT) { e ->
        if (!e.player.canCastSpell() || !Magic.checkMagicAndRunes(e.player, 1, false, RuneSet(Rune.ASTRAL, 2, Rune.EARTH, 10, Rune.WATER, 5))) {
            return@onItemClick
        }
        Magic.checkMagicAndRunes(e.player, 1, true, RuneSet(Rune.ASTRAL, 2, Rune.EARTH, 10, Rune.WATER, 5))
        e.player.lock(Ticks.fromSeconds(3))
        e.player.anim(6298)
        e.player.spotAnim(SpotAnim(1063, 0, 50))
        e.player.inventory.deleteItem(LIVID_PLANT, 5)
        e.player.inventory.addItem(LIVID_PLANT_BUNCH, 1)
        e.player.skills.addXp(Constants.MAGIC, if (e.player.getLevel(Skills.MAGIC) >= 80) 124.0 else 124.0 / 2)
        e.player.skills.addXp(Constants.CONSTRUCTION, 405.0)
        e.player.addSpellDelay(2)
        e.player.soundEffect(3617, true)
    }
    onLogin { e ->
        e.player.vars.setVarBit(lunarfm_spells_unlocked, e.player.lividClaims)
        e.player.vars.setVarBit(lunarfm_produce_points, e.player.producePoints)
        e.player.vars.syncVarsToClient()
    }
}