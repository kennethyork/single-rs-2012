package com.rs.game.content.minigames.herblorehabitat.npcs

import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.game.World
import com.rs.game.content.Effect
import com.rs.game.content.minigames.herblorehabitat.HabitatFeature
import com.rs.game.content.minigames.herblorehabitat.JadinkoType
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.CAUGHT_JADINKO_ATTR
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.godJadinkos
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.isGodJadinko
import com.rs.game.content.minigames.herblorehabitat.JadinkoType.Companion.populateHerbloreHabitatDatabase
import com.rs.game.content.minigames.herblorehabitat.habitatFeature
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.Ticks
import com.rs.utils.shop.ShopsHandler


private const val PAPA_MAMBO = 3122

@ServerStartupEvent
fun handlePapaMamboOptions() {
    onNpcClick(PAPA_MAMBO, options = arrayOf("Talk-to")) { (player, npc) ->
        papaMamboDialogue(player, npc)
    }
    onNpcClick(PAPA_MAMBO, options = arrayOf("Trade")) { (player, _) ->
        ShopsHandler.openShop(player, "papa_mambos_shop")
    }
    onNpcClick(PAPA_MAMBO, options = arrayOf("Build")) { (player, npc) ->
        player.openHabitatBuildDialogue(player, npc)
    }
    onNpcClick(PAPA_MAMBO, options = arrayOf()) { (player, npc) ->
        wardOff(player, npc)
    }
}


fun papaMamboDistractionComplete(p: Player) {
    p.startConversation {
        npc(PAPA_MAMBO, HAPPY_TALKING, "Heh heh heh, well done! You have caught all of the normal jadinkos this week. Come talk to me for your reward.")
    }
}

fun papaMamboGodDistractionComplete(p: Player) {
    p.startConversation {
        npc(PAPA_MAMBO, HAPPY_TALKING, "Heh heh heh. I's impressed. You have caught not only the normal jadinko, but the powerful ones as well. I's got an extra special reward for that feat.")
    }
}

fun papaMamboProgress(p: Player, npc: NPC) {
    p.populateHerbloreHabitatDatabase()
    val caughtTypes = JadinkoType.entries.filter {
        p.weeklyAttributes["HabitatCaptured_" + it.name] as Boolean
    }
    val message = if (caughtTypes.isEmpty()) {
        "You haven't caught any Jadinko's so far this week."
    } else {
        val names = caughtTypes.joinToString(", ") {
            it.name.lowercase().replaceFirstChar(Char::uppercaseChar)
        }
        "Heh, heh, heh. You have caught $names Jadinko's so far this week I's impressed."
    }
    p.startConversation {
        npc(PAPA_MAMBO, HAPPY_TALKING, message)
        val caughtSet = JadinkoType.entries.filter {
            p.weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
        }.toSet()
        val allNonGod = JadinkoType.entries.filterNot { it.isGodJadinko() }
        if (allNonGod.all { it in caughtSet } && p.weeklyAttributes["HabitatRewardClaimed"] == false) claimNormalReward(p, npc)
        if (JadinkoType.entries.all { it in caughtSet } && p.weeklyAttributes["HabitatGodRewardClaimed"] == false) claimGodReward(p, npc)
    }
}

