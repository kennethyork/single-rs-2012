package com.rs.game.content.quests.princealirescue

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.game.content.quests.princealirescue.PrinceAliRescue.*

private const val HASSAN = 923

@ServerStartupEvent
fun mapHassanPrinceAliRescue() {
    onNpcClick(HASSAN, options = arrayOf("Talk-to")) { (player, npc) ->
        player.hassanDialogue(npc)
    }
}

fun Player.hassanDialogue(npc: NPC) {
    startConversation {
        npc(npc, HeadE.HAPPY_TALKING, "Greetings I am Hassan, Chancellor to the Emir of Al- Kharid.")
        options("Choose an option:") {
            if (questManager.getStage(Quest.PRINCE_ALI_RESCUE) == PrinceAliRescue.NOT_STARTED) {
                op("Can I help you? You must need some help here in the desert.") {
                    npc(npc, HeadE.TALKING_ALOT, "I need the services of someone, yes. If you are interested, see the spymaster, Osman. I manage the finances here. Come to me when you need payment.")
                    options("Start Prince Ali To The Rescue?") {
                        op("Yes.") {
                            npc(npc, HeadE.HAPPY_TALKING, "Speak to Osman outside the palace, he will give you more details...") {
                                questManager.setStage(Quest.PRINCE_ALI_RESCUE, PrinceAliRescue.STARTED)
                            }
                            player(HeadE.HAPPY_TALKING, "Okay...")
                        }
                        op("No.")
                    }
                }
            }
            op("It's just too hot here. How can you stand it?") {
                npc(npc, HeadE.HAPPY_TALKING, "We manage, in our humble way. We are a wealthy town and we have water. It cures many thirsts.")
            }
            op("Do you mind if I just kill your warriors?") {
                npc(npc, HeadE.HAPPY_TALKING, "You are welcome. They are not expensive. We have them here to stop the elite guard being bothered. They are a little harder to kill.")
            }
        }
    }
}
