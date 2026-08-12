package com.rs.game.content

import com.rs.game.World.broadcastLoot
import com.rs.game.content.skills.dungeoneering.DungeonRewards.HerbicideSetting
import com.rs.game.content.skills.prayer.Burying
import com.rs.game.content.skills.prayer.Burying.Bone
import com.rs.game.content.skills.summoning.Pouch
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.item.getOwner
import com.rs.game.model.item.schedule
import com.rs.lib.Constants
import com.rs.lib.game.GroundItem
import com.rs.lib.game.Item
import com.rs.lib.game.SpotAnim
import com.rs.lib.net.packets.encoders.Sound
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcDrop
import java.util.*

@ServerStartupEvent
fun mapDrops() {
    onNpcDrop(null, null) { e ->
        if (yellDrop(e.item.id))
            broadcastLoot("${e.player.displayName} has just received a ${e.item.name} drop from ${e.npc.definitions.name}!")
        if (e.item.shouldLootbeam(e.player)) {
            e.player.packets.sendSound(Sound(307, 0, Sound.SoundType.JINGLE))
            e.enableTasks().schedule {
                e.player.packets.sendGroundItemMessage(
                    50,
                    0xFF0000,
                    e.item,
                    "<shad=000000><col=cc0033>You received: ${e.item.getAmount()} ${e.item.definitions.getName()}"
                )
                while(true) {
                    val owner = e.item.getOwner() ?: break
                    owner.packets.sendSpotAnim(SpotAnim(owner.getI("lootbeamAnim")), e.item.tile)
                    wait(2)
                }
            }
        }
    }

    onNpcDrop(null, null) { e ->
        if (e.player.nsv.getB("sendingDropsToBank")) {
            if (e.item.definitions.isNoted) e.item.id = e.item.definitions.certId
            NPC.sendDropDirectlyToBank(e.player, Item(e.item))
            e.deleteItem()
        }
    }

    onNpcDrop(null, Bone.entries.filter { bone -> !listOf(Bone.ACCURSED_ASHES, Bone.IMPIOUS_ASHES, Bone.INFERNAL_ASHES).contains(bone) }.map { it.id }.toTypedArray()) { e ->
        if (bonecrush(e.player, e.item))
            e.deleteItem()
    }

    onNpcDrop(null, HerbicideSetting.entries.map { it.herb.herbId }.toTypedArray()) { e ->
        if (herbicide(e.player, e.item))
            return@onNpcDrop e.deleteItem()
        if (e.player.familiarPouch === Pouch.MACAW && e.player.familiar.inventory.freeSlot() > 0) {
            e.player.sendMessage("Your macaw picks up the " + e.item.name.lowercase(Locale.getDefault()) + " from the ground.", true)
            e.player.familiar.inventory.add(Item(e.item.id, e.item.amount))
            e.deleteItem()
        }
    }

    onNpcDrop(null, arrayOf(12158, 12159, 12160, 12161, 12162, 12163, 12168)) { e ->
        if ((e.player.equipment.containsOneItem(25350) || e.player.inventory.containsItem(25350, 1)) && e.player.inventory.hasRoomFor(e.item)) {
            e.player.inventory.addItem(Item(e.item))
            e.deleteItem()
        }
    }

    onNpcDrop(null, arrayOf(995)) { e ->
        if (e.player.equipment.containsOneItem(25351) || e.player.inventory.containsItem(25351, 1)) {
            e.player.inventory.addCoins(e.item.amount)
            e.deleteItem()
        }
    }
}

fun herbicide(player: Player, item: Item): Boolean {
    if (!player.inventory.containsItem(19675, 1)) return false
    val setting = HerbicideSetting.forGrimy(item.id)
    if (setting == null || !player.herbicideSettings.contains(setting)) return false
    if (player.skills.getLevel(Constants.HERBLORE) >= setting.herb.level) {
        player.skills.addXp(Constants.HERBLORE, setting.herb.experience * 2)
        return true
    }
    return false
}

fun bonecrush(player: Player, item: Item): Boolean {
    if (!player.inventory.containsItem(18337, 1)) return false
    val bone = Bone.forId(item.id)
    if (bone != null && bone != Bone.ACCURSED_ASHES && bone != Bone.IMPIOUS_ASHES && bone != Bone.INFERNAL_ASHES) {
        player.skills.addXp(Constants.PRAYER, bone.experience)
        Burying.handleNecklaces(player, bone.id)
        return true
    }
    return false
}

fun GroundItem.shouldLootbeam(player: Player) =
    player.getI("lootbeamAnim", -1) != -1 && (
    definitions.getValue() * getAmount() > player.getI("lootbeamThreshold", 90000) ||
    definitions.name.contains("Scroll box") ||
    definitions.name.contains(" defender") ||
    yellDrop(id))

fun yellDrop(itemId: Int): Boolean {
    //Revenants
    if ((itemId >= 20125 && itemId <= 20174) || (itemId >= 13845 && itemId <= 13990)) return true
    //Barrows
    if (itemId >= 4708 && itemId <= 4759 && itemId != 4740) return true
    //New GWD Items
    if (itemId >= 24974 && itemId <= 25039) return true
    return when (itemId) {
        25312, 25314, 25316, 25318, 11286, 11732, 15486, 11235, 4151, 21369, 6731, 6733, 6735, 6737, 6739, 21777, 21787, 21790, 21793, 3140, 14484, 11702, 11704, 11706, 11708, 11718, 11722, 11720, 11724, 11726, 11728, 11716, 11730 -> true
        else -> false
    }
}