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
package com.rs.game.content.skills.prayer;

import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.npc.combat.NPCCombatDefinitions;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.lib.util.GenericAttribMap;
import java.util.*;


public class Sap {

	private static final String SAP_DRAIN_PREFIX = "sap_drain_";
	private static final String SAP_TIMER_PREFIX = "sap_timers_";
	private static final String SAP_PROC_PREFIX = "sap_proc_";
	private static final String SAP_DECAY_PREFIX = "sap_decay_";

	private static final int DECAY_INTERVAL = 10000; // 10s per stack
	private static final int DECAY_LOCKOUT = 60000; // 60s lockout on fresh proc
	private static final int MAX_STACKS = 5;
	private static final int SAP_DRAIN_PER_STACK = 2;
	private static final int INITIAL_SAP_DRAIN_PERCENT = 10;

	public enum SapType {
		ATTACK(Skills.ATTACK),
		STRENGTH(Skills.STRENGTH),
		DEFENSE(Skills.DEFENSE),
		RANGED(Skills.RANGE),
		MAGIC(Skills.MAGIC);

		public final int skillId;

		SapType(int skillId) {
			this.skillId = skillId;
		}

	}

	public enum SapCurse {
		WARRIOR(List.of(SapType.ATTACK, SapType.STRENGTH, SapType.DEFENSE), Prayer.SAP_WARRIOR, 2214, 2215, 2216, 0),
		RANGER(List.of(SapType.RANGED, SapType.DEFENSE), Prayer.SAP_RANGE, 2217, 2218, 2219, 1),
		MAGE(List.of(SapType.MAGIC, SapType.DEFENSE), Prayer.SAP_MAGE, 2220, 2221, 2222, 2),
		SPIRIT(null, Prayer.SAP_SPIRIT, 2223, 2224, 2225, 3);

		public final List<SapType> drains;
		private final Prayer prayer;
		private final int spotAnimStart;
		private final int projAnim;
		private final int spotAnimHit;
		private final int curseId;

		SapCurse(List<SapType> drains, Prayer prayer, int spotAnimStart, int projAnim, int spotAnimHit, int curseId) {
			this.drains = drains;
			this.prayer = prayer;
			this.spotAnimStart = spotAnimStart;
			this.projAnim = projAnim;
			this.spotAnimHit = spotAnimHit;
			this.curseId = curseId;
		}
	}

	public static void applySapSpirit(Player player, Entity target, SapCurse type) {
		if (!(target instanceof Player))
			return;

		((Player) target).getCombatDefinitions().drainSpec(10);

		playSapEffect(player, target, type.spotAnimStart, type.projAnim, type.spotAnimHit, 8115);
	}

	public static void applySapCurse(Player player, Entity target, SapCurse curse) {
		for (SapType type : curse.drains) {
			handleSapOnHit(player, target, type.skillId);
		}

		switch (curse) {
			case WARRIOR -> playSapEffect(player, target, 2214, 2215, 2216, 8115);
			case RANGER -> playSapEffect(player, target, 2217, 2218, 2219, 8115);
			case MAGE -> playSapEffect(player, target, 2220, 2221, 2222, 8115);
		}
	}

	//called when attacker hits a target and sap applies
	public static void handleSapOnHit(Player attacker, Entity target, int statId) {
		String drainKey = SAP_DRAIN_PREFIX + statId;
		String timerKey = SAP_TIMER_PREFIX + statId;
		String procKey = SAP_PROC_PREFIX + statId;

		GenericAttribMap attribs = target.getTempAttribs();
		int attackerIndex = attacker.getIndex();

		Map<Integer, Integer> drainMap = attribs.getO(drainKey);
		if (drainMap == null) {
			drainMap = new HashMap<>();
			attribs.setO(drainKey, drainMap);
		}

		Map<Integer, List<Long>> timerMap = attribs.getO(timerKey);
		if (timerMap == null) {
			timerMap = new HashMap<>();
			attribs.setO(timerKey, timerMap);
		}

		Map<Integer, Long> procMap = attribs.getO(procKey);
		if (procMap == null) {
			procMap = new HashMap<>();
			attribs.setO(procKey, procMap);
		}

		List<Long> stackTimes = timerMap.computeIfAbsent(attackerIndex, k -> new ArrayList<>());
		int currentStacks = drainMap.getOrDefault(attackerIndex, 0);

		int base = target instanceof Player
				? ((Player) target).getSkills().getLevelForXp(statId)
				: ((NPC) target).getCombatDefinitions().getLevels().get(getSkillFromId(statId));

		int current = target instanceof Player
				? ((Player) target).getSkills().getLevel(statId)
				: ((NPC) target).getLevel(statId);

		int maxDrain = (int) Math.round(base * 0.2);

		boolean drainCapped = maxDrain > 0 && currentStacks >= MAX_STACKS;

		// Prevent drain if total drained from any source (Leech or Sap) is 20% or higher
		if (drainCapped){
			procMap.put(attackerIndex, System.currentTimeMillis());
		} else {
			// Add Stack
			currentStacks++;

			// Save updated values
			 drainMap.put(attackerIndex, currentStacks);
			 stackTimes.add(System.currentTimeMillis());
			 procMap.put(attackerIndex, System.currentTimeMillis());

			// Apply drain
			if (currentStacks > 0)
				applyStatDrain(target, statId, currentStacks);
		}

	}

