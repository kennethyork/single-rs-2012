package com.rs.game.content.minigames.lividfarm.npcs

import com.rs.game.content.minigames.lividfarm.LividFarmController
import com.rs.game.content.skills.magic.Magic
import com.rs.game.content.skills.magic.Rune
import com.rs.game.content.skills.magic.RuneSet
import com.rs.lib.game.SpotAnim
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun initIbisExamineSpell() {
    onNpcClick(LividFarmController.Ibis, checkDistance = false) { e ->
        if (!e.player.tempAttribs.getB("LividFarm.meteoraIbis.IbisPrompted") || e.player.tempAttribs.getB("LividFarm.meteoraIbis.Claimed")) {
            e.player.sendMessage("You feel no particular need to examine this creature.")
            return@onNpcClick
        }
        if (e.player.tempAttribs.getB("LividFarm.meteoraIbis.IbisExamined")) {
            e.player.sendMessage("The Ibis is suffering from ${LividFarmController.activeAilment}")
            return@onNpcClick
        }
        if (!e.player.canCastSpell() || !Magic.checkMagicAndRunes(e.player, 1, false, RuneSet(Rune.ASTRAL, 1, Rune.MIND, 1, Rune.COSMIC, 1))) {
            return@onNpcClick
        }
        Magic.checkMagicAndRunes(e.player, 1, true, RuneSet(Rune.ASTRAL, 1, Rune.MIND, 1, Rune.COSMIC, 1))
        e.player.sendMessage("You examine the ibis closely...")
        e.player.spotAnim(SpotAnim(729, 0, 100))
        e.player.anim(4411)
        e.player.sendMessage("The Ibis is suffering from ${LividFarmController.activeAilment}")
        e.player.tempAttribs.setB("IbisExamined", true)
    }
}
