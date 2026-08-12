package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.engine.quest.Quest
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills

enum class HouseHeraldry(val ccname: String, val description: String, val cost: Int, val requirements: String, val requirementCheck: (Player) -> Boolean) {
    ARRAV(
        "Arrav",
        "Shield of Arrav symbol",
        5000,
        "Quest points: Shield of Arrav",
        { player -> player.isQuestComplete(Quest.SHIELD_OF_ARRAV) }),
    ASGARNIA("Asgarnia", "Symbol of Asgarnia", 5000, "", { true }),
    DORGESHUUN(
        "Dorgeshuun",
        "Dorgeshuun brooch",
        5000,
        "Quest points: The Lost Tribe",
        { player -> player.isQuestComplete(Quest.LOST_TRIBE) }),
    DRAGON(
        "Dragon",
        "Dragon",
        5000,
        "Quest points: Dragon Slayer I",
        { player -> player.isQuestComplete(Quest.DRAGON_SLAYER) }),
    FAIRY("Fairy", "Fairy", 5000, "Quest points: Lost City", { player -> player.isQuestComplete(Quest.LOST_CITY) }),
    GUTHIX(
        "Guthix",
        "Symbol of Guthix",
        5000,
        "Prayer: 70+ Prayer (unboostable)",
        { player -> player.skills.getLevel(Skills.PRAYER) >= 70 }),
    HAM("HAM", "Symbol of the HAM cult", 5000, "", { true }),
    HORSE(
        "Horse",
        "Horse",
        5000,
        "Toy horsey in inventory",
        { player -> player.inventory.containsOneItem(2520, 2522, 2524, 2526) }),
    JOGRE("Jogre", "Jungle Ogre", 5000, "", { true }),
    KANDARIN("Kandarin", "Symbol of Kandarin", 5000, "", { true }),
    MISTHALIN("Misthalin", "Symbol of Misthalin", 5000, "", { true }),
    MONEY("Money (500000gp)", "Money", 500000, "", { true }),
    SARADOMIN(
        "Saradomin",
        "Symbol of Saradomin",
        5000,
        "Prayer: 70+ Prayer (unboostable)",
        { player -> player.skills.getLevel(Skills.PRAYER) >= 70 }),
    SKULL("Skull", "Skull", 5000, "Skulled", { player -> player.hasSkull() }),
    VARROCK("Varrock", "Symbol of Varrock", 5000, "", { true }),
    ZAMORAK(
        "Zamorak",
        "Symbol of Zamorak",
        5000,
        "Prayer: 70+ Prayer (unboostable)",
        { player -> player.skills.getLevel(Skills.PRAYER) >= 70 });

    companion object {
        @JvmField
        val ROUND_SHIELD_IDS = intArrayOf(
            13734, 13735, 13736, 13737, 13738, 13739,
            13740, 13741, 13742, 13743, 13744, 13745,
            13746, 13747, 13748, 13749
        )

        @JvmField
        val SQUARE_SHIELD_IDS = intArrayOf(
            13766, 13767, 13768, 13769, 13770, 13771,
            13772, 13773, 13774, 13775, 13776, 13777,
            13778, 13779, 13780, 13781
        )

        @JvmField
        val KITE_SHIELD_IDS = intArrayOf(
            13750, 13751, 13752, 13753, 13754, 13755,
            13756, 13757, 13758, 13759, 13760, 13761,
            13762, 13763, 13764, 13765
        )

        @JvmField
        val MAHOGANY_WALL_DECORATION_IDS = intArrayOf(
            13782, 13783, 13784, 13785, 13786, 13787, 13788, 13789,
            13790, 13791, 13792, 13793, 13794, 13795, 13796, 13797
        )

        @JvmField
        val OAK_WALL_DECORATION_IDS = intArrayOf(
            13798, 13799, 13800, 13801, 13802, 13803, 13804, 13805,
            13806, 13807, 13808, 13809, 13810, 13811, 13812, 13813
        )

        @JvmField
        val TEAK_WALL_DECORATION_IDS = intArrayOf(
            13814, 13815, 13816, 13817, 13818, 13819, 13820, 13821,
            13822, 13823, 13824, 13825, 13826, 13827, 13828, 13829
        )

        fun getCurrentCrest(player: Player?): HouseHeraldry {
            val crestName = player?.getSavingAttributes()?.getOrDefault("FamilyCrest", "VARROCK") as String
            return entries.firstOrNull { it.name.equals(crestName, ignoreCase = true) } ?: VARROCK
        }

        fun setCurrentCrest(player: Player, crest: HouseHeraldry) {
            player.getSavingAttributes()["FamilyCrest"] = crest.name
        }
    }
}