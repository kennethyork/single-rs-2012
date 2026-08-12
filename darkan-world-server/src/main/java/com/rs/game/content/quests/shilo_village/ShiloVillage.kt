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
package com.rs.game.content.quests.shilo_village

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.pathfinder.Direction
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.World
import com.rs.game.World.addGroundItem
import com.rs.game.World.removeGroundItem
import com.rs.game.World.spawnNPC
import com.rs.game.content.quests.shilo_village.ShiloVillage.Companion.DEAL_WITH_RASHILIYIAS_CORPSE
import com.rs.game.content.quests.shilo_village.ShiloVillage.Companion.FIND_TEMPLE_OF_AH_ZA_RHOON
import com.rs.game.content.quests.shilo_village.ShiloVillage.Companion.INVESTIGATE_B_TOMB
import com.rs.game.content.quests.shilo_village.ShiloVillage.Companion.RETURN_TO_TRUFITUS_AGAIN
import com.rs.game.content.world.doors.Doors
import com.rs.game.map.ChunkManager
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.tasks.WorldTasks
import com.rs.lib.Constants
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.plugin.PluginManager
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.events.PickupItemEvent
import com.rs.plugin.kts.*

@QuestHandler(
    quest = Quest.SHILO_VILLAGE,
    startText = "Talk to Mosol Rei, who is outside of Shilo Village in the south part of Karamja.",
    itemsText = "A spool of bronze wire, 3 regular bones, a rope, a chisel, and a torch or other light source.",
    combatText = "Nazastarool (Level 53) (First appears as a zombie, then as a skeleton, and finally as a ghost)",
    rewardsText = "2 Quest Points<br>3,750 Crafting XP<br>Access to the Shilo Village, gem rocks and cart<br>travel between Shilo Village and Brimhaven<br>2,000 coins from Yanni Salika in Shilo Village if you sell him the quest's items",
    completedStage = 9
)

@PluginEventHandler
class ShiloVillage : QuestOutline() {
    companion object {
        const val NOT_STARTED = 0
        const val DELIVER_BELT = 1
        const val FIND_TEMPLE_OF_AH_ZA_RHOON = 2
        const val VERIFY_TEMPLE_OF_AH_ZA_RHOON = 3
        const val RETURN_TO_TRUFITUS = 4
        const val INVESTIGATE_B_TOMB = 5
        const val RETURN_TO_TRUFITUS_AGAIN = 6
        const val RASHILIYIAS_TOMB = 7
        const val DEAL_WITH_RASHILIYIAS_CORPSE = 8
        const val COMPLETED = 9
    }

    override fun getJournalLines(player: Player, stage: Int) = when (stage) {
        NOT_STARTED -> listOf("Talk to Mosol Rei, who is outside of Shilo Village in the south part of Karamja.")
        DELIVER_BELT -> listOf("Take the Wampum belt to Trufitus in Tai Bwo Wannai Village.")
        FIND_TEMPLE_OF_AH_ZA_RHOON -> listOf("Find the Temple of Ah Za Rhoon.")
        VERIFY_TEMPLE_OF_AH_ZA_RHOON -> listOf("The cavern system I found might be Ah Za Rhoon, if I could identify the cavern it would help a lot.")
        RETURN_TO_TRUFITUS -> listOf("Take what I found at the Temple of Ah Za Rhoon to Trufitus.")
        INVESTIGATE_B_TOMB -> listOf("Search Bervirius' tomb for clues.")
        RETURN_TO_TRUFITUS_AGAIN -> listOf("Talk to Trufitus about what I found at Berivius' Tomb.")
        RASHILIYIAS_TOMB -> listOf("Search Rashiliyia's tomb for clues.")
        DEAL_WITH_RASHILIYIAS_CORPSE -> listOf("Deal with Rashiliyia's corpse.")
        COMPLETED -> listOf("QUEST COMPLETE!")
        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        player.skills.addXpQuest(Constants.CRAFTING, 3875.0)
        sendQuestCompleteInterface(player, 1891)
    }

}

