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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.minecraftpixeldungeon.Assets;
import com.minecraftpixeldungeon.Encryption;
import com.minecraftpixeldungeon.MinecraftPixelDungeon;
import com.minecraftpixeldungeon.scenes.GameScene;
import com.minecraftpixeldungeon.scenes.PixelScene;
import com.minecraftpixeldungeon.ui.CheckBox;
import com.minecraftpixeldungeon.ui.OptionSlider;
import com.minecraftpixeldungeon.ui.RedButton;
import com.minecraftpixeldungeon.ui.Toolbar;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import android.os.Environment;
import android.widget.Toast;

public class WndSettings extends WndTabbed {
	private static final String TXT_SWITCH_PORT = "Switch to portrait";
	private static final String TXT_SWITCH_LAND = "Switch to landscape";

	private static final int WIDTH = 112;
	private static final int HEIGHT = 112;
	private static final int SLIDER_HEIGHT = 25;
	private static final int BTN_HEIGHT = 20;
	private static final int GAP_SML = 2;
	private static final int GAP_LRG = 10;

	private ScreenTab screen;
	private UITab ui;
	private AudioTab audio;
	private SaveTab saves;

//	private RedButton btnExportInfo;
//	private RedButton btnExportFiles;
	
	private RedButton btnImportInfo;
	private RedButton btnImportFiles;

