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
package com.rs.game.content.skills.cooking;

import com.rs.game.content.skills.util.CreateActionD;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.Constants;
import com.rs.lib.game.Item;

public class CookingCombos {

	public enum CookingCombo {
		PIE_SHELL(new Item(2315, 1), 1, 0, new Item[] { new Item(2313, 1), new Item(1953, 1) }),
		PART_MUD_PIE(new Item[] { new Item(7164, 1), new Item(1925, 1) }, 29, 0, new Item[] { new Item(2315, 1), new Item(6032, 1) }),
		PART_MUD_PIE2(new Item[] { new Item(7166, 1), new Item(1925, 1) }, 29, 0, new Item[] { new Item(7164, 1), new Item(1929, 1) }),
		RAW_MUD_PIE(new Item(7168, 1), 29, 0, new Item[] { new Item(7166, 1), new Item(434, 1) }),
		PART_GARDEN_PIE(new Item(7172, 1), 34, 0, new Item[] { new Item(2315, 1), new Item(1982, 1) }),
		PART_GARDEN_PIE2(new Item(7174, 1), 34, 0, new Item[] { new Item(7172, 1), new Item(1957, 1) }),
		RAW_GARDEN_PIE(new Item(7176, 1), 34, 0, new Item[] { new Item(7174, 1), new Item(1965, 1) }),
		PART_FISH_PIE(new Item(7182, 1), 47, 0, new Item[] { new Item(2315, 1), new Item(333, 1) }),
		PART_FISH_PIE2(new Item(7184, 1), 47, 0, new Item[] { new Item(7182, 1), new Item(339, 1) }),
		RAW_FISH_PIE(new Item(7186, 1), 47, 0, new Item[] { new Item(7184, 1), new Item(1942, 1) }),
		PART_ADMIRAL_PIE(new Item(7192, 1), 70, 0, new Item[] { new Item(2315, 1), new Item(329, 1) }),
		PART_ADMIRAL_PIE2(new Item(7194, 1), 70, 0, new Item[] { new Item(7192, 1), new Item(361, 1) }),
		RAW_ADMIRAL_PIE(new Item(7196, 1), 70, 0, new Item[] { new Item(7194, 1), new Item(1942, 1) }),
		PART_WILD_PIE(new Item(7202, 1), 85, 0, new Item[] { new Item(2315, 1), new Item(2136, 1) }),
		PART_WILD_PIE2(new Item(7204, 1), 85, 0, new Item[] { new Item(7202, 1), new Item(2876, 1) }),
		RAW_WILD_PIE(new Item(7206, 1), 85, 0, new Item[] { new Item(7204, 1), new Item(3226, 1) }),
		PART_SUMMER_PIE(new Item(7212, 1), 95, 0, new Item[] { new Item(2315, 1), new Item(5504, 1) }),
		PART_SUMMER_PIE2(new Item(7214, 1), 95, 0, new Item[] { new Item(7212, 1), new Item(5982, 1) }),
		RAW_SUMMER_PIE(new Item(7216, 1), 95, 0, new Item[] { new Item(7214, 1), new Item(1955, 1) }),
		UNCOOKED_APPLE_PIE(new Item(2317, 1), 30, 0, new Item[] { new Item(2315, 1), new Item(1955, 1) }),
		UNCOOKED_MEAT_PIE(new Item(2319, 1), 20, 0, new Item[] { new Item(2315, 1), new Item(2140, 1) }),
		UNCOOKED_BERRY_PIE(new Item(2321, 1), 10, 0, new Item[] { new Item(2315, 1), new Item(1951, 1) }),

		// Pizzas
		INCOMPLETE_PIZZA(new Item(2285, 1), 35, 0, new Item[] { new Item(1982, 1), new Item(2283, 1) }),
		UNCOOKED_PIZZA(new Item(2287, 1), 35, 0, new Item[] { new Item(2285, 1), new Item(1985, 1) }),
		MEAT_PIZZA_MEAT(new Item(2293, 1), 45, 26, new Item[] { new Item(2289, 1), new Item(2142, 1) }),
		MEAT_PIZZA_CHICKEN(new Item(2293, 1), 45, 26, new Item[] { new Item(2289, 1), new Item(2140, 1) }),
		ANCHOVY_PIZZA(new Item(2297, 1), 55, 39, new Item[] { new Item(2289, 1), new Item(319, 1) }),
		PINEAPPLE_PIZZA_RINGS(new Item(2301, 1), 65, 45, new Item[] { new Item(2289, 1), new Item(2118, 1) }),
		PINEAPPLE_PIZZA_CHUNKS(new Item(2301, 1), 65, 45, new Item[] { new Item(2289, 1), new Item(2116, 1) }),

