package com.rs.game.content.world.npcs

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rs.game.World
import com.rs.game.content.skills.fishing.FishingSpot
import com.rs.game.content.skills.mining.RockType
import com.rs.game.content.skills.woodcutting.TreeType
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.gameobject.GameObject
import com.rs.lib.Constants
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.lib.util.Utils
import com.rs.net.decoders.handlers.NPCHandler
import com.rs.net.decoders.handlers.ObjectHandler

data class SimulatedPlayerActivitySettings(
    val enabled: Boolean = true,
    val groupSize: Int = 4,
    val actionIntervalTicks: Int = 8,
    val baseXpPerAction: Double = 24.0,
    val saveIntervalTicks: Int = 100
)

private data class SavedBotProgress(
    val xp: MutableMap<Int, Double> = mutableMapOf(),
    var actions: Long = 0
)

private data class ActivityGroup(
    val name: String,
    val members: List<SimulatedPlayerBot>,
    var activity: BotActivity,
    val anchor: Tile,
    val ownerUsername: String = ""
)

private data class BotActivity(val skill: Int, val name: String, val animation: Int, val verb: String)

/** Gives headless players visible routines, persistent XP, and stable regional training groups. */
object SimulatedPlayerActivityManager {
    private const val PROGRESS_ATTR = "simulatedPlayerProgressV1"
    private const val PLAYER_JOINED_KEY = "singleRsJoinedActivityGroup"
    private const val PLAYER_GROUP_NAME_KEY = "singleRsOwnedActivityGroup"
    private const val PLAYER_GROUP_BOTS_KEY = "singleRsActivityGroupBots"
    private const val PLAYER_GROUP_SKILL_KEY = "singleRsActivityGroupSkill"
    private val gson = Gson()
    private val progressType = object : TypeToken<MutableMap<String, SavedBotProgress>>() {}.type
    private val progress = mutableMapOf<String, SavedBotProgress>()
    private val groups = mutableMapOf<String, ActivityGroup>()
    private val defaultGroups = mutableMapOf<String, ActivityGroup>()
    private val playerGroups = mutableMapOf<String, ActivityGroup>()
    private val memberIndex = mutableMapOf<String, Int>()
    private val soloActivities = mutableMapOf<String, BotActivity>()
    private val soloAnchors = mutableMapOf<String, Tile>()
    private val activeTaskDescriptions = mutableMapOf<String, String>()
    private var loaded = false
    private var dirty = false
    private var lastSaveTick = 0L

    private val activities = mapOf(
        "Attack" to BotActivity(Skills.ATTACK, "combat training", 422, "practises attack combinations"),
        "Strength" to BotActivity(Skills.STRENGTH, "strength training", 422, "trains strength"),
        "Defence" to BotActivity(Skills.DEFENSE, "defence drills", 424, "practises defensive stances"),
        "Constitution" to BotActivity(Skills.HITPOINTS, "combat conditioning", 422, "works on combat conditioning"),
        "Ranged" to BotActivity(Skills.RANGE, "ranged training", 426, "practises ranged attacks"),
        "Prayer" to BotActivity(Skills.PRAYER, "prayer training", 645, "offers a prayer"),
        "Magic" to BotActivity(Skills.MAGIC, "magic training", 711, "practises a spell"),
        "Cooking" to BotActivity(Skills.COOKING, "cooking", 896, "cooks supplies"),
        "Woodcutting" to BotActivity(Skills.WOODCUTTING, "woodcutting", 867, "chops wood"),
        "Fletching" to BotActivity(Skills.FLETCHING, "fletching", 1248, "fletches logs"),
        "Fishing" to BotActivity(Skills.FISHING, "fishing", 622, "catches fish"),
        "Firemaking" to BotActivity(Skills.FIREMAKING, "firemaking", 733, "lights a fire"),
        "Crafting" to BotActivity(Skills.CRAFTING, "crafting", 1249, "crafts supplies"),
        "Smithing" to BotActivity(Skills.SMITHING, "smithing", 898, "works metal"),
        "Mining" to BotActivity(Skills.MINING, "mining", 624, "mines ore"),
        "Herblore" to BotActivity(Skills.HERBLORE, "herblore", 363, "mixes a potion"),
        "Agility" to BotActivity(Skills.AGILITY, "agility training", 751, "runs an agility drill"),
        "Thieving" to BotActivity(Skills.THIEVING, "thieving practice", 881, "practises sleight of hand"),
        "Slayer" to BotActivity(Skills.SLAYER, "slayer training", 422, "trains for a slayer task"),
        "Farming" to BotActivity(Skills.FARMING, "farming", 2291, "tends a patch"),
        "Runecrafting" to BotActivity(Skills.RUNECRAFTING, "runecrafting", 791, "crafts runes"),
        "Hunter" to BotActivity(Skills.HUNTER, "hunter training", 5208, "checks a trap"),
        "Construction" to BotActivity(Skills.CONSTRUCTION, "construction", 3683, "builds furniture"),
        "Summoning" to BotActivity(Skills.SUMMONING, "summoning training", 8502, "practises summoning"),
        "Dungeoneering" to BotActivity(Skills.DUNGEONEERING, "Dungeoneering", 13624, "prepares for a floor")
    )