private fun wardOff(player: Player, npc: NPC) {
    if (!player.hasEffect(Effect.MAMBO_WARDING) || player.savingAttributes["MAMBO_WARDING_TYPE"] == null) {
        player.removeEffects(Effect.MAMBO_WARDING)
        player.startConversation {
            npc(npc, HAPPY_TALKING, "Which jadinko would you like me to ward off?")
            options {
                JadinkoType.entries.forEach { type ->
                    if (type in godJadinkos) return@forEach
                    op(type.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() }) {
                        exec {
                            player.sendInputInteger(
                                "How many striped vines will you give me?<br>" +
                                        "(Each wards off ${type.name.replace('_',' ')
                                            .lowercase().replaceFirstChar { it.uppercase() }} jadinkos for 15 minutes.)"
                            ) { num ->
                                val spend = num.coerceIn(0, player.inventory.getNumberOf(19978))
                                player.sendOptionDialogue { opts ->
                                    opts.add("Spend $spend striped vines for ${spend*15} minutes warding") {
                                        if (player.inventory.containsItem(19978, spend)) {
                                            player.inventory.deleteItem(19978, spend)
                                            player.savingAttributes["MAMBO_WARDING_TYPE"] = type.name
                                            player.addEffect(Effect.MAMBO_WARDING, Ticks.fromMinutes((15 * spend)).toLong())
                                            player.sendMessage(
                                                "<col=00FF00>Papa Mambo wards off ${type.name
                                                    .replace('_',' ')
                                                    .lowercase()
                                                    .replaceFirstChar { it.uppercase() }} jadinkos for ${spend*15} minutes!"
                                            )
                                        } else {
                                            player.sendMessage("You don’t have $spend striped vines.")
                                        }
                                    }
                                    opts.add("Never mind") {
                                        player.sendMessage("Maybe another time.")
                                    }
                                }
                            }
                        }
                    }
                }
                op("Never mind") {
                    player.sendMessage("Alright, I won’t ward off anything.")
                }
            }
        }
    }
    else {
        val warded = player.savingAttributes["MAMBO_WARDING_TYPE"] as? JadinkoType
        if (warded != null) {
            player.sendMessage(
                "Papa Mambo is already warding off " +
                        warded.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() } +
                        " jadinkos!"
            )
        }
    }
}

private fun claimNormalReward(p: Player, npc: NPC) {
    if (p.inventory.freeSlots < 2 ) {
        p.startConversation {
            npc(npc, CALM_TALK, "Ah, you not have room for my gifts. Come back when you have space in your inventory.")
            return@startConversation
        }
    }
    p.startConversation {
        npc(npc, HAPPY_TALKING, "Heh heh heh. You have hunted at least one of each creature that comes 'ere. I am impressed. For your efforts, I offer a gift of knowledge. But, tell me, what would you like knowledge of?")
        options("Which skill would you like to improve?") {
            opExec("Farming") { giveSkillReward(p, npc, Skills.FARMING, 20044, 20045) }
            opExec("Hunter") { giveSkillReward(p, npc, Skills.HUNTER, 20044, 20045) }
            opExec("Herblore") { giveSkillReward(p, npc, Skills.HERBLORE, 20044, 20045) }
            op("I'll think about it") {
                player(CALM_TALK, "I'll need to think about it for a moment. I'll come back when I've chosen.")
                npc(npc, CALM_TALK, "As you wish, but do not take too long.")
            }
        }
    }
}

private fun claimGodReward(p: Player, npc: NPC) {
    if (p.inventory.freeSlots < 1 ) {
        p.startConversation {
            npc(npc, CALM_TALK, "Ah, you not have room for my gift. Come back when you have space in your inventory.")
            return@startConversation
        }
    }
    p.startConversation {
        npc(npc, HAPPY_TALKING, "Heh heh heh. As you have caught all the special jadinkos, I's be giving you this extra special gift.")
        options("Which skill would you like to improve?") {
            opExec("Farming") { giveSkillReward(p, npc, Skills.FARMING, 20046, null) }
            opExec("Hunter") { giveSkillReward(p, npc, Skills.HUNTER, 20046, null) }
            opExec("Herblore") { giveSkillReward(p, npc, Skills.HERBLORE, 20046, null) }
            op("I'll think about it") {
                player(CALM_TALK, "I'll need to think about it for a moment. I'll come back when I've chosen.")
                npc(npc, CALM_TALK, "As you wish, but do not take long.")
            }
        }
    }
}

private fun canClaimNormalReward(p: Player): Boolean {
    p.populateHerbloreHabitatDatabase()
    val caughtSet = JadinkoType.entries.filter {
        p.weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
    }.toSet()
    val allNonGod = JadinkoType.entries.filterNot { it.isGodJadinko() }
    return allNonGod.all { it in caughtSet }
}

private fun canClaimGodReward(p: Player): Boolean {
    p.populateHerbloreHabitatDatabase()
    val caughtSet = JadinkoType.entries.filter {
        p.weeklyAttributes[CAUGHT_JADINKO_ATTR + it.name] as Boolean
    }.toSet()
    return JadinkoType.entries.all { it in caughtSet }
}

