package com.rs.game.content.bosses.godwars.factions

import com.rs.game.model.entity.player.Player
import java.util.*

enum class GodFaction(val kcVarbit: Int, private val itemIdentifiers: Set<ItemIdentifier>) {
    SARADOMIN(3938, setOf(NameContains("saradomin", "holy symbol", "holy book", "monk", "citharede"), ExactId(3839))),
    ARMADYL(3939, setOf(NameContains("armadyl", "book of law"), ExactId(19614))),
    ZAROS(8725, setOf(NameContains("pernix", "torva", "virtus", "zaryte"))),
    BANDOS(3941, setOf(NameContains("bandos", "book of war", "ancient mace", "granite mace"), ExactId(19612))),
    ZAMORAK(3942, setOf(NameContains("zamorak", "unholy"), ExactId(3841)));

    fun isWearing(player: Player): Boolean {
        if (this != ZAROS && ZAROS.isWearing(player)) return true

        return player.equipment.itemsCopy.filterNotNull()
            .any { item ->
                val name = item.definitions.getName().lowercase(Locale.getDefault())
                itemIdentifiers.any { it.matches(name, item.id) }
            }
    }
}

private sealed interface ItemIdentifier {
    fun matches(itemName: String, itemId: Int): Boolean
}

private class NameContains(vararg val patterns: String) : ItemIdentifier {
    override fun matches(itemName: String, itemId: Int) = patterns.any { itemName.contains(it) }
}

private class ExactId(val id: Int) : ItemIdentifier {
    override fun matches(itemName: String, itemId: Int) = itemId == id
}