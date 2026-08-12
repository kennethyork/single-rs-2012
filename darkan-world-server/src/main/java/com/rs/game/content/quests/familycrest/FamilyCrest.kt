package com.rs.game.content.quests.familycrest

import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.lib.Constants
import com.rs.lib.game.Item
import com.rs.plugin.annotations.PluginEventHandler
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemClick
import com.rs.plugin.kts.onItemOnItem

const val NOT_STARTED = 0
const val TALK_TO_CALEB = 1
const val TALK_TO_GEM_TRADER = 2
const val TALK_TO_AVAN = 3
const val TALK_TO_BOOT = 4
const val GIVE_AVAN_JEWLERY = 5
const val TALK_TO_JOHNATHAN = 6
const val KILL_CHRONOZON = 7
const val QUEST_COMPLETE = 8

// Items
const val PERFECT_RUBY_RING = 773
const val PERFECT_RUBY_NECKLACE = 774
const val CALEB_CREST = 779
const val AVAN_CREST = 780
const val JOHNATHAN_CREST = 781
const val FAMILY_CREST_ITEM = 782
const val COOKING_GAUNTLETS = 775
const val GOLDSMITH_GAUNTLETS = 776
const val CHAOS_GAUNTLETS = 777
const val FAMILY_GAUNTLETS = 778

const val PERFECT_ORE = 446
const val PERFECT_BAR = 2365
const val RUBY = 1603
const val RING_MOULD = 1592
const val NECKLACE_MOULD = 1597

@QuestHandler(
    quest = Quest.FAMILY_CREST,
    startText = "Talk to Dimintheis in south-east Varrock, next to the Fancy Dress Shop.",
    itemsText = "Cooked shrimp, cooked salmon, cooked tuna, cooked bass, cooked swordfish, 2 cut rubies, antipoison potion. You will likely need a magic weapon to cast Air Blast, Water Blast, Earth Blast, Fire Blast (59 Magic) from the normal spellbook.",
    combatText = "You will need to defeat a level 84 demon.",
    rewardsText = "Family gauntlets (chaos gauntlets, cooking gauntlets, smelting gauntlets)<br>Access to the hellhounds in Witchaven Dungeon",
    completedStage = QUEST_COMPLETE
)
@PluginEventHandler
class FamilyCrest : QuestOutline() {

    override fun getJournalLines(player: Player, stage: Int) = when (stage) {
        NOT_STARTED -> listOf(
            "There is a man in Varrock who needs a bit of help.",
            "He is of noble heritage, but without his family's",
            "crest, he can't prove it. Unfortunately, his three",
            "sons took the crest with them when they left,",
            "scattering all across RuneScape.",
            "",
            "~~~Requirements~~~",
            "40 Mining",
            "40 Smithing",
            "59 Magic",
            "40 Crafting",
            ""
        )

        TALK_TO_CALEB -> {
            val lines = mutableListOf(
                "Speak to Caleb in Catherby to ask about the family",
                "crest.",
                ""
            )
            if (player.questManager.getAttribs(Quest.FAMILY_CREST).getB("CALEB_ASKED")) {
                lines.add("I must get Caleb cooked shrimp, salmon, tuna, bass and")
                lines.add("swordfish to get a piece of the crest.")
                lines.add("")
            }
            lines.toList()
        }

        TALK_TO_GEM_TRADER -> listOf(
            "Caleb says Avan is looking for treasure in Al-Kharid. ",
            "Talk to the gem trader in town to learn more.",
            ""
        )

        TALK_TO_AVAN -> listOf(
            "Avan is looking for treasure by the mines in north",
            "Al-Kharid.",
            ""
        )

        TALK_TO_BOOT -> listOf(
            "Avan says he is looking for the 'perfect' gold. He",
            "has a solid lead on the ore from a dwarf named Boot",
            "in the Dwarven Mines of Falador. You must talk to",
            "Boot to progress.",
            ""
        )

        GIVE_AVAN_JEWLERY -> listOf(
            "Boot said there is 'perfect' ore in the Witchaven dungeon in",
            "east Ardougne.",
            ""
        )

        TALK_TO_JOHNATHAN -> listOf(
            "Now that I have two of three crest pieces I only need",
            "one more from Johnathon in north east Varrock",
            ""
        )

        KILL_CHRONOZON -> listOf(
            "Now I just need to kill Chronozon for the last crest piece",
            "I can find him somewhere in Edgeville dungeon. Look south",
            "of the earth obelisk.",
            "",
            "Afterwards I just need to put the pieces back together and",
            "return to Dimintheis."
        )

        QUEST_COMPLETE -> listOf(
            "",
            "",
            "QUEST COMPLETE!"
        )

        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        if (player.inventory.hasFreeSlots())
            player.inventory.addItem(FAMILY_GAUNTLETS, 1)
        sendQuestCompleteInterface(player, 778)
    }

