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
package com.rs.game.content.world.areas.tree_gnome_stronghold

import com.rs.engine.dialogue.startConversation
import com.rs.game.content.quests.dig_site.utils.FRUIT_BLAST
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onItemClick

private const val ORANGE_ID = 2108
private const val PINEAPPLE_ID = 2114
private const val LEMON_ID = 2102
private const val VODKA_ID = 2015
private const val LIME_ID = 2120
private const val GIN_ID = 2019
private const val BRANDY_ID = 2021
private const val DWELLBERRIES_ID = 2126
private const val WHISKEY_ID = 2017
private const val EQUA_LEAVES_ID = 2128
private const val BUCKET_OF_MILK_ID = 1927
private const val CHOCOLATE_BAR_ID = 1973
private const val COCKTAIL_MIXER_ID = 2025
private const val COCKTAIL_GLASS_ID = 2026
private const val LEMON_SLICES_ID = 2106
private const val COCKTAIL_INTERFACE_ID = 436 //Gnome Cocktails

data class Ingredient(val id: Int, val amount: Int)
data class Cocktail(val name: String, val id: Int, val req: Int, val productid: Int, val ingredients: List<Ingredient>)

private val cocktails = listOf(

    Cocktail("Mixed blast", 9568, 6, 2084,listOf(Ingredient(ORANGE_ID, 1), Ingredient(PINEAPPLE_ID,1), Ingredient(LEMON_ID,1))),
    Cocktail("Mixed blizzard", 9566, 18, 2054,listOf(Ingredient(VODKA_ID,2), Ingredient(GIN_ID,1), Ingredient(ORANGE_ID,1), Ingredient(LIME_ID,1), Ingredient(LEMON_ID,1))),
    Cocktail("Mixed blurberry special", 9570, 37, 2064, listOf(Ingredient(VODKA_ID,1), Ingredient(GIN_ID,1), Ingredient(BRANDY_ID,1), Ingredient(LEMON_ID,2), Ingredient(ORANGE_ID,1))),
    Cocktail("Mixed dragon", 9574, 32, 2092,listOf(Ingredient(VODKA_ID,1), Ingredient(GIN_ID,1), Ingredient(DWELLBERRIES_ID,1))),
    Cocktail("Mixed punch", 9569, 8, 2048,listOf(Ingredient(ORANGE_ID,1), Ingredient(PINEAPPLE_ID,2), Ingredient(LEMON_ID,1))),
    Cocktail("Mixed saturday", 9571, 33, 2074, listOf(Ingredient(WHISKEY_ID,1), Ingredient(EQUA_LEAVES_ID,1), Ingredient(BUCKET_OF_MILK_ID,1), Ingredient(CHOCOLATE_BAR_ID,1))),
    Cocktail("Mixed sgg", 9567, 20, 2080, listOf(Ingredient(VODKA_ID,1), Ingredient(LIME_ID,3))),
)

fun getMixableCocktails(player: Player, cocktails: List<Cocktail>): List<Cocktail> {
    return cocktails.filter { cocktail ->
        cocktail.ingredients.all { ingredient ->
            player.inventory.containsItem(Item(ingredient.id, ingredient.amount))
        } && player.skills.getLevel(Skills.COOKING) >= cocktail.req
    }
}

fun justMix(player: Player, canMix: List<Cocktail>, id: Int ) {
    val drink = canMix.firstOrNull { it.id == id } ?: return
    player.closeInterfaces()
    player.startConversation() {
        simple("You create a ${drink.name}.") {
            for (ingredient in drink.ingredients) {
                player.inventory.deleteItem(ingredient.id, 1)
            }
            player.inventory.addItem(drink.id, 1)
            player.inventory.deleteItem(Item(COCKTAIL_MIXER_ID, 1))
        }
    }
}

@ServerStartupEvent
fun cocktailMixingListener() {
    onItemClick(COCKTAIL_MIXER_ID, options = arrayOf("Mix-cocktail")) { (player) ->
        player.interfaceManager.sendInterface(COCKTAIL_INTERFACE_ID)
        player.packets.setIFItem(1, 1, 328,1)
        val canMix = getMixableCocktails(player, cocktails)
        val componentToName = mapOf(
            3 to "Mixed blizzard",
            16 to "Mixed sgg",
            23 to "Mixed blast",
            32 to "Mixed punch",
            41 to "Mixed dragon",
            50 to "Mixed saturday",
            61 to "Mixed blurberry special"
        )
        val components = arrayOf(3, 16, 23,  32, 41, 50, 61)
        components.forEach { componentId ->
            val name = componentToName[componentId]
            val match = canMix.firstOrNull { it.name.equals(name, ignoreCase = true) }
            val itemId = match?.productid ?: 9489
            player.packets.setIFItem(436, componentId, itemId, 1)
        }
    }

    onButtonClick(COCKTAIL_INTERFACE_ID) { e ->
        val canMix = getMixableCocktails(e.player, cocktails)
        when (e.componentId) {
            3 -> justMix(e.player, canMix, 9566) //blizzard
            16 -> justMix(e.player, canMix, 9567) //sgg
            23 -> justMix(e.player, canMix, 9568) //fruit blast
            32 -> justMix(e.player, canMix, 9569) //Pineapple Punch
            41 -> justMix(e.player, canMix, 9574) //Dragon
            50 -> justMix(e.player, canMix, 9571) //Saturday
            61 -> justMix(e.player, canMix, 9571) //Blur Special
        }
    }

    cocktails.forEach { cocktail ->
        onItemClick(cocktail.id, options = arrayOf("Pour")) { e ->
            if (e.player.inventory.containsItem(Item(COCKTAIL_GLASS_ID, 1))) {
                if(cocktail.id == 9568 && !e.player.inventory.containsItem(LEMON_SLICES_ID, 1)){
                    e.player.startConversation {
                        simple("You need a lemon slice to properly garnish the Fruit blast.")
                    }
                    return@onItemClick
                }
                else {
                    e.player.inventory.deleteItem(LEMON_SLICES_ID, 1)
                }
                e.player.startConversation {
                    e.player.sendMessage("You pour a " + Item(cocktail.productid).name + ".")
                    exec {
                        e.player.inventory.deleteItem(cocktail.id, 1)
                        e.player.inventory.deleteItem(COCKTAIL_GLASS_ID, 1)
                        e.player.inventory.addItem(COCKTAIL_MIXER_ID)
                        e.player.inventory.addItem(cocktail.productid)
                    }
                }
            } else {
                e.player.startConversation {
                    simple("You need a cocktail glass to pour the "+Item(cocktail.productid).name+".")
                }
            }
        }
    }
}



