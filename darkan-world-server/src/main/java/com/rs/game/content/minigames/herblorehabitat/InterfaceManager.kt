package com.rs.game.content.minigames.herblorehabitat


import com.rs.game.content.skills.farming.StorableItem
import com.rs.game.model.entity.player.Player
import com.rs.lib.net.ClientPacket
import com.rs.lib.util.Logger
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick

enum class HabitatStorableItem(val itemId: Int, val componentID: Int, val varbit: Int, val maxAmount: Int = 30) {
    JUJU_HUNTER(20024, 7, 8399),
    JUJU_FARMING(20012, 9, 8400),
    SCENTLESS(20028, 11, 8401),
    SARADOMIN_BLESSING(20032, 13, 8403),
    ZAMORAK_FAVOR(20036, 15, 8404),
    GUTHIX_GIFT(20040, 17, 8402),
    CORRUPT_VINE(19979, 23, 10206),
    MARBLE_VINE(19980, 25, 10207),
    SHADOW_VINE(19977, 27, 10208),
    SARADOMIN_VINE(19981, 29, 10209),
    ZAMORAK_VINE(19983, 31, 10210),
    GUTHIX_VINE(19982, 33, 10211);

    companion object {
        fun forId(id: Int) = entries.find { it.itemId == id }
    }
}

fun Player.getHabitatStoredAmount(item: HabitatStorableItem): Int {
    return vars.getVarBit(item.varbit)
}

fun Player.setHabitatStoredAmount(item: HabitatStorableItem, amount: Int) {
    val clamped = amount.coerceIn(0, item.maxAmount)
    vars.saveVarBit(item.varbit, clamped)
}

fun Player.storeHabitatItem(item: HabitatStorableItem, amount: Int) {
    val inv = inventory
    val currentStored = getHabitatStoredAmount(item)
    val maxStoreAmount = minOf(item.maxAmount - currentStored, inv.getNumberOf(item.itemId))
    val toStore = amount.coerceAtMost(maxStoreAmount).coerceAtLeast(0)
    if (toStore > 0) {
        inv.deleteItem(item.itemId, toStore)
        setHabitatStoredAmount(item, currentStored + toStore)
    }
}

fun Player.takeHabitatItem(item: HabitatStorableItem, amount: Int) {
    val currentStored = getHabitatStoredAmount(item)
    val toTake = amount.coerceAtMost(currentStored).coerceAtMost(inventory.freeSlots)
    if (toTake > 0) {
        inventory.addItem(item.itemId, toTake)
        setHabitatStoredAmount(item, currentStored - toTake)
    }
}

fun openToolStorage(player: Player) {
    for (i in StorableItem.entries) i.updateVars(player)
    player.interfaceManager.sendInterface(125)
    player.interfaceManager.sendInventoryOverlay(126)
}

fun openPotionStorage(player: Player) {
    player.interfaceManager.sendInterface(513)
    player.interfaceManager.sendInventoryOverlay(74)
}

@ServerStartupEvent
fun mapHabitatPotionInterface() {
    onButtonClick(74) { e ->
        Logger.info(HabitatStorableItem::class.java, "mapHabitatPotionInterface", "onButtonClick(74) ${e.packet} ${e.slotId2} ${e.slotId} ${e.componentId} ${e.interfaceId}")
        val item = HabitatStorableItem.forId(e.slotId2) ?: return@onButtonClick
        when (e.packet) {
            ClientPacket.IF_OP1 -> e.player.storeHabitatItem(item, 1)
            ClientPacket.IF_OP2 -> e.player.storeHabitatItem(item, 5)
            ClientPacket.IF_OP3 -> e.player.storeHabitatItem(item, Int.MAX_VALUE)
            ClientPacket.IF_OP4 -> e.player.sendInputInteger("How many would you like to store?") {
                e.player.storeHabitatItem(item, it)
            }
            else -> Logger.info(HabitatStorableItem::class.java, "mapHabitatPotionInterface", "Unhandled packet: ${e.packet}")
        }
    }

    onButtonClick(513) { e ->
        Logger.info(HabitatStorableItem::class.java, "mapHabitatPotionInterface", "onButtonClick(93) ${e.packet} ${e.slotId2} ${e.slotId} ${e.componentId} ${e.interfaceId}")
        val item = HabitatStorableItem.forId(e.slotId2) ?: return@onButtonClick
        when (e.packet) {
            ClientPacket.IF_OP1 -> e.player.takeHabitatItem(item, 1)
            ClientPacket.IF_OP2 -> e.player.takeHabitatItem(item, 5)
            ClientPacket.IF_OP3 -> e.player.takeHabitatItem(item, Int.MAX_VALUE)
            ClientPacket.IF_OP4 -> e.player.sendInputInteger("How many would you like to take?") {
                e.player.takeHabitatItem(item, it)
            }
            else -> Logger.info(HabitatStorableItem::class.java, "mapHabitatPotionInterface", "Unhandled packet: ${e.packet}")
        }
    }
}
