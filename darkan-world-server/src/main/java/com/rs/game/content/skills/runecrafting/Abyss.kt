package com.rs.game.content.skills.runecrafting

import com.rs.game.World
import com.rs.game.content.skills.mining.Pickaxe
import com.rs.game.content.skills.woodcutting.Hatchet
import com.rs.game.model.entity.Teleport
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils.random
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

private val ABYSS_TELEPORT_OUTER = arrayOf<IntArray>(
    intArrayOf(3059, 4817), intArrayOf(3062, 4812), intArrayOf(3052, 4810), intArrayOf(3041, 4807), intArrayOf(3035, 4811), intArrayOf(3030, 4808), intArrayOf(3026, 4810), intArrayOf(3021, 4811),
    intArrayOf(3015, 4810), intArrayOf(3020, 4818), intArrayOf(3018, 4819), intArrayOf(3016, 4824), intArrayOf(3013, 4827), intArrayOf(3017, 4828), intArrayOf(3015, 4837), intArrayOf(3017, 4843),
    intArrayOf(3014, 4849), intArrayOf(3021, 4847), intArrayOf(3022, 4852), intArrayOf(3027, 4849), intArrayOf(3031, 4856), intArrayOf(3035, 4854), intArrayOf(3043, 4855), intArrayOf(3045, 4852),
    intArrayOf(3050, 4857), intArrayOf(3054, 4855), intArrayOf(3055, 4848), intArrayOf(3060, 4848), intArrayOf(3059, 4844), intArrayOf(3065, 4841), intArrayOf(3061, 4836), intArrayOf(3063, 4832),
    intArrayOf(3064, 4828), intArrayOf(3060, 4824), intArrayOf(3063, 4821), intArrayOf(3041, 4808), intArrayOf(3030, 4810), intArrayOf(3018, 4816), intArrayOf(3015, 4829), intArrayOf(3017, 4840),
    intArrayOf(3020, 4849), intArrayOf(3031, 4855), intArrayOf(3020, 4854), intArrayOf(3035, 4855), intArrayOf(3047, 4854), intArrayOf(3060, 4846), intArrayOf(3062, 4836), intArrayOf(3060, 4828),
    intArrayOf(3063, 4820), intArrayOf(3028, 4806)
)

@ServerStartupEvent
fun mapAbyssObjects() {
    onObjectClick(7137, 7139, 7140, 7131, 7130, 7129, 7136, 7135, 7133, 7132, 7141, 7134, 7138) { e ->
        when (e.objectId) {
            7137 -> RunecraftingAltar.Ruins.WATER.canEnter(e.player, true)
            7139 -> RunecraftingAltar.Ruins.AIR.canEnter(e.player, true)
            7140 -> RunecraftingAltar.Ruins.MIND.canEnter(e.player, true)
            7131 -> RunecraftingAltar.Ruins.BODY.canEnter(e.player, true)
            7130 -> RunecraftingAltar.Ruins.EARTH.canEnter(e.player, true)
            7129 -> RunecraftingAltar.Ruins.FIRE.canEnter(e.player, true)
            7136 -> RunecraftingAltar.Ruins.DEATH.canEnter(e.player, true)
            7135 -> RunecraftingAltar.Ruins.LAW.canEnter(e.player, true)
            7133 -> RunecraftingAltar.Ruins.NATURE.canEnter(e.player, true)
            7132 -> RunecraftingAltar.Ruins.COSMIC.canEnter(e.player, true)
            7141 -> RunecraftingAltar.Ruins.BLOOD.canEnter(e.player, true)
            7134 -> RunecraftingAltar.Ruins.CHAOS.canEnter(e.player, true)
            7138 -> e.player.sendMessage("A strange power blocks your exit..")
        }
    }

    onObjectClick(7142, 7143, 7144, 7145, 7146, 7147, 7148, 7149, 7150, 7152, 7153) { e ->
        e.player.clearObstacle(when(e.option) {
            "Burn-down" -> Skills.FIREMAKING
            "Distract" -> Skills.THIEVING
            "Squeeze-through" -> Skills.AGILITY
            "Chop" -> Skills.WOODCUTTING
            "Mine" -> Skills.MINING
            else -> -1
        }, e.obj)
    }
}

private fun Player.clearObstacle(skill: Int, obj: GameObject) {
    if (skill != -1 && skills.getLevel(skill) < 30) return sendMessage("You need 30 ${Skills.SKILL_NAME[skill]} to pass this obstacle.")
    val animId = when(skill) {
        Skills.MINING -> {
            val pick = Pickaxe.getBest(this) ?: return sendMessage("You need a pickaxe in order to clear this obstacle.")
            pick.animId
        }
        Skills.WOODCUTTING -> {
            val hatchet = Hatchet.getBest(this) ?: return sendMessage("You need a hatchet in order to clear this obstacle.")
            hatchet.animNormal()
        }
        Skills.THIEVING -> 866
        Skills.AGILITY, -1 -> 844
        Skills.FIREMAKING -> 733
        else -> -1
    }
    lock()
    schedule {
        faceObject(obj)
        wait(1)
        anim(animId)
        wait(2)
        val success = if (skill == -1) true else (skills.getLevel(skill).toDouble() / 99.0) > Math.random()
        if (!success) {
            unlock()
            anim(-1)
            return@schedule
        }
        if (skill != -1 && skill != Skills.AGILITY) {
            vars.setVarBit(
                625, when (skill) {
                    Skills.MINING -> 12
                    Skills.WOODCUTTING -> 14
                    Skills.FIREMAKING -> 16
                    Skills.THIEVING -> 18
                    else -> 0
                }
            )
            wait(2)
            vars.setVarBit(625, vars.getVarBit(625) + 1)
            wait(2)
        }
        val closestTeleTile = World.getClosestObjectByObjectId(7181, tile)?.tile ?: Tile.of(3040, 4844, 0)
        tele(closestTeleTile)
        resetReceivedHits()
        unlockNextTick()
        anim(-1)
        vars.setVarBit(625, random(10))
    }
}

fun teleportRCAbyss(player: Player, npc: NPC) {
    player.lock()
    npc.resetWalkSteps()
    npc.faceEntityTile(player)
    npc.forceTalk("Veniens! Sallkar! Rinnesset!")
    npc.anim(722)
    npc.spotAnim(343)
    player.spotAnim(342)
    player.schedule {
        wait(3)
        val randomTile = ABYSS_TELEPORT_OUTER.random()
        player.tele(Tile.of(randomTile[0], randomTile[1], 0))
        Teleport.checkDestinationControllers(player, Tile.of(randomTile[0], randomTile[1], 0))
        player.prayer.drainPrayer(player.prayer.points)
        player.setWildernessSkull()
        player.vars.setVarBit(625, random(10))
        player.unlockNextTick()
    }
}