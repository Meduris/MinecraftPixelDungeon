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
package com.minecraftpixeldungeon.windows;

import com.minecraftpixeldungeon.MinecraftPixelDungeon;
import com.minecraftpixeldungeon.items.Heap;
import com.minecraftpixeldungeon.items.Item;
import com.minecraftpixeldungeon.items.Heap.Type;
import com.minecraftpixeldungeon.items.artifacts.Artifact;
import com.minecraftpixeldungeon.items.rings.Ring;
import com.minecraftpixeldungeon.items.wands.Wand;
import com.minecraftpixeldungeon.scenes.PixelScene;
import com.minecraftpixeldungeon.sprites.ItemSprite;
import com.minecraftpixeldungeon.ui.ItemSlot;
import com.minecraftpixeldungeon.ui.Window;
import com.minecraftpixeldungeon.utils.Utils;
import com.watabou.noosa.BitmapTextMultiline;

public class WndInfoItem extends Window {
	
	private static final String TTL_CHEST           = "Chest";
	private static final String TTL_LOCKED_CHEST	= "Locked chest";
	private static final String TTL_CRYSTAL_CHEST	= "Crystal chest";
	private static final String TTL_TOMB			= "Tomb";
	private static final String TTL_SKELETON		= "Skeletal remains";
	private static final String TTL_REMAINS 		= "Heroes remains";
	private static final String TXT_WONT_KNOW		= "You won't know what's inside until you open it!";
	private static final String TXT_NEED_KEY		= TXT_WONT_KNOW + " But to open it you need a golden key.";
	private static final String TXT_INSIDE			= "You can see %s inside, but to open the chest you need a golden key.";
	private static final String TXT_OWNER	=
		"This ancient tomb may contain something useful, " +
		"but its owner will most certainly object to checking.";
	private static final String TXT_SKELETON =
		"This is all that's left of some unfortunate adventurer. " +
		"Maybe it's worth checking for any valuables.";
	private static final String TXT_REMAINS =
		"This is all that's left from one of your predecessors. " +
		"Maybe it's worth checking for any valuables.";
	
	private static final float GAP	= 2;
	
	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	
	public WndInfoItem( Heap heap ) {
		
		super();
		
		if (heap.type == Heap.Type.HEAP || heap.type == Heap.Type.FOR_SALE) {
			
			Item item = heap.peek();
			
			int color = TITLE_COLOR;
			if (item.levelKnown && item.level() > 0) {
				color = ItemSlot.UPGRADED;
			} else if (item.levelKnown && item.level() < 0) {
				color = ItemSlot.DEGRADED;
			}
			fillFields( item.image(), item.glowing(), color, item.toString(), item.info() );
			
		} else {
			
			String title;
			String info;
			
			if (heap.type == Type.CHEST || heap.type == Type.MIMIC) {
				title = TTL_CHEST;
				info = TXT_WONT_KNOW;
			} else if (heap.type == Type.TOMB) {
				title = TTL_TOMB;
				info = TXT_OWNER;
			} else if (heap.type == Type.SKELETON) {
				title = TTL_SKELETON;
				info = TXT_SKELETON;
			} else if (heap.type == Type.REMAINS) {
				title = TTL_REMAINS;
				info = TXT_REMAINS;
			} else if (heap.type == Type.CRYSTAL_CHEST) {
				title = TTL_CRYSTAL_CHEST;
				if (heap.peek() instanceof Artifact)
					info = Utils.format( TXT_INSIDE, "an artifact" );
				else if (heap.peek() instanceof Wand)
					info = Utils.format( TXT_INSIDE, "a wand" );
				else if (heap.peek() instanceof Ring)
					info = Utils.format( TXT_INSIDE, "a ring" );
				else
					info = Utils.format( TXT_INSIDE, Utils.indefinite( heap.peek().name() ) );
			} else {
				title = TTL_LOCKED_CHEST;
				info = TXT_NEED_KEY;
			}
			
			fillFields( heap.image(), heap.glowing(), TITLE_COLOR, title, info );
			
		}
	}
	
	public WndInfoItem( Item item ) {
		
		super();
		
		int color = TITLE_COLOR;
		if (item.levelKnown && item.level() > 0) {
			color = ItemSlot.UPGRADED;
		} else if (item.levelKnown && item.level() < 0) {
			color = ItemSlot.DEGRADED;
		}
		
		fillFields( item.image(), item.glowing(), color, item.toString(), item.info() );
	}
	
	private void fillFields( int image, ItemSprite.Glowing glowing, int titleColor, String title, String info ) {

		int width = MinecraftPixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( image, glowing ) );
		titlebar.label( Utils.capitalize( title ), titleColor );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );
		
		BitmapTextMultiline txtInfo = PixelScene.createMultiline( info, 6 );
		txtInfo.maxWidth = width;
		txtInfo.measure();
		txtInfo.x = titlebar.left();
		txtInfo.y = titlebar.bottom() + GAP;
		add( txtInfo );
		
		resize( width, (int)(txtInfo.y + txtInfo.height()) );
	}
}
