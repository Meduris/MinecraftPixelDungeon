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
import com.minecraftpixeldungeon.ResultDescriptions;
import com.minecraftpixeldungeon.actors.Actor;
import com.minecraftpixeldungeon.actors.Char;
import com.minecraftpixeldungeon.actors.buffs.Chill;
import com.minecraftpixeldungeon.effects.Splash;
import com.minecraftpixeldungeon.items.Heap;
import com.minecraftpixeldungeon.sprites.TrapSprite;
import com.minecraftpixeldungeon.utils.GLog;
import com.minecraftpixeldungeon.utils.Utils;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ChillingTrap extends Trap{

	{
		name = "Chilling trap";
		color = TrapSprite.WHITE;
		shape = TrapSprite.DOTS;
	}

	@Override
	public void activate() {
		if (Dungeon.visible[ pos ]){
			Splash.at( sprite.center(), 0xFFB2D6FF, 5);
			Sample.INSTANCE.play( Assets.SND_SHATTER );
		}

		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null) heap.freeze();

		Char ch = Actor.findChar( pos );
		if (ch != null){
			Chill.prolong(ch, Chill.class, 5f + Random.Int(Dungeon.depth));
			ch.damage(Random.NormalIntRange(1 , Dungeon.depth), this);
			if (!ch.isAlive() && ch == Dungeon.hero){
				Dungeon.fail( Utils.format(ResultDescriptions.TRAP, name) );
				GLog.n("You succumb to the chilling trap...");
			}
		}
	}

	@Override
	public String desc() {
		return "When activated, chemicals in this trap will trigger a snap-frost at its location.";
	}
}
