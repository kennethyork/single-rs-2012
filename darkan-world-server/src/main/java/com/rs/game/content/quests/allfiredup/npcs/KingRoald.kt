// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.quests.allfiredup.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.player.Player
import com.rs.utils.ItemConfig

private const val KING_ROALD = 648

fun KingRoaldAllFiredUpD(player: Player) {
    when (player.questManager.getStage(Quest.ALL_FIRED_UP)) {
        0 -> stage0(player)
        1 -> stage1to2(player)
        3, 4, 5 -> stage3to5(player)
        6 -> stage6(player)
        7 -> complete(player)
    }
}

private fun stage0(player: Player) {
    player.startConversation {
        npc(
            KING_ROALD,
            HeadE.HAPPY_TALKING,
            "Well, you see, because of the mounting threats from Morytania and the Wilderness, the southern kingdoms have banded together to take action."
        )
        npc(
            KING_ROALD,
            HeadE.CALM_TALK,
            "We have constructed a network of beacons that stretch all the way from the source of the River Salve to the northwestern-most edge of the Wilderness."
        )
        npc(
            KING_ROALD,
            HeadE.CALM_TALK,
            "Should there be any threat from these uncivilized lands, we'll be able to spread the word as fast as the light of the flames can travel."
        )
        player(HeadE.CONFUSED, "So, how could I be of help?")
        npc(
            KING_ROALD,
            HeadE.SKEPTICAL,
            "The task itself should be rather straightforward: I need you to help us test the network of beacons to make sure everything is in order, in case the worst should occur."
        )
        questStart(Quest.ALL_FIRED_UP)
        player(HeadE.HAPPY_TALKING, "I'd be happy to help.")
        npc(
            KING_ROALD,
            HeadE.HAPPY_TALKING,
            "Excellent! The kingdom of Misthalin is eternally grateful."
        )
        player(HeadE.CONFUSED, "So, what do I need to do?")
        npc(
            KING_ROALD,
            HeadE.CALM_TALK,
            "Talk to the head fire tender, Blaze Sharpeye - he'll explain everything. He is stationed just south of the Temple of Paterdomus, on the cliffs by the River Salve."
        )
        player(HeadE.CALM_TALK, "Thank you, Your Majesty, I'll seek out Blaze straight away.")
        npc(
            KING_ROALD,
            HeadE.HAPPY_TALKING,
            "With speed, " + player.displayName + ". The security of Misthalin is in your hands."
        )
        exec {
            player.questManager.setStage(Quest.ALL_FIRED_UP, 1)
        }
        return@startConversation
    }
}

private fun stage1to2(player: Player) {
    player.startConversation {
        npc(KING_ROALD, HeadE.HAPPY_TALKING, "So? Are you here to bring me news of the beacons?")
        player(
            HeadE.CONFUSED,
            "Yes, about that: could you remind me where to go and what I need to do?"
        )
        npc(
            KING_ROALD,
            HeadE.SKEPTICAL_HEAD_SHAKE,
            "Just talk to Blaze Sharpeye, the finest fire tender this side of the River Salve. You'll find him near the Temple of Paterdomus. He'll tell you everything you need to know."
        )
        player(HeadE.CALM_TALK, "Okay, thanks.")
        return@startConversation
    }
}

private fun stage3to5(player: Player) {
    player.startConversation {
        npc(KING_ROALD, HeadE.HAPPY_TALKING, "So? Are you here to bring me news of the beacons?")
        player(
            HeadE.HAPPY_TALKING,
            "Well, I've met with Blaze, but we've not finished testing the system."
        )
        npc(
            KING_ROALD,
            HeadE.VERY_FRUSTRATED,
            "Then what are you doing talking to me? Don't you realise the security of Misthalin is at stake?"
        )
        player(HeadE.SAD_MILD_LOOK_DOWN, "Sorry; I'll get right back to it.")
        npc(KING_ROALD, HeadE.FRUSTRATED, "See that you do, and be quick about it!")
        return@startConversation
    }
}

private fun stage6(player: Player) {
    player.startConversation {
        player(
            HeadE.HAPPY_TALKING,
            "I'm happy to report that the beacon network seems to be working as expected."
        )
        npc(KING_ROALD, HeadE.HAPPY_TALKING, "Excellent! I'm delighted to hear it.")
        player(HeadE.HAPPY_TALKING, "So, about that reward you promised?")
        npc(
            KING_ROALD,
            HeadE.LOSING_IT_LAUGHING,
            "What happened to the days when adventurers felt rewarded in full by the knowledge of a job well done?"
        )
        player(HeadE.FRUSTRATED, "Well, before my time, I'm afraid.")
        npc(
            KING_ROALD,
            HeadE.HAPPY_TALKING,
            "Hmph. Well, I suppose a king must stick to his word. Mind you, let me stress how grateful we are - and how grateful we'd be if you could continue helping us test the beacons."
        )
        npc(
            KING_ROALD,
            HeadE.HAPPY_TALKING,
            "There is much more to be done and this is but a pittance compared to what I'm willing to offer for further assistance!"
        )
        exec {
            player.questManager.completeQuest(Quest.ALL_FIRED_UP)
        }
        return@startConversation
    }
}

