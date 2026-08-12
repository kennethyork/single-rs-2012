package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.game.map.instance.InstanceBuilder

enum class HouseRoofs(val chunkX: Int, val chunkY: Int, vararg dirs: Int) {
    ROOF1(233, 634, InstanceBuilder.NORTH, InstanceBuilder.SOUTH),
    ROOF2(235, 634, InstanceBuilder.NORTH, InstanceBuilder.EAST, InstanceBuilder.SOUTH),
    ROOF3(235, 634, InstanceBuilder.NORTH, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.NORTH),
    DUNGEON_ROOF1(235, 632, InstanceBuilder.NORTH, InstanceBuilder.EAST, InstanceBuilder.SOUTH, InstanceBuilder.NORTH)
}