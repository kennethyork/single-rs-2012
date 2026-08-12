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
package com.rs.db.collection;

import com.google.gson.JsonIOException;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.Indexes;
import com.rs.game.World;
import com.rs.Settings;
import com.rs.db.local.LocalFileStore;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.db.DBItemManager;
import com.rs.lib.file.JsonFileManager;
import com.rs.lib.util.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class PlayerManager extends DBItemManager {

	public PlayerManager() {
		super("players");
	}

	@Override
	public void initCollection() {
		getDocs().createIndex(Indexes.text("username"));
	}

	/**
	 * Result of an asynchronous player lookup for login. {@code loadError} is set when an
	 * existing player document could not be read/decoded from Mongo (e.g. a corrupt or
	 * schema-drifted save). This must be kept distinct from a null player (which legitimately
	 * means "no save yet, create a fresh player") so a decode failure never silently wipes a
	 * character, and the client gets a proper error instead of hanging.
	 */
	public record PlayerResult(Player player, boolean loadError) {
		public static PlayerResult of(Player player) {
			return new PlayerResult(player, false);
		}

		public static PlayerResult error() {
			return new PlayerResult(null, true);
		}
	}

	public void getByUsername(String username, Consumer<Player> func) {
		if (Settings.getConfig().isSinglePlayer()) { func.accept(getSyncUsername(username)); return; }
		execute(() -> func.accept(getSyncUsername(username)));
	}

	/**
	 * Loads a player for login, reporting a decode failure separately from "not found" via
	 * {@link PlayerResult}.
	 */
	public void getByUsernameForLogin(String username, Consumer<PlayerResult> func) {
		if (Settings.getConfig().isSinglePlayer()) {
			try { func.accept(PlayerResult.of(loadAndDecodeUsername(username))); }
			catch (Throwable e) {
				Logger.handle(PlayerManager.class, "getByUsernameForLogin", "Failed to load local player: " + username, e);
				func.accept(PlayerResult.error());
			}
			return;
		}
		execute(() -> {
			try {
				func.accept(PlayerResult.of(loadAndDecodeUsername(username)));
			} catch (Throwable e) {
				Logger.handle(PlayerManager.class, "getByUsernameForLogin", "Failed to load player: " + username, e);
				func.accept(PlayerResult.error());
			}
		});
	}

	public void save(Player player) {
		save(player, null);
	}

	public void save(Player account, Runnable done) {
		if (Settings.getConfig().isSinglePlayer()) {
			saveSync(account);
			if (done != null) done.run();
			return;
		}
		execute(() -> {
			saveSync(account);
			if (done != null)
				done.run();
		});
	}

	public void saveSync(Player account) {
		try {
			if (Settings.getConfig().isSinglePlayer()) {
				LocalFileStore.writeAtomic("players/" + account.getUsername() + ".json", JsonFileManager.toJson(account));
				return;
			}
			getDocs().findOneAndReplace(eq("username", account.getUsername()), Document.parse(JsonFileManager.toJson(account)), new FindOneAndReplaceOptions().upsert(true));
		} catch(Throwable e) {
			Logger.handle(PlayerManager.class, "saveSync", "Error saving player: " + account.getUsername(), e);
		}
	}

	/**
	 * Loads and decodes a player, propagating any decode failure so callers can distinguish
	 * "not found" from "failed to decode". Everything that just wants a best-effort lookup
	 * should use the null-safe {@link #getSyncUsername(String)}.
	 */
	private Player loadAndDecodeUsername(String username) throws JsonIOException, IOException {
		Player loggedIn = World.getPlayerByUsername(username);
		if (loggedIn != null)
			return loggedIn;
		if (Settings.getConfig().isSinglePlayer()) {
			String json = LocalFileStore.read("players/" + username + ".json");
			return json == null ? null : JsonFileManager.fromJSONString(json, Player.class);
		}
		Document accDoc = getDocs().find(eq("username", username)).first();
		if (accDoc == null)
			return null;
		return JsonFileManager.fromJSONString(JsonFileManager.toJson(accDoc), Player.class);
	}

	public Player getSyncUsername(String username) {
		try {
			return loadAndDecodeUsername(username);
		} catch (Throwable e) {
			Logger.handle(PlayerManager.class, "getSyncUsername", "Failed to load player: " + username, e);
			return null;
		}
	}

	public boolean usernameExists(String username) {
		return getSyncUsername(username) == null;
	}

}
