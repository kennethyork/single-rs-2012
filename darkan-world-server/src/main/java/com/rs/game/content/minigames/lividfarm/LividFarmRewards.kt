package com.rs.game.content.minigames.lividfarm

import com.rs.game.World.addGroundItem
import com.rs.game.content.Effect
import com.rs.game.content.minigames.lividfarm.LividFarmRewards.tryUnlock
import com.rs.game.content.skills.farming.ProduceType
import com.rs.game.content.skills.herblore.CraftablePotion
import com.rs.game.content.skills.herblore.HerbCleaning.Herbs
import com.rs.game.content.skills.magic.Rune
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.utils.Ticks


private const val REWARD_INTERFACE = 1083

object LividFarmRewards {
    //Cost is stored as /10
    enum class LividSpell(val spellComponent: Int, val learnComponent: Int, val requiredTier: Int, val cost: Int) {
        TELEPORT_SOUTH_FALADOR(387, 263, 0, 6984),
        REPAIR_RUNE_POUCH(74, 272, 1, 13976),
        TELEPORT_NORTH_ARDOUGNE(69, 281, 2, 22712),
        REMOTE_FARMING(64, 290, 3, 31448),
        SPIRITUAL_HEALING(59, 299, 4, 40184),
        MAKE_LEATHER(54, 308, 5, 48920),
        DISRUPTION_SHIELD(49, 317, 6, 57656),
        VENGEANCE_GROUP(44, 326, 7, 66392),
        TELEPORT_TROLLHEIM(433, 467, 8, 74440),
        GROUP_TELEPORT_TROLLHEIM(433, 477, 9, 80640),
        BORROWED_POWER(455, 487, 10, 85000);

        companion object {
            fun fromLearnComponent(id: Int): LividSpell? = entries.firstOrNull { it.learnComponent == id }
        }
    }

    //Cost is stored as /10
    enum class LividWish( val wishComponent: Int, val cost: Int) {
        VIAL_MY_HERBS( 353, 550),
        TURN_LUNAR_LUMBER_INTO_RUNES(362, 550),
        GIVE_ME_AN_ARCANE_CAPACITOR(545, 1800),
        ID_LIKE_A_NEW_PATCH(535, 1800),
        MORE_PLANKS_PLEASE(380, 1800),
        GIMME_HERBS(344, 1800),
        LET_IT_RAIN_SEEDS(335, 1800),
        REDUCE_THE_FISH_I_BURN(371, 1800),
        PROTECT_A_PATCH_FOR_ME(565, 3700),
        LET_IT_RAIN_AWESOME_SEEDS(525, 5500);

        companion object {
            fun fromWishComponent(id: Int): LividWish? = entries.firstOrNull { it.wishComponent == id }
        }
    }


    @JvmStatic
    fun checkUnlocked(player: Player, spell: LividSpell) = player.lividClaims > spell.requiredTier

    fun tryUnlock(player: Player, spell: LividSpell): Boolean {
        val current = player.lividClaims

        if (current != spell.requiredTier) {
            player.sendMessage("You must unlock each spell in order.")
            return false
        }

        val requiredPoints = spell.cost

        val readableName = spell.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

        if (player.producePoints < requiredPoints) {
            player.sendMessage("You do not have enough points to unlock $readableName ${player.producePoints * 10}/${requiredPoints * 10}")
            return false
        }
        player.sendMessage("You have unlocked the $readableName spell!")
        player.lividClaims += 1
        if(spell == LividSpell.BORROWED_POWER)
            player.producePoints = 0

        player.schedule {
            wait(Ticks.fromSeconds(1))
            player.interfaceManager.sendInterface(REWARD_INTERFACE)
            player.packets.sendRunScript(4266)
            player.packets.sendRunScript(4267)
        }

        player.vars.syncVarsToClient()
        return true
    }
}

