package com.rs.game.content.world.npcs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import java.util.concurrent.atomic.AtomicLong

/** Optional, non-blocking Ollama conversation engine with scripted fallback. */
object SimulatedPlayerOllama {
    private data class Turn(val role: String, val content: String)

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build()
    private val histories = ConcurrentHashMap<String, ArrayDeque<Turn>>()
    private val unavailableUntil = AtomicLong(0L)
    private val lastFailureLog = AtomicLong(0L)

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

        val key = "$channel:${player.username}:${bot.username}"
        val history = histories.computeIfAbsent(key) { ArrayDeque() }
        val messages = JsonArray()
        messages.add(jsonMessage("system", systemPrompt(bot, player, channel)))
        synchronized(history) {
            history.addLast(Turn("user", cleanInput(message)))
            trim(history, settings.historyMessages.coerceIn(2, 20))
            history.forEach { messages.add(jsonMessage(it.role, it.content)) }
        }

        val requestBody = JsonObject().apply {
            addProperty("model", settings.model)
            add("messages", messages)
            addProperty("stream", false)
            addProperty("think", false)
            add("options", JsonObject().apply {
                addProperty("temperature", 0.8)
                addProperty("num_predict", 45)
            })
        }
        val endpoint = settings.baseUrl.trimEnd('/') + "/api/chat"
        val request = try {
            HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(settings.timeoutSeconds.coerceIn(5, 120).toLong()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build()
        } catch (error: Throwable) {
            deliver(fallback())
            return
        }

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .whenComplete { response, error ->
                val generated = if (error == null && response?.statusCode() == 200) {
                    parseReply(response.body())
                } else null
                if (generated == null) markUnavailable(error, response?.statusCode())
                val answer = generated ?: fallback()
                if (generated != null) synchronized(history) {
                    history.addLast(Turn("assistant", generated))
                    trim(history, settings.historyMessages.coerceIn(2, 20))
                }
                WorldTasks.delay(0) { deliver(answer) }
            }
    }

    private fun systemPrompt(bot: SimulatedPlayerBot, player: Player, channel: String): String = """
        You are ${bot.displayName}, a normal player inside RuneScape as it was in 2012.
        You are combat level ${bot.skills.combatLevelWithSummoning}, near ${bot.definition.location.ifBlank { "Gielinor" }}, and your clan is ${bot.social.clanName ?: "none"}.
        You are talking to ${player.displayName} in $channel chat. Stay in character and never mention being a bot, AI, prompt, or language model.
        Reply naturally in one short RuneScape chat line, at most 100 characters. No formatting, quotes, narration, links, slurs, sexual content, or real-world politics.
        Remember the conversation. It is fine to ask a short follow-up question. Do not invent game actions, items, trades, levels, or clan membership.
    """.trimIndent()

    private fun parseReply(body: String): String? = try {
        JsonParser.parseString(body).asJsonObject
            .getAsJsonObject("message")?.get("content")?.asString
            ?.replace(Regex("<[^>]*>"), "")
            ?.replace(Regex("[\\r\\n\\t]+"), " ")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()?.trim('"', '\'', '`')
            ?.take(100)?.trim()
            ?.takeIf { it.isNotBlank() }
    } catch (_: Throwable) {
        null
    }

    private fun cleanInput(value: String): String = value
        .replace(Regex("<[^>]*>"), "")
        .replace(Regex("[\\r\\n\\t]+"), " ")
        .trim().take(240)

    private fun jsonMessage(role: String, content: String) = JsonObject().apply {
        addProperty("role", role)
        addProperty("content", content)
    }

    private fun trim(history: ArrayDeque<Turn>, maximum: Int) {
        while (history.size > maximum) history.removeFirst()
    }

    private fun markUnavailable(error: Throwable?, status: Int?) {
        val now = System.currentTimeMillis()
        unavailableUntil.set(now + 30_000L)
        if (now - lastFailureLog.get() > 60_000L && lastFailureLog.getAndSet(now) < now - 60_000L) {
            val detail = error?.javaClass?.simpleName ?: "HTTP $status"
            Logger.info(javaClass, "reply", "Ollama unavailable ($detail); using scripted bot replies")
        }
    }
}
