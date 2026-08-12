package com.rs.game.content.minigames.herblorehabitat.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.getAllActiveJadinkoTypes
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import java.util.*

private const val Astlayrix = 13104

@ServerStartupEvent
fun handleAstlayrixOptions() {
    onNpcClick(Astlayrix, options = arrayOf("Talk-to")) { (player, npc) ->
        astlayrixDialogue(player, npc)
    }
    onNpcClick(Astlayrix, options = arrayOf("Jadinko-requirements")) { (player, _) ->
        player.interfaceManager.sendInterface(72)
    }
}

fun astlayrixDialogue(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, HeadE.CALM, "Hello! Isn't this place exciting? So many different jadinkos, so many factors that influence their life here. Truly, Guthix moves in mysterious ways.")
        options("Select an option") {
            op("Who are you?") {
                player(HeadE.CALM, "Who are you?")
                npc(npc, HeadE.CALM, "My name is Astlayrix. I'm originally from Taverley, but I simply had to come and see this astounding habitat in person.")
                exec { whoAreYou(p, npc) }
            }
            op("What influences the jadinkos?") {
                player(HeadE.CALM_TALK, "What influences the jadinkos?")
                exec { influenceJadinkos(p, npc) }
            }
            op("Can you show me which creatures I've attracted so far?") {
                exec { knownJadinkos(p, npc) }
            }
            op("Can you tell me which creatures I'm currently attracting?") {
                val activeTypes = p.getAllActiveJadinkoTypes()
                val message = if (activeTypes.isEmpty()) {
                    "You aren't attracting any specific Jadinkos at the moment."
                } else {
                    val names = activeTypes.joinToString(separator = ", ") { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
                    "You are currently attracting: $names Jadinkos."
                }
                npc(npc, HeadE.HAPPY_TALKING, message)
            }
        }
    }
}

private fun whoAreYou(p: Player, npc: NPC) {
    p.startConversation {
        options("Select an option") {
            op("What's so special about it?") {
                player(HeadE.CALM_TALK, "What's so special about it?")
                npc(npc, HeadE.CALM_TALK, "Everything! Well, okay, maybe not everything, but a decent proportion. For a start, did you know that you can grow combinations of plants to attract different jadinkos?")

                exec { aboutJadinkoOrGrow(p, npc) }
            }
            op("What can I grow?") {
                exec { grow(p, npc) }
            }
            op("What influences the jadinkos?") {
                exec { influenceJadinkos(p, npc) }
            }
            op("Can you show me which creatures I've attracted so far?") {
                exec { knownJadinkos(p, npc) }
            }
        }
    }
}

private fun aboutJadinkoOrGrow(p: Player, npc: NPC) {
    p.startConversation {
        options("Select an option") {
            op("What's a jadinko?") {
                player(HeadE.CALM_TALK, "What's a jadinko?")
                npc(npc, HeadE.CALM_TALK, "It's a curious name, I agree. Papa Mambo apparently named them. A jadinko is a strange lizard-like creature.")
                        npc(npc, HeadE.CALM_TALK,  "They're evidently some kind of walking plant: fascinating, really. What's more, they have vines growing from them, each infused with remarkable magical energies.")
                                npc(npc, HeadE.CALM_TALK,   "Imagine the potions you could create using those vines as ingredients.")
                exec { afterAboutJadinko(p, npc) }
            }
            op("What can I grow?") {
                exec { grow(p, npc) }
            }
            op("What influences the jadinkos?") {
                exec { influenceJadinkos(p, npc) }
            }
            op("Can you show me which creatures I've attracted so far?") {
                exec { knownJadinkos(p, npc) }
            }
        }
    }
}

private fun afterAboutJadinko(p: Player, npc: NPC) {
    p.startConversation {
        options("Select an option") {
            op("What potions do you think I could create?") {
                player(HeadE.CALM_TALK, "What potions do you think I could create?")
                npc(npc, HeadE.CALM_TALK, "Hmm...I'm afraid you have me a little stumped there, friend, but I'm willing to bet that the potions have unique effects.")
                npc(npc, HeadE.CALM_TALK, "Why, there are even new herbs that can be grown here. I'm guessing that, combined with some of those magical vines, you can get a variety of effects.")
                exec { grow(p, npc) }
            }
            op("What influences the jadinkos?") {
                exec { influenceJadinkos(p, npc) }
            }
            op("Can you show me which creatures I've attracted so far?") {
                exec { knownJadinkos(p, npc) }
            }
        }
    }
}

