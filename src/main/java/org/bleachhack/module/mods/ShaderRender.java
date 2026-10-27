/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;

import net.minecraft.util.Identifier;

/**
 * Cycles through the vanilla post effects that are still shipped in 1.21.11.
 * The old "super secret settings" shaders no longer exist in the game.
 */
public class ShaderRender extends Module {

	private Identifier lastId = null;

	private final List<Identifier> shaders = new ArrayList<>();

	public ShaderRender() {
		super("ShaderRender", KEY_UNBOUND, ModuleCategory.RENDER, "Cycles through the vanilla post effects.",
				new SettingMode("Shader", "Creeper", "Spider", "Blur", "Invert", "None").withDesc("Post effect to use."));

		shaders.add(Identifier.of("minecraft", "creeper"));
		shaders.add(Identifier.of("minecraft", "spider"));
		shaders.add(Identifier.of("minecraft", "blur"));
		shaders.add(Identifier.of("minecraft", "invert"));
		shaders.add(null);
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		Identifier id = shaders.get(getSetting(0).asMode().getMode());

		if (!Objects.equals(id, lastId)) {
			lastId = id;

			if (id != null) {
				mc.gameRenderer.setPostProcessor(id);
			} else {
				mc.gameRenderer.clearPostProcessor();
			}
		}
	}

	@Override
	public void onDisable(boolean inWorld) {
		if (lastId != null) {
			mc.gameRenderer.clearPostProcessor();
		}

		lastId = null;
		super.onDisable(inWorld);
	}

}
