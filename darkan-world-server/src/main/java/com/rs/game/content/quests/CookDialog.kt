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
package com.rs.game.content.quests

import com.rs.engine.dialogue.DialogueBuilder
import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Item
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mapCookDialog() {
    onNpcClick(278) { (player, npc) ->
        when {
            !player.isQuestComplete(Quest.COOKS_ASSISTANT) -> handleCooksAssistant(player, npc)
            //canStartAnotherCooksQuest(player) -> handleAnotherCooksQuest(player, npc)
            else -> defaultCookDialog(player, npc)
        }
    }
}

private fun canStartAnotherCooksQuest(player: Player): Boolean {
    return player.isQuestComplete(Quest.COOKS_ASSISTANT) &&
            !player.isQuestComplete(Quest.ANOTHER_COOKS_QUEST) &&
            player.skills.getLevel(Skills.COOKING) >= 10
}

private fun handleCooksAssistant(player: Player, npc: NPC) {
    when (player.getQuestStage(Quest.COOKS_ASSISTANT)) {
        0 -> startCooksAssistantQuest(player, npc)
        1 -> checkCooksAssistantProgress(player, npc)
    }
}

private fun startCooksAssistantQuest(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, HeadE.SAD_MILD, "What am I to do?")
        player(HeadE.CONFUSED, "What's wrong?")
        npc(npc, HeadE.UPSET, "Oh dear, oh dear, I'm in a terrible, terrible mess!")
        npc(npc, HeadE.UPSET, "It's the duke's birthday today, and I should be making him a big birthday cake using special ingredients... but I've forgotten to get the ingredients.")
        npc(npc, HeadE.SAD_MILD, "I'll never get them in time now. He'll sack me!<br>Whatever will I do?")
        npc(npc, HeadE.UPSET, "I have four children and a goat to look after. Would you help me? Please?")

        options {
            op("Of course. What is it you are looking for?") {
                player(HeadE.HAPPY_TALKING, "Of course. What is it you are looking for?")
                npc(npc, HeadE.HAPPY_TALKING, "Oh, thank you, thank you. I must tell you that this is no ordinary cake, though - only the best ingredients will do!")
                npc(npc, HeadE.HAPPY_TALKING, "I need an egg, some milk, and a pot of flour.")
                player(HeadE.CONFUSED, "Where can I find those, then?")
                npc(npc, HeadE.CONFUSED, "That's the problem: I don't exactly know. I usually send my assistant to get them for me but he quit.")
                player(HeadE.HAPPY_TALKING, "Well don't worry then, I'll be back with the ingredients soon.") {
                    player.questManager.setStage(Quest.COOKS_ASSISTANT, 1)
                }
            }
            op("Sorry, I can't help right now.")
        }
    }
}

private fun checkCooksAssistantProgress(player: Player, npc: NPC) {
    val requiredItems = listOf(Item(1933, 1), Item(1944, 1), Item(1927, 1))

    player.startConversation {
        npc(npc, HeadE.CONFUSED, "How are you getting with finding the ingredients?")

        if (!player.inventory.containsItems(*requiredItems.toTypedArray())) {
            player(HeadE.WORRIED, "I haven't quite gotten them all yet. I'll be back when I have the rest of them.")
            return@startConversation
        }

        player(HeadE.HAPPY_TALKING, "I have all of the items right here!")
        npc(npc, HeadE.HAPPY_TALKING, "You've brought me everything I need! I am saved! Thank you!")
        player(HeadE.CONFUSED, "So, do I get to go to the Duke's party?")
        npc(npc, HeadE.CALM_TALK, "I'm afraid not. Only the big cheeses get to dine with the Duke.")
        player(HeadE.CALM_TALK, "Well, maybe one day, I'll be important enough to sit at the Duke's table.")
        npc(npc, HeadE.CALM_TALK, "Maybe, but I won't be holding my breath.") {
            player.inventory.removeItems(*requiredItems.toTypedArray())
            player.questManager.completeQuest(Quest.COOKS_ASSISTANT)
        }
    }
}

