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
package com.rs.game.content.quests.the_golem

import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE.CALM_TALK
import com.rs.engine.dialogue.HeadE.CONFUSED
import com.rs.engine.dialogue.HeadE.NERVOUS
import com.rs.engine.dialogue.HeadE.SHAKING_HEAD
import com.rs.engine.dialogue.startConversation
import com.rs.engine.pathfinder.Direction
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemClick
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.content.quests.plague_city.utils.PESTLE_AND_MORTAR
import com.rs.game.content.skills.crafting.EMERALD
import com.rs.game.content.skills.crafting.RUBY
import com.rs.game.content.skills.crafting.SAPPHIRE
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.tasks.WorldTasks
import com.rs.lib.Constants
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.kts.onItemOnItem
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick
import com.rs.engine.variables.golem_retrieved_statuette
import com.rs.engine.variables.golem_statuettestatusa
import com.rs.engine.variables.golem_statuettestatusb
import com.rs.engine.variables.golem_statuettestatusc
import com.rs.engine.variables.golem_statuettestatusd
import com.rs.engine.variables.golem_throne_gems

private const val EXT_STAIRCASE_ID = 34978
private const val INT_STAIRCASE_ID = 6372
private const val VIAL_ID = 229
private const val BLACK_MUSHROOMS_OBJECT_ID = 6311
private const val BLACK_MUSHROOM_ITEM_ID = 4620
private const val BLACK_MUSHROOMS_INK_ID = 4622
private const val PICKUP_ANIM_ID = 827
private const val LETTER_ID = 4615
private const val VARMENS_NOTES_ID = 4616
private const val DISPLAY_CABINET_KEY_ID = 4617
private const val STATUETTE_ID = 4618
private const val PAPYRUS_ID = 970
private const val PAPYRUS_DUPE_ID = 13577
private const val PHOENIX_FEATHER_ID = 4621
private const val PHOENIX_QUIL_PEN_ID = 4623
private const val GOLEM_PROGRAM_ID = 4624
private const val STATUETTE_DISPLAY_CASE_ID = 24626
private const val DEMON_DOOR_ID = 6310
private const val DEMON_PORTAL_ID = 6282
private const val THRONE_ID = 6300
private const val CHISEL_ID = 1755

const val STAGE_ADD_CLAY = 1
const val STAGE_CONTINUE_INVESTIGATION = 2
const val STAGE_TALK_TO_ELISSA_ABOUT_LETTER = 3
const val STAGE_RETRIEVE_NOTES = 4
const val STAGE_LOOT_MUSEUM = 5
const val STAGE_RETURN_TO_UZER = 6
const val STAGE_TELL_GOLEM = 7
const val STAGE_CONVINCE_GOLEM = 8
const val STAGE_QUEST_COMPLETE = 9

@QuestHandler(
    quest = Quest.THE_GOLEM,
    startText = "Deep in the desert stand the ruins of the once great city of Uzer, where a lone survivor, an ancient clay golem, forever paces back and forth. It is badly damaged, and its mind cannot rest until it has completed the task for which it was created.",
    itemsText = "Vial. Pestle and mortar. Papyrus. 4 soft clay. Phoenix feather.",
    combatText = "None.",
    rewardsText = "1,000 Crafting XP<br>1,000 Thieving XP<br>The ability to take a carpet ride from Shantay Pass to Uzer.",
    completedStage = 9
)
@PluginEventHandler
class TheGolem : QuestOutline() {
    override fun getJournalLines(player: Player, stage: Int) = when (stage) {
        0 -> listOf("I can start this quest investigating the ruins at Uzer in the Kharidian Desert.")
        1 -> listOf("The golem needs repaired. 4 soft clay should do the trick.")
        2 -> listOf("With the golem repaired I should look around for any leads.")
        3 -> listOf("I should ask Elissa about this letter. Maybe I'll find her at the dig site?")
        4 -> listOf("Elissa said that Varmen's notes are in the exam centre. I should see what they say.")
        5 -> listOf("The statuette is currently at the Varrock Mueseum. I should check with Curator Haig Halen.")
        6 -> listOf("I should return to uzer and help the golem with his task.")
        7 -> listOf("The demon met it's end long ago. I should tell the golem.")
        8 -> listOf("Figure out how to convince the golem that it's task is complete.")
        9 -> listOf("QUEST COMPLETE!")
        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        player.skills.addXpQuest(Constants.CRAFTING, 1000.0)
        player.skills.addXpQuest(Constants.THIEVING, 1000.0)
        sendQuestCompleteInterface(player, 1761)
    }
}

