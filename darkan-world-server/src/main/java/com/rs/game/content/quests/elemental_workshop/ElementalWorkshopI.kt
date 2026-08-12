package com.rs.game.content.quests.elemental_workshop

import com.rs.engine.book.Book
import com.rs.engine.book.BookPage
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.World
import com.rs.game.content.world.doors.Doors
import com.rs.game.model.entity.async.schedule
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.*

const val STAGE_UNSTARTED = 0
const val STAGE_READ_BOOK = 1
const val STAGE_SLASHED_BOOK = 2
const val STAGE_OPEN_ODD_WALL = 3
const val STAGE_FOUND_WORKSHOP = 4
const val STAGE_COMPLETE = 5

private const val BOOKCASE = 26113
private const val ODD_LOOKING_WALL_1: Int = 26114
private const val ODD_LOOKING_WALL_2: Int = 26115
private const val STAIRCASE_DOWN = 3415
private const val STAIRCASE_UP = 3416
private const val HATCH = 3413
private const val FURNACE = 3410
private const val WORKBENCH = 3402
private const val BELLOWS = 3407
private const val WATER_CONTROLS_LEFT = 3404
private const val WATER_CONTROLS_RIGHT = 3403
private const val WATER_LEVER = 3406
private const val AIR_LEVER = 3409
private const val LEVER_PULLED = 3417
private const val RANDOM_CRATE_1 = 3394
private const val RANDOM_CRATE_2 = 3395
private const val RANDOM_CRATE_3 = 3397
private const val RANDOM_CRATE_4 = 3398
private const val RANDOM_CRATE_5 = 3399
private const val RANDOM_CRATE_6 = 3400
private const val RANDOM_CRATE_7 = 3401
private val LAVA_TROUGHS: Array<Any> = (18519..18523).toList().toTypedArray()
private val RANDOM_CRATES = arrayOf(RANDOM_CRATE_1, RANDOM_CRATE_2, RANDOM_CRATE_3, RANDOM_CRATE_4, RANDOM_CRATE_5, RANDOM_CRATE_6, RANDOM_CRATE_7)

private const val BATTERED_BOOK = 2886
private const val BATTERED_KEY = 2887
private const val SLASHED_BOOK = 9715
private const val ELEMENTAL_SHIELD = 2890
private const val ELEMENTAL_ORE = 2892
private const val ELEMENTAL_BAR = 2893
private const val STONE_BOWL = 2888
private const val STONE_BOWL_LAVA = 2889
private const val COAL = 453
private const val LEATHER = 1741
private const val THREAD = 1734
private const val NEEDLE = 1733
private const val HAMMER = 2347

private const val NPC_EARTH_ELEMENTAL_ROCK = 4910
private const val NPC_ELEMENTAL_ROCK = 4911

private const val ANIM_MAKE_SHIELD = 896
private const val ANIM_SMELTING = 899
private const val ANIM_ROCK_WAKE_UP = 4865
private const val ANIM_TURN_WATER_CONTROLS = 4861
private const val ANIM_FILL_STONE_BOWL = 4862
private const val ANIM_FIX_BELLOWS = 4863

private const val VAR_WATERWHEEL_MACHINERY = 2060
private const val VAR_WATER_CONTROLS_LEFT = 2058
private const val VAR_WATER_CONTROLS_RIGHT = 2059
private const val VAR_FIXED_BELLOWS = 2061
private const val VAR_BELLOWS = 2063
private const val VAR_HATCH = 2641
private const val VAR_FURNACE = 2062

private const val SOUND_LEVER = 2400
private const val SOUND_DOOR_LOCKED = 2402
private const val SOUND_DOOR_SCRAPE = 2410
private const val SOUND_LAVA_BOWL_FILL = 1537
private const val SOUND_LAVA_BOWL_POUR = 1538
private const val SOUND_WATERWHEEL_RESET = 1540
private const val SOUND_SQUEEKY_VALVE = 1541
private const val SOUND_WATERWHEEL_START = 1547
private const val SOUND_WATERWHEEL_STOP = 1548
private const val SOUND_SHIELD_MAKE = 2721

private val WORKSHOP_STAIRS_DOWN: Tile = Tile.of(2716, 9888, 0)
private val WORKSHOP_STAIRS_UP: Tile = Tile.of(2709, 3498, 0)

