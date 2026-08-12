package com.rs.game.content.skills.hunter.herblorehabitat

import com.rs.lib.game.Tile

enum class TrailObjects (
    val objectID: Int,
    val tile: Tile,
    val type: String,
    val varbit: Int? = null
) {
    HOLE1(56713, Tile.of(2945,2888,0), "HOLE"),
    HOLE2(56715, Tile.of(2964,2903,0), "HOLE"),
    HOLE3(56714, Tile.of(2950,2898,0), "HOLE"),

    BURROW1(56733, Tile.of(2963,2900,0), "BURROW"),
    BURROW2(56732, Tile.of(2948,2888,0), "BURROW"),

    BUSH1(56735, Tile.of(2953,2898,0), "BUSH"),
    BUSH2(56735, Tile.of(2959,2900,0), "BUSH"),
    BUSH3(56735, Tile.of(2962,2900,0), "BUSH"),
    BUSH4(56735, Tile.of(2957,2889,0), "BUSH"),


    JUNGLEPLANT1(56731, Tile.of(2949,2896,0), "PLANT"),
    JUNGLEPLANT2(56731, Tile.of(2954,2896,0),"PLANT"),
    JUNGLEPLANT3(56730, Tile.of(2956,2898,0),"PLANT"),
    JUNGLEPLANT4(56721, Tile.of(2958,2898,0),"PLANT"),
    JUNGLEPLANT5(56730, Tile.of(2965,2901,0),"PLANT"),
    JUNGLEPLANT6(56731, Tile.of(2965,2899,0),"PLANT"),
    JUNGLEPLANT7(56731, Tile.of(2967,2900,0),"PLANT"),
    JUNGLEPLANT8(56718, Tile.of(2967,2898,0),"PLANT"),
    JUNGLEPLANT9(56725, Tile.of(2964,2895,0),"PLANT"),
    JUNGLEPLANT10(56731, Tile.of(2962,2894,0),"PLANT"),
    JUNGLEPLANT11(56727, Tile.of(2960,2892,0),"PLANT"),
    JUNGLEPLANT12(56730, Tile.of(2958,2894,0),"PLANT"),
    JUNGLEPLANT13(56720, Tile.of(2956,2892,0),"PLANT"),
    JUNGLEPLANT14(56731, Tile.of(2953,2888,0),"PLANT"),
    JUNGLEPLANT15(56731, Tile.of(2952,2890,0),"PLANT"),
    JUNGLEPLANT16(56731, Tile.of(2950,2889,0),"PLANT"),;
}
