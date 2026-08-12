package com.rs.game.content.skills.hunter.herblorehabitat

import com.rs.lib.game.Tile

class JadinkoBurrow(
    val burrowId: Int,
    val burrowTile: Tile,
    var current: JadinkoTrail?,
    val endTile: Tile
) {
    fun matchesStart(objId: Int, tile: Tile): Boolean =
        objId == burrowId && tile == burrowTile

    fun getNextTargetObject(): JadinkoTrail? = current

    fun isExpected(objectId: Int, tile: Tile): Boolean =
        current?.let { it.objectId == objectId && it.tile == tile } == true

    fun advance() {
        current = current?.nextFirstOrNull()
    }

    override fun toString(): String =
        "JadinkoBurrow(id=$burrowId, start=$burrowTile, end=$endTile)"
}