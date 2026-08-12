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
package com.rs.net;

import com.rs.Settings;
import com.rs.game.model.entity.player.Player;
import com.rs.db.local.LocalAccountStore;
import com.rs.lib.model.Account;
import com.rs.lib.model.FriendsChat;
import com.rs.lib.model.clan.Clan;
import com.rs.lib.net.packets.Packet;
import com.rs.lib.web.APIUtil;
import com.rs.lib.web.dto.LoginRequest;
import com.rs.lib.web.dto.PacketDto;
import com.rs.lib.web.dto.UpdateFC;
import com.rs.lib.web.dto.WorldPlayerAction;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LobbyCommunicator {

	public static void addWorldPlayer(Account account, Consumer<Boolean> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(true); return; }
		post(Boolean.class, new WorldPlayerAction(account, Settings.getConfig().getWorldInfo()), "addworldplayer", cb);
	}

	public static void removeWorldPlayer(Player player) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(new WorldPlayerAction(player.getAccount(), Settings.getConfig().getWorldInfo()), "removeworldplayer");
	}

	public static Account authWorldLogin(String username, String password) throws InterruptedException, ExecutionException, IOException {
		if (Settings.getConfig().isSinglePlayer()) return LocalAccountStore.authenticate(username, password);
		return postSync(Account.class, new LoginRequest(username, password), "authworldlogin");
	}

	public static void getAccountByDisplay(String displayName, Consumer<Account> cb) {
		if (Settings.getConfig().isSinglePlayer()) { cb.accept(LocalAccountStore.find(displayName)); return; }
		post(Account.class, new LoginRequest(displayName, "cock"), "getaccountbydisplay", cb);
	}

	public static void getAccount(String username, String password, Consumer<Account> cb) {
		if (Settings.getConfig().isSinglePlayer()) { cb.accept(LocalAccountStore.authenticate(username, password)); return; }
		post(Account.class, new LoginRequest(username, password), "getaccountauth", cb);
	}

	public static void getAccount(String username, Consumer<Account> cb) {
		if (Settings.getConfig().isSinglePlayer()) { cb.accept(LocalAccountStore.find(username)); return; }
		post(Account.class, new LoginRequest(username, "cock"), "getaccount", cb);
	}

	public static void updatePunishments(Player player) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(player.getAccount(), "updatepunishments");
	}

	public static void updatePunishments(Player player, Consumer<Boolean> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(true); return; }
		post(Boolean.class, player.getAccount(), "updatepunishments", cb);
	}

	public static void updateRights(Player player) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(player.getAccount(), "updaterights");
	}

	public static void updateRights(Player player, Consumer<Boolean> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(true); return; }
		post(Boolean.class, player.getAccount(), "updaterights", cb);
	}

	public static void updateSocial(Player player) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(player.getAccount(), "updatesocial");
	}

	public static void updateSocial(Player player, Consumer<Boolean> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(true); return; }
		post(Boolean.class, player.getAccount(), "updatesocial", cb);
	}

	public static void updateFC(Player player, Consumer<FriendsChat> cb) {
		if (Settings.getConfig().isSinglePlayer()) { cb.accept(player.getSocial().getFriendsChat()); return; }
		post(FriendsChat.class, new UpdateFC(player.getDisplayName(), player.getSocial().getFriendsChat()), "updatefc", cb);
	}

	public static void forwardPackets(Player player, Packet... packets) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(Boolean.class, new PacketDto(player.getUsername(), packets), "forwardpackets", res -> {
			if (res != null && !res)
				player.sendMessage("Error forwarding packet to lobby.");
		});
	}

	public static void forwardPacket(Player player, Packet packet, Consumer<Boolean> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(true); return; }
		post(Boolean.class, new PacketDto(player.getUsername(), packet), "forwardpackets", cb);
	}

	public static void getClan(String clan, Consumer<Clan> cb) {
		if (Settings.getConfig().isSinglePlayer()) { cb.accept(null); return; }
		get(Clan.class, "clans/"+clan, cb);
	}

	public static void updateClan(Clan clan, Consumer<Clan> res) {
		if (Settings.getConfig().isSinglePlayer()) { res.accept(clan); return; }
		if (clan == null) {
			res.accept(null);
			return;
		}
		post(Clan.class, clan, "clans/update", res);
	}

	public static void post(Object body, String endpoint) {
		if (Settings.getConfig().isSinglePlayer()) return;
		post(null, body, endpoint, null);
	}
	
	public static <T> void get(Class<T> type, String endpoint, Consumer<T> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null) cb.accept(null); return; }
		APIUtil.get(type, "http://"+Settings.getConfig().getLobbyIp()+":4040/api/"+endpoint, Settings.getConfig().getLobbyApiKey(), cb);
	}

	public static <T> T getSync(Class<T> type, String endpoint) {
		if (Settings.getConfig().isSinglePlayer()) return null;
		return APIUtil.getSync(type, "http://"+Settings.getConfig().getLobbyIp()+":4040/api/"+endpoint, Settings.getConfig().getLobbyApiKey());
	}

	public static <T> void post(Class<T> type, Object body, String endpoint, Consumer<T> cb) {
		if (Settings.getConfig().isSinglePlayer()) { if (cb != null && type == Boolean.class) cb.accept(type.cast(Boolean.TRUE)); return; }
		APIUtil.post(type, body, "http://"+Settings.getConfig().getLobbyIp()+":4040/api/"+endpoint, Settings.getConfig().getLobbyApiKey(), cb);
	}

	public static <T> T postSync(Class<T> type, Object body, String endpoint) {
		if (Settings.getConfig().isSinglePlayer()) return type == Boolean.class ? type.cast(Boolean.TRUE) : null;
		return APIUtil.postSync(type, body, "http://"+Settings.getConfig().getLobbyIp()+":4040/api/"+endpoint, Settings.getConfig().getLobbyApiKey());
	}
}
