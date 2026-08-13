package com.rs.game.content.world.npcs

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.rs.db.local.LocalFileStore
import com.rs.game.model.entity.player.Player
import com.rs.game.tasks.WorldTasks
import com.rs.lib.util.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/** Optional, non-blocking Ollama conversation engine with scripted fallback. */
object SimulatedPlayerOllama {
    private const val HISTORY_FILE = "ollama-conversations.json"
    private const val MAX_SAVED_CONVERSATIONS = 500

    private data class Turn(val role: String = "", val content: String = "")
    private data class PendingReply(
        val bot: SimulatedPlayerBot,
        val player: Player,
        val channel: String,
        val input: String,
        val fallback: () -> String,
        val deliver: (String) -> Unit
    )

    private val gson = Gson()
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
    private val histories = ConcurrentHashMap<String, ArrayDeque<Turn>>()
    private val queues = ConcurrentHashMap<String, ConcurrentLinkedQueue<PendingReply>>()
    private val activeConversations = ConcurrentHashMap.newKeySet<String>()
    private val unavailableUntil = AtomicLong(0L)
    private val lastFailureLog = AtomicLong(0L)
    private val lastSuccessfulResponse = AtomicLong(0L)
    @Volatile private var configured = false

    fun configure() {
        if (configured) return
        configured = true
        if (!SimulatedPlayerPopulationManager.ollamaSettings.persistHistory) return
        try {
            val json = LocalFileStore.read(HISTORY_FILE) ?: return
            val type = object : TypeToken<Map<String, List<Turn>>>() {}.type
            val saved: Map<String, List<Turn>> = gson.fromJson(json, type) ?: return
            saved.entries.toList().takeLast(MAX_SAVED_CONVERSATIONS).forEach { (key, turns) ->
                histories[key] = ArrayDeque(turns.takeLast(historyLimit()))
            }
            Logger.info(javaClass, "configure", "Restored ${histories.size} Ollama bot conversations")
        } catch (error: Throwable) {
            Logger.handle(javaClass, "configure", "Unable to restore Ollama conversation history", error)
        }
    }

    fun reply(
        bot: SimulatedPlayerBot,
        player: Player,
        channel: String,
        message: String,
        fallback: () -> String,
        deliver: (String) -> Unit
    ) {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        if (!settings.enabled || System.currentTimeMillis() < unavailableUntil.get()) {
            deliver(fallback())
            return
        }
        val key = conversationKey(channel, player, bot)
        val queue = queues.computeIfAbsent(key) { ConcurrentLinkedQueue() }
        if (queue.size >= 10) {
            deliver(fallback())
            return
        }
        queue.add(PendingReply(bot, player, channel, message, fallback, deliver))
        startNext(key)
    }

    fun forget(player: Player): Int {
        val marker = ":${player.username.lowercase()}:"
        val keys = histories.keys.filter { it.contains(marker) }
        keys.forEach(histories::remove)
        persistHistories()
        return keys.size
    }

    fun statusLines(): List<String> {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        val now = System.currentTimeMillis()
        val state = when {
            !settings.enabled -> "disabled (scripted replies only)"
            now < unavailableUntil.get() -> "temporarily unavailable; retrying automatically"
            lastSuccessfulResponse.get() > 0L -> "connected; last response ${((now - lastSuccessfulResponse.get()) / 1_000L).coerceAtLeast(0)}s ago"
            else -> "enabled; waiting for the first response"
        }
        return listOf(
            "<col=00ffff>Ollama bot chat:</col> $state",
            "Model: ${settings.model}  Endpoint: ${settings.baseUrl}",
            "Memory: ${settings.historyMessages} messages, ${if (settings.persistHistory) "saved locally" else "session only"}; queued: ${queues.values.sumOf { it.size }}",
            "Public cooldown: ${settings.publicCooldownSeconds}s  Clan cooldown: ${settings.clanCooldownSeconds}s"
        )
    }

