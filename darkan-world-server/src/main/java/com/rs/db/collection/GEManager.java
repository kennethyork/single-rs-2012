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

import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonIOException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.rs.game.ge.Offer;
import com.rs.Settings;
import com.rs.db.local.LocalFileStore;
import com.rs.game.ge.Offer.State;
import com.rs.lib.db.DBItemManager;
import com.rs.lib.file.JsonFileManager;
import com.rs.lib.util.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.Comparator;
import java.util.stream.Collectors;

public class GEManager extends DBItemManager {
	private final Object localLock = new Object();

	public GEManager() {
		super("grandexchange");
	}

	@Override
	public void initCollection() {
		getDocs().createIndex(Indexes.text("owner"));
		getDocs().createIndex(Indexes.compoundIndex(Indexes.ascending("itemId"), Indexes.ascending("completedAmount")));
		getDocs().createIndex(Indexes.compoundIndex(Indexes.ascending("itemId"), Indexes.ascending("state")));
		getDocs().createIndex(Indexes.compoundIndex(Indexes.ascending("itemId"), Indexes.ascending("price")));
		getDocs().createIndex(Indexes.compoundIndex(Indexes.ascending("itemId"), Indexes.ascending("selling")));
	}

	public void get(String username, Consumer<List<Offer>> func) {
		if (Settings.getConfig().isSinglePlayer()) { func.accept(getSync(username)); return; }
		execute(() -> func.accept(getSync(username)));
	}

	public void save(Offer offer) {
		save(offer, null);
	}

	public void save(Offer offer, Runnable done) {
		if (Settings.getConfig().isSinglePlayer()) {
			saveSync(offer);
			if (done != null) done.run();
			return;
		}
		execute(() -> {
			saveSync(offer);
			if (done != null)
				done.run();
		});
	}

