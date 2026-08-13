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
package com.rs.net.decoders.handlers.impl;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.content.world.npcs.SimulatedPlayerBot;
import com.rs.game.content.world.npcs.SimulatedPlayerPopulationManager;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.model.Account;
import com.rs.lib.model.Friend;
import com.rs.lib.net.ClientPacket;
import com.rs.lib.net.packets.PacketHandler;
import com.rs.lib.net.packets.decoders.SocialAddRemove;
import com.rs.lib.net.packets.encoders.social.FriendStatus;
import com.rs.net.LobbyCommunicator;

public class SocialAddRemoveHandler implements PacketHandler<Player, SocialAddRemove> {

	@Override
	public void handle(Player player, SocialAddRemove packet) {
		if (Settings.getConfig().isSinglePlayer()) {
			SimulatedPlayerBot bot = SimulatedPlayerPopulationManager.INSTANCE.findByDisplayName(packet.getName());
			Player online = World.getPlayerByDisplay(packet.getName());
			Account target = bot != null ? bot.getAccount() : online != null ? online.getAccount() : null;
			if (target == null) {
				player.sendMessage("Unable to find " + packet.getName() + ".");
				return;
			}
			if (packet.getOpcode() == ClientPacket.ADD_FRIEND) {
				player.getSocial().addFriend(target);
				player.getSession().write(new FriendStatus(player.getAccount(),
						new Friend(target, Settings.getConfig().getWorldInfo(), false)));
			} else if (packet.getOpcode() == ClientPacket.REMOVE_FRIEND) {
				player.getSocial().removeFriend(target);
			} else if (packet.getOpcode() == ClientPacket.ADD_IGNORE) {
				player.getSocial().addIgnore(target);
			} else if (packet.getOpcode() == ClientPacket.REMOVE_IGNORE) {
				player.getSocial().removeIgnore(target);
			}
			return;
		}
		LobbyCommunicator.forwardPackets(player, packet);
	}

}
