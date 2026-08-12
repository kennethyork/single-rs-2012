package com.rs.game.content.skills.magic.lunar

import com.rs.game.content.Effect
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.lib.game.Tile
import com.rs.utils.Ticks

class DreamAction(private val startTile: Tile) : PlayerAction() {
    companion object {
        private const val REGEN_AMOUNT = 10
        private const val INTERVAL = 20
    }

    private var timer = INTERVAL

    override fun start(player: Player): Boolean {
        if(!player.hasEffect(Effect.LUNARS_DREAM))
            return false
        player.schedule {
            wait(Ticks.fromSeconds(3))
            player.isResting = true
            player.anim(6296)
            player.appearance.setBAS(6296)
        }
        return true
    }

    override fun process(player: Player): Boolean {
        if (player.tile != startTile) return false
        if (player.inCombat(1) || player.hasBeenHit(1)) return false
        if (player.hitpoints >= player.maxHitpoints) return false
        player.spotAnim(1056)
        return true
    }

    override fun processWithDelay(player: Player): Int {
        timer--
        if (timer > 0) return 1
        timer = INTERVAL
        player.heal(REGEN_AMOUNT)
        return 1
    }

    override fun stop(player: Player) {
        player.isResting = false
        player.anim(6297)
        player.emotesManager.setNextEmoteEnd()
        player.appearance.setBAS(-1)
        player.removeEffect(Effect.LUNARS_DREAM)
    }
}