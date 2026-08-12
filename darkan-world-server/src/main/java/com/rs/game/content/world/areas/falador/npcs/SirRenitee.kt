package com.rs.game.content.world.areas.falador.npcs
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.skills.construction.playerOwnedHouse.HouseHeraldry
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

enum class MapAndPortrait(val itemName: String, val itemID: Int, val price: Int, val requirements: String, val requirementCheck: (Player) -> Boolean) {
    SMALL_MAP("Small map", 8004, 1000, "51 Quest points", { player -> player.questManager.questPoints >= 51 }),
    MEDIUM_MAP("Medium map", 8005, 1000, "101 Quest points", { player -> player.questManager.questPoints >= 101 }),
    LARGE_MAP("Large map", 8006, 1000, "151 Quest points", { player -> player.questManager.questPoints >= 151 }),
    ELENA_PORTRAIT("Elena portrait", 7996, 1000, "Quest: Plague City", { player -> player.isQuestComplete(Quest.PLAGUE_CITY) }),
    ARTHUR_PORTRAIT("Arthur portrait", 7995, 1000, "Quest: Merlin's Crystal and Holy Grail", { player -> player.isQuestComplete(Quest.MERLINS_CRYSTAL) && player.isQuestComplete(Quest.HOLY_GRAIL) }),
    MISC_PORTRAIT("Misc. portrait", 7998, 1000, "Quest: Throne of Miscellania", { player -> player.isQuestComplete(Quest.THRONE_OF_MISCELLANIA) }),
    KELDAGRIM_PORTRAIT("Keldagrim portrait", 7997, 1000, "Quest: The Giant Dwarf", { player -> player.isQuestComplete(Quest.GIANT_DWARF) }),
    LUMBRIDGE_PAINTING("Lumbridge painting", 8002, 2000, "Quests: Cook's Assistant, Rune Mysteries, Sheep Shearer, The Restless Ghost", {
            player -> player.isQuestComplete(Quest.COOKS_ASSISTANT) && player.isQuestComplete(Quest.RUNE_MYSTERIES) && player.isQuestComplete(Quest.SHEEP_HERDER) && player.isQuestComplete(Quest.RESTLESS_GHOST)
    }),
    DESERT_PAINTING("Desert painting", 7999, 2000, "Quests: Prince Ali Rescue, Tourist Trap, The Feud, The Golem", {
            player -> player.isQuestComplete(Quest.PRINCE_ALI_RESCUE) && player.isQuestComplete(Quest.TOURIST_TRAP) && player.isQuestComplete(Quest.THE_FEUD) && player.isQuestComplete(Quest.THE_GOLEM)
    }),
    MORYTANIA_PAINTING("Morytania painting", 8003, 2000, "Quests: Ghosts Ahoy, Shades of Mort'ton, Creature of Fenkenstrain, Haunted Mine", {
            player -> player.isQuestComplete(Quest.GHOSTS_AHOY) && player.isQuestComplete(Quest.SHADES_OF_MORTTON) && player.isQuestComplete(Quest.CREATURE_OF_FENKENSTRAIN) && player.isQuestComplete(Quest.HAUNTED_MINE)
    }),
    KARAMJA_PAINTING("Karamja painting", 8001, 2000, "Quests: Pirate's Treasure, Shilo Village, Tai Bwo Wannai Trio", {
            player -> player.isQuestComplete(Quest.PIRATES_TREASURE) && player.isQuestComplete(Quest.SHILO_VILLAGE) && player.isQuestComplete(Quest.TAI_BWO_WANNAI_TRIO)
    }),
    ISAFDAR_PAINTING("Isafdar painting", 8000, 2000, "Quest: Roving Elves", { player -> player.isQuestComplete(Quest.ROVING_ELVES) });

}

