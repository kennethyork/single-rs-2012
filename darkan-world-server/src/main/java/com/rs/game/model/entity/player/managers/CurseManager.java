package com.rs.game.model.entity.player.managers;

import com.rs.game.content.skills.prayer.Sap;
import com.rs.game.content.skills.prayer.Turmoil;
import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.player.Player;
import com.rs.game.content.skills.prayer.Prayer;
import com.rs.game.content.skills.prayer.Sap.SapCurse;
import com.rs.game.content.skills.prayer.Leech;
import com.rs.game.content.skills.prayer.Leech.LeechType;



public class CurseManager {
    private static final double LEECH_PROC_CHANCE = 0.20;
    private static final double SAP_PROC_CHANCE = 0.25;

    private static final LeechType[] LEECH_PRIORITY = {
            LeechType.ATTACK,
            LeechType.STRENGTH,
            LeechType.DEFENSE,
            LeechType.RANGED,
            LeechType.MAGIC
    };

    private static final SapCurse[] SAP_PRIORITY = {
            SapCurse.WARRIOR,
            SapCurse.RANGER,
            SapCurse.MAGE,
    };

    public static void handleCurseEffects(Player player, Entity target) {
        if (!player.getPrayer().isCurses())
            return;

        // === Always give utility curses a chance to proc ===
        if (player.getPrayer().active(Prayer.LEECH_SPECIAL) && Math.random() <= LEECH_PROC_CHANCE) {
            Leech.applyLeechSpecial(player, target, LeechType.SPECIAL);
        }
        if (player.getPrayer().active(Prayer.LEECH_ENERGY) && Math.random() <= LEECH_PROC_CHANCE) {
            Leech.applyLeechRun(player, target, LeechType.ENERGY);
        }
        if (player.getPrayer().active(Prayer.SAP_SPIRIT) && Math.random() <= SAP_PROC_CHANCE) {
            Sap.applySapSpirit(player, target, SapCurse.SPIRIT);
        }

        // === Combat Curses (only one may apply per hit) ===

        // Try Leech curses first
        for (LeechType type : LEECH_PRIORITY) {
            if (type.skillId == -1)
                return;
            if (!player.getPrayer().active(type.getPrayer()))
                continue;

            if (Math.random() <= LEECH_PROC_CHANCE) {
                Leech.applyLeechCurse(player, target, type);
                return; // Block further curse procs this hit
            }
        }

        // Try Sap curses next
        for (SapCurse curse : SAP_PRIORITY) {
            if (!isSapCurseActive(player, curse))
                continue;

            if (Math.random() <= SAP_PROC_CHANCE) {
                Sap.applySapCurse(player, target, curse);
                return;
            }
        }

        if (player.getPrayer().active(Prayer.TURMOIL)) {
            Turmoil.applyTurmoilOnHit(player, target);
        }
    }

    private static boolean isSapCurseActive(Player player, SapCurse curse) {
        return switch (curse) {
            case WARRIOR -> player.getPrayer().active(Prayer.SAP_WARRIOR);
            case RANGER -> player.getPrayer().active(Prayer.SAP_RANGE);
            case MAGE -> player.getPrayer().active(Prayer.SAP_MAGE);
            case SPIRIT -> player.getPrayer().active(Prayer.SAP_SPIRIT);
        };
    }
}
