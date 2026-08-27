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
import com.rs.game.model.entity.player.managers.PrayerManager;
import com.rs.lib.util.GenericAttribMap;

import java.util.*;

public class Leech {

	public static final int MAX_LEECH_STACKS = 5;
	public static final int LEECH_DECAY_TIME = 10000;
	public static final int INACTIVITY_TIMEOUT = 60000;

	public static final int INITIAL_LEECH_DRAIN_PERCENT = 10;
	public static final int STACK_LEECH_DRAIN_PERCENT = 3;
	public static final int STACK_LEECH_BOOST_PERCENT = 1;

	private static final String LEECH_DRAIN_PREFIX = "leech_drain_";
	private static final String LEECH_DRAIN_TIMER_PREFIX = "leech_drain_timers_";
	private static final String LEECH_PROC_PREFIX = "leech_proc_";
	private static final String LEECH_BOOST_PREFIX = "leech_boost_";
	private static final String LEECH_BOOST_TIMER_PREFIX = "leech_boost_timers_";
	private static final String LEECH_BOOST_PROC_PREFIX = "leech_boost_proc_";
	private static final String LEECH_DECAY_PREFIX = "leech_decay_";
	private static final String LEECH_BOOST_DECAY_PREFIX = "leech_boost_decay_";

	public enum LeechType {
		ATTACK(Skills.ATTACK, Prayer.LEECH_ATTACK, 1, 2231, 2232),
		STRENGTH(Skills.STRENGTH, Prayer.LEECH_STRENGTH, 1, 2248, 2250),
		DEFENSE(Skills.DEFENSE, Prayer.LEECH_DEFENSE, 1, 2244, 2246),
		RANGED(Skills.RANGE, Prayer.LEECH_RANGE, 1, 2236, 2238),
		MAGIC(Skills.MAGIC, Prayer.LEECH_MAGIC, 1, 2240, 2242),
		SPECIAL(-1, Prayer.LEECH_SPECIAL, 1, 2256, 2258),
		ENERGY(-1, Prayer.LEECH_ENERGY, 1, 2252, 2254);

		public final int skillId;
		private final Prayer prayer;
		private final int castAnim;
		private final int projAnim;
		private final int spotAnimHit;

		LeechType(int skillId, Prayer prayer, int castAnim, int projAnim, int spotAnimHit) {
			this.skillId = skillId;
			this.prayer = prayer;
			this.castAnim = castAnim;
			this.projAnim = projAnim;
			this.spotAnimHit = spotAnimHit;
		}

		public Prayer getPrayer() {
			return prayer;
		}

	}

	public static void applyLeechSpecial(Player player, Entity entity, LeechType type) {
		if (!(entity instanceof Player))
			return;

		((Player) entity).getCombatDefinitions().drainSpec(10);
		player.getCombatDefinitions().restoreSpecialAttack(10);
		playLeechEffect(player, entity, type.castAnim, type.projAnim, type.spotAnimHit, 8109);
	}

	public static void applyLeechRun(Player player, Entity entity, LeechType type) {
		if (!(entity instanceof Player))
			return;

		((Player) entity).drainRunEnergy(10.0);
		player.restoreRunEnergy(10.0);
		playLeechEffect(player, entity, type.castAnim, type.projAnim, type.spotAnimHit, 8109);
	}

	public static void applyLeechCurse(Player player, Entity target, LeechType type) {
		handleLeechOnHit(player, target, type.skillId);
		playLeechEffect(player, target, type.castAnim, type.projAnim, type.spotAnimHit, 8109);
	}

	//called when attacker hits a target and leech applies
	public static void handleLeechOnHit(Player attacker, Entity target, int statId) {
		GenericAttribMap attribs = target.getTempAttribs();
		int attackerIndex = attacker.getIndex();

		String drainKey = LEECH_DRAIN_PREFIX + statId;
		String timerKey = LEECH_DRAIN_TIMER_PREFIX + statId;
		String procKey = LEECH_PROC_PREFIX + statId;

		// Get or init target-side maps (for drains)
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

		List<Long> stackTimes = timerMap.computeIfAbsent(attackerIndex, k-> new ArrayList<>());
		int currentStacks = drainMap.getOrDefault(attackerIndex, 0);

		// Drain cap check
		int base = target instanceof Player
				? ((Player) target).getSkills().getLevelForXp(statId)
				: ((NPC) target).getCombatDefinitions().getLevels().get(getSkillFromId(statId));
		int current = target instanceof Player
				? ((Player) target).getSkills().getLevel(statId)
				: ((NPC) target).getLevel(statId);
		int maxDrain = (int) Math.floor(base * .25);
		boolean drainCapped = maxDrain > 0 && (base - current >= maxDrain || currentStacks >= MAX_LEECH_STACKS);

		procMap.put(attackerIndex, System.currentTimeMillis());

		// Apply drain
		if (!drainCapped)
			currentStacks++;
		drainMap.put(attackerIndex, currentStacks);
		stackTimes.add(System.currentTimeMillis());
		applyStatDrain(target, statId, currentStacks);


		// Apply boost
		int boostStacks = 1;
		grantStatBoost(attacker, statId, boostStacks);
	}

