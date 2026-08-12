package com.rs.game.content.world.areas.draynor.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.engine.dialogue.statements.MakeXStatement
import com.rs.engine.quest.Quest
import com.rs.game.content.items.Dye
import com.rs.game.content.quests.princealirescue.PrinceAliRescue
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mapAggie() {
    onNpcClick(922, options = arrayOf("Talk-to")) { (player, npc) ->
        handleAggie(player, npc)
    }
    onNpcClick(922, options = arrayOf("Make-dyes")) { (player, npc) ->
        makeDyes(player, npc)
    }
}

private fun makeDyes(player: Player, npc: NPC) {
    val availableDyes = Dye.entries.filter { dye ->
        dye.inputID != null && dye.inputQuantity != null &&
                player.inventory.containsItem(dye.inputID, dye.inputQuantity)
    }
    if (availableDyes.isEmpty()) {
        player.startConversation {
            npc(npc, SHAKING_HEAD, "Sorry, you don't have enough ingredients to make any dyes right now.")
        }
        return
    }
    val dyeItemIds = availableDyes.map { it.id }.toIntArray()
    val maxAmount = availableDyes.maxOf {
        val fromItems = player.inventory.getAmountOf(it.inputID!!) / it.inputQuantity!!
        val fromCoins = player.inventory.coinsAsInt / 5
        minOf(fromItems, fromCoins)
    }
    player.startConversation {
        npc(npc, HAPPY_TALKING, "What sort of dye would you like? Red, yellow or blue?")
        makeX(MakeXStatement.MakeXType.MAKE, "How many dyes would you like to make?", dyeItemIds, maxAmount)

        availableDyes.forEach { dye ->
            appendToCurrent {
                val quantity = MakeXStatement.getQuantity(player)
                val maxFromItem = player.inventory.getAmountOf(dye.inputID!!) / dye.inputQuantity!!
                val maxFromCoins = player.inventory.coinsAsInt / 5
                val maxMake = minOf(maxFromItem, maxFromCoins)

                if (quantity <= 0 || maxMake <= 0) {
                    player.sendMessage("You need at least ${dye.inputQuantity} ${Item(dye.inputID).name.lowercase()} and 5 coins per dye.")
                    return@appendToCurrent
                }

                val toMake = minOf(quantity, maxMake)
                var made = 0
                repeat(toMake) {
                    val hasItems = player.inventory.getAmountOf(dye.inputID) >= dye.inputQuantity
                    val hasCoins = player.inventory.coinsAsInt >= 5
                    if (hasItems && hasCoins) {
                        player.inventory.deleteItem(dye.inputID, dye.inputQuantity)
                        player.inventory.removeCoins(5)
                        player.inventory.addItem(dye.id)
                        made++
                    } else {
                        return@repeat
                    }
                }
                val dyeName = dye.name.lowercase().replaceFirstChar { it.uppercase() }
                player.sendMessage("Aggie mixes the ingredients and creates $made $dyeName dye.")
            }
        }
    }
}