    fun sendStatus(player: Player) {
        statusLines().forEach(player::sendMessage)
        probe { result -> if (!player.hasFinished()) player.sendMessage(result) }
    }

    @JvmStatic
    fun sendStartupStatus(player: Player) {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        if (!settings.enabled) {
            player.sendMessage("Bot conversations are using scripted replies. Use ::ollamastatus for details.")
            return
        }
        player.sendMessage("Bot AI is enabled with ${settings.model}; scripted replies take over if Ollama is unavailable.")
        if (isLocalEndpoint(settings.baseUrl))
            player.sendMessage("<col=80ff80>Privacy:</col> Ollama conversations and memory stay on this computer. Use ::ollamaforget to erase saved chat memory.")
        else
            player.sendMessage("<col=ffcc00>Privacy:</col> Bot messages are sent to the configured remote Ollama endpoint.")
        probe { result -> if (!player.hasFinished()) player.sendMessage(result) }
    }

    private fun startNext(key: String) {
        if (!activeConversations.add(key)) return
        val pending = queues[key]?.poll()
        if (pending == null) {
            activeConversations.remove(key)
            queues.remove(key)
            return
        }
        execute(key, pending)
    }

    private fun execute(key: String, pending: PendingReply) {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        if (!settings.enabled || System.currentTimeMillis() < unavailableUntil.get()) {
            finish(key, pending, null)
            return
        }
        val history = histories.computeIfAbsent(key) { ArrayDeque() }
        val messages = JsonArray().apply { add(jsonMessage("system", systemPrompt(pending))) }
        synchronized(history) {
            history.addLast(Turn("user", cleanInput(pending.input)))
            trim(history)
            history.forEach { messages.add(jsonMessage(it.role, it.content)) }
        }
        val body = JsonObject().apply {
            addProperty("model", settings.model)
            add("messages", messages)
            addProperty("stream", false)
            addProperty("think", false)
            add("options", JsonObject().apply {
                addProperty("temperature", 0.8)
                addProperty("num_predict", 45)
            })
        }
        val request = try {
            HttpRequest.newBuilder(URI.create(settings.baseUrl.trimEnd('/') + "/api/chat"))
                .timeout(Duration.ofSeconds(settings.timeoutSeconds.coerceIn(5, 120).toLong()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build()
        } catch (_: Throwable) {
            finish(key, pending, null)
            return
        }
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete { response, error ->
            val generated = if (error == null && response?.statusCode() == 200) parseReply(response.body()) else null
            if (generated == null) markUnavailable(error, response?.statusCode()) else {
                lastSuccessfulResponse.set(System.currentTimeMillis())
                unavailableUntil.set(0L)
            }
            finish(key, pending, generated)
        }
    }

    private fun finish(key: String, pending: PendingReply, generated: String?) {
        if (generated != null) histories[key]?.let { history -> synchronized(history) {
            history.addLast(Turn("assistant", generated)); trim(history)
        } } else histories[key]?.let { history -> synchronized(history) {
            if (history.peekLast()?.role == "user") history.removeLast()
        } }
        if (generated != null) persistHistories()
        WorldTasks.delay(0) { pending.deliver(generated ?: pending.fallback()) }
        activeConversations.remove(key)
        startNext(key)
    }

    private fun persistHistories() {
        if (!SimulatedPlayerPopulationManager.ollamaSettings.persistHistory) return
        try {
            val snapshot = histories.entries.toList().takeLast(MAX_SAVED_CONVERSATIONS).associate { (key, history) ->
                key to synchronized(history) { history.toList() }
            }
            LocalFileStore.writeAtomic(HISTORY_FILE, gson.toJson(snapshot))
        } catch (error: Throwable) {
            Logger.handle(javaClass, "persistHistories", "Unable to save Ollama conversation history", error)
        }
    }

    private fun systemPrompt(pending: PendingReply): String = with(pending) { """
        You are ${bot.displayName}, a normal player inside RuneScape as it was in 2012.
        You are combat level ${bot.skills.combatLevelWithSummoning}, near ${bot.definition.location.ifBlank { "Gielinor" }}, and your clan is ${bot.social.clanName ?: "none"}.
        You are talking to ${player.displayName} in $channel chat. Stay in character and never mention being a bot, AI, prompt, or language model.
        Reply naturally in one short RuneScape chat line, at most 100 characters. No formatting, quotes, narration, links, slurs, sexual content, or real-world politics.
        Remember the conversation. It is fine to ask a short follow-up question. Do not invent game actions, items, trades, levels, or clan membership.
    """.trimIndent() }

    private fun parseReply(body: String): String? = try {
        JsonParser.parseString(body).asJsonObject.getAsJsonObject("message")?.get("content")?.asString
            ?.replace(Regex("<[^>]*>"), "")?.replace(Regex("[\\r\\n\\t]+"), " ")
            ?.replace(Regex("\\s+"), " ")?.trim()?.trim('"', '\'', '`')?.take(100)?.trim()?.takeIf { it.isNotBlank() }
    } catch (_: Throwable) { null }

    private fun conversationKey(channel: String, player: Player, bot: SimulatedPlayerBot) =
        "$channel:${player.username.lowercase()}:${bot.username.lowercase()}"
    private fun historyLimit() = SimulatedPlayerPopulationManager.ollamaSettings.historyMessages.coerceIn(2, 20)
    private fun trim(history: ArrayDeque<Turn>) { while (history.size > historyLimit()) history.removeFirst() }
    private fun cleanInput(value: String) = value.replace(Regex("<[^>]*>"), "")
        .replace(Regex("[\\r\\n\\t]+"), " ").trim().take(240)
    private fun jsonMessage(role: String, content: String) = JsonObject().apply {
        addProperty("role", role); addProperty("content", content)
    }

    private fun probe(deliver: (String) -> Unit) {
        val settings = SimulatedPlayerPopulationManager.ollamaSettings
        if (!settings.enabled) return
        val request = try {
            HttpRequest.newBuilder(URI.create(settings.baseUrl.trimEnd('/') + "/api/tags"))
                .timeout(Duration.ofSeconds(3)).GET().build()
        } catch (_: Throwable) {
            deliver("Ollama connection check: invalid endpoint; scripted fallback is active.")
            return
        }
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete { response, error ->
            val result = if (error == null && response?.statusCode() == 200) {
                val installed = try {
                    JsonParser.parseString(response.body()).asJsonObject.getAsJsonArray("models")
                        ?.any { it.asJsonObject.get("name")?.asString == settings.model } == true
                } catch (_: Throwable) { false }
                if (installed) "<col=80ff80>Ollama connection check: online; ${settings.model} is installed.</col>"
                else "<col=ffcc00>Ollama is online, but ${settings.model} is not installed. Run: ollama pull ${settings.model}</col>"
            } else "<col=ffcc00>Ollama connection check: offline; scripted fallback is active.</col>"
            WorldTasks.delay(0) { deliver(result) }
        }
    }

    private fun isLocalEndpoint(baseUrl: String): Boolean = try {
        val host = URI.create(baseUrl).host ?: return false
        host.equals("localhost", true) || host == "127.0.0.1" || host == "::1" || host.startsWith("127.")
    } catch (_: Throwable) { false }

    private fun markUnavailable(error: Throwable?, status: Int?) {
        val now = System.currentTimeMillis()
        unavailableUntil.set(now + 30_000L)
        if (now - lastFailureLog.get() > 60_000L && lastFailureLog.getAndSet(now) < now - 60_000L) {
            Logger.info(javaClass, "reply", "Ollama unavailable (${error?.javaClass?.simpleName ?: "HTTP $status"}); using scripted bot replies")
        }
    }
}