private fun papaMamboDialogue(p: Player, npc: NPC) {
    when {
        p.weeklyAttributes["HabitatRewardClaimed"] == false && canClaimNormalReward(p) -> claimNormalReward(p, npc)
        p.weeklyAttributes["HabitatGodRewardClaimed"] == false && canClaimGodReward(p) -> claimGodReward(p, npc)
        else -> {
            p.startConversation {
                npc(npc, NO_EXPRESSION, "Heh heh heh.")
                options("Select an option") {
                    opExec("Why are you laughing?") { laughing1(p, npc) }
                    opExec("What is this place?") { placeIntro(p, npc) }
                    if (p.savingAttributes["receivedHHSeeds"] == true)
                        opExec("Can I see your store?") { shopOptions(p, npc) }
                    else
                        opExec("Do you have any free seeds?") { freeSeeds(p, npc) }
                    opExec("Who are you?") { whoAreYou(p, npc) }
                    opExec("Can you tell me which Jadinko's I've caught?") { papaMamboProgress(p, npc) }
                    opExec("I've talked enough for now.") { leaveYouBe(p, npc) }
                }
            }
        }
    }
}

private fun laughing1(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "Why are you laughing?")
        npc(npc, NO_EXPRESSION, "Heh heh heh. Can you not hear her joke? It not one of her best, but it's still a good one.")
        options("Select an option") {
            opExec("Whose joke?") { whoseJoke(p, npc) }
            opExec("What is this place?") { placeIntro(p, npc) }
            opExec("Who are you?") { whoAreYou(p, npc) }
            opExec("I'll leave you be for now.") { leaveYouBe(p, npc) }
        }
    }
}

private fun whoseJoke(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "Whose joke are you talking about?")
        npc(npc, NO_EXPRESSION, "Why, the vine's joke, of course. Can you hear someone else round here? Perhaps you're going crazy?")
        options("Select an option") {
            opExec("She?") { hearsVine(p, npc) }
            opExec("What is this place?") { placeIntro(p, npc) }
            opExec("Who are you?") { whoAreYou(p, npc) }
            opExec("I'll leave you be for now.") { leaveYouBe(p, npc) }
        }
    }
}

private fun hearsVine(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "Yes I can hear her.")
        npc(npc, NO_EXPRESSION, "Oh good, ah am so glad you can hear her. She has been nagging me since you arrived. Between you an' me, she can be quite annoying at times.")
        options("Select an option") {
            op("She's telling me that you should give up all your gold coins.") {
                player(CALM_TALK, "She's telling me that you should give up all your gold coins.")
                npc(npc, SAD, "Ah. You sadden me. I had hoped you could hear her beautiful singing, but you clearly cannot. I pay you no ill will, the loss is yours my friend.")
                whoWhatBe(p, npc)
            }
            op("I thought her joke wasn't that bad.") {
                player(CALM_TALK, "I thought her joke wasn't that bad.")
                npc(npc, NO_EXPRESSION, "Heh heh heh, you should hear her on a full moon. She's always on top form in the moonlight.")
                whoWhatBe(p, npc)
            }
        }
    }
}

private fun whoWhatBe(p: Player, npc: NPC) {
    p.startConversation {
        options("Select an option") {
            opExec("Tell me about yourself.") { whoAreYou(p, npc) }
            opExec("What is this place?") { placeIntro(p, npc) }
            opExec("I'll leave you be for now.") { leaveYouBe(p, npc) }
        }
    }
}

private fun placeIntro(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "What is this place?")

        npc(npc, CALM_TALK,"Oh, I see you have taken an interest in this little vine.")
        npc(npc, CALM_TALK,"I grew this vine from a root cutting of the Jade Vine to the north.")
        npc(npc, CALM_TALK,"She was a little clumsy and cut me while I took it.")
        npc(npc, CALM_TALK,"I have since imbued the cutting with my own magic, to help make her what she is today.")
        npc(npc, CALM_TALK,  "She grows food for her jadinkos and extends her arms so that they can find home in her shade.")
        npc(npc, CALM_TALK,  "And she grows different tings; each ting is nectar to one type of jadinko... but poison to another.")
        npc(npc, CALM_TALK,   "Encourage her to grow different combinations of plants and watch as different jadinkos are drawn into her embrace.")

        player(CALM_TALK, "What would be the point of attracting different jadinkos?")
        npc(npc, CALM_TALK,"Aside from the joy of seeing nature's majesty? Ah suppose you could hunt the jadinkos you attract.")
        npc(npc, CALM_TALK,"They have special properties you could use. I have some special marasamaw traps that will catch them, if you have the skill.")
        npc(npc, CALM_TALK,"The jadinkos here are part of the vine herself. They are infused with her magic.")
        npc(npc, CALM_TALK,"Once hunted, they could provide ingredients for powerful new potions, if you have the know-how.")
        jadinkoIntro(p, npc)
    }
}

