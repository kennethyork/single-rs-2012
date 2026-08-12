package com.rs.game.content.skills.crafting;

import com.rs.game.content.skills.util.CreateActionD;
import com.rs.lib.Constants;
import com.rs.lib.game.Item;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemOnItemHandler;

@PluginEventHandler
public class LightOrbCrafting  {

	private static final Item[][] materials = { { new Item(10980), new Item(10981) } };
	private static final Item[][] products = { { new Item(10973) } };
	private static final int[] reqs = { 87 };
	private static final double[] xp = { 104 };
	private static final int[] anims = { 886 };


	public static ItemOnItemHandler craftLightOrb = new ItemOnItemHandler(10980, new int[] { 10981 }, e -> e.getPlayer().startConversation(new CreateActionD(e.getPlayer(), materials, products, xp, anims, reqs, Constants.CRAFTING, 2)));
}
