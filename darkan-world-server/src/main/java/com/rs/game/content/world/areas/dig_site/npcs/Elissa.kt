package com.rs.game.content.world.areas.dig_site.npcs

import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.the_golem.STAGE_LOOT_MUSEUM
import com.rs.game.content.quests.the_golem.STAGE_RETRIEVE_NOTES
import com.rs.game.content.quests.the_golem.STAGE_TALK_TO_ELISSA_ABOUT_LETTER
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val ELISSA_ID = 1912

private fun DialogueBuilder.elissaDialogWhatDoYouDoHere(player: Player, npc: NPC) {
    player(HeadE.CALM_TALK, "What do you do here?")
    npc(npc, HeadE.CALM_TALK, "I'm helping with the dig. I'm an expert on Third Age architecture.")
    goto("main")
}

private fun DialogueBuilder.elissaDialogWhatIsThisPlace(player: Player, npc: NPC) {
    player(HeadE.CONFUSED, "What is this place?")
    npc(npc, HeadE.HAPPY_TALKING, "In the Third Age, this was a great city, which would have put Varrock to shame!")
    options("Select an option") {
        op("I don't know, Varrock is pretty impressive.") {
            player(HeadE.SKEPTICAL, "I don't know, Varrock is pretty impressive.")
            npc(npc, HeadE.SKEPTICAL, "Hmph. I don't think it will look this good when it's been buried in the ground for three thousand years!")
            goto("main")
        }
        op("What happened to the city?") {
            player(HeadE.CONFUSED, "What happened to the city?")
            npc(npc, HeadE.CALM_TALK, "No one knows for sure. But the Third Age was a time of destruction, when the gods were violently at war. Many great civilizations were destroyed then.")
            goto("main")
        }
    }
}

private fun DialogueBuilder.elissaDialogNotes(player: Player, npc: NPC) {
    npc(npc, HeadE.CALM_TALK, "Hello there.")
    options("Select an option") {
        op("What do you do here?") {
            elissaDialogWhatDoYouDoHere(player, npc)
        }
        op("What is this place?") {
            elissaDialogWhatIsThisPlace(player, npc)
        }
        op("Where did you say the notes were?") {
            player(HeadE.CONFUSED, "Where did you say the notes were?")
            npc(npc, HeadE.CALM_TALK, "They're on a bookcase in the exam centre.")
        }
    }
}

private fun DialogueBuilder.elissaPostNotesDialog(player: Player, npc: NPC) {
    npc(npc, HeadE.CALM_TALK, "Hello there.")
    options("Select an option") {
        op("What do you do here?") {
            elissaDialogWhatDoYouDoHere(player, npc)
        }
        op("What is this place?") {
            elissaDialogWhatIsThisPlace(player, npc)
        }
        op("Where is the statuette that Varmen took back from Uzer?") {
            player(HeadE.CONFUSED, "Where is the statuette that Varmen took back from Uzer?")
            npc(npc, HeadE.CALM_TALK, "The statuette? Oh, yes... That statuette was the only thing we had to show from that expedition. It was very worn, but you can still make out a lot of detail.")
            npc(npc, HeadE.CALM_TALK, "The Uzerians were expert sculptors. It's a pity we only have that small example. Now it's on display in the Varrock museum.")
        }
    }
}

private fun DialogueBuilder.defaultElissaDialog(player: Player, npc: NPC) {
    npc(npc, HeadE.CALM_TALK, "Hello there.")
    label("main")
    options {
        op("What do you do here?") {
            elissaDialogWhatDoYouDoHere(player, npc)
        }
        op("What is this place?") {
            elissaDialogWhatIsThisPlace(player, npc)
        }
        if (player.getQuestStage(Quest.THE_GOLEM) == STAGE_TALK_TO_ELISSA_ABOUT_LETTER) {
            op("I found a letter in the desert with your name on it.") {
                player(HeadE.CALM_TALK, "I found a letter in the desert with your name on it.")
                npc(npc, HeadE.CALM_TALK, "Ah, so you've found the ruins of Uzer. I wrote that letter to my late husband when he was exploring there. That was a great city as well, but the museum could only fund one excavation and this one was closer to home.")
                npc(npc, HeadE.CALM_TALK, "If you're interested in his expedition, the notes he made are in the library in the Exam Centre.")
                exec { player.questManager.setStage(Quest.THE_GOLEM, STAGE_RETRIEVE_NOTES) }
            }
        }
    }
}


@ServerStartupEvent
fun mapElissa() {
    onNpcClick(ELISSA_ID) { (player, npc) ->
        player.startConversation {
            if(player.questManager.getStage(Quest.THE_GOLEM) != STAGE_RETRIEVE_NOTES) {
                defaultElissaDialog(player, npc)
            }
            else {
                if (player.questManager.getStage(Quest.THE_GOLEM) != STAGE_LOOT_MUSEUM) {
                    elissaDialogNotes(player, npc)
                } else {
                    elissaPostNotesDialog(player, npc)
                }
            }
        }
    }
}