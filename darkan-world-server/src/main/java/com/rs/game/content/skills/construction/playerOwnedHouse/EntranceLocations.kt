package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.lib.game.Tile

enum class EntranceLocations(val objectId: Int, val tile: Tile, val levelRequired: Int, val cost: Int) {
    RIMMINGTON(15478, Tile.of(2953, 3224, 0), 1, 5000),

    TAVERLY(15477, Tile.of(2882, 3452, 0), 1, 5000),

    POLLNIVNEACH(15479, Tile.of(3340, 3003, 0), 20, 7500),

    RELLEKKA(15480, Tile.of(2670, 3631, 0), 30, 10000),

    BRIMHAVEN(15481, Tile.of(2757, 3178, 0), 40, 15000),

    YANILLE(15482, Tile.of(2544, 3096, 0), 50, 25000)
}
