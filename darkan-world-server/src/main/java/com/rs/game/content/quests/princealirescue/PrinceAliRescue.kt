package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.World
import com.rs.game.content.items.Dye
import com.rs.game.content.world.doors.Doors.handleDoor
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Animation
import com.rs.lib.game.Item
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnItem
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onItemOnObject
import com.rs.plugin.kts.onObjectClick

@QuestHandler(
    quest = Quest.PRINCE_ALI_RESCUE,
    startText = "Speak to Hassan in the palace at Al Kharid.",
    itemsText = "Soft clay<br>3 balls of wool<br>Yellow dye or 2 onions and 5 coins<br>Redberries<br>Ashes<br>Bucket of water or jug of water (obtainable during quest)<br>Pot of flour<br>Bronze bar<br>Pink skirt<br>3 beers<br>Rope (can be bought during the quest at Ned for 18 coins or 4 balls of wool)<br>At least 100 coins",
    combatText = "The ability to get past aggressive combat level 26 jail guards.",
    rewardsText = "700 coins<br>Free passage through the Lumbridge -> Al Kharid toll gate from now on<br>(Members) Access to the Sorceress's Garden Thieving minigame.",
    completedStage = 5
)
class PrinceAliRescue : QuestOutline() {

    override fun getJournalLines(player: Player, stage: Int): List<String> = when (stage) {
        NOT_STARTED -> listOf(
            "Prince Ali of Al Kharid has been kidnapped by ",
            "the scheming Lady Keli. You are hired to stage a rescue mission.",
            "",
            "Speak to Chancellor Hassan at the Al-Kharid palace  to start",
            "your mission.",
            ""
        )
        STARTED -> listOf(
            "The mission has started, I must speak to Osman outside the palace",
            "to the northwest by the Alkharid Scimitar shop.",
            ""
        )
        GEAR_CHECK -> {
            val inv = player.inventory
            val lines = mutableListOf(
                "Prince Ali has been taken for randsom! To break",
                "him out of his prison we are going to need to",
                "infiltrate the jail and get him out...",
                "",
                "For this we will need a skin paste, a yellow wig, some beer,",
                "the jail cell key and a pink skirt.",
                "",
                "For the skin paste I can talk to Aggie the witch in ",
                "Draynor.",
                "",
                "For the yellow wig I can talk to Ned in Draynor.",
                "",
                "I remember a pink skirt being sold in Varrock's",
                "clothing store",
                "",
                "For the jail key I will have to be really sneaky and",
                "get a key print from Lady Keli. Afterward I can get a",
                "bronze bar turned into a key by talking to Osman",
                "in Alkharid then pick up the key delivery from Leela.",
                "",
                "Before I start the breakout I will need the following items:",
                (if (inv.containsItem(BEER, 3)) "<str>" else "") + "3 beer",
                (if (inv.containsItem(ROPE, 1)) "<str>" else "") + "Rope",
                (if (inv.containsItem(BRONZE_KEY, 1)) "<str>" else "") + "A jail key",
                (if (inv.containsItem(BLONDE_WIG, 1)) "<str>" else "") + "A yellow wig",
                (if (inv.containsItem(PASTE, 1)) "<str>" else "") + "Skin paste",
                (if (inv.containsItem(PINK_SKIRT, 1)) "<str>" else "") + "A pink skirt",
                "",
                "Once I gathered all these supplies I should talk to Leela.",
                ""
            )
            lines
        }
        4 -> listOf("", "")
        5 -> listOf("", "", "QUEST COMPLETE!")
        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        player.inventory.addCoins(700)
        sendQuestCompleteInterface(player, 6964)
    }

//    override fun updateStage(player: Player?, stage: Int) {
//        super.updateStage(player, stage)
//        if(player?.hasRights(Rights.DEVELOPER) == true)
//        player.sendMessage("Prince Ali stage $stage")
//    }

