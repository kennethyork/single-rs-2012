package com.rs.game.content.world.areas.port_phasmatys.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

private const val BEDSHEET = 4284
private const val ECTOBEDSHEET = 4285
private const val PETITION = 4283
private const val ECTOTOKENS = 4278

@ServerStartupEvent
fun mapGhostInnkeeper() {
    onNpcClick(1700) { (player, npc) ->
        if (!PortPhasmatys.hasGhostSpeak(player))
            PortPhasmatys.GhostSpeakResponse(player, npc)
        else
            ghostInnkeeperPreQuest(player, npc)
    }
}

fun ghostInnkeeperPreQuest(player: Player, npc: NPC) {
    player.startConversation {
        player(HeadE.CALM_TALK, "Hello there!")
        npc(npc, HeadE.CALM_TALK, "Greetings, traveller. Welcome to 'The Green Ghost' Tavern. What can I do you for?")
        options {
            op("Can I buy a beer?") {
                player(HeadE.CALM_TALK, "Can I buy a beer?")
                npc(npc, HeadE.CALM_TALK, "Sorry, but our pumps dried up over half a century ago. We of this village do not have much of a thirst these days.")
            }
            op("Can I hear some gossip?") {
                player(
                    HeadE.CALM_TALK,
                    "Can I hear some gossip?"
                )
                npc(npc, HeadE.CALM_TALK, "I suppose, as long as you keep it to yourself...")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "You see Gravingas out there in the marketplace? He speaks for the silent majority of Port Phasmatys, for those of us who would prefer to pass over into the next world."
                )
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "But old Gravingas is far too obvious in his methods.")
                npc(npc, HeadE.CALM_TALK,"Now Velorina, she's a ghost of a different colour altogether. If you feel like helping our cause at all, go speak to Velorina.")

            }
            op("What happened to the folk of this town ?") {
                player(HeadE.CALM_TALK, "What happened to the folk of this town?")
                npc(npc, HeadE.CALM_TALK, "That 's a long story, my friend, but you look like you have the time...")
                npc(npc, HeadE.CALM_TALK, "Do you know why ghosts exist?")
                player(
                    HeadE.CALM_TALK,
                    "A ghost is a soul left in limbo, unable to pass over to the next world; they might have something left to do in this world that torments them, or the might just have died in a state of confusion."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Yes, that is normally the case. But here in Port Phasmatys, we of this town once chose freely to become ghosts!"
                )

                player(HeadE.SCARED, "Why on earth would you do such a thing?!")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "It is a long story. Many years ago, this was a thriving port, a trading centre to the eastern lands. We became rich on the profits made from the traders that came from across the eastern seas."
                )

                npc(npc, HeadE.CALM_TALK, "We were very happy...<br>Until Lord Drakan noticed us.")
                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "He sent unholy creatures to demand that a blood-tithe be paid to the Lord Vampyre, is is required from all in the domain of Mortyania. We had no choice but to agree to his demands."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "As the years went by, our numbers dwindled and many spoke of abandoning the town for safer lands. Then Necrovarus came to us."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "He came from the eastern lands, but of more than that, little is known. Some say he was once a mage, some say a priest. Either way, he was in possession of knowledge totally unknown to even the most learned among us."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Necrovarus told us that he had been brought by a vision he'd had of an underground source of power. He inspired us to delve beneath the town, promising us the power to repel the vampyres."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Deep underneath Phasmatys, we found a pool of green slime that Necrovarus called Ectoplasm. He showed us how to build the Ectofuntus, which would turn the ectoplasm into the power he had promised us."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Indeed, this Ectopower did repel the Vampyres; the would not enter Phasmatys once the Ectofuntus began working. But little did we know that we had exchanged one evil for yet another - Ectopower."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "Little by little, we began to lose any desire for food or water, and our desire for the Ectopower grew until it dominated our thoughts entirely. Our bodies shrivelled and, one by one, we died."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "The Ectofuntus and the power it emanates keeps us here as ghosts - some, like myself, content to remain in this world; some becoming tortured souls who we do not allow to pass our gates."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "We would be able to pass over into the next world but Necrovarus has used his power to create a psychic barrier, preventing us."
                )

                npc(
                    npc,
                    HeadE.CALM_TALK,
                    "We must remain her for all eternity, even unto the very end of the world."
                )
            }

            op("Do you have any jobs I can do?") {
                player(HeadE.CALM_TALK, "Do you have any jobs I can do?")
                if(!player.inventory.containsItems(BEDSHEET)) {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Yes, actually, I do. We have a very famous Master Bowman named Robin staying with us at the moment. Could you take him some clean bed linen for me?"
                    )

                    options {

                        op("Yes, I'd be delighted.") {
                            player(
                                HeadE.CALM_TALK,
                                "Yes, I'd be delighted."
                            )
                            npc(npc, HeadE.CALM_TALK, "Oh, thank you. Be careful with that Robin, though - he's far too full of himself, that one.")
                            exec{ (player.inventory.addItem(BEDSHEET)) }
                        }
                        op("No, I didn't mean a job like that.") {
                            player(
                                HeadE.CALM_TALK,
                                "No, I didn't mean a job like that."
                            )
                        }
                    }
                }
                else {
                    npc(npc, HeadE.CALM_TALK, "Well, you could take that bedsheet through to Robin like I asked.")
                    return@op
                }
            }
            op("Nothing, thanks.") {
                player(
                    HeadE.CALM_TALK,
                    "Nothing, thanks."
                )
            }
        }
    }
}