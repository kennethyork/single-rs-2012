package com.rs.game.content.holidayevents.easter.easter22;

import com.rs.cache.loaders.ObjectType;
import com.rs.engine.dialogue.Dialogue;
import com.rs.game.World;
import com.rs.game.content.holidayevents.easter.easter22.npcs.EasterChick;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.gameobject.GameObject;
import com.rs.game.tasks.WorldTasks;
import com.rs.lib.game.Animation;
import com.rs.lib.game.SpotAnim;
import com.rs.lib.game.Tile;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import kotlin.Pair;

@PluginEventHandler
public class EventEasterEgg extends GameObject {
	
	private boolean found = false;
	private String hint;

	public EventEasterEgg(int id, ObjectType type, int rotation, int x, int y, int plane) {
		super(id, type, rotation, x, y, plane);
	}
	
	public void setHint(String loc) {
		hint = loc;
	}
	
	public String getHint() {
		return hint;
	}
	
	public void setFound(boolean spotted) {
		found = spotted;
	}
	
	public boolean getFound() {
		return found;
	}

	private void spotted(Player p) {
		if (getFound() == false)
			World.sendWorldMessage("<col=ffff00>" + p.getDisplayName() + " has found an egg " + getHint(), false);
		setFound(true);
	}
	
    public static ObjectClickHandler crackEgg = new ObjectClickHandler(false, new Object[] { 70106, 70107, 70108, 70109, 70110 }, e -> {
    	if (!(e.getObj() instanceof EventEasterEgg egg))
    		return;
    	if (!Easter2022.ENABLED)
    		return;
        if (e.getOption().equals("Crack")) {
            if (e.getPlayer().getEquipment().getWeaponId() != Easter2022.EGGSTERMINATOR && e.getPlayer().getEquipment().getWeaponId() != Easter2022.PERMANENT_EGGSTERMINATOR) {
                e.getPlayer().sendMessage("You need the Eggsterminator to crack open this egg. Speak with the Evil Chicken or the Chocatrice in Varrock Square.");
                return;
            }
            e.getPlayer().lock();
            e.getPlayer().resetWalkSteps();
        	e.getPlayer().faceObject(e.getObj());
            e.getPlayer().setNextAnimation(new Animation(12174));
            e.getPlayer().setNextSpotAnim(new SpotAnim(2138));
            int attackStyle = e.getPlayer().getCombatDefinitions().getAttackStyleId();
            int delay = World.sendProjectile(e.getPlayer().getTile(), Tile.of(e.getObj().getTile()), (attackStyle == 0 ? 3034 : 3035), new Pair<>(20, 10), 30, 5, 0).getTaskDelay();
    		int npcId = (attackStyle == 0 ? Easter2022.CHICK : Easter2022.CHOCOCHICK);
    		egg.spotted(e.getPlayer());
    		WorldTasks.scheduleTimer(delay, (tick) -> {
            	switch (tick) {
            		case 0 -> { 
            			e.getPlayer().getVars().saveVarBit(e.getObj().getDefinitions().varpBit, (attackStyle == 0 ? 2 : 1));
                        e.getPlayer().getPackets().sendObjectAnimation(e.getObj(), new Animation(16432));
            		}
            		case 2 -> {
                        EasterChick npc = new EasterChick(e.getPlayer(), npcId, World.getFreeTile(Tile.of(e.getObj().getX(), e.getObj().getY(), e.getObj().getPlane()), 2), e.getObj().getDefinitions().varpBit);
                        e.getPlayer().startConversation(new Dialogue().addItem(Easter2022.PERMANENT_EGGSTERMINATOR, "You shatter the egg with the Eggsterminator. A " + npc.getName().toLowerCase() + " appears."));
                        e.getPlayer().sendMessage("You shatter the egg with the Eggsterminator. A " + npc.getName().toLowerCase() + " appears.");
                        if (e.getPlayer().getI(Easter2022.STAGE_KEY+"CurrentHunt", 0) != EggHunt.getHunt())
                            e.getPlayer().set(Easter2022.STAGE_KEY+"CurrentHunt", EggHunt.getHunt());
                        e.getPlayer().unlock();
                        return false;
            		}
            	}
                return true;
            });
        }
    });
}