	private static void applyStatDrain(Entity target, int statId, int stacks) {
		if (stacks <= 0)
			return;

		int percent = INITIAL_LEECH_DRAIN_PERCENT + (STACK_LEECH_DRAIN_PERCENT * stacks);
		if (percent > 25)
			percent = 25;

		int base = target instanceof Player
				? ((Player) target).getSkills().getLevelForXp(statId)
				: ((NPC) target).getCombatDefinitions().getLevels().get(getSkillFromId(statId));

		int maxDrain = (int) Math.floor(base * .25);
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
				case 0 -> npc.lowerAttack(percent, .25);
				case 1 -> npc.lowerDefense(percent,.25);
				case 2 -> npc.lowerStrength(percent, .25);
				case 4 -> npc.lowerRange(percent, .25);
				case 6 -> npc.lowerMagic(percent, .25);
			}
        }
	}

	private static void grantStatBoost(Player player, int statId, int stacks)  {
		GenericAttribMap attribs = player.getTempAttribs();

		String boostKey = LEECH_BOOST_PREFIX + statId;
		String timerKey = LEECH_BOOST_TIMER_PREFIX + statId;
		String procKey = LEECH_BOOST_PROC_PREFIX + statId;

		int currentStacks = attribs.getI(boostKey, 0);
		if (currentStacks < MAX_LEECH_STACKS) {
			currentStacks++;
			attribs.setI(boostKey, currentStacks);
		}

		List<Long> times = attribs.getO(timerKey);
		if (times == null)
			times = new ArrayList<>();

		Map<Integer, Long> procMap = attribs.getO(procKey);
		if (procMap == null) {
			procMap = new HashMap<>();
			attribs.setO(procKey, procMap);
		}
		times.add(System.currentTimeMillis());
		attribs.setO(timerKey, times);
		procMap.put(player.getIndex(), System.currentTimeMillis());

		applyStatBoost(player, statId, currentStacks);
	}

	private static void applyStatBoost(Player player, int statId, int stacks) {
		if (stacks <= 0)
			return;

		int percent =   STACK_LEECH_BOOST_PERCENT * stacks;
		if (percent > 5)
			percent = 5;

		PrayerManager.StatMod statMod = switch (statId) {
			case 0 -> PrayerManager.StatMod.ATTACK;
			case 1 -> PrayerManager.StatMod.DEFENSE;
			case 2 -> PrayerManager.StatMod.STRENGTH;
			case 4 -> PrayerManager.StatMod.RANGE;
			case 6 -> PrayerManager.StatMod.MAGE;
			default -> null;
		};

		if (statMod != null)
			player.getPrayer().setStatMod(statMod, percent);
	}

	public static void playLeechEffect(Player player, Entity target, int castSpotAnim, int projectileID, int targetSpotAnim, int soundId) {
		player.sync(12565, castSpotAnim);
		player.soundEffect(target, soundId, true);

		player.getPrayer().curseProjectile(player, target, projectileID, targetSpotAnim, true);
	}

	public static void tickLeechDecay(Entity target) {
		long now = System.currentTimeMillis();

		for (LeechType type : LeechType.values()) {
			int statID = type.skillId;
			String drainKey = LEECH_DRAIN_PREFIX + statID;
			String timerKey = LEECH_DRAIN_TIMER_PREFIX + statID;
			String procKey = LEECH_PROC_PREFIX + statID;
			String decayKey = LEECH_DECAY_PREFIX + statID;

			Map<Integer, Integer> drainMap = target.getTempAttribs().getO(drainKey);
			Map<Integer, List<Long>> timerMap = target.getTempAttribs().getO(timerKey);
			Map<Integer, Long> procMap = target.getTempAttribs().getO(procKey);

			if (drainMap == null || timerMap == null || procMap == null)
				continue;

			Iterator<Map.Entry<Integer, List<Long>>> it = timerMap.entrySet().iterator();
			while (it.hasNext()) {
				var entry = it.next();
				int attacker = entry.getKey();
				List<Long> stackTimes = entry.getValue();
				long lastProc = procMap.getOrDefault(attacker, 0L);
				Map<Integer,Long> decayMap = target.getTempAttribs().getO(decayKey);
				if (decayMap == null) {
					decayMap = new HashMap<>();
					target.getTempAttribs().setO(decayKey, decayMap);
				}
				long lastDecay = decayMap.getOrDefault(attacker, 0L);

				if (now - lastProc < INACTIVITY_TIMEOUT || stackTimes.isEmpty())
					continue;
				if (now - lastDecay < LEECH_DECAY_TIME  || now - stackTimes.get(0) < LEECH_DECAY_TIME)
					continue;

				stackTimes.remove(0);
				int newStack = Math.max(0, drainMap.getOrDefault(attacker, 1) - 1);
				drainMap.put(attacker, newStack);
				decayMap.put(attacker, now);

				if (newStack == 0) {
					it.remove();
					drainMap.remove(attacker);
					procMap.remove(attacker);
					restoreLeechDrain(target, type.skillId, true);
				} else {
					restoreLeechDrain(target, type.skillId, false);
				}
			}
		}
	}

	public static void tickLeechBoostDecay(Player player) {
		long now = System.currentTimeMillis();

		for (LeechType type : LeechType.values()) {
			int statId = type.skillId;
			String boostKey = LEECH_BOOST_PREFIX + statId;
			String timerKey = LEECH_BOOST_TIMER_PREFIX + statId;
			String procKey = LEECH_BOOST_PROC_PREFIX + statId;
			String decayKey = LEECH_BOOST_DECAY_PREFIX + statId;
			List<Long> stackTimes = player.getTempAttribs().getO(timerKey);
			if (stackTimes == null || stackTimes.isEmpty())
				continue;

			Map<Integer, Long> procMap = player.getTempAttribs().getO(procKey);
			Map<Integer, Long> decayMap = player.getTempAttribs().getO(decayKey);
			if (procMap == null)
				continue;
			if (decayMap == null) {
				decayMap = new HashMap<>();
				player.getTempAttribs().setO(decayKey, decayMap);
			}
			long lastProc = procMap.getOrDefault(player.getIndex(), 0L);
			long lastDecay = decayMap.getOrDefault(player.getIndex(), 0L);

			if (now - lastProc < INACTIVITY_TIMEOUT)
				continue;

			if (now - lastDecay < LEECH_DECAY_TIME || now - stackTimes.get(0) < LEECH_DECAY_TIME)
				continue;

			stackTimes.remove(0);
			int currentStacks = player.getTempAttribs().getI(boostKey, 1) - 1;
			//System.out.println("Current Boosts on player: " + currentStacks + "/");
			if (currentStacks <= 0 || stackTimes.isEmpty()) {
				player.getTempAttribs().removeI(boostKey);
				player.getTempAttribs().removeO(timerKey);
				player.getTempAttribs().removeO(procKey);
				player.getTempAttribs().removeO(decayKey);
				restoreLeechBoost(player, type.skillId, true);
			} else {
				player.getTempAttribs().setI(boostKey, currentStacks);
				decayMap.put(player.getIndex(), now);
				player.getTempAttribs().setO(decayKey, decayMap);
				restoreLeechBoost(player, type.skillId, false);
			}
		}
	}

	private static void restoreLeechDrain(Entity target, int statId, boolean isLast) {
		double percent = STACK_LEECH_DRAIN_PERCENT;
		if (isLast) percent += INITIAL_LEECH_DRAIN_PERCENT;

		int base = getBaseLevel(target, statId);
		int current = getCurrentLevel(target, statId);
		int restore = (int) Math.round(base * percent / 100.0);
		int newLevel = Math.min(base, current + restore);
		setLevel(target, statId, newLevel);
	}

	private static void restoreLeechBoost(Player player, int statId, boolean isLast) {
        PrayerManager.StatMod statMod = switch (statId) {
			case 0 -> PrayerManager.StatMod.ATTACK;
			case 1 -> PrayerManager.StatMod.DEFENSE;
			case 2 -> PrayerManager.StatMod.STRENGTH;
			case 4 -> PrayerManager.StatMod.RANGE;
			case 6 -> PrayerManager.StatMod.MAGE;
			default -> null;
		};

		if (statMod != null)
			player.getPrayer().decreaseStatMod(statMod, STACK_LEECH_BOOST_PERCENT);
	}

	public static void clearLeechBoost(Player player, int statId) {
		String boostKey = LEECH_BOOST_PREFIX + statId;
		String timerKey = LEECH_BOOST_TIMER_PREFIX + statId;
		String procKey = LEECH_BOOST_PROC_PREFIX + statId;
		String decayKey = LEECH_BOOST_DECAY_PREFIX + statId;

		player.getTempAttribs().removeI(boostKey);
		player.getTempAttribs().removeO(timerKey);
		player.getTempAttribs().removeO(procKey);
		player.getTempAttribs().removeO(decayKey);
	}

	public static NPCCombatDefinitions.Skill getSkillFromId(int statId) {
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
