package com.rs.db.local

import com.google.gson.Gson
import com.rs.Settings
import com.rs.lib.game.Rights
import com.rs.lib.model.Account
import com.rs.lib.util.Utils
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private data class LocalCredential(val salt: String, val passwordHash: String, val iterations: Int = 120_000)

object LocalAccountStore {
    private val gson = Gson()
    private val random = SecureRandom()

    @JvmStatic
    @Synchronized
    fun authenticate(rawUsername: String?, password: String?): Account? {
        val username = Utils.formatPlayerNameForProtocol(rawUsername ?: "")
        if (!username.matches(Regex("[a-z0-9_]{1,12}"))) return null

        val path = "accounts/$username.json"
        // Single-player saves are selected solely by username. Keep a small account
        // marker so account discovery still works, but never require or persist a password.
        if (LocalFileStore.read(path) == null)
            LocalFileStore.writeAtomic(path, "{\"local\":true}")

        return Account(username).also {
            it.rights = if (username == "root" || Settings.isOwner(username)) Rights.OWNER else Rights.PLAYER
        }
    }

    @JvmStatic
    fun find(username: String?): Account? {
        val normalized = Utils.formatPlayerNameForProtocol(username ?: "")
        if (LocalFileStore.read("accounts/$normalized.json") == null) return null
        return Account(normalized).also {
            it.rights = if (normalized == "root" || Settings.isOwner(normalized)) Rights.OWNER else Rights.PLAYER
        }
    }

    private fun hash(password: String, salt: ByteArray, iterations: Int): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        return try {
            Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)
        } finally {
            spec.clearPassword()
        }
    }

    private fun constantTimeEquals(left: String, right: String): Boolean {
        val a = left.toByteArray()
        val b = right.toByteArray()
        var difference = a.size xor b.size
        for (index in 0 until maxOf(a.size, b.size))
            difference = difference or ((a.getOrElse(index) { 0 }.toInt()) xor b.getOrElse(index) { 0 }.toInt())
        return difference == 0
    }
}
