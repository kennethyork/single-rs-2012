package com.rs.game.content.bosses.godwars.factions

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
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.npcCombat
import java.util.*

val zilyanaSpeech = arrayOf(
    "Death to the enemies of the light!" to 3247,
    "Slay the evil ones!" to 3242,
    "Saradomin lend me strength!" to 3263,
    "By the power of Saradomin!" to 3262,
    "May Saradomin be my sword." to 3251,
    "Good will always triumph!" to 3260,
    "Forward! Our allies are with us!" to 3245,
    "Saradomin is with us!" to 3266,
    "In the name of Saradomin!" to 3250,
    "Attack! Find the Godsword!" to 3258
)

@ServerStartupEvent
fun mapSaradominGodwars() {
    npcCombat(6247) zilyanaCombat@{ npc, target ->
        val defs = npc.combatDefinitions
        if (Utils.getRandomInclusive(3) == 0) {
            val (text, voiceEffect) = zilyanaSpeech.random()
            npc.forceTalk(text)
            npc.voiceEffect(target, voiceEffect, true)
        }
        if (Utils.getRandomInclusive(1) == 0) { // mage magical attack
            npc.anim(6967)
            for (t in npc.possibleTargets) {
                if (!t.withinDistance(npc.tile, 3)) continue
                val damage = getMaxHit(npc, defs.maxHit, CombatStyle.MAGIC, t)
                if (damage > 0) {
                    delayHit(npc, 1, t, Hit.magic(npc, damage))
                    t.spotAnim(1194)
                }
            }
        } else { // melee attack
            npc.anim(defs.attackEmote)
            delayHit(npc, 0, target, Hit.melee(npc, getMaxHit(npc, defs.maxHit, CombatStyle.MELEE, target)))
        }
        return@zilyanaCombat npc.attackSpeed
    }

    instantiateNpc(6254, 6255, 6256, 6257, 6258, 6259) { npcId, tile ->
        GWDFactionNPC(npcId, tile, false, GodFaction.SARADOMIN)
    }

    instantiateNpc(6247) { npcId, tile -> CommanderZilyana(npcId, tile, false) }
}

class CommanderZilyana(id: Int, tile: Tile, spawned: Boolean) : GWDBoss(id, tile, spawned) {
    init {
        setMinions(
            GWDMinion(6248, tile.transform(4, -4), spawned),
            GWDMinion(6250, tile.transform(0, -6), spawned),
            GWDMinion(6252, tile.transform(4, 2), spawned)
        )
    }
}