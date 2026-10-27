/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import java.util.Collections;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventRenderTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

	@Inject(method = "drawMouseoverTooltip", at = @At("HEAD"))
	private void drawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
		EventRenderTooltip event = new EventRenderTooltip((Screen) (Object) this, context,
				Collections.emptyList(), x, y, x, y);
		BleachHack.eventBus.post(event);
	}
}
