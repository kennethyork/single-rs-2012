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
package com.rs.game.content.world.areas.draynor;

import com.rs.engine.dialogue.Conversation;
import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.engine.dialogue.Options;
import com.rs.engine.quest.Quest;
import com.rs.game.World;
import com.rs.game.content.achievements.AchievementSystemD;
import com.rs.game.content.achievements.SetReward;
import com.rs.game.content.quests.dragonslayer.DragonSlayer;
import com.rs.game.content.quests.dragonslayer.NedDragonSlayerD;
import com.rs.game.content.skills.agility.Agility;
import com.rs.game.content.world.areas.global.AgilityShortcuts;
import com.rs.game.content.world.doors.DoorPair;
import com.rs.game.model.entity.player.Inventory;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.gameobject.GameObject;
import com.rs.game.tasks.WorldTasks;
import com.rs.lib.Constants;
import com.rs.lib.game.Animation;
import com.rs.lib.game.Item;
import com.rs.lib.game.Tile;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.NPCClickHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import com.rs.utils.shop.ShopsHandler;

import static com.rs.game.content.quests.princealirescue.PrinceAliRescue.*;

@PluginEventHandler
public class Draynor {

    public static NPCClickHandler handleNed = new NPCClickHandler(new Object[]{918}, e -> e.getPlayer().startConversation(new Conversation(e.getPlayer()) {
        {
            addNPC(e.getNPCId(), HeadE.CHEERFUL, "Why, hello there, " + e.getPlayer().genderTerm("lad", "lass") + ". Me friends call me Ned. I was a man of the sea, but it's past me now." +
                " Could I be making or selling you some rope?");
            addOptions("What would you like to say?", new Options() {
                @Override
                public void create() {
                    if (e.getPlayer().getQuestManager().getStage(Quest.DRAGON_SLAYER) == DragonSlayer.PREPARE_FOR_CRANDOR)
                        option("About Dragon Slayer", new Dialogue()
                            .addNext(() -> e.getPlayer().startConversation(new NedDragonSlayerD(e.getPlayer()).getStart())));
                    option("Yes, I would like some rope.", new Dialogue()
                        .addPlayer(HeadE.HAPPY_TALKING, "Yes, I would like some rope.")
                        .addNext(() -> player.startConversation(new Conversation(player) {
                            {
                                if (player.getInventory().hasCoins(15)) {
                                    addNPC(NED, HeadE.HAPPY_TALKING, "Okay, that will be 15 coins...");
                                    addPlayer(HeadE.CALM_TALK, "That's good, here you go!");
                                    addNext(() -> {
                                        player.getInventory().removeCoins(15);
                                        player.getInventory().addItem(954, 1);
                                    });

                                } else if (player.getInventory().containsItem(BALL_WOOL, 4)) {
                                    addNPC(NED, HeadE.CALM_TALK, "Okay I will need 4 balls of wool...");
                                    addPlayer(HeadE.HAPPY_TALKING, "That's good, here you go!");
                                    addNext(() -> {
                                        player.getInventory().deleteItem(BALL_WOOL, 4);
                                        player.getInventory().addItem(954, 1);
                                    });
                                } else {
                                    addNPC(NED, HeadE.CALM_TALK, "Okay, bring me 15 coins or 4 balls of wool and I'll give you some.");
                                    addPlayer(HeadE.HAPPY_TALKING, "Sounds good.");

                                }
                                create();
                            }
                        })));
                    option("About the Achievement System...", () -> new AchievementSystemD(player, e.getNPCId(), SetReward.EXPLORERS_RING));
                    option("Ned, could you make other things from wool?", new Dialogue()
                        .addNPC(NED, HeadE.HAPPY_TALKING, "I am sure I can. What are you thinking of?")
                        .addOptions("Choose an option:", new Options() {
                            @Override
                            public void create() {
                                option("Could you knit me a sweater?", new Dialogue()
                                    .addPlayer(HeadE.HAPPY_TALKING, "Could you knit me a sweater?")
                                    .addNPC(NED, HeadE.FRUSTRATED, "Do I look like a member of a sewing circle? Be off wi' you. I have" +
                                        " fought monsters that would turn your hair blue.")
                                    .addNPC(NED, HeadE.FRUSTRATED, "I don't need to be laughed at just 'cos I am getting a bit old."));
                                option("How about some sort of a wig?", new Dialogue()
                                    .addPlayer(HeadE.SKEPTICAL_THINKING, "How about some sort of a wig?")
                                    .addNext(() -> player.startConversation(new Conversation(player) {
                                        {
                                            addNPC(NED, HeadE.SKEPTICAL_THINKING, "Well... That's an interesting thought. Yes, I think I could do something. " +
                                                "Give me 3 balls of wool and I might be able to do it.");
                                            if (player.getInventory().containsItem(BALL_WOOL, 3))
                                                addOptions("Choose an option:", new Options() {
                                                    @Override
                                                    public void create() {
                                                        option("I have that now. Please, make me a wig.", new Dialogue()
                                                            .addPlayer(HeadE.CALM_TALK, "I have that now. Please, make me a wig.")
                                                            .addNPC(NED, HeadE.CALM_TALK, "Okay, I will have a go.")
                                                            .addSimple("You hand Ned 3 balls of wool. Ned works with the wool. His hands " +
                                                                "move with a speed you couldn't imagine.")
                                                            .addNPC(NED, HeadE.HAPPY_TALKING, "Here you go, how's that for a quick effort? Not bad I think!")
                                                            .addSimple("Ned gives you a pretty good wig.", () -> {
                                                                player.getInventory().deleteItem(BALL_WOOL, 3);
                                                                player.getInventory().addItem(WIG, 1);
                                                            })
                                                            .addPlayer(HeadE.HAPPY_TALKING, "Thanks Ned, there's more to you than meets the eye."));
                                                        option("I will come back when I need you to make me one.", new Dialogue());
                                                    }
                                                });
                                            else
                                                addPlayer(HeadE.HAPPY_TALKING, "I will come back if I need one.");
                                            create();
                                        }
                                    })));
                                option("Could you repair the arrow holes in the back of my shirt?", new Dialogue()
                                    .addNPC(NED, HeadE.HAPPY_TALKING, "Ah yes, it's a tough world these days. There's a few brave enough to " +
                                        "attack from 10 metres away.")
                                    .addNPC(NED, HeadE.HAPPY_TALKING, "There you go, good as new.")
                                    .addPlayer(HeadE.HAPPY_TALKING, "Thanks Ned. Maybe next time they will attack me face to face.")
                                );
                            }
                        }));
                }
            });
            create();
        }
    }));

