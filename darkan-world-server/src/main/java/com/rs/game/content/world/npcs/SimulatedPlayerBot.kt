package com.rs.game.content.world.npcs

import com.rs.game.World
import com.rs.game.content.world.areas.wilderness.WildernessController
import com.rs.game.tasks.WorldTasks
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.interactions.PlayerCombatInteraction
import com.rs.game.model.entity.player.Equipment
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.entity.player.managers.InterfaceManager.ScreenMode
import com.rs.lib.Constants
import com.rs.lib.game.Item
import com.rs.lib.game.PublicChatMessage
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.model.Account
import com.rs.lib.io.InputStream
import com.rs.lib.net.Decoder
import com.rs.lib.net.Session
import com.rs.net.encoders.WorldEncoder
import com.rs.utils.shop.ShopsHandler
import com.rs.lib.util.Utils
import io.netty.channel.embedded.EmbeddedChannel
import kotlin.math.abs

/** Social packet encoders require every sender to have a crown/rights value. */
internal fun simulatedPlayerAccount(name: String) = Account(name).also { it.rights = Rights.PLAYER }

/** A real, server-controlled Player entity with no attached game client. */
class SimulatedPlayerBot(val definition: SimulatedPlayerDefinition) : Player(simulatedPlayerAccount(definition.name)) {
    val personality = personalityFor(definition)
    private val home = Tile.of(definition.x, definition.y, definition.plane)
    private val seed = definition.name.hashCode().let { if (it == Int.MIN_VALUE) 0 else abs(it) }
    private var nextChatTick = 12L + seed % 25

    init {
        setTile(home)
        val botSession = Session(EmbeddedChannel(), object : Decoder() {
            override fun decode(stream: InputStream): Int = 0
        })
        botSession.setEncoder(WorldEncoder(this, botSession))
        init(botSession, account, ScreenMode.FIXED.ordinal, 765, 503, null)
        configureSkills()
        SimulatedPlayerActivityManager.restoreProgress(this)
        configureAppearance()
        startHeadless()
        setCanPvp(definition.mode == SimulatedPlayerMode.PK && WildernessController.isAtWild(home))
    }

    override fun isHeadless(): Boolean = true

    override fun processEntity() {
        super.processEntity()
        if (isDead || isLocked) return

        if (definition.mode == SimulatedPlayerMode.PK && WildernessController.isAtWild(tile)) {
            if (!isCanPvp) setCanPvp(true)
            if (!inCombat() && tickCounter % 5 == 0L) {
                World.players
                    .filter { it !== this && !it.isHeadless && it.isCanPvp && !it.isDead && withinDistance(it.tile, 10) }
                    .minByOrNull { Utils.getDistance(tile, it.tile) }
                    ?.let { interactionManager.setInteraction(PlayerCombatInteraction(this, it)) }
            }
        } else if (isCanPvp) {
            setCanPvp(false)
        }

        SimulatedPlayerActivityManager.process(this, seed)

        if (tickCounter >= nextChatTick) {
            val spoke = SimulatedPlayerChat.trySpeak(this, seed)
            nextChatTick = tickCounter + if (spoke) 100L + seed % 80 else 30L + seed % 35
        }

        if (!SimulatedPlayerActivityManager.controlsMovement(this) && !inCombat() && definition.wander && tickCounter % (8L + seed % 9) == 0L) {
            val radius = 5
            val x = home.x + ((seed + tickCounter.toInt() * 3) % (radius * 2 + 1)) - radius
            val y = home.y + ((seed / 7 + tickCounter.toInt() * 5) % (radius * 2 + 1)) - radius
            resetWalkSteps()
            addWalkSteps(x, y, 12, true)
        }
    }

    fun showStatsTo(viewer: Player) {
        val equipmentNames = equipment.itemsCopy.filterNotNull().take(8).joinToString(", ") { it.definitions.name }
        val baseCombat = skills.combatLevel
        val combatWithSummoning = skills.combatLevelWithSummoning
        val combatDisplay = if (World.isPvpArea(this)) {
            val summoningDifference = combatWithSummoning - baseCombat
            if (summoningDifference > 0) "$baseCombat (+$summoningDifference with Summoning)" else "$baseCombat"
        } else {
            "$combatWithSummoning"
        }
        viewer.sendMessage("<col=00ffff>${displayName}'s player profile</col>")
        viewer.sendMessage("Combat: $combatDisplay  Total level: ${skills.totalLevel}")
        viewer.sendMessage("Clan: ${social.clanName ?: "None"}")
        viewer.sendMessage("Personality: ${personality.title}; ${personality.temperament}, ${personality.speechStyle}")
        viewer.sendMessage("Favorites: ${personality.favoriteSkill} and ${personality.favoriteActivity}")
        viewer.sendMessage("Current goal: ${personality.currentGoal}")
        viewer.sendMessage("Activity: ${SimulatedPlayerActivityManager.status(this)}")
        viewer.sendMessage("Group: ${SimulatedPlayerActivityManager.groupDescription(this)}")
        viewer.sendMessage("Attack ${skills.getLevelForXp(Constants.ATTACK)}, Strength ${skills.getLevelForXp(Constants.STRENGTH)}, Defence ${skills.getLevelForXp(Constants.DEFENSE)}, Constitution ${skills.getLevelForXp(Constants.HITPOINTS)}")
        viewer.sendMessage("Ranged ${skills.getLevelForXp(Constants.RANGE)}, Magic ${skills.getLevelForXp(Constants.MAGIC)}, Prayer ${skills.getLevelForXp(Constants.PRAYER)}, Summoning ${skills.getLevelForXp(Constants.SUMMONING)}")
        viewer.sendMessage("Equipment: ${equipmentNames.ifEmpty { "None" }}")
    }

