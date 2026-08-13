package com.rs

import java.net.InetSocketAddress
import java.net.Socket
import java.io.File

/** Starts the local world and matching client in one JVM. */
object SinglePlayerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.contains("--smoke-test")) {
            smokeTest()
            return
        }
        System.setProperty("darkan.singlePlayer", "true")
        Launcher.main(emptyArray())
        waitForWorld(Settings.getConfig().worldInfo.port())
        Runtime.getRuntime().addShutdownHook(Thread({
            try {
                Launcher.saveFilesSync()
            } catch (_: Throwable) {
                // The 30-second autosave remains the fallback if shutdown is abrupt.
            }
        }, "single-player-final-save"))

        val client = Class.forName("com.rs.Loader")
        val clientMain = client.getMethod("main", Array<String>::class.java)
        clientMain.invoke(null, arrayOf("127.0.0.1"))
    }

    private fun smokeTest() {
        check(Runtime.version().feature() >= 24) { "Java 24 or newer is required" }
        val cache = File(System.getProperty("darkan.cache.path", "../darkan-cache"))
        check(File(cache, "main_file_cache.dat2").isFile) { "Cache is missing from ${cache.absolutePath}" }
        check(File("./data/npcs/simulated-players.json").isFile) { "Simulated-player configuration is missing" }
        Class.forName("com.rs.Launcher", false, javaClass.classLoader)
        Class.forName("com.rs.Loader", false, javaClass.classLoader)
        println("SMOKE TEST PASSED: Java ${Runtime.version().feature()}, cache, world, client, and bot configuration")
    }

    private fun waitForWorld(port: Int) {
        repeat(100) {
            try {
                Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 100) }
                return
            } catch (_: Exception) {
                Thread.sleep(100)
            }
        }
        error("The local world did not open port $port.")
    }
}
