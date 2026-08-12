package com.rs.game.content.skills.thieving.roguesDen

import com.rs.cache.loaders.ItemDefinitions
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.game.model.gameobject.GameObject
import com.rs.game.tasks.WorldTasks
import com.rs.lib.Constants
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick
import java.util.Locale.getDefault

private const val REQUIRED_LEVEL = 50
private const val XP_REWARD = 70.0
private const val WALL_SAFE_ID = 7235
private const val STETHOSCOPE_ID = 5560
private const val ANIM_CRACKING = 2246
private const val ANIM_SPIKES = 615
private const val MIN_FAIL_DAMAGE = 20
private const val MAX_FAIL_DAMAGE = 60

private const val COINS = 995
private const val UNCUT_SAPPHIRE = 1623
private const val UNCUT_EMERALD = 1621
private const val UNCUT_RUBY = 1619

private fun rollSuccess(player: Player): Boolean {
    val level = player.skills.getLevel(Constants.THIEVING)
    val mul = player.auraManager.thievingMul
    return if (player.inventory.containsItem(STETHOSCOPE_ID))
        Utils.skillSuccess(level, mul, 16, 192)
    else
        Utils.skillSuccess(level, mul, 8, 160)
}

private fun giveLoot(player: Player) {
    val roll = Utils.random(128)
    val (itemId, quantity) = when {
        roll < 25  -> Pair(COINS, 10)
        roll < 81  -> Pair(COINS, 20)
        roll < 102 -> Pair(COINS, 40)
        roll < 114 -> Pair(UNCUT_SAPPHIRE, 1)
        roll < 123 -> Pair(UNCUT_EMERALD, 1)
        else -> Pair(UNCUT_RUBY, 1)
    }
    player.inventory.addItemDrop(itemId, quantity)
    player.sendMessage("You successfully cracked the safe and looted $quantity ${ItemDefinitions.getDefs(itemId).name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }}.")
}

private fun checkAll(player: Player): Boolean {
    if (player.isDead || player.hasFinished() || player.hasPendingHits()) return false
    if (player.skills.getLevel(Constants.THIEVING) < REQUIRED_LEVEL) {
        player.sendMessage("You need a Thieving level of $REQUIRED_LEVEL to crack this safe.")
        return false
    }
    if (player.attackedBy != null && player.inCombat()) {
        player.sendMessage("You can't do this while you're under combat.")
        return false
    }
    return true
}

private class WallSafeCrackAction(private val obj: GameObject) : PlayerAction() {
    private var success = false

    override fun start(player: Player): Boolean {
        if (!checkAll(player)) return false
        success = rollSuccess(player)
        player.faceObject(obj)
        player.sendMessage("You attempt to crack the safe.")
        if(player.inventory.containsItem(STETHOSCOPE_ID))
            WorldTasks.delay(0) { player.anim(ANIM_CRACKING + 1) }
        WorldTasks.delay(1) { player.anim(ANIM_CRACKING) }
        setActionDelay(player, 5)
        player.lock()
        return true
    }

    override fun process(player: Player): Boolean = checkAll(player)

    override fun processWithDelay(player: Player): Int {
        if (!success) {
            player.sendMessage("You fail to crack the safe and have activated a trap.")
            player.spotAnim(ANIM_SPIKES)
            val damage = Utils.random(MIN_FAIL_DAMAGE, MAX_FAIL_DAMAGE)
            player.applyHit(Hit(player, damage, Hit.HitLook.TRUE_DAMAGE))
            stop(player)
        } else {
            player.skills.addXp(Constants.THIEVING, XP_REWARD)
            giveLoot(player)
            stop(player)
        }
        return -1
    }

    override fun stop(player: Player) {
        player.unlock()
        setActionDelay(player, 1)
        if (!success) player.lock(4)
    }
}

@ServerStartupEvent
fun mapWallSafeCracking() {
    onObjectClick(WALL_SAFE_ID) { (player, obj) ->
        player.actionManager.setAction(WallSafeCrackAction(obj))
    }
    onItemOnObject(arrayOf(WALL_SAFE_ID), arrayOf(STETHOSCOPE_ID)) { (player, obj) ->
        player.actionManager.setAction(WallSafeCrackAction(obj))
    }
}