    private val fishingSpots = mapOf(
        1174 to FishingSpot.KARAMBWANJI,
        1176 to FishingSpot.KARAMBWAN,
        1177 to FishingSpot.KARAMBWAN,
        1178 to FishingSpot.KARAMBWAN,
        312 to FishingSpot.LOBSTER,
        1332 to FishingSpot.LOBSTER,
        5470 to FishingSpot.LOBSTER,
        7046 to FishingSpot.LOBSTER,
        313 to FishingSpot.BIG_NET,
        1333 to FishingSpot.BIG_NET,
        5471 to FishingSpot.BIG_NET,
        317 to FishingSpot.FLY_FISHING,
        315 to FishingSpot.FLY_FISHING,
        309 to FishingSpot.FLY_FISHING,
        326 to FishingSpot.SHRIMP,
        323 to FishingSpot.SHRIMP,
        7045 to FishingSpot.SHRIMP,
        324 to FishingSpot.LOBSTER,
        325 to FishingSpot.SHRIMP,
        327 to FishingSpot.SHRIMP,
        328 to FishingSpot.FLY_FISHING,
        329 to FishingSpot.FLY_FISHING,
        330 to FishingSpot.SHRIMP
    )

    fun restoreProgress(bot: SimulatedPlayerBot) {
        loadProgress()
        progress[bot.username.lowercase()]?.xp?.forEach { (skill, xp) ->
            if (skill in 0 until Skills.SIZE && xp > bot.skills.getXp(skill)) {
                bot.skills.setXp(skill, xp)
                bot.skills.set(skill, bot.skills.getLevelForXp(skill))
            }
        }
    }

    fun initialize(bots: List<SimulatedPlayerBot>) {
        groups.clear()
        defaultGroups.clear()
        playerGroups.clear()
        memberIndex.clear()
        soloActivities.clear()
        soloAnchors.clear()
        activeTaskDescriptions.clear()
        val size = SimulatedPlayerPopulationManager.activitySettings.groupSize.coerceIn(2, 8)
        val labels = listOf("Adventurers", "Training Crew", "Regulars", "Wayfarers", "Skill Team", "Raid Friends")
        bots.groupBy { it.definition.location.ifBlank { "Gielinor" } }.forEach { (region, regionalBots) ->
            val soloBots = regionalBots.filterIndexed { index, _ -> index % (size + 1) == 0 }.toMutableList()
            regionalBots.filterNot(soloBots::contains).chunked(size).forEachIndexed { groupIndex, members ->
                if (members.size < 2) {
                    soloBots += members
                    return@forEachIndexed
                }
                val leader = members.first()
                val activity = if (leader.definition.mode == SimulatedPlayerMode.PK)
                    BotActivity(Skills.ATTACK, "Wilderness patrol", 422, "patrols the Wilderness")
                else activities[leader.personality.favoriteSkill] ?: activities.getValue("Fishing")
                val group = ActivityGroup("$region ${labels[groupIndex % labels.size]}", members, activity, leader.tile)
                members.forEachIndexed { index, bot ->
                    groups[bot.username.lowercase()] = group
                    defaultGroups[bot.username.lowercase()] = group
                    memberIndex[bot.username.lowercase()] = index
                }
            }
            soloBots.forEach { bot ->
                val key = bot.username.lowercase()
                soloActivities[key] = if (bot.definition.mode == SimulatedPlayerMode.PK)
                    BotActivity(Skills.ATTACK, "solo Wilderness patrol", 422, "patrols the Wilderness alone")
                else activities[bot.personality.favoriteSkill] ?: activities.getValue("Fishing")
                soloAnchors[key] = bot.tile
            }
        }
        Logger.info(javaClass, "initialize", "Organized ${bots.size} simulated players into ${groups.values.distinct().size} activity groups and ${soloActivities.size} solo routines")
    }

