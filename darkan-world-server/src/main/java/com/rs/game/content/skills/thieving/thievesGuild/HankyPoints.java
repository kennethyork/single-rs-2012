package com.rs.game.content.skills.thieving.thievesGuild;

import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.game.World;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.game.model.gameobject.GameObject;
import com.rs.game.tasks.WorldTasks;
import com.rs.lib.Constants;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.NPCClickHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import com.rs.utils.Ticks;

@PluginEventHandler
public class HankyPoints {
    public static int maxPoints(Player player){
        int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
        int maxPoints = 4 * (10 + (thievingLevel / 9));
        return maxPoints;
    }

    private static double calculateXP(Player player) {
        int[][] experienceData = {
                {99, 10080},
                {98, 9840},
                {97, 9680},
                {96, 9440},
                {95, 9280},
                {94, 9120},
                {93, 8880},
                {92, 8720},
                {91, 8560},
                {90, 8320},
                {89, 8132},
                {88, 7980},
                {87, 7828},
                {86, 7600},
                {85, 7448},
                {84, 7296},
                {83, 7144},
                {82, 6916},
                {81, 6764},
                {80, 6624},
                {79, 6480},
                {78, 6336},
                {77, 6120},
                {76, 5976},
                {75, 5832},
                {74, 5688},
                {73, 5544},
                {72, 5400},
                {71, 5236},
                {70, 5100},
                {69, 4964},
                {68, 4828},
                {67, 4692},
                {66, 4556},
                {65, 4420},
                {64, 4284},
                {63, 4148},
                {62, 4032},
                {61, 3904},
                {60, 3776},
                {59, 3648},
                {58, 3520},
                {57, 3456},
                {56, 3384},
                {55, 3200},
                {54, 3072},
                {53, 3000},
                {52, 2880},
                {51, 2760},
                {50, 2700},
                {49, 2580},
                {48, 2460},
                {47, 2400},
                {46, 2280},
                {45, 2160},
                {44, 2072},
                {43, 2016},
                {42, 1904},
                {41, 1848},
                {40, 1736},
                {39, 1680},
                {38, 1568},
                {37, 1512},
                {36, 1412},
                {35, 1352},
                {34, 1300},
                {33, 1248},
                {32, 1092},
                {31, 1092},
                {30, 1092},
                {29, 988},
                {28, 936},
                {27, 832},
                {26, 816},
                {25, 768},
                {24, 720},
                {23, 672},
                {22, 624},
                {21, 576},
                {20, 528},
                {19, 480},
                {18, 432},
                {17, 396},
                {16, 352},
                {15, 352},
                {14, 308},
                {13, 264},
                {12, 264},
                {11, 220},
                {10, 220}
        };
        int xp = 0;
        int level = player.getSkills().getLevel(Skills.THIEVING);

        for (int[] row : experienceData) {
            if (row[0] == level) {
                xp = row[1];
                break;
            }
        }
        return xp;
    }

    public static void claimHankyPoints(Player player, NPC npc) {
        int hankyPoints = player.getWeeklyI("HankyPoints");
        int claimedPoints = player.getWeeklyI("ClaimedHankyPoints");
        int maxPoints = maxPoints(player);
        int remainingPoints = maxPoints - claimedPoints;
        int claimablePoints = Math.max(0, Math.min(hankyPoints, remainingPoints));

        if (remainingPoints <= 0) {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK,
                    "You have claimed the maximum amount of hanky points this week. Come back next week to claim more rewards!");
            return;
        }

