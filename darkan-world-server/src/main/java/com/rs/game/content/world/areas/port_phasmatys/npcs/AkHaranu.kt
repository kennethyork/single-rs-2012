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
package com.rs.game.content.world.areas.port_phasmatys.npcs

import com.rs.engine.dialogue.HeadE
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.game.content.quests.ghosts_ahoy.GhostsAhoy
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnNpc
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler
private const val oakLongbowS = 4236
private const val oakLongbow = 845
private const val translationManual = 4249
fun akHaranu(p: Player, npc: NPC) {
	val questStage: Boolean = p.getQuestStage(Quest.GHOSTS_AHOY) == GhostsAhoy.STAGE_5_GATHER_ITEMS
	if(p.questManager.getAttribs(Quest.GHOSTS_AHOY).getB("AKHARANU_REWARD")) {
		p.startConversation {
			player(HeadE.CALM_TALK, "Thank you for the translation manual, Ak-Haranu - it may save many souls before long.")
			npc(npc, HeadE.CALM_TALK, "And Ak-Haranu thanks you for kind gift of shieldbow.")
		}
		return
	}
	if(questStage && p.inventory.containsItem(oakLongbowS)) {
		akHaranuReward(p, npc)
		return
	}
	if(questStage && !p.inventory.containsItem(oakLongbow)) {
		p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("SPOKE_WITH_AKHARANU", true)
		p.startConversation {
			player(HeadE.CALM_TALK, "It's nice to see a human face around here.")
			npc(npc, HeadE.CALM_TALK, "My name is Ak-Haranu. I am trader, come from many far across sea in east.")
			player(HeadE.CALM_TALK, "You come from the lands of the East? Do you have anything that can help me translate a book that is scribed in your language?")
			npc(npc,HeadE.SECRETIVE, "Ak-Haranu may help you. A translation manual I have. Much good for reading Eastern language.")
			player(HeadE.CALM_TALK, "How much do you want for it?")
			npc(npc,HeadE.SECRETIVE, "Ak-Haranu not want money for this book, as is such small thing. But there may be something you could do for Ak-Haranu.")
			npc(npc,HeadE.SECRETIVE, "I am big admirer of Robin, Master Bowman. He staying in village inn.")
			player(HeadE.CALM_TALK, "What would you like me to do?")
			npc(npc,HeadE.SECRETIVE, "Please get Master Bowman sign an oak longbow for me. So Ak-Haranu can show family and friends when returning home and become much admired. Then I give book in exchange.")
			player(HeadE.CALM_TALK, "Have you got an Oak Longbow that I can get robin to sign for you?")
			npc(npc, HeadE.CALM_TALK, "No, Ak-Haranu afraid that no longbow in supply at moment. You must make or buy one.")
		}
		return
	}
	if(questStage && p.inventory.containsItem(oakLongbow)) {
		p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("SPOKE_WITH_AKHARANU", true)
		p.startConversation {
			player(HeadE.CALM_TALK, "It's nice to see a human face around here.")
			npc(npc, HeadE.CALM_TALK, "My name is Ak-Haranu. I am trader, come from many far across sea in east.")
			player(HeadE.CALM_TALK, "You come from the lands of the East? Do you have anything that can help me translate a book that is scribed in your language?")
			npc(npc,HeadE.SECRETIVE, "Ak-Haranu may help you. A translation manual I have. Much good for reading Eastern language.")
			player(HeadE.CALM_TALK, "How much do you want for it?")
			npc(npc,HeadE.SECRETIVE, "Ak-Haranu not want money for this book, as is such small thing. But there may be something you could do for Ak-Haranu.")
			npc(npc,HeadE.SECRETIVE, "I am big admirer of Robin, Master Bowman. He staying in village inn.")
			player(HeadE.CALM_TALK, "What would you like me to do?")
			npc(npc,HeadE.SECRETIVE, "Please get Master Bowman sign an oak longbow for me. So Ak-Haranu can show family and friends when returning home and become much admired. Then I give book in exchange.")
			player(HeadE.CALM_TALK, "Ok, wait here - I'll get you your bow.")
		}
		return
	}
	else
		p.startConversation {
			player(HeadE.HAPPY_TALKING, "It's nice to see a human face around here.")
			npc(npc, HeadE.HAPPY_TALKING, "My name is Ak-Haranu. I am trader, come from many far across sea in east.")
			player(HeadE.HAPPY_TALKING, "You come from the lands of the East?")
			npc(npc, HeadE.HAPPY_TALKING, "Yes, Would you like buy Eastern gifts?")
			label("initialOps")
			options {
				op("I'd like to see what you have for sale.") {
					exec{(ShopsHandler.openShop(p, "akharanus_exotic_shop"))}
				}
				op("No thanks.") {
					player(HeadE.CONFUSED, "No thanks.")
				}
			}
		}
}

fun akHaranuReward(p: Player, npc: NPC) {
	p.startConversation {
		player(HeadE.CALM_TALK, "I have your signed longbow for you.")
		npc(npc, HeadE.CALM_TALK, "Ah, can it be true? You have obtained bow from Master Bowman?")
		player(HeadE.CALM_TALK, "He was more than happy to oblige *cough*. Here you are.")
		item(translationManual,"Ak-Haranu gives you a translation manual in return for the signed oak shieldbow.")
		p.inventory.deleteItem(oakLongbowS, 1)
		p.inventory.addItem(translationManual)
		p.questManager.getAttribs(Quest.GHOSTS_AHOY).setB("AKHARANU_REWARD", true)
		npc(npc, HeadE.CALM_TALK, "May honour be bestowed upon you and your family!")
	}
	return
}

@ServerStartupEvent
fun mapAkHaranu() {
	onNpcClick(1687, options = arrayOf("Talk-To")) { (player, npc) ->
		akHaranu(player, npc)
	}
	onNpcClick(1687, options = arrayOf("Trade")) { (player) ->
		ShopsHandler.openShop(player, "akharanus_exotic_shop")
	}
	onItemOnNpc(1687) { e ->
		if (e.item.id == oakLongbowS) akHaranuReward(e.player, e.npc)
	}
}