    fun controlsMovement(bot: SimulatedPlayerBot): Boolean =
        SimulatedPlayerPopulationManager.activitySettings.enabled &&
            (groups.containsKey(bot.username.lowercase()) || soloActivities.containsKey(bot.username.lowercase()))

    fun process(bot: SimulatedPlayerBot, seed: Int) {
        val settings = SimulatedPlayerPopulationManager.activitySettings
        if (!settings.enabled || bot.inCombat()) return
        val key = bot.username.lowercase()
        if (bot.actionManager.action != null || bot.interactionManager.interaction != null) {
            saveRealProgress(bot)
            maybeSave(settings)
            return
        }
        val group = groups[key]
        if (group == null) {
            val activity = soloActivities[key] ?: return
            if (!performActivity(bot, activity, settings, seed) && bot.tickCounter % 12L == (seed % 12).toLong()) {
                val anchor = soloAnchors[key] ?: bot.tile
                val target = soloMovementTarget(anchor, bot.tickCounter, seed)
                bot.resetWalkSteps()
                bot.addWalkSteps(target.x, target.y, 18, true)
            }
            maybeSave(settings)
            return
        }
        val index = memberIndex[bot.username.lowercase()] ?: 0
        val cycle = phase(group, bot.tickCounter)
        val target = movementTarget(anchorFor(group), index, cycle, bot.tickCounter, seed)

        if (Utils.getDistance(bot.tile, target) > 2) {
            if (bot.tickCounter % 6L == (seed % 6).toLong()) {
                bot.resetWalkSteps()
                bot.addWalkSteps(target.x, target.y, 25, true)
            }
            maybeSave(settings)
            return
        }

        when (cycle) {
            0 -> if (bot.tickCounter % 24L == (seed % 24).toLong()) {
                effectiveMembers(group).firstOrNull()?.let(bot::faceEntityTile)
            }
            1 -> performActivity(bot, group.activity, settings, seed)
            else -> Unit
        }
        maybeSave(settings)
    }

    fun status(bot: SimulatedPlayerBot): String {
        val key = bot.username.lowercase()
        activeTaskDescriptions[key]?.let { return it }
        val group = groups[key] ?: return soloActivities[key]?.let { "looking for a nearby task (solo)" } ?: "wandering"
        return when (phase(group, bot.tickCounter)) {
            0 -> "travelling with ${group.name}"
            1 -> "looking for a nearby real task with ${group.name}"
            else -> "moving with ${group.name}"
        }
    }

    fun groupDescription(bot: SimulatedPlayerBot): String {
        val group = groups[bot.username.lowercase()] ?: return "None"
        val leader = if (group.ownerUsername.isNotBlank()) group.ownerUsername else group.members.first().displayName
        return "${group.name} (${effectiveMembers(group).size + if (group.ownerUsername.isNotBlank()) 1 else 0} members; leader $leader)"
    }

    fun restorePlayerState(player: Player) {
        if (player.isHeadless) return
        if (playerGroups.containsKey(player.username.lowercase())) return
        val ownedName = savedString(player, PLAYER_GROUP_NAME_KEY)
        if (ownedName.isBlank()) return
        val botNames = savedString(player, PLAYER_GROUP_BOTS_KEY).split('|').filter(String::isNotBlank)
        val bots = botNames.mapNotNull(SimulatedPlayerPopulationManager::findByDisplayName)
            .filter { bot -> groups[bot.username.lowercase()]?.ownerUsername.isNullOrBlank() }
            .take(7)
        val skillName = savedString(player, PLAYER_GROUP_SKILL_KEY)
        val activity = activityFor(skillName) ?: bots.firstOrNull()?.let { activityFor(it.personality.favoriteSkill) }
            ?: activities.getValue("Fishing")
        installPlayerGroup(player, ownedName, bots, activity)
    }

