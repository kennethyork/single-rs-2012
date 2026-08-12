package com.rs.game.content.skills.magic.lunar

import com.rs.game.content.AchievementTitles
import com.rs.game.content.skills.farming.PatchLocation
import com.rs.game.content.skills.magic.Magic
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.game.SpotAnim
import com.rs.lib.net.ClientPacket
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import java.util.*

private const val REMOTE_FARM_INTERFACE: Int = 1082

private val COMPONENT_IDS: IntArray = intArrayOf(
    30, 32, 34, 36, 38, 49, 51, 53, 55, 57, 59, 62, 64, 66, 68, 70, 72, 74, 76,
    190, 79, 81, 83, 85, 88, 90, 92, 94, 97, 99, 101, 104, 106, 108, 110, 115, 117,
    119, 121, 123, 125, 131, 127, 129, 2, 173, 175, 177, 182, 184, 186, 188
)

@ServerStartupEvent
fun registerRemoteFarmButtons() {
    onButtonClick(REMOTE_FARM_INTERFACE) { e ->
        if (e.packet != ClientPacket.IF_OP1) return@onButtonClick

        val player = e.player
        if (!player.tempAttribs.getB("RemoteFarm")) {
            AchievementTitles.handleButtons(player, e.componentId)
            return@onButtonClick
        }

        val clickedComponent = e.componentId
        for (i in COMPONENT_IDS.indices) {
            if (COMPONENT_IDS[i] + 1 == clickedComponent) {
                val location = PatchLocation.entries.getOrNull(i) ?: continue
                val patch = player.getPatch(location)
                if (patch != null) {
                    if (patch.diseased) {
                        patch.diseased = false
                        refreshRemoteFarm(player)
                    } else {
                        player.sendMessage("This patch isn't diseased.")
                    }
                }
                break
            }
        }
    }
}

fun openRemoteFarm(player: Player) {
    if (!player.canCastSpell()) {
        player.sendMessage("There is a 5 second delay to this.")
        return
    }
    player.anim(529)
    player.spotAnim(SpotAnim(748, 0, 100))
    player.addSpellDelay(10)
    LunarSpell.REMOTE_FARM.xp?.let { player.skills.addXp(Constants.MAGIC, it) }
//    Magic.triggerSpellCastEvent(player, LunarSpell.REMOTE_FARM.name)
    player.tempAttribs.setB("RemoteFarm", true)
    player.interfaceManager.sendInterface(REMOTE_FARM_INTERFACE)
    refreshRemoteFarm(player)
}

fun refreshRemoteFarm(player: Player) {
    if (!player.tempAttribs.getB("RemoteFarm")) return

    val patchLocations = PatchLocation.entries.toTypedArray()

    for (i in COMPONENT_IDS.indices) {
        val name = if (i < patchLocations.size)
            patchLocations[i].name.replace("_", " ").lowercase(Locale.getDefault())
        else ""
        player.packets.setIFText(REMOTE_FARM_INTERFACE, COMPONENT_IDS[i], name)
    }

    for (i in COMPONENT_IDS.indices) {
        val location = patchLocations.getOrNull(i)
        if (location == null) {
            player.packets.setIFText(REMOTE_FARM_INTERFACE, COMPONENT_IDS[i] + 1, "")
            continue
        }

        val patch = player.getPatch(location)
        val status = when {
            patch == null -> ""
            patch.weeds >= 3 -> "Full of weeds"
            patch.dead -> "<col=8f13b5>Is dead!"
            patch.diseased -> "<col=FF0000>Is diseased!"
            patch.checkedHealth && patch.seed != null && patch.fullyGrown() -> "<col=00FF00>Is ready for health check"
            patch.fullyGrown() -> "<col=00FF00>Is fully grown"
            patch.seed != null -> "Is growing healthy."
            else -> "Is empty"
        }
        player.packets.setIFText(REMOTE_FARM_INTERFACE, COMPONENT_IDS[i] + 1, status)
    }
}
