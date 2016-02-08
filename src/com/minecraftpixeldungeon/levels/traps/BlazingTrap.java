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
import com.minecraftpixeldungeon.actors.blobs.Blob;
import com.minecraftpixeldungeon.actors.blobs.Fire;
import com.minecraftpixeldungeon.effects.CellEmitter;
import com.minecraftpixeldungeon.effects.particles.FlameParticle;
import com.minecraftpixeldungeon.levels.Level;
import com.minecraftpixeldungeon.scenes.GameScene;
import com.minecraftpixeldungeon.sprites.TrapSprite;
import com.watabou.noosa.audio.Sample;

public class BlazingTrap extends Trap {

	{
		name = "Blazing trap";
		color = TrapSprite.ORANGE;
		shape = TrapSprite.STARS;
	}


	@Override
	public void activate() {
		for (int i : Level.NEIGHBOURS9DIST2){
			if (Level.insideMap(pos+i) && !Level.solid[pos+i]) {
				if (Level.pit[pos+i] || Level.water[pos+i])
					GameScene.add(Blob.seed(pos + i, 1, Fire.class));
				else
					GameScene.add(Blob.seed(pos + i, 5, Fire.class));
				CellEmitter.get(pos + i).burst(FlameParticle.FACTORY, 5);
			}
		}
		Sample.INSTANCE.play(Assets.SND_BURNING);
	}

	@Override
	public String desc() {
		return  "Stepping on this trap will ignite a powerful chemical mixture, setting a wide area ablaze.";
	}
}
