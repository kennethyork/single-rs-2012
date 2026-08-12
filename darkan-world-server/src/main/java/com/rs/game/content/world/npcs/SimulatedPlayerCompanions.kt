package com.rs.game.content.world.npcs

import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.Hit
import com.rs.game.model.entity.interactions.PlayerCombatInteraction
import com.rs.game.content.minigames.pest.PestControlGameController
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Tile
import java.util.concurrent.ConcurrentHashMap

data class SimulatedPlayerCompanionSettings(
    val enabled: Boolean = true,
    val maxPerPlayer: Int = 4,
    val followDistance: Int = 10,
    val countForDungeoneeringDifficulty: Boolean = true
)

/**
 * NPC-backed party members. They intentionally remain outside the real Player
 * party list so no fake account can receive XP, drops, chat, or saved state.
 */
object SimulatedPlayerCompanionManager {
    private val companions = ConcurrentHashMap<String, MutableList<SimulatedPlayerCompanion>>()

    @JvmStatic
    fun recruit(player: Player, source: PKBotNPC) {
        val settings = SimulatedPlayerPopulationManager.companionSettings
        if (!settings.enabled) {
            player.sendMessage("Simulated-player companions are disabled.")
            return
        }
        if (source is SimulatedPlayerCompanion) {
            player.sendMessage("That bot is already adventuring with someone.")
            return
        }

        val party = companions.computeIfAbsent(player.username) { mutableListOf() }
        synchronized(party) {
            if (party.size >= settings.maxPerPlayer.coerceIn(1, 4)) {
                player.sendMessage("You already have the maximum number of bot companions.")
                return
            }
            val role = Attack.entries[party.size % Attack.entries.size]
            val definition = SimulatedPlayerDefinition(
                name = "${source.customName?.replace("<col=FFFFFF>", "") ?: "Adventurer"} (${role.displayName})",
                x = player.x,
                y = player.y,
                plane = player.plane,
                style = role,
                mode = SimulatedPlayerMode.SOCIAL,
                wander = false,
                dropsEquipment = false,
                combatLevel = player.skills.combatLevelWithSummoning.coerceIn(3, 138)
            )
            party += SimulatedPlayerCompanion(player, definition)
            player.sendMessage("${definition.name} joined you. Use Dismiss on a companion to remove it.")
        }
    }

    @JvmStatic
    fun dismiss(player: Player, bot: PKBotNPC) {
        if (bot !is SimulatedPlayerCompanion || bot.owner !== player) {
            player.sendMessage("That bot is not one of your companions.")
            return
        }
        remove(bot)
        player.sendMessage("${bot.customName?.replace("<col=FFFFFF>", "") ?: "Your companion"} left your party.")
    }

    internal fun remove(bot: SimulatedPlayerCompanion) {
        companions[bot.owner.username]?.let { party ->
            synchronized(party) {
                party.remove(bot)
                if (party.isEmpty()) companions.remove(bot.owner.username, party)
            }
        }
        bot.cancelRespawnTask()
        if (!bot.hasFinished()) bot.finish()
    }

    @JvmStatic
    fun count(player: Player): Int = companions[player.username]?.size ?: 0

    @JvmStatic
    fun ensureMinimum(player: Player, requested: Int, activity: String) {
        val settings = SimulatedPlayerPopulationManager.companionSettings
        if (!settings.enabled) return
        val targetSize = requested.coerceIn(1, settings.maxPerPlayer.coerceIn(1, 4))
        val party = companions.computeIfAbsent(player.username) { mutableListOf() }
        synchronized(party) {
            while (party.size < targetSize) {
                val role = Attack.entries[party.size % Attack.entries.size]
                val definition = SimulatedPlayerDefinition(
                    name = "$activity Bot ${party.size + 1} (${role.displayName})",
                    x = player.x,
                    y = player.y,
                    plane = player.plane,
                    style = role,
                    mode = SimulatedPlayerMode.SOCIAL,
                    wander = false,
                    dropsEquipment = false,
                    combatLevel = player.skills.combatLevelWithSummoning.coerceIn(40, 138)
                )
                party += SimulatedPlayerCompanion(player, definition)
            }
        }
        player.sendMessage("Your bot party is ready for $activity.")
    }

    @JvmStatic
    fun countForDungeoneering(players: Collection<Player>): Int {
        if (!SimulatedPlayerPopulationManager.companionSettings.countForDungeoneeringDifficulty) return 0
        return players.sumOf(::count)
    }

    @JvmStatic
    fun combatLevelsForDungeoneering(players: Collection<Player>): Int {
        if (!SimulatedPlayerPopulationManager.companionSettings.countForDungeoneeringDifficulty) return 0
        return players.sumOf { player ->
            companions[player.username]?.sumOf { it.combatLevel } ?: 0
        }
    }
}

class SimulatedPlayerCompanion(
    val owner: Player,
    definition: SimulatedPlayerDefinition
) : PKBotNPC(Tile.of(owner.tile), definition) {
    private val followDistance = SimulatedPlayerPopulationManager.companionSettings.followDistance.coerceIn(4, 30)

    init {
        setRandomWalk(false)
        setNoDistanceCheck(true)
        setForceMultiAttacked(true)
    }

    override fun processNPC() {
        if (!owner.hasStarted() || !owner.isRunning || owner.hasFinished()) {
            SimulatedPlayerCompanionManager.remove(this)
            return
        }
        super.processNPC()
        if (isDead || isLocked) return

        val pestControl = (owner.controllerManager.controller as? PestControlGameController)?.control
        val allowedDistance = if (pestControl != null) 80 else followDistance
        if (plane != owner.plane || !withinDistance(owner.tile, allowedDistance)) {
            removeCombatTarget()
            resetWalkSteps()
            tele(Tile.of(owner.tile))
        }

        val target = ownerCombatTarget() ?: pestControl?.getBotTarget(this)
        if (target != null && validTarget(target)) {
            if (combatTarget !== target) setCombatTarget(target)
        } else if (!isUnderCombat && !withinDistance(owner.tile, 2)) {
            calcFollow(owner, if (run) 2 else 1, true)
        }
    }

    private fun ownerCombatTarget(): NPC? {
        val interaction = owner.interactionManager.interaction
        val active = (interaction as? PlayerCombatInteraction)?.action?.target as? NPC
        if (active != null) return active
        return owner.attackedBy as? NPC
    }

    private fun validTarget(target: NPC): Boolean =
        target !== this && target !is SimulatedPlayerCompanion && !target.isDead &&
            !target.hasFinished() && target.plane == owner.plane &&
            ((owner.controllerManager.controller is PestControlGameController && target.withinDistance(owner.tile, 80)) ||
                target.withinDistance(owner.tile, 16))

    override fun canAggroPlayer(target: Player): Boolean = false

    override fun isCantInteract(): Boolean = true

    override fun drop(killer: Player?) = Unit

    /** Credit companion damage to the owner, like a combat familiar. */
    override fun handlePreHitOut(target: Entity, hit: Hit) {
        hit.source = owner
    }

    override fun sendDeath(killer: Entity?) {
        super.sendDeath(killer)
        owner.sendMessage("One of your bot companions was defeated and will rejoin you shortly.")
    }
}

private val Attack.displayName: String
    get() = when (this) {
        Attack.MELEE -> "Melee"
        Attack.RANGE -> "Ranged"
        Attack.ICE_BARRAGE -> "Mage"
    }