    public static NPCClickHandler handleAva = new NPCClickHandler(new Object[]{5199}, e -> {
        if (!e.getPlayer().isQuestComplete(Quest.ANIMAL_MAGNETISM, "to buy Ava's devices."))
            return;
        Conversation chooseDevice = new Conversation(e.getPlayer()) {
            {
                addOptions("Which device would you like?", new Options() {
                    @Override
                    public void create() {
                        option("The attractor", () -> {
                            if (player.getInventory().hasCoins(999)) {
                                player.getInventory().removeCoins(999);
                                player.getInventory().addItem(10498, 1);
                                player.startConversation(
                                    new Dialogue().addSimple("You buy an attractor for 999 coins."));
                            } else
                                player.startConversation(new Conversation(e.getPlayer()).addNPC(e.getNPCId(),
                                    HeadE.NO_EXPRESSION,
                                    "I'm not running a charity. You need at least 999 coins to buy a new attractor."));
                        });
                        if (e.getPlayer().getSkills().getLevelForXp(Constants.RANGE) >= 50)
                            option("The accumulator", () -> {
                                if (player.getInventory().hasCoins(999)
                                    && player.getInventory().containsItem(886, 75)) {
                                    player.getInventory().removeCoins(999);
                                    player.getInventory().deleteItem(886, 75);
                                    player.getInventory().addItem(10499, 1);
                                    player.startConversation(new Dialogue().addSimple(
                                        "You buy an accumulator for 999 coins and 75 steel arrows."));
                                } else
                                    player.startConversation(new Conversation(e.getPlayer()).addNPC(e.getNPCId(),
                                        HeadE.NO_EXPRESSION,
                                        "I'm not running a charity. You need at least 999 coins and 75 steel arrows to buy a new accumulator."));
                            });
                        if (player.isQuestComplete(Quest.DO_NO_EVIL, "to claim an alerter."))
                            option("The alerter", () -> {
                                if (player.getInventory().hasCoins(999)
                                    && player.getInventory().containsItem(886, 75)) {
                                    player.getInventory().removeCoins(999);
                                    player.getInventory().deleteItem(886, 75);
                                    player.getInventory().addItem(20068, 1);
                                    player.startConversation(new Dialogue()
                                        .addSimple("You buy an alerter for 999 coins and 75 steel arrows."));
                                } else
                                    player.startConversation(new Conversation(e.getPlayer()).addNPC(e.getNPCId(),
                                        HeadE.NO_EXPRESSION,
                                        "I'm not running a charity. You need at least 999 coins and 75 steel arrows to buy a new alerter."));
                            });
                    }
                });
            }
        };

        switch (e.getOpNum()) {
            case 1:
                e.getPlayer().startConversation(new Conversation(e.getPlayer()) {
                    {
                        addNPC(e.getNPCId(), HeadE.CONFUSED,
                            "Hello again; I'm busy with my newest research, so can't gossip too much. Are you after information, an upgrade, another device, or would you like to see my goods for sale?");
                        addOptions("What would you like to say?", new Options() {
                            @Override
                            public void create() {
                                option("I seem to need a new device.", () -> player.startConversation(chooseDevice));
                                option("I'd like to see your stuff for sale please.",
                                    () -> ShopsHandler.openShop(player, "avas_odds_and_ends"));
                            }
                        });
                    }
                });
                break;
            case 3:
                ShopsHandler.openShop(e.getPlayer(), "avas_odds_and_ends");
                break;
            case 4:
                e.getPlayer().startConversation(chooseDevice);
                break;
        }
    });

