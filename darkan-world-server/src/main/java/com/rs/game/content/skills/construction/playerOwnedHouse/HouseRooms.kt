package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.map.instance.InstanceBuilder

enum class HouseRooms(
    val price: Int,
    val level: Int,
    val chunkX: Int,
    val chunkY: Int,
    val isShowRoof: Boolean,
    vararg val doorDirs: Int // `vararg` can be used for the doors
) {
    PARLOUR(1000, 1, 232, 639, true, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    GARDEN(
        1000,
        1,
        232,
        633,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    KITCHEN(5000, 5, 234, 639, true, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    DINING_ROOM(5000, 10, 236, 639, true, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    WORKSHOP(10000, 15, 232, 637, true, InstanceBuilder.NORTH, InstanceBuilder.SOUTH),
    BEDROOM(10000, 20, 238, 639, true, InstanceBuilder.NORTH, InstanceBuilder.WEST),
    HALL_SKILL(
        15000,
        25,
        233,
        638,
        true,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    GAMES_ROOM(25000, 30, 237, 636, true, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    COMBAT_ROOM(25000, 32, 235, 636, true, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    HALL_QUEST(
        25000,
        35,
        237,
        638,
        true,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    MENAGERIE(
        30000,
        37,
        239,
        634,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    STUDY(50000, 40, 236, 637, true, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    COSTUME_ROOM(
        50000,
        42,
        238,
        633,
        true,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    CHAPEL(50000, 45, 234, 637, true, InstanceBuilder.SOUTH, InstanceBuilder.WEST),
    PORTAL_CHAMBER(100000, 50, 233, 636, true, InstanceBuilder.SOUTH),
    FORMAL_GARDEN(
        75000,
        55,
        234,
        633,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    THRONE_ROOM(150000, 60, 238, 637, true, InstanceBuilder.SOUTH),
    OUTBLIETTE(
        150000,
        65,
        238,
        635,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    DUNGEON_CORRIDOR(7500, 70, 236, 635, false, InstanceBuilder.NORTH, InstanceBuilder.SOUTH),
    DUNGEON_JUNCTION(
        7500,
        70,
        232,
        635,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    DUNGEON_STAIRS(
        7500,
        70,
        234,
        635,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    DUNGEON_PIT(
        10000,
        70,
        237,
        634,
        false,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    TREASURE_ROOM(250000, 75, 239, 636, true, InstanceBuilder.SOUTH),
    HALL_SKILL_DOWN(
        15000,
        25,
        235,
        638,
        true,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    ),
    HALL_QUEST_DOWN(
        25000,
        35,
        239,
        638,
        true,
        InstanceBuilder.NORTH,
        InstanceBuilder.EAST,
        InstanceBuilder.SOUTH,
        InstanceBuilder.WEST
    );
}
