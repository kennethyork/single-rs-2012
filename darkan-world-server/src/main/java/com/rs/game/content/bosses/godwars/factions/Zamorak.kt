package com.rs.game.content.bosses.godwars.factions

import com.rs.engine.pathfinder.RouteEvent
import com.rs.game.World
import com.rs.game.content.bosses.godwars.GWDBoss
import com.rs.game.content.bosses.godwars.GWDFactionNPC
import com.rs.game.content.bosses.godwars.GWDMinion
import com.rs.game.content.bosses.godwars.factions.zaros.ZarosFactionNPC
import com.rs.game.content.combat.CombatStyle
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.npc.combat.CombatScript.delayHit
import com.rs.game.model.entity.npc.combat.CombatScript.getMaxHit
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils.getRandomInclusive
import com.rs.lib.util.Utils.random
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.npcCombat
import com.rs.plugin.kts.onObjectClick
import java.util.*

private val krilSpeech = arrayOf(
    "Attack them, you dogs!",
    "Forward!",
    "Death to Saradomin's dogs!",
    "Kill them, you cowards!",
    "The Dark One will have their souls!",
    "Zamorak curse them!",
    "Rend them limb from limb!",
    "No retreat!",
    "Flay them all!"
)

@ServerStartupEvent
fun mapZamorakGodwars() {
    onObjectClick(26439, checkDistance = false) { (player, obj) ->
        player.setRouteEvent(RouteEvent(Tile.of(obj.getTile())) {
            if (player.withinDistance(obj.getTile(), 3)) {
                if (player.y < 5334) {
                    if (player.skills.getLevel(Constants.HITPOINTS) >= 70) {
                        player.useStairs(6999, Tile.of(2885, 5347, 2), 1, 1)
                        player.prayer.drainPrayer(player.prayer.points)
                        player.sendMessage("You jump over the broken bridge. You feel the power of Zamorak take sap away at your prayer points.")
                    } else player.sendMessage("You need a Constitution level of 70 to enter this area.")
                } else player.useStairs(6999, Tile.of(2885, 5330, 2), 1, 1)
            }
        })
    }

    npcCombat(6203) { npc, target ->
        if (random(4) == 0)
            npc.forceTalk(krilSpeech.random())
        val attackStyle = getRandomInclusive(2)
        when (attackStyle) {
            0 -> {
                npc.sync(14962, 1210)
                for (t in npc.possibleTargets) {
                    delayHit(npc, 1, t, Hit.magic(npc, getMaxHit(npc, 300, CombatStyle.MAGIC, t)))
                    World.sendProjectile(npc, t, 1211, 41 to 16, 41, 5, 16)
                    if (getRandomInclusive(4) == 0) t.poison.makePoisoned(168)
                }
            }

            1, 2 -> {
                var damage = 300
                for (e in npc.possibleTargets) {
                    if (e is Player && e.prayer.isProtectingMelee && random(10) == 0) {
                        damage = 497
                        npc.forceTalk("YARRRRRRR!")
                        e.prayer.drainPrayer(e.prayer.points / 2)
                        e.sendMessage("K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.")
                    }
                    npc.anim(if (damage <= 463) 14963 else 14968)
                    if (damage <= 463) delayHit(npc, 0, e, Hit.melee(npc, getMaxHit(npc, damage, CombatStyle.MELEE, e)))
                    else delayHit(npc, 0, e, Hit.flat(npc, getMaxHit(npc, damage, CombatStyle.MELEE, e)))
                }
            }
        }
        return@npcCombat npc.attackSpeed
    }

    instantiateNpc(6210, 6211, 6212, 6213, 6214, 6215, 6216, 6217, 6218, 6219, 6220, 6221) { npcId, tile ->
        GWDFactionNPC(npcId, tile, false, GodFaction.ZAMORAK)
    }

    instantiateNpc(6203) { npcId, tile ->
        KrilTstsaroth(npcId, tile, false)
    }
}

class KrilTstsaroth(id: Int, tile: Tile, spawned: Boolean) : GWDBoss(id, tile, spawned) {
    init {
        setMinions(
            GWDMinion(6204, tile.transform(8, 4), true),
            GWDMinion(6206, tile.transform(-6, 6), true),
            GWDMinion(6208, tile.transform(-6, -2), true)
        )
    }
}