	public void saveSync(Offer offers) {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				List<Offer> all = loadLocalOffers();
				all.removeIf(offer -> offer.getOwner().equals(offers.getOwner()) && offer.getBox() == offers.getBox());
				all.add(offers);
				saveLocalOffers(all);
			}
			return;
		}
		getDocs().findOneAndReplace(Filters.and(Filters.eq("owner", offers.getOwner()), Filters.eq("box", offers.getBox())), Document.parse(JsonFileManager.toJson(offers)), new FindOneAndReplaceOptions().upsert(true));
	}

	public List<Offer> getSync(String username) {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				return loadLocalOffers().stream().filter(offer -> offer.getOwner().equals(username)).collect(Collectors.toList());
			}
		}
		FindIterable<Document> offerDocs = getDocs().find(Filters.eq("owner", username));
		if (offerDocs == null)
			return null;
		try {
			List<Offer> offers = new ArrayList<>(6);
			for (Document d : offerDocs) {
				Offer offer = JsonFileManager.fromJSONString(JsonFileManager.toJson(d), Offer.class);
				offers.add(offer);
			}
			return offers;
		} catch (JsonIOException | IOException e) {
			Logger.handle(GEManager.class, "getSync", e);
			return null;
		}
	}

	public void remove(String username, int box, Runnable done) {
		if (Settings.getConfig().isSinglePlayer()) {
			removeSync(username, box);
			if (done != null) done.run();
			return;
		}
		execute(() -> {
			removeSync(username, box);
			if (done != null)
				done.run();
		});
	}

	public void removeSync(String username, int box) {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				List<Offer> all = loadLocalOffers();
				all.removeIf(offer -> offer.getOwner().equals(username) && offer.getBox() == box);
				saveLocalOffers(all);
			}
			return;
		}
		getDocs().findOneAndDelete(Filters.and(Filters.eq("owner", username), Filters.eq("box", box)));
	}

	public List<Offer> getBestOffersSync(Offer other) {
		if (other.getState() == State.FINISHED)
			return null;
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				return loadLocalOffers().stream()
						.filter(offer -> offer.getState() == State.STABLE && offer.getItemId() == other.getItemId() && offer.isSelling() != other.isSelling())
						.filter(offer -> other.isSelling() ? offer.getPrice() >= other.getPrice() : offer.getPrice() <= other.getPrice())
						.sorted(Comparator.comparingInt(Offer::getPrice))
						.collect(Collectors.toList());
			}
		}
		List<Offer> result = new ArrayList<>();
		FindIterable<Document> docs = getDocs().find(Filters.and(Filters.eq("state", State.STABLE.toString()), Filters.eq("itemId", other.getItemId()), Filters.eq("selling", !other.isSelling()), other.isSelling() ? Filters.gte("price", other.getPrice()) : Filters.lte("price", other.getPrice()))).sort(Sorts.ascending("price"));
		for (Document doc : docs)
			try {
				Offer offer = JsonFileManager.fromJSONString(JsonFileManager.toJson(doc), Offer.class);
				result.add(offer);
			} catch (JsonIOException | IOException e) {
				Logger.handleNoRecord(GEManager.class, "getBestOffersSync", "Error converting document: " + result, e);
			}
		return result;
	}

	public void getBestOffer(int itemId, boolean sell, Consumer<Offer> func) {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				Comparator<Offer> order = Comparator.comparingInt(Offer::getPrice);
				if (!sell) order = order.reversed();
				func.accept(loadLocalOffers().stream()
						.filter(offer -> offer.getState() == State.STABLE && offer.getItemId() == itemId && offer.isSelling() != sell)
						.sorted(order).findFirst().orElse(null));
			}
			return;
		}
		execute(() -> {
			FindIterable<Document> docs = getDocs().find(Filters.and(Filters.eq("state", State.STABLE.toString()), Filters.eq("itemId", itemId), Filters.eq("selling", !sell))).sort(sell ? Sorts.ascending("price") : Sorts.descending("price"));
			try {
				func.accept(JsonFileManager.fromJSONString(JsonFileManager.toJson(docs.first()), Offer.class));
			} catch (JsonIOException | IOException e) {
				func.accept(null);
			}
		});
	}

	public void getAllOffersOfType(boolean selling, Consumer<List<Offer>> func) {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				func.accept(loadLocalOffers().stream().filter(offer -> offer.getState() == State.STABLE && offer.isSelling() == selling).collect(Collectors.toList()));
			}
			return;
		}
		execute(() -> {
			List<Offer> result = new ArrayList<>();
			FindIterable<Document> docs = getDocs().find(Filters.and(Filters.eq("state", State.STABLE.toString()), Filters.eq("selling", selling)));
			for (Document doc : docs)
				try {
					Offer offer = JsonFileManager.fromJSONString(JsonFileManager.toJson(doc), Offer.class);
					result.add(offer);
				} catch (JsonIOException | IOException e) {
					Logger.handleNoRecord(GEManager.class, "getAllOffersOfType", "Error converting document: " + result, e);
				}
			func.accept(result);
		});
	}

	public List<Offer> getAllStableOffersSync() {
		if (Settings.getConfig().isSinglePlayer()) {
			synchronized (localLock) {
				return loadLocalOffers().stream().filter(offer -> offer.getState() == State.STABLE).collect(Collectors.toList());
			}
		}
		List<Offer> result = new ArrayList<>();
		FindIterable<Document> docs = getDocs().find(Filters.eq("state", State.STABLE.toString()));
		for (Document doc : docs)
			try {
				result.add(JsonFileManager.fromJSONString(JsonFileManager.toJson(doc), Offer.class));
			} catch (JsonIOException | IOException e) {
				Logger.handleNoRecord(GEManager.class, "getAllStableOffersSync", "Error converting offer document", e);
			}
		return result;
	}

	@Override
	public void execute(Runnable task) {
		if (Settings.getConfig().isSinglePlayer()) task.run();
		else super.execute(task);
	}

	private List<Offer> loadLocalOffers() {
		String json = LocalFileStore.read("ge/offers.json");
		if (json == null || json.trim().isEmpty()) return new ArrayList<>();
		try {
			List<Offer> offers = JsonFileManager.getGson().fromJson(json, new TypeToken<List<Offer>>() { }.getType());
			return offers == null ? new ArrayList<>() : new ArrayList<>(offers);
		} catch (Throwable error) {
			Logger.handle(GEManager.class, "loadLocalOffers", "Unable to read the local Grand Exchange", error);
			return new ArrayList<>();
		}
	}

	private void saveLocalOffers(List<Offer> offers) {
		LocalFileStore.writeAtomic("ge/offers.json", JsonFileManager.toJson(offers));
	}

}
