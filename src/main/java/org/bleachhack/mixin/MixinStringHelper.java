/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.NoKeyBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.StringHelper;

@Mixin(StringHelper.class)
public class MixinStringHelper {

	@Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
	private static void isValidChar(int chr, CallbackInfoReturnable<Boolean> ci) {
		Module noKeyBlock = ModuleManager.getModule(NoKeyBlock.class);

		if (noKeyBlock.isEnabled()) {
			ci.setReturnValue((noKeyBlock.getSetting(0).asToggle().getState() || chr != 167)
					&& (noKeyBlock.getSetting(1).asToggle().getState() || chr >= ' ')
					&& (noKeyBlock.getSetting(2).asToggle().getState() || chr != 127));
		}
	}
}
