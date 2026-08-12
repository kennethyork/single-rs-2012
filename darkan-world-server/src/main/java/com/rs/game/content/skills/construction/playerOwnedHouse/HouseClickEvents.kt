package com.rs.game.content.skills.construction.playerOwnedHouse

import com.rs.engine.dialogue.startConversation
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import com.rs.plugin.kts.onObjectClick

@ServerStartupEvent
fun POHClickHAndlers() {
    val intObjectIds = EntranceLocations.entries.map { it.objectId }
    //Portal click event
    onObjectClick(*intObjectIds.toTypedArray()) { e ->
        e.player.startConversation {
            options {
                ops("Go to your house.") {
                    exec {
                        e.player.house.setBuildMode(false)
                        e.player.house.enterMyHouse()
                    }
                }
                ops("Go to your house (building mode).") {
                    exec {
                        e.player.house.kickGuests()
                        e.player.house.setBuildMode(true)
                        e.player.house.enterMyHouse()
                    }
                }
                ops("Go to a friend's house.") {
                    exec {
                        if (e.player.isIronMan) {
                            e.player.sendMessage("You cannot enter another player's house as an ironman.")
                            return@exec
                        }
                        e.player.sendInputName(
                            "Enter name of the person whose house you'd like to join:"
                        ) { name ->
                            House.enterHouse(e.player, name)
                        }
                    }
                }
                ops("Nevermind.")
            }
        }
    }
    //House controls in settings tab
    onButtonClick(398) { (player, _, componentId) ->
        when (componentId) {
            19 -> player.interfaceManager.sendSubDefault(InterfaceManager.Sub.TAB_SETTINGS)
            15, 1 -> player.house.setBuildMode(componentId == 15)
            25, 26 -> player.house.setArriveInPortal(componentId == 25)
            27 -> player.house.expelGuests()
            29 -> House.leaveHouse(player)
        }
    }
    //Room Creation menu
    onButtonClick(402) { (player, _, componentId) ->
        if (componentId in 93..115) {
            player.house.createRoom(componentId - 93)
        }
    }
////394 is a subscription promo interface, 396 Catering - Gielinor Games.
//    onButtonClick(394, 396) { (player, _, componentId, slotId) ->
//        if (componentId == 11) {
//            player.house.build(slotId)
//        }
//    }
}