private fun handleAnotherCooksQuest(player: Player, npc: NPC) {
    when (player.getQuestStage(Quest.ANOTHER_COOKS_QUEST)) {
        0 -> startAnotherCooksQuest(player, npc)
        1 -> checkAnotherCooksQuestProgress(player, npc)
        2 -> reminderToVisitFeast(player, npc)
        3 -> handlePostCutsceneDialog(player, npc)
    }
}

private fun startAnotherCooksQuest(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, HeadE.SAD_MILD, "Hello friend, how is the adventuring going?")
        options {
            op("Do you have any other quests for me?") {
                showQuestIntroduction(player, npc)
            }
            op("I am getting strong and mighty.") {}
            op("I keep on dying.") {}
            op("Can I use your range?") {
                npc(npc, HeadE.HAPPY_TALKING, "Yes. Thank you for the help! Feel free to use my range!")
            }
        }
    }
}

private fun DialogueBuilder.showQuestIntroduction(player: Player, npc: NPC) {
    player(HeadE.HAPPY_TALKING, "The last one of yours was so much fun. I was hoping you would!")
    npc(npc, HeadE.SAD_MILD, "Ooooh dear, I might have, but it's all very embarrassing...")
    player(HeadE.CONFUSED, "Embarrassing? How do you mean?")
    npc(npc, HeadE.SAD_MILD, "Weeellll... You know I told you that it was the Duke's birthday?")
    player(HeadE.HAPPY_TALKING, "Yup, that's why I had to go and collect you things to make a cake with wasn't it?")
    npc(npc, HeadE.SAD_MILD, "Yeeeesssss... Weeeelllll...")
    npc(npc, HeadE.SAD_MILD, "It isn't. I don't even know when his birthday is!")
    player(HeadE.UPSET, "GRRRR! Why did you have me running around finding ingredients for you then?!?!")
    npc(npc, HeadE.SAD_MILD, "I needed to test you and see if I could trust you...")
    npc(npc, HeadE.SAD_MILD, "I am in a terrible pickle, and I desperately need somebody trustworthy to help me out!")

    options {
        op("What seems to be the problem?") {
            showQuestDetails(player, npc)
        }
        op("No thanks, I don't want to help.") {
            player(HeadE.UPSET, "No thanks, I don't want to help.")
            npc(npc, HeadE.SAD_SNIFFLE, "OK then.")
        }
        op("How do you still have a job?") {
            player(HeadE.UPSET, "How do you still have a job?")
        }
    }
}

