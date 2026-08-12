package com.rs.game.content.world.areas.dungeons.polypore

import com.rs.game.model.entity.player.Player
import com.rs.lib.net.ClientPacket
import com.rs.lib.util.Logger
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onNpcClick

enum class PolyporeStorableItem(
    val itemId: Int,
    val singleVarbit: Int? = null,
    val lowVarbit: Int? = null,
    val highVarbit: Int? = null,
    val maxAmount: Int = 8191
) {
    FUNGAL_FLAKE(22449,10235),
    GRIFOLIC_FLAKE(22450, 10236),
    GANODERMIC_FLAKE(22451,10237),
    POLYPORE_SPORE(22448, 10234),
    NEEM_OIL(22444,null, 10238, 10239,  2000);

    companion object {
        fun forId(id: Int) = entries.find { it.itemId == id }
    }

    fun getStored(p: Player): Int {
        singleVarbit?.let { return p.vars.getVarBit(it) }
        val lo = lowVarbit?.let(p.vars::getVarBit) ?: 0
        val hi = highVarbit?.let(p.vars::getVarBit) ?: 0
        return hi * 32 + lo
    }

    fun setStored(p: Player, amount: Int) {
        val clamped = amount.coerceIn(0, maxAmount)
        singleVarbit?.let { p.vars.saveVarBit(it, clamped); return }
        val hi = clamped / 32
        val lo = clamped % 32
        lowVarbit?.let { p.vars.saveVarBit(it, lo) }
        highVarbit?.let { p.vars.saveVarBit(it, hi) }
    }
}

private fun Player.store(item: PolyporeStorableItem, req: Int) {
    if (item == PolyporeStorableItem.NEEM_OIL) {
        val jug = inventory.getItemById(1935) ?: inventory.getItemById(PolyporeStorableItem.NEEM_OIL.itemId)
        if (jug == null) {
            sendMessage("You need a neem oil jug to store your neem oil.")
            return
        }
        val haveCharges = jug.getMetaDataI("neemCharges", 0)
        val stored = item.getStored(this)
        val canStore = (item.maxAmount - stored).coerceAtMost(haveCharges)
        val toStore = req.coerceAtMost(canStore).coerceAtLeast(0)
        if (toStore > 0) {
            jug.addMetaData("neemCharges", haveCharges - toStore)
            if (jug.getMetaDataI("neemCharges", 0) == 0) {
                jug.id = 1935
                inventory.refresh(jug.id)
            }
            item.setStored(this, stored + toStore)
            sendMessage("You store $toStore doses of neem oil.")
        } else {
            sendMessage("You have no neem oil in your jug to store.")
        }
    } else {
        val have = inventory.getNumberOf(item.itemId)
        val stored = item.getStored(this)
        val canStore = (item.maxAmount - stored).coerceAtMost(have)
        val toStore = req.coerceAtMost(canStore).coerceAtLeast(0)
        if (toStore > 0) {
            inventory.deleteItem(item.itemId, toStore)
            item.setStored(this, stored + toStore)
            sendMessage("You store $toStore ${item.name.lowercase().replace('_', ' ')}s.")
        } else {
            sendMessage("You have no ${item.name.lowercase().replace('_', ' ')}s stored, or no free space..")
        }
    }
}

private fun openStorage(player: Player) {
    player.interfaceManager.sendInterface(893)
    player.interfaceManager.sendInventoryOverlay(894)
}

private fun Player.take(item: PolyporeStorableItem, req: Int) {
    if (item == PolyporeStorableItem.NEEM_OIL) {
        val stored = item.getStored(this)
        if (stored <= 0) {
            sendMessage("You have no neem oil stored.")
            return
        }
        val slot = inventory.getItemById(item.itemId)
        val jug = inventory.items[slot.slot]
        val haveCharges = jug.getMetaDataI("neemCharges", 0)
        jug.addMetaData("neemCharges", haveCharges + 1)
        item.setStored(this, stored - 1)
        inventory.refresh(jug.id)
        sendMessage("You withdraw 1 dose of neem oil.")
    } else {
        val stored = item.getStored(this)
        val toTake = req.coerceAtMost(stored).coerceAtMost(inventory.freeSlots)
        if (toTake > 0) {
            inventory.addItem(item.itemId, toTake)
            item.setStored(this, stored - toTake)
            sendMessage("You withdraw $toTake ${item.name.lowercase().replace('_', ' ')}s.")
        } else {
            sendMessage("You have ${item.name.lowercase().replace('_', ' ')}s stored, or no free space.")
        }
    }
}

@ServerStartupEvent
fun mapPolyporeStorageInterface() {
    onNpcClick(14620, options = arrayOf("Exchange")) { e ->
        openStorage(e.player)
    }

    onButtonClick(894) { e ->
        val item = PolyporeStorableItem.forId(e.slotId2) ?: return@onButtonClick
        when (e.packet) {
            ClientPacket.IF_OP1 -> e.player.store(item, 1)
            ClientPacket.IF_OP2 -> e.player.store(item, 5)
            ClientPacket.IF_OP3 -> e.player.store(item, Int.MAX_VALUE)
            ClientPacket.IF_OP4 -> e.player.sendInputInteger("How many would you like to store?") {
                e.player.store(item, it)
            }
            else -> Logger.info(PolyporeStorableItem::class.java, "mapPolyporeStorageInterface", "Unhandled packet: ${e.packet}")
        }
    }

    onButtonClick(893) { e ->
        val item = PolyporeStorableItem.forId(e.slotId2) ?: return@onButtonClick
        when (e.packet) {
            ClientPacket.IF_OP1 -> e.player.take(item, 1)
            ClientPacket.IF_OP2 -> e.player.take(item, 5)
            ClientPacket.IF_OP3 -> e.player.take(item, Int.MAX_VALUE)
            ClientPacket.IF_OP4 -> e.player.sendInputInteger("How many would you like to take?") {
                e.player.take(item, it)
            }
            else -> Logger.info(PolyporeStorableItem::class.java, "mapPolyporeStorageInterface", "Unhandled packet: ${e.packet}")
        }
    }
}
