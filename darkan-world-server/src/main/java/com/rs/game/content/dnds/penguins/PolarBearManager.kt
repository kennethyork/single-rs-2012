package com.rs.game.content.dnds.penguins

import com.google.gson.Gson
import com.rs.game.World
import com.rs.lib.util.Logger
import com.rs.engine.variables.peng_spy_bear

enum class PolarBearLocation(val id: Int) { RELLEKKA(0), VARROCK(1), RIMMINGTON(2), MUSA_POINT(3), ARDOUGNE(4), FALADOR(5) }

class PolarBearManager() {
    private var polarBearLocationId: Int = 0
    private var previousLocations = mutableListOf<PolarBearLocation>()

    fun setLocation(forceNewLocation: Boolean = false) {
        val polarBear = getPolarBear()
        polarBear?.let {
            polarBearLocationId = it.location.id
            previousLocations = it.previousLocations.toMutableList()
            Logger.debug(PolarBearManager::class.java, "setLocation", "Polar bear location set to ${getLocationName(polarBearLocationId)}.")
        }

        if (forceNewLocation) {
            val newLocation = getNextPolarBearLocation()
            if (newLocation != null) {
                updatePolarBearState(newLocation)
                polarBearLocationId = newLocation.id

                World.players.forEach { player ->
                    player.vars.setVarBit(peng_spy_bear, polarBearLocationId)
                }

                Logger.debug(PolarBearManager::class.java, "setLocation", "Polar bear location changed to $newLocation for ${World.players.size()} logged-in player(s).")
            }
        }
    }

    private fun getNextPolarBearLocation(): PolarBearLocation? {
        val availableLocations = PolarBearLocation.entries.toMutableList()
        availableLocations.removeAll(previousLocations)

        if (availableLocations.isEmpty()) {
            previousLocations.clear()
            availableLocations.addAll(PolarBearLocation.entries)
        }

        val newLocation = availableLocations.random()
        previousLocations.add(newLocation)

        if (previousLocations.size > 4) {
            previousLocations.removeAt(0)
        }

        return newLocation
    }

    fun getCurrentLocationId(): Int = polarBearLocationId

    fun getLocationName(locationId: Int): String? = PolarBearLocation.entries.find { it.id == locationId }?.name
}

fun updatePolarBearState(newLocation: PolarBearLocation) {
    val polarBear = getPolarBear() ?: PolarBear(
        location = newLocation,
        points = 1,
        previousLocations = listOf(),
        spotters = mutableListOf()
    )

    val updatedPolarBear = polarBear.copy(
        location = newLocation,
        previousLocations = polarBear.previousLocations.toMutableList().apply {
            add(newLocation)
            if (size > 4) removeAt(0)
        },
        spotters = mutableListOf()
    )

    World.data.attribs.setO<String>(POLAR_BEAR_WORLD_ATTR, Gson().toJson(updatedPolarBear))
    Logger.debug(PolarBearManager::class.java, "updatePolarBearState", "Updated polar bear state: ${Gson().toJson(updatedPolarBear)}")
}


fun getPolarBear(): PolarBear? {
    val polarBearJson: String? = World.data.attribs.getO(POLAR_BEAR_WORLD_ATTR)
    return if (!polarBearJson.isNullOrEmpty()) {
        Gson().fromJson(polarBearJson, PolarBear::class.java)
    } else null
}

data class PolarBear(
    val location: PolarBearLocation,
    val points: Int,
    val previousLocations: List<PolarBearLocation>,
    val spotters: MutableList<String> = mutableListOf()
) {
    fun addSpotter(username: String) {
        if (!spotters.contains(username)) {
            spotters.add(username)
        }
    }
}
