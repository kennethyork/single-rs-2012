package com.rs.game.content.bosses.godwars

import com.rs.game.content.bosses.godwars.factions.GodFaction
import com.rs.game.content.combat.isMaging
import com.rs.game.content.combat.isRanging
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile

open class GWDFactionNPC(id: Int, tile: Tile, spawned: Boolean, val faction: GodFaction) : NPC(id, tile, spawned) {
    init {
        isIgnoreDocile = true
        setCanAggroNPCs(true)
    }

    override fun canAggroNPC(npc: NPC) = npc !is GWDFactionNPC || npc.faction != faction
    override fun canAggroPlayer(player: Player) = !faction.isWearing(player)

    override fun canBeAttackedBy(player: Player): Boolean {
        if (faction == GodFaction.ARMADYL && !isRanging(player) && !isMaging(player)) {
            player.sendMessage("The Aviansie is flying too high for you to attack using melee.")
            return false
        }
        return true
    }
}