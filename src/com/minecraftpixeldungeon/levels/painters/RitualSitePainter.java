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
package com.minecraftpixeldungeon.levels.painters;

import com.minecraftpixeldungeon.Assets;
import com.minecraftpixeldungeon.Dungeon;
import com.minecraftpixeldungeon.items.keys.IronKey;
import com.minecraftpixeldungeon.items.quest.CeremonialCandle;
import com.minecraftpixeldungeon.levels.Level;
import com.minecraftpixeldungeon.levels.Room;
import com.minecraftpixeldungeon.levels.Terrain;
import com.minecraftpixeldungeon.ui.CustomTileVisual;
import com.watabou.utils.Point;

public class RitualSitePainter extends Painter {

	public static void paint( Level level, Room room) {

		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}

		fill(level, room, Terrain.WALL);
		fill(level, room, 1, Terrain.EMPTY);

		RitualMarker vis = new RitualMarker();
		Point c = room.center();
		vis.pos(c.x - 1, c.y - 1);

		level.customTiles.add(vis);

		fill(level, c.x-1, c.y-1, 3, 3, Terrain.EMPTY_DECO);

		level.addItemToSpawn(new CeremonialCandle());
		level.addItemToSpawn(new CeremonialCandle());
		level.addItemToSpawn(new CeremonialCandle());
		level.addItemToSpawn(new CeremonialCandle());

		CeremonialCandle.ritualPos = c.x + (Level.WIDTH * c.y);
	}

	public static class RitualMarker extends CustomTileVisual{

		{
			name = "Ritual marker";

			tx = Assets.PRISON_QUEST;
			txX = txY = 0;
			tileW = tileH = 3;
		}

		@Override
		public String desc() {
			return "A painted marker for some dark ritual. Candles are usually placed on the four corners.";
		}
	}

}