@ServerStartupEvent
fun mapShiloVillage() {
    var lit = false
    var isBossUp = false
    val BONE_SHARD_ID = 604
    val BONE_KEY_ID = 605
    val STONE_PLAQUE_ID = 606
    val CHISEL_ID = 1755
    val Z_CORPSE_ID = 610
    val LIT_CANDLE_ID = 33
    val UNLIT_CANDLE_ID = 36
    val LIT_TORCH_ID = 594
    val UNLIT_TORCH_ID = 596
    val TATTERED_SCROLL_ID = 607
    val CRUMPLED_SCROLL_ID = 608
    val BURIED_MOUND_ID = 2217
    val WATERFALL_ROCKS_ID = 2225
    val CAVE_IN_ID = 2220
    val STRANGE_STONE_ID = 2221
    val GALLOWS_ID = 2224
    val SACKS_ID = 2223
    val LOOSE_ROCKS_ID = 2222
    val UNROPED_FISSURE_ID = 2218
    val ROPED_FISSURE_ID = 2219
    val DIG_ANIM_ID = 830
    val PICKUP_ANIM_ID = 827
    val WELL_STACKED_ROCKS_ID = 2234
    val ROCK_SLIDE_ID = 2236
    val B_DOLMEN_ID = 2235
    val SWORD_POMMEL_ID = 623
    val LOCATING_SPHERE_ID = 611
    val BERVIRIUS_NOTES_ID = 624
    val CHISEL_ANIM_ID = 886
    val BONE_BEADS_ID = 618
    val BEADS_OF_THE_DEAD_ID = 616
    val BRONZE_WIRE_ID = 5602
    val BRONZE_WIRE2_ID = 1794
    val TOMB_EXIT_ID = 2243
    val ANCIENT_METAL_GATE1_ID = 2255
    val ANCIENT_METAL_GATE2_ID = 2256
    val ROCKS_ID = 2257
    val BONE_DOOR1_ID = 2246
    val BONE_DOOR2_ID = 2247
    val BONE_DOOR3_ID = 2248
    val BONE_DOOR4_ID = 2249
    val BONE_DOOR5_ID = 2250
    val R_DOLMEN_ID = 2258
    val BOSS_NPC1_ID = 507
    val BOSS_NPC2_ID = 508
    val BOSS_NPC3_ID = 509
    val R_CORPSE_ID = 609
    val R_NPC_ID = 506
    val VIGROY_NPC_ID = 511
    val HAGEDY_NPC_ID = 510
    val VIGROY_CART_ID = 2265
    val HAGEDY_CART_ID = 2230
    val SHILO_GATE_ID = 2216

    //TODO ADD MUSIC AND SPOT ANIMS
    //TODO LOCATING ORB FUNCTIONALITY(optional)
    //todo using bone key on trufitus(optional)
    //TODO DROPPING/DESTROYING QUEST ITEM WARNINGS/FUNCTIONALITY
    //TODO LACK OF BOD NECKLACE CAUSING RASHIYILIA TO SPAWN IN MONSTERS THAT ATTACK YOU
    //TODO SMASHED TABLE(optional)
    //TODO OPTIONAL RAFT CUTSCENE(optional)

    fun spawnBoss(player: Player) {
        player.packets.sendCameraShake(3, 50, 1, 20, 1)
        WorldTasks.schedule(2) {
            val nazastarool = spawnNPC(BOSS_NPC1_ID, Tile.of(player.tile, 1), true, true)
            nazastarool.isForceAgressive = true
            nazastarool.setForceAggroDistance(10)
            nazastarool.combatTarget = player
            WorldTasks.schedule(2) {
                player.packets.sendCameraShake(3, 0, 0, 0, 0)
                player.sendMessage("Nazastarool: Who dares disturb Rashiliyia's rest?")
                nazastarool.forceTalk("Who dares disturb Rashiliyia's rest?")
                WorldTasks.schedule(2) {
                    player.sendMessage("Nazastarool: I am Nazastarool! Prepare to die!")
                    nazastarool.forceTalk("I am Nazastarool! Prepare to die!")
                }
            }
        }
    }

    onNpcDeath(BOSS_NPC1_ID){ e ->
        val location = e.npc.tile
        WorldTasks.schedule(7) {
            val nazastarool = spawnNPC(BOSS_NPC2_ID, location, true, true)
            nazastarool.faceDir(e.npc.direction)
            nazastarool.isForceAgressive = true
            nazastarool.setForceAggroDistance(10)
            nazastarool.combatTarget = e.killer
            val player = e.killer as Player?
            WorldTasks.schedule(2) {
                player?.sendMessage("Nazastarool: Quake in fear, for I am reborn!")
                WorldTasks.schedule(2) {
                    player?.sendMessage("Nazastarool: Your death will be swift.")
                }
            }
        }
    }

    onNpcDeath(BOSS_NPC2_ID){ e ->
        WorldTasks.schedule(7) {
            val location = e.npc.tile
            val nazastarool = spawnNPC(BOSS_NPC3_ID, location, true, true)
            nazastarool.faceDir(e.npc.direction)
            nazastarool.isForceAgressive = true
            nazastarool.setForceAggroDistance(10)
            nazastarool.combatTarget = e.killer
            val player = e.killer as Player?
            WorldTasks.schedule(2) {
                player?.sendMessage("Nazastarool: Nazastarool returns with vengeance!")
                nazastarool.forceTalk("Nazastarool returns with vengeance!")
                WorldTasks.schedule(2) {
                    player?.sendMessage("Nazastarool: Soon you will serve Rashiliyia.")
                    nazastarool.forceTalk("Soon you will serve Rashiliyia.")
                }
            }
        }
    }

    onNpcDeath(BOSS_NPC3_ID){ e ->
        e.killer.soundEffect(438, true) //bugfix for death sound not playing
        isBossUp = false
        val player = e.killer as Player?
        player?.sendMessage("You see something appear on the dolmen.")
        player?.sendMessage("Nazastarool: May you perish in the fires of Zamorak's furnace! Rashiliyia's curse is upon you!")
        player?.startConversation {
            simple("You hear a disembodied voice fading away into the distance:<br>May you perish in the fires of Zamorak's furnace!<br>Rashiliyia's curse is upon you!")
            exec{
                addGroundItem(Item(R_CORPSE_ID), Tile.of(2892, 9487, 0), player)
                player.questManager?.getAttribs(Quest.SHILO_VILLAGE)?.setB("HAS_KILLED_BOSS", true)
            }
        }
    }

    onPickupItem(R_CORPSE_ID){ e ->
        e.player.startConversation {
            item(e.item.id, "You take the mummified remains of a human female. You feel certain that these are Rashiliyia's remains. You carefully place the remains in your inventory.")
        }
        e.player.setQuestStage(Quest.SHILO_VILLAGE, DEAL_WITH_RASHILIYIAS_CORPSE)
    }

    onItemOnItem(intArrayOf(CHISEL_ID), intArrayOf(BONE_SHARD_ID)) { e ->
        when (e.player.getQuestStage(Quest.SHILO_VILLAGE)) {
            7 -> {
                if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_TRIED_LOCK")) {
                    if (e.player.getLevel(Skills.CRAFTING) >= 20) {
                        e.player.startConversation {
                            simple("Remembering Zadimus' words and the strange bone lock, you start to craft the bone. You successfully make a key out of the bone shard.")
                            exec {
                                e.player.anim(CHISEL_ANIM_ID)
                                WorldTasks.schedule(2) {
                                    e.player.inventory.removeItems(Item(BONE_SHARD_ID, 1))
                                    e.player.inventory.addItem(Item(BONE_KEY_ID, 1))
                                }
                            }
                        }
                    } else {
                        e.player.startConversation {
                            simple("You need to have a Crafting level of at least 20 to work this material.")
                        }
                    }
                }else{
                    e.player.startConversation {
                        simple("You're not quite sure what to make with this. Perhaps it will come to you as you discover more about Rashiliyia?")
                    }
                }
            }
            else -> {
                e.player.startConversation {
                    simple("You're not quite sure what to make with this. Perhaps it will come to you as you discover more about Rashiliyia?")
                }
            }
        }
    }

    onItemOnItem(intArrayOf(CHISEL_ID), intArrayOf(SWORD_POMMEL_ID)) { e ->
        when (e.player.getQuestStage(Quest.SHILO_VILLAGE)) {
            7 -> {
                e.player.startConversation {
                    item(SWORD_POMMEL_ID, "You prepare the ivory pommel and the chisel to start crafting...")
                    item(BONE_BEADS_ID, "You successfully craft some of the ivory into beads. They may look good as part of a necklace.")
                    exec {
                        e.player.anim(CHISEL_ANIM_ID)
                        WorldTasks.schedule(2) {
                            e.player.inventory.removeItems(Item(SWORD_POMMEL_ID, 1))
                            e.player.inventory.addItem(Item(BONE_BEADS_ID, 1))
                        }
                    }
                }
            }
            else -> {
                e.player.startConversation {
                    simple("You're not quite sure what to make with this. Perhaps it will come to you as you discover more about Rashiliyia?")
                }}
        }
    }

    onItemOnItem(intArrayOf(BRONZE_WIRE_ID, BRONZE_WIRE2_ID), intArrayOf(BONE_BEADS_ID)) { e ->
        when (e.player.getQuestStage(Quest.SHILO_VILLAGE)) {
            7 -> {
                e.player.startConversation {
                    item(BEADS_OF_THE_DEAD_ID, "You successfully craft the beads and bronze wire into a necklace which you name, 'Beads of the Dead'")
                    exec {
                        e.player.anim(CHISEL_ANIM_ID)
                        WorldTasks.schedule(1) {
                            e.player.inventory.removeItems(Item(e.item1.id, 1))
                            e.player.inventory.removeItems(Item(e.item2.id, 1))
                            e.player.inventory.addItem(Item(BEADS_OF_THE_DEAD_ID, 1))
                        }
                    }
                }
            }
            else -> {
                e.player.startConversation {
                    simple("You're not quite sure what to make with this. Perhaps it will come to you as you discover more about Rashiliyia?")
                }}
        }
    }

    onItemClick(BONE_SHARD_ID, options = arrayOf("Inspect")){ (player) ->
        player.startConversation {
            simple("You look at the shard of bone. The words of Zadimus come back to you. 'I am the key, but only kin may approach her.'")
        }
    }

    onItemClick(Z_CORPSE_ID, options = arrayOf("Bury")){ (player) ->
        val statuetile = Tile.of(2795, 3090, 0)
        if(player.tile.withinDistance(statuetile, 4) && player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_SHOWN_Z_CORPSE")) {
            player.sendMessage("You feel an unearthly compunction to bury this corpse!")
            player.sendMessage("You dig a hole for the remains.")
            player.lock(9)
            WorldTasks.schedule(1) {
                player.anim(DIG_ANIM_ID)
            }
            WorldTasks.schedule(2) {
                player.anim(PICKUP_ANIM_ID)
                player.sendMessage("You lay the remains of Zadimus to rest")
                player.inventory.removeItems(Item(Z_CORPSE_ID, 1))
            }
            WorldTasks.schedule(4) {
                player.faceDir(Direction.SOUTH)
            }
            WorldTasks.schedule(6) {
                player.lock()
                val ghost = spawnNPC(501, Tile.of(player.tile), true, true)
                ghost.setRandomWalk(false)
                ghost.faceDir(Direction.SOUTH)
                ghost.addWalkSteps(Tile.of(2975, 3084, 0), 2, false)
                ghost.faceDir(Direction.NORTH)
                player.startConversation {
                    simple("You hear an unearthly moaning sound as you see an apparition materialise right in front of you.")
                    npc(501,HeadE.CALM_TALK,"You have released me from my torture, and now I shall aid you. You seek to dispell the one who tortured and killed me. Remember this... 'I am the key, but only kin may approach her.'") {
                        exec {
                            player.unlock()
                            ghost.finish()
                            player.inventory.addItem(604, 1)
                            player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_BURIED_Z_CORPSE", true)
                        }
                    }
                    item(604,"The apparition disappears into the ground where you buried the corpse. You see the ground in front of you shake as a shard of bone forces its way to the surface. You take the bone shard and place it in your")
                    item(604, "inventory.")
                }
            }
        }else{
            player.sendMessage("You feel an unearthly compunction to bury this corpse!")
            WorldTasks.schedule(2) {
                player.startConversation {
                    simple("You hear a ghostly wailing sound coming from the corpse and a whispering voice says,'Let me rest in a sacred place and assist you I will'.")
                }
            }
        }
    }

    onItemClick(LIT_CANDLE_ID, LIT_TORCH_ID, options = arrayOf("Extinguish")) { e ->
        e.player.inventory.removeItems(Item(e.item.id, 1))
        if(e.item.id == LIT_CANDLE_ID){ //lit candle
            e.player.inventory.addItem(UNLIT_CANDLE_ID, 1) //unlit candle
        }
        if(e.item.id == LIT_TORCH_ID){ //lit torch
            e.player.inventory.addItem(UNLIT_TORCH_ID, 1) //unlit torch
        }
    }

    onItemClick(TATTERED_SCROLL_ID, options = arrayOf("Read")) { e ->
        e.player.startConversation {
            simple("This looks like part of a scroll about someone called Bervirius<br>Would you like to read it?")
            options("SELECT AN OPTION") {
                op("Yes please.") {
                    exec{
                        e.player.interfaceManager.sendInterface(174)
                        e.player.getPackets().setIFText(174, 1, "")
                        e.player.getPackets().setIFText(174, 2, "")
                        e.player.getPackets().setIFText(174, 3, "Bervirius, son of King Danthalas, was killed in ")
                        e.player.getPackets().setIFText(174, 4, "battle. His bravery became legend... devout mother")
                        e.player.getPackets().setIFText(174, 5, "Rashiliya was so heartbroken that she swore fealty")
                        e.player.getPackets().setIFText(174, 6, "to Zamorak if he would return her son to her.")
                        e.player.getPackets().setIFText(174, 7, "Bervirius returned as an undead creature...")
                        e.player.getPackets().setIFText(174, 8, "Many guards died fighting the undead Bervirius.")
                        e.player.getPackets().setIFText(174, 9, "Eventually the undead Bervirius was set on fire...")
                        e.player.getPackets().setIFText(174, 10, "His remains were taken far to the South, and then")
                        e.player.getPackets().setIFText(174, 11, "towards the setting sun to a tomb that is")
                        e.player.getPackets().setIFText(174, 12, "surrounded by and level with the sea. This is the")
                        e.player.getPackets().setIFText(174, 13, "only way to contain the spirits of witches and the")
                        e.player.getPackets().setIFText(174, 14, "undead.")
                        e.player.getPackets().setIFText(174, 15, "")
                        e.player.getPackets().setIFText(174, 16, "")
                        if(!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_TATTERED_SCROLL")) {
                            if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_CRUMPLED_SCROLL")) {
                                if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_STONE_PLAQUE")) {
                                    if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_COLLECTED_Z_CORPSE")) {
                                        e.player.setQuestStage(Quest.SHILO_VILLAGE, 4)
                                    }
                                }
                            }
                        }
                        e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_TATTERED_SCROLL", true)
                    }
                }
                op("No thanks.") {
                    simple("You decide not to open the scroll. But instead place it carefully back in your inventory.")
                }
            }
        }
    }

    onItemClick(BERVIRIUS_NOTES_ID, options = arrayOf("Read")) { e ->
        e.player.startConversation {
            item(BERVIRIUS_NOTES_ID,"This scroll is a collection of writings. Some of them are just scraps of papyrus with what looks like random scribblings. Which would you like to read?")
            options("Select an Option") {
                op("Tattered Yellow papyrus.") {
                    simple("...and rest like your mother who is silent in the peace of her tomb far to the North of Ah Za Rhoon. Near the sea, and under the hills deep in the underground to watch all nature from the darkness of her final resting place.")
                    exec{
                        if(!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_YELLOW_PAPYRUS")) {
                            if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_ORANGE_PAPYRUS")) {
                                if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_WHITE_PAPYRUS")) {
                                    e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_BERVIRIUS_NOTES", true)
                                }
                            }
                        }
                        e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_YELLOW_PAPYRUS", true)
                    }
                }
                op("Decayed White papyrus.") {
                    simple("...Rashiliyia did so love objects of beauty. Her tomb was adorned with crystals that glowed brightly when near to each other.")
                    exec{
                        if(!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_WHITE_PAPYRUS")) {
                            if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_ORANGE_PAPYRUS")) {
                                if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_YELLOW_PAPYRUS")) {
                                    e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_BERVIRIUS_NOTES", true)
                                }
                            }
                        }
                        e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_WHITE_PAPYRUS", true)
                    }
                }
                op("Crusty Orange papyrus.") {
                    simple("...Rashiliyia did so love objects of beauty. Her tomb was adorned with crystals that glowed brightly when near to each other.")
                    exec{
                        if(!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_ORANGE_PAPYRUS")) {
                            if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_WHITE_PAPYRUS")) {
                                if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_YELLOW_PAPYRUS")) {
                                    e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_BERVIRIUS_NOTES", true)
                                }
                            }
                        }
                        e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_ORANGE_PAPYRUS", true)
                    }
                }
            }
        }
    }

    onItemClick(CRUMPLED_SCROLL_ID, options = arrayOf("Read")) { e ->
        e.player.startConversation {
            simple("This looks like part of a scroll about Rashiliya. Would you like to read it?")
            options("SELECT AN OPTION") {
                op("Yes please.") {
                    exec{
                        e.player.interfaceManager.sendInterface(174)
                        e.player.getPackets().setIFText(174, 1, "")
                        e.player.getPackets().setIFText(174, 2, "")
                        e.player.getPackets().setIFText(174, 3, "Rashiliya's rage went unchecked. She killed")
                        e.player.getPackets().setIFText(174, 4, "without mercy for revenge of her son's life. Like a")
                        e.player.getPackets().setIFText(174, 5, "spectre through the night she entered houses and")
                        e.player.getPackets().setIFText(174, 6, "one by one quietly strangled life from the.")
                        e.player.getPackets().setIFText(174, 7, "occupants. It is said that only a handful survived,")
                        e.player.getPackets().setIFText(174, 8, "protected by necklace wards to keep the Witch ")
                        e.player.getPackets().setIFText(174, 9, "Queen at bay.")
                        e.player.getPackets().setIFText(174, 10, "")
                        e.player.getPackets().setIFText(174, 11, "")
                        e.player.getPackets().setIFText(174, 12, "")
                        e.player.getPackets().setIFText(174, 13, "")
                        e.player.getPackets().setIFText(174, 14, "")
                        e.player.getPackets().setIFText(174, 15, "")
                        e.player.getPackets().setIFText(174, 16, "")
                        if(!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_CRUMPLED_SCROLL")) {
                            if(e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_STONE_PLAQUE")) {
                                if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_TATTERED_SCROLL")) {
                                    if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_COLLECTED_Z_CORPSE")) {
                                        e.player.setQuestStage(Quest.SHILO_VILLAGE, 4)
                                    }
                                }
                            }
                        }
                        e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_CRUMPLED_SCROLL", true)
                    }
                }
                op("No thanks.") {
                    exec{
                        e.player.sendMessage("You decide to leave the scroll well alone.")
                    }
                }
            }
        }
    }

    onItemClick(STONE_PLAQUE_ID, options = arrayOf("Read")) { e ->
        e.player.startConversation {
            simple("The markings are very intricate. It's a very strange language. The meaning of it evades you though. Perhaps Trufitus can decipher the markings?")
            {
                exec {
                    if (!e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_STONE_PLAQUE")) {
                        if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_CRUMPLED_SCROLL")) {
                            if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_TATTERED_SCROLL")) {
                                if (e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_COLLECTED_Z_CORPSE")) {
                                    e.player.setQuestStage(Quest.SHILO_VILLAGE, 4)
                                }
                            }
                        }
                    }
                    e.player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_READ_STONE_PLAQUE", true)
                }

            }
        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(UNROPED_FISSURE_ID), itemNamesOrIds = arrayOf("Lit candle", "Lit torch", "Lit black candle")){ (player, obj, item) ->
        if((player.getQuestStage(Quest.SHILO_VILLAGE) >= 2)) {
            player.anim(PICKUP_ANIM_ID)
            WorldTasks.schedule(2) {
                player.inventory.removeItems(Item(item.id, 1))
                lit = true
                player.startConversation {
                    item(item.id,"You drop the " + item.name + " into the fissure and see that there is quite a large drop after you get through the hole. There is a good anchor point nearby onto which you could tie a rope.")
                    simple("Some rope might help here.")
                }
            }
            WorldTasks.schedule(30) {
                lit = false
                player.startConversation {
                    item(item.id, "The ${item.name} burns out.")
                }
            }
        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(B_DOLMEN_ID), itemNamesOrIds = arrayOf(R_CORPSE_ID)){ (player, obj, item) ->

        player.anim(PICKUP_ANIM_ID)
        WorldTasks.schedule(1) {
            player.inventory.removeItems(Item(item.id, 1))
            WorldTasks.schedule(2) {
                val rashiliyia = spawnNPC(R_NPC_ID, Tile.of(player.tile, 1), true, true)
                rashiliyia.faceEntity(player)
                player.faceEntity(rashiliyia)
                rashiliyia.setRandomWalk(false)
                player.startConversation {
                    item(
                        item.id,
                        "You carefully place Rashiliyias remains on the dolmen. You feel a strange vibration in the air."
                    )
                    exec {
                        player.sendMessage("Without warning the spirit of Rashiliyia disappears")
                    }
                    npc(
                        R_NPC_ID,
                        HeadE.CALM_TALK,
                        "You have my gratitude for releasing my spirit. I have suffered a vengeful and evil existence. I was tricked by Zamorak. He returned my son to me as an undead creature."
                    )
                    npc(
                        R_NPC_ID,
                        HeadE.CALM_TALK,
                        "My hatred and bitterness corrupted me. I tried to destroy all life... now I am released. And am grateful to contemplate eternal rest..."
                    )
                    exec {
                        player.questManager.completeQuest(Quest.SHILO_VILLAGE)
                        player.questManager.setStage(Quest.SHILO_VILLAGE, 9)
                        rashiliyia.finish()
                    }
                }
            }
        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(BONE_DOOR1_ID, BONE_DOOR2_ID, BONE_DOOR3_ID, BONE_DOOR4_ID, BONE_DOOR5_ID), itemNamesOrIds = arrayOf("Bones")){ (player, obj, item) ->
        if(player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_USED_ONE_BONE")) {
            if(player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_USED_TWO_BONES")) {
                player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_USED_THREE_BONES", true)
                player.sendMessage("You carefully place another bone in the door...")
                player.anim(PICKUP_ANIM_ID)
                WorldTasks.schedule(2) {
                    player.inventory.removeItems(Item(item.id, 1))
                    World.getObject(Tile.of(2893,9480,0))?.setIdTemporary(BONE_DOOR5_ID, 270)
                }
                player.inventory.removeItems(Item(item.id, 1))
                player.startConversation{
                    item(item.id, "You place the bone into the last recess on the skeletal door and it fits. All the recesses are filled...")
                    simple("The door seems to change slightly. Two depictions of skeletal warriors turn their heads towards you. They are alive! The skeletons wrench themselves free of the door. Stepping out of the door, with grinning teeth they push the huge doors open.")
                }
            }else{
                player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_USED_TWO_BONES", true)
                player.sendMessage("You carefully place another bone in the door...")
                player.anim(PICKUP_ANIM_ID)
                WorldTasks.schedule(2) {
                    player.inventory.removeItems(Item(item.id, 1))
                    World.getObject(Tile.of(2892,9480,0))?.setIdTemporary(BONE_DOOR4_ID, 270)
                    player.startConversation{
                        item(item.id, "You place the bone into the skeletal door and it fits. There is just one recess left now.")
                    }
                }
            }
        }else{
            player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_USED_ONE_BONE", true)
            player.sendMessage("You carefully place a bone in the door...")
            player.anim(PICKUP_ANIM_ID)
            WorldTasks.schedule(2) {
                player.inventory.removeItems(Item(item.id, 1))
                World.getObject(Tile.of(2892,9480,0))?.setIdTemporary(BONE_DOOR3_ID, 270)
                player.startConversation{
                    item(item.id, "You place the bone into the skeletal door and it fits. There are two recesses left now.")
                }
            }
            player.sendMessage("You fit the bone into the recess.")
        }
        if(player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_USED_THREE_BONES")) {
            Doors.handleDoubleDoor(player, World.getObject(Tile.of(2893,9480,0)))
        }
    }

    onItemOnObject(arrayOf("Ancient Door"), itemNamesOrIds = arrayOf(BONE_KEY_ID)){ (player, obj, item) ->
        player.sendMessage("You try the key with the lock")
        player.anim(PICKUP_ANIM_ID)
        WorldTasks.schedule(1) {
            player.sendMessage("A shimmering light dances over the doors, before you can blink, the doors creak open.")
        }
        WorldTasks.schedule(2) {
            player.vars.setVarBit(4882, 2)
        }
        WorldTasks.schedule(3) {
            player.startConversation{
                simple("The doors close behind you with the sound of crunching bone. Before you stretches a winding tunnel blocked by an ancient gate.")
                player.tele(2928, 9521, 0)
            }
            WorldTasks.schedule(1) {
                if (player.equipment.getNeckId() != BEADS_OF_THE_DEAD_ID) {
                    player.sendMessage("Rashiliyia appears!")
                    WorldTasks.schedule(5) {
                        player.sendMessage("You feel invisible hands starting to choke you...")
                    }
                }
            }
        }
    }

    onItemOnObject(arrayOf(TOMB_EXIT_ID), itemNamesOrIds = arrayOf(BONE_KEY_ID)){ (player, obj, item) ->
        player.sendMessage("You unlock the door with the key.")
        player.anim(PICKUP_ANIM_ID)
        WorldTasks.schedule(1) {
            player.sendMessage("The doors creak open revealing bright daylight.")
        }
        WorldTasks.schedule(2) {
            player.sendMessage("You walk outside into the warmth of the jungle heat.")
            player.tele(2916, 3092, 0)
        }
        //todo only if the dungeon isnt complete
        WorldTasks.schedule(3) {
            player.sendMessage("You get a sense that something seems incomplete.")
        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(UNROPED_FISSURE_ID), itemNamesOrIds = arrayOf("Rope")){ (player, obj, item) ->
        if(!lit) {
            player.startConversation {
                player(HeadE.CONFUSED, "There is not enough light to see a safe place place to anchor it.")
            }
        }
        if(lit) {
            player.startConversation {
                item(item.id,"You see where to attach the rope very clearly. You secure the rope well.")
            }
            player.anim(PICKUP_ANIM_ID)
            WorldTasks.schedule(2) {
                obj.setIdTemporary(2219, 360)
                player.inventory.removeItems(Item(item.id, 1))
            }



        }
    }

    onItemOnObject(objectNamesOrIds = arrayOf(STRANGE_STONE_ID), itemNamesOrIds = arrayOf("Chisel")){ (player) ->
        if(!player.inventory.containsItem(STONE_PLAQUE_ID) && !player.bank.containsItem(STONE_PLAQUE_ID)) {
            player.startConversation {
                item(
                    STONE_PLAQUE_ID,
                    "You cleanly cut the plaque of letters away from the rock. You place it carefully into your inventory."
                )
                player.anim(PICKUP_ANIM_ID)
                if (player.inventory.hasFreeSlots()) {
                    player.sendMessage("You add the stone plaque to your inventory.")
                    player.inventory.addItem(STONE_PLAQUE_ID, 1, false)
                    player.questManager.getAttribs(Quest.SHILO_VILLAGE).setB("HAS_COLLECTED_PLAQUE", true)
                } else {
                    player.sendMessage("You do not have enough room in your inventory.")
                }
            }
        }
    }

    onObjectClick(UNROPED_FISSURE_ID) { (player, obj, options) ->
        if (options == "Search") {
            player.startConversation {
                simple("You see a small fisure in the granite that you might just be able to crawl through. Beyond the fissure is a long fall. Do you want to try to crawl through the fissure?")
                options("CLIMB INTO THE FISURE?") {
                    op("Yes, I'll give it a go!") {
                        //Todo FAILURE BECAUSE THERES NO ROPE ATTACHED - FOLLOWING LINE IS IMPROV.
                        player(HeadE.CALM_TALK, "I had better use a rope.")

                    }
                    op("No thanks, I'm having second thoughts.") {
                        simple("You think better of attempting to squeeze into the fissure.")
                        exec {player.forceTalk("It looks very dangerous, and dark. Scary!")}
                    }
                }
            }
        }
        if (options == "Look-at") {
            player.startConversation {
                simple("It looks as if something is buried here.")
            }
        }
    }

    onObjectClick("Foliage") { (player, obj, options) ->
        if (options == "Search") {
            player.startConversation {
                simple("You search the folliage...")
                exec{
                    player.sendMessage("transforming varbit")
                    player.anim(PICKUP_ANIM_ID)
                    WorldTasks.schedule(2) {
                        player.vars.setVarBit(4882, 1)
                    }
                }
                simple("...and reveal an ancient doorway set into the side of the hill!")
            }
        }
    }

    onObjectClick(ANCIENT_METAL_GATE1_ID, ANCIENT_METAL_GATE2_ID) { (player, obj, options) ->
        if (options == "Search") {
            player.startConversation {
                simple("There is an ancient symbol on the gate. It looks like a human figure with something around its neck. It looks pretty scary.")
            }
        }else{
            if (player.equipment.getNeckId() == BEADS_OF_THE_DEAD_ID) {
                player.sendMessage("The Beads of the Dead start to glow...")
                Doors.handleDoubleDoor(player, obj)
            }
        }
    }

    onObjectClick("Ancient Door") { (player, obj, options) ->
        if (options == "Enter") {
            player.startConversation {
                player.vars.setVarBit(4882, 0)
            }
        }
        if (options == "Open") {
            player.startConversation {
                simple("There seems to be some sort of recepticle on the door. Perhaps it needs a key?")
            }
        }
        if (options == "Search") {
            player.startConversation {
                simple("Examining the door, you see that it has a very strange lock. You're shocked to find that it seems to be made out of bone!")
                exec{
                    player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_TRIED_LOCK", true)
                }
            }
        }
        if (options == "Examine") {
            player.startConversation {
                simple("There seems to be some sort of recepticle on the door. Perhaps it needs a key?")
            }
        }
    }

    onObjectClick(ROPED_FISSURE_ID) { (player, obj, options) ->
        if (options == "Look-at") {
            player.startConversation {
                simple("You see a small fissure in the granite that you might just be able to crawl through. You can see that a rope is attached nearby.")
            }
        }
        if (options == "Search") {
            player.startConversation {
                simple("You see a small fisure in the granite that you might just be able to crawl through. You can see that a rope is attached nearby. would you like to climb down?.")
                options("CLIMB INTO THE FISURE?") {
                    op("Yes, I'll give it a go!") {
                        exec {
                            player.anim(PICKUP_ANIM_ID)
                            WorldTasks.schedule(1) {
                                player.tele(2898, 9401, 0)
                                player.faceDir(Direction.SOUTH)
                            }
                            if(!player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_FOUND_AH_ZA_RHOON")) {
                                player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_FOUND_AH_ZA_RHOON", true)
                                player.setQuestStage(Quest.SHILO_VILLAGE, 3)
                            }
                        }
                    }
                    op("No thanks, I'm having second thoughts.") {
                        simple("You think better of attempting to squeeze into the fissure."){
                            exec {player.forceTalk("It looks very dangerous, and dark. Scary!")}
                        }
                    }
                }
            }
        }
    }

    onObjectClick(STRANGE_STONE_ID) { (player, obj, options) ->
        if (options == "Look-at") {
            player.startConversation {
                simple("This stone seems to have strange markings on it.")
            }
        }
        if (options == "Investigate") {
            player.startConversation {
                simple("This stone seems to have strange markings on it. Maybe Trufitus can decipher them? THe stone is too heavy to carry but the letters stand proud on a plaque. Maybe you could seperate the plaque from the rock?")
            }
        }
    }

    onObjectClick(WATERFALL_ROCKS_ID) { (player, obj,options) ->
        if (options == "Look-at") {
            player.startConversation {
                simple("There is a huge cascading waterfall in front of you, the roar of the water is quite loud. To the east you can see what looks like a trecherous pathway, it might lead outside.")
            }
        }
        if (options == "Search") {
            player.startConversation {
                simple("You see a huge waterfall blocking your path. The path leads on through the cascading waterfall, but it looks quite dangerous. Would you like to try and follow the path?")
                options("SELECT AN OPTION"){
                    op("Yes, I'll follow the path."){
                        exec {
                            player.sendMessage("You start carefully edging along the slippery path...")
                            player.lock(6)
                            WorldTasks.schedule(2) {
                                player.anim(PICKUP_ANIM_ID)
                            }
                            WorldTasks.schedule(4) {
                                player.tele(2923, 2997, 0)
                            }
                            //todo possibility of failure
                            //if the player fails:
                            //You fall!
                            //Player: Arrgghhhh!
                            //You're pumelled as the thrashing water throws
                            //you against the rocks.
                            //The player is dealt some damage.
                            //(End of dialogue)
                        }
                    }
                    op("No, I'll look for another exit."){
                        simple("You decide to have another look around. And see if you can find a better way to get out.")
                    }
                }
            }
        }
    }

    onObjectClick(LOOSE_ROCKS_ID) { (player) ->
        if(player.inventory.containsItem(TATTERED_SCROLL_ID) || player.bank.containsItem(TATTERED_SCROLL_ID)){
            player.sendMessage("You have already searched the rocks and retrieved a tattered scroll.")
            return@onObjectClick
        }
        player.startConversation {
            simple("You see that there is something hidden behind some rocks. Do you want to have a look? it looks a bit dangerous as the ceiling doesn't look at all safe")
            options("SELECT AN OPTION") {
                op("Yes, I'll carefully move the rocks to see what's behind them."){
                    simple("You start to slowly move the rocks to one side")
                    item(TATTERED_SCROLL_ID,"You carefully manage to remove enough rocks to see a book shelf. You gingerly remove a delicate scroll from the shelf and place it in your inventory"){
                        exec {
                            player.anim(PICKUP_ANIM_ID)
                            if (player.inventory.hasFreeSlots()) {
                                player.sendMessage("You add the tattered scroll to your inventory.")
                                player.inventory.addItem(TATTERED_SCROLL_ID,1,false)
                            } else {
                                player.sendMessage("You do not have enough room in your inventory.")
                            }
                        }
                    }
                }
                op("No, I'll leave them alone. I don't like the look of that ceiling.") {
                    simple("You decide to leave the rocks well alone. The ceiling does look a little unsafe.")
                }
            }
        }
    }

    onObjectClick(TOMB_EXIT_ID) { (player, obj, options) ->

        if (options == "Open") {
            player.sendMessage("The door seems to be locked!")
            WorldTasks.schedule(1) {
                player.forceTalk("Oh no, I'm going to be stuck in here forever!")
            }
            WorldTasks.schedule(3) {
                player.forceTalk("How will I ever get out!")
            }
            WorldTasks.schedule(5) {
                player.forceTalk("I'm too young to die!")
            }
        }
        if (options == "Search") {
            player.startConversation {
                simple("You can see a small recepticle, not unlike the one on the opposite side of the door!")
            }
        }

    }

    onObjectClick(SACKS_ID) { (player) ->
        if(player.inventory.containsItem(CRUMPLED_SCROLL_ID) || player.bank.containsItem(CRUMPLED_SCROLL_ID)){
            player.sendMessage("You have already searched the sacks and retrieved a crumpled scroll.")
            return@onObjectClick
        }
        player.itemDialogue(CRUMPLED_SCROLL_ID,"You find a tattered but very ornate scroll, which you place carefully in your inventory.")
        player.anim(PICKUP_ANIM_ID)
        if (player.inventory.hasFreeSlots()) {
            player.sendMessage("You add the crumpled scroll to your inventory.")
            player.inventory.addItem(CRUMPLED_SCROLL_ID, 1, false)
        } else {
            player.sendMessage("You do not have enough room in your inventory.")
        }
    }

    onObjectClick(GALLOWS_ID) { ( player, obj, options) ->
        if(player.inventory.containsItem(610) || player.bank.containsItem(610)){
            player.sendMessage("You have already searched the gallows and retrieved the corpse.")
            return@onObjectClick
        }
        if (options == "Look-at") {
            player.startConversation {
                simple("You take a look at th gallows. It's pretty eerie looking.")
                simple("A grisly sight meets your eyes. A human corpse hangs from the noose. His hands have been tied behind his back.")
            }
        }
        if (options == "Search") {
            player.startConversation {
                simple("You find a human corpse hanging in the noose. It looks as if the corpse can be removed easily. Would you like to remove the corpse from the noose?")
                options("SELECT AN OPTION") {
                    op("I don't think so, it might animate and attack me!") {
                        simple("You move away from the corpse quietly and slowly... ...you have an eerie feeling about this!"){
                            exec{
                                player.forceTalk("**GULP!**")
                            }
                        }
                    }
                    op("Yes, I may find something else on the corpse.") {

                        item(610, "You gently support the frame of the skeleton and lift the skull through the noose."){
                            exec{
                                player.sendMessage("You search the gallows...")
                            }
                        }
                        item(
                            610,
                            "You find an old sack and place the skeleton in it. Maybe Trufitus can give you some tips on what to do with it."
                        ) {
                            exec {
                                player.anim(PICKUP_ANIM_ID)
                                if (player.inventory.hasFreeSlots()) {
                                    player.sendMessage("You add the corpse to your inventory.")
                                    player.inventory.addItem(610, 1, false)
                                    if(!player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_COLLECTED_Z_CORPSE")) {
                                        if (player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_CRUMPLED_SCROLL")) {
                                            if (player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_STONE_PLAQUE")) {
                                                if (player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_READ_TATTERED_SCROLL")) {
                                                    player.setQuestStage(Quest.SHILO_VILLAGE, 4)
                                                }
                                            }
                                        }
                                    }
                                    player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_COLLECTED_Z_CORPSE", true)
                                } else {
                                    player.sendMessage("You do not have enough room in your inventory.")
                                }
                            }
                        }
                        simple("You sense that there is a spirit that needs to be put to rest.")
                    }
                }
            }
        }
    }

    onObjectClick(CAVE_IN_ID) { (player) ->
        player.startConversation {
            simple("You see that there is a narrow gap through into the darkness. You could try to wriggle through and see where it takes you")
            options("WRIGGLE THROUGH THE RUBBLE?") {
                op("Yes, I'll wriggle through.") {
                    exec {
                        player.anim(PICKUP_ANIM_ID)
                        WorldTasks.schedule(1) {
                            val dungtile = Tile.of(2889, 9285, 0)
                            if(player.tile.withinDistance(dungtile, 3)) {
                                player.tele(2888, 9373, 0)
                                player.faceDir(Direction.NORTH)
                            }else{
                                player.tele(2889, 9285, 0)
                                player.faceDir(Direction.NORTH)
                            }
                        }
                    }
                }
                op("No, I'll stay here.") {}
            }
        }
    }

    onObjectClick(BURIED_MOUND_ID) { (player, obj, options) ->
        if(player.getQuestStage(Quest.SHILO_VILLAGE) >= FIND_TEMPLE_OF_AH_ZA_RHOON) {
            if (options == "Search") {
                player.startConversation {
                    simple("It looks as if something is buried here. You may need some tools to excavate further.")
                    options("WHAT DO YOU WANT TO DO WITH THE MOUND?") {
                        op("Try and excavate the mound.") {
                            exec {
                                if (player.getInventory().containsItem(952, 1)) {
                                    player.lock(15)
                                    WorldTasks.schedule(1) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(3) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(5) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(7) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(9) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(11) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(13) {
                                        player.anim(DIG_ANIM_ID)
                                    }
                                    WorldTasks.schedule(14) {
                                        obj.setIdTemporary(UNROPED_FISSURE_ID, 3600)
                                    }
                                }else{
                                    player.sendMessage("You need to have a spade with you to excavate this.")
                                }
                            }
                        }
                        op("Leave the mound alone.") {}
                    }
                }
            }
            if (options == "Look-at") {
                player.startConversation {
                    simple("It looks as if something is buried here.")
                }
            }
        }
    }

    onObjectClick(WELL_STACKED_ROCKS_ID) { (player, obj, options) ->
        if(player.getQuestStage(Quest.SHILO_VILLAGE) >= INVESTIGATE_B_TOMB) {
            if (options == "Search") {
                player.startConversation {
                    simple("You investigate the rocks and find a dank, narrow crawl-way. Do you want to crawl into this dank, dark, narrow, possibly dangerous hole?<br>Crawl into hole?")
                    options("CRAWL INTO HOLE?") {
                        op("Yes please, I can think of nothing nicer!") {
                            exec {
                                player.lock(3)
                                WorldTasks.schedule(1) {
                                    player.anim(PICKUP_ANIM_ID)
                                }
                                WorldTasks.schedule(2) {
                                    player.tele(2763, 9375, 0)
                                }
                                player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).setB("HAS_FOUND_BERVIRIUS_TOMB", true)
                            }
                        }
                        op("No way could you get me to go in there!") {}
                    }
                }
            }
            if (options == "Investigate") {
                player.startConversation {
                    simple("These rocks look like they have been stacked uniformly.")
                }
            }
        }
    }

    onObjectClick(ROCKS_ID, checkDistance = true) { (player, obj, options) ->
        player.lock(3)
        player.anim(PICKUP_ANIM_ID)
        WorldTasks.schedule(1) {
            val dungtile2 = Tile.of(2929, 9516, 0)
            if (player.tile.withinDistance(dungtile2, 3)) {
                player.tele(2928, 9511, 0)
                player.faceDir(Direction.SOUTH)
            } else {
                player.tele(2929, 9516, 0)
                player.faceDir(
                    Direction.NORTH
                )
            }
        }
    }

    onObjectClick(ROCK_SLIDE_ID) { (player, obj, options) ->
        if (options == "Climb") {
            player.startConversation {
                simple("You attempt to climb the granite rock.")
                simple("You manage to climb back out again!") {
                    exec {
                        player.lock(3)
                        WorldTasks.schedule(1) {
                            player.anim(PICKUP_ANIM_ID)
                        }
                        WorldTasks.schedule(2) {
                            player.tele(2762, 2989, 0)
                        }
                    }
                }
            }
        }
    }

    onObjectClick(BONE_DOOR1_ID, BONE_DOOR2_ID, BONE_DOOR3_ID, BONE_DOOR4_ID, BONE_DOOR5_ID) { (player, obj, options) ->
        if (options == "Search") {
            player.startConversation {
                simple("The door is ornately carved with depictions of skeletal warriors. You notice that some of the skeletal warriors depictions are not complete. Instead, there are recesses where some of the bones should be. There are three recesses.")
            }
        }
        if (options == "Open") {
            player.startConversation {
                if(!player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_USED_THREE_BONES")) {
                    simple("The door won't budge.")
                }else{
                    Doors.handleDoubleDoor(player, World.getObject(Tile.of(2893,9480,0)))
                }
            }
        }
    }

    onObjectClick(R_DOLMEN_ID) { (player, obj, options) ->

        if (!isBossUp) {
            if (!player.getQuestManager().getAttribs(Quest.SHILO_VILLAGE).getB("HAS_KILLED_BOSS")) {
                player.startConversation {
                    simple("You touch the dolmen, and the ground starts to shake. You hear an unearthly voice booming and you step away from the dolmen in anticipation.")
                    exec {
                        spawnBoss(player)
                    }
                    return@startConversation
                }
            } else {
                if (!player.inventory.containsItem(R_CORPSE_ID) && !player.bank.containsItem(R_CORPSE_ID)) {
                    val chunk = ChunkManager.getChunk(player.chunkId)
                    val groundItemsMap = chunk.getGroundItems()
                    var foundCorpse = false
                    for ((_, tileMap) in groundItemsMap) {
                        for ((_, groundItemList) in tileMap) {
                            val iterator = groundItemList.iterator()
                            while (iterator.hasNext()) {
                                val groundItem = iterator.next()
                                if (groundItem.id == R_CORPSE_ID && groundItem.tile == Tile.of(2892,9487,0) && groundItem.creatorUsername == player.username) {
                                    val e1 = PickupItemEvent(player, groundItem, false)
                                    PluginManager.handle(e1)
                                    foundCorpse = true;
                                    if (!e1.isCancelPickup) {
                                        player.startConversation {
                                            if (options == "Search") {
                                                simple("You search the dolmen... And find the mumified remains of a human female.")
                                            }
                                            exec{
                                                player.soundEffect(2582, false)
                                                removeGroundItem(player, groundItem, true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!foundCorpse){
                        player.sendMessage("You see something appear on the dolmen.")
                        addGroundItem(Item(R_CORPSE_ID), Tile.of(2892, 9487, 0), player)
                    }
                }
            }
        }else {
            player.sendMessage("Tha dolmen remains silent.")
        }

    }

    onObjectClick(B_DOLMEN_ID) { (player, obj, options) ->

        val hasPommel = player.inventory.containsItem(SWORD_POMMEL_ID) || player.bank.containsItem(SWORD_POMMEL_ID)
        val hasSphere =
            player.inventory.containsItem(LOCATING_SPHERE_ID) || player.bank.containsItem(LOCATING_SPHERE_ID)
        val hasNotes = player.inventory.containsItem(BERVIRIUS_NOTES_ID) || player.bank.containsItem(BERVIRIUS_NOTES_ID)
        val missingItems = mutableListOf<Pair<Int, Int>>()

        if (!hasPommel) missingItems.add(SWORD_POMMEL_ID to 1)
        if (!hasSphere) missingItems.add(LOCATING_SPHERE_ID to 1)
        if (!hasNotes) missingItems.add(BERVIRIUS_NOTES_ID to 1)

        val itemCount = missingItems.size

        if (options == "Inspect") {
            when (player.getQuestStage(Quest.SHILO_VILLAGE)) {
                DEAL_WITH_RASHILIYIAS_CORPSE -> {
                    player.sendMessage("There is nothing new about this dolmen.")
                }
                INVESTIGATE_B_TOMB -> {
                    player.startConversation {
                        if (itemCount == 0) {
                            simple("You already have all of the items from the dolmen.")
                            return@startConversation
                        }
                        simple("The dolmen is intricately decorated with the symbol of two crossed palm trees. It might be the family crest? You can see that there ${if (itemCount > 1) "are some items" else "is an item"} on the dolmen.")
                    }
                }
            }
        }

        if (options == "Search") {
            when (player.getQuestStage(Quest.SHILO_VILLAGE)) {
                DEAL_WITH_RASHILIYIAS_CORPSE -> {
                    player.sendMessage("There is nothing new about this dolmen.")
                }
                INVESTIGATE_B_TOMB -> {
                    player.startConversation {
                        if (itemCount == 0) {
                            simple("You already have all of the items from the dolmen.")
                            return@startConversation
                        }
                        simple("The dolmen is intricately decorated with the symbol of two crossed palm trees. It might be the family crest? You can see that there ${if (itemCount > 1) "are some items" else "is an item"} on the dolmen.")
                        if (player.inventory.freeSlots >= itemCount) {
                            for ((id, amount) in missingItems) {
                                if (id == SWORD_POMMEL_ID) {
                                    item(
                                        id,
                                        "You find a rusty sword with an ivory pommel. You take the Ivory pommel and place it in your inventory."
                                    )
                                }
                                if (id == LOCATING_SPHERE_ID) {
                                    item(
                                        id,
                                        "You find a Crystal Sphere. You take the crystal and place it carefully in your inventory."
                                    )
                                }
                                if (id == BERVIRIUS_NOTES_ID) {
                                    item(
                                        id,
                                        "You find some writing on the dolmen, you grab some nearby scraps of delicate paper together and copy the text as best you can and collect them together as a scroll."
                                    )
                                }
                                player.inventory.addItem(id, amount)
                            }
                            player.setQuestStage(Quest.SHILO_VILLAGE, RETURN_TO_TRUFITUS_AGAIN)
                        } else {
                            player.sendMessage("You need $itemCount free inventory slot${if (itemCount > 1) "s" else ""} to collect the item${if (itemCount > 1) "s" else ""} from the dolmen.")
                        }
                    }
                }
            }
        }
    }

    onNpcClick(VIGROY_NPC_ID){ (player, npc) ->
        if(player.questManager.isComplete(Quest.SHILO_VILLAGE)){
            player.startConversation {
                npc(npc,HeadE.CALM_TALK,"I am offering a cart ride to Brimhaven if you're interested. It will cost 10 gold coins...")
                options("Select an option") {
                    op("Yes please. I'd like to go to Brimhaven."){
                        if (player.getInventory().hasCoins(10)) {
                            player.getInventory().removeCoins(10)
                            simple("You hop into the cart and the driver urges The horses on. You take a taxing journey through the jungle to Brimhaven.")
                            exec{player.fadeScreen(Runnable {player.tele(Tile.of(2779, 3213, 0)) })}
                            simple("You feel tired from the journey, but at least you didn't have to walk all that distance.")
                        } else player.sendMessage("You don't have enough coins for the ride.")
                    }
                    op("No, thanks."){
                        player(HeadE.CALM_TALK,"No, thanks.")
                    }
                }
            }
        }else{
            player.sendMessage("He seems like he doesn't like strangers.")
        }
    }

    onNpcClick(HAGEDY_NPC_ID){ (player, npc) ->
        if(player.questManager.isComplete(Quest.SHILO_VILLAGE)){
            player.startConversation {
                npc(npc,HeadE.CALM_TALK,"I am offering a cart ride to Shilo Village if you're interested. It will cost 10 gold coins...")
                options("Select an option") {
                    op("Yes please. I'd like to go to Shilo Village."){
                        if (player.getInventory().hasCoins(10)) {
                            player.getInventory().removeCoins(10)
                            simple("You hop into the cart and the driver urges The horses on. You take a taxing journey through the jungle to Shilo Village.")
                            exec{player.fadeScreen(Runnable {player.tele(Tile.of(2831, 2955, 0)) })}
                            simple("You feel tired from the journey, but at least you didn't have to walk all that distance.")
                        } else player.sendMessage("You don't have enough coins for the ride.")
                    }
                    op("No, thanks."){
                        player(HeadE.CALM_TALK,"No, thanks.")
                    }
                }
            }
        }else{
            player.sendMessage("He seems like he doesn't like strangers.")
        }
    }

    onObjectClick(VIGROY_CART_ID){(player, obj, options) ->
        if(player.questManager.isComplete(Quest.SHILO_VILLAGE)) {
            if (player.getInventory().hasCoins(10)) {
                player.getInventory().removeCoins(10)
                player.startConversation {
                    simple("You hop into the cart and the driver urges The horses on. You take a taxing journey through the jungle to Brimhaven.")
                    exec { player.fadeScreen(Runnable { player.tele(Tile.of(2779, 3213, 0)) }) }
                    simple("You feel tired from the journey, but at least you didn't have to walk all that distance.")
                }
            } else player.sendMessage("You don't have enough coins for the ride.")
        }else player.sendMessage("The driver doesn't like strangers.")
        return@onObjectClick
    }

    onObjectClick(HAGEDY_CART_ID) { (player, obj, options) ->
        if (player.questManager.isComplete(Quest.SHILO_VILLAGE)) {
            if (player.getInventory().hasCoins(10)) {
                player.getInventory().removeCoins(10)
                player.startConversation {
                    simple("You hop into the cart and the driver urges The horses on. You take a taxing journey through the jungle to Shilo Village.")
                    exec { player.fadeScreen(Runnable { player.tele(Tile.of(2831, 2955, 0)) }) }
                    simple("You feel tired from the journey, but at least you didn't have to walk all that distance.")
                }
            } else player.sendMessage("You don't have enough coins for the ride.")
        } else player.sendMessage("The driver doesn't like strangers.")
        return@onObjectClick
    }

    onObjectClick(SHILO_GATE_ID) { e ->
        if (e.player.questManager.isComplete(Quest.SHILO_VILLAGE)) {
            e.player.startConversation {
                simple("You approach the cart and see undead creatures gathering by the village gates. There is a note attached to the cart. The note says, :: Danger :: Deadly green mist DO NOT ENTER IF YOU VALUE YOUR LIFE !")
                options(""){
                    op("Yes, I am very nimble and agile!"){
                        exec{
                            e.player.sendMessage("You nimbly jump from one side of the cart...")
                            e.player.ladder(
                                if (e.player.x > e.obj.x) e.player.transform(-4, 0, 0)
                                else e.player.transform(4, 0, 0)
                            )
                            WorldTasks.schedule(2) {
                                e.player.sendMessage("...to the other and climb down again.")
                            }
                        }
                    }
                    op("No, I am happy where I am thanks!"){
                        exec{
                            e.player.sendMessage("You think better of clambering over the cart, you might get dirty.")
                            WorldTasks.schedule(2) {
                                e.player.forceTalk("I'd probably have just scraped my knees up as well.")
                            }
                        }
                    }
                }
            }

        }else{
            e.player.startConversation {
                simple("The village looks like it isn't safe to enter.")
            }
        }
        return@onObjectClick
    }
}

