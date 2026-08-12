package com.rs.game.content.skills.prayer;

import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.game.model.entity.player.managers.PrayerManager;

import java.util.Objects;

public class Turmoil {
    // Tracking last opponent to refresh boosts when changing target
    private static final String LAST_TURMOIL_TARGET_ATTR = "lastTurmoilTarget";
    private static final String LAST_TURMOIL_TIME_ATTR = "turmoilLastHitTime";

    private static final long TURMOIL_TIMEOUT = 10000; // 10 seconds

    public static void applyTurmoilOnHit(Player player, Entity target) {
        if (!player.getPrayer().active(Prayer.TURMOIL))
            return;

        if (!(target instanceof Player || target instanceof NPC))
            return;

        Integer lastTargetIndex = player.getTempAttribs().getO(LAST_TURMOIL_TARGET_ATTR);

        // On new target, recalculate boosts and update
        if (lastTargetIndex == null || lastTargetIndex != target.getIndex()) {
            // New target -> recalculate boosts
            updateTurmoilBoosts(player, target);
            player.getTempAttribs().setO(LAST_TURMOIL_TARGET_ATTR, target.getIndex());
        }

        // Update last hit time every time we hit
        player.getTempAttribs().setL(LAST_TURMOIL_TIME_ATTR, System.currentTimeMillis());
    }

    public static void resetTurmoil(Player player) {
        clearBoost(player, PrayerManager.StatMod.ATTACK);
        clearBoost(player, PrayerManager.StatMod.STRENGTH);
        clearBoost(player, PrayerManager.StatMod.DEFENSE);
        player.getTempAttribs().removeO(LAST_TURMOIL_TARGET_ATTR);
    }

    private static void updateTurmoilBoosts(Player player, Entity target) {
        int targetAtk = getBaseLevel(target, Skills.ATTACK);
        int targetStr = getBaseLevel(target, Skills.STRENGTH);
        int targetDef = getBaseLevel(target, Skills.DEFENSE);
        int playerAtk = player.getSkills().getLevelForXp(Skills.ATTACK);
        int playerStr = player.getSkills().getLevelForXp(Skills.STRENGTH);
        int playerDef = player.getSkills().getLevelForXp(Skills.DEFENSE);
        int atkBoost = 0;
        int strBoost = 0;
        int defBoost = 0;
		atkBoost = (int) (Math.floor(100 * Math.floor(.15 * targetAtk) / playerAtk));
		strBoost = (int) (Math.floor(100 * Math.floor(.10 * targetStr) / playerStr));
		defBoost = (int) (Math.floor(100 * Math.floor(.15 * targetDef) / playerDef));

        player.getPrayer().setStatMod(PrayerManager.StatMod.ATTACK, atkBoost);
        player.getPrayer().setStatMod(PrayerManager.StatMod.STRENGTH, strBoost);
        player.getPrayer().setStatMod(PrayerManager.StatMod.DEFENSE, defBoost);
    }

    private static int getBaseLevel(Entity target, int skillId) {
        if (target instanceof Player p)
            return p.getSkills().getLevelForXp(skillId);
        if (target instanceof NPC n)
            return n.getCombatDefinitions().getLevels().get(Leech.getSkillFromId(skillId));
        return 1;
    }

    private static void clearBoost(Player player, PrayerManager.StatMod skillId) {
        player.getPrayer().setStatMod(skillId, 0);
    }

    public static void tickTurmoilDecay(Player player) {
        if (!player.getPrayer().active(Prayer.TURMOIL))
            return;

        long lastHitTime = player.getTempAttribs().getL(LAST_TURMOIL_TIME_ATTR);
        long elapsed = System.currentTimeMillis() - lastHitTime;
        if (elapsed >= TURMOIL_TIMEOUT) {
            resetTurmoil(player);
            player.getTempAttribs().removeO(LAST_TURMOIL_TARGET_ATTR);
            player.getTempAttribs().removeL(LAST_TURMOIL_TIME_ATTR);
        }
    }
}
