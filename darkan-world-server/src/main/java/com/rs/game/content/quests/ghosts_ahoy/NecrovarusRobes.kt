package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.model.gameobject.GameObject
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onObjectClick

private const val NECROVARUS_COFFIN = 5278
private const val NECROVARUS_COFFIN_OPEN = 5279
private const val NECROVARUS_ROBE =4247

@ServerStartupEvent
fun mapNecrovarusObjects() {
    onObjectClick(NECROVARUS_COFFIN) { e ->
        e.player.tasks.schedule(1) {
            val openCoffin = GameObject(
                e.obj.id + 1,
                e.obj.type,
                e.obj.rotation,
                e.obj.x,
                e.obj.y,
                e.obj.plane
            )
            World.spawnObjectTemporary(openCoffin, 20)
        }
    }
    onObjectClick(NECROVARUS_COFFIN_OPEN) { e ->
        if(!e.player.inventory.hasFreeSlots()) {
            e.player.sendMessage("You don't have the space in your inventory.")
            return@onObjectClick
        }
        if(e.player.inventory.containsItem(NECROVARUS_ROBE) || e.player.isQuestComplete(Quest.GHOSTS_AHOY) || e.player.questManager.getStage(Quest.GHOSTS_AHOY) >= GhostsAhoy.STAGE_6_AMULET_ENCHANTED)
            e.player.sendMessage("It's just the mortal remains of Necrovarus. You probably don't want to touch that.")
        else {
            e.player.inventory.addItem(NECROVARUS_ROBE)
            e.player.itemDialogue(NECROVARUS_ROBE, "You take the robes of Necrovarus from the remains of his mortal body.")
        }
    }
}