private fun DialogueBuilder.jadinkoIntro(p: Player, npc: NPC) {
    options {
        opExec("What are jadinkos?") { aboutJadinkos(p, npc) }
        opExec("So, you say that different plants attract different jadinkos?") { differentPlants(p, npc) }
        opExec("Tell me about the potions I can make.") { potionsBranch(p, npc) }
        opExec("Who are you?") { whoAreYou(p, npc) }
        opExec("I'll leave you be.") { leaveYouBe(p, npc) }
    }
}

private fun aboutJadinkos(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "What are jadinkos?")
        npc(npc, NO_EXPRESSION, "Heh heh, that is the name I's given to the strange creatures that live here. It has a nice ring to it, I's thinking.")
        jadinkoIntro(p, npc)
    }
}

private fun differentPlants(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "So, you say that different plants attract different jadinkos?")
        npc(npc, CALM_TALK,"Oh yes. The vine is home to many variations of the strange jadinkos that wander here.")
        npc(npc, CALM_TALK,"Each one is attracted by different combinations of plants and environmental features.")
        npc(npc, CALM_TALK,"And some can only be attracted by those who have used a special juju potion on the flowers.")
        npc(npc, CALM_TALK,"Drawn to the magic like a moth to a flame, they are.")
        npc(npc, CALM_TALK,"Most jadinkos, they like the same ting and will always be attracted by that combination.")
        npc(npc, CALM_TALK,"But there are jadinkos whose tastes are fickle and their requirements change every week or so.")
        npc(npc, CALM_TALK,"These fickle creatures, when captured, give you ingredients that can enhance your mastery of this strange habitat.")
        player(CALM_TALK, "Is there any reason to catch them all?")
        npc(npc, NO_EXPRESSION, "Heh heh heh, of course! If you are skilled enough to catch one of each jadinko type that wander 'ere, come and speak to me. I will have a reward for you.")
        jadinkoIntro(p, npc)
    }
}

private fun potionsBranch(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "Tell me about the potions I can make.")
        npc(npc, CALM_TALK, "These potions are different from others you can create.")
        npc(npc, CALM_TALK, "First, they be uniquely tied to you, your essence. No other can benefit from them.")
        npc(npc, CALM_TALK, "Second, they open up new ways of seeing the world and finding new rewards.")
        npc(npc, CALM_TALK, "Each is different and provides a special benefit. Perhaps, most important, is the juju hunter potion you can create, for this potion will attract new creatures to this vine.")
        npc(npc, CALM_TALK, "These juju potions cannot be made in a normal potion vial. They must be made in a specially created juju vial,for only they can hold the power.")
        jadinkoIntro(p, npc)
    }
}

private fun whoAreYou(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, CALM_TALK, "I am Papa Mambo, shaman and protector of this vine. Once, I was the shaman of Shilo Village, and now I's a keeper of this vine.")
        options("Select an option") {
            op("Why did you leave the village?") {
                npc(npc, NO_EXPRESSION, "Heh heh heh heh heh. We did not see eye to eye on all tings. Heh heh. Some thought my studies were...unnerving. But it always seemed such a shame leaving the dead buried, when there was such good work they could do.")
                whoWhatBe(p, npc)
            }
            opExec("What is this place?") { placeIntro(p, npc) }
            opExec("I've talked enough for now.") { leaveYouBe(p, npc) }
        }
    }
}

private fun leaveYouBe(p: Player, npc: NPC) {
    p.startConversation {
        player(CALM_TALK, "I'll leave you be for now.")
        npc(npc, CALM_TALK, "You may be wanting to visit the druid. He's learning about this habitat, like you, and may be able to share the knowledge he has gained. Goodbye, ma friend.")
    }
}

