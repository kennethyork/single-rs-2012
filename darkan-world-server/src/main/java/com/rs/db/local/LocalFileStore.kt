package com.rs.db.local

import com.rs.Settings
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object LocalFileStore {
    @JvmStatic
    fun root(): File = File(Settings.getConfig().savePath).absoluteFile.also { it.mkdirs() }

    @JvmStatic
    fun file(relativePath: String): File = File(root(), relativePath).also { it.parentFile?.mkdirs() }

    @JvmStatic
    fun read(relativePath: String): String? = file(relativePath).takeIf(File::isFile)?.readText(StandardCharsets.UTF_8)

    @JvmStatic
    @Synchronized
    fun writeAtomic(relativePath: String, contents: String) {
        val target = file(relativePath).toPath()
        val temporary = target.resolveSibling("${target.fileName}.tmp")
        Files.newBufferedWriter(temporary, StandardCharsets.UTF_8).use { it.write(contents) }
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: Exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
