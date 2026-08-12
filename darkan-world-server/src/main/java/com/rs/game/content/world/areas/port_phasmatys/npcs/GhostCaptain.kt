package com.rs.game.content.world.areas.port_phasmatys.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.world.areas.port_phasmatys.PortPhasmatys
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
@ServerStartupEvent
fun mapGhostShipCaptain() {
    onNpcClick(1704, options = arrayOf("Talk-To")) { (player, npc) ->
        if (!PortPhasmatys.hasGhostSpeak(player))
            PortPhasmatys.GhostSpeakResponse(player, npc)
        else {
            if (npc.tile == Tile.of(3703, 3487, 0) && !player.isQuestComplete(Quest.GHOSTS_AHOY)) {
                player.startConversation {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Would you like to visit Dragontooth Island? It is a most pleasant island in the sea between Morytania and the lands of the east. Just 25 Ectotokens for a round-trip."
                    )
                    options {
                        if(player.inventory.getTotalNumberOf(4278) >= 25)
                            op("Yes please.") {
                                exec {
                                    player.fadeScreen {
                                        player.tele(Tile.of(3791, 3560, 0))
                                        player.inventory.deleteItem(4278, 25)
                                    }
                                }
                            }
                        else
                            op("Yes, please.") {
                                npc(npc, HeadE.SHAKING_HEAD, "You don't have enough Ectotokens. No Ectotokens, no trip.")
                            }
                        op("No thanks.") {
                            player(HeadE.CALM_TALK, "No thanks.")
                        }
                    }
                }
            }
            else if (npc.tile == Tile.of(3703, 3487, 0)) {
                player.startConversation {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Would you like to visit Dragontooth Island? It is a most pleasant island in the sea between Morytania and the lands of the east."
                    )
                    options {
                        op("Yes please.") {
                            exec {
                                player.fadeScreen {
                                    player.tele(Tile.of(3791, 3560, 0))
                                }
                            }
                        }
                        op("No thanks.") {
                            player(HeadE.CALM_TALK, "No thanks.")
                        }
                    }
                }
            }
            else {
                player.startConversation {
                    options {
                        op("Can you take me back to Phasmatys?") {
                            player(HeadE.CALM_TALK, "Can you take me back to Phasmatys, now?")
                            npc(npc, HeadE.CALM_TALK, "Yes, climb in.")
                            exec {
                                player.fadeScreen {
                                    player.tele(Tile.of(3702, 3487, 0))
                                }
                            }
                        }
                        op("No thanks.") {
                            player(HeadE.CALM_TALK, "No thanks.")
                        }
                    }
                }
            }
        }
    }
    onNpcClick(1704, options = arrayOf("Travel")) { (player, npc) ->
        if (!PortPhasmatys.hasGhostSpeak(player))
            PortPhasmatys.GhostSpeakResponse(player, npc)
        else {
            if (npc.tile == Tile.of(3703, 3487, 0) && !player.isQuestComplete(Quest.GHOSTS_AHOY)) {
                player.startConversation {
                    npc(
                        npc,
                        HeadE.CALM_TALK,
                        "Would you like to visit Dragontooth Island? It is a most pleasant island in the sea between Morytania and the lands of the east. Just 25 Ectotokens for a round-trip."
                    )
                    options {
                        if(player.inventory.getTotalNumberOf(4278) >= 25)
                            op("Yes please.") {
                                exec {
                                    player.fadeScreen {
                                        player.tele(Tile.of(3791, 3560, 0))
                                        player.inventory.deleteItem(4278, 25)
                                    }
                                }
                            }
                        else
                            op("Yes, please.") {
                                npc(npc, HeadE.SHAKING_HEAD, "You don't have enough Ectotokens. No Ectotokens, no trip.")
                            }
                        op("No thanks.") {
                            player(HeadE.CALM_TALK, "No thanks.")
                        }
                    }
                }
            }
            else if (npc.tile == Tile.of(3703, 3487, 0)) {
                player.fadeScreen {
                    player.tele(Tile.of(3791, 3560, 0))
                }
            }
            else {
                player.fadeScreen {
                    player.tele(Tile.of(3702, 3487, 0))
                }
            }
        }
    }
}