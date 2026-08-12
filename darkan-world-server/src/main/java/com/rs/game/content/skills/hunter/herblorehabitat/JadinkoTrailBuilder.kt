package com.rs.game.content.skills.hunter.herblorehabitat

import com.rs.game.content.minigames.herblorehabitat.JadinkoType
import com.rs.game.model.gameobject.GameObject
import kotlin.random.Random

object JadinkoTrailBuilder {

    fun buildTrail(jadinkoType: JadinkoType, startBurrowObj: GameObject): JadinkoBurrow {
        val burrowEntry = TrailObjects.entries.first {
            it.type == "BURROW" && it.objectID == startBurrowObj.id && it.tile == startBurrowObj.tile
        }
        val plantPool  = TrailObjects.entries.filter { it.type == "PLANT" }
        val plantSteps = List(Random.nextInt(1, 4)) { plantPool.random() }
        val holeEntry  = TrailObjects.entries.filter { it.type == "HOLE" }.randomOrNull()
        val bushEntry  = TrailObjects.entries.filter { it.type == "BUSH" }.random()
        val bushTrail = JadinkoTrail(
            objectId = bushEntry.objectID,
            tile     = bushEntry.tile
        )
        val holeTrail = holeEntry?.let {
            JadinkoTrail(
                objectId = it.objectID,
                tile     = it.tile,
                nexts    = listOf(bushTrail)
            )
        } ?: bushTrail
        var head: JadinkoTrail = holeTrail
        for (plant in plantSteps.reversed()) {
            head = JadinkoTrail(
                objectId = plant.objectID,
                tile     = plant.tile,
                nexts    = listOf(head)
            )
        }
        return JadinkoBurrow(
            burrowId   = burrowEntry.objectID,
            burrowTile = burrowEntry.tile,
            current    = head,
            endTile    = bushEntry.tile
        )
    }
}