private fun freeSeeds(p: Player, npc: NPC) {

    if (p.savingAttributes["receivedHHSeeds"] == true) {
        p.startConversation {
            npc(npc, CALM_TALK, "Heh heh heh. I's only got some to sells.")
            shopOptions(p, npc)
        }
    }

    if (p.inventory.freeSlots < 3) {
        p.startConversation {
            npc(npc, CALM_TALK, "Alas, you not 'ave room for this gift.")
            p.sendMessage("You need at least 3 free slots to get Papa Mambo's gift.")
            return@startConversation
        }
    }
    p.startConversation {
        npc(npc, HAPPY_TALKING, "'Ere, as you are new to this vine, take this little gift from me.")
        npc(npc, HAPPY_TALKING, "Use them to make this vine bloom an' blossom, so many jadinkos will come. If you run out, you can buy more from me.")
        p.inventory.addItem(19922, 1)
        p.inventory.addItem(19927, 1)
        p.inventory.addItem(19932, 1)
        p.savingAttributes["receivedHHSeeds"] = true
        p.sendMessage("Papa Mambo hands you a red vine blossom seed, a green vine blossom seed and a blue vine blossom seed.")
    }
}

private fun shopOptions(p: Player, npc: NPC) {
    p.startConversation {
        npc(npc, NO_EXPRESSION, "Heh heh heh. I's got some goods if you fancy a look, ma friend.")
        options {
            op("Trade") {
                ShopsHandler.openShop(p, "papa_mambos_shop")
            }
            op("Nevermind") {
                player(CALM_TALK, "No thanks, just passing by.")
                npc(npc, CALM_TALK, "Very well. Come back if you change your mind, ma friend.")
            }
        }
    }
}

private fun Player.openHabitatBuildDialogue(player: Player, npc: NPC) {
    startConversation {
        npc(npc, CALM_TALK, "You want to build, eh? It'll cost you some coins, ma friend.")
        options {
            HabitatFeature.entries.forEach { feature ->
                op("${feature.name.replace('_', ' ')} - ${feature.mamboCost} coins") {
                    if (inventory.coins < feature.mamboCost) {
                        player(ANGRY, "I don't have enough coins.")
                        npc(npc, NO_EXPRESSION, "Come back when you do.")
                    } else {
                        inventory.removeCoins(feature.mamboCost)
                        player.habitatFeature = feature
                        npc(npc, HAPPY_TALKING, "It is done! The vine thanks you.")
                    }
                }
            }
            op("Nevermind.") {
                player(CALM_TALK, "On second thought, nevermind.")
                npc(npc, NO_EXPRESSION, "Very well, come back if you change your mind.")
            }
        }
    }
}

private fun giveSkillReward(p: Player, npc: NPC, skillId: Int, item1: Int, item2: Int?) {
    // Calculate XP: Y = Z * (Z - 4) + 200 per https://www.runehq.com/minigame/herblore-habitat
    val level = p.skills.getLevel(skillId)
    val xp = level * (level - 4) + 200
    if (item2 != null) {
        p.startConversation {
            npc(npc, HAPPY_TALKING, "Here, have a little gift from me.")
            npc(npc, CALM_TALK, "You be finding that the shaman clothes give you greater...experience, shall we say, for growing plants, finding jadinkos and making juju potions. This gift will only last for half an hour, though.")
            exec {
                p.weeklyAttributes["HabitatRewardClaimed"] = true
                p.skills.addXp(skillId, xp.toDouble())
                if (p.inventory.freeSlots >= 2) {
                    p.inventory.addItem(item1)
                    p.inventory.addItem(item2)
                } else {
                    World.addGroundItem(Item(item1), p.tile, p)
                    World.addGroundItem(Item(item2), p.tile, p)
                }
            }
        }
    } else {
        p.startConversation {
            npc(npc, HAPPY_TALKING, "Here, have a little gift from me.")
            npc(npc, CALM_TALK, "You be finding that the shaman clothes give you greater...experience, shall we say, for growing plants, finding jadinkos and making juju potions. This gift will only last for half an hour, though.")
            exec {
                p.weeklyAttributes["HabitatGodRewardClaimed"] = true
                p.skills.addXp(skillId, xp.toDouble())
                if (p.inventory.freeSlots >= 1) {
                    p.inventory.addItem(item1)
                } else {
                    World.addGroundItem(Item(item1), p.tile, p)
                }
            }
        }
    }
}
