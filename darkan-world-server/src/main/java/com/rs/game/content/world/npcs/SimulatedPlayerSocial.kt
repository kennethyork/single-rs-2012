package com.rs.game.content.world.npcs

import com.rs.Settings
import com.rs.game.World
import com.rs.game.tasks.WorldTasks
import com.rs.game.content.clans.ClansManager
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.PublicChatMessage
import com.rs.lib.model.DisplayNamePair
import com.rs.lib.model.Friend
import com.rs.lib.model.MinimalSocial
import com.rs.lib.model.clan.Clan
import com.rs.lib.model.clan.ClanRank
import com.rs.lib.net.packets.encoders.social.ClanChannelFull
import com.rs.lib.net.packets.encoders.social.ClanSettingsFull
import com.rs.lib.net.packets.encoders.social.FriendStatus
import com.rs.lib.util.Utils
import java.util.concurrent.ConcurrentHashMap

/** Local social routing used when Single RS 2012 runs without a lobby server. */
object SimulatedPlayerSocial {
    private const val RESPONSE_DISTANCE = 14
    private const val RECRUITED_BOTS_KEY = "singleRsRecruitedClanBots"
    private val responseLocks = ConcurrentHashMap<String, Long>()

    @JvmStatic
    fun initializeClans() {
        SimulatedPlayerPopulationManager.activeBots()
            .mapNotNull { bot -> clanName(bot)?.let { it to bot } }
            .groupBy({ it.first }, { it.second })
            .forEach { (name, bots) ->
                if (bots.isEmpty()) return@forEach
                val clan = ClansManager.getCachedClan(name) ?: Clan(name, bots.first().account).also {
                    bots.drop(1).forEach { bot -> it.addMember(bot.account, ClanRank.RECRUIT) }
                    it.setMotto("Adventuring across Gielinor")
                    ClansManager.registerLocalClan(it)
                }
                bots.forEach { bot ->
                    bot.social.setClanName(name)
                    bot.social.setConnectedToClan(true)
                    if (!clan.members.containsKey(bot.username)) clan.addMember(bot.account, ClanRank.RECRUIT)
                }
            }
    }

    @JvmStatic
    fun restoreSocialState(player: Player) {
        if (!Settings.getConfig().isSinglePlayer || player.isHeadless) return
        SimulatedPlayerActivityManager.restorePlayerState(player)
        val friends = SimulatedPlayerPopulationManager.activeBots()
            .filter { bot -> player.social.friends.any { it.equals(bot.username, true) } }
            .map { Friend(it.account, Settings.getConfig().worldInfo, false) }
        if (friends.isNotEmpty()) player.session.write(FriendStatus(player.account, *friends.toTypedArray()))
        val savedClanName = player.social.clanName
        val clan = savedClanName?.let { name ->
            ClansManager.getCachedClan(name) ?: Clan(name, player.account).also {
                it.setMotto("Adventuring across Gielinor")
                ClansManager.registerLocalClan(it)
            }
        }
        clan?.let {
            recruitedBotNames(player).forEach { botName ->
                SimulatedPlayerPopulationManager.findByDisplayName(botName)?.let { bot ->
                    bot.social.setClanName(it.name)
                    bot.social.setConnectedToClan(true)
                    if (!it.members.containsKey(bot.username)) it.addMember(bot.account, ClanRank.RECRUIT)
                }
            }
            if (!it.members.containsKey(player.username)) it.addMember(player.account, ClanRank.RECRUIT)
            player.social.setConnectedToClan(true)
            sendClanState(player, it)
        }
    }