//TODO Add option dialogues - POC
@ServerStartupEvent
fun mapSirRenitee() {
    val npcId = 4249 // Replace with the actual NPC ID

    onNpcClick(npcId, options = arrayOf("Talk-to")) { e ->
        val player = e.player
        player.startConversation {
            npc(npcId, HeadE.CALM_TALK, "Hmm? What's that, young ${player.genderTerm("man", "woman")}? What can I do for you?")
            options {
                ops("I don't know, what can you do for me?") {
                    player(HeadE.CALM_TALK, "I don't know, what can you do for me?")
                    npc(npcId, HeadE.CALM_TALK, "Hmm, well, mmm, do you have a family crest? I keep track of every Gielinor family, you know, so I might be able to find yours.")
                    npc(npcId, HeadE.CALM_TALK, "I'm also something of an, mmm, a painter. If you've met any important persons or visited any nice places I could paint them for you.")
                    label("InitialOps")
                    options {
                        ops("Can you see if I have a family crest?") {
                            player(HeadE.CALM_TALK, "Can you see if I have a family crest?")
                            npc(npcId, HeadE.CALM_TALK, "What is your name?")
                            player(HeadE.CALM_TALK, "${player.displayName}.")
                            npc(npcId, HeadE.CALM_TALK, "Mmm, ${player.displayName}, let me see...")
                            if (player.skills.getLevel(Skills.CONSTRUCTION) < 16) {
                                npc(npcId, HeadE.CALM_TALK, "First things first, young ${player.genderTerm("man", "woman")}! There is not much point in having a family crest if you cannot display it. You should train construction until you can build a wall decoration in your dining room.")
                                exec {
                                    goto("InitialOps")
                                }
                            }
                            if (HouseHeraldry.getCurrentCrest(player) == HouseHeraldry.VARROCK)
                                npc(
                                    npcId,
                                    HeadE.CALM_TALK,
                                    "Well, I don't think you have any noble blood, but I see that your ancestors came from Varrock, so you can use that city's crest."
                                )
                            else
                                npc(npcId, HeadE.CALM_TALK, "According to my records, your crest is the symbol of ${HouseHeraldry.getCurrentCrest(player).ccname}.")
                            options {
                                ops("I don't like that crest. Can I have a different one?") {
                                    npc(npcId, HeadE.CALM_TALK, "Mmm, very well. Changing your crest will cost 5,000 coins.")
                                    if (!player.inventory.hasCoins(5000)) {
                                        player(HeadE.CALM_TALK, "I'll have to go get some.")
                                        return@ops
                                    }
                                    npc(npcId, HeadE.CALM_TALK, "There are sixteen different symbols; which one would you like?")
                                    val availableCrests = HouseHeraldry.entries.filter { it.requirementCheck(player) }
                                    if (availableCrests.isEmpty()) {
                                        npc(npcId, HeadE.CALM_TALK,"It seems you do not meet the requirements for any crests.")
                                        return@ops
                                    }
                                    options {
                                        availableCrests.forEach { crest ->
                                            ops(crest.description) {
                                                player(HeadE.CALM_TALK, "I would like the ${crest.ccname} crest.")
                                                npc(npcId, HeadE.CALM_TALK, "The ${crest.description}? Excellent choice!")
                                                exec {
                                                    HouseHeraldry.setCurrentCrest(player, crest)
                                                    player.inventory.removeCoins(crest.cost)
                                                }
                                            }
                                        }
                                        ops("Never mind") {
                                            player(HeadE.CALM_TALK, "Never mind.")
                                            npc(npcId, HeadE.CALM_TALK, "Come back if you change your mind!")
                                        }
                                    }
                                }
                                ops("Thanks!") {
                                    player(HeadE.CALM_TALK, "Thanks!")
                                    npc(npcId, HeadE.CALM_TALK, "You're welcome, my ${player.genderTerm("boy", "girl")}.")
                                }
                            }
                        }
                        ops("I'd like to buy a portrait.") {
                            player(HeadE.CALM_TALK, "A portrait please.")
                            npc(npcId, HeadE.CALM_TALK, "Mmm, well, there are a few portraits I can paint. I can only let you have one if you've got some connection with that person though. Who would you like?")

                            val availablePortraits = MapAndPortrait.entries
                                .filter { it.name.contains("PORTRAIT", ignoreCase = true) && it.requirementCheck(player) }

                            if (availablePortraits.isEmpty()) {
                                npc(npcId, HeadE.CALM_TALK, "I'm sorry, you don't qualify for any of the portraits I can offer.")
                            } else {
                                exec {
                                    showDynamicOptions(player, npcId, availablePortraits)
                                }
                            }
                        }

                        ops("I'd like to buy a landscape.") {
                            player(HeadE.CALM_TALK, "A landscape please.")
                            npc(npcId, HeadE.CALM_TALK, "Mmm, well, I can paint a few places. Where have you had your adventures?")
                            val availableLandscapes = MapAndPortrait.entries
                                .filter { it.name.contains("PAINTING", ignoreCase = true) && it.requirementCheck(player) }

                            if (availableLandscapes.isEmpty()) {
                                npc(npcId, HeadE.CALM_TALK, "It seems you haven't explored any of the locations I can paint.")
                            } else {
                                exec {
                                    showDynamicOptions(player, npcId, availableLandscapes)
                                }
                            }
                        }

                        ops("I'd like to buy a map.") {
                            player(HeadE.CALM_TALK, "A map please.")
                            npc(npcId, HeadE.CALM_TALK, "Mmm, yes, ah, I have painted maps of the known world on several different sizes of parchment. Which size would you like?")

                            val availableMaps = MapAndPortrait.entries
                                .filter { it.name.contains("MAP", ignoreCase = true) && it.requirementCheck(player) }

                            if (availableMaps.isEmpty()) {
                                npc(npcId, HeadE.CALM_TALK, "I'm sorry, you don't qualify for any of my maps.")
                            } else {
                                exec {
                                    showDynamicOptions(player, npcId, availableMaps)
                                }
                            }
                        }
                        ops("Never mind") {
                            player(HeadE.CALM_TALK, "Never mind.")
                            npc(npcId, HeadE.CALM_TALK, "Come back if you change your mind!")
                        }
                    }
                }
            }
        }
    }
}

fun showDynamicOptions(player: Player, npcId: Int, items: List<MapAndPortrait>) {
    player.startConversation {
        options {
            items.forEach { item ->
                ops("${item.itemName} - ${item.price} coins") {
                    if (!player.inventory.hasCoins(item.price)) {
                        player(HeadE.CALM_TALK, "I don't have enough money for that.")
                    } else {
                        player(HeadE.HAPPY_TALKING, "I'll take the ${item.itemName}.")
                        npc(npcId, HeadE.HAPPY_TALKING, "Here you go, enjoy your ${item.itemName}!")
                        exec {
                            player.inventory.addItem(item.itemID, 1)
                            player.inventory.removeCoins(item.price)
                        }
                    }
                }
            }
            ops("Never mind") {
                player(HeadE.CALM_TALK, "Never mind.")
                npc(npcId, HeadE.CALM_TALK, "Come back if you change your mind!")
            }
        }
    }
}