	public WndSettings() {
		super();

		screen = new ScreenTab();
		add(screen);

		ui = new UITab();
		add(ui);

		audio = new AudioTab();
		add(audio);

		saves = new SaveTab();
		add(saves);

		add(new LabeledTab("Screen") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				screen.visible = screen.active = value;
			}
		});

		add(new LabeledTab("UI") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				ui.visible = ui.active = value;
			}
		});

		add(new LabeledTab("Audio") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				audio.visible = audio.active = value;
			}
		});

		add(new LabeledTab("Save") {
			@Override
			protected void select(boolean value) {
				super.select(value);
				saves.visible = saves.active = value;
			}
		});

		resize(WIDTH, HEIGHT);

		layoutTabs();

		select(0);

	}

	private class ScreenTab extends Group {

		public ScreenTab() {
			super();

			OptionSlider scale = new OptionSlider("Display Scale", (int) Math.ceil(2 * Game.density) + "X",
					PixelScene.maxDefaultZoom + "X", (int) Math.ceil(2 * Game.density), PixelScene.maxDefaultZoom) {
				@Override
				protected void onChange() {
					if (getSelectedValue() != MinecraftPixelDungeon.scale()) {
						MinecraftPixelDungeon.scale(getSelectedValue());
						MinecraftPixelDungeon.resetScene();
					}
				}
			};
			scale.setSelectedValue(PixelScene.defaultZoom);
			if ((int) Math.ceil(2 * Game.density) < PixelScene.maxDefaultZoom) {
				scale.setRect(0, 0, WIDTH, SLIDER_HEIGHT);
				add(scale);
			} else {
				scale.setRect(0, 0, 0, 0);
			}

			OptionSlider brightness = new OptionSlider("Brightness", "Dark", "Bright", -2, 4) {
				@Override
				protected void onChange() {
					MinecraftPixelDungeon.brightness(getSelectedValue());
				}
			};
			brightness.setSelectedValue(MinecraftPixelDungeon.brightness());
			brightness.setRect(0, scale.bottom() + GAP_SML, WIDTH, SLIDER_HEIGHT);
			add(brightness);

			CheckBox chkImmersive = new CheckBox("Hide Software Keys") {
				@Override
				protected void onClick() {
					super.onClick();
					MinecraftPixelDungeon.immerse(checked());
				}
			};
			chkImmersive.setRect(0, brightness.bottom() + GAP_LRG, WIDTH, BTN_HEIGHT);
			chkImmersive.checked(MinecraftPixelDungeon.immersed());
			chkImmersive.enable(android.os.Build.VERSION.SDK_INT >= 19);
			add(chkImmersive);

			RedButton btnOrientation = new RedButton(
					MinecraftPixelDungeon.landscape() ? TXT_SWITCH_PORT : TXT_SWITCH_LAND) {
				@Override
				protected void onClick() {
					MinecraftPixelDungeon.landscape(!MinecraftPixelDungeon.landscape());
				}
			};
			btnOrientation.setRect(0, chkImmersive.bottom() + GAP_LRG, WIDTH, BTN_HEIGHT);
			add(btnOrientation);
		}
	}

	private class UITab extends Group {

		public UITab() {
			super();

			BitmapText barDesc = PixelScene.createText("Toolbar Mode:", 9);
			barDesc.measure();
			barDesc.x = (WIDTH - barDesc.width()) / 2;
			add(barDesc);

			RedButton btnSplit = new RedButton("Split") {
				@Override
				protected void onClick() {
					MinecraftPixelDungeon.toolbarMode(Toolbar.Mode.SPLIT.name());
					Toolbar.updateLayout();
				}
			};
			btnSplit.setRect(1, barDesc.y + barDesc.height(), 36, BTN_HEIGHT);
			add(btnSplit);

			RedButton btnGrouped = new RedButton("Group") {
				@Override
				protected void onClick() {
					MinecraftPixelDungeon.toolbarMode(Toolbar.Mode.GROUP.name());
					Toolbar.updateLayout();
				}
			};
			btnGrouped.setRect(btnSplit.right() + 1, barDesc.y + barDesc.height(), 36, BTN_HEIGHT);
			add(btnGrouped);

			RedButton btnCentered = new RedButton("Center") {
				@Override
				protected void onClick() {
					MinecraftPixelDungeon.toolbarMode(Toolbar.Mode.CENTER.name());
					Toolbar.updateLayout();
				}
			};
			btnCentered.setRect(btnGrouped.right() + 1, barDesc.y + barDesc.height(), 36, BTN_HEIGHT);
			add(btnCentered);

			CheckBox chkFlipToolbar = new CheckBox("Flip Toolbar") {
				@Override
				protected void onClick() {
					super.onClick();
					MinecraftPixelDungeon.flipToolbar(checked());
					Toolbar.updateLayout();
				}
			};
			chkFlipToolbar.setRect(0, btnGrouped.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
			chkFlipToolbar.checked(MinecraftPixelDungeon.flipToolbar());
			add(chkFlipToolbar);

			CheckBox chkFlipTags = new CheckBox("Flip Indicators") {
				@Override
				protected void onClick() {
					super.onClick();
					MinecraftPixelDungeon.flipTags(checked());
					GameScene.layoutTags();
				}
			};
			chkFlipTags.setRect(0, chkFlipToolbar.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
			chkFlipTags.checked(MinecraftPixelDungeon.flipTags());
			add(chkFlipTags);

			OptionSlider slots = new OptionSlider("Quickslots", "0", "4", 0, 4) {
				@Override
				protected void onChange() {
					MinecraftPixelDungeon.quickSlots(getSelectedValue());
					Toolbar.updateLayout();
				}
			};
			slots.setSelectedValue(MinecraftPixelDungeon.quickSlots());
			slots.setRect(0, chkFlipTags.bottom() + GAP_LRG, WIDTH, SLIDER_HEIGHT);
			add(slots);
		}

	}

	private class AudioTab extends Group {

		public AudioTab() {
			OptionSlider musicVol = new OptionSlider("Music Volume", "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					Music.INSTANCE.volume(getSelectedValue() / 10f);
					MinecraftPixelDungeon.musicVol(getSelectedValue());
				}
			};
			musicVol.setSelectedValue(MinecraftPixelDungeon.musicVol());
			musicVol.setRect(0, 0, WIDTH, SLIDER_HEIGHT);
			add(musicVol);

			CheckBox musicMute = new CheckBox("Mute Music") {
				@Override
				protected void onClick() {
					super.onClick();
					MinecraftPixelDungeon.music(!checked());
				}
			};
			musicMute.setRect(0, musicVol.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
			musicMute.checked(!MinecraftPixelDungeon.music());
			add(musicMute);

			OptionSlider SFXVol = new OptionSlider("SFX Volume", "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					Sample.INSTANCE.volume(getSelectedValue() / 10f);
					MinecraftPixelDungeon.SFXVol(getSelectedValue());
				}
			};
			SFXVol.setSelectedValue(MinecraftPixelDungeon.SFXVol());
			SFXVol.setRect(0, musicMute.bottom() + GAP_LRG, WIDTH, SLIDER_HEIGHT);
			add(SFXVol);

			CheckBox btnSound = new CheckBox("Mute SFX") {
				@Override
				protected void onClick() {
					super.onClick();
					MinecraftPixelDungeon.soundFx(!checked());
					Sample.INSTANCE.play(Assets.SND_CLICK);
				}
			};
			btnSound.setRect(0, SFXVol.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
			btnSound.checked(!MinecraftPixelDungeon.soundFx());
			add(btnSound);

			resize(WIDTH, (int) btnSound.bottom());
		}

	}

	private class SaveTab extends Group {
		public SaveTab() {
			super();

//			btnExportInfo = new RedButton("") {
//				@Override
//				protected void onClick() {
//
//				}
//			};
//
//			btnExportFiles = new RedButton("Export save files") {
//				@Override
//				protected void onClick() {
//					exportSaveFiles();
//				}
//			};
//			btnExportFiles.enable(!MinecraftPixelDungeon.savesExported());
//			btnExportFiles.setRect(0, 0, WIDTH, BTN_HEIGHT);
//			add(btnExportFiles);
//
//			btnExportInfo.enable(false);
//			btnExportInfo.setRect(0, btnExportFiles.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
//			add(btnExportInfo);
			
			btnImportInfo = new RedButton(""){
				@Override
				protected void onClick(){
					
				}
			};
			
			btnImportFiles = new RedButton("Import save files") {
				@Override
				protected void onClick() {
					importSaveFiles();
				}
			};
			
			btnImportFiles.enable(!MinecraftPixelDungeon.savesImported());
			btnImportFiles.setRect(0, 0, WIDTH, BTN_HEIGHT);
			add(btnImportFiles);
			
			btnImportInfo.enable(false);
			btnImportInfo.setRect(0, btnImportFiles.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
			add(btnImportInfo);
		}
	}

	private void exportSaveFiles() {
		String savePath = Environment.getExternalStorageDirectory().getPath() + "/minecraft-pd/";
		File savePathFile = new File(savePath);
		savePathFile.mkdirs();

		String[] filesToSave = Game.instance.fileList();
		for (int i = 0; i < filesToSave.length; i++) {
			File exportedFile = new File(savePath + filesToSave[i]);
			try {
				System.err.println(Environment.getExternalStorageState().toString());
				exportedFile.createNewFile();
				FileInputStream streamIn = Game.instance.openFileInput(filesToSave[i]);
				String bundleToExport = read(streamIn, true);
				FileOutputStream streamOut = new FileOutputStream(exportedFile);
				write(bundleToExport, streamOut);
				streamIn.close();
				streamOut.close();
			} catch (IOException e) {
				Toast.makeText(MinecraftPixelDungeon.instance, "Cannot access storage... State: " +
						 Environment.getExternalStorageState().toString(), Toast.LENGTH_LONG).show();

				e.printStackTrace();
			}
		}

//		MinecraftPixelDungeon.savesExported(true);
//		btnExportFiles.enable(!MinecraftPixelDungeon.savesExported());
//		btnExportInfo.text("Savefiles exported!");
//		saves.update();
	}

	private void importSaveFiles() {
		String savePath = Environment.getExternalStorageDirectory().getPath() + "/minecraft-pd/";
		File savePathFile = new File(savePath);
		if(!savePathFile.exists()){
			return;
		}

		String[] filesToSave = savePathFile.list();

		for (int i = 0; i < filesToSave.length; i++) {
			File importedFile = new File(savePath + filesToSave[i]);
			try {
				FileInputStream streamIn = new FileInputStream(importedFile);
				String bundleToImport = read(streamIn, false);
				
				if(bundleToImport.equalsIgnoreCase("Error")){
					throw new IOException("File could not be copied due to an error while decrypting it");
				}
				
				FileOutputStream streamOut = Game.instance.openFileOutput(filesToSave[i], Game.MODE_PRIVATE);
				write(bundleToImport, streamOut);
				streamIn.close();
				streamOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MinecraftPixelDungeon.savesImported(true);
		btnImportFiles.enable(!MinecraftPixelDungeon.savesImported());
		btnImportInfo.text("Savefiles imported!");
		saves.update();
	}

	private String read(InputStream stream, boolean encrypt) throws IOException {
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			ArrayList<String> text = new ArrayList<String>(5);
			String sCurrentLine;
			while((sCurrentLine = reader.readLine()) != null) {
				text.add(sCurrentLine);
			}
			
			String str = "";
			for(int i = 0; i < text.size(); i++){
				str += text.get(i);
			}

			reader.close();

			return Encryption.DeEncryption(str, encrypt);
		} catch (Exception e) {
			throw new IOException();
		}
	}

	private boolean write(String str, OutputStream stream) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
			writer.write(str);
			writer.close();

			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
