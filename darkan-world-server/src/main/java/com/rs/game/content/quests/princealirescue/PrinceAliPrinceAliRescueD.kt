package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.content.quests.princealirescue.*


private const val PRINCE_ALI = 920
private const val PRINCE_ALI2 = 921

@ServerStartupEvent
fun mapPrinceAliPrinceAliRescue() {
    onNpcClick(PRINCE_ALI, options = arrayOf("Talk-to")) { (player, npc) ->
        player.princeAliDialogue(npc)
    }
}

fun Player.princeAliDialogue(npc: NPC) {
    startConversation {
        when {
            isQuestComplete(Quest.PRINCE_ALI_RESCUE) -> {
                npc(npc, HeadE.CALM_TALK, "I owe you my life for that escape. You cannot help me this time, they know who you are. Go in peace, friend of Al-Kharid.")
            }

            inventory.containsItem(PrinceAliRescue.PASTE, 1)
                    && inventory.containsItem(PrinceAliRescue.BLONDE_WIG, 1)
                    && inventory.containsItem(PrinceAliRescue.PINK_SKIRT, 1) -> {
                player(HeadE.SECRETIVE, "Prince, I come to rescue you.")
                npc(npc, HeadE.HAPPY_TALKING, "That is very very kind of you, how do I get out?")
                player(HeadE.SECRETIVE, "With a disguise. I have removed the Lady Keli. She is tied up, but will not stay tied up for long.")
                player(HeadE.SECRETIVE, "Take this disguise, and this key.")
                simple("You hand over the disguise and key over to Prince Ali.") {
                    inventory.removeItems(
                        Item(PrinceAliRescue.PASTE, 1),
                        Item(PrinceAliRescue.PINK_SKIRT, 1),
                        Item(PrinceAliRescue.BLONDE_WIG, 1)
                    )
                    //TODO update this to new logic for NPC transformation
                }
                npc(PRINCE_ALI2, HeadE.HAPPY_TALKING, "Thank you my friend, I must leave you now. My father will pay you well for this.")
                simple("The prince has escaped, well done! You are now a friend of Al-Kharid and may pass through the Al-Kharid toll gate for free.") {
                    questManager.completeQuest(Quest.PRINCE_ALI_RESCUE)
                }
            }

            else -> {
                npc(npc, HeadE.CALM_TALK, "Hello.")
                player(HeadE.SAD, "Darn I don't have all I need for the disguise")
            }
        }
    }
}
