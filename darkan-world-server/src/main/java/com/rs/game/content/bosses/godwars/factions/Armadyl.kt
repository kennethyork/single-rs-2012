package com.rs.game.content.bosses.godwars.factions

import com.rs.game.World
import com.rs.game.World.checkWalkStep
import com.rs.game.content.bosses.godwars.GWDBoss
import com.rs.game.content.bosses.godwars.GWDFactionNPC
import com.rs.game.content.bosses.godwars.GWDMinion
import com.rs.game.content.bosses.godwars.factions.zaros.ZarosFactionNPC
import com.rs.game.content.combat.CombatStyle
import com.rs.game.content.combat.isMaging
import com.rs.game.content.combat.isRanging
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.npc.combat.CombatScript.delayHit
import com.rs.game.model.entity.npc.combat.CombatScript.getMaxHit
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.npcCombat
import com.rs.utils.WorldUtil
import java.util.*

@ServerStartupEvent
fun mapArmadylGodwars() {
    npcCombat(6222) kreeCombat@{ npc, target ->
        if (!npc.isUnderCombat) {
            npc.anim(6997)
            delayHit(npc, 1, target, Hit.melee(npc, getMaxHit(npc, 260, CombatStyle.MELEE, target)))
            return@kreeCombat npc.attackSpeed
        }
        npc.anim(6976)
        for (t in npc.possibleTargets) if (Utils.getRandomInclusive(2) == 0) {
            val p = World.sendProjectile(npc, t, 1198, 60 to 32, 50, 5, 0)
            delayHit(npc, p.taskDelay, t, Hit.magic(npc, getMaxHit(npc, 210, CombatStyle.MAGIC, t)))
            t.spotAnim(1196, p.taskDelay)
        } else {
            val p = World.sendProjectile(npc, t, 1197, 60 to 32, 50, 5, 0)
            delayHit(npc, p.taskDelay, t, Hit.range(npc, getMaxHit(npc, 720, CombatStyle.RANGE, t))) {
                val dir = WorldUtil.getDirectionTo(npc, target)
                if (dir != null) if (checkWalkStep(target.tile, dir, target.getSize())) {
                    target.resetWalkSteps()
                    target.tele(target.transform(dir.dx, dir.dy))
                }
            }
        }
        return@kreeCombat npc.attackSpeed
    }

    instantiateNpc(6228, 6229, 6230, 6231, 6232, 6233, 6234, 6235, 6236, 6237, 6238, 6239, 6240, 6241, 6242, 6243, 6244, 6245, 6246) { npcId, tile ->
        GWDFactionNPC(npcId, tile, false, GodFaction.ARMADYL)
    }

    instantiateNpc(6222) { npcId, tile -> KreeArra(npcId, tile, false) }
}

class KreeArra(id: Int, tile: Tile, spawned: Boolean) : GWDBoss(id, tile, spawned) {
    init {
        setMinions(
            GWDMinion(6223, tile.transform(8, 0), spawned),
            GWDMinion(6225, tile.transform(-4, -2), spawned),
            GWDMinion(6227, tile.transform(-2, -4), spawned)
        )
    }

    override fun canBeAttackedBy(player: Player): Boolean {
        if (!isRanging(player)) {
            player.sendMessage("Kree'arra is flying too high for you to attack using melee.")
            return false
        }
        return true
    }
}