private fun grow(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "What can I grow?")
        npc(npc, HeadE.CALM_TALK, "Well, there are new species of plant to grow here. Each one must be grown from the vine itself.")
                npc(npc, HeadE.CALM_TALK, "There are two new species of bush, three different flowers and five herbs—all can be grown from the vine itself.")
                        npc(npc, HeadE.CALM_TALK, "There's also a fruit tree patch, but there's nothing new about that.")
        options("Select an option") {
            op("What herbs can I grow here?") {
                exec { herbs(p, npc) }
            }
            op("What's a jadinko?") {
                exec { whoAreYou(p, npc) }
            }
            op("What influences the jadinkos?") {
                exec { influenceJadinkos(p, npc) }
            }
            op("Can you show me which creatures I've attracted so far?") {
                exec { knownJadinkos(p, npc) }
            }
        }
    }
}

private fun herbs(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "What herbs can I grow here?")
        npc(npc, HeadE.CALM_TALK, "Well, certainly not the herbs you'll be used to growing!")
                npc(npc, HeadE.CALM_TALK, "Everything you can grow here, with the exception of certain fruit trees, is grown from the vine itself.")
                        npc(npc, HeadE.CALM_TALK,   "So, you can only grow special vine herbs here.")
        options("Select an option") {
            op("Herself?") {
                player(HeadE.CALM_TALK, "Herself?")
                npc(npc, HeadE.CALM_TALK, "Grimy toadflax! He's got me thinking the vine is a female now! It! Clearly, I meant to refer to the vine as 'itself', rather than 'herself'.")
                options("Select an option") {
                    op("Who's got you thinking the vine is a female?") {
                        player(HeadE.CALM_TALK, "Who's got you thinking the vine is a female?")
                        npc(npc, HeadE.CALM_TALK, "Oh, that creepy shaman chap. Calls himself 'Papa Mambo', or something. Quite an unnerving fellow.")
                        npc(3122, HeadE.CALM_TALK, "Heh heh heh. I's can hear you talking 'bout me. Heh Heh heh.")
                        npc(npc, HeadE.CALM_TALK, "Sheesh! It really unnerves me when he does that.")
                        npc(npc, HeadE.CALM_TALK, "That shaman is responsible for encouraging this vine to become such a miraculous habitat.")
                        npc(npc, HeadE.CALM_TALK, "I've absolutely no idea how he did it, but I suspect he has no idea either.")
                        exec { grow(p, npc) }
                    }
                    op("Nevermind") {
                        exec { nevermind(p, npc) }
                    }
                }
            }
            op("Nevermind") {
                exec { nevermind(p, npc) }
            }
        }
    }
}

private fun influenceJadinkos(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "What influences the jadinkos?")
        npc(npc, HeadE.CALM_TALK, "Well, not all creatures are attracted by the same things; some species of jadinko can never be in the habitat at the same time.")
                npc(npc, HeadE.CALM_TALK, "If you look around you'll notice vine patches and a construction area. Grow the right plants and build the right feature to attract a jadinko.")
        options("Select an option") {
            op("What's the point of attracting different jadinkos?") {
                player(HeadE.CALM_TALK, "What's the point of attracting different jadinkos?")
                npc(npc, HeadE.CALM_TALK, "Each jadinko has a different vine crest, which you can glean if you successfully hunt them. These vines each have different magical properties for potions. Each week Papa Mambo offers a reward for catching every type.")
                exec { astlayrixDialogue(p, npc) }
            }
            op("Nevermind") {
                exec { nevermind(p, npc) }
            }
        }
    }
}

private fun knownJadinkos(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Can you show me which creatures I've attracted so far?")
        npc(npc, HeadE.CALM_TALK, "Of course.")
        exec { p.interfaceManager.sendInterface(72) }
    }
}

private fun nevermind(p: Player, npc: NPC) {
    p.startConversation {
        player(HeadE.CALM_TALK, "Nevermind.")
    }
}

