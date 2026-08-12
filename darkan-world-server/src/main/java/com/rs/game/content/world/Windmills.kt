package com.rs.game.content.world

import com.rs.game.content.world.Windmill.Companion.EMPTY_POT
import com.rs.game.content.world.Windmill.Companion.WHEAT
import com.rs.game.content.world.Windmill.Companion.WHEAT_GROUND_KEY
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick

enum class Windmill(val hopperId: Int, val leverId: Int, val flourBinId: Int, val varbitId: Int) {
    LUMBRIDGE(hopperId = 70034, leverId = 2718, flourBinId = 36880, varbitId = 695),
    COOKS_GUILD(hopperId = 24071, leverId = 24072, flourBinId = 954, varbitId = 695),
    ARDOUGNE(hopperId = 70035, leverId = 2721, flourBinId = 1781, varbitId = 695),
    HARMONY_ISLAND(hopperId = 22422, leverId = 22424, flourBinId = 22493, varbitId = 3405),
    ZANARIS(hopperId = 70032, leverId = 52552, flourBinId = 52548, varbitId = 695),
    TAVERLEY(hopperId = 67774, leverId = 67775, flourBinId = 67770, varbitId = 10712);

    fun depositWheat(player: Player) {
        val depositedWheat = player.getI(WHEAT_DEPOSITED_KEY, 0)
        val wheatGround = player.getI(WHEAT_GROUND_KEY, 0)
        val totalWheat = depositedWheat + wheatGround
        val spaceLeft = MAX_WHEAT - totalWheat
        val wheatInInventory = player.inventory.getAmountOf(WHEAT)

        if (spaceLeft > 0) {
            if (wheatInInventory == 1) {
                player.inventory.deleteItem(WHEAT, 1)
                player.anim(832)
                player.set(WHEAT_DEPOSITED_KEY, depositedWheat + 1)
                player.sendMessage("You put the wheat into the hopper.")
            } else if (wheatInInventory > 1) {
                player.sendInputInteger("You are carrying $wheatInInventory wheat.<br>You can deposit up to $spaceLeft more wheat.<br><br>How much wheat would you like to deposit into the hopper?") { desiredAmount ->
                    val wheatToDeposit = when (desiredAmount) {
                        1 -> 1
                        -1 -> minOf(spaceLeft, wheatInInventory)
                        in 1..spaceLeft -> desiredAmount
                        else -> 0
                    }

                    if (wheatToDeposit == 0) {
                        player.simpleDialogue("Please choose a valid amount.", "You can deposit up to $spaceLeft more wheat, and you are carrying $wheatInInventory wheat.")
                    } else {
                        player.inventory.deleteItem(WHEAT, wheatToDeposit)
                        player.anim(832)
                        player.set(WHEAT_DEPOSITED_KEY, depositedWheat + wheatToDeposit)
                        player.sendMessage("You put $wheatToDeposit wheat into the hopper.")
                    }
                }
            } else {
                player.simpleDialogue("You don't have enough wheat to deposit.")
            }
        } else {
            player.sendMessage("The hopper and flour bin are full. There isn't enough room to deposit any more wheat.")
        }
    }

    fun pullLever(player: Player) {
        val depositedWheat = player.getI(WHEAT_DEPOSITED_KEY, 0)
        val wheatGround = player.getI(WHEAT_GROUND_KEY, 0)
        val totalWheat = depositedWheat + wheatGround
        val grindableAmount = (totalWheat).coerceAtMost(MAX_WHEAT) - wheatGround

        if (depositedWheat > 0) {
            if (grindableAmount > 0) {
                player.set(WHEAT_DEPOSITED_KEY, depositedWheat - grindableAmount)
                player.set(WHEAT_GROUND_KEY, wheatGround + grindableAmount)
                player.sendMessage("You hear the grinding of stones and the wheat falls below.")
                updateVarbit(player, player.getI(WHEAT_GROUND_KEY, 0))
            } else {
                player.sendMessage("The bin is full and cannot hold any more flour.")
            }
        } else {
            player.sendMessage("There is no wheat in the hopper to grind.")
        }
    }

