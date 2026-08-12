package com.rs.game.content.world.npcs

import com.rs.db.WorldDB
import com.rs.engine.thread.AsyncTaskExecutor
import com.rs.game.ge.GE
import com.rs.game.ge.Offer
import com.rs.lib.util.Logger
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.utils.EconomyPrices
import com.rs.utils.Ticks
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val BOT_OWNER_PREFIX = "ge_bot_"
data class SimulatedPlayerEconomySettings(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 5,
    val fillChancePercent: Int = 35,
    val maxUnitsPerOffer: Int = 100,
    val minimumGuidePricePercent: Int = 50,
    val maximumGuidePricePercent: Int = 200
)

/**
 * A conservative market-maker that lets the simulated population buy from and
 * sell to real GE offers. It only accepts prices near the server guide price,
 * fills a bounded amount per cycle, and never stores synthetic collection-box
 * proceeds.
 */
object SimulatedPlayerEconomy {
    @Synchronized
    fun runCycle() {
        val settings = SimulatedPlayerPopulationManager.economySettings
        if (!settings.enabled) return
        val offers = WorldDB.getGE().getAllStableOffersSync()
        if (offers.isEmpty()) return

        val ownersToUpdate = HashSet<String>()
        offers.shuffled().forEach { playerOffer ->
            if (playerOffer.owner.startsWith(BOT_OWNER_PREFIX) || playerOffer.amountLeft() <= 0) return@forEach
            if (Random.nextInt(100) >= settings.fillChancePercent.coerceIn(0, 100)) return@forEach

            val guidePrice = EconomyPrices.getPrice(playerOffer.itemId)
            if (guidePrice <= 0 || playerOffer.price !in safePriceRange(guidePrice, settings)) return@forEach

            val amount = min(playerOffer.amountLeft(), settings.maxUnitsPerOffer.coerceIn(1, 100_000))
            val botOffer = Offer(
                "$BOT_OWNER_PREFIX${Random.nextInt(1, 10_000)}",
                0,
                !playerOffer.isSelling,
                playerOffer.itemId,
                amount,
                playerOffer.price,
                if (playerOffer.isSelling) GE.OfferType.BUY else GE.OfferType.SELL
            )
            botOffer.state = Offer.State.STABLE

            if (playerOffer.process(botOffer)) {
                WorldDB.getGE().saveSync(playerOffer)
                ownersToUpdate += playerOffer.owner
            }
        }

        ownersToUpdate.forEach(GE::updateOffers)
        if (ownersToUpdate.isNotEmpty())
            Logger.info(javaClass, "runCycle", "Bot economy updated ${ownersToUpdate.size} player GE offers")
    }

    private fun safePriceRange(guidePrice: Int, settings: SimulatedPlayerEconomySettings): IntRange {
        val lowPercent = settings.minimumGuidePricePercent.coerceIn(1, 10_000)
        val highPercent = settings.maximumGuidePricePercent.coerceIn(lowPercent, 10_000)
        val low = max(1, (guidePrice.toLong() * lowPercent / 100L).toInt())
        val high = min(Int.MAX_VALUE.toLong(), guidePrice.toLong() * highPercent / 100L).toInt()
        return low..high
    }
}

@ServerStartupEvent(ServerStartupEvent.Priority.POST_PROCESS)
fun startSimulatedPlayerEconomy() {
    val settings = SimulatedPlayerPopulationManager.economySettings
    if (!settings.enabled) return
    AsyncTaskExecutor.schedule(
        SimulatedPlayerEconomy::runCycle,
        Ticks.fromMinutes(2),
        Ticks.fromMinutes(settings.intervalMinutes.coerceIn(1, 1440))
    )
}
