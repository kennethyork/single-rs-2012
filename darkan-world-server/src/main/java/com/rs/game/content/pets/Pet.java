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
package com.rs.game.content.pets;

import com.rs.game.content.skills.summoning.Familiar;
import com.rs.game.model.entity.actions.EntityFollow;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.managers.InterfaceManager.Sub;
import com.rs.lib.game.Tile;
import static com.rs.engine.variables.VarPlayer.follower_obj;
import static com.rs.engine.variables.VarBitPlayer.lore_animal_hunger_percent;
import static com.rs.engine.variables.VarBitPlayer.lore_animal_size_percent;
import static com.rs.engine.variables.VarBitPlayer.lore_interface_boolean;

public final class Pet extends NPC {

	private final Player owner;
	private final int itemId;
	private final PetDetails details;
	private double growthRate;
	private final Pets pet;

	public Pet(int id, int itemId, Player owner, Tile tile, PetDetails details) {
		super(id, tile);
		this.owner = owner;
		this.itemId = itemId;
		this.details = details;
		pet = Pets.forId(itemId);
		setIgnoreNPCClipping(true);
		if (pet == Pets.TROLL_BABY && owner.getPetManager().getTrollBabyName() != null)
			setName(owner.getPetManager().getTrollBabyName());
		if (details != null) {
			sendMainConfigurations();
			sendFollowerDetails();
		}
	}
	
	@Override
	public boolean blocksOtherNpcs() {
		return false;
	}

	@Override
	public void processNPC() {
		Familiar.sendLeftClickOption(owner);
		if (details != null) {
			if (pet != Pets.TROLL_BABY || pet.getFood().length > 0) {
				details.updateHunger(0.025);
				owner.getVars().setVarBit(lore_animal_hunger_percent, (int) details.getHunger());
			}
			if (growthRate > 0.000) {
				details.updateGrowth(growthRate);
				owner.getVars().setVarBit(lore_animal_size_percent, (int) details.getGrowth());
				if (details.getGrowth() == 100.0)
					growNextStage();
			}
		}
		if (!withinDistance(owner, 12)) {
			call();
			return;
		}
		if (!getActionManager().hasSkillWorking())
			getActionManager().setAction(new EntityFollow(owner));
	}

	public void growNextStage() {
		if ((details.getStage() == 3) || (pet == null))
			return;
		int npcId = pet.getNpcId(details.getStage() + 1);
		if (npcId < 1)
			return;
		details.setStage(details.getStage() + 1);
		int itemId = pet.getItemId(details.getStage());
		if (pet.getNpcId(details.getStage() + 1) > 0)
			details.updateGrowth(-100.0);
		owner.getPetManager().setItemId(itemId);
		owner.getPetManager().setNpcId(npcId);
		finish();
		Pet newPet = new Pet(npcId, itemId, owner, owner.getTile(), details);
		newPet.growthRate = growthRate;
		owner.setPet(newPet);
		owner.sendMessage("<col=ff0000>Your pet has grown larger.</col>");
	}

	public void pickup() {
		if (itemId > 50000 || owner.getInventory().hasFreeSlots()) {
			if (itemId < 50000)
				owner.getInventory().addItem(itemId, 1);
			owner.setPet(null);
			owner.getPetManager().setNpcId(-1);
			owner.getPetManager().setItemId(-1);
			owner.getPackets().sendRunScript(2471);
			owner.getInterfaceManager().removeSub(Sub.TAB_FOLLOWER);
			finish();
		} else
			owner.sendMessage("You need more inventory slots to pick up your pet.");
	}

	public void call() {
		Tile teleTile = owner.getNearestTeleTile(this);
		if (teleTile != null)
			tele(teleTile);
	}

	/**
	 * Baby troll = var 2480 itemId it's eaten
	 */
	public void sendMainConfigurations() {
		owner.getVars().setVar(follower_obj, itemId);// configures pet type
		owner.getVars().setVar(1174, 0); //refresh pet head
		owner.getVars().setVarBit(lore_interface_boolean, 0); //refresh pet emote
		owner.getPackets().sendVarc(1436, 0);
		owner.getPackets().setIFHidden(747, 9, false);
	}

	public void sendFollowerDetails() {
		if (details == null || owner == null)
			return;
		owner.getVars().setVarBit(lore_animal_size_percent, (int) details.getGrowth());
		owner.getVars().setVarBit(lore_animal_hunger_percent, (int) details.getHunger());
		owner.getInterfaceManager().sendSub(Sub.TAB_FOLLOWER, 662);
		owner.getInterfaceManager().openTab(Sub.TAB_FOLLOWER);
	}

	public PetDetails getDetails() {
		return details;
	}

	public double getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(double growthRate) {
		this.growthRate = growthRate;
	}

	public int getItemId() {
		return itemId;
	}

}