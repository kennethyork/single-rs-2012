package com.rs.game.content.world.areas.ranging_guild.npc

import com.rs.engine.dialogue.HeadE.CALM_TALK
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.Skillcapes
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun mapAaronsArcheryAppendagesDialogue() {
    onNpcClick(682, options = arrayOf("Talk-to")) { e ->
        e.npc.resetDirection()
        e.player.startConversation {
            player(CALM_TALK, "Good day to you.")
            npc(e.npcId, CALM_TALK, "And to you. Can I help you?")
            options {
                op("What do you do here?") {
                    player(CALM_TALK, "What do you do here?")
                    npc(e.npcId, CALM_TALK, "I am a supplier of leather armours and accessories. Ask and I will tell you what I know.")
                    options {
                        op("Tell me about your armours.") {
                            player(CALM_TALK, "Tell me about your armours.")
                            npc(e.npcId, CALM_TALK, "I have normal, studded and hard types.")
                            options {
                                op("Tell me about normal leather.") {
                                    player(CALM_TALK, "Tell me about normal leather.")
                                    npc(e.npcId, CALM_TALK, "Indeed, leather armour is excellent for archers. It's supple and not very heavy.")
                                }
                                op("What's studded leather?") {
                                    player(CALM_TALK, "What's studded leather?")
                                    npc(e.npcId, CALM_TALK, "Ah, now that's leather covered with studs. It's more protective than ordinary leather.")
                                }
                                op("What's hard leather?") {
                                    player(CALM_TALK, "What's hard leather?")
                                    npc(e.npcId, CALM_TALK, "Hard leather is specially treated using oils and drying methods to create a hard-wearing armour.")
                                }
                                op("Enough about armour.") {
                                    player(CALM_TALK, "Enough about armour.")
                                    npc(e.npcId, CALM_TALK, "As you wish, traveller.")
                                }
                            }
                        }

                        op("Tell me about your accessories.") {
                            player(CALM_TALK, "Tell me about your accessories.")
                            npc(e.npcId, CALM_TALK, "Ah yes we have a new range of accessories in stock. Essential items for an archer like you.")
                            npc(e.npcId, CALM_TALK, "We have vambraces, chaps, cowls and coifs.")
                            options {
                                op("Tell me about vambraces.") {
                                    player(CALM_TALK, "Tell me about vambraces.")
                                    npc(e.npcId, CALM_TALK, "Ah yes, vambraces. These useful items are for your arms.")
                                    npc(e.npcId, CALM_TALK, "A protective sheath that favours the bow and arrow. An essential purchase.")
                                }
                                op("Tell me about chaps.") {
                                    player(CALM_TALK, "Tell me about chaps.")
                                    npc(e.npcId, CALM_TALK, "Chaps have two functions: firstly to protect your legs, and secondly for ease of reloading arrows.")
                                    npc(e.npcId, CALM_TALK, "I can highly recommend these to you for quick archery.")
                                }
                                op("Tell me about cowls.") {
                                    player(CALM_TALK, "Tell me about cowls.")
                                    npc(e.npcId, CALM_TALK, "The cowl is a soft leather hat, ideal for protection with maneuverability.")
                                    npc(e.npcId, CALM_TALK, "These are highly favoured with our guards.")
                                }
                                op("Tell me about coifs.") {
                                    player(CALM_TALK, "Tell me about coifs.")
                                    npc(e.npcId, CALM_TALK, "The coif is a specialized cowl, that has extra chain protection to keep your neck and shoulders safe.")
                                    npc(e.npcId, CALM_TALK, "An excellent addition to our range, traveller.")
                                }
                                op("Enough about accessories.") {
                                    player(CALM_TALK, "Enough about accessories.")
                                    npc(e.npcId, CALM_TALK, "As you wish.")
                                }
                            }
                        }

                        op("I've seen enough, thanks.") {
                            player(CALM_TALK, "I've seen enough, thanks.")
                            npc(e.npcId, CALM_TALK, "Very good, adventurer.")
                        }
                    }
                }

                op("I'd like to see what you sell.") {
                    player(CALM_TALK, "I'd like to see what you sell.")
                    npc(e.npcId, CALM_TALK, "Indeed, cast your eyes on my wares, adventurer.")
                    exec { ShopsHandler.openShop(e.player, "aarons_archery_appendages") }
                }

                op("Can you tell me about your cape?") {
                            exec { Skillcapes.valueOf("Ranging").getOffer99CapeDialogue(e.player, e.npcId) }
                }

                op("What is that cape you're wearing?") {
                    exec { Skillcapes.valueOf("Ranging").getOffer99CapeDialogue(e.player, e.npcId) }
                }

                op("I've seen enough, thanks.") {
                    player(CALM_TALK, "I've seen enough, thanks.")
                    npc(e.npcId, CALM_TALK, "Very good, adventurer.")
                }
            }
        }
    }
    onNpcClick(682, options = arrayOf("Trade")) { (player, npc) ->
        ShopsHandler.openShop(player, "aarons_archery_appendages")
    }
}