        if (claimablePoints == 0) {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK,
                    "You have no hanky points to claim! Try some of the guild's training exercises to earn more.");
            return;
        }

        player.startConversation(new Dialogue().addNPC(npc.getId(), HeadE.CALM_TALK, "Sure thing! Let's see now..."));

        double xp = (calculateXP(player) / maxPoints) * claimablePoints;
        player.getSkills().addXp(Skills.THIEVING, xp);

        player.sendMessage("You gain " + Utils.getFormattedNumber((int) xp, ',') + " Thieving XP.");

        player.incWeeklyI("ClaimedHankyPoints", claimablePoints);
        player.setWeeklyI("HankyPoints", hankyPoints - claimablePoints);
    }

    public static void checkPoints(Player player, NPC npc) {
        int hankyPoints = player.getWeeklyI("HankyPoints");
        int claimedPoints = player.getWeeklyI("ClaimedHankyPoints");
        int maxPoints = maxPoints(player);
        int remainingClaimablePoints = Math.max(0, maxPoints - claimedPoints);
        int availablePoints = Math.min(hankyPoints, remainingClaimablePoints);

        if (remainingClaimablePoints == 0) {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK, "You have collected all your hanky points this week.");
            return;
        }

        if (hankyPoints == 0) {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK, "You have no hanky points! Try some of the guild's training exercises.");
            return;
        }

        if (claimedPoints > 0) {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK,
                    "You have " + availablePoints + " " + (availablePoints == 1 ? "hanky point" : "hanky points") + " ready to turn in. " +
                            "You have collected " + claimedPoints + " " + (claimedPoints == 1 ? "hanky point" : "hanky points") + ". " +
                            "You may claim the reward for up to " + remainingClaimablePoints + " more " + (remainingClaimablePoints == 1 ? "hanky point" : "hanky points") + " this week."
            );
        } else {
            player.npcDialogue(npc.getId(), HeadE.CALM_TALK,
                    "You have " + availablePoints + " " + (availablePoints == 1 ? "hanky point" : "hanky points") + " ready to turn in. " +
                            "You may claim the reward for up to " + remainingClaimablePoints + " more " + (remainingClaimablePoints == 1 ? "hanky point" : "hanky points") + " this week."
            );
        }
    }

    public static NPCClickHandler checkPoints = new NPCClickHandler(new Object[] { 11281, 11294, 11282, 11284, 11286 }, new String[] {"Check-points"}, e -> checkPoints(e.getPlayer(), e.getNPC()));

    public static NPCClickHandler pickpockethandler = new NPCClickHandler(new Object[] { 11281, 11282, 11284, 11286 }, new String[] {"Pickpocket"}, e -> e.getPlayer().getActionManager().setAction(new PickPocket(e.getNPC())));

    public static NPCClickHandler loothandler = new NPCClickHandler(new Object[] { 11290, 11292, 11288, 11296 }, new String[] {"Loot"}, e -> {
        if(e.getNPC().getTempAttribs().getO("K.O") == null) {
            e.getPlayer().sendMessage("You should knock him out before attempting to loot him.");
            return;
        }
        if(e.getNPC().getTempAttribs().getO("K.O") == e.getPlayer()) {
            e.getPlayer().getActionManager().setAction(new Loot(e.getNPC()));
        }
        else
            e.getPlayer().sendMessage("Someone else knocked out that target.");
    });

    public static NPCClickHandler lure = new NPCClickHandler(new Object[] { 11290, 11292, 11288, 11296 }, new String[] {"Lure"}, e -> {
        String[] responses = new String[]{
                "Watch out! The fellow behind you has a club!",
                "Behind you! A three-headed monkey!",
                "That's the third biggest platypus I've ever seen!",
                "Look over THERE!",
                "Look! An eagle!",
                "Your shoelace is untied."
        };
        int npcId = e.getNPCId();
        String npcMessage = "Oh nooooo!";
        if (npcId == 11296) {
            npcMessage = "Wha?";
        }
        e.getPlayer().startConversation(new Dialogue()
                .addPlayer(HeadE.SKEPTICAL, responses[Utils.random(5)])
                .addNPC(npcId, HeadE.SCARED, npcMessage)
                .addNext(() -> {
                    e.getNPC().follow(e.getPlayer());
                    e.getNPC().getTempAttribs().setO("lured", e.getPlayer());
                    e.getPlayer().sendMessage();

                    WorldTasks.delay(Ticks.fromSeconds(10), () -> {
                        e.getNPC().getTempAttribs().setO("lured", e.getPlayer());
                        e.getNPC().getActionManager().forceStop();
                    });
                })
        );
    });

    public static ObjectClickHandler handleNorthDoors = new ObjectClickHandler(new Object[] { 52302 }, e -> {
        if(e.getOption().equalsIgnoreCase("Open")) {
            if(e.getPlayer().getTile().getY() >= e.getObj().getY()) {
                World.spawnObjectTemporary(new GameObject(-1, e.getObj().getType(), e.getObj().getRotation(), e.getObj().getTile()), Ticks.fromSeconds(10), true);
                World.spawnObjectTemporary(new GameObject(e.getObjectId() + 1, e.getObj().getType(), e.getObj().getRotation() - 1, e.getObj().getTile().transform(0, -1, 0)), Ticks.fromSeconds(10), true);
                return;
            }
            e.getPlayer().lock();
            e.getPlayer().sendMessage("You examine the lock on the door...");
            WorldTasks.scheduleTimer(i -> {
                switch(i) {
                    case 1 -> {
                        e.getPlayer().faceObject(e.getObj());
                        e.getPlayer().anim(832);
                    }
                    case 3 -> {
                        if (Utils.skillSuccess(e.getPlayer().getSkills().getLevel(Skills.THIEVING), 190, 190)) {
                            e.getPlayer().sendMessage("The door swings open.");
                            e.getPlayer().getSkills().addXp(Constants.THIEVING, 210);
                            World.spawnObjectTemporary(new GameObject(-1, e.getObj().getType(), e.getObj().getRotation(), e.getObj().getTile()), Ticks.fromMinutes(5), true);
                            World.spawnObjectTemporary(new GameObject(e.getObjectId() + 1, e.getObj().getType(), e.getObj().getRotation() - 3, e.getObj().getTile().transform(0, -1, 0)), Ticks.fromMinutes(5), true);
                        } else {
                            e.getPlayer().sendMessage("You fail to pick the lock.");
                            e.getPlayer().unlock();
                            return false;
                        }
                    }
                    case 4 -> e.getPlayer().unlock();
                }
                return true;
            });
        }
    });

    public static ObjectClickHandler handleSouthDoors = new ObjectClickHandler(new Object[] { 52304 }, e -> {
        if(e.getOption().equalsIgnoreCase("Open")) {
            if(e.getPlayer().getY() <= e.getObj().getY()) {
                World.spawnObjectTemporary(new GameObject(-1, e.getObj().getType(), e.getObj().getRotation(), e.getObj().getTile()), Ticks.fromSeconds(10), true);
                World.spawnObjectTemporary(new GameObject(e.getObjectId() + 1, e.getObj().getType(), e.getObj().getRotation() + 1, e.getObj().getTile().transform(0, +1, 0)), Ticks.fromSeconds(10), true);
                return;
            }
            if(e.getPlayer().getSkills().getLevel(Skills.THIEVING) < 35){
                e.getPlayer().simpleDialogue("You need a Thieving level of at least 35 to pick this lock.");
                return;
            }
            if(!e.getPlayer().getInventory().containsOneItem(1523)){
                e.getPlayer().sendMessage("This lock is too complex. You need a lockpick to be able to pick the lock.");
                return;
            }
            e.getPlayer().lock();
            e.getPlayer().sendMessage("You examine the lock on the door...");
            WorldTasks.scheduleTimer(i -> {
                switch(i) {
                    case 1 -> {
                        e.getPlayer().faceObject(e.getObj());
                        e.getPlayer().anim(832);
                    }
                    case 3 -> {
                        if (Utils.skillSuccess(e.getPlayer().getSkills().getLevel(Skills.THIEVING), 190, 190)) {
                            e.getPlayer().sendMessage("The door swings open.");
                            e.getPlayer().getSkills().addXp(Constants.THIEVING, 280);
                            World.spawnObjectTemporary(new GameObject(-1, e.getObj().getType(), e.getObj().getRotation(), e.getObj().getTile()), Ticks.fromMinutes(5), true);
                            World.spawnObjectTemporary(new GameObject(e.getObjectId() + 1, e.getObj().getType(), e.getObj().getRotation() + 1, e.getObj().getTile().transform(0, +1, 0)), Ticks.fromMinutes(5), true);
                        } else {
                            e.getPlayer().sendMessage("You fail to pick the lock.");
                            e.getPlayer().unlock();
                            return false;
                        }
                    }
                    case 4 -> e.getPlayer().unlock();
                }
                return true;
            });
        }
    });

    public static ObjectClickHandler handleNorthChests = new ObjectClickHandler(new Object[] { 52296 }, e -> {
        Player player = e.getPlayer();
        GameObject object = e.getObj();
        player.faceObject(object);
        if (player.getSkills().getLevel(Constants.THIEVING) < 26) {
            player.simpleDialogue("You need a Thieving level of at least 26 to pick this lock.");
            return;
        }
        player.lock(1);
        player.sendMessage("You attempt to pick the lock.");
        player.anim(536);
        if (Utils.skillSuccess(e.getPlayer().getSkills().getLevel(Skills.THIEVING), 190, 190)) {
            player.getSkills().addXp(Constants.THIEVING, 30);
            object.setIdTemporary(e.getObjectId() + 1, Ticks.fromMinutes(1));
            player.incWeeklyI("HankyPoints", 1);
            player.sendMessage("You find a blue handkerchief.");
        }
        else {
            e.getPlayer().sendMessage("You fail to pick the lock.");
            e.getPlayer().unlock();
        }
    });

    public static ObjectClickHandler handleSouthChests = new ObjectClickHandler(new Object[] { 52299 }, e -> {
        Player player = e.getPlayer();
        GameObject object = e.getObj();
        player.faceObject(object);
        if (player.getSkills().getLevel(Constants.THIEVING) < 35) {
            player.simpleDialogue("You need a Thieving level of at least 35 to pick this lock.");
            return;
        }
        player.lock(1);
        player.sendMessage("You attempt to pick the lock.");
        player.anim(536);
        if (Utils.skillSuccess(e.getPlayer().getSkills().getLevel(Skills.THIEVING), 190, 190)) {
            player.getSkills().addXp(Constants.THIEVING, 180);
            player.sendMessage("You find a red handkerchief.");
            player.incWeeklyI("HankyPoints", 4);
            object.setIdTemporary(e.getObjectId() + 1, Ticks.fromMinutes(1));

        }
        else {
            e.getPlayer().sendMessage("You fail to pick the lock.");
            e.getPlayer().unlock();
        }
    });

}
