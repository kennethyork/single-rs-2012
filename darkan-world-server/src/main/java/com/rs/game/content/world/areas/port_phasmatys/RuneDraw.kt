package com.rs.game.content.world.areas.port_phasmatys

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onButtonClick
import kotlin.random.Random

class MutableInt(var value: Int)

class GameState(
    val runePool: MutableList<Rune>,
    val playerSlots: MutableMap<Int, Rune>,
    val opponentSlots: MutableMap<Int, Rune>,
    val playerScore: MutableInt,
    val opponentScore: MutableInt,
    var gameOver: Boolean = false,
    var npc: NPC? = null,
    var isWinner: Boolean = false
)

@ServerStartupEvent
fun runeDraw() {
    onButtonClick(9) { (player, _, componentId) ->
        val gameState = player.tempAttribs.getO<GameState>("RuneDrawGameState")
            ?: return@onButtonClick

        when (componentId) {
            5 -> gameState.npc?.let { handleDraw(player, it, gameState) }
            6 -> gameState.npc?.let { handleHold(player, it, gameState) }
        }
    }
}

fun handleDraw(player: Player, npc: NPC, gameState: GameState) {
    if (gameState.gameOver) return
    player.packets.setIFHidden(9, 5, true)
    player.packets.setIFHidden(9, 6, true)
    drawRuneForPlayer(player, npc, gameState) {
        if (!gameState.gameOver) {
            drawRuneForOpponent(player, npc, gameState) {
                if (!gameState.gameOver) {
                    player.packets.setIFHidden(9, 5, false)
                    player.packets.setIFHidden(9, 6, false)
                }
            }
        }
    }
}

fun handleHold(player: Player, npc: NPC, gameState: GameState) {
    if (gameState.gameOver) return
    player.packets.setIFHidden(9, 5, true)
    player.packets.setIFHidden(9, 6, true)
    opponentDrawsUntilWinOrDeath(player, npc, gameState)
}

fun startRuneDraw(player: Player, npc: NPC) {
    val gameState = GameState(
        runePool = Rune.entries.toMutableList(),
        playerSlots = mutableMapOf(),
        opponentSlots = mutableMapOf(),
        playerScore = MutableInt(0),
        opponentScore = MutableInt(0),
        npc = npc
    )
    player.tempAttribs.setO<GameState>("RuneDrawGameState", gameState)
    setupInterface(player)
}

fun setupInterface(player: Player) {
    player.packets.setIFText(9, 2, "Rune-Draw")
    player.packets.setIFText(9, 3, "Your Score")
    player.packets.setIFText(9, 4, "Opponent's Score")
    player.packets.setIFText(9, 5, "Draw")
    player.packets.setIFText(9, 6, "Hold")
    player.packets.setIFText(9, 29, "")
    player.packets.setIFText(9, 31, "")
    player.packets.setIFHidden(9, 30, true)
    player.packets.setIFHidden(9, 32, true)
    player.interfaceManager.sendInterface(9)
}



fun drawRuneForPlayer(player: Player, npc: NPC, gameState: GameState, onComplete: () -> Unit) {
    val playerSlots = listOf(7, 20, 23, 22, 26, 24, 27, 25, 21, 28)
    assignRuneToNextFreeSlot(player, playerSlots, gameState.runePool, gameState.playerSlots) { rune ->
        gameState.playerScore.value += rune.points
        player.packets.setIFText(9, 29, gameState.playerScore.value.toString())
        gameState.playerSlots.forEach { (slotId, slotRune) ->
            player.packets.setIFHidden(9, slotId, false)
            player.packets.setIFItem(9, slotId, slotRune.id, 1)
        }
        if (rune == Rune.DEATH_RUNE) {
            player.packets.setIFText(9, 29, "DEATH")
            player.packets.setIFHidden(9, 32, false)
            player.packets.setIFText(9, 32, "Opponent Wins!")
            gameState.isWinner = false
            endGame(player, npc, gameState)
        }
        onComplete()
    }
}

fun drawRuneForOpponent(player: Player, npc: NPC, gameState: GameState, onComplete: () -> Unit) {
    if (gameState.gameOver) return
    val opponentSlots = listOf(9, 17, 10, 15, 14, 16, 13, 12, 11, 18)
    player.packets.setIFHidden(9, 32, false)
    player.packets.setIFText(9, 32, "Thinking...")
    player.tasks.scheduleTimer(0, 0) { tick: Int? ->
        when (tick) {
            2 ->
                player.packets.setIFHidden(9, 32, true)
            3 -> {
                assignRuneToNextFreeSlot(player, opponentSlots, gameState.runePool, gameState.opponentSlots) { rune ->
                    gameState.opponentScore.value += rune.points
                    player.packets.setIFText(9, 31, gameState.opponentScore.value.toString())
                    gameState.opponentSlots.forEach { (slotId, slotRune) ->
                        player.packets.setIFHidden(9, slotId, false)
                        player.packets.setIFItem(9, slotId, slotRune.id, 1)
                    }
                    if (rune == Rune.DEATH_RUNE) {
                        gameState.gameOver = true
                        player.packets.setIFText(9, 31, "DEATH")
                        player.packets.setIFHidden(9, 30, false)
                        player.packets.setIFText(9, 30, "You Win!")
                        gameState.isWinner = true
                        endGame(player, npc, gameState)
                    }
                    if (!gameState.gameOver) {
                        player.packets.setIFHidden(9, 5, false)
                        player.packets.setIFHidden(9, 6, false)
                    }
                    onComplete()
                }
                return@scheduleTimer false
            }
        }
        true
    }
}