    companion object {
        @JvmStatic
        fun meetsRequirements(p: Player): Boolean {
            val skill = p.skills
            return skill.getLevel(Constants.MINING) >= 40
                    && skill.getLevel(Constants.SMITHING) >= 40
                    && skill.getLevel(Constants.MAGIC) >= 59
                    && skill.getLevel(Constants.CRAFTING) >= 40
        }
    }
}

@ServerStartupEvent
fun mapFamilyCrestInteractions() {

    onItemOnItem(intArrayOf(CALEB_CREST, AVAN_CREST, JOHNATHAN_CREST), intArrayOf(CALEB_CREST, AVAN_CREST, JOHNATHAN_CREST)) { e ->
        val inv = e.player.inventory
        if (inv.containsItem(CALEB_CREST, 1) && inv.containsItem(AVAN_CREST, 1) && inv.containsItem(JOHNATHAN_CREST, 1)) {
            inv.removeItems(Item(CALEB_CREST, 1), Item(AVAN_CREST, 1), Item(JOHNATHAN_CREST, 1))
            inv.addItem(Item(FAMILY_CREST_ITEM, 1), true)
        }
    }

    onItemClick(COOKING_GAUNTLETS, GOLDSMITH_GAUNTLETS, CHAOS_GAUNTLETS, options = arrayOf("Wear")) { (player, item) ->
        if (player.isEquipDisabled)
            return@onItemClick
        if (!player.isQuestComplete(Quest.FAMILY_CREST)) {
            player.sendMessage("You must complete the Family Crest quest to use this item...")
            return@onItemClick
        }
        Equipment.sendWear(player, item.slot, item.id)
    }
}

fun familyCrestItemOnFurnace(player: Player, item: Item) {
    if (item.id == PERFECT_ORE) {
        player.inventory.replaceItem(PERFECT_BAR, 1, item.slot)
        player.anim(3243)
        return
    }
    if (item.id == PERFECT_BAR) {
        val hasRingMaterials = player.inventory.containsItem(RING_MOULD, 1, true) && player.inventory.containsItem(RUBY, 1)
        val hasNeckMaterials = player.inventory.containsItem(NECKLACE_MOULD, 1, true) && player.inventory.containsItem(RUBY, 1)
        if (!hasRingMaterials && !hasNeckMaterials) {
            player.sendMessage("You don't have a mould to make anything with this.")
            return
        }
        player.startConversation {
            options("Choose an option:") {
                if (hasRingMaterials) {
                    op("Make perfect ring") {
                        exec {
                            player.anim(3243)
                            player.inventory.removeItems(Item(RUBY, 1))
                            player.inventory.replaceItem(PERFECT_RUBY_RING, 1, item.slot)
                        }
                    }
                }
                if (hasNeckMaterials) {
                    op("Make perfect neck") {
                        exec {
                            player.anim(3243)
                            player.inventory.removeItems(Item(RUBY, 1))
                            player.inventory.replaceItem(PERFECT_RUBY_NECKLACE, 1, item.slot)
                        }
                    }
                }
            }
        }
    }
}