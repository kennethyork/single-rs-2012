package com.rs.game.content.items.combinations;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemClickHandler;
import com.rs.plugin.handlers.ItemOnItemHandler;

import java.util.HashMap;
import java.util.Map;

@PluginEventHandler
public class CombinationItems {
    private enum Combineable {
        CAP_AND_GOGGLES(9472, 9945, 9946),
        AMULET_OF_FURY_OR(6585, 19333, 19335),
        DRAGON_FULL_HELM_OR(11335, 19346, 19336),
        DRAGON_PLATEBODY_OR(14479, 19350, 19337),
        DRAGON_PLATELEGS_OR(4087, 19348, 19338),
        DRAGON_PLATESKIRT_OR(4585, 19348, 19339),
        DRAGON_SQUARE_SHIELD_OR(1187, 19352, 19340),
        DRAGON_FULL_HELM_SP(11335, 19354, 19341),
        DRAGON_PLATEBODY_SP(14479, 19358, 19342),
        DRAGON_PLATELEGS_SP(4087, 19356, 19343),
        DRAGON_PLATESKIRT_SP(4585, 19356, 19344),
        DRAGON_SQ_SHIELD_SP(1187, 19360, 19345),
        ABYSSAL_VINE_WHIP(4151, 21369, 21371),
        ABYSSAL_VINE_WHIP_YELLOW(15441, 21369, 21372),
        ABYSSAL_VINE_WHIP_BLUE(15442, 21369, 21373),
        ABYSSAL_VINE_WHIP_WHITE(15443, 21369, 21374),
        ABYSSAL_VINE_WHIP_GREEN(15444, 21369, 21375),
        DRAGONBONE_MAGE_HAT(6918, 24352, 24354),
        DRAGONBONE_MAGE_TOP(6916, 24352, 24355),
        DRAGONBONE_MAGE_BOTTOMS(6924, 24352, 24356),
        DRAGONBONE_MAGE_GLOVES(6922, 24352, 24357),
        DRAGONBONE_MAGE_BOOTS(6920, 24352, 24358),
        DRAGONBONE_FULL_HELM(11335, 24352, 24359),
        DRAGONBONE_PLATEBODY(14479, 24352, 24360),
        DRAGONBONE_GLOVES(13006, 24352, 24361),
        DRAGONBONE_BOOTS(11732, 24352, 24362),
        DRAGONBONE_PLATELEGS(4087, 24352, 24363),
        DRAGONBONE_PLATESKIRT(4585, 24352, 24364),
        DRAGON_KITESHIELD_OR(24365, 25312, 25320),
        DRAGON_KITESHIELD_SP(24365, 25314, 25321),
        ARMADYL_GODSWORD(11702, 11690, 11694),
        BANDOS_GODSWORD(11704, 11690, 11696),
        SARADOMIN_GODSWORD(11706, 11690, 11698),
        ZAMORAK_GODSWORD(11708, 11690, 11700),
        MAPLE_LONGBOW_SIGHTED(851, 18330, 18331),
        MAGIC_LONGBOW_SIGHTED(859, 18330, 18332),
        SKULL_SCEPTRE_SKULL(9007, 9008, 9009),
        SKULL_SCEPTRE_SCEPTRE(9010, 9011, 9012),
        SKULL_SCEPTRE_SKULL_SCEPTRE(9012, 9009, 9013),
        CRYSTAL_KEY(985, 987, 989),
        TEA_BOWL_TO_MILKY_TEA_BOWL(4239, 1927, 4240, 1925),         // Tea bowl + milk bucket -> Milky tea bowl + empty bucket
        TEA_BOWL_TO_CUP(4239, 1981, 4242, 1923),                    // Tea bowl + empty cup -> Tea cup + empty bowl
        TEA_BOWL_TO_PORCELAIN_CUP(4239, 4244, 4245, 1923),          // Tea bowl + empty porcelain cup -> Porcelain cup + empty bowl
        TEA_BOWL_MILKY_TO_CUP(4240, 1981, 4243, 1923),              // Milky tea bowl + empty cup -> Milky tea cup + empty bowl
        TEA_BOWL_MILKY_TO_PORCELAIN_CUP(4240, 4244, 4246, 1923),    // Milky tea bowl + empty porcelain cup -> Milky porcelain cup + empty bowl
        TEA_CUP_TO_MILKY_TEA_CUP(4242, 1927, 4243, 1925),           // Tea cup + milk bucket -> Milky tea cup + empty bucket
        TEA_CUP_TO_PORCELAIN_CUP(4242, 4244, 4245, 1981),           // Tea cup + empty porcelain cup -> Porcelain tea cup + empty cup
        TEA_CUP_MILKY_TO_PORCELAIN_CUP(4243, 4244, 4246, 1981),     // Milky tea cup + empty porcelain cup -> Milky porcelain cup + empty cup
        TEA_PORCELAIN_TO_MILKY_PORCELAIN(4245, 1927, 4246, 1925);   // Porcelain cup + milk bucket -> Milky porcelain cup + empty bucket