		// Stew and Curry
		INCOMPLETE_STEW(new Item(1997, 1), 25, 0, new Item[] { new Item(1921, 1), new Item(1942, 1) }),
		UNCOOKED_STEW_MEAT(new Item(2001, 1), 25, 0, new Item[] { new Item(1997, 1), new Item(2142, 1) }),
		UNCOOKED_STEW_CHICKEN(new Item(2001, 1), 25, 0, new Item[] { new Item(1997, 1), new Item(2140, 1) }),
		UNCOOKED_CURRY_LEAVES(new Item(2009, 1), 60, 0, new Item[] { new Item(2001, 1), new Item(5970, 3) }),
		UNCOOKED_CURRY_SPICE(new Item(2009, 1), 60, 0, new Item[] { new Item(2001, 1), new Item(2007, 1) }),

		// Potato Toppings
		CHOPPED_GARLIC(new Item(7074, 1), 9, 0, new Item[] { new Item(1550, 1), new Item(1923, 1) }),
		SPICY_SAUCE(new Item(7072, 1), 9, 25, new Item[] { new Item(7074, 1), new Item(2169, 1) }),
		CHILLI_CON_CARNE(new Item(7062, 1), 11, 55, new Item[] { new Item(7072, 1), new Item(2142, 1) }),
		UNCOOKED_EGG(new Item(7076, 1), 13, 0, new Item[] { new Item(1944, 1), new Item(1923, 1) }),
		EGG_AND_TOMATO(new Item(7064, 1), 23, 50, new Item[] { new Item(7078, 1), new Item(1982, 1) }),
		CHOPPED_ONION(new Item(1871, 1), 42, 0, new Item[] { new Item(1923, 1), new Item(1957, 1) }),
		SLICED_MUSHROOMS(new Item(7080, 1), 1, 0, new Item[] { new Item(1923, 1), new Item(6004, 1) }),
		MUSHROOM_AND_ONIONS(new Item(7066, 1), 57, 120, new Item[] { new Item(7082, 1), new Item(7084, 1) }),
		TUNA_AND_CORN(new Item(7068, 1), 67, 204, new Item[] { new Item(7086, 1), new Item(5988, 1) }),
		CHOPPED_TUNA(new Item(7086, 1), 67, 0, new Item[] { new Item(1923, 1), new Item(361, 1) }),

		// Potato Combos
		POTATO_WITH_BUTTER(new Item(6703, 1), 39, 40, new Item[] { new Item(6701, 1), new Item(6697, 1) }),
		CHILLI_POTATO(new Item(7054, 1), 41, 15, new Item[] { new Item(7062, 1), new Item(6703, 1) }),
		POTATO_WITH_CHEESE(new Item(6705, 1), 47, 40, new Item[] { new Item(1985, 1), new Item(6703, 1) }),
		EGG_POTATO(new Item(7056, 1), 51, 45, new Item[] { new Item(7064, 1), new Item(6703, 1) }),
		MUSHROOM_AND_ONION_POTATO(new Item(7058, 1), 64, 55, new Item[] { new Item(7066, 1), new Item(6703, 1) }),
		TUNA_POTATO(new Item(7060, 1), 68, 10, new Item[] { new Item(7068, 1), new Item(6703, 1) }),

