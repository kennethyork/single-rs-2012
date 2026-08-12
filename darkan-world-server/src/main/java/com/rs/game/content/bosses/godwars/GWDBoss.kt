package com.rs.game.content.bosses.godwars

import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Tile

open class GWDBoss(id: Int, tile: Tile, spawned: Boolean) : NPC(id, tile, spawned) {
    private var minions: List<GWDMinion>? = null

    init {
        isIntelligentRouteFinder = true
        isIgnoreDocile = true
        setForceAggroDistance(64)
    }

    fun setMinions(vararg minions: GWDMinion) {
        this.minions = minions.toList()
    }

    override fun onRespawn() {
        respawnMinions()
    }

    fun respawnMinions() {
        schedule {
            if (minions != null) {
                for (minion in minions) if (minion.hasFinished() == true || minion.isDead == true)
                    minion.setRespawnTask(2)
            }
        }
    }
}