    fun takeFlour(player: Player) {
        val wheatGround = player.getI(WHEAT_GROUND_KEY, 0)
        if (wheatGround > 0 && player.inventory.containsItem(EMPTY_POT)) {
            player.anim(832)
            player.inventory.deleteItem(EMPTY_POT, 1)
            player.inventory.addItem(FLOUR, 1)
            player.set(WHEAT_GROUND_KEY, wheatGround - 1)
            player.sendMessage("You take the ground flour.", true)
            updateVarbit(player, player.getI(WHEAT_GROUND_KEY, 0))
        } else if (wheatGround == 0) {
            player.sendMessage("There is no flour to collect.")
        } else {
            player.sendMessage("You need an empty pot to collect the flour.")
        }
    }

    fun updateVarbit(player: Player, amount: Int) {
        Windmill.entries.forEach { windmill ->
            if (windmill == TAVERLEY || windmill == HARMONY_ISLAND) {
                player.vars.setVarBit(windmill.varbitId, if (amount > 0) 1 else 0)
            } else {
                player.vars.setVar(windmill.varbitId, if (amount > 0) 1 else 0)
            }
        }
    }

    companion object {
        const val WHEAT = 1947
        const val FLOUR = 1933
        const val EMPTY_POT = 1931
        const val WHEAT_DEPOSITED_KEY = "Windmills_WheatDeposited"
        const val WHEAT_GROUND_KEY = "Windmills_WheatGround"
        const val MAX_WHEAT = 30

        fun findByHopper(hopperId: Int): Windmill? {
            return Windmill.entries.find { it.hopperId == hopperId }
        }

        fun findByLever(leverId: Int): Windmill? {
            return Windmill.entries.find { it.leverId == leverId }
        }

        fun findByFlourBin(flourBinId: Int): Windmill? {
            return Windmill.entries.find { it.flourBinId == flourBinId }
        }
    }
}

@ServerStartupEvent
fun mapWindmills() {
    Windmill.entries.forEach { windmill ->
        onObjectClick(windmill.hopperId) { (player, _) ->
            val windmill = Windmill.findByHopper(windmill.hopperId)
            if (windmill != null) {
                val depositedWheat = player.getI(Windmill.WHEAT_DEPOSITED_KEY, 0)
                player.sendMessage("There is ${if (depositedWheat == 0) "no" else "$depositedWheat"} wheat in the hopper.")
            } else {
                player.sendMessage("Nothing interesting happens.")
            }
        }
    }

    Windmill.entries.forEach { windmill ->
        onObjectClick(windmill.leverId) { (player, _) ->
            val windmill = Windmill.findByLever(windmill.leverId)
            if (windmill != null) {
                windmill.pullLever(player)
            } else {
                player.sendMessage("Nothing interesting happens.")
            }
        }
    }

    Windmill.entries.forEach { windmill ->
        onObjectClick(windmill.flourBinId) { (player, _) ->
            val windmill = Windmill.findByFlourBin(windmill.flourBinId)
            if (windmill != null) {
                windmill.takeFlour(player)
            } else {
                player.sendMessage("Nothing interesting happens.")
            }
        }
    }

    Windmill.entries.forEach { windmill ->
        onItemOnObject(arrayOf(windmill.hopperId), arrayOf(WHEAT)) { (player, _, _) ->
            val windmill = Windmill.findByHopper(windmill.hopperId)
            if (windmill != null) {
                windmill.depositWheat(player)
            } else {
                player.sendMessage("Nothing interesting happens.")
            }
        }
    }

    Windmill.entries.forEach { windmill ->
        onItemOnObject(arrayOf(windmill.flourBinId), arrayOf(EMPTY_POT)) { (player, _, _) ->
            val windmill = Windmill.findByFlourBin(windmill.flourBinId)
            if (windmill != null) {
                windmill.takeFlour(player)
            } else {
                player.sendMessage("Nothing interesting happens.")
            }
        }
    }

    onLogin { e ->
        Windmill.entries.forEach { windmill ->
            windmill.updateVarbit(e.player, e.player.getI(WHEAT_GROUND_KEY, 0))
        }
    }

}
