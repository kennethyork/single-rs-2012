package com.rs.game.content.world

import com.rs.game.content.quests.familycrest.PERFECT_BAR
import com.rs.game.content.quests.familycrest.PERFECT_ORE
import com.rs.game.content.quests.familycrest.familyCrestItemOnFurnace
import com.rs.game.content.skills.crafting.GOLD_BAR
import com.rs.game.content.skills.crafting.Silver
import com.rs.game.content.skills.crafting.Silver.SILVER_BAR
import com.rs.game.content.skills.crafting.openInterface
import com.rs.game.content.skills.smithing.MoltenGlassMaking
import com.rs.game.content.skills.smithing.SmeltingD
import com.rs.game.model.entity.player.Player
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun mapFurnaces() {
    onObjectClick("Furnace", "Small furnace", "Clay forge", "Lava Furnace", 11666) { e -> use(e.player, e.obj) }
    onItemOnObject(arrayOf("Furnace", "Small furnace", "Clay forge", "Lava Furnace", 11666), arrayOf(GOLD_BAR, SILVER_BAR)) { e -> useItem(e.player, e.`object`, e.item) }
    onItemOnObject(arrayOf("Furnace", "Small furnace", "Clay forge", "Lava Furnace", 11666), arrayOf(PERFECT_ORE, PERFECT_BAR)) { e -> familyCrestItemOnFurnace(e.player, e.item) }
}


private fun use(player: Player, furnace: GameObject?) {
    if (player.inventory.containsItems(Item(MoltenGlassMaking.SODA_ASH), Item(MoltenGlassMaking.BUCKET_OF_SAND))) {
        MoltenGlassMaking.openDialogue(player)
        return
    }
    player.startConversation(SmeltingD(player, furnace))
}

private fun useItem(player: Player, furnace: GameObject, item: Item) {
    if (item.id == GOLD_BAR) {
        openInterface(player, false)
        player.tempAttribs.setO<Any>("jewelryObject", furnace)
        return
    }

    if (item.id == SILVER_BAR) {
        Silver.openSilverInterface(player)
        player.tempAttribs.setO<Any>("silverObject", furnace)
        return
    }
}