@ServerStartupEvent
fun mapTheGolem() {

    fun completePuzzle(player: Player) {
        player.sendMessage("The door grinds open.")
        player.vars.saveVarBit(golem_statuettestatusc, 0)
        player.vars.saveVarBit(golem_statuettestatusd, 2)
        player.vars.saveVarBit(golem_statuettestatusb, 1)
        player.vars.saveVarBit(golem_statuettestatusa, 1)
    }


    fun checkPuzzle(player: Player) =
        player.vars.getVarBit(golem_statuettestatusc) == 0 &&
        player.vars.getVarBit(golem_statuettestatusd) == 2 &&
        player.vars.getVarBit(golem_statuettestatusb) == 1 &&
        player.vars.getVarBit(golem_statuettestatusa) == 1

    onItemClick(LETTER_ID, options = arrayOf("Read")) { e ->
        if (e.player.questManager.getStage(Quest.THE_GOLEM) == STAGE_CONTINUE_INVESTIGATION) {
            e.player.questManager.setStage(Quest.THE_GOLEM, STAGE_TALK_TO_ELISSA_ABOUT_LETTER)
        }
        e.player.interfaceManager.sendInterface(174)
        e.player.packets.setIFText(174, 1, "")
        e.player.packets.setIFText(174, 2, "")
        e.player.packets.setIFText(174, 3, "Dearest Varmen,")
        e.player.packets.setIFText(174, 4, "I hope this finds you well. Here are the books you")
        e.player.packets.setIFText(174, 5, "asked for. There has been an exciting development")
        e.player.packets.setIFText(174, 6, "closer to home-- another city from the same period")
        e.player.packets.setIFText(174, 7, "has been discovered east of Varrock, and we are")
        e.player.packets.setIFText(174, 8, "starting a huge excavation project here. I don't")
        e.player.packets.setIFText(174, 9, "know if the museum will be able to finance your ")
        e.player.packets.setIFText(174, 10, "expedition as well as this one, so I fear your ")
        e.player.packets.setIFText(174, 11, "current trip will be the last.")
        e.player.packets.setIFText(174, 12, "May Saradomin grant you a safe journey home.")
        e.player.packets.setIFText(174, 13, "Your loving Elissa.")
        e.player.packets.setIFText(174, 14, "")
        e.player.packets.setIFText(174, 15, "")
        e.player.packets.setIFText(174, 16, "")
    }

    onObjectClick(EXT_STAIRCASE_ID) { (player, obj, options) ->
        player.schedule {
            player.fadeScreen { player.useStairs(-1, Tile.of(2721, 4886, 0), 0, 0) }
            wait(4)
            player.faceDir(Direction.NORTH)
        }
    }

    onObjectClick(INT_STAIRCASE_ID) { (player, obj, options) ->
        player.schedule {
            player.fadeScreen { player.useStairs(-1, Tile.of(3491, 3090, 0), 0, 0) }
            wait(4)
            player.faceDir(Direction.WEST)
        }
    }

    onObjectClick(BLACK_MUSHROOMS_OBJECT_ID) { (player, obj, options) ->
        if (!player.inventory.containsItem(Item(BLACK_MUSHROOM_ITEM_ID))) {
            player.anim(PICKUP_ANIM_ID)
            player.tasks.schedule(2) {
                player.inventory.addItem(Item(BLACK_MUSHROOM_ITEM_ID))
            }
        }
    }

    onObjectClick(STATUETTE_DISPLAY_CASE_ID) { (player, obj, options) ->

        if (options == "Study") {
            if (player.vars.getVarBit(golem_retrieved_statuette) == 1) {
                player.startConversation {
                    simple("Recently this display was stolen and its whereabouts are unknown.")
                }
            } else {
                player.startConversation {
                    item(STATUETTE_ID, "The case contains a golem statuette.")
                }
            }
        }

        if (options == "Open") {

            if (!player.inventory.containsItem(DISPLAY_CABINET_KEY_ID)) {
                player.sendMessage("The cabinet is locked.")
            } else {
                player.anim(PICKUP_ANIM_ID)
                player.tasks.schedule(2) {
                    player.startConversation {
                        item(STATUETTE_ID, "You open the cabinet and retrieve the statuette.")
                    }
                    player.inventory.addItem(STATUETTE_ID)
                    player.vars.saveVarBit(golem_retrieved_statuette, 1)
                    player.questManager.setStage(Quest.THE_GOLEM, STAGE_RETURN_TO_UZER)
                }
            }
        }
    }

    onObjectClick(6305) { (player, obj, options) ->
        if (!checkPuzzle(player)) {
            if (player.vars.getVarBit(golem_statuettestatusc) == 0) {
                player.vars.setVarBit(golem_statuettestatusc, 1)
            } else {
                player.vars.setVarBit(golem_statuettestatusc, 0)
                if (checkPuzzle(player)) {
                    completePuzzle(player)
                }
            }
        }
    }

    onObjectClick(6306) { (player, obj, options) ->
        if (!checkPuzzle(player)) {
            if (player.vars.getVarBit(golem_statuettestatusd) == 1) {
                player.vars.setVarBit(golem_statuettestatusd, 2)
                if (checkPuzzle(player)) {
                    completePuzzle(player)
                }
            } else {
                player.vars.setVarBit(golem_statuettestatusd, 1)
            }
        }
    }

    onObjectClick(6304) { (player, obj, options) ->
        if (!checkPuzzle(player)) {
            if (player.vars.getVarBit(golem_statuettestatusb) == 0) {
                player.vars.setVarBit(golem_statuettestatusb, 1)
                if (checkPuzzle(player)) {
                    completePuzzle(player)
                }
            } else {
                player.vars.setVarBit(golem_statuettestatusb, 0)
            }
        }
    }

    onObjectClick(6303) { (player, obj, options) ->
        if (!checkPuzzle(player)) {
            if (player.vars.getVarBit(golem_statuettestatusa) == 0) {
                player.vars.setVarBit(golem_statuettestatusa, 1)
                if (checkPuzzle(player)) {
                    completePuzzle(player)
                }
            } else {
                player.vars.setVarBit(golem_statuettestatusa, 0)
            }
        }
    }

    onObjectClick(THRONE_ID) { (player, obj, options) ->
        if (player.inventory.containsItem(Item(CHISEL_ID))) {
            player.questManager.getAttribs(Quest.THE_GOLEM).setB("HAS_CHISELED_THRONE", true)
            player.anim(PICKUP_ANIM_ID)
            player.inventory.addItem(Item(SAPPHIRE, 2), true)
            player.inventory.addItem(Item(EMERALD, 2), true)
            player.inventory.addItem(Item(RUBY, 2), true)
            player.vars.setVarBit(golem_throne_gems, 1)
        } else {
            player.sendMessage("You need a chisel to do that.")
        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(6306, 6309), itemNamesOrIds = arrayOf("Statuette")) { (player, obj, item) ->
        player.inventory.removeItems(Item(STATUETTE_ID, 1))
        player.vars.saveVarBit(golem_statuettestatusd, 1)
        player.startConversation {
            simple("You add the statuette to the alcove.")
        }
    }

    //todo make sure people cant log out and log in to chisel throne again
    onItemOnObject(objectNamesOrIds = arrayOf(THRONE_ID), arrayOf("Chisel")) { (player, obj, item) ->
        player.questManager.getAttribs(Quest.THE_GOLEM).setB("HAS_CHISELED_THRONE", true)
        player.anim(PICKUP_ANIM_ID)
        player.inventory.addItem(Item(SAPPHIRE, 2), true)
        player.inventory.addItem(Item(EMERALD, 2), true)
        player.inventory.addItem(Item(RUBY, 2), true)
        player.vars.setVarBit(golem_throne_gems, 1)
    }

    onObjectClick(DEMON_DOOR_ID) { (player, obj, options) ->
        if (checkPuzzle(player)) {
            if (player.questManager.getAttribs(Quest.THE_GOLEM).getB("HAS_CHISELED_THRONE")) {
                player.vars.setVarBit(golem_throne_gems, 1)
            }
            player.sendMessage("You step into the portal")
            player.fadeScreen {
                player.tele(Tile.of(3552, 4948, 0))
                player.tasks.schedule(2) {
                    player.faceDir(Direction.NORTH)
                }
            }
            player.sendMessage("This room is dominated by a colassal horned skeleton!")
            if (player.questManager.getStage(Quest.THE_GOLEM) == STAGE_RETURN_TO_UZER) {
                player.questManager.setStage(Quest.THE_GOLEM, STAGE_TELL_GOLEM)
            }

        } else {
            player.startConversation {
                simple("The door is locked.")
            }
        }
    }

    onObjectClick(DEMON_PORTAL_ID) { (player, obj, options) ->
        player.fadeScreen {
            player.tele(Tile.of(2722, 4911, 0))
            player.tasks.schedule(2) {
                player.faceDir(Direction.SOUTH)
            }
        }
    }

    onItemOnItem(intArrayOf(PHOENIX_FEATHER_ID), intArrayOf(BLACK_MUSHROOM_ITEM_ID)) { e ->
        e.player.sendMessage("You stab the mushroom with the feather but do not get it inky enough.")
        e.player.sendMessage("You will need to grind the mushroom to get the ink out.")
    }

    onItemOnItem(intArrayOf(PHOENIX_FEATHER_ID), intArrayOf(BLACK_MUSHROOMS_INK_ID)) { e ->
        e.player.startConversation {
            simple("You dip the phoenix feather into the ink.")
        }
        e.player.inventory.deleteItem(PHOENIX_FEATHER_ID, 1)
        e.player.inventory.addItem(Item(PHOENIX_QUIL_PEN_ID, 1))
    }
    onItemOnItem(intArrayOf(BLACK_MUSHROOM_ITEM_ID), intArrayOf(PESTLE_AND_MORTAR)) { e ->
        if (e.player.inventory.containsItem(Item(VIAL_ID, 1))) {
            e.player.startConversation {
                e.player.inventory.deleteItem(VIAL_ID, 1)
                e.player.inventory.deleteItem(BLACK_MUSHROOM_ITEM_ID, 1)
                e.player.inventory.addItem(Item(BLACK_MUSHROOMS_INK_ID, 1))
                simple("You grind the black mushroom into ink and pour it into a vial.")
            }
        } else {
            e.player.startConversation {
                simple("You need an empty vial to do that.")
            }
        }
    }
    onItemOnItem(intArrayOf(PAPYRUS_ID, PAPYRUS_DUPE_ID), intArrayOf(PHOENIX_QUIL_PEN_ID)) { e ->
        e.player.startConversation {
            if (e.player.inventory.containsItem(PAPYRUS_ID)) e.player.inventory.deleteItem(PAPYRUS_ID, 1)
            else e.player.inventory.deleteItem(PAPYRUS_DUPE_ID, 1)
            e.player.inventory.addItem(Item(GOLEM_PROGRAM_ID, 1))
            simple("You write on the papyrus:<br>YOUR TASK IS DONE")
        }
    }
}

fun DialogueBuilder.curatorGolemDialogue(npc: NPC) {
    player(CALM_TALK, "I'm looking for a statuette recovered from the city of Uzer.")
    npc(npc, CALM_TALK, "Ah yes, a very impressive artefact. The people of that city were excellent sculptors. It's in the display case upstairs.")
    player(CALM_TALK, "No, I need to take it away with me.")
    npc(npc, CONFUSED, "What do you want it for?")
    options("Select an option") {
        op("I want to open a portal to the lair of an elder-demon.") {
            player(CALM_TALK, "I want to open a portal to the lair of an elder-demon.")
            npc(npc, SHAKING_HEAD, "Good heavens! I'd never let you do such a dangerous thing.")
        }
        op("Well, I, er, just want it.") {
            player(NERVOUS, "Well, I, er, just want it.")
            npc(npc, SHAKING_HEAD, "Well, you can't have it! This museum never lets go of its treasures.")
        }
    }
}

