package com.rs.game.content.skills.firemaking

import com.rs.game.model.entity.TimerBar
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.OwnedNPC
import com.rs.game.model.entity.player.Player
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.Ticks

class FireSpirit(tile: Tile, target: Player) : OwnedNPC(target, 15451, tile, true) {

    private var life = Ticks.fromMinutes(1)

    init {
        anim(15629)
        nextHitBars.add(TimerBar(life * 30))
    }

    override fun processNPC() {
        super.processNPC()
        if (life-- <= 0) {
            schedule {
                anim(15627)
                wait(2)
                finish()
            }
        }
    }

    override fun sendDrop(player: Player, item: Item) {
        player.inventory.addItemDrop(item)
    }

    fun giveReward(player: Player) {
        if (player != owner || player.isLocked) return
        player.lock()
        player.anim(16705)
        WorldTasks.schedule(2) {
            player.unlock()
            player.incrementCount("Fire spirits set free")
            drop(player, false)
            repeat(5) {
                if (Utils.random(100) < 50) drop(player, false)
            }
            player.sendMessage("The fire spirit gives you a reward to say thank you for freeing it, before disappearing.", true)
            schedule {
                anim(15625)
                wait(3)
                finish()
            }
        }
    }
}

@ServerStartupEvent
fun mapFireSpiritInteraction() {
    onNpcClick(15451) { (player, npc) -> if (npc is FireSpirit) npc.giveReward(player); }
}