fun handleWish(player: Player, wish: LividFarmRewards.LividWish) {
    if (wish == LividFarmRewards.LividWish.ID_LIKE_A_NEW_PATCH) {
        player.sendMessage("Why on earth would you ever even try to claim this reward?..")
        return
    }
    if (player.producePoints < wish.cost) {
        player.sendMessage("You need ${wish.cost * 10} produce points to activate this wish.")
        return
    }
    player.producePoints -= wish.cost
    val wishName = wish.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    player.sendMessage("You have activated the $wishName wish!")
    player.interfaceManager.removeInterface(REWARD_INTERFACE)

    when (wish) {
        LividFarmRewards.LividWish.LET_IT_RAIN_SEEDS -> letItRainSeeds(player)
        LividFarmRewards.LividWish.TURN_LUNAR_LUMBER_INTO_RUNES -> turnLunarLumberIntoRunes(player)
        LividFarmRewards.LividWish.VIAL_MY_HERBS -> vialMyHerbs(player)
        LividFarmRewards.LividWish.GIMME_HERBS -> gimmeHerbs(player)
        LividFarmRewards.LividWish.GIVE_ME_AN_ARCANE_CAPACITOR -> giveMeAnArcaneCapacitor(player)
        LividFarmRewards.LividWish.ID_LIKE_A_NEW_PATCH -> iWantANewPatch(player)
        LividFarmRewards.LividWish.MORE_PLANKS_PLEASE -> morePlanksPlease(player)
        LividFarmRewards.LividWish.REDUCE_THE_FISH_I_BURN -> reduceTheFishIBurn(player)
        LividFarmRewards.LividWish.PROTECT_A_PATCH_FOR_ME -> protectAPatchForMe(player)
        LividFarmRewards.LividWish.LET_IT_RAIN_AWESOME_SEEDS -> letItRainAwesomeSeeds(player)
    }
    player.vars.syncVarsToClient()
}


@ServerStartupEvent
fun mapLunarRewards() {
    onButtonClick(REWARD_INTERFACE) { e ->
        val player = e.player
        val compId = e.componentId
        if (player.lividClaims < 11) {
            val spell = LividFarmRewards.LividSpell.fromLearnComponent(compId) ?: return@onButtonClick
            tryUnlock(player, spell)
        } else {
            val wish = LividFarmRewards.LividWish.fromWishComponent(compId) ?: return@onButtonClick
            handleWish(player, wish)
        }
    }
}


fun letItRainSeeds(player: Player) {
    val seeds = listOf(
        ProduceType.Watermelon.seedId,
        ProduceType.Cactus.seedId,
        ProduceType.Poison_ivy.seedId,
        ProduceType.Strawberry.seedId,
        ProduceType.Curry.seedId,
        ProduceType.Orange.seedId
    )
    val baseTile = Tile.of(player.tile)
    val tilesAround = (-2..2).flatMap { dx ->
        (-2..2).map { dy -> baseTile.transform(dx, dy) }
    }
    for (seedId in seeds) {
        val amount = (7..10).random()
        val item = Item(seedId, amount)
        val dropTile = tilesAround.random()
        addGroundItem(item, dropTile, player)
    }
}

fun turnLunarLumberIntoRunes(player: Player) {
    val lumberCount = player.inventory.getTotalNumberOf(20702)
    if (lumberCount == 0) return
    player.inventory.deleteItem(20702, lumberCount)
    val ratio = lumberCount / 28.0
    val astral = (25 * ratio).toInt()
    val nature = (10 * ratio).toInt()
    val death = (10 * ratio).toInt()
    val earth = (10 * ratio).toInt()
    val cosmic = (10 * ratio).toInt()
    val water = (10 * ratio).toInt()
    if (astral > 0) player.inventory.addItem(Rune.ASTRAL.id(), astral)
    if (nature > 0) player.inventory.addItem(Rune.NATURE.id(), nature)
    if (death > 0) player.inventory.addItem(Rune.DEATH.id(), death)
    if (earth > 0) player.inventory.addItem(Rune.EARTH.id(), earth)
    if (cosmic > 0) player.inventory.addItem(Rune.COSMIC.id(), cosmic)
    if (water > 0) player.inventory.addItem(Rune.WATER.id(), water)
}

fun giveMeAnArcaneCapacitor(player: Player) {
    if (player.inventory.hasFreeSlots())
        player.inventory.addItem(21513)
    else {
        player.producePoints += LividFarmRewards.LividWish.GIVE_ME_AN_ARCANE_CAPACITOR.cost
        player.vars.syncVarsToClient()
    }

}