fun opponentDrawsUntilWinOrDeath(player: Player, npc: NPC, gameState: GameState) {
    if (gameState.gameOver) return
    if (gameState.opponentScore.value > gameState.playerScore.value) {
        gameState.gameOver = true
        player.packets.setIFHidden(9, 32, false)
        player.packets.setIFText(9, 32, "Opponent Wins!")
        endGame(player, npc, gameState)
        return
    }
    drawRuneForOpponent(player, npc, gameState) {
        if (gameState.gameOver) return@drawRuneForOpponent
        if (gameState.opponentScore.value <= gameState.playerScore.value && gameState.runePool.isNotEmpty()) {
            opponentDrawsUntilWinOrDeath(player, npc, gameState)
        } else if (gameState.opponentScore.value > gameState.playerScore.value) {
            gameState.gameOver = true
            player.packets.setIFHidden(9, 32, false)
            player.packets.setIFText(9, 32, "Opponent Wins!")
            gameState.isWinner = false
            endGame(player, npc, gameState)
        }
    }
}

fun assignRuneToNextFreeSlot(
    player: Player,
    slots: List<Int>,
    runePool: MutableList<Rune>,
    slotMap: MutableMap<Int, Rune>,
    onRuneAssigned: (Rune) -> Unit
) {
    for (slotId in slots) {
        if (!slotMap.containsKey(slotId)) {
            val rune = runePool.removeAt(Random.nextInt(runePool.size))
            slotMap[slotId] = rune
            onRuneAssigned(rune)
            break
        }
    }
}

fun endGame(player: Player, npc: NPC, gameState: GameState) {
    gameState.gameOver = true
    if (player.hasRights(Rights.ADMIN))
        gameState.isWinner = true
    if(npc.id == 1694 && player.questManager.getStage(Quest.GHOSTS_AHOY) == GhostsAhoy.STAGE_5_GATHER_ITEMS) {
        if (gameState.isWinner) {
            player.questManager.getAttribs(Quest.GHOSTS_AHOY).incI("RobinWins")
            player.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("RobinWinLastRound", false)
            player.sendMessage("You beat Robin at Runedraw, you should speak to him to collect your winnings.")
            return
        }
        else {
            if (player.questManager.getAttribs(Quest.GHOSTS_AHOY).getI("RobinWins") >= 1) {
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("RobinWinLastRound", true)
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).decI("RobinWins")
                player.sendMessage("Robin beat you at Runedraw. You should speak to him and pay back the debt.")
                return
            } else
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("RobinWinLastRound", true)
            player.sendMessage("Robin beat you at Runedraw and now owes you nothing.")
            return
        }
        return
    }
    if(gameState.isWinner) {
        player.sendMessage("You win and collect 25gp from ${npc.name}")
        player.inventory.addCoins(50)
    }
    else {
        player.sendMessage("You loose and ${npc.name} keeps your 25gp")
    }
    player.packets.setIFHidden(9, 5, true)
    player.packets.setIFHidden(9, 6, true)
}

enum class Rune(val id: Int, val points: Int) {
    AIR_RUNE(556,1),
    MIND_RUNE(558,2),
    WATER_RUNE(555,3),
    EARTH_RUNE(557,4),
    FIRE_RUNE(554,5),
    BODY_RUNE(559,6),
    COSMIC_RUNE(564,7),
    CHAOS_RUNE(562,8),
    NATURE_RUNE(561,9),
    DEATH_RUNE(560,0),
}

class RuneDrawInvite {

    companion object {
        fun npcInviteToPlay(player: Player, npc: NPC) {
            player.startConversation {
                player(HeadE.HAPPY_TALKING, "Do you fancy a game of Runedraw?")
                npc(npc, HeadE.HAPPY_TALKING, "Certainly, I bet you 25gp you can't beat me!")
                options {
                    op("Bet ${npc.name} 25gp.") {
                        exec {
                            if (!player.inventory.hasCoins(25)) {
                                player.startConversation {
                                    player(HeadE.SHAKING_HEAD, "Sorry, I don't have enough money.")
                                    npc(npc, HeadE.LAUGH, "Well, you shouldn't have gambled it all then!")
                                }
                                return@exec
                            }
                            player.inventory.removeCoins(25)
                            startRuneDraw(player, npc)
                        }
                    }
                    op("Nevermind.") {
                        player(HeadE.CALM_TALK, "Actually, I'd best save my money.")
                        npc(npc, HeadE.CALM_TALK, "Well, you know where to find me if you change your mind!")
                    }
                }
            }
        }
    }
}