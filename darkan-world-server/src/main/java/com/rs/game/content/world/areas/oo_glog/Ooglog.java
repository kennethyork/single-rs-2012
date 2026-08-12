package com.rs.game.content.world.areas.oo_glog;

import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class Ooglog {

    public static ObjectClickHandler rockPassage = new ObjectClickHandler(new Object[] { 29099 }, e -> {
        if (e.getPlayer().getY() > e.getObj().getY()) e.getPlayer().tele(e.getObj().getTile().transform(1, -1, 0));
        else e.getPlayer().tele(e.getObj().getTile().transform(1, 1, 0));
    });

}
