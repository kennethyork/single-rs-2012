package com.rs.game.content.bosses.godwars.factions

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
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.instantiateNpc
import com.rs.plugin.kts.npcCombat
import java.util.*

private val graardorSpeech = arrayOf(
    "Death to our enemies!" to 3219,
    "Brargh!" to 3209,
    "Break their bones!" to null,
    "For the glory of Bandos!" to null,
    "Split their skulls!" to 3229,
    "We feast on the bones of our enemies tonight!" to 3206,
    "CHAAARGE!" to 3220,
    "Crush them underfoot!" to 3224,
    "All glory to Bandos!" to 3205,
    "GRAAAAAAAAAR!" to 3207,
    "FOR THE GLORY OF THE BIG HIGH WAR GOD!" to null
)

@ServerStartupEvent
fun mapBandosGodwars() {
    npcCombat(6260) graardorCombat@{ npc, target ->
        val defs = npc.combatDefinitions
        if (Utils.getRandomInclusive(3) == 0) {
            val (text, voiceEffect) = graardorSpeech.random()
            npc.forceTalk(text)
            voiceEffect?.let { npc.voiceEffect(target, it, true) }
        }
        if (Utils.getRandomInclusive(2) == 0) {
            npc.anim(7063)
            for (t in npc.possibleTargets) {
                val p = World.sendProjectile(npc, target, 1200, 60 to 32, 50, 5, 0)
                delayHit(npc, p.taskDelay, t, Hit.range(npc, getMaxHit(npc, 355, CombatStyle.RANGE, t)))
            }
        } else {
            npc.anim(defs.attackEmote)
            delayHit(npc, 0, target, Hit.melee(npc, getMaxHit(npc, defs.maxHit, CombatStyle.MELEE, target)))
        }
        return@graardorCombat npc.attackSpeed
    }

    instantiateNpc(6268, 6269, 6270, 6271, 6272, 6273, 6274, 6275, 6276, 6277, 6278, 6279, 6280, 6281, 6282, 6283) { npcId, tile ->
        GWDFactionNPC(npcId, tile, false, GodFaction.BANDOS)
    }

    instantiateNpc(6260) { npcId, tile -> GeneralGraardor(npcId, tile, false) }
}

class GeneralGraardor(id: Int, tile: Tile, spawned: Boolean) : GWDBoss(id, tile, spawned) {
    init {
        setMinions(
            GWDMinion(6261, tile.transform(-8, 0), spawned),
            GWDMinion(6263, tile.transform(0, -6), spawned),
            GWDMinion(6265, tile.transform(-4, 4), spawned)
        )
    }
}