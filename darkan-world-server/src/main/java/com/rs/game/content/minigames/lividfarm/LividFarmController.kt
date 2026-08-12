package com.rs.game.content.minigames.lividfarm

import com.rs.game.World
import com.rs.game.World.sendSpotAnim
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Controller
import com.rs.game.model.entity.player.Player
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.utils.Ticks
import com.rs.engine.variables.lunarfm_pauline_state

object LividFarmController : Controller() {

    private val players = mutableSetOf<Player>()
    private val patchStates = mutableMapOf<Int, Int>()
    private var roundCounter = 0
    private var cycleCounter = 0
    private var running = false


    fun startFarmLoop() {
        if (running) return
        running = true
        Logger.info(LividFarmController::class.java, "startFarmLoop", "Farm loop started")
        WorldTasks.scheduleTimer(0, Ticks.fromSeconds(1)) {
            updatePlayersInArea()
            true
        }
        //Rush the first round on server start to stabilize the state.
        for (initialRound in 1..5) {
            roundCounter = initialRound
            runRound(roundCounter)
        }
        WorldTasks.scheduleTimer(0, Ticks.fromSeconds(60)) {
            roundCounter++
            if (roundCounter > 5) {
                roundCounter = 1
                cycleCounter++
//                Logger.info(LividFarmController::class.java, "startFarmLoop", "Current Cycle: $cycleCounter")
            }
//            Logger.info(LividFarmController::class.java, "startFarmLoop", "Current round: $roundCounter")
            runRound(roundCounter)
            true
        }
    }

    override fun start() {
        joinFarm(player)
    }

    override fun logout(): Boolean {
        leaveFarm(player)
        return false
    }

    override fun login(): Boolean {
        joinFarm(player)
        return false
    }

    override fun forceClose() {
        leaveFarm(player)
    }

    override fun sendDeath(): Boolean {
        leaveFarm(player)
        removeController()
        return true
    }

    fun joinFarm(player: Player) {
        synchronized(players) {
            players.add(player)
            syncRoundStateToPlayer(player)
        }
    }

    private fun leaveFarm(player: Player) {
        synchronized(players) {
            players.remove(player)
            clearRoundStateFromPlayer(player)
            player.tempAttribs.removeB("LividFarm.meteoraIbis.IbisExamined")
            player.tempAttribs.removeB("LividFarm.meteoraIbis.IbisPrompted")
            player.tempAttribs.removeB("LividFarm.meteoraIbis.Claimed")
            player.tempAttribs.removeB("LividFarm.Suqah.Claimed")
            player.tempAttribs.removeB("LividFarm.MurkyPat.Claimed")
        }
    }

    private fun syncRoundStateToPlayer(player: Player) {
        val paulineDrainVarbit = 9063
        val producePileVarbit = 9042
        //Logger.info(LividFarmController::class.java, "syncRoundStateToPlayer", "Sending state to player: ${player.displayName}")
        player.vars.setVarBit(paulineDrainVarbit, if (roundCounter == 2) 1 else 0)
        player.vars.setVarBit(producePileVarbit, if (roundCounter == 4) 1 else 0)
        patchStates.forEach { (varbit, state) ->
            player.vars.setVarBit(varbit, state)
        }
        LividFarm.FENCES.forEach { fence ->
            val state = players.firstOrNull()?.vars?.getVarBit(fence.varbit) ?: 0
            player.vars.setVarBit(fence.varbit, state)
        }
        player.vars.syncVarsToClient()
    }

    private fun clearRoundStateFromPlayer(player: Player) {
        val paulineDrainVarbit = 9063
        val producePileVarbit = 9042
        //Logger.info(LividFarmController::class.java, "clearRoundStateFromPlayer", "Clearing state from player: ${player.displayName}")
        player.vars.setVarBit(paulineDrainVarbit, 0)
        player.vars.setVarBit(producePileVarbit, 0)
        LividFarm.PATCHES.forEach { player.vars.setVarBit(it.varbit, 0) }
        LividFarm.FENCES.forEach { player.vars.setVarBit(it.varbit, 0) }
        player.vars.syncVarsToClient()
    }