fun protectAPatchForMe(player: Player) {
    if (player.inventory.hasFreeSlots())
        player.inventory.addItem(21515)
    else {
        player.producePoints += LividFarmRewards.LividWish.PROTECT_A_PATCH_FOR_ME.cost
        player.vars.syncVarsToClient()
    }
}

data class Herbs(val herbId: Int, val amount: Int)
fun gimmeHerbs(player: Player) {
    val herbs = listOf(
        Herbs(Herbs.KWUARM.herbId, 3),
        Herbs(Herbs.IRIT.herbId, 3),
        Herbs(Herbs.CADANTINE.herbId, 2),
        Herbs(Herbs.AVANTOE.herbId, 2),
        Herbs(Herbs.LANTADYME.herbId, 1)
    )
    for (herb in herbs) {
        val item = Item(herb.herbId +1, herb.amount)
        if(!player.inventory.hasRoomFor(item)) {
            player.producePoints += LividFarmRewards.LividWish.GIMME_HERBS.cost
            return
        }
        else
            player.inventory.addItem(item)
    }
}

fun morePlanksPlease(player: Player) {
    player.addEffect(Effect.LIVID_PLANKS_BOOST, Ticks.fromMinutes(20).toLong())
}

data class SeedDrop(val seedId: Int, val amount: Int)
fun letItRainAwesomeSeeds(player: Player) {
    val seeds = listOf(
        SeedDrop(ProduceType.Rannar.seedId, 4),
        SeedDrop(ProduceType.Watermelon.seedId, 4),
        SeedDrop(ProduceType.Curry.seedId, 6),
        SeedDrop(ProduceType.Pineapple.seedId, 2),
        SeedDrop(ProduceType.Papaya.seedId, 2),
        SeedDrop(ProduceType.Willow.seedId, 4)
    )
    val baseTile = Tile.of(player.tile)
    val tilesAround = (-2..2).flatMap { dx ->
        (-2..2).map { dy -> baseTile.transform(dx, dy) }
    }
    for (seed in seeds) {
        val item = Item(seed.seedId, seed.amount)
        val dropTile = tilesAround.random()
        addGroundItem(item, dropTile, player)
    }
}

fun reduceTheFishIBurn(player: Player) {
    player.addEffect(Effect.LIVID_FISH_COOKING_BOOST, Ticks.fromMinutes(30).toLong())
}

fun vialMyHerbs(player: Player) {
    val MAX_TOTAL = 50
    var totalConverted = 0
    val cleanIdToPotion = CraftablePotion.entries
        .filter { it.name.endsWith("_POTION_UNF") }
        .associateBy { it.secondaries.firstOrNull()?.id }
    for (herb in Herbs.entries) {
        if (totalConverted >= MAX_TOTAL) break
        val cleanId = herb.cleanId
        val notedId = cleanId + 1
        val cleanCount = player.inventory.getAmountOf(cleanId)
        val notedCount = player.inventory.getAmountOf(notedId)
        val available = cleanCount + notedCount
        if (available == 0) continue
        val potion = cleanIdToPotion[cleanId] ?: continue
        val toConvert = minOf(MAX_TOTAL - totalConverted, available)
        val removeFromClean = minOf(cleanCount, toConvert)
        val removeFromNoted = toConvert - removeFromClean
        if (removeFromClean > 0)
            player.inventory.deleteItem(cleanId, removeFromClean)
        if (removeFromNoted > 0)
            player.inventory.deleteItem(notedId, removeFromNoted)
        player.inventory.addItem(potion.product.id + 1, toConvert)
        totalConverted += toConvert
    }
    if(totalConverted == 0) {
        player.producePoints += LividFarmRewards.LividWish.VIAL_MY_HERBS.cost
    }
}

fun iWantANewPatch(player: Player) {
//    player.vars.setVarBit(2603, 1)
//    player.vars.syncVarsToClient()
    player.sendMessage("Not yet implemented. Refunding points")
    player.producePoints += LividFarmRewards.LividWish.ID_LIKE_A_NEW_PATCH.cost
    //TODO: Need to figure out farming implementation, not working with this patch correctly.
}
