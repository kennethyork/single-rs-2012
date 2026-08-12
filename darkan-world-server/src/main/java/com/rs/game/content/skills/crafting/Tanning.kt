package com.rs.game.content.skills.crafting

import com.rs.cache.loaders.ItemDefinitions
import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.engine.dialogue.statements.MakeXStatement
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick

@PluginEventHandler
object Tanning {

    enum class Leather(
        val raw: Int,
        val tanned: Int,
        val defaultPrice: Int?,
        val canifisPrice: Int?,
        val neitiznotPrice: Int?,
        val rangingGuildPrice: Int?
    ) {
        SOFT(1739, 1741, 0, 2, null, 1),
        HARD(1739, 1743, 3, 5, null, 3),
        SNAKE(6287, 6289, 15, 25, null, 15),
        GREEN(1753, 1745, 20, 45, null, 20),
        BLUE(1751, 2505, 20, 45, null, 20),
        RED(1749, 2507, 20, 45, null, 20),
        BLACK(1747, 2509, 20, 45, null, 20),
        ROYAL(24372, 24374, 20, 45, null, 20),
        YAK(10818, 10820, null, null, 5, null)
    }
}

fun tanningD(player: Player, npc: NPC) {
    val craftable = Tanning.Leather.entries.filter { leather ->
        player.inventory.containsItem(leather.raw) &&
                when (npc.id) {
                    14877, 2824, 804, 2320 -> leather.defaultPrice != null
                    1041 -> leather.canifisPrice != null
                    5506 -> leather.neitiznotPrice != null
                    680 -> leather.rangingGuildPrice != null
                    else -> leather.defaultPrice != null
                }
    }

    if (craftable.isEmpty()) {
        player.startConversation {
            npc(npc.id, CHEERFUL, "You don't have any leather that I can tan. Bring me some raw leather and then I can be more help to you.")
        }
        return
    }

    player.startConversation {
        if (craftable.isEmpty()) {
            npc(npc, CHEERFUL, "You don't have any leather that I can tan. Bring me some raw leather and then I can be more help to you.")
            return@startConversation
        }
        val itemIds = craftable.map { it.tanned }.toIntArray()
        val maxAmount = craftable.maxOf { player.inventory.getAmountOf(it.raw) }
        makeX(MakeXStatement.MakeXType.MAKE, "How many hides would you like to tan?", itemIds, maxAmount)
        craftable.forEach { leather ->
            appendToCurrent {
                val quantity = minOf(
                    MakeXStatement.getQuantity(player),
                    player.inventory.getAmountOf(leather.raw)
                )
                val price = when (npc.id) {
                    14877, 2824, 804, 2320 -> leather.defaultPrice
                    1041 -> leather.canifisPrice
                    5506 -> leather.neitiznotPrice
                    680 -> leather.rangingGuildPrice
                    else -> leather.defaultPrice
                }
                if (price == null) return@appendToCurrent

                repeat(quantity) {
                    if (!player.inventory.hasCoins(price)) {
                        player.sendMessage("You have run out of gold.")
                        return@repeat
                    }
                    if (!player.inventory.containsItem(leather.raw)) {
                        player.sendMessage("You have run out of raw leather.")
                        return@repeat
                    }
                    player.inventory.removeCoins(price)
                    player.inventory.deleteItem(leather.raw, 1)
                    player.inventory.addItem(leather.tanned)
                    player.sendMessage("${npc.name} turns your ${ItemDefinitions.getDefs(leather.raw).name} into ${ItemDefinitions.getDefs(leather.tanned).name} for $price coins.")
                }
            }
        }
    }
}

fun Tanning.Leather.isValidForNpc(npcId: Int): Boolean {
    return when (npcId) {
        1041 -> canifisPrice != null
        5506 -> neitiznotPrice != null
        14877, 2824, 804, 2320 -> defaultPrice != null
        680 -> rangingGuildPrice != null
        else -> false
    }
}

fun defaultTannerDialogue(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, CALM_TALK, "Hello stranger. Would you like me to tan any hides for you?")
        options {
            op("Yes please.") {
                tanningD(player, npc)
            }
            op("No thanks, I'm not interested.") {
                player(CALM_TALK, "No thanks, I'm not interested.")
                npc(npc, HAPPY_TALKING, "Okay; you change your mind, you come see me. I'm your guy!")
            }
        }
    }
}

@ServerStartupEvent
fun mapDefaultTanner() {
    onNpcClick(804, 2320, 680, 1041, options = arrayOf("Talk-to")) { (player, npc) ->
        defaultTannerDialogue(player, npc)
    }

    onNpcClick(804, 2320, 680, 1041, options = arrayOf("Trade")) { (player, npc) ->
        tanningD(player, npc)
    }
    onNpcClick(680, 804, 1041, 14877, 2320, 2824, 5506, options = arrayOf("Tan hides", "Tan-hide", "Craft-goods")) { e ->
        tanningD(e.player, e.npc)
    }
    onItemOnNpc(680, 804, 1041, 14877, 2320, 2824, 5506) { e ->
        val leather = Tanning.Leather.entries.find { it.raw == e.item.id && it.isValidForNpc(e.npc.id) }
        if (leather != null) {
            tanningD(e.player, e.npc)
        } else {
            e.player.startConversation {
                npc(e.npc.id, CONFUSED, "Er... Thanks, but no thanks!")
            }
        }
    }
}