    companion object {
        // Quest Stages
        const val NOT_STARTED = 0
        const val STARTED = 1
        const val GEAR_CHECK = 2

        // Items
        const val BEER = 1917
        const val PINK_SKIRT = 1013

        // Key stuff
        const val SOFT_CLAY = 1761
        const val BRONZE_KEY = 2418
        const val KEY_PRINT = 2423
        const val BRONZE_BAR = 2349

        // Wig items
        const val BALL_WOOL = 1759
        const val WIG = 2421
        const val BLONDE_WIG = 2419

        // Paste items
        const val ASHES = 592
        const val REDBERRY = 1951
        const val POT_OF_FLOUR = 1933
        const val WATER_BUCKET = 1929
        const val PASTE = 2424

        const val ROPE = 954

        // NPCs
        const val NED = 918
        const val AGGIE = 922
        const val HASSAN = 923
        const val OSMAN = 5282
        const val LEELA = 915
        const val LADY_KELI = 919
        const val JOE = 916
        const val PRINCE_ALI1 = 920
        const val PRINCE_ALI2 = 921

        // Places
        const val JAIL_REGION_ID = 12338 //Update to new logic

        @JvmStatic
        fun hasAllBreakOutItems(p: Player): Boolean =
            p.inventory.containsItem(BEER, 3)
                    && p.inventory.containsItem(BRONZE_KEY, 1)
                    && p.inventory.containsItem(PASTE, 1)
                    && p.inventory.containsItem(BLONDE_WIG, 1)
                    && p.inventory.containsItem(PINK_SKIRT, 1)
                    && p.inventory.containsItem(ROPE, 1)
    }
}

@ServerStartupEvent
fun mapPrinceAliRescue() {
    onObjectClick(3436) { e ->
        if (e.obj.tile.matches(Tile.of(3128, 3243, 0))) {
            if (e.player.inventory.containsItem(PrinceAliRescue.BRONZE_KEY, 1)) {
                handleDoor(e.player, e.obj)
            } else {
                e.player.startConversation {
                    player(HeadE.CALM_TALK, "It appears to need a key...")
                }
            }
            return@onObjectClick
        }

        if (e.player.inventory.containsItem(PrinceAliRescue.BRONZE_KEY, 1)) {
            for (npc in World.getNPCsInChunkRange(e.player.chunkId, 2)) {
                if (npc.id == PrinceAliRescue.LADY_KELI) {
                    e.player.sendMessage("You'd better get rid of Lady Keli before trying to go through there.")
                    return@onObjectClick
                }
            }
            handleDoor(e.player, e.obj)
        } else {
            e.player.startConversation {
                player(HeadE.CALM_TALK, "It appears to need a key...")
            }
        }
    }

    onItemOnItem(intArrayOf(Dye.YELLOW.id), intArrayOf(PrinceAliRescue.WIG)) { e ->
        if (e.usedWith(PrinceAliRescue.WIG, Dye.YELLOW.id)) {
            e.player.inventory.replace(e.item2, Item(PrinceAliRescue.BLONDE_WIG, 1))
            e.player.inventory.deleteItem(e.item1.slot, e.item1)
        }
    }

    onItemOnNpc(PrinceAliRescue.LADY_KELI) { e ->
        if (e.item.id != PrinceAliRescue.ROPE || e.player.isQuestComplete(Quest.PRINCE_ALI_RESCUE))
            return@onItemOnNpc
        val p = e.player
        if (p.questManager.getAttribs(Quest.PRINCE_ALI_RESCUE).getB("Joe_guard_is_drunk")
            && p.inventory.containsItem(PrinceAliRescue.BRONZE_KEY, 1)
            && p.inventory.containsItem(PrinceAliRescue.PASTE, 1)
            && p.inventory.containsItem(PrinceAliRescue.BLONDE_WIG, 1)
            && p.inventory.containsItem(PrinceAliRescue.PINK_SKIRT, 1)
            && p.inventory.containsItem(PrinceAliRescue.ROPE, 1)
        ) {
            p.inventory.removeItems(Item(PrinceAliRescue.ROPE, 1))
            e.npc.setRespawnTask(90)
            e.npc.finish()
        } else {
            p.startConversation {
                simple("You cannot tie Keli up until you have all equipment and disabled the guard!")
            }
        }
    }
    onItemOnObject(
        objectNamesOrIds = arrayOf("Furnace"), itemNamesOrIds = arrayOf(PrinceAliRescue.KEY_PRINT)) { (player) ->
        if(player.inventory.containsItem(PrinceAliRescue.BRONZE_BAR)) {
            player.anim(Animation(3243))
            player.inventory.deleteItem(PrinceAliRescue.BRONZE_BAR, 1)
            player.inventory.deleteItem(PrinceAliRescue.KEY_PRINT, 1)
            player.inventory.addItem(PrinceAliRescue.BRONZE_KEY, 1)
            player.sendMessage("You make a duplicate Key from the imprint.")
        }
        else
            player.sendMessage("You will need a Bronze Bar before you can duplicate the Key.")
    }
}