    public static ObjectClickHandler handleEnterDraynorSewers = new ObjectClickHandler(new Object[]{6435}, e -> {
        Player p = e.getPlayer();
        GameObject obj = e.getObj();
        if (obj.getTile().matches(Tile.of(3118, 3244, 0)))//south entrance
            p.useStairs(827, Tile.of(3118, 9643, 0), 1, 1);
        if (obj.getTile().matches(Tile.of(3084, 3272, 0)))//north entrance
            p.useStairs(827, Tile.of(3085, 9672, 0), 1, 1);
    });

    public static ObjectClickHandler handleMorgansChest = new ObjectClickHandler(new Object[]{46243}, e -> e.getPlayer().sendMessage("The chest is locked..."));

    public static ObjectClickHandler handleExitsDraynorSewers = new ObjectClickHandler(new Object[]{26518, 32015}, e -> {
        Player p = e.getPlayer();
        GameObject obj = e.getObj();
        if (obj.getTile().matches(Tile.of(3118, 9643, 0)))//north
            p.ladder(Tile.of(3118, 3245, 0));
        if (obj.getTile().matches(Tile.of(3084, 9672, 0)))//south
            p.ladder(Tile.of(3084, 3273, 0));
    });

    public static ObjectClickHandler handleEnterDraynorAvaSecret = new ObjectClickHandler(new Object[]{160, 47404}, e -> {
        e.getPlayer().lock();
        e.getPlayer().setNextFaceTile(e.getObj().getTile());
        e.getPlayer().setNextAnimation(new Animation(1548));
        WorldTasks.delay(2, () -> e.getPlayer().addWalkSteps(e.getPlayer().transform(0, e.getObjectId() == 47404 ? -1 : 1), 1, true));
        WorldTasks.delay(4, () -> {
            GameObject door1 = World.getObjectWithId(
                e.getObj().getTile().transform(e.getObjectId() == 47404 ? 0 : 1, e.getObjectId() == 47404 ? -1 : 2),
                47531);
            GameObject door2 = World.getObjectWithId(
                e.getObj().getTile().transform(e.getObjectId() == 47404 ? 0 : 1, e.getObjectId() == 47404 ? -2 : 1),
                47529);
            if (door1 != null && door2 != null) {
                World.spawnObjectTemporary(new GameObject(door1, 83), 2, true);
                World.spawnObjectTemporary(new GameObject(door2, 83), 2, true);
                World.spawnObjectTemporary(new GameObject(DoorPair.getOpposingDoor(e.getPlayer(), door1),
                    door1.getType(), door1.getRotation(1), door1.getTile().transform(-1, 0, 0)), 2, true);
                World.spawnObjectTemporary(new GameObject(DoorPair.getOpposingDoor(e.getPlayer(), door2),
                    door2.getType(), door2.getRotation(-1), door2.getTile().transform(-1, 0, 0)), 2, true);
            }
            e.getPlayer().addWalkSteps(
                e.getObj().getTile().transform(e.getObjectId() == 47404 ? -1 : 1, e.getObjectId() == 47404 ? -1 : 1),
                3, false);
            e.getPlayer().unlock();
        });
    });

