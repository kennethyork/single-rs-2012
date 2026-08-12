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
package com.rs.game.content.skills.construction.playerOwnedHouse

object HouseConstants {
    const val HAMMER: Int = 2347
    const val SAW: Int = 8794
    const val IRON_BAR: Int = 2351
    const val LIMESTONE_BRICKS: Int = 3420
    const val SOFT_CLAY: Int = 1761
    const val MARBLE_BLOCK: Int = 8786
    const val PLANK: Int = 960
    const val NAILS: Int = 1539
    const val OAK_PLANK: Int = 8778
    const val TEAK_PLANK: Int = 8780
    const val MAHOGANY_PLANK: Int = 8782
    const val BOLT_OF_CLOTH: Int = 8790
    const val STEEL_BAR: Int = 2353
    const val GOLD_LEAF: Int = 8784
    const val MAGIC_STONE: Int = 8788
    const val MOLTEN_GLASS: Int = 1775

    var WINDOW_SPACE_ID: Int = 13830
    var WINDOW_IDS: IntArray = intArrayOf(13099, 13091, 13005, 13112, 13011, 13117, 14354)
    var WALL_IDS: IntArray = intArrayOf(13098, 13090, 13004, 13111, 13011, 13116, 14354)
    var NAIL_IDS: IntArray = intArrayOf(1539, 4819, 4820, 4821, 4822, 4823, 4824)

    @JvmField
	val BEERS: IntArray = intArrayOf(1917, 5763, 1905, 1909, 1911, 5755)

    @JvmField
	val DINNER_BUILDS: Array<HouseBuilds> =
        arrayOf(HouseBuilds.SHELVES, HouseBuilds.LARDER, HouseBuilds.KITCHEN_TABLE, HouseBuilds.STOVE)
    @JvmField
	val TEA_BUILDS: Array<HouseBuilds> =
        arrayOf(HouseBuilds.SHELVES, HouseBuilds.SINK, HouseBuilds.LARDER, HouseBuilds.KITCHEN_TABLE, HouseBuilds.STOVE)
    val DRINKS_BUILDS: Array<HouseBuilds> = arrayOf(HouseBuilds.SHELVES, HouseBuilds.BARRELS)

    @JvmField
	val BANKABLE_ITEMS: IntArray = intArrayOf(
        PLANK, OAK_PLANK, TEAK_PLANK,
        MAHOGANY_PLANK, SOFT_CLAY, LIMESTONE_BRICKS, STEEL_BAR,
        BOLT_OF_CLOTH, GOLD_LEAF, MARBLE_BLOCK, MAGIC_STONE
    )

    val LAND: IntArray = intArrayOf(233, 632)

    // empty dungeon space
    val DUNGEON: IntArray =  /*{ 237, 632 }*/intArrayOf(235, 632)

    val BLACK: IntArray = intArrayOf(237, 632)

    val CHAIR_EMOTES: IntArray = intArrayOf(4073, 4075, 4079, 4081, 4083, 4085, 4087)

    val BENCH_EMOTES: IntArray = intArrayOf(4089, 4091, 4093, 4095, 4097, 4099, 4101)

    val THRONE_EMOTES: IntArray = intArrayOf(4111, 4112, 4113, 4114, 4115, 4116, 4117)

    @JvmStatic
    fun getSitAnimation(id: Int): Int = when (id) {
        in 13581..13587 -> CHAIR_EMOTES[id - 13581]
        in 13300..13306 -> BENCH_EMOTES[id - 13300]
        in 13665..13671 -> THRONE_EMOTES[id - 13665]
        in 13694..13696 -> when (id) {
            13694 -> 4097
            13695 -> 4099
            13696 -> 4101
            else -> -1
        }
        else -> -1
    }
}
