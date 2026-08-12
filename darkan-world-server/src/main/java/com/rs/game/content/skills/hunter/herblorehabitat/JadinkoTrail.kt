package com.rs.game.content.skills.hunter.herblorehabitat

import com.rs.lib.game.Tile

data class JadinkoTrail(
    val objectId: Int,
    val tile: Tile,
    val varbit: Int = 0,
    val value: Int = 0,
    val nexts: List<JadinkoTrail> = emptyList()
) {
    fun hasNext(): Boolean = nexts.isNotEmpty()

    fun nextFirstOrNull(): JadinkoTrail? = nexts.firstOrNull()

    fun allTiles(): List<Tile> {
        val tiles = mutableListOf<Tile>()
        var current: JadinkoTrail? = this
        while (current != null) {
            tiles.add(current.tile)
            current = current.nextFirstOrNull()
        }
        return tiles
    }

    override fun toString(): String =
        "JadinkoTrail(id=$objectId, tile=$tile, nexts=${nexts.size})"
}
