package com.rs.game.content.world.areas.fremennik_isles

import com.rs.game.ge.GE
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.shop.ShopsHandler


fun getFremennikName(player: Player): String {
    if (player.getSavingAttributes()["FremennikName"] == null) {
        setFremennikName(player)
    }
    return player.getSavingAttributes()["FremennikName"].toString()
}

fun setFremennikName(player: Player) {
    val prefix = listOf(
        "Bal", "Bar", "Dal", "Dar",
        "Den", "Dok", "Jar", "Jik",
        "Lar", "Rak", "Ral", "Ril",
        "Sig", "Tal", "Thor", "Ton"
    )
    val suffix = listOf(
        "dar", "dor", "dur", "kal",
        "kar", "kir", "kur", "lah",
        "lak", "lim", "lor", "rak",
        "tin", "ton", "tor", "vald"
    )
    val name = prefix[Utils.random(prefix.size)] + suffix[Utils.random(suffix.size)]
    player.getSavingAttributes()["FremennikName"] = name
}

@ServerStartupEvent
fun mapFremennikHandlers() {
    //JatizsoUp
    onObjectClick(21395) { e ->
        e.player.useLadder(828, e.player.transform(0, 0, 2))
    }
    //JatizsoDown
    onObjectClick(21396) { e ->
        e.player.useLadder(828, e.player.transform(0, 0, -2))
    }
    //MiningCaveDown
    onObjectClick(21455) { e ->
        e.player.useLadder(828, Tile.of(2405, 10188, 0))
    }
    //MiningCaveUp
    onObjectClick(21578) { e ->
        e.player.useLadder(828, Tile.of(2399, 3812, 0))
    }
    //handleLadders
    onObjectClick(21512, 21513, 21514, 21515) { e ->
        when (e.obj.id) {
            21512 -> e.player.useLadder(e.player.transform(2, 0, 2))
            21513 -> e.player.useLadder(e.player.transform(-2, 0, -2))
            21514 -> e.player.useLadder(e.player.transform(-2, 0, 1))
            21515 -> e.player.useLadder(e.player.transform(2, 0, -1))
        }
    }
    //handleShops
    onNpcClick(5509, 5487, 5484, 5486, 5485, 5483, 5495, options = arrayOf("Trade")) { e ->
        when (e.npc.id) {
            5509 -> ShopsHandler.openShop(e.player, "neitiznot_supplies")
            5487 -> ShopsHandler.openShop(e.player, "keepa_kettilons_store")
            5484 -> ShopsHandler.openShop(e.player, "flosis_fishmongers")
            5486 -> ShopsHandler.openShop(e.player, "weapons_galore")
            5485 -> ShopsHandler.openShop(e.player, "armour_shop")
            5483 -> ShopsHandler.openShop(e.player, "ore_store")
            5495 -> ShopsHandler.openShop(e.player, "contraband_yak_produce")
        }
    }
    //handleNeitzTravel
    onNpcClick(5507, 5508) { e ->
        val dest = if (e.npc.id == 5507) Tile.of(2644, 3709, 0) else Tile.of(2310, 3781, 0)
        e.player.tele(dest)
    }
    //handleJatizoTravel
    onNpcClick(5482, 5481) { e ->
        val dest = if (e.npc.id == 5482) Tile.of(2644, 3709, 0) else Tile.of(2420, 3781, 0)
        e.player.tele(dest)
    }
    //handleMagnusBanker
    onNpcClick(5488, options = arrayOf("Bank", "Collect", "Talk-to"), checkDistance = false) { e ->
        when (e.option) {
            "Talk-to" -> e.player.bank.open()
            "Bank" -> e.player.bank.open()
            "Collect" -> GE.openCollection(e.player)
        }
    }
}