class ElementalShieldBook : Book(
    "Book of the Elemental Shield",
    BookPage(
        arrayOf(
            "Within the pages of this book",
            "you will find the secret to",
            "working the very elements",
            "themselves.",
            "Early in the fifth age, a new",
            "ore was discovered. This ore",
            "has a unique property of",
            "absorbing, transforming or",
            "focusing elemental energy. A",
            "workshop was erected close",
            "by to work this new material.",
            "The workshop was set up for",
            "artisans and inventors to be",
            "able to come and create",
            "devices made from the"
        ),
        arrayOf(
            "unique ore, found only in the",
            "village of the Seers."
        )
    ),
    BookPage(
        arrayOf(
            "After some time of successful",
            "industry the true power of",
            "this ore became apparent, as",
            "greater and more powerful",
            "weapons were created.",
            "Realising the threat this",
            "posed, the magi of the time",
            "closed down the workshop",
            "and bound it under lock and",
            "key, also trying to destroy all",
            "knowledge of manufacturing",
            "processes. Yet this book",
            "remains and you may still",
            "find a way to enter the",
            "workshop within this leather"
        ),
        arrayOf("bound volume.")
    )
)

fun Player.readElementalShieldBook() {
    sendMessage("The book has two parts: an introduction and an instruction section.")
    sendMessage("You flip the book open to the introduction and start reading.")
    openBook(ElementalShieldBook())
}