    @JvmStatic
    fun onPublicMessage(player: Player, message: String) {
        if (!Settings.getConfig().isSinglePlayer || player.isHeadless) return
        val bot = SimulatedPlayerPopulationManager.activeBots()
            .asSequence()
            .filter { !it.isDead && it.withinDistance(player, RESPONSE_DISTANCE) }
            .minByOrNull { Utils.getDistance(it.tile, player.tile) }
            ?: return
        val ollama = SimulatedPlayerPopulationManager.ollamaSettings
        if (!acquireLock("public:${player.username}", ollama.publicCooldownSeconds.coerceIn(1, 120) * 1_000L)) return

        WorldTasks.delay(responseDelay(message)) {
            if (player.hasFinished() || bot.hasFinished() || !bot.withinDistance(player, RESPONSE_DISTANCE)) return@delay
            SimulatedPlayerOllama.reply(bot, player, "public", message) { answer ->
                if (!player.hasFinished() && !bot.hasFinished() && bot.withinDistance(player, RESPONSE_DISTANCE)) {
                    bot.faceEntityTile(player)
                    bot.sendPublicChatMessage(PublicChatMessage(answer, 0))
                }
            }
        }
    }

    @JvmStatic
    fun handlePrivateMessage(player: Player, recipient: String, message: String): Boolean {
        if (!Settings.getConfig().isSinglePlayer) return false
        val bot = SimulatedPlayerPopulationManager.findByDisplayName(recipient) ?: return false
        player.packets.sendPrivateMessage(bot.displayName, message)
        WorldTasks.delay(responseDelay(message)) {
            if (!player.hasFinished() && !bot.hasFinished()) {
                if (wantsClanInvite(message)) joinClan(player, bot)
                SimulatedPlayerOllama.reply(bot, player, "private", message) { answer ->
                    if (!player.hasFinished() && !bot.hasFinished())
                        player.packets.receivePrivateMessage(bot.account, answer)
                }
            }
        }
        return true
    }

    @JvmStatic
    fun onClanMessage(player: Player, message: String, guest: Boolean): Boolean {
        if (!Settings.getConfig().isSinglePlayer) return false
        val name = if (guest) player.social.guestedClanChat else player.social.clanName
        val clan = ClansManager.getCachedClan(name) ?: return false
        clanViewers(clan, guest).forEach { it.packets.receiveClanChatMessage(player.account, message, guest) }
        val clanBots = SimulatedPlayerPopulationManager.activeBots().filter { clanName(it) == clan.name }
        val bot = clanBots.takeIf { it.isNotEmpty() }?.let {
            it[Math.floorMod(message.hashCode() + (System.currentTimeMillis() / 30_000L).toInt(), it.size)]
        }
        val cooldown = SimulatedPlayerPopulationManager.ollamaSettings.clanCooldownSeconds.coerceIn(1, 120) * 1_000L
        if (bot != null && acquireLock("clan:${clan.name}", cooldown)) {
            WorldTasks.delay(3) {
                SimulatedPlayerOllama.reply(bot, player, "clan", message) { answer ->
                    clanViewers(clan, guest).forEach {
                        it.packets.receiveClanChatMessage(bot.account, answer, guest)
                    }
                }
            }
        }
        return true
    }

    @JvmStatic
    fun joinClan(player: Player, bot: SimulatedPlayerBot) {
        val name = clanName(bot)
        if (name == null) {
            player.sendMessage("${bot.displayName} is not currently in a clan.")
            return
        }
        val clan = ClansManager.getCachedClan(name) ?: return
        val existing = player.social.clanName
        if (existing != null && !existing.equals(name, true)) {
            player.sendMessage("You are already in the clan $existing.")
            return
        }
        if (!clan.members.containsKey(player.username)) clan.addMember(player.account, ClanRank.RECRUIT)
        player.social.setClanName(name)
        player.social.setConnectedToClan(true)
        sendClanState(player, clan)
        player.sendMessage("You have joined <col=00ffff>$name</col>. Use the Clan Chat tab to talk.")
    }