        private static final Map<Integer, Combineable> BY_PRODUCT = new HashMap<>();
        private static final Map<Integer, Combineable> BY_COMPONENT = new HashMap<>();

        static {
            for (Combineable c : Combineable.values()) {
                BY_PRODUCT.put(c.resultId1, c);
                if (c.resultId2 != -1) {
                    BY_PRODUCT.put(c.resultId2, c);
                }
                BY_COMPONENT.put((c.item1 << 16) + c.item2, c);
            }
        }
        private final int item1;
        private final int item2;
        private final int resultId1;
        private final int resultId2;

        Combineable(int item1, int item2, int resultId1) {
            this(item1, item2, resultId1, -1);
        }

        Combineable(int item1, int item2, int resultId1, int resultId2) {
            this.item1 = item1;
            this.item2 = item2;
            this.resultId1 = resultId1;
            this.resultId2 = resultId2;
        }
    }

    public static ItemOnItemHandler combine = new ItemOnItemHandler(true, Combineable.BY_COMPONENT.keySet().toArray(), e -> {
        Combineable combineable = Combineable.BY_COMPONENT.get((e.getItem1().getId() << 16) + e.getItem2().getId());
        if (combineable == null)
            combineable = Combineable.BY_COMPONENT.get((e.getItem2().getId() << 16) + e.getItem1().getId());
        if (combineable == null)
            return;
        e.getPlayer().getInventory().deleteItem(e.getItem1());
        e.getPlayer().getInventory().deleteItem(e.getItem2());
        e.getPlayer().getInventory().addItem(combineable.resultId1, 1);
        if (combineable.resultId2 != -1) {
            e.getPlayer().getInventory().addItem(combineable.resultId2, 1);
        }
    });

    public static ItemClickHandler split = new ItemClickHandler(Combineable.BY_PRODUCT.keySet().stream().filter(prod -> ItemDefinitions.getDefs(prod).containsOption("Split") || ItemDefinitions.getDefs(prod).containsOption("Dismantle")).toArray(), new String[] { "Split", "Dismantle" }, e -> {
        Combineable combineable = Combineable.BY_PRODUCT.get(e.getItem().getId());
        if (combineable == null)
            return;
        if (!e.getPlayer().getInventory().hasFreeSlots()) {
            e.getPlayer().sendMessage("You don't have enough inventory space.");
            return;
        }
        e.getPlayer().getInventory().deleteItem(e.getItem());
        e.getPlayer().getInventory().addItem(combineable.item1, 1);
        e.getPlayer().getInventory().addItem(combineable.item2, 1);
    });
}

    /**
     * if (itemUsed == 21358 && usedWith == 21359 || usedWith == 21358 && itemUsed == 21359) {
     * 			if (player.getInventory().containsItem(21359, 2) && player.getSkills().getLevel(Constants.FLETCHING) >= 72) {
     * 				player.getInventory().deleteItem(21358, 1);
     * 				player.getInventory().deleteItem(21359, 2); // BOLAS
     * 				player.getInventory().addItem(21365, 1);
     * 				player.getSkills().addXp(Constants.FLETCHING, 25);
     *                        } else
     * 				player.sendMessage("You need 2 excressence, 1 mutated vine, and 72 fletching to create bolas.");
     * 			return true;* 		}
     * 		//spotanim 450 sagie
     */

