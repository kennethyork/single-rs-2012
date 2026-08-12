package com.rs.game.content.quests.allfiredup.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.minigames.allfiredup.allFiredUpDialogue
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent

private const val Fyre = 8042

fun SquireFyreD(player: Player, npc: NPC) {
    if (!player.isQuestStarted(Quest.ALL_FIRED_UP) || player.questManager.isComplete(Quest.ALL_FIRED_UP)) {
        allFiredUpDialogue(player, npc, HeadE.HAPPY_TALKING)
        return
    }
    when (player.questManager.getStage(Quest.ALL_FIRED_UP)) {
        2 -> stage2(player, npc)
        3, 4 -> stage3to4(player, npc)
        5 -> stage5(player, npc)
        6 -> stage6(player, npc)
    }
}


private fun stage2 (player: Player, npc: NPC) {
    player.startConversation {
        npc(Fyre, HeadE.SHAKING_HEAD, "Sorry, I'm trying to finish this beacon right now.")
        return@startConversation
    }
}

private fun stage3to4 (player: Player, npc: NPC) {
    player.startConversation {
        player(
            HeadE.HAPPY_TALKING,
            "Hi there. I'm helping Blaze and King Roald test the beacon network. Can you see it from here? Blaze said you have pretty sharp eyes."
        )
        npc(
            Fyre,
            HeadE.LAUGH,
            "Of course I can see it. I haven't spent my entire life practicing my seeing skills for nothing! I'm happy to report that the fire near Blaze is burning brightly."
        )
        player(
            HeadE.HAPPY_TALKING,
            "Terrific! Blaze has asked me to light this fire as well, so he can see how things look from his vantage point."
        )
        npc(
            Fyre, HeadE.HAPPY_TALKING, "Be my guest!"
        )
        exec { player.questManager.setStage(Quest.ALL_FIRED_UP, 4) }
        return@startConversation
    }
}

private fun stage5 (player: Player, npc: NPC) {
    player.startConversation {
        player(HeadE.HAPPY_TALKING, "The beacon is lit!")
        npc(
            Fyre,
            HeadE.HAPPY_TALKING,
            "And doesn't it look fantastic? Ah, I could gaze into such beautiful flames for hours; too bad I have to keep an eye on the horizon as well. But I'm keeping you too long - you should report back to Blaze to make sure he can see the beacon clearly."
        )
        player(HeadE.HAPPY_TALKING, "I will, thanks.")
        return@startConversation
    }
}

private fun stage6 (player: Player, npc: NPC) {
    player.startConversation {
        player(
            HeadE.HAPPY_TALKING,
            "Just to let you know, Blaze can see the beacon clearly - all's well."
        )
        npc(Fyre, HeadE.HAPPY_TALKING, "Wonderful news! King Roald must be so pleased.")
        player(HeadE.CONFUSED, "Well, I haven't exactly filled him in yet.")
        npc(
            Fyre,
            HeadE.HAPPY_TALKING,
            "Oh, but you shouldn't delay! King Roald has so many sleepless nights about the security of Misthalin and he'll be very keen to hear that everything is working smoothly."
        )
        player(HeadE.HAPPY_TALKING, "Yes. I suppose I should pay him a visit.")
        return@startConversation
    }
}