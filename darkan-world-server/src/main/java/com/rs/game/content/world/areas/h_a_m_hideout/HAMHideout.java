package com.rs.game.content.world.areas.h_a_m_hideout;

import com.rs.game.World;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.game.model.gameobject.GameObject;
import com.rs.lib.Constants;
import com.rs.lib.game.Tile;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import com.rs.utils.Ticks;
import static com.rs.engine.variables.VarBitPlayer.ham_thief;

@PluginEventHandler
public class HAMHideout {
    public static ObjectClickHandler handleHAMEntranceTrapdoor = new ObjectClickHandler(new Object[]{ 5490, 5492 }, Tile.of(3165, 3252, 0), e -> {
        switch (e.getOption()) {
            case "Open" -> e.getPlayer().sendMessage("The trapdoor is securely locked");
            case "Close" -> e.getPlayer().getVars().setVarBit(ham_thief, 0);
            case "Climb-down" -> e.getPlayer().ladder(Tile.of(3149, 9652, 0));
            case "Pick-lock" -> PickLock(e.getPlayer(), e.getObj());
        }
    });

    public static ObjectClickHandler hamJailDoor = new ObjectClickHandler(new Object[]{5501}, Tile.of(3183, 9611, 0), e ->{
        switch (e.getOption()) {
            case "Open" -> e.getPlayer().sendMessage("The door is securely locked");
            case "Pick-lock" -> {
                e.getPlayer().faceObject(e.getObj());
                if(e.getPlayer().getX() >= 3183) {
                    double hasLockpickRate;
                    e.getPlayer().lock();
                    if (e.getPlayer().getInventory().containsOneItem(1523, 11682)) {
                        hasLockpickRate = 1.3;
                    } else {
                        hasLockpickRate = 1.0;
                    }
                    e.getPlayer().anim(832);

                    if (Utils.skillSuccess(e.getPlayer().getSkills().getLevel(Skills.THIEVING), hasLockpickRate, 190, 190)) {
                        e.getPlayer().getSkills().addXp(Constants.THIEVING, 0.5);
                        World.removeObjectTemporary(e.getObj(), Ticks.fromSeconds(9));
                        World.spawnObjectTemporary(new GameObject(e.getObjectId() + 1, e.getObj().getType(), e.getObj().getRotation()+1, e.getObj().getTile().transform(-1, 0, 0)), Ticks.fromSeconds(10), true);
                        e.getPlayer().unlock();
                    } else {
                        e.getPlayer().sendMessage("You fail to pick the lock.");
                        e.getPlayer().unlock();
                    }
                }
                else {
                    e.getPlayer().faceObject(e.getObj());
                    e.getPlayer().anim(832);
                    e.getPlayer().sendMessage("You fail to pick the lock.");
                    e.getPlayer().unlock();
                }
            }
        }
    });

    private static void PickLock(Player player, GameObject object) {
        double hasLockpickRate;
        player.lock();
        if (player.getInventory().containsOneItem(1523, 11682)) {
            hasLockpickRate = 1.3;
        } else {
            hasLockpickRate = 1.0;
        }
        player.faceObject(object);
        player.anim(832);

        if (Utils.skillSuccess(player.getSkills().getLevel(Skills.THIEVING), hasLockpickRate, 190, 190)) {
            player.getSkills().addXp(Constants.THIEVING, 0.5);
            player.getVars().setVarBit(ham_thief, 1);
            player.unlock();
        } else {
            player.sendMessage("You fail to pick the lock.");
            player.unlock();
        }
    }

    public static ObjectClickHandler handleLadder = new ObjectClickHandler(new Object[] { 5493 }, e -> e.getPlayer().ladder(Tile.of(3165, 3251, 0)));

}