    @JvmStatic
    fun handleGroupAction(player: Player, bot: SimulatedPlayerBot) {
        val ownGroup = playerGroups[player.username.lowercase()]
        if (ownGroup != null) {
            if (ownGroup.members.any { it.username.equals(bot.username, true) }) {
                removeFromPlayerGroup(player, bot)
            } else {
                inviteToPlayerGroup(player, bot)
            }
            return
        }
        val targetGroup = groups[bot.username.lowercase()]
        if (targetGroup == null) {
            player.sendMessage("${bot.displayName} is not currently in an activity group.")
            return
        }
        val joined = savedString(player, PLAYER_JOINED_KEY)
        if (joined.equals(targetGroup.name, true)) {
            leaveJoinedGroup(player)
            return
        }
        player.set(PLAYER_JOINED_KEY, targetGroup.name)
        player.sendMessage("You joined <col=00ffff>${targetGroup.name}</col>. Stay near the group while it trains to participate and gain normal XP.")
        player.sendMessage("Use ::botgroup status or ::botgroup leave at any time.")
    }

    fun createPlayerGroup(player: Player, requestedName: String) {
        if (playerGroups.containsKey(player.username.lowercase()) || savedString(player, PLAYER_GROUP_NAME_KEY).isNotBlank()) {
            player.sendMessage("You already own an activity group. Use ::botgroup disband first.")
            return
        }
        val clean = requestedName.replace(Regex("[^A-Za-z0-9 ]"), "").trim().replace(Regex("\\s+"), " ").take(24)
        val name = clean.ifBlank { "${player.displayName}'s Team" }
        if ((groups.values + playerGroups.values).any { it.name.equals(name, true) }) {
            player.sendMessage("An activity group named $name already exists. Choose another name.")
            return
        }
        player.set(PLAYER_JOINED_KEY, "")
        player.set(PLAYER_GROUP_NAME_KEY, name)
        player.set(PLAYER_GROUP_BOTS_KEY, "")
        player.set(PLAYER_GROUP_SKILL_KEY, "Fishing")
        installPlayerGroup(player, name, emptyList(), activities.getValue("Fishing"))
        player.sendMessage("You formed <col=00ffff>$name</col> with Fishing as its first activity.")
        player.sendMessage("Right-click a simulated player and choose Group options to invite them (up to 7 bots).")
        player.sendMessage("Use ::botgroup skill Mining to change the shared activity.")
    }

    fun setPlayerGroupSkill(player: Player, requestedSkill: String) {
        val group = playerGroups[player.username.lowercase()] ?: run {
            player.sendMessage("You must form your own group first with ::botgroup create [name].")
            return
        }
        val activity = activityFor(requestedSkill)
        if (activity == null) {
            player.sendMessage("Choose a real resource task: Fishing, Woodcutting, or Mining.")
            return
        }
        group.activity = activity
        player.set(PLAYER_GROUP_SKILL_KEY, Constants.SKILL_NAME[activity.skill])
        player.sendMessage("${group.name} will now train <col=00ffff>${activity.name}</col> together.")
    }

    fun leaveJoinedGroup(player: Player) {
        val joined = savedString(player, PLAYER_JOINED_KEY)
        if (joined.isBlank()) {
            player.sendMessage("You have not joined a bot activity group.")
            return
        }
        player.set(PLAYER_JOINED_KEY, "")
        player.sendMessage("You left <col=00ffff>$joined</col>.")
    }

    fun disbandPlayerGroup(player: Player) {
        val group = playerGroups.remove(player.username.lowercase()) ?: run {
            player.sendMessage("You do not own an activity group.")
            return
        }
        group.members.forEach { bot ->
            val key = bot.username.lowercase()
            val default = defaultGroups[key]
            if (default != null) {
                groups[key] = default
                memberIndex[key] = default.members.indexOf(bot).coerceAtLeast(0)
            } else {
                groups.remove(key)
                memberIndex.remove(key)
            }
        }
        player.set(PLAYER_GROUP_NAME_KEY, "")
        player.set(PLAYER_GROUP_BOTS_KEY, "")
        player.set(PLAYER_GROUP_SKILL_KEY, "")
        player.sendMessage("You disbanded <col=00ffff>${group.name}</col>. Its bot members returned to their regional groups.")
    }

