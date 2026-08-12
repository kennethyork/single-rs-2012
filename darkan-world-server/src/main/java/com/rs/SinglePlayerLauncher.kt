package com.rs

import java.net.InetSocketAddress
import java.net.Socket

/** Starts the local world and matching client in one JVM. */
object SinglePlayerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
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