private fun complete(player: Player) {
    player.startConversation {
        player(HeadE.CALM_TALK, "I was wondering if you could tell me more about the beacons.")
        npc(KING_ROALD, HeadE.HAPPY_TALKING, "Of course. What would you like to know?")
        options {
            op("What else needs to be done to test the beacon network?") {
                player(HeadE.CONFUSED, "What else needs to be done to test the beacon network?")
                npc(
                    KING_ROALD,
                    HeadE.FRUSTRATED,
                    "What *doesn't* need to be done is more like it! We need to make sure the entire beacon network is as robust as possible. I'd be extremely grateful if you'd help us by trying to keep as many beacons alight simultaneously as you possibly can."
                )
                player(HeadE.CONFUSED, "How does that help?")
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "Well, obviously, it helps us verify that all the beacons are in good working order, of course. The more testing, the better, " + player.displayName + "."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "I have generous rewards for you if you manage to keep six, ten and all fourteen beacons alight simultaneously."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "However, I'd be positively delighted if you continued on testing them day and night, over and over, relentlessly and without ceasing!"
                )
                player(HeadE.AMAZED_MILD, "I see this means a great deal to you.")
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "Oh, it does. The safety of one's kingdom is of paramount importance, you know. Do you have any other question for me, " + player.displayName + "?"
                )
            }
            op("Tell me more about rewards.") {
                player(HeadE.CALM_TALK, "Tell me about rewards.")
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "Ahem, well, it's all well and good having a network of beacons, but it's important to make sure it works properly...and remains working. Thus, I'm willing to offer various rewards if you are willing to help me out."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "There are rewards available for keeping six, ten and all fourteen beacons alight simultaneously."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "For keeping six beacons alight, I'm prepared to grant you a ring that will aid you in Firemaking."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "For keeping ten beacons alight, I will grant you a pair of gloves that will work in combination with the ring."
                )
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "For keeping all fourteen beacons alight, well, I have the magnificent Inferno Adze - a fire-enchanting tool for both Woodcutting and Mining - that I'm prepared to part with. I think you'll find it of great use."
                )
                player(HeadE.HAPPY_TALKING, "How do I claim these rewards?")
                npc(
                    KING_ROALD,
                    HeadE.HAPPY_TALKING,
                    "Just light the beacons! If you manage to keep the requisite number alight simultaneously, return@startConversation to me and I'll have your reward waiting."
                )
                player(HeadE.HAPPY_TALKING, "But how will you know I've kept them lit?")
                npc(
                    KING_ROALD,
                    HeadE.SKEPTICAL_THINKING,
                    "Oh, don't you worry about that. I have my eyes and ears. Is there anything else?"
                )
            }
            op("I don't have anything more to talk about, actually.") {
                player(HeadE.SHAKING_HEAD, "I don't have anything more to talk about, actually?")
                npc(KING_ROALD, HeadE.ANGRY, "Then begone; I am a busy man.")
            }
            op("Check rewards") { exec { checkRewards(player) } }

        }
    }
}


fun checkRewards(player: Player) {
    val RingOfFire = 13659
    val FlameGloves = 13660
    val InfernoAdze = 13661
    if (!player.questManager.isComplete(Quest.ALL_FIRED_UP)) return
    if (player.getBool("AllFiredUpI")) {
        if (!player.getBool("AllFiredUpI.claimed")) {
            if (player.inventory.hasFreeSlots()) {
                player.set("AllFiredUpI.claimed", true)
                player.inventory.addItem(RingOfFire)
                player.sendMessage(
                    "King Roald hands you a " + ItemConfig.get(RingOfFire).uidName.replace(
                        "_".toRegex(),
                        " "
                    )
                )
            } else player.sendMessage("You do not have enough space to claim a reward")
        }
    }
    if (player.getBool("AllFiredUpII")) {
        if (!player.getBool("AllFiredUpII.claimed")) {
            if (player.inventory.hasFreeSlots()) {
                player.set("AllFiredUpII.claimed", true)
                player.inventory.addItem(FlameGloves)
                player.sendMessage(
                    "King Roald hands you a " + ItemConfig.get(FlameGloves).uidName.replace(
                        "_".toRegex(),
                        " "
                    )
                )
            } else player.sendMessage("You do not have enough space to claim a reward")
        }
    }
    if (player.getBool("AllFiredUpIII")) {
        if (!player.getBool("AllFiredUpIII.claimed")) {
            if (player.inventory.hasFreeSlots()) {
                player.set("AllFiredUpIII.claimed", true)
                player.inventory.addItem(InfernoAdze)
                player.sendMessage(
                    "King Roald hands you a " + ItemConfig.get(InfernoAdze).uidName.replace(
                        "_".toRegex(),
                        " "
                    )
                )
            } else player.sendMessage("You do not have enough space to claim a reward")
        }
    } else
        player.startConversation {
            npc(KING_ROALD, HeadE.CALM_TALK, "You don't have any rewards to claim at this time.")
        }
}
