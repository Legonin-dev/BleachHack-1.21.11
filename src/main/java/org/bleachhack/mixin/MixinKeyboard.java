/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventKeyPress;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.setting.option.Option;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyInput;

@Mixin(Keyboard.class)
public class MixinKeyboard {

	@Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
	private void onKeyEvent(long windowPointer, int action, KeyInput input, CallbackInfo callbackInfo) {
		int key = input.key();
		int scanCode = input.scancode();

		if (key < 0) {
			return;
		}

		EventKeyPress.Global event = new EventKeyPress.Global(key, scanCode, action, input.modifiers());
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callbackInfo.cancel();
			return;
		}

		if (action == GLFW.GLFW_PRESS && MinecraftClient.getInstance().currentScreen == null) {
			if (Option.CHAT_QUICK_PREFIX.getValue() && Command.getPrefix().length() == 1 && key == Command.getPrefix().charAt(0)) {
				MinecraftClient.getInstance().setScreen(new ChatScreen(Command.getPrefix(), false));
			}

			ModuleManager.handleKey(key);

			EventKeyPress.InWorld inWorldEvent = new EventKeyPress.InWorld(key, scanCode);
			BleachHack.eventBus.post(inWorldEvent);

			if (inWorldEvent.isCancelled()) {
				callbackInfo.cancel();
			}
		}
	}
}
