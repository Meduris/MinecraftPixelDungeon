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
import com.minecraftpixeldungeon.actors.buffs.Buff;
import com.minecraftpixeldungeon.actors.buffs.Paralysis;
import com.minecraftpixeldungeon.effects.CellEmitter;
import com.minecraftpixeldungeon.effects.Speck;
import com.minecraftpixeldungeon.levels.Level;
import com.minecraftpixeldungeon.sprites.TrapSprite;
import com.minecraftpixeldungeon.utils.GLog;
import com.minecraftpixeldungeon.utils.Utils;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class RockfallTrap extends Trap {

	{
		name = "Rockfall trap";
		color = TrapSprite.GREY;
		shape = TrapSprite.DIAMOND;
	}

	@Override
	public void activate() {

		if (Dungeon.visible[ pos ]){
			CellEmitter.get( pos - Level.WIDTH ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
			Camera.main.shake( 3, 0.7f );
			Sample.INSTANCE.play( Assets.SND_ROCKS );
		}

		Char ch = Actor.findChar( pos );

		if (ch != null){
			int damage = Random.NormalIntRange(5+Dungeon.depth, 10+Dungeon.depth*3);
			damage -= Random.IntRange( 0, ch.dr());
			ch.damage( Math.max(damage, 0) , this);

			Buff.prolong( ch, Paralysis.class, Paralysis.duration(ch)*2);

			if (!ch.isAlive() && ch == Dungeon.hero){
				Dungeon.fail(Utils.format(ResultDescriptions.TRAP, name));
				GLog.n("You were crushed by the rockfall trap...");
			}
		}
	}

	@Override
	public String desc() {
		return "This trap is connected to a series of loose rocks above, " +
				"triggering it will cause them to come crashing down.";
	}
}