private fun DialogueBuilder.showQuestDetails(player: Player, npc: NPC) {
    player(HeadE.CONFUSED, "You need help AGAIN? Well, what seems to be the problem?")
    npc(npc, HeadE.SAD_MILD, "Well, you're obviously trustworthy, I guess I could tell you...")
    player(HeadE.CONFUSED, "Please do. The suffering of others always seems to turn out very profitable for me.")
    npc(npc, HeadE.SAD_MILD, "Well... It's like this...")
    npc(npc, HeadE.UPSET, "Did you ever hear of Franizzard Van Lumbcook?")
    player(HeadE.CONFUSED, "Erm... Can't say I have.")
    player(HeadE.CONFUSED, "And with a name like that, I'm sure I would remember.")
    npc(npc, HeadE.UPSET, "Well, he was a great chef, and also my ancestor.")
    npc(npc, HeadE.UPSET, "One hundred years ago there was a very important gathering held here in Lumbridge Castle, and he cooked a banquet for the meeting, which was so stupendously amazing that he was offered a job for life, along with all")
    npc(npc, HeadE.UPSET, "of his descendants.")
    player(HeadE.CONFUSED, "So you have your job because a hundred years ago your ancestor was a very good cook.")
    player(HeadE.CONFUSED, "I don't see how that's much of a problem for you...")
    player(HeadE.CONFUSED, "In fact I would say you are a VERY LUCKY man indeed. Frankly, I'd have fired you by now if you worked for me.")
    npc(npc, HeadE.UPSET, "Oooooh no.... You don't understand...")
    npc(npc, HeadE.SAD_MILD, "This big important secret meeting that Franizzard cooked for is held here in Lumbridge castle every 10 years, and tonight will be the tenth anniversary of it!")
    player(HeadE.CONFUSED, "Still not seeing the problem...")
    npc(npc, HeadE.UPSET, "Well, I have always felt like a failure as a cook, and that my job here is down to the skills of my ancestor.")
    npc(npc, HeadE.UPSET, "I want to prove to the world that I am a great cook too!")
    npc(npc, HeadE.UPSET, "But to do that... (sigh) No, it just won't work...")
    player(HeadE.CONFUSED, "What won't? Come on man, pull yourself together and speak to me!")
    npc(npc, HeadE.UPSET, "(sniff) Weeeeelll... I thought that I would recreate the meal Franizzard cooked all of those years ago, the one that impressed")
    npc(npc, HeadE.UPSET, "everybody so much... Theres just this small problem though...")
    player(HeadE.CONFUSED, "Let me take a wild guess: You have the recipe but you don't have any of the ingredients, and OBVIOUSLY you can't just go and get them yourself?")
    npc(npc, HeadE.UPSET, "Yes! However did you know?")
    player(HeadE.CONFUSED, "Call it a hunch.")
    player(HeadE.CONFUSED, "Okay, tell me what you're after and then I'll decide if I want to help you or not.")
    npc(npc, HeadE.UPSET, "Well, they are common enough ingredients, although I must admit the recipe I am following is very strange indeed...")
    npc(npc, HeadE.UPSET, "I will pay you upfront for any expenses you may incur while obtaining them, and of course you may keep the change for being so helpful to me.")
    npc(npc, HeadE.UPSET, "Please can you help me? the feast is scheduled to start very soon, and I simply cannot leave!")

    options {
        op("YES") {
            player(HeadE.HAPPY_TALKING, "I would love to help you! What do I have to do?")
            npc(npc, HeadE.HAPPY_TALKING, "Well, I have an incredibly important banquet that I have to prepare for, and I am using an ancient cookbook to recreate the meal my famous ancestor made a hundred years ago.")
            npc(npc, HeadE.HAPPY_TALKING, "Most of the ingredients I was able to obtain fairly easily, but there were some... oddities... that I have been unable to lay my hands on.")
            player(HeadE.CONFUSED, "Like what?")
            npc(npc, HeadE.HAPPY_TALKING, "I need the following ingredients as soon as possible!")
            npc(npc, HeadE.HAPPY_TALKING, "One Eye of Newt<br>A freshly made 'Dirty Blast'<br>A rotten tomato")
            npc(npc, HeadE.HAPPY_TALKING, "And a glass of greenman's ale.")
            npc(npc, HeadE.HAPPY_TALKING, "I have given you 100 coins to cover any expenses you may incur, feel free to keep the change.") {
                player.inventory.addCoins(100)
                player.setQuestStage(Quest.ANOTHER_COOKS_QUEST, 1)
            }
            player(HeadE.HAPPY_TALKING, "I'll go look for those for you then!")
        }
        op("NO")
    }
}

data class QuestIngredient(val item: Item, val name: String, val hint: String)

val ingredients = listOf(
    QuestIngredient(Item(221, 1), "an eye of newt", "herblore and magic shops"),
    QuestIngredient(Item(2518, 1), "a rotten tomato", "public spectacles"),
    QuestIngredient(Item(1909, 1), "a Greenman's ale", "Castle Wars"),
    QuestIngredient(Item(7497, 1), "a 'dirty blast'", "fresh Fruit Blast with ash")
)

private fun checkAnotherCooksQuestProgress(player: Player, npc: NPC) {
    val foundIngredients = ingredients.filter { player.inventory.containsItems(it.item) }

    player.startConversation {
        npc(npc, HeadE.HAPPY_TALKING, "Great! You're back!")
        npc(npc, HeadE.CONFUSED, "Did you bring the ingredients I asked for?")

        when (foundIngredients.size) {
            0 -> {
                player(HeadE.WORRIED, "Erm... Sorry, I don't have any of that with me...")
                showIngredientHelp(npc, ingredients)
            }
            4 -> {
                player(HeadE.HAPPY_TALKING, "You will be happy to know... I have all of that with me!") {
                    ingredients.forEach { player.inventory.removeItems(it.item) }
                    npc(npc, HeadE.HAPPY_TALKING, "You absolutely MUST go through to the dining room just there and see the feast I have prepared.")
                    npc(npc, HeadE.HAPPY_TALKING, "I simply won't take no for an answer!")
                    player.setQuestStage(Quest.ANOTHER_COOKS_QUEST, 2)
                }
            }
            else -> {
                val missingItems = ingredients - foundIngredients.toSet()
                val foundNames = foundIngredients.joinToString(" and ") { it.name }
                player(HeadE.WORRIED, "I have some ingredients: $foundNames, but I'm still missing ${missingItems.size} items.")
                npc(npc, HeadE.CALM_TALK, "Come back when you have all of them! And hurry! It is a VERY important meeting!")
                showIngredientHelp(npc, missingItems)
            }
        }
    }
}

