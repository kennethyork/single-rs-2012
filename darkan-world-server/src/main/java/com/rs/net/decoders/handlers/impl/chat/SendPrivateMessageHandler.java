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
package com.rs.net.decoders.handlers.impl.chat;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.content.world.npcs.SimulatedPlayerSocial;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.net.packets.PacketHandler;
import com.rs.lib.net.packets.decoders.chat.SendPrivateMessage;
import com.rs.net.LobbyCommunicator;

public class SendPrivateMessageHandler implements PacketHandler<Player, SendPrivateMessage> {

	@Override
	public void handle(Player player, SendPrivateMessage packet) {
		if (player.getAccount().isMuted()) {
			player.sendMessage("You are muted. The mute will be lifted at " + player.getAccount().getUnmuteDate());
			return;
		}
		if (Settings.getConfig().isSinglePlayer()) {
			if (SimulatedPlayerSocial.handlePrivateMessage(player, packet.getToDisplayName(), packet.getMessage()))
				return;
			Player target = World.getPlayerByDisplay(packet.getToDisplayName());
			if (target != null && !target.isHeadless() && !target.hasFinished()) {
				player.getPackets().sendPrivateMessage(target.getDisplayName(), packet.getMessage());
				target.getPackets().receivePrivateMessage(player.getAccount(), packet.getMessage());
			} else {
				player.sendMessage("Unable to find " + packet.getToDisplayName() + ".");
			}
			return;
		}
		LobbyCommunicator.forwardPackets(player, packet);
	}

}