@ServerStartupEvent
fun mapWorkshop1() {

    onItemClick(ELEMENTAL_SHIELD, options = arrayOf("Wield")) { (player, item) ->
        if (player.isEquipDisabled)
            return@onItemClick
        if (!player.isQuestComplete(Quest.ELEMENTAL_WORKSHOP_I)) {
            player.sendMessage("You must complete the Elemental Workshop I quest to use this item...")
            return@onItemClick
        }
        Equipment.sendWear(player, item.slot, item.id)
    }

    onObjectClick(BOOKCASE) {
        it.player.apply {
            val item = if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) >= STAGE_SLASHED_BOOK)
                SLASHED_BOOK else BATTERED_BOOK

            startConversation {
                if (inventory.containsOneItem(BATTERED_BOOK, SLASHED_BOOK)) {
                    item(item, "There is a book here titled 'The Elemental Shield'. It can stay here, as you have a copy in your backpack.")
                } else if (bank.containsItem(BATTERED_BOOK) || bank.containsItem(SLASHED_BOOK)) {
                    item(item, "There is a book here titled 'The Elemental Shield'. It can stay here, as you have a copy in the bank.")
                } else if (inventory.freeSlots == 0) {
                    item(item, "You find a book titled 'The Elemental Shield'. It can stay here, as you don't have space in your backpack.")
                } else {
                    item(item, "You find a book titled 'The Elemental Shield'.") {
                        inventory.addItem(item, 1)
                    }
                }
                if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) >= STAGE_SLASHED_BOOK) {
                    if (inventory.freeSlots == 0) {
                        item(BATTERED_KEY, "You also find a key. It can also stay here, as you don't have space in your backpack.")
                    } else if (!inventory.containsOneItem( BATTERED_KEY) && !bank.containsItem(BATTERED_KEY)) {
                        item(BATTERED_KEY, "You also find a key.") {
                            inventory.addItem(BATTERED_KEY)
                        }
                    }
                }
            }

        }
    }

    onObjectClick(ODD_LOOKING_WALL_1, ODD_LOOKING_WALL_2) { (player, obj) ->
        player.apply {
            if (inventory.containsItem(BATTERED_KEY)) {
                sendMessage("You use the battered key to open the doors.")
                soundEffect(SOUND_DOOR_SCRAPE, false)
                Doors.handleDoubleDoor(player, obj)
                if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) == STAGE_SLASHED_BOOK) {
                    setQuestStage(Quest.ELEMENTAL_WORKSHOP_I, STAGE_OPEN_ODD_WALL)
                }
                return@onObjectClick
            }
            if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) >= STAGE_OPEN_ODD_WALL) {
                soundEffect(SOUND_DOOR_SCRAPE, false)
                Doors.handleDoubleDoor(player, obj)
                return@onObjectClick
            }
            soundEffect(SOUND_DOOR_LOCKED, false)
            sendMessage("You see a small hole in the wall but no way to open it.")
        }
    }

    onItemOnObject(arrayOf(ODD_LOOKING_WALL_1, ODD_LOOKING_WALL_2)) { (player, obj, item) ->
        player.apply {
            when (item.id) {
                BATTERED_KEY -> {
                    soundEffect(SOUND_DOOR_SCRAPE, false)
                    Doors.handleDoubleDoor(player, obj)
                }
                else -> {
                    soundEffect(SOUND_DOOR_LOCKED, false)
                    sendMessage("You can't unlock the door with that.")
                }
            }
        }
    }

    onObjectClick(STAIRCASE_DOWN) {
        it.player.apply {
            schedule {
                useStairs(WORKSHOP_STAIRS_DOWN)
                wait(3)
                musicsManager.playSongAndUnlock(88)
                if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) == STAGE_OPEN_ODD_WALL) {
                    setQuestStage(Quest.ELEMENTAL_WORKSHOP_I, STAGE_FOUND_WORKSHOP)
                    playerDialogue(HeadE.CALM_TALK, "Now to explore this area thoroughly, to find what forgotten secrets it contains.")
                }
            }
        }
    }

    onObjectClick(STAIRCASE_UP) { it.player.useStairs(WORKSHOP_STAIRS_UP) }

    onItemClick(BATTERED_BOOK, SLASHED_BOOK, options = arrayOf("Read", "Cut-open")) { (player, item, option) ->
        player.apply {
            if (!isQuestStarted(Quest.ELEMENTAL_WORKSHOP_I)) {
                when (option) {
                    "Read" -> {
                        startConversation {
                            questStart(Quest.ELEMENTAL_WORKSHOP_I)
                            exec {
                                setQuestStage(Quest.ELEMENTAL_WORKSHOP_I, STAGE_READ_BOOK)
                                readElementalShieldBook()
                            }
                        }
                    }
                    "Cut-open" -> sendMessage("You don't want to damage the book")
                }
            } else {
                when (option) {
                    "Read" -> readElementalShieldBook()
                    "Cut-open" -> {
                        // TODO: When we get a list of all slash items we can guard this behind knives or scimitars and stuff
                        inventory.replace(BATTERED_BOOK, SLASHED_BOOK)
                        inventory.addItemDrop(BATTERED_KEY)
                        sendMessage("You make a small cut in the spine of the book.")
                        sendMessage("Inside you find a small, old, battered key.")
                        setQuestStage(Quest.ELEMENTAL_WORKSHOP_I, STAGE_SLASHED_BOOK)
                    }
                }
            }
        }
    }

    onNpcClick(NPC_ELEMENTAL_ROCK) { (player, npc) ->
        if (player.skills.getLevel(Skills.MINING) < 20) {
            player.sendMessage("You need a Mining level of at least 20 to mine elemental ore.")
            return@onNpcClick
        }
        val npcs: List<NPC> = World.getNPCsInChunkRange(npc.chunkId, 1)
        npcs.forEach {
            if (it.id == NPC_EARTH_ELEMENTAL_ROCK && (it.combatTarget == player && it.lineOfSightTo(player, false))) {
                it.forceTalk("I told you... Ge'roff us!")
                it.combatTarget = player
                return@onNpcClick
            }
        }
        npc.transformIntoNPC(NPC_EARTH_ELEMENTAL_ROCK)
        npc.resetHP()
        npc.combatTarget = player
        npc.anim(ANIM_ROCK_WAKE_UP)
        npc.forceTalk("Grr... Ge'roff us!")
        player.lock(2)
    }

    val containers = arrayOf<Any>(1923, 1925, 1931, 1935, 2313, 1980)
    onItemOnObject(LAVA_TROUGHS, arrayOf(STONE_BOWL, STONE_BOWL_LAVA, *containers)) { (player, _, item) ->
        player.apply {
            when (item.id) {
                item.id -> {
                    sendMessage("You fill the bowl with hot lava.")
                    schedule {
                        lock()
                        anim(ANIM_FILL_STONE_BOWL)
                        wait(3)
                        soundEffect(SOUND_LAVA_BOWL_FILL, false)
                        inventory.replace(item, Item(STONE_BOWL_LAVA))
                        unlock()
                    }
                }
                item.id -> sendMessage("The bowl is already full of lava.")
                in containers -> simpleDialogue("That won't hold the lava for long. I should have a look for something more durable.")
            }
        }
    }

    onItemOnObject(arrayOf(FURNACE)) {
        it.player.apply {
            val furnaceVar = vars.getVarBit(VAR_FURNACE)
            val bellowsVar = vars.getVarBit(VAR_BELLOWS)
            val wheelVar = vars.getVarBit(VAR_WATERWHEEL_MACHINERY)
            when (it.item.id) {
                STONE_BOWL_LAVA -> {
                    sendMessage("You empty the lava into the furnace.")
                    soundEffect(SOUND_LAVA_BOWL_POUR, false)
                    inventory.replace(it.item, Item(STONE_BOWL))
                    schedule {
                        lock()
                        wait(3)
                        vars.saveVarBit(VAR_FURNACE, 1)
                        when (furnaceVar) {
                            0 -> sendMessage("The furnace bursts to life.")
                            else -> sendMessage("The lava makes little difference to the furnace's searing heat.")
                        }
                        unlock()
                    }
                }
                ELEMENTAL_ORE -> {
                    if (wheelVar == 0 || bellowsVar == 0) {
                        sendMessage("The furnace needs to be hotter to be of any use.")
                        return@onItemOnObject
                    }
                    if (inventory.getAmountOf(COAL) < 4) {
                        sendMessage("You need four heaps of coal to smelt elemental ore.")
                        return@onItemOnObject
                    }
                    if (skills.getLevel(Skills.SMITHING) < 20) {
                        simpleDialogue("You need a Smithing level of at least 20 to smelt elemental bars.")
                        return@onItemOnObject
                    }
                    sendMessage("You place the elemental ore and four lumps of coal into the furnace.")
                    inventory.removeItems(Item(COAL, 4), Item(ELEMENTAL_ORE, 1))
                    schedule {
                        lock()
                        anim(ANIM_SMELTING)
                        wait(3)
                        unlock()
                        sendMessage("You retrieve a bar of elemental metal.")
                        inventory.addItem(ELEMENTAL_BAR)
                        skills.addXp(Skills.SMITHING, 7.0)
                    }
                }
                else -> if (furnaceVar == 0) {
                    sendMessage("This furnace is cold and useless for anything.")
                }
            }
        }
    }

    onObjectClick(HATCH) {
        it.player.simpleDialogue("You're unable to open the locked hatch.")
    }

    onObjectClick(WATER_LEVER) {
        it.player.apply {
            sendMessage("You pull the lever.")
            soundEffect(SOUND_LEVER, false)
            it.obj.setIdTemporary(LEVER_PULLED, 1)
            val leftVar = vars.getVarBit(VAR_WATER_CONTROLS_LEFT)
            val rightVar = vars.getVarBit(VAR_WATER_CONTROLS_RIGHT)
            if (leftVar == 1 && rightVar == 1) {
                val wheelVar = vars.getVarBit(VAR_WATERWHEEL_MACHINERY)
                if (wheelVar == 1) {
                    sendMessage("You hear the sound of the water wheel coming to a standstill.")
                    soundEffect(SOUND_WATERWHEEL_STOP, false)
                } else {
                    sendMessage("You hear the sound of the water wheel starting up.")
                    soundEffect(SOUND_WATERWHEEL_START, false)
                }
                vars.saveVarBit(VAR_BELLOWS, 0)
                vars.saveVarBit(VAR_WATERWHEEL_MACHINERY, wheelVar.xor(1))
            } else {
                vars.saveVarBit(VAR_WATER_CONTROLS_LEFT, 0)
                vars.saveVarBit(VAR_WATER_CONTROLS_RIGHT, 0)
                sendMessage("You hear the sound of the flow gates resetting.")
                soundEffect(SOUND_WATERWHEEL_RESET, false)
            }
        }
    }

    onObjectClick(WATER_CONTROLS_LEFT, WATER_CONTROLS_RIGHT) { (player, obj) ->
        player.apply {
            val wheelVar = vars.getVarBit(VAR_WATERWHEEL_MACHINERY)
            if (wheelVar == 1) {
                sendMessage("Now that the water wheel is running, the valve seems locked off.")
                return@onObjectClick
            }
            val leftVar = vars.getVarBit(VAR_WATER_CONTROLS_LEFT)
            val rightVar = vars.getVarBit(VAR_WATER_CONTROLS_RIGHT)
            schedule {
                sendMessage("You turn the handle.")
                anim(ANIM_TURN_WATER_CONTROLS)
                soundEffect(SOUND_SQUEEKY_VALVE, false)
                wait(3)
                if (leftVar != 1 || rightVar != 1) when (obj.id) {
                    WATER_CONTROLS_LEFT -> vars.saveVarBit(VAR_WATER_CONTROLS_LEFT, leftVar.xor(1))
                    WATER_CONTROLS_RIGHT -> vars.saveVarBit(VAR_WATER_CONTROLS_RIGHT, rightVar.xor(1))
                }
            }
        }
    }

    onObjectClick(AIR_LEVER) {
        it.player.apply {
            sendMessage("You pull the lever.")
            soundEffect(SOUND_LEVER, false)
            it.obj.setIdTemporary(LEVER_PULLED, 1)
            val wheelVar = vars.getVarBit(VAR_WATERWHEEL_MACHINERY)
            val fixedBellowsVar = vars.getVarBit(VAR_FIXED_BELLOWS)
            if (wheelVar == 0 || fixedBellowsVar == 0) {
                sendMessage("Nothing happens; the lever resets itself.")
                return@onObjectClick
            }
            val bellowsVar = vars.getVarBit(VAR_BELLOWS)
            when (bellowsVar) {
                0 -> sendMessage("The bellows pump air down the pipe.")
                1 -> sendMessage("The bellows stop pumping air.")
            }
            vars.saveVarBit(VAR_BELLOWS, bellowsVar.xor(1))
        }
    }

    onObjectClick(BELLOWS) {
        it.player.apply {
            if (skills.getLevel(Skills.CRAFTING) < 20) {
                sendMessage("You need a Crafting level of 20 to fix this.")
                return@onObjectClick
            }
            if (vars.getVarBit(VAR_FIXED_BELLOWS) == 1) {
                sendMessage("The bellows already look fixed to you.")
                return@onObjectClick
            }
            if (!inventory.containsItems( THREAD, LEATHER) || !inventory.containsItem(NEEDLE)) {
                sendMessage("You need a piece of leather, some thread and a needle to fix this.")
                return@onObjectClick
            }
            inventory.removeItems(Item(LEATHER, 1), Item(THREAD, 1))
            sendMessage("You stitch the leather over the hole in the bellows.")
            vars.saveVarBit(VAR_FIXED_BELLOWS, 1)
            schedule {
                lock()
                anim(ANIM_FIX_BELLOWS)
                wait(3)
                unlock()
            }
        }
    }

    onItemOnObject(arrayOf(WORKBENCH), arrayOf(ELEMENTAL_BAR)) { (player, _, item) ->
        player.apply {
            if (!inventory.containsOneItem(HAMMER)) {
                simpleDialogue("You need a hammer to work the metal with.")
                return@onItemOnObject
            }
            if (!inventory.containsOneItem(SLASHED_BOOK)) {
                simpleDialogue("This workbench is too complicated. You need instructions to follow.")
                return@onItemOnObject
            }
            if (skills.getLevel(Skills.SMITHING) < 20) {
                simpleDialogue("You need a Smithing level of at least 20 to work elemental bars.")
                return@onItemOnObject
            }
            sendMessage("Following the instructions in the book, you make an elemental shield.")
            // Make X dialogue too complicated to add cleaner than this
            schedule {
                lock()
                anim(ANIM_MAKE_SHIELD)
                soundEffect(SOUND_SHIELD_MAKE, true)
                wait(3)
                unlock()
                inventory.replace(item, Item(ELEMENTAL_SHIELD, 1))
                skills.addXp(Skills.SMITHING, 20.0)
                if (getQuestStage(Quest.ELEMENTAL_WORKSHOP_I) != STAGE_COMPLETE) {
                    completeQuest(Quest.ELEMENTAL_WORKSHOP_I)
                }
            }
        }
    }

    onObjectClick(*RANDOM_CRATES) { (player, obj) ->
        player.apply {
            val attribs = questManager.getAttribs(Quest.ELEMENTAL_WORKSHOP_I)
            val objectIdName = obj.id.toString()
            val randomLoot = attribs.getI(objectIdName)
            if (inventory.containsOneItem(randomLoot)) {
                sendMessage("It's empty.")
                return@onObjectClick
            }
            when (randomLoot) {
                LEATHER, THREAD -> {
                    attribs.removeI(objectIdName)
                    sendMessage("You find some ${Item(randomLoot).name}.")
                }
                NEEDLE, STONE_BOWL -> {
                    sendMessage("You find a ${Item(randomLoot).name}.")
                }
                else -> {
                    sendMessage("It's empty.")
                    return@onObjectClick
                }
            }
            inventory.addItemDrop(randomLoot)
        }
    }

    instantiateNpc(NPC_ELEMENTAL_ROCK) { id, tile -> EarthElemental(id, tile) }

}