    fun openTradeFor(viewer: Player) {
        if (definition.mode == SimulatedPlayerMode.PK)
            viewer.sendMessage("${displayName} is not interested in trading while in the Wilderness.")
        else
            ShopsHandler.openShop(viewer, "simulated_player_trade")
    }

    override fun finish() {
        if (hasFinished()) return
        SimulatedPlayerActivityManager.onBotFinished()
        setFinished(true)
        World.removePlayer(this)
        com.rs.game.map.ChunkManager.updateChunks(this)
        session.channel.close()
    }

    private fun configureSkills() {
        val base = (definition.combatLevel * 0.72).toInt().coerceIn(3, 99)
        for (skill in 0 until Constants.SKILL_NAME.size) {
            val level = when (skill) {
                Constants.ATTACK, Constants.STRENGTH, Constants.DEFENSE, Constants.HITPOINTS,
                Constants.RANGE, Constants.MAGIC, Constants.PRAYER, Constants.SUMMONING -> base
                else -> (1 + (seed + skill * 17) % base).coerceIn(1, 99)
            }
            skills.setXp(skill, Skills.getXPForLevel(level).toDouble())
            skills.set(skill, level)
        }
        hitpoints = maxHitpoints
    }

    private fun configureAppearance() {
        appearance.setMale(seed % 2 == 0)
        appearance.setHairStyle(if (seed % 2 == 0) 5 + seed % 15 else 45 + seed % 12)
        for (index in 0 until 5) appearance.setColor(index, (seed / (index + 1) + index * 19) % 220)

        val sets = when (definition.style) {
            Attack.MELEE -> arrayOf(
                intArrayOf(10828, 1725, 11724, 11726, 21371, 22358, 21787, 20072, 20771),
                intArrayOf(1163, 1725, 1127, 1079, 4587, 7462, 3105, 1201, 6570),
                intArrayOf(3751, 6585, 2503, 2497, 4151, 7462, 3105, -1,  fireCape(seed))
            )
            Attack.RANGE -> arrayOf(
                intArrayOf(20147, 25034, 20151, 20155, 20171, 22362, 21790, -1, 20771),
                intArrayOf(3749, 6585, 2503, 2497, 861, 7462, 2577, -1, 6570),
                intArrayOf( coif(seed),  amulet(seed), 1135, 1099, 11235, 1065, 2577, -1, 10499)
            )
            Attack.ICE_BARRAGE -> arrayOf(
                intArrayOf(20159, 18335, 20163, 20167, 15486, 22366, 24986, 13738, 20771),
                intArrayOf(6918, 6585, 6916, 6924, 1381, 6922, 6920, -1, 2412),
                intArrayOf(4099, 1725, 4101, 4103, 4675, 7462, 3105, -1, 2413)
            )
        }
        val set = sets[seed % sets.size]
        val slots = intArrayOf(Equipment.HEAD, Equipment.NECK, Equipment.CHEST, Equipment.LEGS, Equipment.WEAPON, Equipment.HANDS, Equipment.FEET, Equipment.SHIELD, Equipment.CAPE)
        slots.indices.forEach { index -> if (set[index] >= 0) equipment.setSlot(slots[index], Item(set[index])) }
        appearance.generateAppearanceData()
    }

    private fun fireCape(value: Int) = if (value % 3 == 0) 6570 else 1052 + value % 10
    private fun coif(value: Int) = if (value % 2 == 0) 1169 else 3749
    private fun amulet(value: Int) = if (value % 2 == 0) 1725 else 6585

}

/** Low-frequency public chat shared by the simulated-player population. */
private object SimulatedPlayerChat {
    private const val VIEW_DISTANCE = 14
    private const val BASE_GLOBAL_COOLDOWN_MS = 18_000L
    private var nextPopulationChatAt = 0L

