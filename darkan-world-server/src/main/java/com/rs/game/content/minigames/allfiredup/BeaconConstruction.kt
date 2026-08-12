package com.rs.game.content.minigames.allfiredup

import com.rs.engine.pathfinder.Direction
import com.rs.game.content.skills.agility.Agility
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.events.LoginEvent
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onObjectClick
import com.rs.utils.ItemConfig
import com.rs.engine.variables.firedup_access12
import com.rs.engine.variables.firedup_access10
import com.rs.engine.variables.firedup_access13
import com.rs.engine.variables.firedup_access9

@ServerStartupEvent
fun mapBeaconConstruction() {
    onLogin { e: LoginEvent ->
        if (e.player.getBool("AllFiredUp.Burthorpe")) e.player.vars.setVarBit(firedup_access9, 1, true)
        if (e.player.getBool("AllFiredUp.DeathPlateau")) e.player.vars.setVarBit(firedup_access10, 1, true)
        if (e.player.getBool("AllFiredUp.GodWars")) e.player.vars.setVarBit(firedup_access12, 1, true)
        if (e.player.getBool("AllFiredUp.WildernessTemple")) e.player.vars.setVarBit(firedup_access13, 1, true)
    }
    //Repair Burthorpe
    onObjectClick(34893, checkDistance = false) { e ->
        if (e.player.getBool("AllFiredUp.Burthorpe")) e.player.vars.saveVarBit(firedup_access10, 1)
        if (e.option.equals("repair", ignoreCase = true)) {
            if (e.player.skills.getLevel(Skills.SMITHING) < 56) {
                e.player.sendMessage("You do not possess the required level of Smithing(56) to repair the ladder.")
                return@onObjectClick
            }
            if (e.player.inventory.getNumberOf(2351) < 2) {
                e.player.sendMessage("You need 2 iron bars to repair the ladder.")
                return@onObjectClick
            }
            else {
                e.player.sendMessage("You repair the ladder using 2 iron bars.")
                e.player.inventory.deleteItem(2351, 2)
                e.player.vars.saveVarBit(firedup_access9, 1)
                e.player.set("AllFiredUp.Burthorpe", true)
                return@onObjectClick
            }
        }
        if (e.option.equals("climb", ignoreCase = true)) {
            if (e.player.x > e.obj.x) {
                e.player.fadeScreen {
                    e.player.walkToAndExecute(Tile.of(2934, 3559, 0)) {
                        e.player.tele(Tile.of(2934, 3559, 0))
                        e.player.faceDir(Direction.WEST)
                    }
                }
            } else {
                e.player.fadeScreen {
                    e.player.walkToAndExecute(Tile.of(2932, 3559, 0)) {
                        e.player.tele(Tile.of(2932, 3559, 0))
                        e.player.faceDir(Direction.EAST)
                    }
                }
            }
            Agility.handleObstacle(
                e.player,
                3303,
                1,
                e.player.transform(if (e.player.x < e.obj.x) 5 else -5, 0, 0),
                0.0
            )
        }
    }

    //Repair DeathPlateau
    onObjectClick(34992) { e ->
        if (e.option.equals("repair", ignoreCase = true)) {
            if (e.player.getBool("AllFiredUp.DeathPlateau")) e.player.vars.saveVarBit(firedup_access10, 1)
            if (e.player.skills.getLevel(Skills.CONSTRUCTION) < 42) {
                e.player.sendMessage("You do not possess the required level of Construction(42) to repair the ladder.")
                return@onObjectClick
            }
            if (!e.player.getBool("AllFiredUp.DeathPlateau")) {
                if (!e.player.getBool("AllFiredUp.DeathPlateauI")) {
                    if (e.player.inventory.getNumberOf(960) < 2) {
                        e.player.sendMessage(
                            "You need 2 ${
                                ItemConfig.get(960).uidName.replace(
                                    "_".toRegex(),
                                    " "
                                )
                            } to repair the ladder."
                        )
                    } else {
                        e.player.sendMessage(
                            "You repair the ladder using 2 ${
                                ItemConfig.get(960).uidName.replace(
                                    "_".toRegex(),
                                    " "
                                )
                            }."
                        )
                        e.player.inventory.deleteItem(960, 2)
                        e.player.set("AllFiredUp.DeathPlateauI", true)
                    }
                }
                if (!e.player.getBool("AllFiredUp.DeathPlateauII")) {
                    if (e.player.inventory.getNumberOf(4819) < 4) e.player.sendMessage(
                        "You need 4 ${
                            ItemConfig.get(4819).uidName.replace(
                                "_".toRegex(),
                                " "
                            )
                        } to repair the ladder."
                    )
                    else {
                        e.player.sendMessage(
                            "You repair the ladder using 4 ${
                                ItemConfig.get(4819).uidName.replace(
                                    "_".toRegex(),
                                    " "
                                )
                            }."
                        )
                        e.player.inventory.deleteItem(4819, 4)
                        e.player.set("AllFiredUp.DeathPlateauII", true)
                    }
                }
                if (e.player.getBool("AllFiredUp.DeathPlateauI") && e.player.getBool("AllFiredUp.DeathPlateauII")) {
                    e.player.set("AllFiredUp.DeathPlateau", true)
                    e.player.vars.saveVarBit(firedup_access10, 1)
                }
            }
        }
        if (e.option.equals("climb", ignoreCase = true)) {
            if (e.player.x > e.obj.x) {
                e.player.fadeScreen {
                    e.player.walkToAndExecute(Tile.of(2953, 3617, 0)) {
                        e.player.tele(Tile.of(2953, 3617, 0))
                        e.player.faceDir(Direction.WEST)
                    }
                }
            } else {
                e.player.fadeScreen {
                    e.player.walkToAndExecute(Tile.of(2948, 3617, 0)) {
                        e.player.tele(Tile.of(2948, 3617, 0))
                        e.player.faceDir(Direction.EAST)
                    }
                }
            }
            Agility.handleObstacle(
                e.player,
                3303,
                1,
                e.player.transform(if (e.player.x < e.obj.x) 6 else -6, 0, 0),
                0.0
            )
        }
    }
    //Repair GodWars
    onObjectClick(38495) { e ->
        val player = e.player
        if (e.option.equals("repair", ignoreCase = true)) {
            if (player.getBool("AllFiredUp.GodWars")) player.vars.saveVarBit(firedup_access12, 1)
            if (player.skills.getLevel(Skills.CRAFTING) < 60) {
                player.sendMessage("You do not possess the required level of Crafting(42) to repair the windbreak.")
                return@onObjectClick
            }
            if (!player.getBool("AllFiredUp.GodWars")) if (player.inventory.getNumberOf(1733) >= 1) {
                if (player.inventory.getNumberOf(5931) >= 3) {
                    player.sendMessage(
                        "You repair the windbreak using 3 " + ItemConfig.get(5931).uidName.replace(
                            "_".toRegex(),
                            " "
                        ) + "."
                    )
                    player.set("AllFiredUp.GodWars", true)
                    player.vars.saveVarBit(firedup_access12, 1)
                    player.inventory.deleteItem(5931, 3)
                } else player.sendMessage(
                    "You need 3 " + ItemConfig.get(5931).uidName.replace(
                        "_".toRegex(),
                        " "
                    ) + " to repair the windbreak."
                )
            } else player.sendMessage(
                "You need a " + ItemConfig.get(1733).uidName.replace(
                    "_".toRegex(),
                    " "
                ) + " and 3 " + ItemConfig.get(5931).uidName.replace(
                    "_".toRegex(),
                    " "
                ) + " to repair the windbreak."
            )
        }
    }
    //Repair Wilderness Temple
    onObjectClick(38499, 38497, checkDistance = false) { e ->
        val player = e.player
        if (e.option.equals("repair", ignoreCase = true)) {
            if (player.getBool("AllFiredUp.WildernessTemple")) player.vars.saveVarBit(firedup_access13, 1)
            if (player.skills.getLevel(Skills.CONSTRUCTION) < 59) {
                player.sendMessage("You do not possess the required level of Construction(59) to repair the ladder.")
                return@onObjectClick
            }
            if (!player.getBool("AllFiredUp.WildernessTemple")) if (!player.getBool("AllFiredUp.WildernessTempleI")) {
                if (player.inventory.getNumberOf(960) < 2) player.sendMessage(
                    "You need 2 " + ItemConfig.get(960).uidName.replace(
                        "_".toRegex(),
                        " "
                    ) + " to repair the ladder."
                )
                else {
                    player.sendMessage(
                        "You repair the ladder using 2 " + ItemConfig.get(960).uidName.replace(
                            "_".toRegex(),
                            " "
                        ) + "."
                    )
                    player.inventory.deleteItem(960, 2)

                    player.set("AllFiredUp.WildernessTempleI", true)
                }
            }
            if (!player.getBool("AllFiredUp.WildernessTempleII")) {
                if (player.inventory.getNumberOf(4819) < 4) player.sendMessage(
                    "You need 2 " + ItemConfig.get(4819).uidName.replace(
                        "_".toRegex(),
                        " "
                    ) + " to repair the ladder."
                )
                else {
                    player.sendMessage(
                        "You repair the ladder using 4 " + ItemConfig.get(4819).uidName.replace(
                            "_".toRegex(),
                            " "
                        ) + "."
                    )
                    player.inventory.deleteItem(4819, 4)
                    player.set("AllFiredUp.WildernessTempleII", true)
                }
            }
            if (player.getBool("AllFiredUp.WildernessTempleI") && player.getBool("AllFiredUp.WildernessTempleII")) {
                player.set("AllFiredUp.WildernessTemple", true)
                player.vars.saveVarBit(firedup_access13, 1)
            }
        }
        if (e.option.equals("climb", ignoreCase = true)) {
            if (e.player.x > e.obj.x) {
                player.fadeScreen {
                    player.walkToAndExecute(Tile.of(2956, 3835, 0)) {
                        player.forceMove(Tile.of(2956, 3835, 0), 0, 5)
                        player.faceDir(Direction.WEST)
                    }
                }
            } else {
                player.fadeScreen {
                    player.walkToAndExecute(Tile.of(2951, 3835, 0)) {
                        player.forceMove(Tile.of(2951, 3835, 0), 0, 5)
                        player.faceDir(Direction.EAST)
                    }
                }
            }
            Agility.handleObstacle(
                e.player,
                3303,
                1,
                e.player.transform(if (e.player.x < e.obj.x) 7 else -7, 0, 0),
                0.0
            )
        }
    }
}