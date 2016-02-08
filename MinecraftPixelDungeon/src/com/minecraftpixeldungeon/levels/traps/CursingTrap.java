/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2015 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.minecraftpixeldungeon.levels.traps;

import com.minecraftpixeldungeon.Assets;
import com.minecraftpixeldungeon.Dungeon;
import com.minecraftpixeldungeon.actors.hero.Hero;
import com.minecraftpixeldungeon.effects.CellEmitter;
import com.minecraftpixeldungeon.effects.particles.ShadowParticle;
import com.minecraftpixeldungeon.items.*;
import com.minecraftpixeldungeon.items.armor.Armor;
import com.minecraftpixeldungeon.sprites.TrapSprite;
import com.minecraftpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class CursingTrap extends Trap {

	{
		name = "Cursing trap";
		color = TrapSprite.VIOLET;
		shape = TrapSprite.WAVES;
	}

	@Override
	public void activate() {
		if (Dungeon.visible[ pos ]) {
			CellEmitter.get(pos).burst(ShadowParticle.UP, 5);
			Sample.INSTANCE.play(Assets.SND_CURSED);
		}

		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null){
			for (Item item : heap.items){
				if (item.isUpgradable())
					item.cursed = item.cursedKnown = true;
			}
		}

		if (Dungeon.hero.pos == pos){
			Hero hero = Dungeon.hero;
			KindOfWeapon weapon = hero.belongings.weapon;
			Armor armor = hero.belongings.armor;
			KindofMisc misc1 = hero.belongings.misc1;
			KindofMisc misc2 = hero.belongings.misc2;
			if (weapon != null) weapon.cursed = weapon.cursedKnown = true;
			if (armor != null)  armor.cursed = armor.cursedKnown = true;
			if (misc1 != null)  misc1.cursed = misc1.cursedKnown = true;
			if (misc2 != null)  misc2.cursed = misc2.cursedKnown = true;
			EquipableItem.equipCursed(hero);
			GLog.n("Your worn equipment becomes cursed!");
		}
	}

	@Override
	public String desc() {
		return "This trap contains the same malevolent magic found in cursed equipment. " +
				"Triggering it will curse all worn items, and all items in the immediate area.";
	}
}
