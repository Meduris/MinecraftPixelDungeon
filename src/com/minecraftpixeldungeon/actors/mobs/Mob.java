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
package com.minecraftpixeldungeon.actors.mobs;

import com.minecraftpixeldungeon.Badges;
import com.minecraftpixeldungeon.Challenges;
import com.minecraftpixeldungeon.Dungeon;
import com.minecraftpixeldungeon.Statistics;
import com.minecraftpixeldungeon.actors.Actor;
import com.minecraftpixeldungeon.actors.Char;
import com.minecraftpixeldungeon.actors.buffs.Amok;
import com.minecraftpixeldungeon.actors.buffs.Buff;
import com.minecraftpixeldungeon.actors.buffs.Corruption;
import com.minecraftpixeldungeon.actors.buffs.Hunger;
import com.minecraftpixeldungeon.actors.buffs.Sleep;
import com.minecraftpixeldungeon.actors.buffs.SoulMark;
import com.minecraftpixeldungeon.actors.buffs.Terror;
import com.minecraftpixeldungeon.actors.hero.Hero;
import com.minecraftpixeldungeon.actors.hero.HeroSubClass;
import com.minecraftpixeldungeon.effects.Speck;
import com.minecraftpixeldungeon.effects.Surprise;
import com.minecraftpixeldungeon.effects.Wound;
import com.minecraftpixeldungeon.items.Generator;
import com.minecraftpixeldungeon.items.Item;
import com.minecraftpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.minecraftpixeldungeon.items.rings.RingOfAccuracy;
import com.minecraftpixeldungeon.items.rings.RingOfWealth;
import com.minecraftpixeldungeon.levels.Level;
import com.minecraftpixeldungeon.levels.Level.Feeling;
import com.minecraftpixeldungeon.sprites.CharSprite;
import com.minecraftpixeldungeon.utils.GLog;
import com.minecraftpixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public abstract class Mob extends Char {

	{
		actPriority = 2; //hero gets priority over mobs.
	}
	
	private static final String	TXT_DIED	= "You hear something died in the distance";
	
	protected static final String TXT_NOTICE1	= "?!";
	protected static final String TXT_RAGE		= "#$%^";
	protected static final String TXT_EXP		= "%+dEXP";

	public AiState SLEEPING     = new Sleeping();
	public AiState HUNTING		= new Hunting();
	public AiState WANDERING	= new Wandering();
	public AiState FLEEING		= new Fleeing();
	public AiState PASSIVE		= new Passive();
	public AiState state = SLEEPING;
	
	public Class<? extends CharSprite> spriteClass;
	
	protected int target = -1;
	
	protected int defenseSkill = 0;
	
	protected int EXP = 1;
	protected int maxLvl = Hero.MAX_LEVEL;
	
	protected Char enemy;
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;
	
	public boolean hostile = true;
	public boolean ally = false;
	
	private static final String STATE	= "state";
	private static final String SEEN	= "seen";
	private static final String TARGET	= "target";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );

		if (state == SLEEPING) {
			bundle.put( STATE, Sleeping.TAG );
		} else if (state == WANDERING) {
			bundle.put( STATE, Wandering.TAG );
		} else if (state == HUNTING) {
			bundle.put( STATE, Hunting.TAG );
		} else if (state == FLEEING) {
			bundle.put( STATE, Fleeing.TAG );
		} else if (state == PASSIVE) {
			bundle.put( STATE, Passive.TAG );
		}
		bundle.put( SEEN, enemySeen );
		bundle.put( TARGET, target );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );

		String state = bundle.getString( STATE );
		if (state.equals( Sleeping.TAG )) {
			this.state = SLEEPING;
		} else if (state.equals( Wandering.TAG )) {
			this.state = WANDERING;
		} else if (state.equals( Hunting.TAG )) {
			this.state = HUNTING;
		} else if (state.equals( Fleeing.TAG )) {
			this.state = FLEEING;
		} else if (state.equals( Passive.TAG )) {
			this.state = PASSIVE;
		}

		enemySeen = bundle.getBoolean( SEEN );

		target = bundle.getInt( TARGET );
	}
	
	public CharSprite sprite() {
		CharSprite sprite = null;
		try {
			sprite = spriteClass.newInstance();
		} catch (Exception e) {
		}
		return sprite;
	}
	
	@Override
	protected boolean act() {
		
		super.act();
		
		boolean justAlerted = alerted;
		alerted = false;
		
		sprite.hideAlert();
		
		if (paralysed > 0) {
			enemySeen = false;
			spend( TICK );
			return true;
		}
		
		enemy = chooseEnemy();
		
		boolean enemyInFOV = enemy != null && enemy.isAlive() && Level.fieldOfView[enemy.pos] && enemy.invisible <= 0;

		return state.act( enemyInFOV, justAlerted );
	}
	
	protected Char chooseEnemy() {

		Terror terror = buff( Terror.class );
		if (terror != null) {
			Char source = (Char)Actor.findById( terror.object );
			if (source != null) {
				return source;
			}
		}

		//resets target if: there is no current target, the target is dead, the target has been lost (wandering)
		//or if the mob is amoked/corrupted and targeting the hero (will try to target something else)
		if ( enemy == null || !enemy.isAlive() || state == WANDERING ||
				((buff( Amok.class ) != null || buff(Corruption.class) != null) && enemy == Dungeon.hero )) {

			HashSet<Char> enemies = new HashSet<Char>();

			//if the mob is amoked or corrupted...
			if ( buff(Amok.class) != null || buff(Corruption.class) != null) {

				//try to find an enemy mob to attack first.
				for (Mob mob : Dungeon.level.mobs)
					if (mob != this && Level.fieldOfView[mob.pos] && mob.hostile)
							enemies.add(mob);
				if (enemies.size() > 0) return Random.element(enemies);

				//try to find ally mobs to attack second.
				for (Mob mob : Dungeon.level.mobs)
					if (mob != this && Level.fieldOfView[mob.pos] && mob.ally)
						enemies.add(mob);
				if (enemies.size() > 0) return Random.element(enemies);

				//if there is nothing, go for the hero, unless corrupted, then go for nothing.
				if (buff(Corruption.class) != null) return null;
				else return Dungeon.hero;

			//if the mob is not amoked...
			} else {

				//try to find ally mobs to attack.
				for (Mob mob : Dungeon.level.mobs)
					if (mob != this && Level.fieldOfView[mob.pos] && mob.ally)
						enemies.add(mob);

				//and add the hero to the list of targets.
				enemies.add(Dungeon.hero);

				//target one at random.
				return Random.element(enemies);

			}

		} else
			return enemy;
	}

	protected boolean moveSprite( int from, int to ) {

		if (sprite.isVisible() && (Dungeon.visible[from] || Dungeon.visible[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.place( to );
			return true;
		}
	}
	
	@Override
	public void add( Buff buff ) {
		super.add( buff );
		if (buff instanceof Amok) {
			if (sprite != null) {
				sprite.showStatus( CharSprite.NEGATIVE, TXT_RAGE );
			}
			state = HUNTING;
		} else if (buff instanceof Terror) {
			state = FLEEING;
		} else if (buff instanceof Sleep) {
			state = SLEEPING;
			this.sprite().showSleep();
			postpone( Sleep.SWS );
		}
	}
	
	@Override
	public void remove( Buff buff ) {
		super.remove( buff );
		if (buff instanceof Terror) {
			sprite.showStatus( CharSprite.NEGATIVE, TXT_RAGE );
			state = HUNTING;
		}
	}
	
	protected boolean canAttack( Char enemy ) {
		return Level.adjacent( pos, enemy.pos );
	}
	
	protected boolean getCloser( int target ) {
		
		if (rooted) {
			return false;
		}
		
		int step = Dungeon.findPath( this, pos, target,
			Level.passable,
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean getFurther( int target ) {
		int step = Dungeon.flee( this, pos, target,
			Level.passable,
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) != null)
			sprite.add( CharSprite.State.PARALYSED );
	}

	@Override
	public void move( int step ) {
		super.move( step );
		
		if (!flying) {
			Dungeon.level.mobPress( this );
		}
	}
	
	protected float attackDelay() {
		return 1f;
	}
	
	protected boolean doAttack( Char enemy ) {
		
		boolean visible = Dungeon.visible[pos];
		
		if (visible) {
			sprite.attack( enemy.pos );
		} else {
			attack( enemy );
		}
				
		spend( attackDelay() );
		
		return !visible;
	}
	
	@Override
	public void onAttackComplete() {
		attack( enemy );
		super.onAttackComplete();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		if (enemySeen && paralysed == 0) {
			int defenseSkill = this.defenseSkill;
			int penalty = 0;
			for (Buff buff : enemy.buffs(RingOfAccuracy.Accuracy.class)) {
				penalty += ((RingOfAccuracy.Accuracy) buff).level;
			}
			if (penalty != 0 && enemy == Dungeon.hero)
				defenseSkill *= Math.pow(0.75, penalty);
			return defenseSkill;
		} else {
			return 0;
		}
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {
		if (!enemySeen && enemy == Dungeon.hero) {
			if (((Hero)enemy).subClass == HeroSubClass.ASSASSIN) {
				damage *= 1.34f;
				Wound.hit(this);
			} else {
				Surprise.hit(this);
			}
		}

		//become aggro'd by a corrupted enemy
		if (enemy.buff(Corruption.class) != null) {
			aggro(enemy);
			target = enemy.pos;
			if (state == SLEEPING || state == WANDERING)
				state = HUNTING;
		}

		if (buff(SoulMark.class) != null) {
			int restoration = Math.max(damage, HP);
			Dungeon.hero.buff(Hunger.class).satisfy(restoration*0.5f);
			Dungeon.hero.HP = (int)Math.ceil(Math.min(Dungeon.hero.HT, Dungeon.hero.HP+(restoration*0.25f)));
			Dungeon.hero.sprite.emitter().burst( Speck.factory(Speck.HEALING), 1 );
		}

		return damage;
	}

	public void aggro( Char ch ) {
		enemy = ch;
		if (state != PASSIVE){
			state = HUNTING;
		}
	}

	@Override
	public void damage( int dmg, Object src ) {

		Terror.recover( this );

		if (state == SLEEPING) {
			state = WANDERING;
		}
		alerted = true;
		
		super.damage( dmg, src );
	}
	
	
	@Override
	public void destroy() {
		
		super.destroy();
		
		Dungeon.level.mobs.remove( this );
		
		if (Dungeon.hero.isAlive()) {
			
			if (hostile) {
				Statistics.enemiesSlain++;
				Badges.validateMonstersSlain();
				Statistics.qualifiedForNoKilling = false;
				
				if (Dungeon.level.feeling == Feeling.DARK) {
					Statistics.nightHunt++;
				} else {
					Statistics.nightHunt = 0;
				}
				Badges.validateNightHunter();
			}

			int exp = exp();
			if (exp > 0) {
				Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, TXT_EXP, exp );
				Dungeon.hero.earnExp( exp );
			}
		}
	}

	public int exp() {
		return Dungeon.hero.lvl <= maxLvl ? EXP : 0;
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );

		float lootChance = this.lootChance;
		int bonus = 0;
		for (Buff buff : Dungeon.hero.buffs(RingOfWealth.Wealth.class)) {
			bonus += ((RingOfWealth.Wealth) buff).level;
		}

		lootChance *= Math.pow(1.1, bonus);
		
		if (Random.Float() < lootChance && Dungeon.hero.lvl <= maxLvl + 2) {
			Item loot = createLoot();
			if (loot != null)
				Dungeon.level.drop( loot , pos ).sprite.drop();
		}
		
		if (Dungeon.hero.isAlive() && !Dungeon.visible[pos]) {
			GLog.i( TXT_DIED );
		}
	}
	
	protected Object loot = null;
	protected float lootChance = 0;
	
	@SuppressWarnings("unchecked")
	protected Item createLoot() {
		Item item;
		if (loot instanceof Generator.Category) {

			item = Generator.random( (Generator.Category)loot );

		} else if (loot instanceof Class<?>) {

			item = Generator.random( (Class<? extends Item>)loot );

		} else {

			item = (Item)loot;

		}
		return item;
	}
	
	public boolean reset() {
		return false;
	}
	
	public void beckon( int cell ) {
		
		notice();
		
		if (state != HUNTING) {
			state = WANDERING;
		}
		target = cell;
	}
	
	public String description() {
		return "Real description is coming soon!";
	}
	
	public void notice() {
		sprite.showAlert();
	}
	
	public void yell( String str ) {
		GLog.n( "%s: \"%s\" ", name, str );
	}

	//returns true when a mob sees the hero, and is currently targeting them.
	public boolean focusingHero() {
		return enemySeen && (target == Dungeon.hero.pos);
	}

	public interface AiState {
		public boolean act( boolean enemyInFOV, boolean justAlerted );
		public String status();
	}

	protected class Sleeping implements AiState {

		public static final String TAG	= "SLEEPING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && Random.Int( distance( enemy ) + enemy.stealth() + (enemy.flying ? 2 : 0) ) == 0) {

				enemySeen = true;

				notice();
				state = HUNTING;
				target = enemy.pos;

				if (Dungeon.isChallenged( Challenges.SWARM_INTELLIGENCE )) {
					for (Mob mob : Dungeon.level.mobs) {
						if (mob != Mob.this) {
							mob.beckon( target );
						}
					}
				}

				spend( TIME_TO_WAKE_UP );

			} else {

				enemySeen = false;

				spend( TICK );

			}
			return true;
		}

		@Override
		public String status() {
			return Utils.format( "This %s is sleeping", name );
		}
	}

	protected class Wandering implements AiState {

		public static final String TAG	= "WANDERING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && (justAlerted || Random.Int( distance( enemy ) / 2 + enemy.stealth() ) == 0)) {

				enemySeen = true;

				notice();
				state = HUNTING;
				target = enemy.pos;

			} else {

				enemySeen = false;

				int oldPos = pos;
				if (target != -1 && getCloser( target )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					target = Dungeon.level.randomDestination();
					spend( TICK );
				}

			}
			return true;
		}

		@Override
		public String status() {
			return Utils.format( "This %s is wandering", name );
		}
	}

	protected class Hunting implements AiState {

		public static final String TAG	= "HUNTING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

				return doAttack( enemy );

			} else {

				if (enemyInFOV) {
					target = enemy.pos;
				}

				int oldPos = pos;
				if (target != -1 && getCloser( target )) {

					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else {

					spend( TICK );
					state = WANDERING;
					target = Dungeon.level.randomDestination();
					return true;
				}
			}
		}

		@Override
		public String status() {
			return Utils.format( "This %s is hunting", name );
		}
	}

	protected class Fleeing implements AiState {

		public static final String TAG	= "FLEEING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			//loses target when 0-dist rolls a 6 or greater.
			if (enemy == null || !enemyInFOV && 1 + Random.Int(Level.distance(pos, target)) >= 6){
				target = -1;
			} else {
				target = enemy.pos;
			}

			int oldPos = pos;
			if (target != -1 && getFurther( target )) {

				spend( 1 / speed() );
				return moveSprite( oldPos, pos );

			} else {

				spend( TICK );
				nowhereToRun();

				return true;
			}
		}

		protected void nowhereToRun() {
		}

		@Override
		public String status() {
			return Utils.format( "This %s is fleeing", name );
		}
	}

	protected class Passive implements AiState {

		public static final String TAG	= "PASSIVE";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = false;
			spend( TICK );
			return true;
		}

		@Override
		public String status() {
			return Utils.format( "This %s is passive", name );
		}
	}
}