		// Dung Potato Combos
		GISSEL_POTATO(new Item(18095, 1), 1, 0, new Item[] { new Item(18093, 1), new Item(17819, 1) }),
		EDICAP_POTATO(new Item(18097, 1), 1, 0, new Item[] { new Item(18093, 1), new Item(17821, 1) }),
		HEIM_CRAB_POTATO(new Item(18099, 1), 3, 0, new Item[] { new Item(18093, 1), new Item(18159, 1) }),
		HEIM_CRAB_AND_GISSEL_POTATO(new Item(18119, 1), 6, 0, new Item[] { new Item(18099, 1), new Item(17819, 1) }),
		HEIM_CRAB_AND_EDICAP_POTATO(new Item(18139, 1), 9, 0, new Item[] { new Item(18099, 1), new Item(17821, 1) }),
		RED_EYE_POTATO(new Item(18101, 1), 13, 0, new Item[] { new Item(18093, 1), new Item(18161, 1) }),
		RED_EYE_AND_GISSEL_POTATO(new Item(18121, 1), 16, 0, new Item[] { new Item(18101, 1), new Item(17819, 1) }),
		RED_EYE_AND_EDICAP_POTATO(new Item(18141, 1), 19, 0, new Item[] { new Item(18101, 1), new Item(17821, 1) }),
		DUSK_EEL_POTATO(new Item(18103, 1), 23, 0, new Item[] { new Item(18093, 1), new Item(18163, 1) }),
		DUSK_EEL_AND_GISSEL_POTATO(new Item(18123, 1), 26, 0, new Item[] { new Item(18103, 1), new Item(17819, 1) }),
		DUSK_EEL_AND_EDICAP_POTATO(new Item(18143, 1), 29, 0, new Item[] { new Item(18103, 1), new Item(17821, 1) }),
		GIANT_FLATFISH_POTATO(new Item(18105, 1), 33, 0, new Item[] { new Item(18093, 1), new Item(18165, 1) }),
		FLATFISH_AND_GISSEL_POTATO(new Item(18125, 1), 36, 0, new Item[] { new Item(18105, 1), new Item(17819, 1) }),
		FLATFISH_AND_EDICAP_POTATO(new Item(18145, 1), 39, 0, new Item[] { new Item(18105, 1), new Item(17821, 1) }),
		SHORT_FIN_EEL_POTATO(new Item(18107, 1), 43, 0, new Item[] { new Item(18093, 1), new Item(18167, 1) }),
		SHORT_FIN_AND_GISSEL_POTATO(new Item(18127, 1), 46, 0, new Item[] { new Item(18107, 1), new Item(17819, 1) }),
		SHORT_FIN_AND_EDICAP_POTATO(new Item(18147, 1), 49, 0, new Item[] { new Item(18107, 1), new Item(17821, 1) }),
		SNIPPER_POTATO(new Item(18109, 1), 53, 0, new Item[] { new Item(18093, 1), new Item(18169, 1) }),
		SNIPPER_AND_GISSEL_POTATO(new Item(18129, 1), 56, 0, new Item[] { new Item(18109, 1), new Item(17819, 1) }),
		SNIPPER_AND_EDICAP_POTATO(new Item(18149, 1), 59, 0, new Item[] { new Item(18109, 1), new Item(17821, 1) }),
		BOULDABASS_POTATO(new Item(18111, 1), 63, 0, new Item[] { new Item(18093, 1), new Item(18171, 1) }),
		BOULDABASS_AND_GISSEL_POTATO(new Item(18131, 1), 66, 0, new Item[] { new Item(18111, 1), new Item(17819, 1) }),
		BOULDABASS_AND_EDICAP_POTATO(new Item(18151, 1), 69, 0, new Item[] { new Item(18111, 1), new Item(17821, 1) }),
		SALVE_EEL_POTATO(new Item(18113, 1), 73, 0, new Item[] { new Item(18093, 1), new Item(18173, 1) }),
		SALVE_EEL_AND_GISSEL_POTATO(new Item(18133, 1), 76, 0, new Item[] { new Item(18113, 1), new Item(17819, 1) }),
		SALVE_EEL_AND_EDICAP_POTATO(new Item(18153, 1), 79, 0, new Item[] { new Item(18113, 1), new Item(17821, 1) }),
		BLUE_CRAB_POTATO(new Item(18115, 1), 83, 0, new Item[] { new Item(18093, 1), new Item(18175, 1) }),
		BLUE_CRAB_AND_GISSEL_POTATO(new Item(18135, 1), 86, 0, new Item[] { new Item(18115, 1), new Item(17819, 1) }),
		BLUE_CRAB_AND_EDICAP_POTATO(new Item(18155, 1), 89, 0, new Item[] { new Item(18115, 1), new Item(17821, 1) }),
		CAVE_MORAY_POTATO(new Item(18117, 1), 93, 0, new Item[] { new Item(18093, 1), new Item(18177, 1) }),
		MORAY_AND_GISSEL_POTATO(new Item(18137, 1), 96, 0, new Item[] { new Item(18117, 1), new Item(17819, 1) }),
		MORAY_AND_EDICAP_POTATO(new Item(18157, 1), 99, 0, new Item[] { new Item(18117, 1), new Item(17821, 1) });


		private final Item[] product;
		private final int req;
		private final double xp;
		private final Item[] materials;

		private CookingCombo(Item product, int req, double xp, Item[] materials) {
			this.product = new Item[] { product };
			this.req = req;
			this.xp = xp;
			this.materials = materials;
		}

		private CookingCombo(Item[] product, int req, double xp, Item[] materials) {
			this.product = product;
			this.req = req;
			this.xp = xp;
			this.materials = materials;
		}

		public static CookingCombo forMaterials(Item i1, Item i2) {
			for (CookingCombo c : CookingCombo.values())
				if ((c.materials[0].getId() == i1.getId() || c.materials[1].getId() == i1.getId()) && (c.materials[0].getId() == i2.getId() || c.materials[1].getId() == i2.getId()))
					return c;
			return null;
		}
	}

	public static boolean handleCombos(Player player, Item used, Item usedWith) {
		CookingCombo combo = CookingCombo.forMaterials(used, usedWith);
		if (combo != null) {
			if (player.getSkills().getLevel(Constants.COOKING) < combo.req) {
				player.sendMessage("You need a cooking level of " + combo.req + " to make that.");
				return true;
			}
			if (!player.getInventory().containsItems(combo.materials))
				return true;
			player.startConversation(new CreateActionD(player, new Item[][] { combo.materials }, new Item[][] { combo.product }, new double[] { combo.xp}, new int[] { -1 }, new int[] { combo.req }, Constants.COOKING, 0));
			return true;
		}
		return false;
	}

}