private fun handleAggie(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, CALM, "What can I help you with?")
        options("Select an Option") {
            if (player.questManager.getStage(Quest.PRINCE_ALI_RESCUE) == PrinceAliRescue.GEAR_CHECK) {
                op("Could you think of a way to make skin paste?") {
                    player(HAPPY_TALKING, "Could you think of a way to make skin paste?")
                    exec {
                        val inv = player.inventory
                        if (
                            inv.containsItem(PrinceAliRescue.ASHES, 1) &&
                            inv.containsItem(PrinceAliRescue.REDBERRY, 1) &&
                            inv.containsItem(PrinceAliRescue.POT_OF_FLOUR, 1) &&
                            inv.containsItem(PrinceAliRescue.WATER_BUCKET, 1)
                        ) {
                            npc(PrinceAliRescue.AGGIE, HAPPY_TALKING, "Yes I can, I see you already have the ingredients. Would you like me to mix some for you now?")
                            options("Choose an option:") {
                                op("Yes please. Mix me some skin paste.") {
                                    player(HAPPY_TALKING, "Yes please. Mix me some skin paste.")
                                    npc(PrinceAliRescue.AGGIE, HAPPY_TALKING, "That should be simple. Hand the things to Aggie then.")
                                    simple("You hand the ash, flour, water and redberries to Aggie. Aggie tips the ingredients into a cauldron and mutters some words.")
                                    npc(PrinceAliRescue.AGGIE, CALM_TALK, "Tourniquet, Fenderbaum, Tottenham, Marshmallow, MarbleArch.")
                                    simple("Aggie hands you the Skin Paste.") {
                                        inv.deleteItem(PrinceAliRescue.ASHES, 1)
                                        inv.deleteItem(PrinceAliRescue.REDBERRY, 1)
                                        inv.deleteItem(PrinceAliRescue.POT_OF_FLOUR, 1)
                                        inv.deleteItem(PrinceAliRescue.WATER_BUCKET, 1)
                                        inv.addItem(PrinceAliRescue.PASTE, 1)
                                    }
                                    npc(PrinceAliRescue.AGGIE, HAPPY_TALKING, "There you go dearie, your skin potion. That will make you look good at the Varrock dances.")
                                    player(SKEPTICAL_THINKING, "Umm, thanks.")
                                }
                                op("No thank you, I don't need any skin paste right now.") {
                                    npc(PrinceAliRescue.AGGIE, HAPPY_TALKING, "Okay dearie, that's always your choice.")
                                }
                            }
                        } else {
                            npc(PrinceAliRescue.AGGIE, HAPPY_TALKING, "For skin paste I am going to need ashes, a pot of flour, redberries and a bucket of water.")
                            player(HAPPY_TALKING, "Okay, thanks. I'll be back with those four things.")
                        }
                    }
                }
            } else {
                op("Hey, you are a witch aren't you?") {
                    player(SKEPTICAL_THINKING, "Hey, you are a witch aren't you?")
                    npc(npc, AMAZED, "My, you are observant!")
                    player(CALM_TALK, "Cool, do you turn people into frogs?")
                    npc(npc, NO_EXPRESSION, "Oh, not for years. But if you meet a talking chicken, you have probably met the professor in the manor north of here. A few years ago it was a flying fish. That machine is a menace.")
                }
            }
            op("So what is actually in that cauldron?") {
                player(CALM_TALK, "So what is actually in that cauldron?")
                npc(npc, FRUSTRATED, "You don't really expect me to give away trade secrets, do you?")
            }
            op("What's new in Draynor village?") {
                player(CALM_TALK, "What's new in Draynor village?")
                npc(npc, HAPPY_TALKING, "Hm, a while ago there was a portal that appeared in the woods to the east of Draynor")
                npc(npc, HAPPY_TALKING, "When the portal opened, the god Zamorak stepped through!")
                npc(npc, HAPPY_TALKING, "However, immediately he was confronted by Saradomin and a battle ensued. Both forces fought valiantly for months, unable to gain the upper hand")
                npc(npc, HAPPY_TALKING, "In the end, however, the power of Zamorak was no match for the might of Saradomin. Zamorak was defeated")
                npc(npc, HAPPY_TALKING, "Zamorak retreated with the help of his general. But the victory had cost Saradomin dearly, and so he left the battlefield to regroup")
                npc(npc, HAPPY_TALKING, "The battlefield is at peace now")
                player(HAPPY_TALKING, "Ok, thanks!")
            }
            op("What could you make for me?") {
                player(SKEPTICAL_THINKING, "What could you make for me?")
                npc(npc, HAPPY_TALKING, "I mostly just make what I find pretty. I sometimes make dye for the women's clothes to brighten the place up. I can make red, yellow and blue dyes. If you'd like some, just bring me the appropriate ingredients.")
            }
            op("Can you make dyes for me please?") {
                player(CALM_TALK, "Can you make dyes for me please?")
                exec {
                    makeDyes(player, npc)
                }
            }
        }
    }
}