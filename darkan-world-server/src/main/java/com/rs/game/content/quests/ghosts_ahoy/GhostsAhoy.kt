package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.command.Commands
import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.content.items.Dye
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin
import com.rs.engine.variables.ahoy_questvar

private const val Ectophial = 4251
val HARICANTO_TILE = Tile.of(3803, 3530, 0)
internal val GHOSTS_AHOY_SAIL_DYES = arrayOf(
    Dye.RED,
    Dye.YELLOW,
    Dye.BLUE,
    Dye.ORANGE,
    Dye.PURPLE,
    Dye.GREEN
)
private val GHOSTS_AHOY_SAIL_DYE_NAMES = GHOSTS_AHOY_SAIL_DYES.map { it.name }.toSet()

private fun randomGhostsAhoySailDye(): Dye = GHOSTS_AHOY_SAIL_DYES[Utils.random(GHOSTS_AHOY_SAIL_DYES.size)]

private fun ensureGhostsAhoySailColors(player: Player) {
    val attribs = player.questManager.getAttribs(Quest.GHOSTS_AHOY)

    fun ensureTargetColor(key: String) {
        val color = attribs.getO<String>(key)
        if (color == null || color !in GHOSTS_AHOY_SAIL_DYE_NAMES)
            attribs.setO<String>(key, randomGhostsAhoySailDye().name)
    }

    ensureTargetColor("sailColour1")
    ensureTargetColor("sailColour2")
    ensureTargetColor("sailColour3")
}

@QuestHandler(
    quest = Quest.GHOSTS_AHOY,
    startText = "Speak to Velorina in Port Phasmatys.",
    itemsText = "Approximately 1000 coins, thread, silk, spade, oak longbow, nettle tea, bucket of milk, ghostspeak amulet, 3 colours of dye, ectotokens or charter ships to enter Port Phasmatys three times.",
    combatText = "You will need to defeat a level 42 giant lobster.",
    rewardsText = "2,400 Prayer XP<br>" +
            "Free passage into Port Phasmatys<br>" +
            "The Ectophial",
    completedStage = 9
)

class GhostsAhoy : QuestOutline() {

    companion object {
        const val STAGE_1_BEGIN_QUEST = 1
        const val STAGE_2_PLEAD_WITH_NECROVARUS = 2
        const val STAGE_3_NECROVARUS_REFUSES = 3
        const val STAGE_4_SEEK_OLD_WOMAN = 4
        const val STAGE_5_GATHER_ITEMS = 5
        const val STAGE_6_AMULET_ENCHANTED = 6
        const val STAGE_7_COMMAND_NECROVARUS = 7
        const val STAGE_8_TELL_VELORINA = 8
        const val STAGE_9_QUEST_COMPLETE = 9
    }

    override fun getJournalLines(player: Player, stage: Int): List<String> {
        val lines = ArrayList<String>()
        when (stage) {
            STAGE_1_BEGIN_QUEST -> {
                lines.add("To start this quest, I can speak to Velorina in Port Phasmatys.")
            }

            STAGE_2_PLEAD_WITH_NECROVARUS -> {
                lines.add("Velorina told me of the trouble the ghosts in Port Phasmatys face.")
                lines.add("She wants me to plead with Necrovarus in the temple to let them pass on.")
            }

            STAGE_3_NECROVARUS_REFUSES -> {
                lines.add("My pleas to Necrovarus were unsuccessful.")
                lines.add("Velorina mentioned an old woman who fled the city before the tragedy—maybe she has a plan.")
            }

            STAGE_4_SEEK_OLD_WOMAN -> {
                lines.add("I found the old woman, who says she can enchant my Amulet of Ghostspeak to command Necrovarus,")
                lines.add("but I need to bring her three items: the Book of Haricanto, the Robes of Necrovarus, and a translation manual.")
            }

            STAGE_5_GATHER_ITEMS -> {
                lines.add("I must gather:")
                lines.add("- The Book of Haricanto")
                lines.add("- The Robes of Necrovarus")
                lines.add("- A translation manual")
                lines.add("…and return to the old woman so she can perform the enchantment.")
            }

            STAGE_6_AMULET_ENCHANTED -> {
                lines.add("I’ve given the old woman the required items.")
                lines.add("She has now enchanted my Amulet of Ghostspeak!")
            }

            STAGE_7_COMMAND_NECROVARUS -> {
                lines.add("With my newly enchanted amulet, I commanded Necrovarus to lift his ban.")
                lines.add("The ghosts of Port Phasmatys are finally free to move on.")
            }

            STAGE_8_TELL_VELORINA -> {
                lines.add("I should return to Velorina and let her know that Necrovarus has lifted his ban.")
            }

            STAGE_9_QUEST_COMPLETE -> {
                lines.add("I told Velorina the good news!")
                lines.add("She rewarded me with an Ectophial, which teleports me to the temple.")
                lines.add("Quest complete!")
            }
            else -> lines.add("Invalid quest stage. Report this to an administrator.")
        }
        return lines
    }

    override fun complete(player: Player) {
        player.inventory.addItem(Ectophial)
        player.skills.addXp(Skills.PRAYER, 2400.0)
        player.vars.saveVar(217, 5)
        sendQuestCompleteInterface(player, Ectophial)
    }

    override fun updateStage(player: Player, stage: Int) {
        if (stage == STAGE_5_GATHER_ITEMS) {
            player.vars.saveVarBit(ahoy_questvar, 5)
            player.vars.syncVarsToClient()
            ensureGhostsAhoySailColors(player)
        }
        else
            return
    }
}

@ServerStartupEvent
fun mapGAVarbits(){
    onLogin { (player) ->
        if (player.getQuestStage(Quest.GHOSTS_AHOY) >= GhostsAhoy.STAGE_4_SEEK_OLD_WOMAN && !player.isQuestComplete(Quest.GHOSTS_AHOY))
             ensureGhostsAhoySailColors(player)
        if (player.isQuestComplete(Quest.GHOSTS_AHOY) || player.getQuestStage(Quest.GHOSTS_AHOY) >= GhostsAhoy.STAGE_5_GATHER_ITEMS) {
            player.vars.saveVarBit(ahoy_questvar, 5)
            player.vars.syncVarsToClient()
        }
    }
}

object SailColorManager {
    fun getSailColor(player: Player, key: String): String {
        return player.questManager.getAttribs(Quest.GHOSTS_AHOY).getO<String>(key) ?: "white"
    }

    fun setSailColor(player: Player, key: String, value: String) {
        player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>(key, value)
    }

    fun getAllSailColors(player: Player): Map<String, String> {
        return mapOf(
            "sailColour1" to getSailColor(player, "sailColour1"),
            "sailColour1Player" to getSailColor(player, "sailColour1Player"),
            "sailColour2" to getSailColor(player, "sailColour2"),
            "sailColour2Player" to getSailColor(player, "sailColour2Player"),
            "sailColour3" to getSailColor(player, "sailColour3"),
            "sailColour3Player" to getSailColor(player, "sailColour3Player")
        )
    }
}

@ServerStartupEvent
fun resetGhostsAhoy() {
    Commands.add(Rights.PLAYER, "resetghostsahoy", "Reset Ghosts Ahoy Quest.", ) { p, args ->
        if (p.isQuestComplete(Quest.GHOSTS_AHOY)) return@add
        p.questManager.resetQuest(Quest.GHOSTS_AHOY)
        p.questManager.setStage(Quest.GHOSTS_AHOY, 1)
    }
}