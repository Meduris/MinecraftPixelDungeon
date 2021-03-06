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
package com.minecraftpixeldungeon.actors.buffs;

import com.minecraftpixeldungeon.Dungeon;
import com.minecraftpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class LockedFloor extends Buff {

	//the amount of turns remaining before beneficial passive effects turn off
	private float left = 50; //starts at 50 turns

	@Override
	public boolean act() {
		spend(TICK);

		if (!Dungeon.level.locked)
			detach();

		if (left >= 1)
			left --;

		return true;
	}

	public void addTime(float time){
		left += time;
	}

	public boolean regenOn(){
		return left >= 1;
	}

	private final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( LEFT, left );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat( LEFT );
	}

	@Override
	public int icon() {
		return BuffIndicator.LOCKED_FLOOR;
	}

	@Override
	public String toString() {
		return "Floor is Locked";
	}

	@Override
	public String desc() {
		return "The current floor is locked, and you are unable to leave it!\n" +
				"\n" +
				"While a floor is locked, you will not gain hunger or take damage from starving. In addition, " +
				"if you do not work towards defeating this floor's boss, passive regeneration effects will also stop.\n " +
				"\n" +
				"Additionally, if you are revived by an unblessed ankh while the floor is locked, then it will reset.\n" +
				"\n" +
				"Kill this floor's boss to break the lock.\n";
	}
}
