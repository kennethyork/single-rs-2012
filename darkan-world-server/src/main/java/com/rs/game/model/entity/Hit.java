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
package com.rs.game.model.entity;

import com.rs.game.model.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public final class Hit {

	public enum HitLook {
		MISSED(8),
		TRUE_DAMAGE(3),
		MELEE_DAMAGE(0),
		RANGE_DAMAGE(1),
		MAGIC_DAMAGE(2),
		REFLECTED_DAMAGE(4),
		ABSORB_DAMAGE(5),
		POISON_DAMAGE(6),
		DISEASE_DAMAGE(7),
		HEALED_DAMAGE(9),
        MELEE_CRIT(10),
        RANGE_CRIT(11),
        MAGIC_CRIT(12),
		CANNON_DAMAGE(13),
        MELEE_DAMAGE_OTHER(14),
        RANGE_DAMAGE_OTHER(15),
        MAGIC_DAMAGE_OTHER(16),
        TRUE_DAMAGE_OTHER(17),
        REFLECTED_DAMAGE_OTHER(18),
        ABSORB_DAMAGE_OTHER(19),
        POISON_DAMAGE_OTHER(20),
        DISEASE_DAMAGE_OTHER(21),
        BLANK_22(22),
        HEALED_DAMAGE_OTHER(23),
        MELEE_CRIT_OTHER(24),
        RANGE_CRIT_OTHER(25),
        MAGIC_CRIT_OTHER(26),
        CANNON_DAMAGE_OTHER(27),
        POLYPORE_DAMAGE(28),
        BOOGIE_BOW_100K(29),
        BOOGIE_BOW_150K(30),
        BOOGIE_BOW_200K(31),
        BOOGIE_BOW_250K(32),
        BOOGIE_BOW_300K(33),
        BOOGIE_BOW_350K(34),
        BOOGIE_BOW_400K(35),
        BOOGIE_BOW_450K(36),
        BOOGIE_BOW_500K(37),
        BOOGIE_BOW_550K(38),
        BOOGIE_BOW_600K(39),
        BOOGIE_BOW_650K(40),
        BOOGIE_BOW_700K(41),
        BOOGIE_BOW_750K(42),
        BOOGIE_BOW_800K(43),
        BOOGIE_BOW_850K(44),
        BOOGIE_BOW_900K(45),
        BOOGIE_BOW_950K(46),
        BOOGIE_BOW_1M(47),
        ;

		private final int mark;

		HitLook(int mark) {
			this.mark = mark;
		}

		public int getMark() {
			return mark;
		}
	}

	private Entity source;
	private HitLook look;

	private int maxHit;
	private int damage;
    private String text = null;
	private boolean critical;
	private Hit soaking;
	private final int delay;
	private final Map<String, Object> data = new HashMap<>();

	public void setCriticalMark() {
		critical = true;
	}

	public void setHealHit() {
		look = HitLook.HEALED_DAMAGE;
		critical = false;
	}

    public Hit setText(String text) {
        this.text = text;
        return this;
    }

	public Hit(int damage, HitLook look) {
		this(null, damage, look, 0);
	}

	public Hit(Entity source, int damage, HitLook look) {
		this(source, damage, look, 0);
	}

	public Hit(Entity source, int damage, HitLook look, int delay) {
		this.source = source;
		this.damage = damage;
		this.look = look;
		this.delay = delay;
	}

	public boolean missed() {
		return damage == 0;
	}

	public boolean interactingWith(Player player, Entity victm) {
		return player == victm || player == source;
	}

	public int getMark(Player player, Entity victm) {
		if (HitLook.HEALED_DAMAGE == look)
			return look.getMark();
		if (damage == 0)
			return HitLook.MISSED.getMark();
		int mark = look.getMark();
		if (critical)
			mark += 10;
		if (!interactingWith(player, victm))
			mark += 14;
		return mark;
	}

	public Hit setMaxHit(int maxHit) {
		this.maxHit = maxHit;
		return this;
	}

    public String getText() {
        return text;
    }

	public HitLook getLook() {
		return look;
	}

	public int getDamage() {
		return damage;
	}

	public Hit setDamage(int damage) {
		this.damage = damage;
		return this;
	}

	public Entity getSource() {
		return source;
	}

	public Hit setSource(Entity source) {
		this.source = source;
		return this;
	}

	public Hit setLook(HitLook look) {
		this.look = look;
		return this;
	}

	public Hit getSoaking() {
		return soaking;
	}

	private void setSoaking(Hit soaking) {
		this.soaking = soaking;
	}

	public Hit addSoaking(int damage) {
		if (soaking == null)
			setSoaking(new Hit(source, damage, HitLook.ABSORB_DAMAGE));
		else
			soaking.damage += damage;
        return this;
	}

	public void soakDamage(double reductionPerc) {
		int reduction = (int) ((double) this.damage * reductionPerc);
		if (reduction <= 0)
			return;
		this.damage -= reduction;
		addSoaking(reduction);
	}

	public int getDelay() {
		return delay;
	}

	public int getMaxHit() {
		return maxHit;
	}

	public Hit setData(String key, Object obj) {
		data.put(key, obj);
		return this;
	}

	public static Hit miss(Entity source) {
		return new Hit(source, 0, HitLook.MISSED);
	}

	public static Hit melee(Entity source, int damage) {
		return new Hit(source, damage, HitLook.MELEE_DAMAGE);
	}

	public static Hit range(Entity source, int damage) {
		return new Hit(source, damage, HitLook.RANGE_DAMAGE);
	}

	public static Hit magic(Entity source, int damage) {
		return new Hit(source, damage, HitLook.MAGIC_DAMAGE);
	}

	public static Hit flat(Entity source, int damage) {
		return new Hit(source, damage, HitLook.TRUE_DAMAGE);
	}

	public static Hit heal(Entity source, int damage) {
		return new Hit(source, damage, HitLook.HEALED_DAMAGE);
	}

	public <T> T getData(String key, Class<T> clazz) {
		try {
			if (data.containsKey(key))
				return clazz.cast(data.get(key));
		} catch(ClassCastException e) {
			return null;
		}
		return null;
	}

	public Object getData(String key) {
		if (data.containsKey(key))
			return data.get(key);
		return null;
	}
}