    @JvmStatic
    fun handleClanAction(player: Player, bot: SimulatedPlayerBot) {
        if (clanName(bot) != null) {
            joinClan(player, bot)
            return
        }
        val clan = ClansManager.getCachedClan(player.social.clanName)
        if (clan == null) {
            player.sendMessage("${bot.displayName} is clanless. Create your clan from the Clan Chat tab, then recruit them.")
            return
        }
        if (clan.getRank(player.username) != ClanRank.OWNER) {
            player.sendMessage("Only the owner can recruit ${bot.displayName} into this clan.")
            return
        }
        clan.addMember(bot.account, ClanRank.RECRUIT)
        bot.social.setClanName(clan.name)
        bot.social.setConnectedToClan(true)
        val recruits = recruitedBotNames(player).toMutableSet()
        recruits.add(bot.displayName)
        player.set(RECRUITED_BOTS_KEY, ArrayList(recruits))
        sendClanState(player, clan)
        player.sendMessage("${bot.displayName} has joined <col=00ffff>${clan.name}</col>.")
    }

    @JvmStatic
    fun createPlayerClan(player: Player, requestedName: String) {
        val name = requestedName.trim().replace(Regex("\\s+"), " ")
        if (player.social.clanName != null) {
            player.sendMessage("You are already in a clan.")
            return
        }
        if (name.length !in 1..12 || !name.all { it.isLetterOrDigit() || it == ' ' }) {
            player.sendMessage("Clan names must be 1-12 letters, numbers, or spaces.")
            return
        }
        if (ClansManager.getCachedClan(name) != null) {
            player.sendMessage("That clan name is already in use.")
            return
        }
        val clan = Clan(name, player.account)
        clan.setMotto("Adventuring across Gielinor")
        ClansManager.registerLocalClan(clan)
        player.social.setClanName(name)
        player.social.setConnectedToClan(true)
        player.set(RECRUITED_BOTS_KEY, ArrayList<String>())
        sendClanState(player, clan)
        player.sendMessage("You created <col=00ffff>$name</col>. Right-click a clanless player and choose Clan options to recruit them.")
    }

    private fun sendClanState(player: Player, clan: Clan) {
        val accounts = SimulatedPlayerPopulationManager.activeBots()
            .filter { clanName(it) == clan.name }
            .map { it.account } + player.account
        val names = accounts.associate { it.username to DisplayNamePair(it.displayName, it.prevDisplayName) }
        val settings = ClanSettingsFull.generateBlock(clan, names, 0)
        clan.updateBlock = settings
        player.session.write(ClanSettingsFull(settings, false))
        val members = accounts.map { MinimalSocial(it, Settings.getConfig().worldInfo) }
        player.session.write(ClanChannelFull(ClanChannelFull.generateBlock(clan, 1, members), false))
    }

    private fun clanViewers(clan: Clan, guest: Boolean): List<Player> = World.players.filter {
        !it.isHeadless && !it.hasFinished() && if (guest) {
            it.social.guestedClanChat?.equals(clan.name, true) == true
        } else {
            it.social.isConnectedToClan && it.social.clanName?.equals(clan.name, true) == true
        }
    }

    fun recruitedBotNames(player: Player): List<String> {
        val saved = player.getO<Any>(RECRUITED_BOTS_KEY) as? Collection<*> ?: return emptyList()
        return saved.mapNotNull { it as? String }
    }

    private fun clanName(bot: SimulatedPlayerBot): String? =
        bot.social.clanName?.trim()?.ifBlank { null } ?: bot.definition.clan.trim().ifBlank { null }

    private fun wantsClanInvite(message: String): Boolean {
        val normalized = message.lowercase()
        return listOf("join clan", "clan invite", "invite me", "can i join").any(normalized::contains)
    }
    private fun responseDelay(message: String): Int {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        val minimum = settings.minimumResponseDelayTicks.coerceIn(0, 50)
        val maximum = settings.maximumResponseDelayTicks.coerceIn(minimum, 100)
        return minimum + Math.floorMod(message.hashCode(), maximum - minimum + 1)
    }
    private fun acquireLock(key: String, duration: Long): Boolean {
        val now = System.currentTimeMillis()
        val previous = responseLocks.put(key, now + duration)
        return previous == null || previous <= now
    }
}
