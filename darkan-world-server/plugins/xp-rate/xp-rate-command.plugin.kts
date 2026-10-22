import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.lib.game.Rights
import java.io.File

// In-game XP rate control: ::xprate shows the current rate, ::xprate <n> changes it.
// The chosen rate is saved to saves/xp-rate.txt and restored on the next launch,
// unless a rate is passed to the launcher (e.g. run.bat 25), which takes priority.

val xpRateFile = File(System.getProperty("darkan.save.path") ?: "./saves", "xp-rate.txt")

fun applyXpRate(rate: Int) {
    System.setProperty("darkan.xpRate", rate.toString())
    runCatching {
        val field = Settings::class.java.getDeclaredField("xpRate")
        field.isAccessible = true
        when (field.type) {
            Int::class.javaPrimitiveType -> field.setInt(Settings.getConfig(), rate)
            Double::class.javaPrimitiveType -> field.setDouble(Settings.getConfig(), rate.toDouble())
        }
    }
}

if (System.getProperty("darkan.xpRate") == null) {
    xpRateFile.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()?.takeIf { it > 0 }?.let { applyXpRate(it) }
}

Commands.add(Rights.PLAYER, "xprate,setxprate [rate]", "Shows or sets the XP rate multiplier (saved between sessions).") { player, args ->
    if (args.isEmpty()) {
        player.sendMessage("Your XP rate is ${Settings.getConfig().xpRate}x. Use ::xprate <number> to change it.")
    } else {
        val rate = args[0].removeSuffix("x").toIntOrNull()
        if (rate == null || rate < 1) {
            player.sendMessage("The XP rate must be a whole number of 1 or more, e.g. ::xprate 25")
        } else {
            applyXpRate(rate)
            runCatching { xpRateFile.parentFile?.mkdirs(); xpRateFile.writeText(rate.toString()) }
            player.sendMessage("XP rate set to ${Settings.getConfig().xpRate}x.")
        }
    }
}