    private fun updatePlayersInArea() {
        //Logger.info(LividFarmController::class.java, "updatePlayersInArea", "Looking for players")
        val currentlyInArea = World.players.filter {
            it.tile.x in 2096..2120 && it.tile.y in 3941..3953
        }.toSet()
        currentlyInArea.forEach { player ->
            if (!players.contains(player)) {
                //Logger.info(LividFarmController::class.java, "updatePlayersInArea", "Updating players adding ${player.displayName}")
                joinFarm(player)
            }
        }
        val toRemove = players.filterNot { currentlyInArea.contains(it) }
        toRemove.forEach { player ->
            //Logger.info(LividFarmController::class.java, "updatePlayersInArea", "Updating players removing ${player.displayName}")
            leaveFarm(player)
        }
    }

    private fun runRound(round: Int) {
        triggerPaulineWorldTalk()
        WorldTasks.delay(Ticks.fromSeconds(3)) {
            updatePatchStates()
            resetAndBreakFences()
            triggerDistractions()
            updateRoundSpecificVarbits(round)
        }
    }

    private fun updatePatchStates() {
        // 1) Random growth transitions
        val nextStates = mutableMapOf<Int, Int>()
        for (patch in LividFarm.PATCHES) {
            val vb = patch.varbit
            val current = patchStates[vb] ?: 0
            nextStates[vb] = when (current) {
                0 -> 1                      // empty → bud
                1 -> 2                      // bud → healthy
                2 -> Utils.random(3, 4)     // healthy → diseased(3) or fully-grown(4)
                3 -> Utils.random(3, 0)     // diseased → stays diseased(3) or becomes empty(0)
                4 -> 0                      // fully-grown → empty
                else -> 1
            }
        }

        // 2) Clamp to exactly 3 empty patches
        val empties = nextStates.filterValues { it == 0 }.keys.toMutableList()
        if (empties.size > 3) {
            // too many empties → revert extras back to healthy
            empties.shuffled().take(empties.size - 3).forEach { vb ->
                nextStates[vb] = 2
            }
        } else if (empties.size < 3) {
            // too few empties → turn healthy or diseased into empty
            nextStates.filterValues { it == 2 || it == 3 }
                .keys
                .shuffled()
                .take(3 - empties.size)
                .forEach { vb ->
                    nextStates[vb] = 0
                }
        }

        // 3) Clamp to exactly 3 diseased plants
        val beforeDiseased = patchStates.filter { it.value == 3 }.keys
        val diseased = nextStates.filterValues { it == 3 }.keys.toMutableList()

        if (diseased.size > 3) {
            // too many diseased → revert extras back to healthy
            diseased.shuffled().take(diseased.size - 3).forEach { vb ->
                nextStates[vb] = 2
            }
        } else if (diseased.size < 3) {
            // too few diseased → promote healthy → diseased
            nextStates.filterValues { it == 2 }
                .keys
                .shuffled()
                .take(3 - diseased.size)
                .forEach { vb ->
                    nextStates[vb] = 3
                }
        }

        // spot-anim only on patches that became diseased this round
        val afterDiseased = nextStates.filterValues { it == 3 }.keys
        (afterDiseased - beforeDiseased).forEach { vb ->
            val patch = LividFarm.PATCHES.first { it.varbit == vb }
            sendSpotAnim(patch.tile, SpotAnim(2309, 0, 100))
        }

        // 4) Commit states and sync all players
        nextStates.forEach { (vb, state) ->
            patchStates[vb] = state
            players.forEach { it.vars.setVarBit(vb, state) }
        }
        players.forEach { it.vars.syncVarsToClient() }
    }


    private fun resetAndBreakFences() {
        LividFarm.FENCES.forEach { fence ->
            players.forEach { it.vars.setVarBit(fence.varbit, 0) }
        }

        LividFarm.FENCES.shuffled().take(2).forEach { fence ->
            players.forEach { it.vars.setVarBit(fence.varbit, 1) }
        }
    }

    private fun updateRoundSpecificVarbits(round: Int) {
        val paulineDrainVarbit = 9063
        val producePileVarbit = 9042
        players.forEach {
            it.vars.setVarBit(paulineDrainVarbit, if (round == 2) 1 else 0)
            it.vars.setVarBit(producePileVarbit, if (round == 4) 1 else 0)
        }
    }

    private fun triggerPaulineWorldTalk() {
        LividFarm.pauline?.let { npc ->
            npc.anim(4432)
            npc.spotAnim(742, 0, 100)
            when (Utils.random(3)) {
                1 -> npc.forceTalk("More spellcasting's required.")
                2 -> npc.forceTalk("Time for more work!")
                3 -> npc.forceTalk("The farm's moved on to its next stage.")
                else -> npc.forceTalk("Everything's advanced. Help, please!")
            }
        }
    }

