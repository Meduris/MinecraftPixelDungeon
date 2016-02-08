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
import com.minecraftpixeldungeon.effects.CellEmitter;
import com.minecraftpixeldungeon.effects.MagicMissile;
import com.minecraftpixeldungeon.effects.particles.ShadowParticle;
import com.minecraftpixeldungeon.levels.Level;
import com.minecraftpixeldungeon.mechanics.Ballistica;
import com.minecraftpixeldungeon.sprites.TrapSprite;
import com.minecraftpixeldungeon.utils.GLog;
import com.minecraftpixeldungeon.utils.Utils;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GrimTrap extends Trap {

	{
		name = "Grim trap";
		color = TrapSprite.GREY;
		shape = TrapSprite.LARGE_DOT;
	}

	@Override
	public Trap hide() {
		//cannot hide this trap
		return reveal();
	}

	@Override
	public void activate() {
		Char target = Actor.findChar(pos);

		//find the closest char that can be aimed at
		if (target == null){
			for (Char ch : Actor.chars()){
				Ballistica bolt = new Ballistica(pos, ch.pos, Ballistica.PROJECTILE);
				if (bolt.collisionPos == ch.pos &&
						(target == null || Level.distance(pos, ch.pos) < Level.distance(pos, target.pos))){
					target = ch;
				}
			}
		}

		if (target != null){
			final Char finalTarget = target;
			MagicMissile.shadow(target.sprite.parent, pos, target.pos, new Callback() {
				@Override
				public void call() {

					if (finalTarget == Dungeon.hero) {
						//almost kill the player
						if (((float)finalTarget.HP/finalTarget.HT) >= 0.9f){
							finalTarget.damage((finalTarget.HP-1), this);
						//kill 'em
						} else {
							finalTarget.damage(finalTarget.HP, this);
						}
						Sample.INSTANCE.play(Assets.SND_CURSED);
						if (!finalTarget.isAlive()) {
							Dungeon.fail(Utils.format(ResultDescriptions.TRAP, name));
							GLog.n("You were killed by the blast of a grim trap...");
						}
					} else {
						finalTarget.damage(finalTarget.HP, this);
						Sample.INSTANCE.play(Assets.SND_BURNING);
					}
					finalTarget.sprite.emitter().burst(ShadowParticle.UP, 10);
				}
			});
		} else {
			CellEmitter.get(pos).burst(ShadowParticle.UP, 10);
			Sample.INSTANCE.play(Assets.SND_BURNING);
		}
	}

	@Override
	public String desc() {
		return "Extremely powerful destructive magic is stored within this trap, enough to instantly kill all but the healthiest of heroes. " +
				"Triggering it will send a ranged blast of lethal magic towards the nearest character.";
	}
}
