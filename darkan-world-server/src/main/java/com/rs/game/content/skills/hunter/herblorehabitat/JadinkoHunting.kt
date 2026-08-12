package com.rs.game.content.skills.hunter.herblorehabitat

import com.rs.game.content.minigames.herblorehabitat.JadinkoType
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.captureJadinko
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.getAllActiveJadinkoTypes
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.isTrackerJadinko
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Animation
import com.rs.lib.game.Tile
import com.rs.lib.net.packets.encoders.HintTrail
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick
import java.util.*

private val NOOSE_WAND_ANIM = Animation(3297)
private const val HUNTER_TRAIL_MARKER = 19345
private val TRACK_OBJECT_IDS: IntArray = Arrays.stream(TrailObjects.entries.toTypedArray())
    .filter { o -> o.type == "PLANT" || o.type == "HOLE" }
    .mapToInt(TrailObjects::objectID)
    .toArray()

private val BUSH_IDS: IntArray = Arrays.stream(TrailObjects.entries.toTypedArray())
    .filter { o -> o.type == "BUSH" }
    .mapToInt(TrailObjects::objectID)
    .toArray()

@ServerStartupEvent
fun handleJadinkoClicks() {
    // Initial burrow clicks
    onObjectClick(56732, 56733) { (player, obj, option) ->
        if (option != "Inspect") return@onObjectClick
        val trackerTypes = player.getAllActiveJadinkoTypes().filter { it.isTrackerJadinko() }
        if (trackerTypes.isEmpty()) {
            player.sendMessage("You don't sense any Jadinko tracks nearby.")
            return@onObjectClick
        }
        val tracker = trackerTypes.first()
        val burrow = JadinkoTrailBuilder.buildTrail(tracker, obj)
        player.sendMessage("You sense a ${tracker.name.lowercase().replaceFirstChar(Char::uppercaseChar)} Jadinko nearby.")
        player.tempAttribs.removeO<Tile>("lastAdvancedTrack")
        startTrackingTrail(player, burrow)
    }

    // Tracking plants/holes
    onObjectClick(*TRACK_OBJECT_IDS.toTypedArray()) { (player, obj, option) ->
        if (option != "Inspect") {
            return@onObjectClick
        }
        val burrow = player.tempAttribs.getO<JadinkoBurrow>("currentTrail")
        if (burrow == null) {
            player.sendMessage("You search the ${obj.definitions.name} and find nothing.")
            return@onObjectClick
        }
        val lastAdv = player.tempAttribs.getO<Tile>("lastAdvancedTrack")
        if (lastAdv == obj.tile) {
            return@onObjectClick
        }
        val current = burrow.getNextTargetObject()
        if (current != null && burrow.isExpected(obj.id, obj.tile)) {
            burrow.advance()
            player.tempAttribs.setO<Tile>("lastAdvancedTrack", obj.tile)
            player.tempAttribs.removeO<Tile>("lastCheckedTrack")
            val next = burrow.getNextTargetObject()
            if (next != null) {
                startTrackingTrail(player, burrow)
                player.sendMessage("You find fresh tracks leading onward...")
            } else {
                player.sendMessage("You have successfully tracked the Jadinko! Check the bushes nearby.")
                player.session.writeToQueue(HintTrail(null, -1, null, null, -1))
                player.tempAttribs.setB("bushReady", true)
            }
            return@onObjectClick
        }
        if (current == null) {
            player.session.writeToQueue(HintTrail(null, -1, null, null, -1))
            player.tempAttribs.removeO<Any>("currentTrail")
            player.tempAttribs.removeO<Tile>("lastAdvancedTrack")
            player.tempAttribs.removeO<Tile>("lastCheckedTrack")
            return@onObjectClick
        }
        val lastChecked = player.tempAttribs.getO<Tile>("lastCheckedTrack")
        if (lastChecked != obj.tile) {
            player.sendMessage("You see no tracks.")
            player.tempAttribs.setO<Tile>("lastCheckedTrack", obj.tile)
        } else {
        }
    }

    // Bush interactions
    onObjectClick(*BUSH_IDS.toTypedArray()) { (player, obj, option) ->
        if (option != "Search" && option != "Attack") return@onObjectClick
        val burrow = player.tempAttribs.getO<JadinkoBurrow>("currentTrail") ?: return@onObjectClick
        if (obj.tile != burrow.endTile) {
            val lastWrong = player.tempAttribs.getO<Tile>("lastWrongBush")
            if (lastWrong != obj.tile) {
                player.sendMessage("You find nothing here.")
                player.tempAttribs.setO<Tile>("lastWrongBush", obj.tile)
            }
            return@onObjectClick
        }
        when (option) {
            "Search" -> {
                val nextTarget = burrow.getNextTargetObject()
                if (nextTarget != null && (nextTarget.objectId != obj.id || nextTarget.tile != obj.tile)) {
                    player.sendMessage("You need to follow the full trail before searching here.") //This spams the user
                    return@onObjectClick
                }
                if (!player.tempAttribs.getB("bushReady")) {
                    player.tempAttribs.setB("bushReady", true)
                    player.sendMessage("You suspect something is hiding here.")
                }
            }
            "Attack" -> {
                if (!player.tempAttribs.getB("bushReady")) {
                    player.sendMessage("You don’t see anything to attack.")
                    return@onObjectClick
                }
                player.tempAttribs.removeB("bushReady")
                player.anim(NOOSE_WAND_ANIM)
                val active = player.getAllActiveJadinkoTypes()
                if (active.isEmpty()) return@onObjectClick
                val type = active.first()
                player.inventory.addItem(type.drop, 1)
                player.sendMessage("You catch the ${type.name.lowercase().replaceFirstChar(Char::uppercaseChar)} jadinko!")
                val xp = when (type) {
                    JadinkoType.SHADOW -> 600.0
                    JadinkoType.DISEASED -> 950.0
                    JadinkoType.CAMOUFLAGED -> 975.0
                    else -> 0.0
                }
                player.skills.addXp(Skills.HUNTER, xp)
                player.captureJadinko(type)
                player.session.writeToQueue(HintTrail(null, -1, null, null, -1))
                player.tempAttribs.removeO<Any>("currentTrail")
            }
        }
    }
}

fun startTrackingTrail(player: Player, burrow: JadinkoBurrow) {
    if (player.skills.getLevel(Skills.HUNTER) < 1 || player.equipment.weaponId != 10150) {
        player.sendMessage("You need a noose wand and level 1 Hunter to track.")
        return
    }
    player.tempAttribs.setO<Tile>("currentTrail", burrow)
    player.tempAttribs.removeB("bushReady")
    val next = burrow.getNextTargetObject() ?: return
    val nextTile = next.tile
    player.session.writeToQueue(
        HintTrail(
            player.tile,
            HUNTER_TRAIL_MARKER,
            intArrayOf(nextTile.x().toInt()),
            intArrayOf(nextTile.y().toInt()),
            1
        )
    )
}