	private static void applyStatDrain(Entity target, int statId, int stacks) {
		if (stacks <= 0)
			return;

		int percent = INITIAL_SAP_DRAIN_PERCENT + (SAP_DRAIN_PER_STACK * stacks);
		if (percent > 20)
			percent = 20;

		int base = target instanceof Player
				? ((Player) target).getSkills().getLevelForXp(statId)
				: ((NPC) target).getLevel(statId);

		int maxDrain = (int) Math.floor(base * 0.2);
		int newLevel = base - (int) Math.floor(base * percent / 100.0);

		if (target instanceof Player player) {
			int current = player.getSkills().getLevel(statId);
			if (base - current >= maxDrain)
				return; // Already drained max

			if (current > newLevel)
				player.getSkills().set(statId, newLevel);
		} else {
            NPC npc = (NPC) target;
            int current = npc.getLevel(statId);
            if (base - current >= maxDrain)
                return;
			switch (statId) {
				case 0 -> npc.lowerAttack(percent, .20);
				case 1 -> npc.lowerDefense(percent,.20);
				case 2 -> npc.lowerStrength(percent, .20);
				case 4 -> npc.lowerRange(percent, .20);
				case 6 -> npc.lowerMagic(percent, .20);
			}
        }
	}

	public static void playSapEffect(Player player, Entity target, int castSpotAnim, int projectileId, int targetSpotAnim, int soundId) {
		player.sync(12569, castSpotAnim);
		player.soundEffect(target, soundId, true);

		player.getPrayer().curseProjectile(player, target, projectileId, targetSpotAnim, false);
	}

	public static void tickSapDecay(Entity target) {
		long now = System.currentTimeMillis();

		for (SapType type : SapType.values()) {
			String drainKey = SAP_DRAIN_PREFIX + type.skillId;
			String timerKey = SAP_TIMER_PREFIX + type.skillId;
			String procKey = SAP_PROC_PREFIX + type.skillId;
			String decayKey = SAP_DECAY_PREFIX + type.skillId;

			Map<Integer, Integer> drainMap = target.getTempAttribs().getO(drainKey);
			Map<Integer, List<Long>> timerMap = target.getTempAttribs().getO(timerKey);
			Map<Integer, Long> procMap = target.getTempAttribs().getO(procKey);

			if (drainMap == null || timerMap == null || procMap == null)
				continue;

			Iterator<Map.Entry<Integer, List<Long>>> it = timerMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, List<Long>> entry = it.next();
				int attacker = entry.getKey();
				List<Long> stackTimes = entry.getValue();
				long lastProc = procMap.getOrDefault(attacker, 0L);
				Map<Integer, Long> decayMap = target.getTempAttribs().getO(decayKey);
				if (decayMap == null) {
					decayMap = new HashMap<>();
					target.getTempAttribs().setO(decayKey, decayMap);
				}
				long lastDecay = decayMap.getOrDefault(attacker, 0L);

				if (now - lastProc < DECAY_LOCKOUT || stackTimes.isEmpty())
					continue;

				if (now - lastDecay < DECAY_INTERVAL || now - stackTimes.get(0) < DECAY_INTERVAL)
					continue;

				stackTimes.remove(0);
				int newStack = Math.max(0, drainMap.getOrDefault(attacker, 1) - 1);
				drainMap.put(attacker, newStack);
				decayMap.put(attacker, now);

				if (newStack == 0 || stackTimes.isEmpty()) {
					it.remove();
					drainMap.remove(attacker);
					procMap.remove(attacker);
					decayMap.remove(attacker);
				}

				restoreSapDrain(target, type.skillId);
			}
		}
	}

	private static void restoreSapDrain(Entity target, int statId) {
		int base = getBaseLevel(target, statId);
		int current = getCurrentLevel(target, statId);
		int restore = (int) Math.round(base * SAP_DRAIN_PER_STACK / 100.0);
		int newLevel = Math.min(base, current + restore);
		setLevel(target, statId, newLevel);
	}

	private static NPCCombatDefinitions.Skill getSkillFromId(int statId) {
		return switch (statId) {
			case 0 -> NPCCombatDefinitions.Skill.ATTACK;
			case 1 -> NPCCombatDefinitions.Skill.DEFENSE;
			case 2 -> NPCCombatDefinitions.Skill.STRENGTH;
			case 4 -> NPCCombatDefinitions.Skill.RANGE;
			case 6 -> NPCCombatDefinitions.Skill.MAGE;
			default -> null;
		};
	}

	private static int getBaseLevel(Entity e, int statId) {
		if (e instanceof Player p) return p.getSkills().getLevelForXp(statId);
		if (e instanceof NPC n) return n.getCombatLevel(getSkillFromId(statId));
		return 1;
	}

	private static int getCurrentLevel(Entity e, int statId) {
		if (e instanceof Player p) return p.getSkills().getLevel(statId);
		if (e instanceof NPC n) return n.getCombatLevel(getSkillFromId(statId));
		return 1;
	}

	private static void setLevel(Entity e, int statId, int level) {
		if (e instanceof Player p) p.getSkills().set(statId, level);
		if (e instanceof NPC n) n.setStat(getSkillFromId(statId), level);
	}
}
