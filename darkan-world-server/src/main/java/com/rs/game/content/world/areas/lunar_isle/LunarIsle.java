package com.rs.game.content.world.areas.lunar_isle;

import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.engine.quest.Quest;
import com.rs.engine.quest.QuestManager;
import com.rs.game.content.skills.magic.Magic;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Equipment;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Tile;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.NPCClickHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import com.rs.utils.shop.ShopsHandler;

@PluginEventHandler
public class LunarIsle {

    public static NPCClickHandler enterBabaYagaHouse = new NPCClickHandler(new Object[] { 4512 }, e -> e.getPlayer().tele(Tile.of(3103, 4447, 0)));

    public static ObjectClickHandler exitBabaYagaHouse = new ObjectClickHandler(new Object[] { 16774 }, e -> e.getPlayer().tele(Tile.of(2087, 3930, 0)));

    public static NPCClickHandler babaYaga = new NPCClickHandler(new Object[] { 4513 }, e -> {
        switch(e.getOption()) {
            case "Talk-to" -> e.getPlayer().startConversation(new Dialogue()
                    .addNPC(4513, HeadE.CHEERFUL, "Ah, a visitor from a distant land. How can I help?")
                    .addOptions(ops -> {
                        if (e.getPlayer().isQuestComplete(Quest.LUNAR_DIPLOMACY))
                            ops.add("Have you got anything to trade?");

                        ops.add("It's a very interesting house you have here.")
                                .addPlayer(HeadE.CONFUSED, "It's a very interesting house you have here. Does he have a name?")
                                .addNPC(4513, HeadE.CHEERFUL, "Why of course. It's Berty.")
                                .addPlayer(HeadE.CONFUSED, "Berty? Berty the Chicken leg house?")
                                .addNPC(4513, HeadE.CHEERFUL, "Yes.")
                                .addPlayer(HeadE.CONFUSED, "May I ask why?")
                                .addNPC(4513, HeadE.LAUGH, "It just has a certain ring to it, don't you think? Beeerteeee!")
                                .addPlayer(HeadE.SHAKING_HEAD, "You're ins...")
                                .addNPC(4513, HeadE.CHEERFUL, "Insane? Very.");

                        ops.add("I'm good, thanks, bye.")
                                .addPlayer(HeadE.CHEERFUL, "I'm good, thanks, bye.");
                    })
            );
            case "Trade" -> {
                if (e.getPlayer().isQuestComplete(Quest.LUNAR_DIPLOMACY))
                    ShopsHandler.openShop(e.getPlayer(), "baba_yagas_magic_shop");
            }
        }
    });

    public static ObjectClickHandler handleLaddersUp = new ObjectClickHandler(new Object[] { 16959, 16960, 16640 }, e -> {
        if (e.getObj().getId() == 16640) {
            e.getPlayer().useStairs(827, Tile.of(2141, 3944, 0));
            return;
        }
        switch (e.getObj().getRotation()) {
            case 0 -> e.getPlayer().useStairs(828, e.getPlayer().transform(2, 0, 1), 1, 1);
            case 1 -> e.getPlayer().useStairs(828, e.getPlayer().transform(0, -2, 1), 1, 1);
            case 2, 3 -> e.getPlayer().useStairs(828, e.getPlayer().transform(0, 2, 1), 1, 1);
        }
    });

    public static ObjectClickHandler handleLaddersDown = new ObjectClickHandler(new Object[] { 16961, 16962, 36306 }, e -> {
        if (e.getObj().getId() == 36306) {
            e.getPlayer().useStairs(827, Tile.of(2329, 10353, 2));
            return;
        }
        switch (e.getObj().getRotation()) {
            case 0 -> e.getPlayer().useStairs(827, e.getPlayer().transform(-2, 0, -1), 1, 1);
            case 1 -> e.getPlayer().useStairs(827, e.getPlayer().transform(0, 2, -1), 1, 1);
            case 2, 3 -> e.getPlayer().useStairs(827, e.getPlayer().transform(0, -2, -1), 1, 1);
        }
    });

    public static ObjectClickHandler handleMinePassage = new ObjectClickHandler(new Object[] { 11399 }, e -> {
        if (e.getObj().getRotation() == 3)
            e.getPlayer().useStairs(827, Tile.of(2341, 10356, 2));
        else
            e.getPlayer().useStairs(827, Tile.of(2335, 10345, 2));
    });

    public static boolean hasSealOfPassage(Player player) {
        return player.getInventory().containsItem(9083) || player.getEquipment().wearingSlot(Equipment.NECK, 9083);
    }

    public static boolean SealOfPassageCheck(Player player, NPC npc) {
        if(!Quest.LUNAR_DIPLOMACY.isImplemented() || player.isQuestComplete(Quest.LUNAR_DIPLOMACY) || hasSealOfPassage(player)) {
            return true;
        }
        player.startConversation(new Dialogue()
                .addPlayer(HeadE.NERVOUS, "Hi, I...")
                .addNPC(npc, HeadE.ANGRY, "What are you doing here, Fremennik?!")
                .addPlayer(HeadE.NERVOUS, "I have a seal of pass...")
                .addNPC(npc, HeadE.ANGRY, "No you do not! Begone!")
                .addNext(() -> {
                    Magic.sendNormalTeleportSpell(player, 0, 0.0, Tile.of(2643, 3677, 0));
                    player.sendMessage("Ooops. Suppose I need a seal of passage when I'm walking around that island.");
                }));
        return false;
    }
}

