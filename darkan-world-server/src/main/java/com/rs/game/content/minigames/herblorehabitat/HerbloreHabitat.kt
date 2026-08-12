package com.rs.game.content.minigames.herblorehabitat

import com.rs.engine.pathfinder.Direction
import com.rs.game.World.getPlayersInChunkRange
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.godJadinkos
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.updateGroup
import com.rs.game.content.skills.agility.Agility
import com.rs.game.content.skills.farming.ProduceType
import com.rs.game.content.skills.woodcutting.Hatchet
import com.rs.game.content.skills.woodcutting.TreeType
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick
import com.rs.engine.variables.jaro_shortcut


@ServerStartupEvent
fun initUpdateTask() {
    onLogin { (player) ->
        player.tasks.scheduleLooping(0, 10) {
            if (player in getPlayersInChunkRange(Tile.of(2959, 2915, 0).chunkId, 5)) {
                if (player.hasStarted() && !player.hasFinished()) {
                    updateGroup(player, *JadinkoType.entries.toTypedArray())
                }
            }
        }
    }
}

private val godBushOptions = listOf(ProduceType.Lergberry, ProduceType.Kalferberry, null)
private val godTreeOptions = listOf(ProduceType.Apple, ProduceType.Orange, ProduceType.Banana, null)
private val godFeatureOptions = HabitatFeature.entries + listOf(null)

@ServerStartupEvent
fun mapWeeklyJadinkoTask() {
    onLogin { (player) ->
        if (player.weeklyAttributes["god_jadinkos_set"] == null) {
            godJadinkos.forEach { god ->
                val keyBase = "god_jadinko_${god.name.lowercase()}"
                val blossom = god.blossom
                val bush = godBushOptions.random()
                val tree = godTreeOptions.random()
                val feature = godFeatureOptions.random()
                player.setWeeklyI("${keyBase}_bush", bush?.ordinal ?: -1)
                player.setWeeklyI("${keyBase}_tree", tree?.ordinal ?: -1)
                player.setWeeklyI("${keyBase}_feature", feature?.ordinal ?: -1)
                player.weeklyAttributes["god_jadinkos_set"] = 1
                Logger.info(JadinkoType::class.java, "onLogin", "${player.username}: Set $god -> blossom=${blossom?.name}, bush=${bush?.name}, tree=${tree?.name}, feature=${feature?.name}")
            }
        }
    }
}

@ServerStartupEvent
fun handleRootTunnel() {
    onObjectClick(12321) { e ->
        when(e.option) {
            "Chop-roots" -> chop(e.player, e.obj)
            "Enter" -> e.player.useLadder(Tile.of(2946,2886,0))
        }
    }
    onObjectClick(12322) { e ->
        e.player.useLadder(Tile.of(3024,9224,0))
    }
}

@ServerStartupEvent
fun handleAgilityVine() {
    //West Up
    data class AgilityObstacle(
        val objectId: Int,
        val tile: Tile,
        val animationId: Int,
        val level: Int = 1,
        val destination: Tile,
        val delay: Double = 0.0
    )

    val agilityObstacles = listOf(
        // West
        AgilityObstacle(27126, Tile.of(2963, 2904, 0), 3303, destination = Tile.of(2964, 2904, 1)),
        AgilityObstacle(27130, Tile.of(2963, 2904, 1), 3303, destination = Tile.of(2962, 2904, 0)),
        AgilityObstacle(27151, Tile.of(2966, 2903, 1), 3533, destination = Tile.of(2966, 2903, 2)),
        AgilityObstacle(27129, Tile.of(2966, 2903, 2), 3527, destination = Tile.of(2965, 2903, 1)),

        // East
        AgilityObstacle(27126, Tile.of(2977, 2906, 0), 3303, destination = Tile.of(2976, 2906, 1)),
        AgilityObstacle(27130, Tile.of(2977, 2906, 1), 3303, destination = Tile.of(2978, 2906, 0)),
        AgilityObstacle(27151, Tile.of(2973, 2903, 1), 3533, destination = Tile.of(2973, 2903, 2)),
        AgilityObstacle(27129, Tile.of(2973, 2903, 2), 3527, destination = Tile.of(2974, 2903, 1)),
    )

    agilityObstacles.forEach { obstacle ->
        onObjectClick(obstacle.objectId, tiles = arrayOf(obstacle.tile)) { e ->
            Agility.handleObstacle(e.player, obstacle.animationId, obstacle.level, obstacle.destination, obstacle.delay)
        }
    }

    onObjectClick(56840) { (player, obj) ->
        if (!Utils.skillSuccess(player.skills.getLevel(Skills.AGILITY), 185, 255)) {
            player.anim(15545) // TODO: Not authentic
        } else {
            player.forceMove(
                Tile.of(if (player.x > obj.x) 2967 else 2972, 2903, 2),
                if (player.x < obj.x) Direction.EAST else Direction.WEST,
                14554,
                0,
                100
            )
        }
    }
}

private fun chop(player: Player, obj: GameObject) {
    if (player.skills.getLevel(Skills.WOODCUTTING) < 40) {
        player.sendMessage("You need a woodcutting level of 40 to chop this tree.")
        return
    }

    player.actionManager.setAction(object : PlayerAction() {
        val hatchet = Hatchet.getBest(player)

        override fun start(player: Player): Boolean {
            player.actionManager.actionDelay = 3
            player.faceObject(obj)
            if (hatchet == null) {
                player.sendMessage("You do not have a hatchet that you have the woodcutting level to use.")
                return false
            }
            return true
        }

        override fun process(player: Player): Boolean {
            player.faceObject(obj)
            player.anim(hatchet.getAnim(TreeType.NORMAL))
            return true
        }

        override fun processWithDelay(player: Player): Int {
            player.vars.saveVarBit(jaro_shortcut, 1)
            player.anim(-1)
            return -1
        }

        override fun stop(player: Player) {
            player.anim(-1)
        }
    })
}