    private fun triggerDistractions() {
        if(cycleCounter == 0)
            return
        if(Utils.random(10) == 0)
            when (Utils.random(3)) {
                0 -> meteoraIbis()
                1 -> suqah()
                2 -> murkyPat()
            }
    }

    val Suqah = 13622
    val Meteora = 13623
    val Ibis = 13624
    val MurkyPat = 13625
    val Pauline = 13621

    var activeMeteora: NPC? = null
    var activeIbis: NPC? = null
    var activeSuqah: NPC? = null
    var activeMurkyPat: NPC? = null

    val ailments = listOf(
        "Wing rash", "Indigestion", "Bruised ego", "Cracked beak", "Crumpled feathers",
        "Fishy throat", "Blistered feet", "Scratched eyelids", "Extreme headache", "Trapped wind"
    )
    var activeAilment: String? = null

    val targets = listOf(
        "angry wives", "ship captains", "ladies in chicken houses", "Purky Matt", "Rellekka bards",
        "chefs on vessels", "anyone who calls themselves Timfraku", "Lunar Isle monsters"
    )
    var activeTarget: String? = null

    private fun meteoraIbis() {
        val meteoraNPC = World.spawnNPC(Meteora, Tile(2109, 3944, 0))
        val ibisNPC = World.spawnNPC(Ibis, Tile(2109, 3941, 0))
        meteoraNPC.forceTalk("Help!")
        activeMeteora = meteoraNPC
        activeIbis = ibisNPC
        activeAilment = ailments.random()
        players.forEach {
            it.tempAttribs.removeB("LividFarm.meteoraIbis.Claimed")
            it.tempAttribs.removeB("LividFarm.meteoraIbis.IbisExamined")
            it.tempAttribs.removeB("LividFarm.meteoraIbis.PaulineExamined")
        }
        ibisNPC.faceEntity(meteoraNPC)
        WorldTasks.scheduleTimer {
            if (it >= Ticks.fromSeconds(50)) meteoraNPC.forceTalk("I'm going to leave shortly!")
            if (it >= Ticks.fromSeconds(60)) {
                meteoraNPC.finish(); ibisNPC.finish()
                if (activeMeteora === meteoraNPC) activeMeteora = null
                if (activeIbis === ibisNPC) activeIbis = null
                false
            } else true
        }
    }

    private fun suqah() {
        val npc = World.spawnNPC(Suqah, Tile(2114, 3946, 0))
        activeSuqah = npc
        npc.capDamage = 0
        LividFarm.pauline?.capDamage ?: 0
        npc.combatTarget = LividFarm.pauline
        npc.attackedBy = LividFarm.pauline
        players.forEach {
            it.vars.setVarBit(lunarfm_pauline_state, 1);
            it.vars.syncVarsToClient()
            it.tempAttribs.removeB("LividFarm.Suqah.Claimed")
        }
        WorldTasks.scheduleTimer {
            if (it >= Ticks.fromSeconds(30)) {
                npc.anim(4389)
                WorldTasks.delay(Ticks.fromSeconds(2)) {
                    players.forEach {
                        it.vars.setVarBit(lunarfm_pauline_state, 0);
                    }
                    npc.finish()
                    activeSuqah = null
                }
                if (activeSuqah === npc) activeSuqah = null
                false
            } else true
        }
    }

    private fun murkyPat() {
        val npc = World.spawnNPC(MurkyPat, Tile(2115, 3944, 0))
        activeMurkyPat = npc
        activeTarget = targets.random()
        players.forEach {
            it.tempAttribs.removeB("LividFarm.MurkyPat.Claimed")
        }
        WorldTasks.scheduleTimer {
            if (it < Ticks.fromSeconds(50) && it % Ticks.fromSeconds(10) == 0)
                npc.forceTalk(listOf("Hey! A little help here!", "Help!", "Hey!").random())
            if (it >= Ticks.fromSeconds(50)) npc.forceTalk("I'm going to leave shortly!")
            if (it >= Ticks.fromSeconds(60)) {
                npc.finish()
                if (activeMurkyPat === npc) activeMurkyPat = null
                activeTarget = null
                false
            } else true
        }
    }
}

@ServerStartupEvent
fun initLividFarmController() {
    LividFarmController.startFarmLoop()
}