    fun sendPlayerGroupStatus(player: Player) {
        val owned = playerGroups[player.username.lowercase()]
        if (owned != null) {
            val bots = owned.members.joinToString { it.displayName }.ifBlank { "No bots invited yet" }
            player.sendMessage("<col=00ffff>${owned.name}</col>: you lead ${owned.members.size} bot member${if (owned.members.size == 1) "" else "s"}.")
            player.sendMessage("Activity: ${owned.activity.name}. Members: $bots")
            return
        }
        val joined = savedString(player, PLAYER_JOINED_KEY)
        if (joined.isBlank()) player.sendMessage("You are not in an activity group. Join through Group options or use ::botgroup create [name].")
        else player.sendMessage("You are a member of <col=00ffff>$joined</col>. Stay near its bots during training to gain XP.")
    }

    fun sendNearbyGroups(player: Player) {
        restorePlayerState(player)
        val nearby = allGroups()
            .map { group -> group to effectiveMembers(group).minOf { Utils.getDistance(player.tile, it.tile) } }
            .filter { (_, distance) -> distance <= 32 }
            .sortedBy { (_, distance) -> distance }
            .take(8)
        if (nearby.isEmpty()) {
            player.sendMessage("There are no simulated-player groups within 32 tiles.")
            return
        }
        player.sendMessage("<col=00ffff>Nearby simulated-player groups</col>")
        nearby.forEach { (group, distance) ->
            val phase = when (phase(group, effectiveMembers(group).first().tickCounter)) {
                0 -> "travelling"
                1 -> "using nearby resources"
                else -> "moving"
            }
            val memberCount = effectiveMembers(group).size + if (group.ownerUsername.isNotBlank()) 1 else 0
            player.sendMessage("${group.name}: $memberCount members, $phase ($distance tiles away)")
        }
    }

    private fun performActivity(bot: SimulatedPlayerBot, activity: BotActivity, settings: SimulatedPlayerActivitySettings, seed: Int): Boolean {
        if (bot.definition.mode == SimulatedPlayerMode.PK) return false
        val interval = settings.actionIntervalTicks.coerceIn(4, 30)
        if (bot.tickCounter % interval.toLong() != Math.floorMod(seed, interval).toLong()) return false
        if (bot.inventory.freeSlots < 4) bot.inventory.reset()

        val preferred = when (activity.skill) {
            Skills.WOODCUTTING -> 0
            Skills.MINING -> 1
            Skills.FISHING -> 2
            else -> Math.floorMod(seed, 3)
        }
        val started = when (preferred) {
            0 -> tryWoodcutting(bot) || tryMining(bot) || tryFishing(bot)
            1 -> tryMining(bot) || tryFishing(bot) || tryWoodcutting(bot)
            else -> tryFishing(bot) || tryWoodcutting(bot) || tryMining(bot)
        }
        if (!started) activeTaskDescriptions.remove(bot.username.lowercase())
        return started
    }

    private fun tryWoodcutting(bot: SimulatedPlayerBot): Boolean {
        val tree = closestObject(bot) { obj ->
            val type = TreeType.forObject(bot, obj)
            type != null && type.level <= bot.skills.getLevelForXp(Skills.WOODCUTTING)
        } ?: return false
        if (!bot.inventory.containsItem(1351)) bot.inventory.addItem(1351)
        activeTaskDescriptions[bot.username.lowercase()] = "woodcutting at ${tree.definitions.name}"
        ObjectHandler.handleOption1(bot, tree)
        return true
    }

    private fun tryMining(bot: SimulatedPlayerBot): Boolean {
        val rock = closestObject(bot) { obj ->
            val type = rockType(obj.definitions.name)
            type != null && type.level <= bot.skills.getLevelForXp(Skills.MINING)
        } ?: return false
        if (!bot.inventory.containsItem(1265)) bot.inventory.addItem(1265)
        activeTaskDescriptions[bot.username.lowercase()] = "mining at ${rock.definitions.name}"
        ObjectHandler.handleOption1(bot, rock)
        return true
    }

    private fun tryFishing(bot: SimulatedPlayerBot): Boolean {
        val candidate = World.getNPCsInChunkRange(bot.chunkId, 2).asSequence()
            .filter { it.plane == bot.plane && Utils.getDistance(bot.tile, it.tile) <= 15 }
            .mapNotNull { npc -> fishingSpots[npc.id]?.let { npc to it } }
            .filter { (_, spot) -> spot.level <= bot.skills.getLevelForXp(Skills.FISHING) }
            .minByOrNull { (npc, _) -> Utils.getDistance(bot.tile, npc.tile) }
            ?: return false
        val (npc, spot) = candidate
        spot.tool.firstOrNull()?.let { if (!bot.inventory.containsItem(it)) bot.inventory.addItem(it) }
        spot.bait?.firstOrNull()?.let { if (!bot.inventory.containsItem(it)) bot.inventory.addItem(it, 500) }
        activeTaskDescriptions[bot.username.lowercase()] = "fishing at a real fishing spot"
        NPCHandler.handleOption1(bot, npc)
        return true
    }