    fun trySpeak(bot: SimulatedPlayerBot, seed: Int): Boolean {
        val now = System.currentTimeMillis()
        if (now < nextPopulationChatAt) return false

        val listener = World.players
            .asSequence()
            .filter { it !== bot && !it.isHeadless && !it.isDead && !it.hasFinished() && bot.withinDistance(it, VIEW_DISTANCE) }
            .minByOrNull { Utils.getDistance(bot.tile, it.tile) }
            ?: return false

        val activity = SimulatedPlayerActivityManager.status(bot)
        SimulatedPlayerOllama.reply(
            bot,
            listener,
            "ambient",
            "Start a spontaneous conversation while $activity. Say something natural based on your identity, surroundings, or goals."
        ) { line ->
            if (bot.hasFinished() || listener.hasFinished() || !bot.withinDistance(listener, VIEW_DISTANCE)) return@reply
            bot.faceEntityTile(listener)
            bot.sendPublicChatMessage(PublicChatMessage(line, 0))
            nearbyPartner(bot, listener)?.let { partner ->
                WorldTasks.delay(4) {
                    if (!bot.hasFinished() && !partner.hasFinished() && !listener.hasFinished() &&
                        partner.withinDistance(bot, VIEW_DISTANCE) && partner.withinDistance(listener, VIEW_DISTANCE)) {
                        SimulatedPlayerOllama.reply(
                            partner,
                            listener,
                            "ambient",
                            "${bot.displayName} just said: $line Reply naturally to them in your own voice."
                        ) { response ->
                            if (!partner.hasFinished() && !listener.hasFinished() && partner.withinDistance(listener, VIEW_DISTANCE)) {
                                partner.faceEntityTile(bot)
                                partner.sendPublicChatMessage(PublicChatMessage(response, 0))
                            }
                        }
                    }
                }
            }
        }
        nextPopulationChatAt = now + BASE_GLOBAL_COOLDOWN_MS + (seed % 10) * 1_000L
        return true
    }

    private fun nearbyPartner(speaker: SimulatedPlayerBot, listener: Player): SimulatedPlayerBot? =
        SimulatedPlayerPopulationManager.activeBots().firstOrNull {
            it !== speaker && !it.isDead && !it.hasFinished() &&
                it.withinDistance(speaker, VIEW_DISTANCE) && it.withinDistance(listener, VIEW_DISTANCE)
        }

    private fun partnerReply(bot: SimulatedPlayerBot, speaker: SimulatedPlayerBot, message: String): String =
        when (Math.floorMod(bot.username.hashCode() + message.hashCode(), 5)) {
            0 -> "Yeah, ${speaker.displayName} has a point."
            1 -> "What are you training next?"
            2 -> "I was just thinking that."
            3 -> "Anyone want to team up later?"
            else -> "Fair enough."
        }

    private fun linesFor(bot: SimulatedPlayerBot, listener: Player): List<String> {
        if (bot.definition.mode == SimulatedPlayerMode.PK) {
            return if (bot.inCombat()) bot.personality.ambient + listOf(
                "Sit!",
                "You're not getting away!",
                "Should've banked first.",
                "Good luck escaping this one.",
                "Protect item might help!"
            ) else bot.personality.ambient + listOf(
                "Watch your back out here.",
                "Anyone seen a white dot?",
                "The Wilderness is quiet today.",
                "Risking much, ${listener.displayName}?",
                "Stay out of multi."
            )
        }

        if (bot.inCombat()) return bot.personality.ambient + listOf(
            "Almost got it.",
            "Come on, hit!",
            "I should've brought more food.",
            "This is taking forever.",
            "There goes another potion."
        )

        return bot.personality.ambient + when (bot.definition.location.lowercase()) {
            "grand exchange" -> listOf(
                "Anyone selling sharks?",
                "Price check on dragon bones?",
                "The GE is busy today.",
                "Just waiting on an offer.",
                "I should've bought these yesterday."
            )
            "lumbridge" -> listOf(
                "Back to Lumbridge again.",
                "Where did I put my tinderbox?",
                "These goblins never learn.",
                "Nice weather for fishing.",
                "Welcome to Lumbridge, ${listener.displayName}."
            )
            "varrock" -> listOf(
                "Heading to the Grand Exchange?",
                "Varrock is packed today.",
                "I need to visit the west bank.",
                "Anyone training Mining?",
                "The guards here have it easy."
            )
            "catherby" -> listOf(
                "Fishing levels?",
                "Nothing beats Catherby fishing.",
                "I could use a bigger net.",
                "Banking another load.",
                "These lobsters are taking ages."
            )
            "seers village" -> listOf(
                "The maples are crowded again.",
                "Anyone doing the agility course?",
                "Just one more Woodcutting level.",
                "Banking these logs.",
                "Seers is always relaxing."
            )
            "daemonheim" -> listOf(
                "Anyone up for Dungeoneering?",
                "Need one more for a floor.",
                "What complexity are we doing?",
                "I forgot to bind my weapon.",
                "Large floor after this?"
            )
            "edgeville" -> listOf(
                "Anyone heading into the Wilderness?",
                "Bank your valuables first.",
                "Edgeville never changes.",
                "I need more food before I go.",
                "Careful past that ditch."
            )
            "karamja" -> listOf(
                "Forgot my coins for the boat again.",
                "The fishing spot moved.",
                "Karamja is too hot.",
                "Anyone brought a lobster pot?",
                "Time to bank this catch."
            )
            else -> listOf(
                "What are you training, ${listener.displayName}?",
                "Nearly got another level.",
                "I need to clear some bank space.",
                "Nice gear.",
                "Where should I train next?",
                "Anyone doing a farm run?",
                "I miss bonus XP weekend."
            )
        }
    }
}