    public static ObjectClickHandler handleWizardsTowerBasementDown = new ObjectClickHandler(new Object[] { 2147 }, e -> {e.getPlayer().ladder(Tile.of(3104, 9576, 0));});
    public static ObjectClickHandler handleWizardsTowerBasementUp = new ObjectClickHandler(new Object[]{32015}, new Tile[]{Tile.of(3103, 9576, 0)}, e -> e.getPlayer().ladder(Tile.of(3105, 3162, 0)));

    public static ObjectClickHandler handleDraynorManorBasement = new ObjectClickHandler(new Object[]{47643, 164}, e -> {
        if (e.getObjectId() == 47643)
            e.getPlayer().useStairs(Tile.of(3080, 9776, 0));
        else
            e.getPlayer().useStairs(Tile.of(3115, 3355, 0));
    });

    public static ObjectClickHandler handleDraynorManorRailing = new ObjectClickHandler(new Object[]{37703}, e -> {
        Player p = e.getPlayer();
        GameObject obj = e.getObj();
        if (!Agility.hasLevel(p, 28)) {
            p.getPackets().sendGameMessage("You need level 28 agility to use this shortcut.");
            return;
        }
        if (!obj.getTile().matches(Tile.of(3083, 3353, 0)))
            return;
        if (p.getX() > obj.getX())
            AgilityShortcuts.climbOver(p, Tile.of(obj.getX(), obj.getY(), obj.getPlane()));
        if (p.getX() <= obj.getX())
            AgilityShortcuts.climbOver(p, Tile.of(obj.getX() + 1, obj.getY(), obj.getPlane()));
    });

    public static ObjectClickHandler handleDraynorManorStairs = new ObjectClickHandler(new Object[]{47364, 47657}, e -> e.getPlayer().useStairs(e.getPlayer().transform(0, e.getObjectId() == 47364 ? 5 : -5, e.getObjectId() == 47364 ? 1 : -1)));
	public static ObjectClickHandler handleDrawers = new ObjectClickHandler(new Object[] { 46250 }, e -> e.getPlayer().getInventory().addItem(new Item(1550, 1)));
    public static ObjectClickHandler handleStairs = new ObjectClickHandler(new Object[] { 11355, 11356 }, e -> {
        switch (e.getObjectId()) {
            case 11355 -> e.getPlayer().useStairs(-1, Tile.of(2677, 5214, 2), 0, 1);
            case 11356 -> e.getPlayer().useStairs(-1, Tile.of(3110, 3363, 2), 0, 1);
        }
    });

    public static ObjectClickHandler handleLadders = new ObjectClickHandler(new Object[] { 2347, 2348 }, e -> {
        switch (e.getObjectId()) {
            case 2347 -> e.getPlayer().tele(e.getPlayer().transform(e.getObj().getRotation() == 3 ? 3 : -3, 0, 1));
            case 2348 -> e.getPlayer().tele(e.getPlayer().transform(e.getObj().getRotation() == 3 ? -3 : 3, 0, -1));
        }
    });
}