    private fun closestObject(bot: SimulatedPlayerBot, predicate: (GameObject) -> Boolean): GameObject? =
        World.getAllObjectsInChunkRange(bot.chunkId, 2).asSequence()
            .filter { it.plane == bot.plane && Utils.getDistance(bot.tile, it.tile) <= 15 }
            .filter(predicate)
            .minByOrNull { Utils.getDistance(bot.tile, it.tile) }

    private fun rockType(name: String): RockType? = when (name) {
        "Clay rocks", "Clay vein", "Clay rock" -> RockType.CLAY
        "Copper ore rocks", "Copper ore vein", "Copper rock" -> RockType.COPPER
        "Tin ore rocks", "Tin ore vein", "Tin rock" -> RockType.TIN
        "Iron ore rocks", "Iron ore vein" -> RockType.IRON
        "Silver ore rocks", "Silver ore vein" -> RockType.SILVER
        "Gold ore rocks", "Gold ore vein" -> RockType.GOLD
        "Coal rocks", "Coal vein" -> RockType.COAL
        "Mithril ore rocks", "Mithril ore vein" -> RockType.MITHRIL
        "Adamantite ore rocks", "Adamantite ore vein" -> RockType.ADAMANT
        "Runite ore rocks" -> RockType.RUNE
        "Granite rocks" -> RockType.GRANITE
        "Sandstone rocks" -> RockType.SANDSTONE
        "Gem rocks" -> RockType.GEM
        else -> null
    }

    private fun saveRealProgress(bot: SimulatedPlayerBot) {
        if (bot.tickCounter % 20L != 0L) return
        val state = progress.getOrPut(bot.username.lowercase()) { SavedBotProgress() }
        var changed = false
        for (skill in 0 until Skills.SIZE) {
            val xp = bot.skills.getXp(skill)
            if (xp > (state.xp[skill] ?: 0.0)) {
                state.xp[skill] = xp
                changed = true
            }
        }
        if (changed) {
            state.actions++
            dirty = true
        }
    }

    private fun installPlayerGroup(player: Player, name: String, bots: List<SimulatedPlayerBot>, activity: BotActivity) {
        val group = ActivityGroup(name, bots, activity, player.tile, player.username)
        playerGroups[player.username.lowercase()] = group
        bots.forEachIndexed { index, bot ->
            groups[bot.username.lowercase()] = group
            memberIndex[bot.username.lowercase()] = index
        }
    }

    private fun inviteToPlayerGroup(player: Player, bot: SimulatedPlayerBot) {
        val current = playerGroups[player.username.lowercase()] ?: return
        val occupied = groups[bot.username.lowercase()]
        if (occupied?.ownerUsername?.isNotBlank() == true && !occupied.ownerUsername.equals(player.username, true)) {
            player.sendMessage("${bot.displayName} already belongs to ${occupied.name}.")
            return
        }
        if (current.members.size >= 7) {
            player.sendMessage("Your activity group is full: you plus 7 simulated players.")
            return
        }
        val bots = current.members + bot
        installPlayerGroup(player, current.name, bots, current.activity)
        savePlayerBots(player, bots)
        player.sendMessage("${bot.displayName} joined <col=00ffff>${current.name}</col> and will follow your shared routine.")
    }

    private fun removeFromPlayerGroup(player: Player, bot: SimulatedPlayerBot) {
        val current = playerGroups[player.username.lowercase()] ?: return
        val bots = current.members.filterNot { it.username.equals(bot.username, true) }
        val key = bot.username.lowercase()
        val default = defaultGroups[key]
        if (default != null) {
            groups[key] = default
            memberIndex[key] = default.members.indexOf(bot).coerceAtLeast(0)
        } else {
            groups.remove(key)
            memberIndex.remove(key)
        }
        installPlayerGroup(player, current.name, bots, current.activity)
        savePlayerBots(player, bots)
        player.sendMessage("${bot.displayName} left your activity group and returned to its original routine.")
    }