private fun DialogueBuilder.showIngredientHelp(npc: NPC, ingredients: List<QuestIngredient>) {
    options {
        op("Okay then.") {
            player(HeadE.CALM_TALK, "Okay then, I'll get on it.")
        }
        ingredients.forEach { ingredient ->
            op("Where do I get ${ingredient.name}?") {
                player(HeadE.CONFUSED, "Where would I find ${ingredient.name}?")
                npc(npc, HeadE.CALM_TALK, ingredient.hint)
            }
        }
    }
}

private fun reminderToVisitFeast(player: Player, npc: NPC) {
    val gender = if (player.appearance.isMale) "guy" else "gal"
    player.startConversation {
        npc(npc, HeadE.HAPPY_TALKING, "Hello again adventurer! How's the adventuring going?")
        npc(npc, HeadE.CONFUSED, "And why haven't you gone and looked at the feast I have prepared yet?")
        npc(npc, HeadE.HAPPY_TALKING, "Trust me, it will be a sight the likes of which you will never see again!")
        player(HeadE.CONFUSED, "I don't know... I'm a busy $gender...")
        npc(npc, HeadE.HAPPY_TALKING, "No, honestly, you MUST go in there!")
        npc(npc, HeadE.ANGRY, "GO IN THERE!")
    }
}

private fun handlePostCutsceneDialog(player: Player, npc: NPC) {
    player.startConversation {
        if (!player.questManager.getAttribs(Quest.ANOTHER_COOKS_QUEST).getB("HAS_TALKED_TO_COOK_POST_CUTSCENE")) {
            npc(npc, HeadE.HAPPY_TALKING, "Wow! You were incredible!")
            npc(npc, HeadE.HAPPY_TALKING, "The way you defeated the Culinaromancer, you made it seem so easy!")
            player(HeadE.CONFUSED, "Uh... What?")
            npc(npc, HeadE.HAPPY_TALKING, "With the foods, and that big portal thingy, and everything!")
            npc(npc, HeadE.HAPPY_TALKING, "I'm just glad you were around, who knows WHAT could have happened if you weren't able to help us!")
            player(HeadE.CONFUSED, "I'm sorry, I don't have any clue what you...")
            player(HeadE.CALM, "Oh. Oh, right. This must be part of that whole time-bubble thing the fortuneteller told me about.")
            player(HeadE.CALM, "So the cook outside of the bubble remembers me having already saved the council members even though I haven't actually gone and done it yet...")
            player(HeadE.DIZZY, "Gah! This time travel stuff makes my head hurt!")
            exec { player.questManager.getAttribs(Quest.ANOTHER_COOKS_QUEST).setB("HAS_TALKED_TO_COOK_POST_CUTSCENE", true) }
        } else {
            if (!player.questManager.isComplete(Quest.RECIPE_FOR_DISASTER)) {
                npc(npc, HeadE.HAPPY_TALKING, "The way you defeated the Culinaromancer, you made it seem so easy!")
                player(HeadE.CONFUSED, "So... I don't suppose you can remember exactly how I defeated him can you?")
                npc(npc, HeadE.HAPPY_TALKING, "Of course! Which bit can't you remember doing?")
                options("Which bit can't you remember doing?") {
                    op("Protecting the Dwarf") {}
                    op("Protecting the Goblins") {}
                    op("Protecting the Pirate") {}
                    op("More...") {}
                }
            }
        }
    }
}

private fun defaultCookDialog(player: Player, npc: NPC) {
    player.startConversation {
        npc(npc, HeadE.HAPPY_TALKING, "Thank you for the help! Feel free to use my range!")
    }
}