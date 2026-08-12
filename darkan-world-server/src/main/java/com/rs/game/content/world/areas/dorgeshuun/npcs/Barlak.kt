package com.rs.game.content.world.areas.dorgeshuun.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

data class BarlakItem(
    val id: Int,
    val pricePaid: Int,
    val xp: Double?,
    val skill: Int?
)

val barlakItems = listOf(
    BarlakItem(10976, 1000, 1500.0, Constants.CONSTRUCTION),  // Long bone
    BarlakItem(10977, 2000, 2250.0, Constants.CONSTRUCTION),  // Curved bone
    /*BarlakItem(10978, 600, null, null),                     // Snail shell
    BarlakItem(10979, 600, 500, Constants.CRAFTING),          // Perfect snail shell
    BarlakItem(10980, 600, null, null),                       // Tortoise shell
    BarlakItem(10981, 600, 500, Constants.CRAFTING)*/         // Perfect shell
)

class Barlak(val player: Player, val npc: NPC) {
    fun hasNoBones() {
        player.startConversation {
            npc(npc, CALM_TALK, "Bones!")
            player(CALM_TALK, "What?")
            npc(npc, CALM_TALK, "I don't have any bones!")
            label("initialOps")
            options {
                op("Then how do you stand up?") {
                    player(CALM_TALK, "Then how do you stand up?")
                    npc(npc, CALM_TALK, "What?")
                    player(CALM_TALK, "How do you stand up if you have no bones? Shouldn't you collapse into a gelatinous blob?")
                    npc(npc, CALM_TALK, "Ha, ha, ha, ha, ha!")
                    goto("initialOps")
                }
                op("What do you need bones for?") {
                    player(CALM_TALK, "What do you need bones for?")
                    npc(npc, CALM_TALK, "To stand up properly. Otherwise I'd collapse into a gelatinous blob! Ha, ha, ha, ha, ha! No, seriously, I need bones to use as a Construction material. We always need big bones to prop up the mine shafts and to make other temporary structures.")
                    goto("initialOps")
                }
                op("Will you buy anything besides bones?") {
                    player(CALM_TALK, "Will you buy anything besides bones?")
                    npc(npc, CALM_TALK, "Well, I've had a few people bring me some interesting giant shells. They say they came from giant snails and giant tortoises. Of course, like the bones, some shells are better than others.")
                    npc(npc, CALM_TALK, "I'll give you 250gp for ordinary shells, but what I really need are perfect shells. I'll pay double for them and give you a few tips about Crafting too.")
                    goto("initialOps")
                }
                op("What kind of bones do you need?") {
                    player(CALM_TALK, "What kind of bones do you need?")
                    npc(npc, CALM_TALK, "Enormous bones, as big as you can get. Not just any big bones will do. Sometimes you might find a particularly long, straight bone. That's the kind of thing I need; I'll give you 1,000gp for one of them.")
                    npc(npc, CALM_TALK, "Occasionally, you might find a long curved bone - these are especially valuable - I'll give you 2,000gp for them. I'll also teach you a bit about how we use the bones in Construction, if you like.")
                    goto("initialOps")
                }
                op("Goodbye.") {
                    player(CALM_TALK, "Goodbye.")
                    npc(npc, CALM_TALK, "Goodbye.")
                }
            }

        }
    }

    fun hasBones() {
        val barlakItemIds = barlakItems.map { it.id }
        val inventoryItems = barlakItemIds.mapNotNull { itemId ->
            val quantity = player.inventory.getAmountOf(itemId)
            barlakItems.find { it.id == itemId }?.takeIf { quantity > 0 }?.let { barlakItem ->
                BarlakItem(itemId, barlakItem.pricePaid * quantity, barlakItem.xp?.times(quantity), barlakItem.skill)
            }
        }
        val totalQuantity = inventoryItems.sumOf { player.inventory.getAmountOf(it.id) }
        val totalPrice = inventoryItems.sumOf { it.pricePaid }

        player.startConversation {
            npc(npc, CALM_TALK, "Those bones! Those are exactly the sort of thing I need! Will you sell them?")
            npc(npc, CALM_TALK, "I'll give you ${totalPrice}gp for the bone${if (totalQuantity > 1) "s" else ""} you're carrying. I'll try to teach you something about Construction as well, but it's highly technical so you won't understand if you don't already have level 30 Construction.")
            label("initialOPs")
            options {
                op("Okay.") {
                    player(CALM_TALK, "Okay.")
                    npc(npc, CALM_TALK, "Thanks! Now let me explain...") {
                        inventoryItems.forEach { item ->
                            player.inventory.deleteItem(item.id, totalQuantity)
                            if (player.skills.getLevelForXp(Constants.CONSTRUCTION) >= 30) {
                                item.xp?.let { player.skills.addXp(Constants.CONSTRUCTION, it) }
                            } else {
                                player.sendMessage("You need a Construction level of at least 30 to understand what Barlak told you about Construction.")
                            }
                        }
                        player.inventory.addCoins(totalPrice)
                    }
                    if (player.skills.getLevelForXp(Constants.CONSTRUCTION) >= 30) {
                        simple("Barlak gives you a short lecture and you learn more about Construction.")
                    }
                }
                op("No, I'll keep the bones.") {
                    player(CALM_TALK, "No thanks.")
                }
            }
        }
    }
}

@ServerStartupEvent
fun mapBarlak() {
    val barlakItemIds = barlakItems.map { it.id }
    onNpcClick(5828) { (player, npc) ->
        if (player.inventory.containsOneItem(*barlakItemIds.toIntArray())) {
            Barlak(player, npc).hasBones()
        } else {
            Barlak(player, npc).hasNoBones()
        }
    }
}
