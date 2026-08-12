package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.model.gameobject.GameObject

enum class HouseBuilds(
    private val ids: IntArray,
    private val water: Boolean,
    vararg val pieces: HouseObjects
) {
    //GARDEN
    CENTREPIECE(
        15361,
        false,
        HouseObjects.EXIT_PORTAL,
        HouseObjects.DECORATIVE_ROCK,
        HouseObjects.POND,
        HouseObjects.IMP_STATUE,
        HouseObjects.DUNGEON_ENTRACE
    ),
    BIG_TREE(
        15362,
        true,
        HouseObjects.BIG_DEATH_TREE,
        HouseObjects.BIG_NICE_TREE,
        HouseObjects.BIG_OAK_TREE,
        HouseObjects.BIG_WILLOW_TREE,
        HouseObjects.BIG_MAPLE_TREE,
        HouseObjects.BIG_YEW_TREE,
        HouseObjects.BIG_MAGIC_TREE
    ),
    TREE(
        15363,
        true,
        HouseObjects.DEATH_TREE,
        HouseObjects.NICE_TREE,
        HouseObjects.OAK_TREE,
        HouseObjects.WILLOW_TREE,
        HouseObjects.MAPLE_TREE,
        HouseObjects.YEW_TREE,
        HouseObjects.MAGIC_TREE
    ),
    SMALL_PLANT_1(15366, true, HouseObjects.PLANT, HouseObjects.SMALL_FERN, HouseObjects.FERN),
    SMALL_PLANT_2(15367, true, HouseObjects.DOCK_LEAF, HouseObjects.THISTLE, HouseObjects.REEDS),
    BIG_PLANT_1(15364, true, HouseObjects.FERN_B, HouseObjects.BUSH, HouseObjects.TALL_PLANT),
    BIG_PLANT_2(15365, true, HouseObjects.SHORT_PLANT, HouseObjects.LARGE_LEAF_PLANT, HouseObjects.HUGE_PLANT),

    // PARLOUR
    CHAIRS_1(
        15410,
        false,
        HouseObjects.CRUDE_WOODEN_CHAIR,
        HouseObjects.WOODEN_CHAIR,
        HouseObjects.ROCKING_CHAIR,
        HouseObjects.OAK_CHAIR,
        HouseObjects.OAK_ARMCHAIR,
        HouseObjects.TEAK_ARMCHAIR,
        HouseObjects.MAHOGANY_ARMCHAIR
    ),
    CHAIRS_2(
        15411,
        false,
        HouseObjects.CRUDE_WOODEN_CHAIR,
        HouseObjects.WOODEN_CHAIR,
        HouseObjects.ROCKING_CHAIR,
        HouseObjects.OAK_CHAIR,
        HouseObjects.OAK_ARMCHAIR,
        HouseObjects.TEAK_ARMCHAIR,
        HouseObjects.MAHOGANY_ARMCHAIR
    ),
    CHAIRS_3(
        15412,
        false,
        HouseObjects.CRUDE_WOODEN_CHAIR,
        HouseObjects.WOODEN_CHAIR,
        HouseObjects.ROCKING_CHAIR,
        HouseObjects.OAK_CHAIR,
        HouseObjects.OAK_ARMCHAIR,
        HouseObjects.TEAK_ARMCHAIR,
        HouseObjects.MAHOGANY_ARMCHAIR
    ),
    FIREPLACE(15418, false, HouseObjects.CLAY_FIREPLACE, HouseObjects.STONE_FIREPLACE, HouseObjects.MARBLE_FIREPLACE),
    CURTAINS(15419, false, HouseObjects.TORN_CURTAINS, HouseObjects.CURTAINS, HouseObjects.OPULENT_CURTAINS),
    BOOKCASE(15416, false, HouseObjects.WOODEN_BOOKCASE, HouseObjects.OAK_BOOKCASE, HouseObjects.MAHOGANY_BOOKCASE),
    STAIRCASE(
        15380,
        false,
        HouseObjects.OAK_STAIRCASE,
        HouseObjects.TEAK_STAIRCASE,
        HouseObjects.SPIRAL_STAIRCASE,
        HouseObjects.MARBLE_STAIRCASE,
        HouseObjects.MARBLE_SPIRAL
    ),
    STAIRCASE_DOWN(
        15381,
        false,
        HouseObjects.OAK_STAIRCASE_DOWN,
        HouseObjects.TEAK_STAIRCASE_DOWN,
        HouseObjects.SPIRAL_STAIRCASE,
        HouseObjects.MARBLE_STAIRCASE_DOWN,
        HouseObjects.MARBLE_SPIRAL
    ),
    STAIRCASE_1(
        15390,
        false,
        HouseObjects.OAK_STAIRCASE,
        HouseObjects.TEAK_STAIRCASE,
        HouseObjects.SPIRAL_STAIRCASE,
        HouseObjects.MARBLE_STAIRCASE,
        HouseObjects.MARBLE_SPIRAL
    ),
    STAIRCASE_DOWN_1(
        15391,
        false,
        HouseObjects.OAK_STAIRCASE_DOWN,
        HouseObjects.TEAK_STAIRCASE_DOWN,
        HouseObjects.SPIRAL_STAIRCASE,
        HouseObjects.MARBLE_STAIRCASE_DOWN,
        HouseObjects.MARBLE_SPIRAL
    ),
    RUG(intArrayOf(15415, 15414, 15413), false, HouseObjects.BROWN_RUG, HouseObjects.RUG, HouseObjects.OPULENT_RUG),

    // TODO dungeon stair id
    STAIRCASE_2(
        15390,
        false,
        HouseObjects.OAK_STAIRCASE,
        HouseObjects.TEAK_STAIRCASE,
        HouseObjects.SPIRAL_STAIRCASE,
        HouseObjects.MARBLE_STAIRCASE,
        HouseObjects.MARBLE_SPIRAL
    ),

    // TROPHY ROOM
    HEAD_TROPHY(
        15382,
        false,
        HouseObjects.CRAWLING_HAND,
        HouseObjects.COCKATRICE_HEAD,
        HouseObjects.BASALISK_HEAD,
        HouseObjects.KURASK,
        HouseObjects.ABBYSAL_DEMON,
        HouseObjects.KING_BLACK_DRAGON,
        HouseObjects.KALPHITE_QUEEN
    ),
    MOUNTED_FISH(15383, false, HouseObjects.MOUNTED_BASS, HouseObjects.MOUNTED_SWORDFISH, HouseObjects.MOUNTED_SHARK),
    RUNE_CASE(15386, false, HouseObjects.RUNE_CASE_1, HouseObjects.RUNE_CASE_2),
    DECORATIVE_ARMOUR(
        15384,
        false,
        HouseObjects.BASIC_DECORATIVE,
        HouseObjects.DETAILED_DECORATIVE,
        HouseObjects.INTRICATE_DECORATIVE,
        HouseObjects.PROFOUND_DECORATIVE
    ),
    BASIC_ARMOURS(34255, false, HouseObjects.MITHRIL_ARMOUR, HouseObjects.ADAMANT_ARMOUR, HouseObjects.RUNITE_ARMOUR),
    RUG_3(intArrayOf(15379, 15378, 15377), false, HouseObjects.RUG, HouseObjects.OPULENT_RUG),

    // BEDROOM
    BED(
        15260,
        false,
        HouseObjects.WOODEN_BED,
        HouseObjects.OAK_BED,
        HouseObjects.LARGE_OAK_BED,
        HouseObjects.TEAK_BED,
        HouseObjects.LARGE_TEAK_BED,
        HouseObjects.MAHOGANY_BED,
        HouseObjects.LARGE_MAHOGANY_BED
    ),
    BEDROOM_FIREPLACE(15267, false, HouseObjects.CLAY_FIREPLACE, HouseObjects.STONE_FIREPLACE, HouseObjects.MARBLE_FIREPLACE),
    BEDROOM_CURTAINS(15263, false, HouseObjects.TORN_CURTAINS, HouseObjects.CURTAINS, HouseObjects.OPULENT_CURTAINS),
    DRESSERS(
        15262,
        false,
        HouseObjects.SHAVING_STAND,
        HouseObjects.OAK_SHAVING_STAND,
        HouseObjects.OAK_DRESSER,
        HouseObjects.TEAK_DRESSER,
        HouseObjects.FANCY_TEAK_DRESSER,
        HouseObjects.MAHOGANY_DRESSER,
        HouseObjects.GILDED_DRESSER
    ),
    CLOCKS(15268, false, HouseObjects.OAK_CLOCK, HouseObjects.TEAK_CLOCK, HouseObjects.GILDED_CLOCK),
    WARDROBE(
        15261,
        false,
        HouseObjects.SHOE_BOX,
        HouseObjects.OAK_DRAWERS,
        HouseObjects.OAK_WARDROBE,
        HouseObjects.TEAK_DRAWERS,
        HouseObjects.TEAK_WARDROBE,
        HouseObjects.MAHOGANY_WARDROBE,
        HouseObjects.GILDED_WARDROBE
    ),
    RUG_B(intArrayOf(15266, 15265, 15264), false, HouseObjects.BROWN_RUG, HouseObjects.RUG, HouseObjects.OPULENT_RUG),

    //MENAGERIE
    OBELISK(44911, false, HouseObjects.SMALLOBELISK),
    FEEDER(44910, false, HouseObjects.OAKPETFEEDER, HouseObjects.TEAKPETFEEDER, HouseObjects.MAHOGANYPETFEEDER),
    PETHOUSE(
        44909,
        false,
        HouseObjects.OAKPETHOUSE,
        HouseObjects.TEAKPETHOUSE,
        HouseObjects.MAHOGANYPETHOUSE,
        HouseObjects.CONSECRATEDPETHOUSE,
        HouseObjects.DESECRATEDPETHOUSE,
        HouseObjects.NATURALPETHOUSE
    ),
    HABITAT(
        intArrayOf(
            44843, 44844, 44845, 44846, 44847, 44848, 44849, 44850, 44851, 44852, 44853,
            44854, 44855, 44856, 44857, 44858, 44859, 44860, 44861, 44862, 44863, 44864,
            44865, 44866, 44867, 44868, 44869, 44870, 44871, 44872, 44873, 44874, 44875,
            44876, 44877, 44878, 44879, 44880, 44881, 44882, 44883, 44884, 44885, 44886,
            44887, 44888, 44889, 44890, 44891, 44892, 44893, 44894, 44895, 44896, 44897,
            44898, 44899, 44900, 44901, 44902, 44903, 44904, 44905, 44906, 44907, 44908
        ),
        false,
        HouseObjects.GARDEN_HABITAT,
        HouseObjects.JUNGLE_HABITAT,
        HouseObjects.DESERT_HABITAT,
        HouseObjects.POLAR_HABITAT,
        HouseObjects.VOLCANIC_HABITAT
    ),

    //THRONE ROOM
    THRONE(
        15426,
        false,
        HouseObjects.OAKTHRONE,
        HouseObjects.TEAKTHRONE,
        HouseObjects.MAHOGANYTHRONE,
        HouseObjects.GILDEDTHRONE,
        HouseObjects.SKELETONTHRONE,
        HouseObjects.CRYSTALTHRONE,
        HouseObjects.DEMONICTHRONE
    ),
    LEVER(15435, false, HouseObjects.OAKLEVER, HouseObjects.TEAKLEVER, HouseObjects.MAHOGANYLEVER),
    FLOOR(
        intArrayOf(15427, 15428, 15429, 15430, 15431, 15432),
        false,
        HouseObjects.FLOORDECORATION,
        HouseObjects.STEELCAGE,
        HouseObjects.TRAPDOOR,
        HouseObjects.LESSERMAGICCAGE,
        HouseObjects.GREATERMAGICCAGE
    ),
    SEATING(intArrayOf(15436, 15437), false, HouseObjects.CARVEDTEAKBENCH, HouseObjects.MAHOGANYBENCH, HouseObjects.GILDEDBENCH),
    TRAPDOOR(15438, false, HouseObjects.OAKTRAPDOOR, HouseObjects.TEAKTRAPDOOR, HouseObjects.MAHOGANYTRAPDOOR),
    DECORATION_SPACE(
        15434,
        false,
        HouseObjects.OAK_DECORATION,
        HouseObjects.TEAK_DECORATION,
        HouseObjects.GILDED_DECORATION,
        HouseObjects.ROUND_SHIELD,
        HouseObjects.SQUARE_SHIELD,
        HouseObjects.KITE_SHIELD
    ),

    //FORMAL GARDEN
    FENCING(
        15369,
        false,
        HouseObjects.BOUNDARYSTONES,
        HouseObjects.WOODENFENCE,
        HouseObjects.STONEWALL,
        HouseObjects.IRONRAILINGS,
        HouseObjects.PICKETFENCE,
        HouseObjects.GARDENFENCE,
        HouseObjects.MARBLEWALL
    ),
    HEDGING_FORM(
        intArrayOf(15370, 15371, 15372),
        true,
        HouseObjects.THORNYHEDGE,
        HouseObjects.NICEHEDGE,
        HouseObjects.SMALLBOXHEDGE,
        HouseObjects.TOPIARYHEDGE,
        HouseObjects.FANCYHEDGE,
        HouseObjects.TALLFANCYHEDGE,
        HouseObjects.TALLBOXHEDGE
    ),
    CENTREPEICE_FORMAL(
        15368,
        false,
        HouseObjects.EXITPORTAL,
        HouseObjects.GAZEBO,
        HouseObjects.DUNGEONENTRANCE,
        HouseObjects.SMALLFOUNTAIN,
        HouseObjects.LARGEFOUNTAIN,
        HouseObjects.POSHFOUNTAIN
    ),
    SMALL_PLANT1_FORM(15375, true, HouseObjects.SUNFLOWER_S, HouseObjects.MARIGOLDS_S, HouseObjects.ROSES_S),
    SMALL_PLANT2_FORM(15376, true, HouseObjects.ROSEMARY_S, HouseObjects.DAFFODILS_S, HouseObjects.BLUEBELLS_S),
    BIG_PLANT1_FORM(15373, true, HouseObjects.SUNFLOWER, HouseObjects.MARIGOLDS, HouseObjects.ROSES),
    BIG_PLANT2_FORM(15374, true, HouseObjects.ROSEMARY, HouseObjects.DAFFODILS, HouseObjects.BLUEBELLS),

    // KITCHEN
    LARDER(15403, false, HouseObjects.WOODEN_LARDER, HouseObjects.OAK_LARDER, HouseObjects.TEAK_LARDER),
    SINK(15404, false, HouseObjects.PUMP_AND_DRAIN, HouseObjects.PUMP_AND_TUB, HouseObjects.SINK),
    KITCHEN_TABLE(15405, false, HouseObjects.WOODEN_KITCHEN_TABLE, HouseObjects.OAK_KITCHEN_TABLE, HouseObjects.TEAK_KITCHEN_TABLE),
    CAT_BLANKET(15402, false, HouseObjects.CAT_BLANKET, HouseObjects.CAT_BASKET, HouseObjects.CUSHIONED_CAT_BASKET),
    STOVE(
        15398,
        false,
        HouseObjects.FIREPIT,
        HouseObjects.FIREPIT_WITH_HOOK,
        HouseObjects.FIREPIT_WITH_POT,
        HouseObjects.SMALL_OVEN,
        HouseObjects.LARGE_OVEN,
        HouseObjects.STEEL_RANGE,
        HouseObjects.FANCY_RANGE
    ),
    SHELVES(
        intArrayOf(15399, 15400),
        false,
        HouseObjects.WOODEN_SHELVES_1,
        HouseObjects.WOODEN_SHELVES_2,
        HouseObjects.WOODEN_SHELVES_3,
        HouseObjects.OAK_SHELVES_1,
        HouseObjects.OAK_SHELVES_2,
        HouseObjects.TEAK_SHELVES_1,
        HouseObjects.TEAK_SHELVES_2
    ),
    BARRELS(
        15401,
        false,
        HouseObjects.BEER_BARREL,
        HouseObjects.CIDER_BARREL,
        HouseObjects.ASGARNIAN_ALE,
        HouseObjects.GREENMAN_ALE,
        HouseObjects.DRAGON_BITTER_ALE,
        HouseObjects.CHEFS_DELIGHT
    ),

    // DINING ROOM
    FIREPLACE_DINING(15301, false, HouseObjects.CLAY_FIREPLACE, HouseObjects.STONE_FIREPLACE, HouseObjects.MARBLE_FIREPLACE),
    DINING_TABLE(
        15298,
        false,
        HouseObjects.WOOD_DINING,
        HouseObjects.OAK_DINING,
        HouseObjects.CARVED_OAK_DINING,
        HouseObjects.TEAK_DINING,
        HouseObjects.CARVED_TEAK_DINING,
        HouseObjects.MAHOGANY_DINING,
        HouseObjects.OPULENT_TABLE
    ),
    DINING_CURTAINS(15302, false, HouseObjects.TORN_CURTAINS, HouseObjects.CURTAINS, HouseObjects.OPULENT_CURTAINS),
    DINING_BENCH(
        intArrayOf(15299, 15300),
        false,
        HouseObjects.WOOD_BENCH,
        HouseObjects.OAK_BENCH,
        HouseObjects.CARVED_OAK_BENCH,
        HouseObjects.TEAK_BENCH,
        HouseObjects.CARVED_TEAK_BENCH,
        HouseObjects.MAHOGANY_BENCH,
        HouseObjects.GILDED_BENCH
    ),
    ROPE_BELL_PULL(15304, false, HouseObjects.ROPE_BELL_PULL, HouseObjects.BELL_PULL, HouseObjects.POSH_BELL_PULL),
    DECORATION(15303, false, HouseObjects.OAK_DECORATION, HouseObjects.TEAK_DECORATION, HouseObjects.GILDED_DECORATION),

    // WORKSPACE
    REPAIR(15448, false, HouseObjects.REPAIR_BENCH, HouseObjects.WHETSTONE, HouseObjects.ARMOUR_STAND),
    WORKBENCH(
        15439,
        false,
        HouseObjects.WOODEN_WORKBENCH,
        HouseObjects.OAK_WORKBENCH,
        HouseObjects.STEEL_FRAMED_WORKBENCH,
        HouseObjects.BENCH_WITH_VICE,
        HouseObjects.BENCH_WITH__A_LATHE
    ),
    CRAFTING(
        15441,
        false,
        HouseObjects.CRAFTING_TABLE,
        HouseObjects.CRAFTING_TABLE_2,
        HouseObjects.CRAFTING_TABLE_3,
        HouseObjects.CRAFTING_TABLE_4
    ),
    TOOL1(
        15443,
        false,
        HouseObjects.TOOL_STORE_1,
        HouseObjects.TOOL_STORE_2,
        HouseObjects.TOOL_STORE_3,
        HouseObjects.TOOL_STORE_4,
        HouseObjects.TOOL_STORE_5
    ),
    TOOL2(
        15444,
        false,
        HouseObjects.TOOL_STORE_1,
        HouseObjects.TOOL_STORE_2,
        HouseObjects.TOOL_STORE_3,
        HouseObjects.TOOL_STORE_4,
        HouseObjects.TOOL_STORE_5
    ),
    TOOL3(
        15445,
        false,
        HouseObjects.TOOL_STORE_1,
        HouseObjects.TOOL_STORE_2,
        HouseObjects.TOOL_STORE_3,
        HouseObjects.TOOL_STORE_4,
        HouseObjects.TOOL_STORE_5
    ),
    TOOL4(
        15446,
        false,
        HouseObjects.TOOL_STORE_1,
        HouseObjects.TOOL_STORE_2,
        HouseObjects.TOOL_STORE_3,
        HouseObjects.TOOL_STORE_4,
        HouseObjects.TOOL_STORE_5
    ),
    TOOL5(
        15447,
        false,
        HouseObjects.TOOL_STORE_1,
        HouseObjects.TOOL_STORE_2,
        HouseObjects.TOOL_STORE_3,
        HouseObjects.TOOL_STORE_4,
        HouseObjects.TOOL_STORE_5
    ),
    HERALDRY(15450, false, HouseObjects.PLUMING_STAND, HouseObjects.SHIELD_EASEL, HouseObjects.BANNER_EASEL),

    // COSTUME ROOM
    DRESS_BOX(18814, false, HouseObjects.OAKCOSTUMEBOX, HouseObjects.TEAKCOSTUMEBOX, HouseObjects.MAHOGANYCOSTUMEBOX),
    TOY_BOX(18812, false, HouseObjects.OAKTOYBOX, HouseObjects.TEAKTOYBOX, HouseObjects.MAHOGANYTOYBOX),
    CAPE_RACK(
        18810,
        false,
        HouseObjects.OAKCAPERACK,
        HouseObjects.TEAKCAPERACK,
        HouseObjects.MAHOGANYCAPERACK,
        HouseObjects.GILDEDCAPERACK,
        HouseObjects.MARBLECAPERACK,
        HouseObjects.MAGICALCAPERACK
    ),
    WARDROBE_COST(
        18811,
        false,
        HouseObjects.OAKMAGICWARDROBE,
        HouseObjects.CARVEDOAKMAGICWARDROBE,
        HouseObjects.TEAKMAGICWARDROBE,
        HouseObjects.CARVEDTEAKMAGICWARDROBE,
        HouseObjects.MAHOGANYMAGICWARDROBE,
        HouseObjects.GILDEDMAGICWARDROBE,
        HouseObjects.MARBLEMAGICWARDROBE
    ),
    TREASURE_CHEST(18813, false, HouseObjects.OAKTREASURECHEST, HouseObjects.TEAKTREASURECHEST, HouseObjects.MAHOGANYTREASURECHEST),
    ARMOR_CASE(18815, false, HouseObjects.OAKARMOURCASE, HouseObjects.TEAKARMOURCASE, HouseObjects.MAHOGANYARMOURCASE),

    //DUNGEON
    LIGHTING_D(15355, false, HouseObjects.CANDLE, HouseObjects.TORCH, HouseObjects.SKULLTORCH),
    RUG_D(15379, false, HouseObjects.BROWN_RUG, HouseObjects.RUG, HouseObjects.OPULENT_RUG),

    //		DOOR_D(36672, false, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.OAKDOOR, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.STEELPLATEDDOOR, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.MARBLEDOOR),
    //		DOOR_D2(36675, false, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.OAKDOOR, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.STEELPLATEDDOOR, com.rs.game.content.skills.construction.playerOwnedHouse.HObject.MARBLEDOOR),
    PITGUARD(
        36676,
        false,
        HouseObjects.PITDOG,
        HouseObjects.PITOGRE,
        HouseObjects.PITROCKPROTECTER,
        HouseObjects.PITSCABARITE,
        HouseObjects.PITBLACKDEMON,
        HouseObjects.PITIRONDRAGON
    ),
    PITTRAP(
        intArrayOf(39229, 39230, 39231, 36692),
        false,
        HouseObjects.MINORPITTRAP,
        HouseObjects.MAGORPITTRAP,
        HouseObjects.SUPERIORPITTRAP
    ),
    TRAP(
        15324,
        false,
        HouseObjects.SPIKETRAP,
        HouseObjects.MANTRAP,
        HouseObjects.TANGLEVINE,
        HouseObjects.MARBLETRAP,
        HouseObjects.TELEPORTTRAP
    ),
    TRAP2(
        15325,
        false,
        HouseObjects.SPIKETRAP,
        HouseObjects.MANTRAP,
        HouseObjects.TANGLEVINE,
        HouseObjects.MARBLETRAP,
        HouseObjects.TELEPORTTRAP
    ),
    GUARD2(
        15337,
        false,
        HouseObjects.SKELETON,
        HouseObjects.GUARDDOG,
        HouseObjects.HOBGOBLIN,
        HouseObjects.BABYREDDRAGON,
        HouseObjects.HUGESPIDER,
        HouseObjects.TROLLGUARD,
        HouseObjects.HELLHOUND
    ),
    GUARD3(
        15323,
        false,
        HouseObjects.SKELETON,
        HouseObjects.GUARDDOG,
        HouseObjects.HOBGOBLIN,
        HouseObjects.BABYREDDRAGON,
        HouseObjects.HUGESPIDER,
        HouseObjects.TROLLGUARD,
        HouseObjects.HELLHOUND
    ),
    GUARD4(
        15336,
        false,
        HouseObjects.SKELETON,
        HouseObjects.GUARDDOG,
        HouseObjects.HOBGOBLIN,
        HouseObjects.BABYREDDRAGON,
        HouseObjects.HUGESPIDER,
        HouseObjects.TROLLGUARD,
        HouseObjects.HELLHOUND
    ),
    GUARD5(
        15354,
        false,
        HouseObjects.SKELETON,
        HouseObjects.GUARDDOG,
        HouseObjects.HOBGOBLIN,
        HouseObjects.BABYREDDRAGON,
        HouseObjects.HUGESPIDER,
        HouseObjects.TROLLGUARD,
        HouseObjects.HELLHOUND
    ),

    OUB_CAGE(
        intArrayOf(15352, 15353),
        false,
        HouseObjects.OAK_CAGE,
        HouseObjects.OAK_ST_CAGE,
        HouseObjects.STEEL_CAGE,
        HouseObjects.SPIKED_CAGE,
        HouseObjects.BONE_CAGE
    ),
    OUB_TRAP(
        intArrayOf(15347, 15348, 15349, 15350, 15351),
        false,
        HouseObjects.SPIKES,
        HouseObjects.TENTACLES,
        HouseObjects.FLAMES,
        HouseObjects.ROCNAR
    ),
    OUB_LADDER(15356, false, HouseObjects.OAK_LADDER, HouseObjects.TEAK_LADDER, HouseObjects.MAHOGANY_LADDER),

    //TREASURE ROOM
    DECORATIVE2(15331, false, HouseObjects.DECORATIVEBLOOD, HouseObjects.DECORATIVEPIPE, HouseObjects.HANGINGSKELETON),
    LIGHTING(15330, false, HouseObjects.CANDLE, HouseObjects.TORCH, HouseObjects.SKULLTORCH),
    TREASURE(
        15256,
        false,
        HouseObjects.WOODENCRATE,
        HouseObjects.OAKCHEST,
        HouseObjects.TEAKCHEST,
        HouseObjects.MAHOGANYCHEST,
        HouseObjects.MAGICCHEST
    ),
    GUARDIAN(
        15257,
        false,
        HouseObjects.DEMON,
        HouseObjects.KALPHITESOLDIER,
        HouseObjects.TOKXIL,
        HouseObjects.DAGANNOTH,
        HouseObjects.STEELDRAGON
    ),
    DOOR(intArrayOf(15326, 15327, 15328, 15329), false, HouseObjects.OAKDOOR, HouseObjects.STEELPLATEDDOOR, HouseObjects.MARBLEDOOR),

    // GAMES ROOM
    ELEMENTAL_BALANCE(
        15345,
        false,
        HouseObjects.ELEMENTAL_BALANCE_1,
        HouseObjects.ELEMENTAL_BALANCE_2,
        HouseObjects.ELEMENTAL_BALANCE_3
    ),
    ATTACK_STONE(15344, false, HouseObjects.CLAY_ATTACK_STONE, HouseObjects.LIMESTONE_ATTACK_STONE, HouseObjects.MARBLE_ATTACK_STONE),
    RANGING_GAME(15346, false, HouseObjects.HOOP_AND_STICK, HouseObjects.DART_BOARD, HouseObjects.ARCHERY_TARGET),
    GAME(15342, false, HouseObjects.JESTER, HouseObjects.TREASURE_HUNT, HouseObjects.HANGMAN),
    GAME_CHEST(15343, false, HouseObjects.OAK_PRIZE_CHEST, HouseObjects.TEAK_PRIZE_CHEST, HouseObjects.GILDED_PRIZE_CHEST),

    // BOXING ROOM
    DECORATIONS(15297, false, HouseObjects.OAK_DECORATION, HouseObjects.TEAK_DECORATION, HouseObjects.GILDED_DECORATION),
    WEAPONS_RACK(15296, false, HouseObjects.GLOVE_RACK, HouseObjects.WEAPON_RACK, HouseObjects.EXTRA_WEAPON_RACK),
    COMBAT_RING(
        intArrayOf(
            15277, 15278, 15279, 15280, 15281, 15282, 15286, 15287,  // ring
            15289, 15290, 15294, 15295,  // 2 side
            15288, 15291, 15293, 15292
        ), false, HouseObjects.BOXING_RING, HouseObjects.FENCING_RING, HouseObjects.COMBAT_RING
    ),  // 0side

    // QUEST ROOM
    QUEST_BOOKCASE(15397, false, HouseObjects.WOODEN_BOOKCASE, HouseObjects.OAK_BOOKCASE, HouseObjects.MAHOGANY_BOOKCASE),
    SWORD(15395, false, HouseObjects.SILVERLIGHT_SWORD, HouseObjects.EXCALIBER_SWORD, HouseObjects.DARKLIGHT_SWORD),
    MAP(15396, false, HouseObjects.MAP_SMALL, HouseObjects.MAP_MEDIUM, HouseObjects.MAP_LARGE),
    LANDSCAPE(15393, false, HouseObjects.LUMBRIDGE, HouseObjects.DESERT, HouseObjects.MORTANYIA, HouseObjects.KARAMJA, HouseObjects.ISFADAR),
    PORTRAIT(15392, false, HouseObjects.KING_ARTHUR, HouseObjects.ELENA, HouseObjects.GIANT_DWARF, HouseObjects.MISCELLENIA),
    GUILD_TROPHY(15394, false, HouseObjects.ANTIDRAGON_SHIELD, HouseObjects.GLORY, HouseObjects.CAPE_OF_LEGENDS),

    // STUDY ROOM
    STUDY_BOOKCASE(15425, false, HouseObjects.WOODEN_BOOKCASE, HouseObjects.OAK_BOOKCASE, HouseObjects.MAHOGANY_BOOKCASE),
    CHARTS(15423, false, HouseObjects.ALCHEMICAL_CHART, HouseObjects.ASTRONOMICAL_CHART, HouseObjects.INFERNAL),
    LECTERN_STATUE(48662, false, HouseObjects.LECTURN_STATUE),
    GLOBE(
        15421,
        false,
        HouseObjects.GLOBE,
        HouseObjects.ORNAMENTAL_GLOBE,
        HouseObjects.LUNAR_GLOBE,
        HouseObjects.CELESTIAL_GLOBE,
        HouseObjects.ARMILLARY_SPHERE,
        HouseObjects.SMALL_ORRERY,
        HouseObjects.LARGE_ORRERY
    ),
    LECTURN(
        15420,
        false,
        HouseObjects.EAGLE_LECTURN,
        HouseObjects.DEMON_LECTURN,
        HouseObjects.TEAK_EAGLE_LECTURN,
        HouseObjects.TEAK_DEMON_LECTURN,
        HouseObjects.MAHOGANY_EAGLE_LECTURN,
        HouseObjects.MAHOGANY_DEMON_LECTURN
    ),
    CRYSTAL_BALL(15422, false, HouseObjects.CRYSTAL_BALL, HouseObjects.ELEMENTAL_SPHERE, HouseObjects.CRYSTAL_OF_POWER),
    TELESCOPE(15424, false, HouseObjects.WOODEN_TELESCOPE, HouseObjects.TEAK_TELESCOPE, HouseObjects.MAHOGANY_TELESCOPE),
    RUG_2(intArrayOf(15389, 15388, 15387), false, HouseObjects.RUG, HouseObjects.OPULENT_RUG),

    // PORTAL ROOM
    PORTALS1(
        15406,
        false,
        HouseObjects.PORTAL_FRAME_TEAK,
        HouseObjects.PORTAL_FRAME_MAHOGANY,
        HouseObjects.PORTAL_FRAME_MARBLE,
        HouseObjects.VARROCK_PORTAL_TEAK,
        HouseObjects.LUMBRIDGE_PORTAL_TEAK,
        HouseObjects.FALADOR_PORTAL_TEAK,
        HouseObjects.CAMELOT_PORTAL_TEAK,
        HouseObjects.ARDOUGNE_PORTAL_TEAK,
        HouseObjects.YANILLE_PORTAL_TEAK,
        HouseObjects.KHARYRLL_PORTAL_TEAK,
        HouseObjects.VARROCK_PORTAL_MAHOGANY,
        HouseObjects.LUMBRIDGE_PORTAL_MAHOGANY,
        HouseObjects.FALADOR_PORTAL_MAHOGANY,
        HouseObjects.CAMELOT_PORTAL_MAHOGANY,
        HouseObjects.ARDOUGNE_PORTAL_MAHOGANY,
        HouseObjects.YANILLE_PORTAL_MAHOGANY,
        HouseObjects.KHARYRLL_PORTAL_MAHOGANY,
        HouseObjects.VARROCK_PORTAL_MARBLE,
        HouseObjects.LUMBRIDGE_PORTAL_MARBLE,
        HouseObjects.FALADOR_PORTAL_MARBLE,
        HouseObjects.CAMELOT_PORTAL_MARBLE,
        HouseObjects.ARDOUGNE_PORTAL_MARBLE,
        HouseObjects.YANILLE_PORTAL_MARBLE,
        HouseObjects.KHARYRLL_PORTAL_MARBLE
    ),
    PORTALS2(
        15407,
        false,
        HouseObjects.PORTAL_FRAME_TEAK,
        HouseObjects.PORTAL_FRAME_MAHOGANY,
        HouseObjects.PORTAL_FRAME_MARBLE,
        HouseObjects.VARROCK_PORTAL_TEAK,
        HouseObjects.LUMBRIDGE_PORTAL_TEAK,
        HouseObjects.FALADOR_PORTAL_TEAK,
        HouseObjects.CAMELOT_PORTAL_TEAK,
        HouseObjects.ARDOUGNE_PORTAL_TEAK,
        HouseObjects.YANILLE_PORTAL_TEAK,
        HouseObjects.KHARYRLL_PORTAL_TEAK,
        HouseObjects.VARROCK_PORTAL_MAHOGANY,
        HouseObjects.LUMBRIDGE_PORTAL_MAHOGANY,
        HouseObjects.FALADOR_PORTAL_MAHOGANY,
        HouseObjects.CAMELOT_PORTAL_MAHOGANY,
        HouseObjects.ARDOUGNE_PORTAL_MAHOGANY,
        HouseObjects.YANILLE_PORTAL_MAHOGANY,
        HouseObjects.KHARYRLL_PORTAL_MAHOGANY,
        HouseObjects.VARROCK_PORTAL_MARBLE,
        HouseObjects.LUMBRIDGE_PORTAL_MARBLE,
        HouseObjects.FALADOR_PORTAL_MARBLE,
        HouseObjects.CAMELOT_PORTAL_MARBLE,
        HouseObjects.ARDOUGNE_PORTAL_MARBLE,
        HouseObjects.YANILLE_PORTAL_MARBLE,
        HouseObjects.KHARYRLL_PORTAL_MARBLE
    ),
    PORTALS3(
        15408,
        false,
        HouseObjects.PORTAL_FRAME_TEAK,
        HouseObjects.PORTAL_FRAME_MAHOGANY,
        HouseObjects.PORTAL_FRAME_MARBLE,
        HouseObjects.VARROCK_PORTAL_TEAK,
        HouseObjects.LUMBRIDGE_PORTAL_TEAK,
        HouseObjects.FALADOR_PORTAL_TEAK,
        HouseObjects.CAMELOT_PORTAL_TEAK,
        HouseObjects.ARDOUGNE_PORTAL_TEAK,
        HouseObjects.YANILLE_PORTAL_TEAK,
        HouseObjects.KHARYRLL_PORTAL_TEAK,
        HouseObjects.VARROCK_PORTAL_MAHOGANY,
        HouseObjects.LUMBRIDGE_PORTAL_MAHOGANY,
        HouseObjects.FALADOR_PORTAL_MAHOGANY,
        HouseObjects.CAMELOT_PORTAL_MAHOGANY,
        HouseObjects.ARDOUGNE_PORTAL_MAHOGANY,
        HouseObjects.YANILLE_PORTAL_MAHOGANY,
        HouseObjects.KHARYRLL_PORTAL_MAHOGANY,
        HouseObjects.VARROCK_PORTAL_MARBLE,
        HouseObjects.LUMBRIDGE_PORTAL_MARBLE,
        HouseObjects.FALADOR_PORTAL_MARBLE,
        HouseObjects.CAMELOT_PORTAL_MARBLE,
        HouseObjects.ARDOUGNE_PORTAL_MARBLE,
        HouseObjects.YANILLE_PORTAL_MARBLE,
        HouseObjects.KHARYRLL_PORTAL_MARBLE
    ),
    FOCUS(15409, false, HouseObjects.TELEPORT_FOCUS, HouseObjects.GREATER_FOCUS, HouseObjects.SCRYING_POOL),

    // CHAPEL
    ALTAR(
        15270,
        false,
        HouseObjects.OAK_ALTAR,
        HouseObjects.TEAK_ALTAR,
        HouseObjects.CLOTH_ALTAR,
        HouseObjects.MAHOGANY_ALTAR,
        HouseObjects.LIMESTONE_ALTAR,
        HouseObjects.MARBLE_ALTAR,
        HouseObjects.GUILDED_ALTAR
    ),
    LAMP(
        15271,
        false,
        HouseObjects.STEEL_TORCHES,
        HouseObjects.WOODEN_TORCHES,
        HouseObjects.STEEL_CANDLESTICK,
        HouseObjects.GOLD_CANDLESTICK,
        HouseObjects.INSCENCE_BURNER,
        HouseObjects.MAHOGANY_BURNER,
        HouseObjects.MARBLE_BURNER
    ),
    ICON(
        15269,
        false,
        HouseObjects.GUTHIX_SYMBOL,
        HouseObjects.SARADOMIN_SYMBOL,
        HouseObjects.ZAMORAK_SYMBOL,
        HouseObjects.GUTHIX_ICON,
        HouseObjects.SARADOMIN_ICON,
        HouseObjects.ZAMORAK_ICON,
        HouseObjects.ICON_OF_BOB
    ),
    MUSICAL(15276, false, HouseObjects.WINCHIMES, HouseObjects.BELLS, HouseObjects.ORGAN),
    RUG_CHAPEL(intArrayOf(15274, 15273, 15274), false, HouseObjects.BROWN_RUG, HouseObjects.RUG, HouseObjects.OPULENT_RUG),
    STATUES(15275, false, HouseObjects.SMALL_STATUE, HouseObjects.MEDIUM_STATUE, HouseObjects.LARGE_STATUE),

    // todo find -1(Zenevivia)
    WINDOW(
        intArrayOf(13730, 13728, 13732, 13729, 13733, 13731, 7101),
        false,
        HouseObjects.SHUTTERED_WINDOW,
        HouseObjects.DECORATIVE_WINDOW,
        HouseObjects.STAINED_GLASS
    ),
    ;
    constructor(id: Int, water: Boolean, vararg pieces: HouseObjects) : this(intArrayOf(id), water, *pieces)

    // Methods
    fun containsId(id: Int): Boolean {
        return getIdSlot(id) != -1
    }

    fun getIdSlot(id: Int): Int {
        for (i in ids.indices) {
            if (ids[i] == id) {
                return i
            }
        }
        return -1
    }

    val id: Int
        get() = ids[0]

    val idsOnly: Int
        get() = ids[0]

    fun containsObject(obj: GameObject): Boolean {
        for (piece in pieces) {
            for (id in piece.getIds()) {
                if (obj.id == id) {
                    return true
                }
            }
        }
        return false
    }

    val piecesList: Array<HouseObjects>
        get() = pieces as Array<HouseObjects>

    val isWater: Boolean
        get() = water
}