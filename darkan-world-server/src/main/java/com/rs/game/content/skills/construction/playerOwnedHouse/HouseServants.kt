package com.rs.game.content.skills.construction.playerOwnedHouse

enum class HouseServants(
    val id: Int,
    val hirePrice: Int,
    val bankCost: Int,
    val level: Int,
    val foodId: Int,
    val isSawmill: Boolean,
    val inventorySize: Int,
    val bankDelay: Long
) {
    RICK(4235, 375, 500, 20, 315, false, 6, 100),
    MAID(4237, 750, 1000, 25, 2003, false, 10, 50),
    COOK(4239, 2250, 3000, 30, 2301, true, 16, 28),
    BUTLER(4241, 3750, 5000, 40, 1897, true, 20, 20),
    DEMON_BUTLER(4243, 7500, 10000, 50, 2011, true, 26, 12)
}