private class EarthElemental(id: Int, tile: Tile) : NPC(id, tile) {

    override fun transformIntoNPC(id: Int) {
        if (id == NPC_EARTH_ELEMENTAL_ROCK) {
            schedule {
                lock()
                wait(3)
                unlock()
                // cleanup task
                var waitTime = 20
                while (true) {
                    wait(1)
                    if (combatTarget == null) {
                        waitTime -= 1
                    } else {
                        waitTime = 20
                    }
                    if (waitTime <= 0) {
                        setRespawnTask(5)
                        break
                    }
                }
            }
        }
        super.transformIntoNPC(id)
    }

    override fun finish() {
        transformIntoNPC(NPC_ELEMENTAL_ROCK)
        super.finish()
    }
}

@QuestHandler(
    quest = Quest.ELEMENTAL_WORKSHOP_I,
    startText = "I can start this quest by reading a book found in Seers village.",
    itemsText = "1 thread, 4 coal.",
    combatText = "You will need to defeat a level 44 earth elemental.",
    rewardsText =
        "5,000 Crafting XP<br>" +
                "5,000 Smithing XP<br>" +
                "Access to the Elemental Workshop<br>" +
                "The Ability to make elemental shields<br>",
    completedStage = STAGE_COMPLETE
)
class ElementalWorkshopI : QuestOutline() {
    override fun getJournalLines(player: Player, stage: Int): List<String?> {
        val list = mutableListOf<String>()
        when (stage) {
            STAGE_UNSTARTED -> list.add("I can start this quest by reading a book found in Seers village.")
            STAGE_READ_BOOK -> {
                list.add("I have found a battered book in a house in Seers Village.")
                list.add("It tells of magic ore and a workshop created to fashion it.")
            }
            STAGE_SLASHED_BOOK -> {
                list.add("Cutting open the spine of the book with a blade,")
                list.add("I found a key hidden under the leather binding.")
            }
            STAGE_OPEN_ODD_WALL -> list.add("I have found a secret door in the Seers Village smithy.")
            STAGE_FOUND_WORKSHOP -> list.add("There is obviously lots to do here.")
            STAGE_COMPLETE -> {
                list.add("After fixing up the old workshop machinery, collecting ore and smelting it I was able to create an Elemental Shield.")
                list.add("QUEST COMPLETE!")
            }
            else -> listOf("Invalid quest stage. Report this to an administrator.")
        }
        if (stage <= STAGE_OPEN_ODD_WALL) {
            list.add("Where is this workshop and how do I get in?")
        }
        return list
    }

    override fun complete(player: Player) {
        sendQuestCompleteInterface(player, ELEMENTAL_SHIELD)
        player.skills.addXpQuest(Skills.CRAFTING, 5000.0)
        player.skills.addXpQuest(Skills.SMITHING, 5000.0)
    }

    override fun updateStage(player: Player, stage: Int) {
        if (stage == STAGE_FOUND_WORKSHOP) {
            // generate random crates, throw in stone bowl because I am evil
            val attribs = player.questManager.getAttribs(Quest.ELEMENTAL_WORKSHOP_I)
            val crates = RANDOM_CRATES.toMutableList()
            attribs.clear()
            for (item in arrayOf(LEATHER, NEEDLE, STONE_BOWL, THREAD)) {
                val crate = crates.random()
                crates.remove(crate)
                attribs.setI(crate.toString(), item)
            }
        }
    }

}