// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.skills.slayer.npcs.combat

import com.rs.game.content.combat.CombatStyle
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.npc.combat.CombatScript

class SpiritualMage : CombatScript() {
    override fun getKeys() = arrayOf<Any>(6257, 6221, 6278)

    override fun attack(npc: NPC, target: Entity): Int {
        val defs = npc.combatDefinitions
        npc.anim(defs.attackEmote)
        val damage = getMaxHit(npc, defs.maxHit, CombatStyle.MAGIC, target)
        npc.spotAnim(defs.attackGfx)
        delayHit(npc, 2, target, Hit.magic(npc, damage))
        if (damage > 0)
            npc.schedule {
                wait(2)
                target.spotAnim(defs.attackProjectile)
            }
        return npc.attackSpeed + 2
    }
}