    private fun savePlayerBots(player: Player, bots: List<SimulatedPlayerBot>) {
        player.set(PLAYER_GROUP_BOTS_KEY, bots.joinToString("|") { it.username })
    }

    private fun activityFor(raw: String): BotActivity? {
        val requested = raw.trim()
        return activities.entries.firstOrNull {
            it.value.skill in setOf(Skills.FISHING, Skills.WOODCUTTING, Skills.MINING) &&
                (it.key.equals(requested, true) || it.value.name.equals(requested, true))
        }?.value
    }

    private fun savedString(player: Player, key: String): String = player.getO<String>(key).orEmpty()

    private fun effectiveMembers(group: ActivityGroup): List<SimulatedPlayerBot> =
        group.members.filter { groups[it.username.lowercase()] === group && !it.hasFinished() }

    private fun allGroups(): List<ActivityGroup> =
        (groups.values + playerGroups.values).distinct().filter { effectiveMembers(it).isNotEmpty() }

    private fun anchorFor(group: ActivityGroup): Tile {
        if (group.ownerUsername.isBlank()) return group.anchor
        return World.players.firstOrNull {
            !it.isHeadless && !it.hasFinished() && it.username.equals(group.ownerUsername, true)
        }?.tile ?: group.anchor
    }

    private fun phase(group: ActivityGroup, tick: Long): Int {
        val offset = Math.floorMod(group.name.hashCode(), 360)
        return Math.floorMod(((tick + offset) / 120L).toInt(), 3)
    }

    private fun formationTile(anchor: Tile, index: Int, bankOffset: Int): Tile {
        val offsets = arrayOf(0 to 0, 2 to 0, 0 to 2, 2 to 2, -2 to 0, 0 to -2, -2 to -2, 2 to -2)
        val (x, y) = offsets[index % offsets.size]
        return Tile.of(anchor.x + x + bankOffset, anchor.y + y, anchor.plane)
    }

    private fun movementTarget(anchor: Tile, index: Int, cycle: Int, tick: Long, seed: Int): Tile {
        // Activity-group membership used to suppress normal wandering and leave every bot
        // standing on its formation tile. Move each member around a small, deterministic
        // circuit so every phase remains visibly active without scattering the group.
        val formation = formationTile(anchor, index, if (cycle == 2) 4 else 0)
        val radius = if (cycle == 1) 4 else 3
        val offsets = arrayOf(
            radius to 0,
            radius to radius,
            0 to radius,
            -radius to radius,
            -radius to 0,
            -radius to -radius,
            0 to -radius,
            radius to -radius
        )
        val step = Math.floorMod((tick / 18L).toInt() + index * 2 + seed, offsets.size)
        val (x, y) = offsets[step]
        return Tile.of(formation.x + x, formation.y + y, formation.plane)
    }

    private fun soloMovementTarget(anchor: Tile, tick: Long, seed: Int): Tile {
        val offsets = arrayOf(5 to 0, 4 to 4, 0 to 5, -4 to 4, -5 to 0, -4 to -4, 0 to -5, 4 to -4)
        val step = Math.floorMod((tick / 18L).toInt() + seed, offsets.size)
        val (x, y) = offsets[step]
        return Tile.of(anchor.x + x, anchor.y + y, anchor.plane)
    }

    private fun loadProgress() {
        if (loaded) return
        loaded = true
        try {
            val json: String? = World.data.attribs.getO(PROGRESS_ATTR)
            if (!json.isNullOrBlank()) progress.putAll(gson.fromJson(json, progressType))
        } catch (error: Throwable) {
            Logger.handle(javaClass, "loadProgress", "Unable to restore simulated-player progress", error)
        }
    }

    private fun maybeSave(settings: SimulatedPlayerActivitySettings) {
        if (!dirty) return
        val now = World.getServerTicks()
        if (now - lastSaveTick < settings.saveIntervalTicks.coerceIn(25, 1000)) return
        flush()
        lastSaveTick = now
    }

    fun flush() {
        if (!loaded || !dirty) return
        World.data.attribs.setO<String>(PROGRESS_ATTR, gson.toJson(progress, progressType))
        dirty = false
    }

    fun onBotFinished() {
        if (World.ticksTillUpdate >= 0) flush()
    }
}
