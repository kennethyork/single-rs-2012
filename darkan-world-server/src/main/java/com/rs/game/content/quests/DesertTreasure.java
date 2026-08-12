package com.rs.game.content.quests;

import com.rs.game.model.entity.BodyGlow;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemEquipHandler;
import static com.rs.engine.variables.VarBitPlayer.fd_ladder_present;

@PluginEventHandler
public class DesertTreasure {
	
	public static ItemEquipHandler ringOfVisibility = new ItemEquipHandler(4657, e -> {
		e.getPlayer().setNextBodyGlow(new BodyGlow(1, 0, 0, 0, 128));
		e.getPlayer().getVars().setVarBit(fd_ladder_present, e.equip() ? 1 : 0);
	});
}
